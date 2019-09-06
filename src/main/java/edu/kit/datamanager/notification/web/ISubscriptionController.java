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

import com.github.fge.jsonpatch.JsonPatch;
import edu.kit.datamanager.controller.IGenericResourceController;
import edu.kit.datamanager.notification.domain.HandlerProperties;
import edu.kit.datamanager.notification.domain.Subscription;
import io.swagger.annotations.ApiOperation;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.util.UriComponentsBuilder;

/**
 *
 * @author jejkal
 */
public interface ISubscriptionController extends IGenericResourceController<Subscription>{

  @Override
  @ApiOperation(value = "", hidden = true)
  default ResponseEntity patch(String string, JsonPatch jp, WebRequest wr, HttpServletResponse hsr){
    return new ResponseEntity(HttpStatus.NOT_IMPLEMENTED);
  }

  @Override
  @ApiOperation(value = "", hidden = true)
  default ResponseEntity<List<Subscription>> findByExample(Subscription c, Instant instnt, Instant instnt1, Pageable pgbl, WebRequest wr, HttpServletResponse hsr, UriComponentsBuilder ucb){
    return new ResponseEntity(HttpStatus.NOT_IMPLEMENTED);
  }

  @ApiOperation(value = "Get all handler names and required properties.")
  @RequestMapping(value = "/handlers", method = RequestMethod.GET)
  @ResponseBody
  ResponseEntity<Map<String, HandlerProperties>> getSubscriptionHandlerNamesAndProperties();

}
