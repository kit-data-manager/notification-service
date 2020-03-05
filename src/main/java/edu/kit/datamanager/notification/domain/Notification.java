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
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import edu.kit.datamanager.util.json.CustomInstantDeserializer;
import edu.kit.datamanager.util.json.CustomInstantSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.time.Instant;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.Data;

/**
 *
 * @author jejkal
 */
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Notification element")
@Data
public class Notification implements Serializable{

  public enum SEVERITY{
    INFO,
    WARN,
    ERROR;
  }

  public enum SENDER_TYPE{
    USER,
    SYSTEM;
  }
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Schema(description = "The notification severity.", required = true)
  @Enumerated(EnumType.STRING)
  private SEVERITY severity;
  @Schema(description = "The id of the receipient, which can be a userId or a generic system user id.", required = true)
  private String receipientId;
  @Schema(description = "The notification content.", required = true)
  private String content;
  @Schema(description = "The date the notification was created.", example = "2017-05-10T10:41:00Z", required = true)
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
  @JsonDeserialize(using = CustomInstantDeserializer.class)
  @JsonSerialize(using = CustomInstantSerializer.class)
  private Instant createdAt;
  @Schema(description = "The date the notification expired, e.g. can be deleted.", example = "2017-05-10T10:41:00Z", required = true)
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
  @JsonDeserialize(using = CustomInstantDeserializer.class)
  @JsonSerialize(using = CustomInstantSerializer.class)
  private Instant expiresAt;
  @Schema(description = "The sender type, e.g. another user or the system.", required = true)
  @Enumerated(EnumType.STRING)
  private SENDER_TYPE senderType;
  @Schema(description = "The sender id, e.g. a user id or a system/service id.", required = true)
  private String senderId;
  @Schema(description = "A flag for marking the notification as read.", required = false)
  private Boolean recognized;

}
