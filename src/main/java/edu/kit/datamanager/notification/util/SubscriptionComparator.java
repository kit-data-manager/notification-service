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
package edu.kit.datamanager.notification.util;

import edu.kit.datamanager.notification.domain.Subscription;
import java.util.Comparator;

/**
 *
 * @author jejkal
 */
public class SubscriptionComparator implements Comparator<Subscription>{

  @Override
  public int compare(Subscription o1, Subscription o2){
    if((o1 == null || o1.getReceipientId() == null) && (o2 == null || o2.getReceipientId() == null)){
      return 0;
    } else if((o1 == null || o1.getReceipientId() == null) && (o2 != null && o2.getReceipientId() != null)){
      return 1;
    } else if((o1 != null && o1.getReceipientId() != null) && (o2 == null || o2.getReceipientId() == null)){
      return -1;
    }

    return String.CASE_INSENSITIVE_ORDER.compare(o1.getReceipientId(), o2.getReceipientId());
  }

}
