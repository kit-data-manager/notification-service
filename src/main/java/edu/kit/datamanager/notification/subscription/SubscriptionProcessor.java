/*
 * Copyright 2019 Karlsruhe Institute of Technology.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.kit.datamanager.notification.subscription;

import edu.kit.datamanager.notification.dao.INotificationDao;
import edu.kit.datamanager.notification.dao.ISubscriptionDao;
import edu.kit.datamanager.notification.dao.spec.CurrentSubscriptionsSpec;
import edu.kit.datamanager.notification.dao.spec.NotificationCreationDateSpec;
import edu.kit.datamanager.notification.dao.spec.NotificationReceipientIdSpec;
import edu.kit.datamanager.notification.domain.Notification;
import edu.kit.datamanager.notification.domain.Subscription;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 *
 * @author jejkal
 */
@Component
public class SubscriptionProcessor{

  private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionProcessor.class);

  private final ISubscriptionHandler[] subscriptionHandlers;
  private final Map<String, ISubscriptionHandler> endorsedSubscriptions = new HashMap<>();
  private boolean INITIALIZED = false;
  private boolean NO_SUBSCRIPTION_HANDLER_WARNING_EMITTED = false;
  private final INotificationDao notificationDao;
  private final ISubscriptionDao subscriptionDao;

  @Autowired
  public SubscriptionProcessor(ISubscriptionDao subscriptionDao, INotificationDao notificationDao, Optional<ISubscriptionHandler[]> subscriptionHandlers){
    this.notificationDao = notificationDao;
    this.subscriptionDao = subscriptionDao;
    if(subscriptionHandlers.isPresent()){
      this.subscriptionHandlers = subscriptionHandlers.get();
    } else{
      this.subscriptionHandlers = null;
    }
  }

  @Scheduled(fixedRateString = "${repo.schedule.rate}")
  public void receiveNextMessage(){
    if(subscriptionHandlers == null){
      if(!NO_SUBSCRIPTION_HANDLER_WARNING_EMITTED){
        LOGGER.warn("No subscription handlers registered. Skip receiving all notifications.");
        NO_SUBSCRIPTION_HANDLER_WARNING_EMITTED = true;
      }
      return;
    }

    if(!INITIALIZED){
      //if not initialized, check all handlers for endorsement
      //this is done before handling the first message as at this point, the repository is running in any case, also if the receiver is part of the repository
      //this allows the handler to check for the repository
      for(ISubscriptionHandler handler : subscriptionHandlers){
        LOGGER.trace("Trying to configure handler {}.", handler.getSubscriptionName());
        if(handler.configure()){
          LOGGER.trace("Adding handler {} to list of endorsed handlers.", handler.getSubscriptionName());
          endorsedSubscriptions.put(handler.getSubscriptionName(), handler);
        } else{
          LOGGER.warn("Dropping handler {} due to misconfiguration.", handler.getSubscriptionName());
        }
      }
      INITIALIZED = true;
    }

    List<Subscription> subscriptions = subscriptionDao.findAll(CurrentSubscriptionsSpec.toSpecification(Arrays.asList(endorsedSubscriptions.keySet().toArray(new String[]{}))));

    LOGGER.trace("Obtaining all subscriptions.");
    Map<String, List<Subscription>> subscriptionMap = new HashMap<>();
    subscriptions.forEach((subscription) -> {
      List<Subscription> subscriptionList = subscriptionMap.get(subscription.getReceipientId());
      if(subscriptionList == null){
        subscriptionList = new ArrayList<>();
        subscriptionMap.put(subscription.getReceipientId(), subscriptionList);
      }
      if(subscription.getDisabled() == null || !subscription.getDisabled()){
        subscriptionList.add(subscription);
      }
    });

    LOGGER.trace("Handling subscriptions for {} receipient(s).", subscriptionMap.size());
    for(Entry<String, List<Subscription>> entry : subscriptionMap.entrySet()){
      List<Subscription> subscriptionsByReceipient = entry.getValue();
      for(Subscription subscription : subscriptionsByReceipient){
        if(subscription.getFiresNext() != null && subscription.getFiresNext().isAfter(Instant.now())){
          LOGGER.trace("Subscription {} is not fired before {}. Continue.", subscription.getFiresNext());
          continue;
        }

        ISubscriptionHandler handler = endorsedSubscriptions.get(subscription.getSubscriptionName());
        List<Notification> notifications = notificationDao.findAll(NotificationReceipientIdSpec.toSpecification(entry.getKey(), true).and(NotificationCreationDateSpec.toSpecification(subscription.getFiredLast(), null)));
        if(notifications.isEmpty()){
          LOGGER.trace("No notifications for receipient {} found.", entry.getKey());
          continue;
        }
        if(handler != null){
          try{
            if(handler.handleNotifications(notifications.toArray(new Notification[]{}), subscription.getSubscriptionPropertiesAsMap())){
              //success
              LOGGER.trace("Successfully submitted {} notifications via subscription {} to {}. Updating subscription timestamps.", notifications.size(), subscription.getSubscriptionName(), subscription.getReceipientId());
              subscription.setFiredLast(Instant.now());

              switch(subscription.getFrequency()){
                case HOURLY:
                  subscription.setFiresNext(Instant.now().plus(1, ChronoUnit.HOURS));
                  break;
                case DAILY:
                  subscription.setFiresNext(Instant.now().plus(1, ChronoUnit.DAYS));
                  break;
                case LIVE:
                  subscription.setFiresNext(Instant.now());
                  break;
              }
            } else{
              //error
              LOGGER.error("Failed to submit notifications via subscription {} to {}. Handler returned 'false'.", subscription.getSubscriptionName(), subscription.getReceipientId());
            }
          } catch(IOException ex){
            //error
            LOGGER.error("Failed to submit notifications via subscription {} to {}. Probably, subscription properties are misconfigured.", ex);
          }
        }
      }
      LOGGER.trace("Persisting updated subscriptions.");
      subscriptionDao.saveAll(subscriptionsByReceipient);
    }
    LOGGER.trace("Subscriptions handled.");
  }

}
