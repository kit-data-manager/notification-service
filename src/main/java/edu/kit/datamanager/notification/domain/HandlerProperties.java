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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import lombok.Data;

/**
 *
 * @author jejkal
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class HandlerProperties{

  private String handlerName;
  private Map<String, String> properties = new HashMap<>();

  HandlerProperties(){
  }

  public static HandlerProperties create(){
    return new HandlerProperties();
  }

  public HandlerProperties addProperty(String key, String description){
    properties.put(key, description);
    return this;
  }

  @JsonIgnore
  public Set<String> getKeys(){
    return properties.keySet();
  }

  @JsonIgnore
  public String getDescription(String key){
    return properties.get(key);
  }

}
