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
package edu.kit.datamanager.notification.dao;

import edu.kit.datamanager.notification.domain.Notification;
import edu.kit.datamanager.notification.domain.Notification.SEVERITY;
import edu.kit.datamanager.notification.util.NotificationTestUtil;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

/**
 *
 * @author jejkal
 */
@RunWith(SpringRunner.class)
@DataJpaTest
@ActiveProfiles("test")
public class NotificationDaoTest{

  @Autowired
  private INotificationDao dao;

  @Before
  public void prepare(){

  }

  @After
  public void cleanDb(){
    dao.deleteAll();
  }

  @Test
  public void testFindNotificationsById(){
    Notification created = NotificationTestUtil.createNotification(SEVERITY.INFO, "Success");
    created = dao.save(created);
    Optional<Notification> found = dao.findById(created.getId());
    Assert.assertEquals(created.getContent(), found.get().getContent());

    found = dao.findById(0l);
    Assert.assertTrue(found.isEmpty());
  }

  @Test
  public void testFindNotifications(){
    Notification n = NotificationTestUtil.createNotification(SEVERITY.INFO, "First");
    dao.save(n);
    n = NotificationTestUtil.createNotification(SEVERITY.INFO, "Second");
    dao.save(n);

    List<Notification> found = dao.findAll();
    Assert.assertEquals(2, found.size());
  }

  @Test
  public void testFindOrdered(){
    //sort by severity
    Notification n = NotificationTestUtil.createNotification(SEVERITY.INFO, "Success");
    dao.save(n);
    n = NotificationTestUtil.createNotification(SEVERITY.WARN, "Success");
    dao.save(n);

    List<Notification> found = dao.findAll(Sort.by("severity"));
    Assert.assertEquals(SEVERITY.INFO, found.get(0).getSeverity());
    Assert.assertEquals(SEVERITY.WARN, found.get(1).getSeverity());

    //sort by creation date
    cleanDb();
    n = NotificationTestUtil.createNotification(SEVERITY.INFO, "Last");
    dao.save(n);
    n = NotificationTestUtil.createNotification(SEVERITY.INFO, "First", Instant.ofEpochMilli(0));
    dao.save(n);
    found = dao.findAll(Sort.by("createdAt"));
    Assert.assertEquals("First", found.get(0).getContent());
    Assert.assertEquals("Last", found.get(1).getContent());
  }

}
