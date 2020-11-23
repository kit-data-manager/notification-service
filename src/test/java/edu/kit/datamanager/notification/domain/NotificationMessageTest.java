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
package edu.kit.datamanager.notification.domain;

import edu.kit.datamanager.notification.domain.messaging.NotificationMessage;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author jejkal
 */
public class NotificationMessageTest{

  @Test
  public void testCreateNotificationMessage(){
    Instant t = Instant.now().truncatedTo( ChronoUnit.MILLIS );
    NotificationMessage msg = NotificationMessage.createMessage(NotificationMessage.ACTION.CREATE, "me", "A test", Notification.SEVERITY.INFO, "someone", Notification.SENDER_TYPE.USER, "me", t);

    Assert.assertEquals("notification", msg.getEntityName());
    Assert.assertEquals(NotificationMessage.ACTION.CREATE.toString(), msg.getAction());
    Assert.assertEquals("me", msg.getPrincipal());
    Assert.assertEquals("me", msg.getSender());

    Assert.assertEquals("A test", msg.getMetadata().get(NotificationMessage.CONTENT_KEY));
    Assert.assertEquals(Notification.SENDER_TYPE.USER.toString(), msg.getMetadata().get(NotificationMessage.SENDER_TYPE_KEY));
    Assert.assertEquals(Notification.SEVERITY.INFO.toString(), msg.getMetadata().get(NotificationMessage.SEVERITY_KEY));
    Assert.assertEquals("someone", msg.getMetadata().get(NotificationMessage.RECEIPIENT_ID_KEY));
    Assert.assertEquals(DateTimeFormatter.ISO_DATE_TIME.withZone(ZoneOffset.UTC).format(t), msg.getMetadata().get(NotificationMessage.EXPIRES_AT_KEY));

  }
}
