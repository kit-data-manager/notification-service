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

import edu.kit.datamanager.notification.domain.Subscription;
import java.time.Instant;
import java.util.List;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

/**
 *
 * @author jejkal
 */
public class CurrentSubscriptionsSpec{

  /**
   * Hidden constructor.
   */
  private CurrentSubscriptionsSpec(){
  }

  public static Specification<Subscription> toSpecification(final List<String> endorsedSubscriptionNames){

    return (Root<Subscription> root, CriteriaQuery<?> query, CriteriaBuilder builder) -> {
      return builder.and(builder.or(builder.lessThanOrEqualTo(root.get("firesNext"), Instant.now()), root.get("firesNext").isNull()), root.get("subscriptionName").in(endorsedSubscriptionNames));
    };
  }
}
