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
package edu.kit.datamanager.notification.messaging;

import edu.kit.datamanager.entities.messaging.BasicMessage;
import edu.kit.datamanager.messaging.client.handler.IMessageHandler;
import edu.kit.datamanager.notification.dao.INotificationDao;
import edu.kit.datamanager.notification.domain.Notification;
import edu.kit.datamanager.notification.domain.messaging.NotificationMessage;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author jejkal
 */
@Component
public class NotificationMessageHandler implements IMessageHandler{

  @Autowired
  private Logger LOG;

  @Autowired
  private final INotificationDao notificationDao;

  public NotificationMessageHandler(INotificationDao notificationDao){
    this.notificationDao = notificationDao;
  }

  @Override
  public boolean configure(){
    return true;
  }

  @Override
  public RESULT handle(BasicMessage message){
    LOG.trace("Reconstructing notification from message {}.", message);
    Notification n = new Notification();
    n.setContent(message.getMetadata().get(NotificationMessage.CONTENT_KEY));
    n.setCreatedAt(Instant.ofEpochMilli(message.getTimestamp()));
    n.setExpiresAt(Instant.from(DateTimeFormatter.ISO_DATE_TIME.withZone(ZoneOffset.UTC).parse(message.getMetadata().get(NotificationMessage.EXPIRES_AT_KEY))));
    n.setRecognized(Boolean.FALSE);
    n.setReceipientId(message.getMetadata().get(NotificationMessage.RECEIPIENT_ID_KEY));
    n.setSenderId(message.getSender());
    n.setSenderType(Notification.SENDER_TYPE.valueOf(message.getMetadata().get(NotificationMessage.SENDER_TYPE_KEY)));
    n.setSeverity(Notification.SEVERITY.valueOf(message.getMetadata().get(NotificationMessage.SEVERITY_KEY)));
    LOG.trace("Persisting notification {}.", n);
    notificationDao.save(n);
    return RESULT.SUCCEEDED;
  }

}
