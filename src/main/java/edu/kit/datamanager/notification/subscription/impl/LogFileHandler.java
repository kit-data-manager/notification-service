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
import static edu.kit.datamanager.notification.subscription.impl.EmailHandler.DETAILS_KEY;
import static edu.kit.datamanager.notification.subscription.impl.EmailHandler.EMAIL_KEY;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author jejkal
 */
@Component
public class LogFileHandler implements ISubscriptionHandler{

  @Autowired
  private Logger LOG;
  public final static String FILENAME_KEY = "filename";

  private final HandlerProperties properties = HandlerProperties.create().addProperty(FILENAME_KEY, "The local filename the notifications are written to.");

  @Override
  public String getSubscriptionName(){
    return "logfile";
  }

  @Override
  public HandlerProperties getSubscriptionProperties(){
    return properties;
  }

  @Override
  public boolean configure(){
    return true;
  }

  @Override
  public boolean checkSubscription(Subscription subscription){
    try{
      Map<String, String> props = subscription.getSubscriptionPropertiesAsMap();
      if(!props.containsKey(FILENAME_KEY)){
        return false;
      }
    } catch(IOException ex){
      return false;
    }

    return true;
  }

  @Override
  public boolean handleNotifications(Notification[] notifications, Map<String, String> properties){
    String filename = properties.get(FILENAME_KEY);
    try{
      Path file = Paths.get(filename);
      if(!Files.exists(file)){
        LOG.trace("Creating notification file at {}.", file);
        Files.write(file, "severity;content;createdAt;senderType;senderId;expiresAt\n".getBytes(), StandardOpenOption.CREATE);
      }
      if(Files.isRegularFile(file) && Files.isWritable(file)){
        for(Notification n : notifications){
          Files.write(file, buildLine(n).getBytes(), StandardOpenOption.APPEND);
        }
      }
    } catch(IOException ex){
      LOG.error("Failed to handle notifications. Unable to write to file " + filename, ex);
      return false;
    }
    return true;

  }

  private String buildLine(Notification notification){
    StringBuilder builder = new StringBuilder();
    builder = builder.append(notification.getSeverity().toString()).append(";").
            append(notification.getContent()).append(";").
            append(DateTimeFormatter.ISO_DATE_TIME.withZone(ZoneOffset.UTC).format(notification.getCreatedAt())).append(";").
            append(notification.getSenderType()).append(";").
            append(notification.getSenderId()).append(";").
            append(notification.getSenderType()).append(";").
            append((notification.getExpiresAt() == null) ? "" : DateTimeFormatter.ISO_DATE_TIME.withZone(ZoneOffset.UTC).format(notification.getExpiresAt())).append("\n");
    return builder.toString();
  }

}
