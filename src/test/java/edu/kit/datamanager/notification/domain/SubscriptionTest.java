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

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author jejkal
 */
public class SubscriptionTest{

  @Test
  public void testSubscriptionProperties(){
    Subscription s = new Subscription();
    s.setId(1l);
    s.setDisabled(Boolean.FALSE);
    s.setFiredLast(Instant.now().truncatedTo( ChronoUnit.MILLIS ));
    s.setFiresNext(Instant.now().truncatedTo( ChronoUnit.MILLIS ));
    s.setFrequency(Subscription.FREQUENCY.HOURLY);
    s.setReceipientId("someone");
    s.setSubscriptionName("sub1");

    Assert.assertEquals(1l, s.getId().longValue());
    Assert.assertFalse(s.getDisabled());
    Assert.assertNotNull(s.getFiredLast());
    Assert.assertNotNull(s.getFiresNext());
    Assert.assertEquals(Subscription.FREQUENCY.HOURLY, s.getFrequency());
    Assert.assertEquals("someone", s.getReceipientId());
    Assert.assertEquals("sub1", s.getSubscriptionName());

    Map<String, String> props = new HashMap<>();
    props.put("key", "value");
    try{
      s.setSubscriptionPropertiesFromMap(props);
      String sProps = s.getSubscriptionProperties();

      s.setSubscriptionProperties(sProps);
      Map<String, String> props2 = s.getSubscriptionPropertiesAsMap();

      Assert.assertTrue(props2.containsKey("key"));
      Assert.assertEquals(props.get("key"), props2.get("key"));

      //test with empty properties
      s = new Subscription();
      Assert.assertTrue(s.getSubscriptionPropertiesAsMap().isEmpty());
    } catch(IOException ex){
      ex.printStackTrace();
      Assert.fail("Test testSubscriptionProperties() failed.");
    }

  }
}
