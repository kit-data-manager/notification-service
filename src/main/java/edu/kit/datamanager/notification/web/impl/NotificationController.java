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
package edu.kit.datamanager.notification.web.impl;

import edu.kit.datamanager.notification.dao.INotificationDao;
import edu.kit.datamanager.notification.dao.spec.NotificationCreationDateSpec;
import edu.kit.datamanager.notification.dao.spec.NotificationRecognizedSpec;
import edu.kit.datamanager.notification.dao.spec.NotificationReceipientIdSpec;
import edu.kit.datamanager.notification.dao.spec.NotificationSenderIdSpec;
import edu.kit.datamanager.notification.dao.spec.NotificationSenderTypeSpec;
import edu.kit.datamanager.notification.dao.spec.NotificationSeveritySpec;
import edu.kit.datamanager.notification.domain.Notification;
import edu.kit.datamanager.notification.web.INotificationController;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.util.UriComponentsBuilder;

/**
 *
 * @author jejkal
 */
@Controller
@RequestMapping(value = "/api/v1/notifications")
@Schema(description = "Notification Management")
public class NotificationController implements INotificationController{

  @Autowired
  private Logger LOG;

  @Autowired
  private final INotificationDao notificationDao;

  /**
   * Default constructor.
   *
   * @param notificationDao Notification Dao added e.g. via dependency
   * injection.
   *
   */
  public NotificationController(INotificationDao notificationDao){
    super();
    this.notificationDao = notificationDao;

  }

  @Override
  public ResponseEntity<List<Notification>> create(
          @Parameter(description = "The properties of the collection.", required = true) @Valid @RequestBody List<Notification> content,
          WebRequest request,
          HttpServletResponse response){
    LOG.trace("Calling create({}).", content);

    for(Notification notification : content){
      notification.setId(null);
      if(StringUtils.isEmpty(notification.getContent())){
        return new ResponseEntity("Empty notifications are not supported.", HttpStatus.BAD_REQUEST);
      }
      if(Objects.isNull(notification.getReceipientId())){
        return new ResponseEntity("Empty receipient is not allowed.", HttpStatus.BAD_REQUEST);
      }

      if(Objects.isNull(notification.getSeverity())){
        LOG.trace("Assign default severity {} to notification.", Notification.SEVERITY.INFO);
        notification.setSeverity(Notification.SEVERITY.INFO);
      }

      if(Objects.isNull(notification.getSenderType())){
        LOG.trace("Assign default sender type {} and senderId 'unknown' to notification.", Notification.SENDER_TYPE.SYSTEM);
        notification.setSenderType(Notification.SENDER_TYPE.SYSTEM);
        notification.setSenderId("unknown");
      } else{
        if(Objects.isNull(notification.getSenderId())){
          LOG.trace("Assign default sender id 'unknown' to notification.");
          notification.setSenderId("unknown");
        }
      }

      if(Objects.isNull(notification.getCreatedAt())){
        LOG.trace("Setting notification creation time to now().");
        notification.setCreatedAt(Instant.now());
      }

      LOG.trace("Resetting 'recognized' state.");
      notification.setRecognized(Boolean.FALSE);
    }

    LOG.trace("All provided notifications were checked. Persisting notifications.");
    content.forEach((n) -> {
      Notification persisted = notificationDao.save(n);
      n.setId(persisted.getId());
    });

    return new ResponseEntity<>(content, HttpStatus.CREATED);
  }

  @Override
  public ResponseEntity<Notification> getById(@PathVariable("id") final String id,
          @RequestParam(name = "version", required = false) final Long version,
          final WebRequest request,
          final HttpServletResponse response
  ){
    LOG.trace("Calling getById({}).", id);
    Optional<Notification> result = notificationDao.findById(Long.parseLong(id));

    if(result.isEmpty()){
      LOG.debug("No notification with id {} found.", id);
      return new ResponseEntity("Notification #" + id + " not found.", HttpStatus.NOT_FOUND);
    }

    return ResponseEntity.ok(result.get());
  }

  @Override
  public ResponseEntity<List<Notification>> findByExample(@Parameter(description = "Json representation of the resource serving as example for the search operation. Typically, only first level primitive attributes are evaluated while building queries from examples.", required = true) @RequestBody Notification notification,
          @RequestParam(name = "from", required = false) final Instant createdFrom,
          @RequestParam(name = "until", required = false) final Instant createdUntil,
          Pageable pgbl,
          WebRequest wr,
          HttpServletResponse response,
          UriComponentsBuilder ucb
  ){
    LOG.trace("Calling findByExample({}, {}, {}, {}).", notification, createdFrom, createdUntil, pgbl);
    LOG.trace("Building query spec list.");
    Specification<Notification> querySpec = NotificationReceipientIdSpec.toSpecification(notification.getReceipientId(), true).
            and(NotificationSenderIdSpec.toSpecification(notification.getSenderId(), true)).
            and(NotificationSenderTypeSpec.toSpecification(notification.getSenderType())).
            and(NotificationSeveritySpec.toSpecification(notification.getSeverity())).and(NotificationRecognizedSpec.toSpecification(notification.getRecognized())).
            and(NotificationCreationDateSpec.toSpecification(createdFrom, createdUntil));

    LOG.trace("Querying for result list.");
    Page<Notification> page = notificationDao.findAll(querySpec, pgbl);
    int index_start = page.getNumber() * pgbl.getPageSize();
    int index_end = index_start + pgbl.getPageSize();

    LOG.trace("Adding content range header with index_start {}, index_end {} and totalElements {}.", index_start, index_end, page.getTotalElements());
    response.addHeader("Content-Range", (index_start + "-" + index_end + "/" + page.getTotalElements()));
    return ResponseEntity.ok(page.getContent());
  }

  @Override
  public ResponseEntity<Notification> setNotificationRecognized(
          @Parameter(description = "Identifier for the notification", required = true) @PathVariable("id") String id,
          @Parameter(description = "New value for recognized (either true or false).", required = true, example = "TRUE") @Valid @RequestBody Boolean status
  ){
    LOG.trace("Calling setNotificationRecognized({}, {}).", id, status);

    Optional<Notification> result = notificationDao.findById(Long.parseLong(id));

    if(result.isEmpty()){
      LOG.debug("No notification with id {} found.", id);
      return new ResponseEntity("Notification #" + id + " not found.", HttpStatus.NOT_FOUND);
    }

    Notification notification = result.get();

    if(Objects.equals(status, notification.getRecognized())){
      LOG.trace("Recognized status is already '{}'. Skipping update.", status);
    } else{
      LOG.trace("Updating notification recognized status to {}.", status);
      notification.setRecognized(status);
      LOG.trace("Persisting notification with updated status.");
      notification = notificationDao.save(notification);
    }
    return ResponseEntity.ok(notification);
  }

  @Override
  public ResponseEntity delete(
          @PathVariable("id") final String id,
          final WebRequest request,
          final HttpServletResponse response
  ){
    LOG.trace("Calling delete({}).", id);

    Optional<Notification> result = notificationDao.findById(Long.parseLong(id));

    if(result.isEmpty()){
      LOG.trace("No notification with id {} found.", id);
    } else{
      LOG.trace("Deleting notification with id {}.", id);
      notificationDao.delete(result.get());
    }

    return new ResponseEntity(HttpStatus.NO_CONTENT);
  }
}
