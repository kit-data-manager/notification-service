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

import edu.kit.datamanager.notification.dao.ISubscriptionDao;
import edu.kit.datamanager.notification.domain.HandlerProperties;
import edu.kit.datamanager.notification.domain.Subscription;
import edu.kit.datamanager.notification.subscription.ISubscriptionHandler;
import edu.kit.datamanager.notification.web.ISubscriptionController;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
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
@RequestMapping(value = "/api/v1/subscriptions")
@Schema(description = "Notification Subscription Management")
public class SubscriptionController implements ISubscriptionController{

  @Autowired
  private Logger LOG;

  @Autowired
  private final ISubscriptionDao subscriptionDao;

  @Autowired
  private final ISubscriptionHandler[] subscriptionHandlers;

  /**
   * Default constructor.
   *
   * @param subscriptionDao Subscription Dao added e.g. via dependency
   * injection.
   *
   */
  public SubscriptionController(ISubscriptionDao subscriptionDao, final ISubscriptionHandler[] subscriptionHandlers){
    super();
    this.subscriptionDao = subscriptionDao;
    this.subscriptionHandlers = subscriptionHandlers;
  }

  @Override
  public ResponseEntity<Subscription> create(
          @RequestBody Subscription subscription,
          WebRequest request,
          HttpServletResponse response){
    LOG.trace("Calling create({}).", subscription);

    String name = subscription.getSubscriptionName();
    ISubscriptionHandler selectedHandler = null;
    for(ISubscriptionHandler handler : subscriptionHandlers){
      if(handler.getSubscriptionName().equals(name)){
        selectedHandler = handler;
        break;
      }
    }

    if(selectedHandler == null){
      return new ResponseEntity("Invalid subscription handler " + name + " provided.", HttpStatus.BAD_REQUEST);
    }

    if(subscription.getReceipientId() == null){
      return new ResponseEntity("Mandatory attribute receipientId is missing.", HttpStatus.BAD_REQUEST);
    }

    if(!selectedHandler.checkSubscription(subscription)){
      return new ResponseEntity("Missing or invalid attribute in subscription properties.", HttpStatus.BAD_REQUEST);
    }

    if(subscription.getFrequency() == null){
      LOG.trace("No frequency provided. Settings default frequency {}.", Subscription.FREQUENCY.HOURLY);
      subscription.setFrequency(Subscription.FREQUENCY.HOURLY);
    }

    subscription.setId(null);
    if(subscription.getDisabled() == null){
      LOG.trace("No disabled flag provided, setting default value {}.", Boolean.FALSE);
      subscription.setDisabled(Boolean.FALSE);
    }
    //setting firedLast and firesNext in order not to send all old notifications in the first cycle
    subscription.setFiredLast(Instant.now().truncatedTo( ChronoUnit.MILLIS ));
    subscription.setFiresNext(Instant.now().truncatedTo( ChronoUnit.MILLIS ));

    LOG.trace("Persisting new subscription.");
    subscription = subscriptionDao.save(subscription);
    LOG.trace("Returning persisted subscription.");
    String uriLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(this.getClass()).getById(Long.toString(subscription.getId()), 1l, request, response)).toString();
    LOG.trace("Created resource link is: {}", uriLink);
    return ResponseEntity.created(URI.create(uriLink)).body(subscription);
  }

  @Override
  public ResponseEntity<Subscription> getById(
          @PathVariable(value = "id") String id,
          @RequestParam(value = "version", required = false) Long l,
          WebRequest wr,
          HttpServletResponse hsr
  ){
    LOG.trace("Calling getById({}).", id);

    Optional<Subscription> result = subscriptionDao.findById(Long.parseLong(id));

    if(result.isEmpty()){
      LOG.debug("No subscription with id {} found.", id);
      return new ResponseEntity("Subscription #" + id + " not found.", HttpStatus.NOT_FOUND);
    }

    return ResponseEntity.ok(result.get());

  }

  @Override
  public ResponseEntity<List<Subscription>> findAll(
          @Parameter(hidden = true) @RequestParam(name = "from", required = false) Instant instnt,
          @Parameter(hidden = true) @RequestParam(name = "until", required = false) Instant instnt1,
          Pageable pgbl,
          WebRequest request,
          HttpServletResponse response,
          UriComponentsBuilder ucb
  ){
    LOG.trace("Calling findAll().");

    Page<Subscription> page = subscriptionDao.findAll(pgbl);

    int index_start = page.getNumber() * pgbl.getPageSize();
    int index_end = index_start + pgbl.getPageSize();

    response.addHeader("Content-Range", (index_start + "-" + index_end + "/" + page.getTotalElements()));
    return ResponseEntity.ok(page.getContent());

  }

  @Override
  public ResponseEntity put(
          @PathVariable(value = "id") String id,
          @RequestBody Subscription subscription,
          WebRequest wr,
          HttpServletResponse hsr
  ){
    LOG.trace("Calling put({}, {}).", id, subscription);

    Optional<Subscription> result = subscriptionDao.findById(Long.parseLong(id));

    if(result.isEmpty()){
      LOG.debug("No subscription with id {} found.", id);
      return new ResponseEntity("Subscription #" + id + " not found.", HttpStatus.NOT_FOUND);
    }

    Subscription foundSubscription = result.get();
    LOG.trace("Updating frequency.");
    foundSubscription.setFrequency((subscription.getFrequency() != null) ? subscription.getFrequency() : foundSubscription.getFrequency());

    String name = subscription.getSubscriptionName();
    if(name != null){
      ISubscriptionHandler selectedHandler = null;
      for(ISubscriptionHandler handler : subscriptionHandlers){
        if(handler.getSubscriptionName().equals(name)){
          selectedHandler = handler;
          break;
        }
      }

      if(selectedHandler == null){
        return new ResponseEntity("Invalid subscription handler " + name + " provided.", HttpStatus.BAD_REQUEST);
      }

      LOG.trace("Updating subscription name.");
      foundSubscription.setSubscriptionName(name);
      LOG.trace("Updating subscription properties.");
      foundSubscription.setSubscriptionProperties((subscription.getSubscriptionProperties() != null) ? subscription.getSubscriptionProperties() : foundSubscription.getSubscriptionProperties());

      if(!selectedHandler.checkSubscription(subscription)){
        return new ResponseEntity("Missing or invalid attribute in subscription properties.", HttpStatus.BAD_REQUEST);
      }
    }
    LOG.trace("Updating receipient.");
    foundSubscription.setReceipientId((subscription.getReceipientId() != null) ? subscription.getReceipientId() : foundSubscription.getReceipientId());
    LOG.trace("Updating disabled flag.");
    foundSubscription.setDisabled((subscription.getDisabled() != null) ? subscription.getDisabled() : foundSubscription.getDisabled());
    foundSubscription = subscriptionDao.save(foundSubscription);

    return ResponseEntity.ok(foundSubscription);
  }

  @Override
  public ResponseEntity delete(
          @PathVariable(value = "id") String id,
          WebRequest wr,
          HttpServletResponse hsr
  ){
    LOG.trace("Calling delete({}).", id);

    Optional<Subscription> result = subscriptionDao.findById(Long.parseLong(id));

    if(result.isEmpty()){
      LOG.trace("No subscription with id {} found.", id);
    } else{
      LOG.trace("Deleting subscription with id {}.", id);
      subscriptionDao.delete(result.get());
    }

    return new ResponseEntity(HttpStatus.NO_CONTENT);
  }

  @Override
  public ResponseEntity getSubscriptionHandlerNamesAndProperties(){

    List<HandlerProperties> response = new ArrayList<>();

    for(ISubscriptionHandler handler : subscriptionHandlers){
      HandlerProperties props = handler.getSubscriptionProperties();
      props.setHandlerName(handler.getSubscriptionName());
      response.add(props);
      // response.put(handler.getSubscriptionName(), handler.getSubscriptionProperties());
    }

    return ResponseEntity.ok(response);
  }

}
