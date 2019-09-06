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
package edu.kit.datamanager.notification.subscription.impl;

import edu.kit.datamanager.notification.domain.HandlerProperties;
import edu.kit.datamanager.notification.domain.Notification;
import edu.kit.datamanager.notification.domain.Subscription;
import edu.kit.datamanager.notification.subscription.ISubscriptionHandler;
import java.io.IOException;
import java.util.Map;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 *
 * @author jejkal
 */
@Component
public class EmailHandler implements ISubscriptionHandler{

  private enum DETAILS{
    FULL,
    SHORT;
  }

  @Autowired
  private Logger LOG;
  @Autowired
  public JavaMailSender emailSender;

  public final static String EMAIL_KEY = "email";
  public final static String DETAILS_KEY = "details";

  private final HandlerProperties properties = HandlerProperties.create().
          addProperty(EMAIL_KEY, "The user email address the notitications are sent to.").
          addProperty(DETAILS_KEY, "The detail level of the email content, which is either FULL (list of notifications) or SHORT (number of notifications).");

  @Override
  public String getSubscriptionName(){
    return "email";
  }

  @Override
  public HandlerProperties getSubscriptionProperties(){
    return properties;
  }

  @Override
  public boolean checkSubscription(Subscription subscription){
    try{
      Map<String, String> props = subscription.getSubscriptionPropertiesAsMap();
      if(!props.containsKey(EMAIL_KEY) || !props.containsKey(DETAILS_KEY)){
        return false;
      }
      DETAILS.valueOf(props.get(DETAILS_KEY));
    } catch(IOException | IllegalArgumentException ex){
      return false;
    }

    return true;
  }

  @Override
  public boolean configure(){
    //read mail server config from application.properties_
    return true;
  }

  @Override
  public boolean handleNotifications(Notification[] notifications, Map<String, String> properties){
    SimpleMailMessage message = new SimpleMailMessage();
    message.setTo(properties.get(EMAIL_KEY));
    message.setSubject("New Notifications from KITDM Instance");
    message.setText("This works!");
    emailSender.send(message);
    return true;
  }

}
