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
package edu.kit.datamanager.notification.domain.messaging;

import edu.kit.datamanager.entities.messaging.BasicMessage;
import edu.kit.datamanager.notification.domain.Notification;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;

/**
 *
 * @author jejkal
 */
@Data
public class NotificationMessage extends BasicMessage{

  public enum ACTION{
    CREATE("create");

    private final String value;

    ACTION(String value){
      this.value = value;
    }

    public String getValue(){
      return value;
    }

    @Override
    public String toString(){
      return getValue();
    }
  }

  public final static String CONTENT_KEY = "content";
  public final static String SENDER_TYPE_KEY = "senderType";
  public final static String SEVERITY_KEY = "severity";
  public final static String RECEIPIENT_ID_KEY = "receipientId";
  public final static String EXPIRES_AT_KEY = "expiresAt";

  public static NotificationMessage createMessage(NotificationMessage.ACTION action, String principal, String content, Notification.SEVERITY severity, String receipientId, Notification.SENDER_TYPE senderType, String senderId, Instant expiresAt){
    NotificationMessage msg = new NotificationMessage();
    msg.setAction(action.getValue());
    msg.setPrincipal(principal);
    msg.setSender(senderId);
    msg.setCurrentTimestamp();

    Map<String, String> metadata = new HashMap<>();
    metadata.put(CONTENT_KEY, content);
    metadata.put(SENDER_TYPE_KEY, senderType.toString());
    metadata.put(SEVERITY_KEY, severity.toString());
    metadata.put(RECEIPIENT_ID_KEY, receipientId);
    metadata.put(EXPIRES_AT_KEY, DateTimeFormatter.ISO_DATE_TIME.withZone(ZoneOffset.UTC).format(expiresAt));
    msg.setMetadata(metadata);

    return msg;
  }

  @Override
  public String getEntityName(){
    return "notification";
  }
}
