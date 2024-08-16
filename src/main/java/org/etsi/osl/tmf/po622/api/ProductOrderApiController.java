/*-
 * ========================LICENSE_START=================================
 * org.etsi.osl.tmf.api
 * %%
 * Copyright (C) 2019 - 2021 openslice.io
 * %%
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
 * =========================LICENSE_END==================================
 */
package org.etsi.osl.tmf.po622.api;

import java.net.URI;
import java.security.Principal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.etsi.osl.model.nfv.UserRoleType;
import org.etsi.osl.tmf.common.model.UserPartRoleType;
import org.etsi.osl.tmf.po622.model.ProductOrder;
import org.etsi.osl.tmf.po622.model.ProductOrderCreate;
import org.etsi.osl.tmf.po622.model.ProductOrderUpdate;
import org.etsi.osl.tmf.po622.reposervices.ProductOrderRepoService;
import org.etsi.osl.tmf.so641.api.NotFoundException;
import org.etsi.osl.tmf.so641.model.ServiceOrder;
import org.etsi.osl.tmf.util.AddUserAsOwnerToRelatedParties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@jakarta.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen",
    date = "2020-10-30T10:29:21.184964400+02:00[Europe/Athens]")
@Controller("ProductOrderApiController622")
@RequestMapping("/productOrderingManagement/v4/")
public class ProductOrderApiController implements ProductOrderApi {

  private final ObjectMapper objectMapper;

  private final HttpServletRequest request;


  @Value("${kroki.serverurl}")
  private String KROKI_SERVERURL = "";

  @Autowired
  ProductOrderRepoService productOrderRepoService;

  @org.springframework.beans.factory.annotation.Autowired
  public ProductOrderApiController(ObjectMapper objectMapper, HttpServletRequest request) {
    this.objectMapper = objectMapper;
    this.request = request;
  }


  @PreAuthorize("hasAnyAuthority('ROLE_USER')")
  @Override
  public ResponseEntity<ProductOrder> createProductOrder(Principal principal,
      @Parameter(description = "The ProductOrder to be created",
          required = true) @Valid @RequestBody ProductOrderCreate productOrder) {

    try {
      // Object attr = request.getSession().getAttribute("SPRING_SECURITY_CONTEXT");
      // SecurityContextHolder.setContext( (SecurityContext) attr );


      log.info("authentication=  " + principal.toString());
      String extInfo = null;
      try {


        if (principal instanceof JwtAuthenticationToken) {

          JwtAuthenticationToken pr = (JwtAuthenticationToken) principal;

          Jwt lp = (Jwt) pr.getPrincipal();
          extInfo = lp.getClaimAsString("email");
          log.debug("extInfo=  " + extInfo);

          productOrder.setRelatedParty(AddUserAsOwnerToRelatedParties.addUser(principal.getName(),
              // user.getId()+"",
              principal.getName(), UserPartRoleType.REQUESTER, extInfo,
              productOrder.getRelatedParty()));
        } else if (principal instanceof UsernamePasswordAuthenticationToken) {
          productOrder.setRelatedParty(AddUserAsOwnerToRelatedParties.addUser(principal.getName(),
              // user.getId()+"",
              principal.getName(), UserPartRoleType.REQUESTER, extInfo,
              productOrder.getRelatedParty()));
        }


      } finally {

      }



      ProductOrder c = productOrderRepoService.addProductOrder(productOrder);

      return new ResponseEntity<ProductOrder>(c, HttpStatus.OK);


    } catch (NotFoundException e) {
      log.error("Couldn't create Service Order. ", e);
      return new ResponseEntity<ProductOrder>(HttpStatus.BAD_REQUEST);
    } catch (Exception e) {
      log.error("Couldn't serialize response for content type application/json", e);
      return new ResponseEntity<ProductOrder>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @Override
  @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
  public ResponseEntity<Void> deleteProductOrder(Principal principal, String id) {

    try {
      return new ResponseEntity<Void>(productOrderRepoService.deleteByUuid(id), HttpStatus.OK);
    } catch (Exception e) {
      log.error("Couldn't serialize response for content type application/json", e);
      return new ResponseEntity<Void>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @Override
  @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
  public ResponseEntity<List<ProductOrder>> listProductOrder(Principal principal,
      @Valid String fields, @Valid Integer offset, @Valid Integer limit, @Valid Date starttime,
      @Valid Date endtime) {
    try {

      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      log.debug("principal=  " + principal.toString());

      log.debug("principal ROLE_ADMIN =  " + authentication.getAuthorities()
          .contains(new SimpleGrantedAuthority(UserRoleType.ROLE_ADMIN.getValue())));
      log.debug("principal ROLE_NFV_DEVELOPER =  " + authentication.getAuthorities()
          .contains(new SimpleGrantedAuthority(UserRoleType.ROLE_NFV_DEVELOPER.getValue())));
      log.debug("principal ROLE_EXPERIMENTER =  " + authentication.getAuthorities()
          .contains(new SimpleGrantedAuthority(UserRoleType.ROLE_EXPERIMENTER.getValue())));

      if (authentication.getAuthorities()
          .contains(new SimpleGrantedAuthority(UserRoleType.ROLE_ADMIN.getValue()))) {

        return new ResponseEntity<List<ProductOrder>>(
            productOrderRepoService.findAll(fields, new HashMap<>(), starttime, endtime),
            HttpStatus.OK);
      } else {
        return new ResponseEntity<List<ProductOrder>>(
            productOrderRepoService.findAll(principal.getName(), UserPartRoleType.REQUESTER),
            HttpStatus.OK);
      }


    } catch (Exception e) {
      log.error("Couldn't serialize response for content type application/json", e);
      return new ResponseEntity<List<ProductOrder>>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }


  @Override
  @PreAuthorize("hasAnyAuthority('ROLE_USER')")
  public ResponseEntity<ProductOrder> patchProductOrder(Principal principal,
      @Valid ProductOrderUpdate body, String id) {
    ProductOrder c = productOrderRepoService.updateProductOrder(id, body);

    return new ResponseEntity<ProductOrder>(c, HttpStatus.OK);
  }


  @Override
  @PreAuthorize("hasAnyAuthority('ROLE_USER')")
  public ResponseEntity<ProductOrder> retrieveProductOrder(Principal principal, String id,
      @Valid String fields) {
    try {

      return new ResponseEntity<ProductOrder>( productOrderRepoService.findByUuid( id ), HttpStatus.OK);
  } catch ( Exception e) {
      log.error("Couldn't serialize response for content type application/json", e);
      return new ResponseEntity<ProductOrder>(HttpStatus.INTERNAL_SERVER_ERROR);
  }
  }

  @Override
  @PreAuthorize("hasAnyAuthority('ROLE_USER')")
  public ResponseEntity<Void> getImageProductOrderItemRelationshipGraph(String id, String itemid) {
    String encodedDiagram =
        productOrderRepoService.getImageProductOrderItemRelationshipGraph(id, itemid);

    // consider redirect to kroki..id
    return ResponseEntity.status(HttpStatus.FOUND)
        .location(URI.create(KROKI_SERVERURL + "/blockdiag/svg/" + encodedDiagram)).build();
    // return null;
  }

  @Override
  @PreAuthorize("hasAnyAuthority('ROLE_USER')")
  public ResponseEntity<Void> getImageProductOrderNotesGraph(String id) {
    String encodedDiagram = productOrderRepoService.getImageProductOrderNotesGraph(id);

    // consider redirect to kroki..id
    return ResponseEntity.status(HttpStatus.FOUND)
        .location(URI.create(KROKI_SERVERURL + "/actdiag/svg/" + encodedDiagram)).build();
  }
}
