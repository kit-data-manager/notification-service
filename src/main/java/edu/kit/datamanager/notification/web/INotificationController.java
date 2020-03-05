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
package edu.kit.datamanager.notification.web;

import edu.kit.datamanager.notification.domain.Notification;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.time.Instant;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.util.UriComponentsBuilder;

/**
 *
 * @author jejkal
 */
public interface INotificationController{

  @Operation(summary = "Create one or more notifications.")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "201", description = "Successfully created all notifications. The created notifications are returned in the response."),
    @ApiResponse(responseCode = "401", description = "Unauthorized. Request was not authorized.")})
  @RequestMapping(value = "/",
          produces = {"application/json"},
          method = RequestMethod.POST)
  ResponseEntity<List<Notification>> create(
          @Parameter(description = "The properties of the collection.", required = true) @Valid @RequestBody List<Notification> content,
          WebRequest request,
          HttpServletResponse response);

  @Operation(summary = "Get a resource by id.", description = "Obtain is single resource by its identifier. Depending on a user's role, accessing a specific resource may be allowed or forbidden.")
  @RequestMapping(value = {"/{id}"}, method = {RequestMethod.GET})
  @ResponseBody
  public ResponseEntity<Notification> getById(@Parameter(description = "The resource identifier.", required = true)
          @PathVariable(value = "id") String string, @Parameter(description = "The version of the resource, if supported.", required = false)
          @RequestParam(value = "version") Long l, WebRequest wr, HttpServletResponse hsr);

  @Operation(summary = "List resources by example.", description = "List all resources in a paginated and/or sorted form by example using an example document provided in the request body. The example is a normal instance of the resource. However, search-relevant top level primitives are marked as 'Searchable' within the implementation. For string values, '%' can be used as wildcard character. If the example document is omitted, the response is identical to listing all resources with the same pagination parameters. As well as listing of all resources, the number of total results might be affected by the caller's role.")
  @RequestMapping(value = {"/search"}, method = {RequestMethod.POST}, consumes = {"application/json"})
  @ResponseBody
  public ResponseEntity<List<Notification>> findByExample(
          @Parameter(description = "Json representation of the resource serving as example for the search operation. Typically, only first level primitive attributes are evaluated while building queries from examples.", required = true) @RequestBody Notification c,
          @Parameter(description = "The UTC time of the earliest update of a returned resource.", required = false) @RequestParam(name = "from", required = false) Instant createdFrom,
          @Parameter(description = "The UTC time of the latest update of a returned resource.", required = false) @RequestParam(name = "until", required = false) Instant createdUntil,
          Pageable pgbl, WebRequest wr,
          HttpServletResponse hsr,
          UriComponentsBuilder ucb);

  @Operation(summary = "Update the recognized status of a notification.")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Successful update. The updated notification is returned in the response."),
    @ApiResponse(responseCode = "401", description = "Unauthorized. Request was not authorized."),
    @ApiResponse(responseCode = "403", description = "Forbidden. May be returned, for example, if the caller has no sufficient privileges."),
    @ApiResponse(responseCode = "404", description = "Not found. The notification was not found.")})
  @RequestMapping(value = "/{id}/recognized",
          produces = {"application/json"},
          method = RequestMethod.PUT)
  ResponseEntity<Notification> setNotificationRecognized(
          @Parameter(description = "Identifier for the notification", required = true) @PathVariable("id") String id,
          @Parameter(description = "New value for recognized (either true or false).", required = true) @Valid @RequestBody Boolean status);

  @Operation(summary = "Delete a resource by id.", description = "Delete a single resource. Deleting a resource typically requires the caller to have ADMIN permissions. In some cases, deleting a resource can also be available for the owner or other privileged users or can be forbidden. For resources whose deletion may affect other resources or internal workflows, physical deletion might not be possible at all. In those cases, the resource might be disabled/hidden but not removed from the database. This can then happen optionally at a later point in time, either automatically or manually.")
  @RequestMapping(value = {"/{id}"}, method = {RequestMethod.DELETE})
  @ResponseBody
  public ResponseEntity delete(@Parameter(description = "The resource identifier.", required = true)
          @PathVariable(value = "id") String string, WebRequest wr, HttpServletResponse hsr);
}
