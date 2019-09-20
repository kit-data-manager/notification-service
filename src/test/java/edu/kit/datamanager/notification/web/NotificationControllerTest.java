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
package edu.kit.datamanager.notification.web;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import edu.kit.datamanager.notification.dao.INotificationDao;
import edu.kit.datamanager.notification.domain.Notification;
import edu.kit.datamanager.notification.util.NotificationTestUtil;
import java.time.Instant;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.test.context.web.ServletTestExecutionListener;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 *
 * @author jejkal
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestExecutionListeners(listeners = {ServletTestExecutionListener.class,
  DependencyInjectionTestExecutionListener.class,
  DirtiesContextTestExecutionListener.class,
  TransactionalTestExecutionListener.class,
  WithSecurityContextTestExecutionListener.class})
@ActiveProfiles("test")
public class NotificationControllerTest{

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private INotificationDao dao;

  @Before
  public void setUp() throws JsonProcessingException{
    dao.deleteAll();
  }

  @Test
  public void testPostNotifications() throws Exception{
    Notification n1 = NotificationTestUtil.createNotification(Notification.SEVERITY.ERROR, "This is an error.");
    Notification n2 = NotificationTestUtil.createNotification(Notification.SEVERITY.INFO, "This is an info.");
    Notification n3 = NotificationTestUtil.createNotification(Notification.SEVERITY.WARN, "This is a warning.");

    ObjectMapper map = new ObjectMapper();
    MvcResult result = this.mockMvc.perform(post("/api/v1/notifications/").content(map.writeValueAsBytes(new Notification[]{n1, n2, n3})).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isCreated()).andReturn();

    Notification[] res = map.readValue(result.getResponse().getContentAsString(), Notification[].class);
    Assert.assertNotNull(res);
    Assert.assertEquals(3, res.length);
    Assert.assertEquals(Notification.SEVERITY.ERROR, res[0].getSeverity());
    Assert.assertEquals(Notification.SEVERITY.INFO, res[1].getSeverity());
    Assert.assertEquals(Notification.SEVERITY.WARN, res[2].getSeverity());
    Assert.assertNotNull(res[0].getId());
    Assert.assertNotNull(res[1].getId());
    Assert.assertNotNull(res[2].getId());
  }

  @Test
  public void testPostNotificationsWithoutContent() throws Exception{
    Notification n1 = new Notification();

    ObjectMapper map = new ObjectMapper();
    this.mockMvc.perform(post("/api/v1/notifications/").content(map.writeValueAsBytes(new Notification[]{n1})).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isBadRequest()).andReturn();
  }

  @Test
  public void testPostNotificationsWithoutReceipient() throws Exception{
    Notification n1 = new Notification();
    n1.setContent("This is a test.");
    ObjectMapper map = new ObjectMapper();
    this.mockMvc.perform(post("/api/v1/notifications/").content(map.writeValueAsBytes(new Notification[]{n1})).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isBadRequest()).andReturn();
  }

  @Test
  public void testPostMinimalNotifications() throws Exception{
    Notification n1 = new Notification();
    n1.setContent("This is a test.");
    n1.setReceipientId("self");

    ObjectMapper map = new ObjectMapper();
    MvcResult result = this.mockMvc.perform(post("/api/v1/notifications/").content(map.writeValueAsBytes(new Notification[]{n1})).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isCreated()).andReturn();
    Notification[] res = map.readValue(result.getResponse().getContentAsString(), Notification[].class);

    Assert.assertNotNull(res);
    Assert.assertEquals(1, res.length);
    Assert.assertEquals(Notification.SEVERITY.INFO, res[0].getSeverity());
    Assert.assertEquals("unknown", res[0].getSenderId());
  }

  @Test
  public void testPostNotificationsWithoutSenderId() throws Exception{
    Notification n1 = new Notification();
    n1.setContent("This is a test.");
    n1.setReceipientId("self");
    n1.setSenderType(Notification.SENDER_TYPE.SYSTEM);

    ObjectMapper map = new ObjectMapper();
    MvcResult result = this.mockMvc.perform(post("/api/v1/notifications/").content(map.writeValueAsBytes(new Notification[]{n1})).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isCreated()).andReturn();
    Notification[] res = map.readValue(result.getResponse().getContentAsString(), Notification[].class);
    Assert.assertNotNull(res);
    Assert.assertEquals(1, res.length);
    Assert.assertEquals(Notification.SENDER_TYPE.SYSTEM, res[0].getSenderType());
    Assert.assertEquals("unknown", res[0].getSenderId());
  }

  @Test
  public void testGetById() throws Exception{
    Notification n1 = NotificationTestUtil.createNotification(Notification.SEVERITY.ERROR, "This is an error.");
    n1 = dao.save(n1);

    ObjectMapper map = new ObjectMapper();
    MvcResult result = this.mockMvc.perform(get("/api/v1/notifications/" + n1.getId()).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isOk()).andReturn();

    Notification res = map.readValue(result.getResponse().getContentAsString(), Notification.class);
    Assert.assertNotNull(res);
    Assert.assertEquals(n1.getContent(), res.getContent());
    Assert.assertEquals(n1.getId(), res.getId());
  }

  @Test
  public void testGetByInvalidId() throws Exception{
    this.mockMvc.perform(get("/api/v1/notifications/666").contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isNotFound()).andReturn();
  }

  @Test
  public void testSetNotificationRecognized() throws Exception{
    Notification n1 = NotificationTestUtil.createNotification(Notification.SEVERITY.ERROR, "This is an error.");
    n1 = dao.save(n1);

    ObjectMapper map = new ObjectMapper();
    MvcResult result = this.mockMvc.perform(put("/api/v1/notifications/" + n1.getId() + "/recognized").content(map.writeValueAsBytes(Boolean.TRUE.toString())).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isOk()).andReturn();

    Notification res = map.readValue(result.getResponse().getContentAsString(), Notification.class);
    Assert.assertNotNull(res);
    Assert.assertTrue(res.getRecognized());
  }

  @Test
  public void testSetNotificationRecognizedWithoutBody() throws Exception{
    this.mockMvc.perform(put("/api/v1/notifications/666/recognized").contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isBadRequest()).andReturn();
  }

  @Test
  public void testSetNotificationRecognizedWithInvalidNotification() throws Exception{
    ObjectMapper map = new ObjectMapper();
    MvcResult result = this.mockMvc.perform(put("/api/v1/notifications/666/recognized").content(map.writeValueAsBytes(Boolean.TRUE.toString())).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isNotFound()).andReturn();
  }

  @Test
  public void testSetNotificationRecognizedTwice() throws Exception{
    Notification n1 = NotificationTestUtil.createNotification(Notification.SEVERITY.ERROR, "This is an error.");
    n1 = dao.save(n1);

    ObjectMapper map = new ObjectMapper();
    MvcResult result = this.mockMvc.perform(put("/api/v1/notifications/" + n1.getId() + "/recognized").content(map.writeValueAsBytes(Boolean.TRUE.toString())).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isOk()).andReturn();

    Notification res = map.readValue(result.getResponse().getContentAsString(), Notification.class);
    Assert.assertNotNull(res);
    Assert.assertTrue(res.getRecognized());

    result = this.mockMvc.perform(put("/api/v1/notifications/" + n1.getId() + "/recognized").content(map.writeValueAsBytes(Boolean.TRUE.toString())).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isOk()).andReturn();
    res = map.readValue(result.getResponse().getContentAsString(), Notification.class);
    Assert.assertNotNull(res);
    Assert.assertTrue(res.getRecognized());
  }

  @Test
  public void testDeleteNotification() throws Exception{
    Notification n1 = NotificationTestUtil.createNotification(Notification.SEVERITY.ERROR, "This is an error.");
    n1 = dao.save(n1);

    //delete notification
    this.mockMvc.perform(delete("/api/v1/notifications/" + n1.getId()).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isNoContent()).andReturn();
    //second time, same result
    this.mockMvc.perform(delete("/api/v1/notifications/" + n1.getId()).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isNoContent()).andReturn();
    //notification is gone
    this.mockMvc.perform(get("/api/v1/notifications/" + n1.getId()).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isNotFound()).andReturn();
  }

  @Test
  public void testFindByExample() throws Exception{
    Notification n1 = NotificationTestUtil.createNotification(Notification.SEVERITY.ERROR, "This is an error.");
    n1.setReceipientId("me");
    n1.setRecognized(true);
    n1.setSenderId("me");
    n1.setSenderType(Notification.SENDER_TYPE.USER);
    n1 = dao.save(n1);

    Notification n2 = NotificationTestUtil.createNotification(Notification.SEVERITY.ERROR, "This is an error.");
    n2.setReceipientId("not_me");
    n2.setRecognized(false);
    n2.setSenderId("not_me");
    n2.setSenderType(Notification.SENDER_TYPE.SYSTEM);
    n2 = dao.save(n2);

    Notification n3 = NotificationTestUtil.createNotification(Notification.SEVERITY.INFO, "This is an info.");
    n3.setReceipientId("not_me");
    n3.setRecognized(false);
    n3.setSenderId("not_me");
    n3.setSenderType(Notification.SENDER_TYPE.SYSTEM);
    n3 = dao.save(n3);

    ObjectMapper map = createObjectMapper();

    Notification template = new Notification();
    template.setSeverity(Notification.SEVERITY.ERROR);
    template.setRecognized(null);
    MvcResult result = this.mockMvc.perform(post("/api/v1/notifications/search").content(map.writeValueAsBytes(template)).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isOk()).andReturn();
    Notification[] res = map.readValue(result.getResponse().getContentAsString(), Notification[].class);

    Assert.assertNotNull(res);
    Assert.assertEquals(2, res.length);
    Assert.assertEquals(Notification.SEVERITY.ERROR, res[0].getSeverity());
    Assert.assertEquals(Notification.SEVERITY.ERROR, res[1].getSeverity());

    template.setReceipientId("me");
    template.setRecognized(Boolean.TRUE);
    template.setSenderId("me");
    template.setSenderType(Notification.SENDER_TYPE.USER);

    result = this.mockMvc.perform(post("/api/v1/notifications/search").content(map.writeValueAsBytes(template)).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isOk()).andReturn();
    res = map.readValue(result.getResponse().getContentAsString(), Notification[].class);

    Assert.assertNotNull(res);
    Assert.assertEquals(1, res.length);
    Assert.assertEquals(Notification.SEVERITY.ERROR, res[0].getSeverity());
    Assert.assertEquals("me", res[0].getReceipientId());

    //clear DB to do time range test
    dao.deleteAll();

    n1 = NotificationTestUtil.createNotification(Notification.SEVERITY.ERROR, "This is an error.");
    n1.setCreatedAt(Instant.ofEpochMilli(0));
    n2 = NotificationTestUtil.createNotification(Notification.SEVERITY.ERROR, "This is an error.");
    n2.setCreatedAt(Instant.ofEpochMilli(1000));
    n3 = NotificationTestUtil.createNotification(Notification.SEVERITY.ERROR, "This is an error.");
    n3.setCreatedAt(Instant.ofEpochMilli(2000));
    Notification n4 = NotificationTestUtil.createNotification(Notification.SEVERITY.ERROR, "This is an error.");
    n4.setCreatedAt(Instant.ofEpochMilli(3000));

    n1 = dao.save(n1);
    n2 = dao.save(n2);
    n3 = dao.save(n3);
    n4 = dao.save(n4);

    template = new Notification();

    //get all notifications from second 0 to 2 -> expect first three
    result = this.mockMvc.perform(post("/api/v1/notifications/search").content(map.writeValueAsBytes(template)).contentType(MediaType.APPLICATION_JSON).param("from", "1970-01-01T00:00:00Z").param("until", "1970-01-01T00:00:02Z")).andDo(print()).andExpect(status().isOk()).andReturn();
    res = map.readValue(result.getResponse().getContentAsString(), Notification[].class);
    Assert.assertEquals(3, res.length);

    //get all notifications from second 2 -> expect last one
    result = this.mockMvc.perform(post("/api/v1/notifications/search").content(map.writeValueAsBytes(template)).contentType(MediaType.APPLICATION_JSON).param("from", "1970-01-01T00:00:02Z")).andDo(print()).andExpect(status().isOk()).andReturn();
    res = map.readValue(result.getResponse().getContentAsString(), Notification[].class);
    Assert.assertEquals(1, res.length);
    Assert.assertEquals(n4.getId(), res[0].getId());

    //get all notifications until second 1 -> expect first one
    result = this.mockMvc.perform(post("/api/v1/notifications/search").content(map.writeValueAsBytes(template)).contentType(MediaType.APPLICATION_JSON).param("until", "1970-01-01T00:00:01Z")).andDo(print()).andExpect(status().isOk()).andReturn();
    res = map.readValue(result.getResponse().getContentAsString(), Notification[].class);
    Assert.assertEquals(1, res.length);
    Assert.assertEquals(n1.getId(), res[0].getId());

  }

  private ObjectMapper createObjectMapper(){
    return Jackson2ObjectMapperBuilder.json()
            .serializationInclusion(JsonInclude.Include.NON_EMPTY) // Don’t include null values
            .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS) //ISODate
            .modules(new JavaTimeModule())
            .build();
  }
}
