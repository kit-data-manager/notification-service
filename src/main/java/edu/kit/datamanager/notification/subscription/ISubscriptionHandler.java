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
package edu.kit.datamanager.notification.subscription;

import edu.kit.datamanager.notification.domain.HandlerProperties;
import edu.kit.datamanager.notification.domain.Notification;
import edu.kit.datamanager.notification.domain.Subscription;
import java.util.Map;

/**
 *
 * @author jejkal
 */
public interface ISubscriptionHandler{

  /**
   * Returns the unique name of the subscription. The name can be used to
   * subscribe by the user and to identify the matching subscription.
   *
   * @return The subscription name.
   */
  default String getSubscriptionName(){
    return this.getClass().getSimpleName();
  }

  /**
   * Returns a HandlerProperties object containing required properties by the
   * subscription from the user, e.g. email address, including a short
   * decription in a human readable form.
   *
   * @return A properties object containing the property keys and a short, human
   * readable description.
   */
  HandlerProperties getSubscriptionProperties();

  /**
   * Check if the provided subscription can be processed by this handler, e.g.
   * if all properties are provided in a proper form.
   *
   * @param subscription The subscription to check.
   *
   * @return TRUE if the subscription can be handled.
   */
  boolean checkSubscription(Subscription subscription);

  /**
   * Configure the subscription.
   *
   * @return TRUE if configuration succeeded, FALSE otherwise.
   */
  boolean configure();

  /**
   * Handle a list of notifications, e.g. send them to the user.
   *
   * @param notifications The current list of processed notifications.
   * @param properties The user-specific properties holding all required
   * subscription property values.
   *
   * @return TRUE if the notifications were handled according to the
   * subscription's rules.
   */
  boolean handleNotifications(Notification[] notifications, Map<String, String> properties);

}
