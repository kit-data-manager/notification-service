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

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import edu.kit.datamanager.util.json.CustomInstantDeserializer;
import edu.kit.datamanager.util.json.CustomInstantSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.IOException;
import java.io.Serializable;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author jejkal
 */
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel(description = "Subscription element")
@Data
public class Subscription implements Serializable{

  public enum FREQUENCY{
    LIVE,
    HOURLY,
    DAILY;
  }
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @ApiModelProperty(value = "The subscription name, e.g. the unique name of the subscription.", required = true)
  private String subscriptionName;
  @ApiModelProperty(value = "The receipient id, e.g. the receipientId of a notification matching this subscription.", required = true)
  private String receipientId;
  @ApiModelProperty(value = "JSON-serialized map of subscription-specific properties.", required = false)
  private String subscriptionProperties;
  @ApiModelProperty(value = "The frequency this subscription provides events.", required = false)
  @Enumerated(EnumType.STRING)
  private FREQUENCY frequency;
  @ApiModelProperty(value = "The last timestamp when this subscription was fired sending at least one notification.", required = false)
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
  @JsonDeserialize(using = CustomInstantDeserializer.class)
  @JsonSerialize(using = CustomInstantSerializer.class)
  private Instant firedLast;
  @ApiModelProperty(value = "The next timestamp this subscription will fire.", required = false)
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
  @JsonDeserialize(using = CustomInstantDeserializer.class)
  @JsonSerialize(using = CustomInstantSerializer.class)
  private Instant firesNext;
  @ApiModelProperty(value = "Flag to disable the subscription, e.g. temporarily.", required = false)
  private Boolean disabled;

  @JsonIgnore
  public Map<String, String> getSubscriptionPropertiesAsMap() throws IOException{
    if(StringUtils.isNotEmpty(subscriptionProperties)){
      return new ObjectMapper().readValue(subscriptionProperties, Map.class);
    }
    return new HashMap<>();
  }

  @JsonIgnore
  public void setSubscriptionPropertiesFromMap(Map<String, String> properties) throws JsonProcessingException{
    if(properties != null){
      this.subscriptionProperties = new ObjectMapper().writeValueAsString(properties);
    }
  }

}
