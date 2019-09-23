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

import edu.kit.datamanager.messaging.client.handler.IMessageHandler;
import edu.kit.datamanager.notification.dao.INotificationDao;
import edu.kit.datamanager.notification.domain.Notification;
import edu.kit.datamanager.notification.domain.messaging.NotificationMessage;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author jejkal
 */
@PowerMockRunnerDelegate(SpringJUnit4ClassRunner.class)
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class NotificationMessageHandlerTest{

  @Before
  public void setUp() throws Exception{
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testNotificationHandling(){
    INotificationDao dao = PowerMockito.mock(INotificationDao.class);
    NotificationMessageHandler handler = new NotificationMessageHandler(dao);

    Assert.assertTrue(handler.configure());
    final NotificationMessage msg = NotificationMessage.createMessage(NotificationMessage.ACTION.CREATE, "me", "A test", Notification.SEVERITY.INFO, "someone", Notification.SENDER_TYPE.USER, "me", Instant.now());

    Mockito.when(dao.save(Mockito.any())).then((iom) -> {
      Notification n = iom.getArgument(0);

      if(!n.getContent().equals(msg.getMetadata().get(NotificationMessage.CONTENT_KEY))){
        throw new IllegalArgumentException();
      }
      if(!n.getCreatedAt().equals(Instant.ofEpochMilli(msg.getTimestamp()))){
        throw new IllegalArgumentException();
      }
      if(!n.getExpiresAt().equals(Instant.from(DateTimeFormatter.ISO_DATE_TIME.withZone(ZoneOffset.UTC).parse(msg.getMetadata().get(NotificationMessage.EXPIRES_AT_KEY))))){
        throw new IllegalArgumentException();
      }
      if(!n.getRecognized().equals(Boolean.FALSE)){
        throw new IllegalArgumentException();
      }
      if(!n.getReceipientId().equals(msg.getMetadata().get(NotificationMessage.RECEIPIENT_ID_KEY))){
        throw new IllegalArgumentException();
      }
      if(!n.getSenderId().equals(msg.getSender())){
        throw new IllegalArgumentException();
      }

      if(!n.getSenderType().equals(Notification.SENDER_TYPE.valueOf(msg.getMetadata().get(NotificationMessage.SENDER_TYPE_KEY)))){
        throw new IllegalArgumentException();
      }
      if(!n.getSeverity().equals(Notification.SEVERITY.valueOf(msg.getMetadata().get(NotificationMessage.SEVERITY_KEY)))){
        throw new IllegalArgumentException();
      }

      return n;
    });
    try{
      Assert.assertEquals(IMessageHandler.RESULT.SUCCEEDED, handler.handle(msg));
    } catch(IllegalArgumentException ex){
      ex.printStackTrace();
      Assert.fail("Captured IllegalArgumentException. Notification mapping probably failed.");
    }
  }

}
