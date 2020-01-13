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
import edu.kit.datamanager.notification.dao.ISubscriptionDao;
import edu.kit.datamanager.notification.domain.HandlerProperties;
import edu.kit.datamanager.notification.domain.Subscription;
import edu.kit.datamanager.notification.subscription.impl.LogFileHandler;
import java.util.HashMap;
import java.util.Map;
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
public class SubscriptionControllerTest{

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private ISubscriptionDao dao;

  @Before
  public void setUp() throws JsonProcessingException{
    dao.deleteAll();
  }

  @Test
  public void testPostSubscription() throws Exception{
    Subscription subscription = new Subscription();
    subscription.setSubscriptionName("logfile");
    Map<String, String> props = new HashMap<>();
    props.put(LogFileHandler.FILENAME_KEY, "log.txt");
    subscription.setSubscriptionPropertiesFromMap(props);
    subscription.setReceipientId("admin");
    subscription.setFrequency(Subscription.FREQUENCY.HOURLY);

    ObjectMapper map = new ObjectMapper();
    MvcResult result = this.mockMvc.perform(post("/api/v1/subscriptions/").content(map.writeValueAsBytes(subscription)).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isCreated()).andReturn();

    Subscription res = map.readValue(result.getResponse().getContentAsString(), Subscription.class);
    Assert.assertNotNull(res);
    Assert.assertEquals(Boolean.FALSE, res.getDisabled());
    Assert.assertEquals(Subscription.FREQUENCY.HOURLY, res.getFrequency());
  }

  @Test
  public void testPostSubscriptionWithMissingElements() throws Exception{
    Subscription subscription = new Subscription();

    ObjectMapper map = new ObjectMapper();
    //no handler provided
    this.mockMvc.perform(post("/api/v1/subscriptions/").content(map.writeValueAsBytes(subscription)).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isBadRequest()).andReturn();

    //missing receipient
    subscription.setSubscriptionName("logfile");
    this.mockMvc.perform(post("/api/v1/subscriptions/").content(map.writeValueAsBytes(subscription)).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isBadRequest()).andReturn();

    //missing handler property
    subscription.setReceipientId("admin");
    this.mockMvc.perform(post("/api/v1/subscriptions/").content(map.writeValueAsBytes(subscription)).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isBadRequest()).andReturn();

    //missing frequency 
    Map<String, String> props = new HashMap<>();
    props.put(LogFileHandler.FILENAME_KEY, "log.txt");
    subscription.setSubscriptionPropertiesFromMap(props);
    MvcResult result = this.mockMvc.perform(post("/api/v1/subscriptions/").content(map.writeValueAsBytes(subscription)).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isCreated()).andReturn();
    Subscription res = map.readValue(result.getResponse().getContentAsString(), Subscription.class);
    Assert.assertNotNull(res);
    Assert.assertEquals(Subscription.FREQUENCY.HOURLY, res.getFrequency());
  }

  @Test
  public void testGetSubscriptionById() throws Exception{
    Subscription subscription = new Subscription();
    subscription.setDisabled(Boolean.FALSE);
    subscription.setSubscriptionName("logfile");
    Map<String, String> props = new HashMap<>();
    props.put(LogFileHandler.FILENAME_KEY, "log.txt");
    subscription.setSubscriptionPropertiesFromMap(props);
    subscription.setReceipientId("admin");
    subscription.setFrequency(Subscription.FREQUENCY.HOURLY);

    Subscription sub = dao.save(subscription);

    ObjectMapper map = new ObjectMapper();
    MvcResult result = this.mockMvc.perform(get("/api/v1/subscriptions/" + sub.getId()).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isOk()).andReturn();
    Subscription res = map.readValue(result.getResponse().getContentAsString(), Subscription.class);

    Assert.assertNotNull(res);
    Assert.assertEquals(subscription.getId(), res.getId());
    Assert.assertEquals(Boolean.FALSE, res.getDisabled());
    Assert.assertEquals(Subscription.FREQUENCY.HOURLY, res.getFrequency());
    Assert.assertEquals(subscription.getReceipientId(), res.getReceipientId());

    this.mockMvc.perform(get("/api/v1/subscriptions/666").contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isNotFound()).andReturn();
  }

  @Test
  public void testFindAllSubscriptions() throws Exception{
    Subscription subscription = new Subscription();
    subscription.setDisabled(Boolean.FALSE);
    subscription.setSubscriptionName("logfile");
    Map<String, String> props = new HashMap<>();
    props.put(LogFileHandler.FILENAME_KEY, "log.txt");
    subscription.setSubscriptionPropertiesFromMap(props);
    subscription.setReceipientId("admin");
    subscription.setFrequency(Subscription.FREQUENCY.HOURLY);

    Subscription sub = dao.save(subscription);

    ObjectMapper map = new ObjectMapper();
    MvcResult result = this.mockMvc.perform(get("/api/v1/subscriptions/").contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isOk()).andReturn();
    Subscription[] res = map.readValue(result.getResponse().getContentAsString(), Subscription[].class);

    Assert.assertNotNull(res);
    Assert.assertEquals(1, res.length);
    Assert.assertEquals(subscription.getId(), res[0].getId());
    Assert.assertEquals(Boolean.FALSE, res[0].getDisabled());
    Assert.assertEquals(Subscription.FREQUENCY.HOURLY, res[0].getFrequency());
    Assert.assertEquals(subscription.getReceipientId(), res[0].getReceipientId());
  }

  @Test
  public void testUpdateSubscription() throws Exception{
    Subscription subscription = new Subscription();
    subscription.setDisabled(Boolean.FALSE);
    subscription.setSubscriptionName("logfile");
    Map<String, String> props = new HashMap<>();
    props.put(LogFileHandler.FILENAME_KEY, "log.txt");
    subscription.setSubscriptionPropertiesFromMap(props);
    subscription.setReceipientId("admin");
    subscription.setFrequency(Subscription.FREQUENCY.HOURLY);
    Subscription sub = dao.save(subscription);

    Subscription newSub = new Subscription();
    newSub.setId(sub.getId());
    newSub.setDisabled(Boolean.TRUE);
    newSub.setSubscriptionName("logfile");
    props.put(LogFileHandler.FILENAME_KEY, "log2.txt");
    newSub.setSubscriptionPropertiesFromMap(props);
    newSub.setReceipientId("user");
    newSub.setFrequency(Subscription.FREQUENCY.LIVE);

    ObjectMapper map = new ObjectMapper();
    MvcResult result = this.mockMvc.perform(put("/api/v1/subscriptions/" + sub.getId()).content(map.writeValueAsBytes(newSub)).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isOk()).andReturn();
    Subscription res = map.readValue(result.getResponse().getContentAsString(), Subscription.class);

    Assert.assertNotNull(res);
    Assert.assertEquals(sub.getId(), res.getId());
    Assert.assertEquals(Boolean.TRUE, res.getDisabled());
    Assert.assertEquals(Subscription.FREQUENCY.LIVE, res.getFrequency());
    Assert.assertEquals("user", res.getReceipientId());
    Assert.assertEquals("log2.txt", res.getSubscriptionPropertiesAsMap().get(LogFileHandler.FILENAME_KEY));
  }

  @Test
  public void testUpdateSubscriptionWithInvalidId() throws Exception{
    Subscription subscription = new Subscription();
    ObjectMapper map = new ObjectMapper();
    this.mockMvc.perform(put("/api/v1/subscriptions/666").content(map.writeValueAsBytes(subscription)).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isNotFound()).andReturn();
  }

  @Test
  public void testUpdateSubscriptionWithInvalidHandler() throws Exception{
    Subscription subscription = new Subscription();
    subscription.setDisabled(Boolean.FALSE);
    subscription.setSubscriptionName("logfile");
    Map<String, String> props = new HashMap<>();
    props.put(LogFileHandler.FILENAME_KEY, "log.txt");
    subscription.setSubscriptionPropertiesFromMap(props);
    subscription.setReceipientId("admin");
    subscription.setFrequency(Subscription.FREQUENCY.HOURLY);
    Subscription sub = dao.save(subscription);

    Subscription newSub = new Subscription();
    newSub.setId(sub.getId());
    newSub.setDisabled(Boolean.TRUE);
    newSub.setSubscriptionName("invalidHandler");
    ObjectMapper map = new ObjectMapper();
    this.mockMvc.perform(put("/api/v1/subscriptions/" + sub.getId()).content(map.writeValueAsBytes(newSub)).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isBadRequest()).andReturn();
  }

  @Test
  public void testUpdateSubscriptionWithInvalidProperties() throws Exception{
    Subscription subscription = new Subscription();
    subscription.setDisabled(Boolean.FALSE);
    subscription.setSubscriptionName("logfile");
    Map<String, String> props = new HashMap<>();
    props.put(LogFileHandler.FILENAME_KEY, "log.txt");
    subscription.setSubscriptionPropertiesFromMap(props);
    subscription.setReceipientId("admin");
    subscription.setFrequency(Subscription.FREQUENCY.HOURLY);
    Subscription sub = dao.save(subscription);

    Subscription newSub = new Subscription();
    newSub.setId(sub.getId());
    newSub.setDisabled(Boolean.TRUE);
    newSub.setSubscriptionName("logfile");
    props.clear();
    props.put("badprop", "log2.txt");
    newSub.setSubscriptionPropertiesFromMap(props);

    ObjectMapper map = new ObjectMapper();
    this.mockMvc.perform(put("/api/v1/subscriptions/" + sub.getId()).content(map.writeValueAsBytes(newSub)).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isBadRequest()).andReturn();
  }

  @Test
  public void testDeleteSubscription() throws Exception{
    Subscription subscription = new Subscription();
    subscription.setDisabled(Boolean.FALSE);
    subscription.setSubscriptionName("logfile");
    Map<String, String> props = new HashMap<>();
    props.put(LogFileHandler.FILENAME_KEY, "log.txt");
    subscription.setSubscriptionPropertiesFromMap(props);
    subscription.setReceipientId("admin");
    subscription.setFrequency(Subscription.FREQUENCY.HOURLY);
    Subscription sub = dao.save(subscription);

    this.mockMvc.perform(delete("/api/v1/subscriptions/" + sub.getId()).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isNoContent()).andReturn();
    //second time, same result
    this.mockMvc.perform(delete("/api/v1/subscriptions/" + sub.getId()).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isNoContent()).andReturn();
    //invalid id, same result
    this.mockMvc.perform(delete("/api/v1/subscriptions/666").contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isNoContent()).andReturn();
    //totally invalid id, bad request
    this.mockMvc.perform(delete("/api/v1/subscriptions/abc").contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isBadRequest()).andReturn();

    //however, nothing will be left.
    Assert.assertEquals(0, dao.count());

  }

  @Test
  public void testGetSubscriptionNamesAndProps() throws Exception{
    MvcResult result = this.mockMvc.perform(get("/api/v1/subscriptions/handlers").contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isOk()).andReturn();
    ObjectMapper map = new ObjectMapper();
    HandlerProperties[] res = map.readValue(result.getResponse().getContentAsString(), HandlerProperties[].class);
    Assert.assertNotNull(res);
    Assert.assertFalse(res.length == 0);
    Assert.assertNotNull(res[0]);

    for(HandlerProperties props : res){
      if(props.getHandlerName().equals("logfile")){
        Assert.assertEquals(1, props.getKeys().size());
      }
    }
  }

//  private ObjectMapper createObjectMapper(){
//    return Jackson2ObjectMapperBuilder.json()
//            .serializationInclusion(JsonInclude.Include.NON_EMPTY) // Don’t include null values
//            .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS) //ISODate
//            .modules(new JavaTimeModule())
//            .build();
//  }
}
