/*-
 * ========================LICENSE_START=================================
 * org.etsi.osl.tmf.api
 * %%
 * Copyright (C) 2019 openslice.io
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
package org.etsi.osl.tmf.po622.reposervices;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.hibernate5.jakarta.Hibernate5JakartaModule;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etsi.osl.tmf.common.model.service.Note;
import org.etsi.osl.tmf.pcm620.model.ProductOffering;
import org.etsi.osl.tmf.pcm620.model.ProductOfferingRef;
import org.etsi.osl.tmf.pcm620.model.ProductSpecification;
import org.etsi.osl.tmf.pcm620.model.ProductSpecificationRef;
import org.etsi.osl.tmf.pcm620.reposervices.ProductOfferingRepoService;
import org.etsi.osl.tmf.pcm620.reposervices.ProductSpecificationRepoService;
import org.etsi.osl.tmf.po622.model.ProductOrder;
import org.etsi.osl.tmf.po622.model.ProductOrderCreate;
import org.etsi.osl.tmf.po622.model.ProductOrderItem;
import org.etsi.osl.tmf.po622.model.ProductOrderItemStateType;
import org.etsi.osl.tmf.po622.model.ProductOrderMapper;
import org.etsi.osl.tmf.po622.model.ProductOrderStateType;
import org.etsi.osl.tmf.po622.model.ProductOrderUpdate;
import org.etsi.osl.tmf.po622.repo.ProductOrderRepository;
import org.etsi.osl.tmf.rpm685.model.ResourcePoolMapper;
import org.etsi.osl.tmf.so641.api.NotFoundException;
import org.etsi.osl.tmf.so641.model.ServiceOrder;
import org.etsi.osl.tmf.so641.model.ServiceOrderStateType;
import org.hibernate.SessionFactory;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManagerFactory;
import jakarta.validation.Valid;

@Service
public class ProductOrderRepoService {

  private static final transient Log logger =
      LogFactory.getLog(ProductOrderRepoService.class.getName());

  @Autowired
  ObjectMapper objectMapper;

  @Autowired
  ProductOrderRepository productOrderRepo;


  @Autowired
  ProductSpecificationRepoService  productSpecificationRepoService;

  @Autowired
  ProductOfferingRepoService  productOfferingRepoService;


  private SessionFactory sessionFactory;


  @Autowired
  public ProductOrderRepoService(EntityManagerFactory factory) {
    if (factory.unwrap(SessionFactory.class) == null) {
      throw new NullPointerException("factory is not a hibernate factory");
    }
    this.sessionFactory = factory.unwrap(SessionFactory.class);
  }


  public List<ProductOrder> findAll() {

    // return (List<ProductOrder>) this.productOrderRepo.findAll();
    // return (List<ProductOrder>) this.productOrderRepo.findByOrderByOrderDateDesc();
    return (List<ProductOrder>) this.productOrderRepo.findAllOptimized();
  }


  public List<ProductOrder> findAllParams(Map<String, String> allParams) {
    logger.info("findAll with params:" + allParams.toString());     
    if ( ( allParams !=null)  &&  allParams.get("state") !=null) {
      ProductOrderStateType state = ProductOrderStateType.fromValue( allParams.get("state") );
        logger.info("find by state:" + state );
        return (List<ProductOrder>) this.productOrderRepo.findByState(state);
    }else {
        return findAll();
    }
  }


  public String findAllParamsJsonOrderIDs(Map<String, String> allParams) throws JsonProcessingException {
    List<ProductOrder> lso = findAllParams(allParams);
    ArrayList<String> oids = new ArrayList<>();
    for (ProductOrder object : lso) {
        oids.add(object.getId());
    }
    ObjectMapper mapper = new ObjectMapper();
    // Registering Hibernate4Module to support lazy objects
    // this will fetch all lazy objects before marshaling
    mapper.registerModule(new Hibernate5JakartaModule());
    String res = mapper.writeValueAsString( oids );

    return res;
  }


  @Transactional
  public ProductOrder addProductOrder(@Valid ProductOrderCreate productOrderCreate) throws NotFoundException {
    
    // Ensure that all Product Specifications exist
    List <ProductOrderItem> productOrderItemList = productOrderCreate.getProductOrderItem();
    for (ProductOrderItem productOrderItem: productOrderItemList) {
      
      @Valid
      ProductOfferingRef pOfferf = productOrderItem.getProductOffering();
      
      if (pOfferf==null) {
        throw new NotFoundException(400, "There is no ProductOfferingRef in Product Order Create request");        
      }
      
      ProductOffering pOffer = productOfferingRepoService.findByUuid( pOfferf.getId() );
      @Valid
      ProductSpecificationRef prodSpec = pOffer.getProductSpecification();
      String prodSpecificationId = prodSpec.getId();

        ProductSpecification prodSpecification = productSpecificationRepoService.findByUuid(prodSpecificationId);

        if (prodSpecification == null)
            throw new NotFoundException(400, "There is no Product Specification with Id: " + prodSpecificationId);
    }
    

    ProductOrder so = new ProductOrder();
    so.setOrderDate(OffsetDateTime.now(ZoneOffset.UTC));
    so.setCategory(productOrderCreate.getCategory());
    so.setDescription(productOrderCreate.getDescription());
    so.setExternalId(productOrderCreate.getExternalId());
    so.setNotificationContact(productOrderCreate.getNotificationContact());
    so.priority(productOrderCreate.getPriority());
    so.requestedCompletionDate(productOrderCreate.getRequestedCompletionDate());
    so.requestedStartDate(productOrderCreate.getRequestedStartDate() );
    so.setExpectedCompletionDate( productOrderCreate.getRequestedCompletionDate() );  //this is by default
    if (productOrderCreate.getNote() != null) {
        so.getNote().addAll(productOrderCreate.getNote());
    }
    
    
    

    boolean allAcknowledged = false;
    if (productOrderCreate.getProductOrderItem() != null) {
        allAcknowledged = true;
        so.getProductOrderItem().addAll(productOrderCreate.getProductOrderItem());
        for (ProductOrderItem soi : so.getProductOrderItem()) {                
            if ( ! soi.getState().equals( ProductOrderItemStateType.ACKNOWLEDGED )) {
                allAcknowledged = false;
            }
        }
    }

    if (productOrderCreate.getRelatedParty() != null) {
        so.getRelatedParty().addAll(productOrderCreate.getRelatedParty());
    }
   
    
    Note noteItem = new Note();
    noteItem.setText("Product Order " + ProductOrderStateType.INITIAL);
    noteItem.setAuthor("PO622API-addProductOrder");
    noteItem.setDate(OffsetDateTime.now(ZoneOffset.UTC) );
    so.addNoteItem(noteItem);

    so = this.productOrderRepo.save(so);
    
    if (allAcknowledged) { //in the case were order items are automatically acknowledged
        so.setState( ProductOrderStateType.ACKNOWLEDGED );
        noteItem = new Note();
        noteItem.setText("Product Order " + ProductOrderStateType.ACKNOWLEDGED);
        noteItem.setAuthor("PO622API-addProductOrder");
        noteItem.setDate(OffsetDateTime.now(ZoneOffset.UTC) );
        so.addNoteItem(noteItem);
        
        so = this.productOrderRepo.save(so);
    }
    
    raisePOCreateNotification(so);

    return so;
  }
  
  @Transactional
  public ProductOrder updateProductOrder(String id, @Valid ProductOrderUpdate prodOrderUpd) {
    
    logger.info("Will updateProductOrder:" + id);       
    
    ProductOrder po = this.findByUuid(id);
    boolean stateChanged = false;
    boolean expectedCompletionDateChanged = false;
    
    ProductOrderMapper mapper = Mappers.getMapper( ProductOrderMapper.class );
    po = mapper.updateProductOrder(po, prodOrderUpd);
    if ( prodOrderUpd.getState()!= null ) {
      stateChanged = po.getState() != prodOrderUpd.getState();      
      if ( po.getState().equals( ProductOrderStateType.COMPLETED )) {
          po.setCompletionDate( OffsetDateTime.now(ZoneOffset.UTC));
      }      
    }
    
    if ( prodOrderUpd.getExpectedCompletionDate()!= null ) {      
      expectedCompletionDateChanged = true;
    }
    
    
    po = this.productOrderRepo.save(po);
    
    return po;
    
  }
  
  
  private ProductOrder findByUuid(String id) {
    Optional<ProductOrder> optionalCat = this.productOrderRepo.findByUuid(id);
    return optionalCat.orElse(null);
  }


  @Transactional
  private void raisePOCreateNotification(ProductOrder so) {

    
      
  }




}
