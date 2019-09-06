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
package edu.kit.datamanager.notification.dao.spec;

import edu.kit.datamanager.notification.domain.Notification;
import java.time.Instant;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

/**
 *
 * @author jejkal
 */
public class NotificationCreationDateSpec{

  /**
   * Hidden constructor.
   */
  private NotificationCreationDateSpec(){
  }

  public static Specification<Notification> toSpecification(Instant createdFrom, Instant createdUntil){
    Specification<Notification> newSpec = Specification.where(null);
    if(createdFrom == null && createdUntil == null){
      return newSpec;
    }

    return (Root<Notification> root, CriteriaQuery<?> query, CriteriaBuilder builder) -> {
      if(createdFrom != null && createdUntil != null){
        return builder.and(builder.between(root.get("createdAt"), createdFrom, createdUntil));
      } else if(createdFrom == null){
        return builder.and(builder.lessThan(root.get("createdAt"), createdUntil));
      }

      //otherwise, lastUpdateUntil is null
      return builder.and(builder.greaterThan(root.get("createdAt"), createdFrom), root.get("createdAt").isNotNull());
    };
  }
}
