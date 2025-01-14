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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.hibernate5.jakarta.Hibernate5JakartaModule;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etsi.osl.tmf.common.model.UserPartRoleType;
import org.etsi.osl.tmf.common.model.service.Note;
import org.etsi.osl.tmf.common.model.service.ResourceRef;
import org.etsi.osl.tmf.common.model.service.ServiceRef;
import org.etsi.osl.tmf.pcm620.model.ProductOffering;
import org.etsi.osl.tmf.pcm620.model.ProductOfferingRef;
import org.etsi.osl.tmf.pcm620.model.ProductSpecification;
import org.etsi.osl.tmf.pcm620.model.ProductSpecificationRef;
import org.etsi.osl.tmf.pcm620.reposervices.ProductOfferingRepoService;
import org.etsi.osl.tmf.pcm620.reposervices.ProductSpecificationRepoService;
import org.etsi.osl.tmf.po622.api.ProductOrderApiRouteBuilderEvents;
import org.etsi.osl.tmf.po622.model.OrderItemActionType;
import org.etsi.osl.tmf.po622.model.ProductOrder;
import org.etsi.osl.tmf.po622.model.ProductOrderAttributeValueChangeEvent;
import org.etsi.osl.tmf.po622.model.ProductOrderAttributeValueChangeNotification;
import org.etsi.osl.tmf.po622.model.ProductOrderCreate;
import org.etsi.osl.tmf.po622.model.ProductOrderCreateEvent;
import org.etsi.osl.tmf.po622.model.ProductOrderCreateNotification;
import org.etsi.osl.tmf.po622.model.ProductOrderItem;
import org.etsi.osl.tmf.po622.model.ProductOrderItemStateType;
import org.etsi.osl.tmf.po622.model.ProductOrderMapper;
import org.etsi.osl.tmf.po622.model.ProductOrderStateChangeEvent;
import org.etsi.osl.tmf.po622.model.ProductOrderStateChangeNotification;
import org.etsi.osl.tmf.po622.model.ProductOrderStateType;
import org.etsi.osl.tmf.po622.model.ProductOrderUpdate;
import org.etsi.osl.tmf.po622.repo.ProductOrderRepository;
import org.etsi.osl.tmf.prm669.model.RelatedParty;
import org.etsi.osl.tmf.sim638.model.ServiceUpdate;
import org.etsi.osl.tmf.sim638.service.ServiceRepoService;
import org.etsi.osl.tmf.so641.api.NotFoundException;
import org.etsi.osl.tmf.so641.reposervices.ServiceOrderRepoService;
import org.etsi.osl.tmf.util.KrokiClient;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.hibernate.transform.ResultTransformer;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

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
  

  @Autowired
  ProductOrderApiRouteBuilderEvents productOrderApiRouteBuilder;


  @Autowired
  ServiceRepoService serviceRepoService;

  @Autowired
  ServiceOrderRepoService serviceOrderRepoService;

  private SessionFactory sessionFactory;

  @PersistenceContext EntityManager entityManager;

  @Autowired
  public ProductOrderRepoService(EntityManagerFactory factory) {
    if (factory.unwrap(SessionFactory.class) == null) {
      throw new NullPointerException("factory is not a hibernate factory");
    }
    this.sessionFactory = factory.unwrap(SessionFactory.class);
  }


  @Transactional
  public List<ProductOrder> findAll() {

    // return (List<ProductOrder>) this.productOrderRepo.findAll();
    // return (List<ProductOrder>) this.productOrderRepo.findByOrderByOrderDateDesc();
    return (List<ProductOrder>) this.productOrderRepo.findAllOptimized();
  }
  
  public Void deleteByUuid(String id) {
    
    Optional<ProductOrder> optso = this.productOrderRepo.findByUuid(id);
    ProductOrder so = optso.get();
    if ( so == null ) {
        return null;
    }
    
    this.productOrderRepo.delete(so);
    return null;
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
  
  public String addProductOrderReturnEager(@Valid ProductOrderCreate pOrderCreate) {
    try {
      ProductOrder so = this.addProductOrder(pOrderCreate);
        return this.getProductOrderEagerAsString( so.getUuid());
    } catch (JsonProcessingException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    } catch (NotFoundException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    }
    return null;
}
  
  /**
   * 
   * This findAll is optimized on fields. 
   * @param fields
   * @param allParams
   * @return
   * @throws UnsupportedEncodingException
   */
  @Transactional
  public List findAll(@Valid String fields, Map<String, String> allParams, @Valid Date starttime, @Valid Date endtime)
          throws UnsupportedEncodingException {

      Session session = sessionFactory.openSession();
      Transaction tx = session.beginTransaction();
      List<ProductOrder> alist = null;
      try {
          String sql = "SELECT "
                  + "por.uuid as uuid,"
                  + "por.orderDate as orderDate,"
                  + "por.requestedStartDate as requestedStartDate,"
                  + "por.requestedCompletionDate as requestedCompletionDate,"
                  + "por.expectedCompletionDate as expectedCompletionDate,"
                  + "por.state as state,"
                  + "por.type as type,"
                  + "rp.uuid as relatedParty_uuid,"
                  + "rp.name as relatedParty_name";
          
          if (fields != null && fields.length()>0 ) {
              String[] field = fields.split(",");
              for (String f : field) {
                  sql += ", sor." + f + " as " + f ;
              }
              
          }           
          sql += "  FROM ProductOrder por "
                  + "JOIN por.relatedParty rp ";
          
          if (allParams.size() > 0) {
              sql += " WHERE rp.role = 'REQUESTER' AND ";
              for (String pname : allParams.keySet()) {
                  sql += " " + pname + " LIKE ";
                  String pval = URLDecoder.decode(allParams.get(pname), StandardCharsets.UTF_8.toString());
                  sql += "'" + pval + "'";
              }
          } else {
              sql += " WHERE rp.role = 'REQUESTER' ";             
          }
          
          if ( starttime != null ) {
              sql += " AND por.orderDate >= :param1";     
              
          }
          if ( endtime != null ) {
              sql += " AND por.expectedCompletionDate <= :param2";    
          }
          
          sql += "  ORDER BY por.orderDate DESC";
          
          Query query = session.createQuery( sql );
          if ( starttime != null ) {
              query.setParameter("param1", starttime.toInstant().atOffset(ZoneOffset.UTC)  );
          }
          if ( endtime != null ) {
              query.setParameter("param2", endtime.toInstant().atOffset(ZoneOffset.UTC)  );               
          }
          
          List<Object> mapaEntity = query
                  .setResultTransformer( new ResultTransformer() {
                      
                      @Override
                      public Object transformTuple(Object[] tuple, String[] aliases) {
                          Map<String, Object> result = new LinkedHashMap<String, Object>(tuple.length);
                                  for (int i = 0; i < tuple.length; i++) {
                                      String alias = aliases[i];
                                      if (alias.equals("uuid")) {
                                          result.put("id", tuple[i]);
                                      }
                                      if (alias.equals("type")) {
                                          alias = "@type";
                                      }
                                      if (alias.equals("relatedParty_name")) {
                                          if ( result.get( "relatedParty" ) == null ) {
                                              result.put("relatedParty", new ArrayList<Object>() ) ;                                              
                                          }
                                          ArrayList< Object> rpList =  (ArrayList< Object>) result.get( "relatedParty" );
                                          LinkedHashMap<String, Object> rp = new LinkedHashMap<String, Object>();
                                          rp.put("name", tuple[i]);
                                          rp.put("role", "REQUESTER" );
                                          rpList.add(rp);
                                      }
                                      if (alias != null) {
                                          result.put(alias, tuple[i]);
                                      }
                                  }

                                  return result;
                      }
                      
                      @Override
                      public List transformList(List collection) {
                          return collection;
                      }
                  } )
                  .list();
          
  
           
          
          return mapaEntity;
      
          
          
          
      } finally {
          tx.commit();
          session.close();
      }

  }
  
  public List<ProductOrder> findAll(String rolename, UserPartRoleType requester) {
    return (List<ProductOrder>) this.productOrderRepo.findByRolename(rolename);
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
    
    if ( productOrderCreate.getState() != null ) {
      so.setState( productOrderCreate.getState() );      
    } else {
      so.setState( ProductOrderStateType.INITIAL );
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
    if ( prodOrderUpd.getState()!= null ) {
      stateChanged = po.getState() != prodOrderUpd.getState();      
    }
    boolean expectedCompletionDateChanged = false;
    
    ProductOrderMapper mapper = Mappers.getMapper( ProductOrderMapper.class );
    po = mapper.updateProductOrder(po, prodOrderUpd);
    

    if ( prodOrderUpd.getState() != null ) {
        po.state( prodOrderUpd.getState() );
    }

    if ( prodOrderUpd.getDescription() != null ) {
        po.setDescription( prodOrderUpd.getDescription() );
    }
    
    
    
    if ( po.getState().equals( ProductOrderStateType.COMPLETED )) {
        po.setCompletionDate( OffsetDateTime.now(ZoneOffset.UTC));
    }      
    
    
    if ( prodOrderUpd.getExpectedCompletionDate()!= null ) {      
      expectedCompletionDateChanged = true;
    }
    
    if ( stateChanged ) {
      Note noteItem = new Note();
      noteItem.setText("Product Order " + po.getState() );
      noteItem.setAuthor("PO622API-stateChanged");
      noteItem.setDate(OffsetDateTime.now(ZoneOffset.UTC) );
      po.addNoteItem(noteItem);               
    }
    
    // Update each Service's end date to the updated Service Order's expected completion date
    if (expectedCompletionDateChanged) {
        List<String> services = serviceRepoService.getServicesFromOrderID(id);

        for (String serviceId : services) {
            logger.debug("Will delegate updated Product Order expected completion date " + 
        po.getExpectedCompletionDate() + " to service with id = " + serviceId); 

            @Valid
            ServiceUpdate servUpd = new ServiceUpdate();
            servUpd.setEndDate(po.getExpectedCompletionDate());
            serviceRepoService.updateService(serviceId, servUpd, false, null, null);
        }
    }
    
    po = this.productOrderRepo.save(po);
    if (stateChanged) {
      raisePOStateChangedNotification(po);            
    } else {
        raisePOAttributeValueChangedNotification(po);
    }
    
    return po;
    
  }
  

  @Transactional
  public ProductOrder findByUuid(String id) {
    Optional<ProductOrder> optionalCat = this.productOrderRepo.findByUuid(id);
    return optionalCat.orElse(null);
  }


  @Transactional
  private void raisePOCreateNotification(ProductOrder so) {

    ProductOrderCreateNotification n = new ProductOrderCreateNotification();
    ProductOrderCreateEvent event = new ProductOrderCreateEvent();
    event.getEvent().productOrder( so );
    n.setEvent(event );
    productOrderApiRouteBuilder.publishEvent(n, so.getId());
    
      
  }
  
  @Transactional
  private void raisePOStateChangedNotification(ProductOrder so) {
      ProductOrderStateChangeNotification n = new ProductOrderStateChangeNotification();
      ProductOrderStateChangeEvent event = new ProductOrderStateChangeEvent();
      //event.serviceOrder( getServiceORderEager( so.getId()) );
      event.getEvent().productOrder( so );
      n.setEvent(event );
      productOrderApiRouteBuilder.publishEvent(n, so.getId());
      
  }

  @Transactional
  private void raisePOAttributeValueChangedNotification(ProductOrder so) {
      ProductOrderAttributeValueChangeNotification n = new ProductOrderAttributeValueChangeNotification();
      ProductOrderAttributeValueChangeEvent event = new ProductOrderAttributeValueChangeEvent();
      event.getEvent().productOrder( so );
      n.setEvent(event );
      productOrderApiRouteBuilder.publishEvent(n, so.getId());
  
  }
  
  @Transactional
  public ProductOrder getProductOrderEager(String id) {

    Session session = sessionFactory.openSession();
    Transaction tx = session.beginTransaction();
    
    try {
        ProductOrder s = null;
        try {
            s = (ProductOrder) session.get(ProductOrder.class, id);
            if (s == null) {
                return this.findByUuid(id);// last resort
            }

            Hibernate.initialize(s.getRelatedParty());
            Hibernate.initialize(s.getProductOrderItem() );
            Hibernate.initialize(s.getNote() );
            Hibernate.initialize(s.getChannel());
            for (ProductOrderItem soi : s.getProductOrderItem()) {
              if ( soi.getProduct() != null ) {
                Hibernate.initialize( soi.getProduct().getProductCharacteristic() );                
              }
              Hibernate.initialize( soi.getProductOffering()  );
            }
            
            tx.commit();
        } finally {
            session.close();
        }
        
        return s;
    } catch (Exception e) {
      e.printStackTrace();
    }

    session.close();
    return null;
    
    
}

  @Transactional
  public String getProductOrderEagerAsString(String id) throws JsonProcessingException {
    ProductOrder s = this.getProductOrderEager(id);
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new Hibernate5JakartaModule());
    String res = mapper.writeValueAsString(s);

    return res;
  }


  public String getImageProductOrderItemRelationshipGraph(String id, @NotNull String itemid) {    
    ProductOrder so = this.findByUuid(id);
    String charvalue = null;
    if ( so!=null) {
        for (ProductOrderItem soiOrigin :  so.getProductOrderItem()) {
            if ( soiOrigin.getId().equals(itemid)) {
                charvalue = createItemRelationshipGraphNotation( soiOrigin );

            }

        }           
    }

    return KrokiClient.encodedGraph( charvalue );
  }


  private String createItemRelationshipGraphNotation(ProductOrderItem soiOrigin) {
    String result = getPOItemGraphNotation(soiOrigin, 0 );
    result = "blockdiag {"
            + "default_textcolor = white;\r\n"
            + "default_fontsize = 12;\r\n"
            + "\r\n" + result + "}";
    return result;
  }


  private String getPOItemGraphNotation(ProductOrderItem soiOrigin, int depth) {
    String result = "";
    if (depth>10 || soiOrigin.getProduct()==null) {
        return result;
    }
    for (ServiceRef specRel : soiOrigin.getProduct().getRealizingService() ) {
        if ( !soiOrigin.getProduct().getName().equals( specRel.getName()) ) {
            result += "\""+ soiOrigin.getProduct().getId() + "\""+ " -> " + "\""+ specRel.getId() +"\" "+";\r\n";
            result += "\""+ specRel.getId() + "\""+ " [label =\""+ specRel.getName() +"\", color = \"#2596be\"]; \r\n";
            org.etsi.osl.tmf.sim638.model.Service aService= serviceRepoService.findByUuid( specRel.getId() );
            if ( aService!= null) {
                result += getServiceGraphNotation( aService,0 );                
            }
        }
        
    }
    
    
    result += "\""+ soiOrigin.getProduct().getId() + "\""+ " [label = \""+ soiOrigin.getProduct().getName() +"\", color = \"#2596be\"]; \r\n";
    return result;
  }


  private String getServiceGraphNotation(org.etsi.osl.tmf.sim638.model.Service aService, int depth) {
    String result = "";
    if (depth>10) {
        return result;
    }
    for (ServiceRef specRel : aService.getSupportingService() ) {
        result += "\""+ aService.getId() + "\""+ " -> " + "\""+ specRel.getId()  +"\" "+";\r\n";
        result += "\""+ specRel.getId() + "\""+ " [label = \"" + specRel.getName()  + "\", color = \"#2596be\"];\r\n";
        
        for (ResourceRef resRel : aService.getSupportingResource()) {
            
            result += "\""+ aService.getId() + "\""+ " -> " + "\""+ resRel.getId() + "\""+ ";\r\n";
            result += "\""+ resRel.getId() + "\""+ " [ label = \"" + resRel.getName() +"\",  shape = roundedbox, color = \"#e28743\"]; \r\n";
            
        }
        
    }
    
    
    return result;
  }

  
  public String getImageProductOrderNotesGraph(String id) {
    
    @Data
    class ALane{
        public ALane(String author) {
            this.name =author;
        }
        String name = "";
        List<Note> boxes = new ArrayList<Note>();
    }
    
    Map <String, ALane> lanes = new HashMap<>();
    String charvalue = "";
    Optional<ProductOrder> optionalCat = this.productOrderRepo.findNotesOfProdOrder(id);
    if ( optionalCat.isPresent() ) {
        
      ProductOrder so = optionalCat.get();
        List<Note> notes = so.getNote()
                .stream()
                .sorted( (a, b) -> a.getDate().compareTo(b.getDate()) )
                .collect(Collectors.toList());
        
        for (Note anote : notes ) {
            if ( charvalue.length() > 0 ) {
                charvalue += " -> ";
            }
            charvalue += "\""+ anote.getUuid() + "\""  ;
            
            if ( lanes.get( anote.getAuthor()) == null)  {
                lanes.put( anote.getAuthor() , new ALane( anote.getAuthor() ));
            }
            
            lanes.get( anote.getAuthor()).boxes.add( anote);
            
        }
        
    }

    for (String lane : lanes.keySet()) {
        charvalue += "lane " + lanes.get(lane).name  + " {\r\n";
        for ( Note aNote : lanes.get(lane).boxes) {
            charvalue += aNote.getUuid() +" [label = \"" + aNote.getDateString() + "\r\n "+ aNote.getText()  +"\", color = \"#2596be\"]\r\n";
        }
        charvalue += "}\r\n";
    }
    
    charvalue = "actdiag  {"
            + "default_textcolor = white;\r\n"
            + "default_fontsize = 9;\r\n"
            + "\r\n" + charvalue + "}\r\n";
    return KrokiClient.encodedGraph( charvalue );
    
    
}



}
