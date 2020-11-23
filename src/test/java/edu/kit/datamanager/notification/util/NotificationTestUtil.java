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
package edu.kit.datamanager.notification.util;

import edu.kit.datamanager.notification.domain.Notification;
import edu.kit.datamanager.notification.domain.Notification.SEVERITY;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 *
 * @author jejkal
 */
public class NotificationTestUtil{

  NotificationTestUtil(){
  }

  public static Notification createNotification(SEVERITY severity, String content){
    return createNotification(severity, content, Instant.now().truncatedTo( ChronoUnit.MILLIS ));
  }

  public static Notification createNotification(SEVERITY severity, String content, Instant creationDate){
    Notification n = new Notification();
    n.setContent(content);
    n.setSeverity(severity);
    n.setCreatedAt(creationDate);
    n.setSenderType(Notification.SENDER_TYPE.SYSTEM);
    n.setSenderId("test");
    n.setReceipientId("test");
    return n;
  }
}
