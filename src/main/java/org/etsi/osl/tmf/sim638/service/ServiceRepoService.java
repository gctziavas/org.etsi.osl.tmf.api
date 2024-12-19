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
package org.etsi.osl.tmf.sim638.service;

import java.io.UnsupportedEncodingException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.datatype.hibernate5.jakarta.Hibernate5JakartaModule;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etsi.osl.tmf.common.model.Any;
import org.etsi.osl.tmf.common.model.UserPartRoleType;
import org.etsi.osl.tmf.common.model.service.Characteristic;
import org.etsi.osl.tmf.common.model.service.Note;
import org.etsi.osl.tmf.common.model.service.Place;
import org.etsi.osl.tmf.common.model.service.ResourceRef;
import org.etsi.osl.tmf.common.model.service.ServiceRef;
import org.etsi.osl.tmf.common.model.service.ServiceRelationship;
import org.etsi.osl.tmf.common.model.service.ServiceStateType;
import org.etsi.osl.tmf.prm669.model.RelatedParty;
import org.etsi.osl.tmf.ri639.model.Resource;
import org.etsi.osl.tmf.ri639.model.ResourceAttributeValueChangeNotification;
import org.etsi.osl.tmf.ri639.model.ResourceCreateNotification;
import org.etsi.osl.tmf.ri639.model.ResourceStateChangeNotification;
import org.etsi.osl.tmf.ri639.repo.ResourceRepository;
import org.etsi.osl.tmf.scm633.reposervices.ServiceSpecificationRepoService;
import org.etsi.osl.tmf.sim638.api.ServiceApiRouteBuilderEvents;
import org.etsi.osl.tmf.sim638.model.Service;
import org.etsi.osl.tmf.sim638.model.ServiceActionQueueAction;
import org.etsi.osl.tmf.sim638.model.ServiceActionQueueItem;
import org.etsi.osl.tmf.sim638.model.ServiceAttributeValueChangeEvent;
import org.etsi.osl.tmf.sim638.model.ServiceAttributeValueChangeNotification;
import org.etsi.osl.tmf.sim638.model.ServiceCreate;
import org.etsi.osl.tmf.sim638.model.ServiceCreateEvent;
import org.etsi.osl.tmf.sim638.model.ServiceCreateNotification;
import org.etsi.osl.tmf.sim638.model.ServiceOrderRef;
import org.etsi.osl.tmf.sim638.model.ServiceStateChangeEvent;
import org.etsi.osl.tmf.sim638.model.ServiceStateChangeNotification;
import org.etsi.osl.tmf.sim638.model.ServiceUpdate;
import org.etsi.osl.tmf.sim638.repo.ServiceActionQueueRepository;
import org.etsi.osl.tmf.sim638.repo.ServiceRepository;
import org.etsi.osl.tmf.so641.model.ServiceOrder;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.transform.ResultTransformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.etsi.osl.model.nfv.DeploymentDescriptor;
import org.etsi.osl.model.nfv.DeploymentDescriptorVxFInstanceInfo;
import jakarta.persistence.EntityManagerFactory;
import jakarta.validation.Valid;



@org.springframework.stereotype.Service
public class ServiceRepoService {

	private static final transient Log logger = LogFactory.getLog(ServiceRepoService.class.getName());

	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	ServiceRepository serviceRepo;
	

    @Autowired
    ResourceRepository resourceRepo;
	
	@Autowired
	ServiceActionQueueRepository serviceActionQueueRepo;

	@Autowired
	ServiceSpecificationRepoService  serviceSpecRepoService;

	private SessionFactory  sessionFactory;

	@Autowired
	ServiceApiRouteBuilderEvents serviceApiRouteBuilder;
	
	@Autowired
	public ServiceRepoService(EntityManagerFactory factory) {
		if (factory.unwrap(SessionFactory.class) == null) {
			throw new NullPointerException("factory is not a hibernate factory");
		}
		this.sessionFactory = factory.unwrap(SessionFactory.class);
	}
	
	public List<Service> findAll() {

		return (List<Service>) this.serviceRepo.findAll();
	}
	
	public List findAll(@Valid String fields, Map<String, String> allParams)
			throws UnsupportedEncodingException {
		Session session = sessionFactory.openSession();
		Transaction tx = session.beginTransaction();
		List<ServiceOrder> alist = null;
		try {
//			String sql = "SELECT s FROM ServiceSpecification s";
			String sql = "SELECT "
					+ "srv.uuid as uuid,"
					+ "srv.serviceDate as serviceDate,"
					+ "srv.name as name,"
					+ "srv.startDate as startDate,"
					+ "srv.category as category,"
					+ "srv.state as state,"
					+ "srv.startMode as startMode,"
					+ "srv.serviceType as serviceType,"
					+ "sor.id as serviceOrderId"
//					+ "rp.uuid as relatedParty_uuid,"
//					+ "rp.name as relatedParty_name"
					;
			
			if (fields != null) {
				String[] field = fields.split(",");
				for (String f : field) {
					sql += ", srv." + f + " as " + f ;
				}
				
			}			
			sql += "  FROM Service srv "
					//+ "JOIN srv.relatedParty rp "
					+ "JOIN srv.serviceOrder sor ";
//			if (allParams.size() > 0) {
//				sql += " WHERE rp.role = 'REQUESTER' AND ";
//				for (String pname : allParams.keySet()) {
//					sql += " " + pname + " LIKE ";
//					String pval = URLDecoder.decode(allParams.get(pname), StandardCharsets.UTF_8.toString());
//					sql += "'" + pval + "'";
//				}
//			} else {
//				sql += " WHERE rp.role = 'REQUESTER' ";				
//			}
			
			sql += "  ORDER BY srv.startDate DESC";
			
			List<Object> mapaEntity = session
				    .createQuery(sql )
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
							            if (alias.equals("serviceOrderId")) {
							            	if ( result.get( "serviceOrder" ) == null ) {
								                result.put("serviceOrder", new ArrayList<Object>() ) ;							            		
							            	}
							            	ArrayList< Object> rpList =  (ArrayList< Object>) result.get( "serviceOrder" );
							            	LinkedHashMap<String, Object> rp = new LinkedHashMap<>();
							            	rp.put("id", tuple[i]);							            	
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
			
	
			
//			List<ServiceOrder> mapaEntity = session
//				    .createQuery(sql )
//				    .setResultTransformer( new ResultTransformer() {
//						
//						@Override
//						public ServiceOrder transformTuple(Object[] tuple, String[] aliases) {
//									//Map<String, Object> result = new LinkedHashMap<String, Object>(tuple.length);
//									ServiceOrder so = new ServiceOrder();
//									so.setUuid( (String) tuple[0] );
//									so.setOrderDate(  (OffsetDateTime) tuple[1] );
//									ServiceOrderItem soi = new ServiceOrderItem();
//									so.addOrderItemItem( soi );
//									ServiceRestriction service = new ServiceRestriction();
//									service.setName( (String) tuple[9]  );
//									soi.setService(service );
//									
////							        for (int i = 0; i < tuple.length; i++) {
////							            String alias = aliases[i];
////							            if (alias.equals("type")) {
////							            	alias = "@type";
////							            }
////							            if (alias != null) {
////							                result.put(alias, tuple[i]);
////							            }
////							        }
//
//							        return so;
//						}
//						
//						@Override
//						public List transformList(List collection) {
//							return collection;
//						}
//					} )
//				    .list();
			
//			//this will fetch the whole object fields
//			if ( (( allParams!= null) && ( allParams.size()>0)) ) {
//				List<ServiceSpecification> resultlist = new ArrayList<>();
//				for (ServiceSpecification s : alist) {
//					resultlist.add(  findByUuid( s.getUuid() ));
//				}
//				return resultlist;
//			}
			
			
			
			return mapaEntity;
		
			
			
			
		} finally {
			tx.commit();
			session.close();
		}

	}
	
	
	public List<Service> findAll(String name, UserPartRoleType requester) {
		// TODO Auto-generated method stub
		return (List<Service>) this.serviceRepo.findByRolename(name);
	}
	

    @Transactional
	public Service addService(@Valid ServiceCreate service) {
		logger.info("Will add service: " + service.getName() );
		Service s = new Service();
		if (service.getType()!=null) {
			s.setType(service.getType());			
		}
		s.setName(service.getName());
		s.setCategory( service.getCategory() );
		s.setDescription( service.getDescription() );
		s.setStartDate( service.getStartDate());
		s.setEndDate( service.getEndDate() );
		s.hasStarted( service.isHasStarted());
		s.setIsServiceEnabled( service.isIsServiceEnabled());
		s.setIsStateful(service.isIsStateful());
		s.setServiceDate( service.getServiceDate());
		s.setServiceType( service.getServiceType());
		s.setStartMode( service.getStartMode());
		s.setState(service.getState());
		s.setServiceSpecificationRef( service.getServiceSpecificationRef() );
		if ( service.getNote() != null) {
			s.getNote().addAll( service.getNote() );
		}
		if ( service.getPlace() != null) {
			s.getPlace().addAll( service.getPlace() );
		}
		
		if ( service.getRelatedParty()!=null ) {
			s.getRelatedParty().addAll( service.getRelatedParty());
		}
		
		if ( service.getServiceCharacteristic () != null) {
			s.getServiceCharacteristic().addAll( service.getServiceCharacteristic() );
		}

		if ( service.getServiceOrder() != null) {
			s.getServiceOrder().addAll( service.getServiceOrder() );
		}
		if ( service.getServiceRelationship() != null) {
			s.getServiceRelationship().addAll( service.getServiceRelationship() );
		}
		if ( service.getSupportingResource() != null) {
			s.getSupportingResource().addAll( service.getSupportingResource() );
		}
		if ( service.getSupportingService() != null) {
			s.getSupportingService().addAll( service.getSupportingService() );
		}
		
		Note noteItem = new Note();
		noteItem.setText("Service " + s.getState() );
		noteItem.setAuthor("SIM-638");
		noteItem.setDate(OffsetDateTime.now(ZoneOffset.UTC) );
		s.addNoteItem(noteItem);		
		
        
        s = this.serviceRepo.save( s );

		raiseServiceCreateNotification(s);
		return s;
	}

	

	public Service findByUuid(String id) {
		Optional<Service> optionalCat = this.serviceRepo.findByUuid( id );
		return optionalCat
				.orElse(null);
	}


	/**
	 * @param id
	 * @param servUpd
	 * @param triggerServiceActionQueue
	 * @param updatedFromParentService
	 * @param updatedFromChildService
	 * @return
	 */
	@Transactional
	public Service updateService(String id, @Valid ServiceUpdate servUpd, boolean triggerServiceActionQueue, Service updatedFromParentService, Service updatedFromChildService ) {
	  

      logger.debug("================> Will updateService = " + id );        
		//Service service = this.findByUuid(id);
		Service service = this.getServiceEager(id);

		
		if ( service == null ) {
			logger.error("Service cannot be found in registry, UUID: " + id  );
			return null;
		}
		

	      
	      
		logger.info("Will update service: " + service.getName() );
		//logger.info("Will update service details: " + s.toString() );
		
		ObjectMapper mapper = new ObjectMapper();
		String originaServiceAsJson = null;
		try {
			originaServiceAsJson = mapper.writeValueAsString( service );
		} catch (JsonProcessingException e) {
			logger.error("cannot umarshall service: " + service.getName() );
			e.printStackTrace();
		}
		
		
       		
		if (servUpd.getType()!=null) {
			service.setType(servUpd.getType());			
		}
		
		if (servUpd.getName() != null ) {
			service.setName(servUpd.getName());			
		}

		if (servUpd.getCategory() != null ) {
			service.setCategory( servUpd.getCategory() );			
		}
		if (servUpd.getDescription() != null ) {
			service.setDescription( servUpd.getDescription() );			
		}
		if (servUpd.getStartDate() != null ) {

			service.setStartDate( servUpd.getStartDate());
		}
		if (servUpd.getEndDate() != null ) {
			service.setEndDate( servUpd.getEndDate() );			
		}
		if (servUpd.isHasStarted() != null ) {
			service.hasStarted( servUpd.isHasStarted());
		}
		if (servUpd.isIsServiceEnabled() != null ) {
			service.setIsServiceEnabled( servUpd.isIsServiceEnabled());			
		}
		if (servUpd.isIsStateful() != null ) {
			service.setIsStateful(servUpd.isIsStateful());
		}
		if (servUpd.getServiceDate() != null ) {
			service.setServiceDate( servUpd.getServiceDate());
			
		}
		if (servUpd.getServiceType() != null ) {
			service.setServiceType( servUpd.getServiceType());
			
		}
		if (servUpd.getStartMode() != null ) {
			service.setStartMode( servUpd.getStartMode());
			
		}
		

		boolean stateChanged = false;
		ServiceStateType previousState = service.getState();		
		if (servUpd.getState() != null ) {	
			stateChanged = service.getState() != servUpd.getState();
			service.setState(servUpd.getState());
			
		}
		if (servUpd.getServiceSpecificationRef() != null ) {

			service.setServiceSpecificationRef( servUpd.getServiceSpecificationRef() );
		}

		/**
		 * the following need to be modified for deleting items from lists.
		 */
		
		if ( servUpd.getNote()!=null ) {
			for (Note n : servUpd.getNote()) {
				if (n.getUuid() == null) {
					service.addNoteItem(n);
				}
			}						
		}
		
		if ( servUpd.getPlace()!=null ) {
			for (Place n : servUpd.getPlace()) {
				if (n.getUuid() == null) {
					service.addPlaceItem(n);
				}
			}						
		}

		if ( servUpd.getRelatedParty()!=null ) {
			for (RelatedParty n : servUpd.getRelatedParty()) {
				if (n.getUuid() == null) {
					service.addRelatedPartyItem(n);
				}
			}						
		}

		boolean serviceCharacteristicChanged = false;
		boolean serviceCharacteristicChangedContainsPrimitive = false;
		boolean serviceCharacteristicChangedContainsNSLCM;
		
		String charChangedForNotes = "";
		//List<Characteristic> childCharacteristicsChanged = new ArrayList<>();
		

		//logger.info("==> Will update serviceToString: " + service.toString() );
		
		
		if ( servUpd.getServiceCharacteristic()!=null ) {
			for (Characteristic n : servUpd.getServiceCharacteristic()) {
				serviceCharacteristicChangedContainsNSLCM = false;
				
					if ( service.getServiceCharacteristicByName( n.getName() )!= null ) {
						
						Characteristic origChar = service.getServiceCharacteristicByName( n.getName() );
						if ( ( origChar !=null ) && ( origChar.getValue() !=null ) && ( origChar.getValue().getValue() !=null )) {
							if ( !origChar.getValue().getValue().equals(n.getValue().getValue()) ) {									
//								if ( n.getName().contains("::") ) {
//									childCharacteristicsChanged.add(n); //the characteristic needs later to be propagated to its children
//									
//								}
							  
							  if ( !n.getName().contains("::") ) { //it is not a child characteristic
                                serviceCharacteristicChanged = true; //change only characteristics of this service
                                charChangedForNotes += n.getName() + ", "; 
							  }
                              if ( n.getName().toUpperCase().contains(  "PRIMITIVE::" ) ){
								serviceCharacteristicChanged = true;
                                serviceCharacteristicChangedContainsPrimitive = true;
                              }

							  // Check if the name contains "NSLCM"
							  if (n.getName().toUpperCase().contains("NSLCM")) {
							  	// Flag to indicate that service characteristic with NSLCM is present
							  	serviceCharacteristicChangedContainsNSLCM = true;
  
							  	// Update the NSLCM Characteristic
							  	updateNSLCMCharacteristic(service, n);
							  }

								
							}
						}
						
						 // As the NSLCM Characteristic was already updated, skip that one
						 if (!serviceCharacteristicChangedContainsNSLCM) {
							service.getServiceCharacteristicByName( n.getName() ).setValue( 
									new Any( n.getValue().getValue(), n.getValue().getAlias()  )
									);
						 }

					} else {
						service.addServiceCharacteristicItem(n);

			              
						if ( !n.getName().contains("::") ) { //it is not a child characteristic
	                        serviceCharacteristicChanged = true;    
	                        charChangedForNotes += n.getName() + ", "; 						  
						}
					}
				
			}						
		}
		
		if ( servUpd.getServiceOrder()!=null ) {
			for (ServiceOrderRef n : servUpd.getServiceOrder()) {
				if (n.getUuid() == null) {
					service.addServiceOrderItem(n);
				}
			}						
		}

		if ( servUpd.getServiceRelationship()!=null ) {
			for (ServiceRelationship n : servUpd.getServiceRelationship()) {
				if (n.getUuid() == null) {
					service.addServiceRelationshipItem(n);
				}
			}						
		}
		if ( servUpd.getSupportingResource()!=null ) {
			for (ResourceRef n : servUpd.getSupportingResource()) {
				if (n.getUuid() == null) {
					service.addSupportingResourceItem(n);
				}
			}						
		}
		if ( servUpd.getSupportingService()!=null ) {
			for (ServiceRef n : servUpd.getSupportingService()) {
				if (n.getUuid() == null) {
					service.addSupportingServiceItem(n);
				}
			}						
			//prepei na enimerwsoume ta characteristics edw sto parent servcie pou exei auto ws supported
		}
				

		if (stateChanged) {
			Note noteItem = new Note();
			noteItem.setText("Service is " + service.getState() );
			noteItem.setAuthor("SIM-638");
			noteItem.setDate(OffsetDateTime.now(ZoneOffset.UTC) );
			service.addNoteItem(noteItem);		
	        logger.debug("=============SERVICE STATE ================================>  " + service.getState() );
		}
		
		
		/**
		 * Check here if the characteristics changed are of interest for LCM rules and further processing by the orchestrator 
		 */
		Characteristic lcmchar = service.getServiceCharacteristicByName("_LCM_CHARACTERISTICS_");
        if ( lcmchar != null && lcmchar.getValue() != null && !lcmchar.getValue().getValue().equals("all") && !charChangedForNotes.equals("")) {
          
          // Split the strings into arrays of values
          String[] arrayA = lcmchar.getValue().getValue().split(",");
          String[] arrayB = charChangedForNotes.split(",");
          // Convert strb values into a set for faster lookup
          Set<String> setB = new HashSet<>(Arrays.asList(arrayB));
          // Check if any value from stra exists in strb
          boolean valueExists = false;
          for (String value : arrayA) {
              if (setB.contains(value)) {
                valueExists = true; // A common value exists
              }
          }
          if (!valueExists) {
            serviceCharacteristicChanged=false;
          }
        }
		
        if (charChangedForNotes.contains( "reconciledAt") ) { //this is just a sync message, so we need to igore such changes
          serviceCharacteristicChanged = false;
        }
		
		if (serviceCharacteristicChanged) {
          
          Characteristic noteCheck = service.getServiceCharacteristicByName("_DETAILED_NOTES_");
          if ( noteCheck!= null 
              && noteCheck.getValue() != null
              && noteCheck.getValue().getValue() != null
              && !noteCheck.getValue().getValue().equals("")) {
            Note noteItem = new Note();
            noteItem.setText("Service Characteristic changed: " + charChangedForNotes );
            noteItem.setAuthor("SIM638-API");
            noteItem.setDate(OffsetDateTime.now(ZoneOffset.UTC) );
            service.addNoteItem(noteItem);  
            
          }	
		}

		
		service = this.serviceRepo.save( service );
		
 		
	    String requestedServiceAsJson = null;
	    try {
	      requestedServiceAsJson = mapper.writeValueAsString( service );
	    } catch (JsonProcessingException e) {
	      logger.error("cannot umarshall service: " + service.getName() );
	      e.printStackTrace();
	    }
		
		/**
		 * Save in ServiceActionQueueItem
		 */
		
	    Boolean childServiceCharacteristicChanged = false;
	    
	    if  ( stateChanged  ) {
          ServiceActionQueueItem saqi = new ServiceActionQueueItem();
          saqi.setServiceRefId( id );
          saqi.setOriginalServiceInJSON( originaServiceAsJson );
              		  
          if ( service.getState().equals(  ServiceStateType.INACTIVE) ) {
            saqi.setAction( ServiceActionQueueAction.DEACTIVATE );		
          }else if ( service.getState().equals(  ServiceStateType.TERMINATED) ) {
    		saqi.setAction( ServiceActionQueueAction.TERMINATE );		
          }else if ( service.getState().equals(  ServiceStateType.ACTIVE) ) {
			saqi.setAction( ServiceActionQueueAction.EVALUATE_STATE_CHANGE_TOACTIVE  );	
          }else if ( previousState!=null && previousState.equals( ServiceStateType.ACTIVE) ) {
			saqi.setAction( ServiceActionQueueAction.EVALUATE_STATE_CHANGE_TOINACTIVE  );
          }
			
          if ( saqi.getAction() != ServiceActionQueueAction.NONE  ) {
            logger.debug("==========addServiceActionQueueItem==============> saqi.getAction() = " + saqi.getAction() );
            this.addServiceActionQueueItem(service, saqi);                    
          }
		} else if ( serviceCharacteristicChanged &&  service.getState().equals(  ServiceStateType.ACTIVE) &&  previousState!=null && previousState.equals( ServiceStateType.ACTIVE) && triggerServiceActionQueue ) {
			ServiceActionQueueItem saqi = new ServiceActionQueueItem();
			saqi.setServiceRefId( id );
			saqi.setOriginalServiceInJSON( originaServiceAsJson );		
			saqi.setAction( ServiceActionQueueAction.EVALUATE_CHARACTERISTIC_CHANGED  );	
			if ( serviceCharacteristicChangedContainsPrimitive ) {
				saqi.setAction( ServiceActionQueueAction.EVALUATE_CHARACTERISTIC_CHANGED_MANODAY2  );					
			}
            logger.debug("==========addServiceActionQueueItem==============>  serviceCharacteristicChanged &&  service.getState().eq charChangedForNotes= "  + charChangedForNotes);
			this.addServiceActionQueueItem(service, saqi);
			childServiceCharacteristicChanged = true;
		}
		
		
		
		
		
		
        /*
         * Update any parent service
         */
        for (ServiceRelationship serviceRelationship : service.getServiceRelationship()) {
          if (serviceRelationship.getRelationshipType().equals("ChildService") ) {
            if (serviceRelationship.getService() != null) {
              
              if (stateChanged || childServiceCharacteristicChanged) {
                if (updatedFromParentService == null || (updatedFromParentService != null && !updatedFromParentService.getId().equals(serviceRelationship.getService().getId()))) { // avoid circular
                  ServiceActionQueueItem saqi = new ServiceActionQueueItem(); // this will trigger lcm rule to parent
                  saqi.setServiceRefId(serviceRelationship.getService().getId());
                  try {
                    saqi.setOriginalServiceInJSON( mapper.writeValueAsString( service ) ); //pass the child service as is
                  } catch (JsonProcessingException e) {
                    e.printStackTrace();
                  }
                  if (stateChanged) {
                    saqi.setAction(ServiceActionQueueAction.EVALUATE_CHILD_STATE_CHANGE );
                    logger.debug("==========addServiceActionQueueItem==============>  EVALUATE_CHILD_STATE_CHANGE "  + charChangedForNotes);
                  } else if ( childServiceCharacteristicChanged)  {
                    saqi.setAction(ServiceActionQueueAction.EVALUATE_CHILD_CHARACTERISTIC_CHANGED);
                    logger.debug("==========addServiceActionQueueItem==============>  EVALUATE_CHILD_CHARACTERISTIC_CHANGED "  + charChangedForNotes);
                  }
                  this.addServiceActionQueueItem(service, saqi);
                }
              }
            }
          }
        }	
		
//		if ( childCharacteristicsChanged.size()>0 ) {
//			if ( service.getSupportingService() != null ) { //propagate to children
//				//copy characteristics values from CFS Service  to its supporting services.
//				for (ServiceRef sref : service.getSupportingService() ) {
//					Service aSupportingService = this.findByUuid( sref.getId() );
//					ServiceUpdate supd = new ServiceUpdate();
//					boolean foundCharacteristicForChild = false;
//					for (Characteristic supportingServiceChar : aSupportingService.getServiceCharacteristic() ) {
//						
//						for (Characteristic serviceCharacteristic : childCharacteristicsChanged ) {
//							if ( serviceCharacteristic.getName().contains( aSupportingService.getName() + "::" + supportingServiceChar.getName() )) { 									
//								//supportingServiceChar.setValue( serviceCharacteristic.getValue() );
//								Characteristic cNew = new Characteristic();
//								cNew.setName(supportingServiceChar.getName());
//								cNew.value( new Any( serviceCharacteristic.getValue() ));
//								supd.addServiceCharacteristicItem( cNew );
//								foundCharacteristicForChild = true;
//							}
//						}
//					}					
//					
//					if ( foundCharacteristicForChild ) {
//						Note n = new Note();
//						n.setText("Child Characteristics Changed"  );
//						n.setAuthor( "SIM638-API" );
//						n.setDate( OffsetDateTime.now(ZoneOffset.UTC).toString() );
//						supd.addNoteItem( n );					
//						if ( updatedFromChildService == null || 
//								(updatedFromChildService!=null && !updatedFromChildService.getId().equals( aSupportingService.getId())) ) { //avoid circular
//							this.updateService( aSupportingService.getId(), supd , false, service, null); //update the service							
//						} 
//					}
//				}
//				
//			}
//		}
		
		
		
		/**
		 * notify hub
		 */
		if (stateChanged) {
			raiseServiceStateChangedNotification( service );			
		} else if ( serviceCharacteristicChanged ) {
			raiseServiceAttributeValueChangedNotification( service );
		}
		
		
		Characteristic schart = service.getServiceCharacteristicByName("long_string");

		if ( schart!= null ) {
			String teest = schart.getValue().getValue();
			logger.info("schart size = " + teest.length() );
			
			logger.info("schart " + teest );
			System.out.println("The value is : \n " + teest);
//			try (PrintWriter out = new PrintWriter("C:\\tranoris\\ctranup\\personal\\Invoices\\filename.txt")) {
//			    out.println( teest );
//			} catch (FileNotFoundException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}		
		}
		
		return service;
	}

	/**
     * Updates the NSLCM characteristic within a given service.
     *
     * @param service The service object containing the characteristics.
     * @param n       The characteristic object to be checked and potentially updated.
     */
    public Service updateNSLCMCharacteristic(Service service, Characteristic n) {
        // Create an object mapper for JSON serialization/deserialization
        ObjectMapper primitivesObjectMapper = new ObjectMapper();

        // Retrieve the service characteristic based on the name
        Characteristic aNSLCMStatusesCharacteristic = service.getServiceCharacteristicByName(n.getName());

        // Retrieve the current NSLCM statuses, and extract the new status 
		// to be appended
		String aNSLCMStatusesBeforeUpdate = service
				.getServiceCharacteristicByName(n.getName()).getValue().getValue();
		String aNSLCMNewStatus = n.getValue().getValue();

        // Check if the current NSLCM statuses value is null or explicitly "null" 
		// and, if thats the case start an empty JSON array (this takes place when initializing the characteristic)
        if ( aNSLCMStatusesBeforeUpdate == null ||
			aNSLCMStatusesBeforeUpdate.isEmpty() ||
			aNSLCMStatusesBeforeUpdate.equals("null")
		){
			try {
				service.getServiceCharacteristicByName(n.getName()).getValue().setValue(
					primitivesObjectMapper.writeValueAsString(primitivesObjectMapper.createArrayNode())
				);
			} catch (JsonProcessingException e) {
                e.printStackTrace();
            }
		}

        // Check if the current characteristic value is not null and not explicitly "null"
        if ( aNSLCMNewStatus != null &&
			!aNSLCMNewStatus.isEmpty() &&
			!aNSLCMNewStatus.equals("null")
		) {
            try {
				// Deserialize the current statuses back to an array list
				ArrayNode nslcmStatusesJsonArray = (ArrayNode) primitivesObjectMapper.readTree(
					service.getServiceCharacteristicByName(n.getName()).getValue().getValue()
				);

				// Map the current status to json
				JsonNode currenNSLCMStatus = primitivesObjectMapper.readTree(n.getValue().getValue());

				// Add the new status to the list if it's not already present and is not null
				if (!containsNode(nslcmStatusesJsonArray, currenNSLCMStatus)) {
					nslcmStatusesJsonArray.add(currenNSLCMStatus);
				}

				// Finally, map the statuses list to a Json encoded one
				aNSLCMStatusesCharacteristic.setValue(
					new Any(primitivesObjectMapper.writeValueAsString(nslcmStatusesJsonArray), n.getValue().getAlias())
				);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
		}
		return service;
    }

	/**
     * Helper method to check if an ArrayNode contains a specific JsonNode.
     * This method uses Jackson's `equals()` for deep equality.
	 * @param arrayNode Array of Json Nodes
     * @param jsonNode	the object encoded as Jsons
     */
    private static boolean containsNode(ArrayNode arrayNode, JsonNode jsonNode) {
        for (JsonNode node : arrayNode) {
            if (node.equals(jsonNode)) {
                return true;
            }
        }
        return false;
    }


	/**
	 * @param service
	 * @param parentService
	 */
//	private void propagateCharacteristicsToParentService(Service childService, String parentServiceId) {
//		
//		ServiceUpdate servUpd = new ServiceUpdate();
//		
//		for (Characteristic n : childService.getServiceCharacteristic()) {			
//			Characteristic serviceCharacteristicItem = new Characteristic();
//			serviceCharacteristicItem.setName( childService.getName() + "::" + n.getName());
//			serviceCharacteristicItem.setValue( new Any( n.getValue() ));
//			servUpd.addServiceCharacteristicItem(serviceCharacteristicItem);
//		}
//		
//		this.updateService( parentServiceId, servUpd, false, null, childService);
//	}

    @Transactional  
	public String getServiceEagerAsString(String id) throws JsonProcessingException {
		Service s = this.getServiceEager(id);
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new Hibernate5JakartaModule());
		String res = mapper.writeValueAsString(s);


		Characteristic schart = s.getServiceCharacteristicByName("long_string");

		if ( schart!= null ) {
			String teest = schart.getValue().getValue();
			logger.debug("schart size = " + teest.length() );
			
			logger.debug("schart " + teest );
			logger.debug("======================================================================================================");			
		}
		
		return res;
	}

    @Transactional  
	public Service getServiceEager(String id) {
		if ( id == null || id.equals("")) {
			return null;
		}
		Session session = sessionFactory.openSession();
		Transaction tx = session.beginTransaction();
		Service s = null;
		try {
			s = (Service) session.get(Service.class, id);
			if (s == null) {
				return this.findByUuid(id);// last resort
			}

			Hibernate.initialize(s.getRelatedParty());
			Hibernate.initialize(s.getNote() );
			Hibernate.initialize(s.getServiceCharacteristic() );
			Hibernate.initialize(s.getServiceOrder() );
			Hibernate.initialize(s.getServiceRelationship() );
			Hibernate.initialize(s.getServiceSpecificationRef() );
			Hibernate.initialize(s.getSupportingService() );
			Hibernate.initialize(s.getSupportingResource()  );
			Hibernate.initialize(s.getPlace()  );
			
			tx.commit();
		} finally {
			session.close();
		}
		
		return s;
	}
	

	@Transactional
	private void raiseServiceCreateNotification(Service so) {
		ServiceCreateNotification n = new ServiceCreateNotification();
		ServiceCreateEvent event = new ServiceCreateEvent();
		event.service( so );
		n.setEvent(event );
		serviceApiRouteBuilder.publishEvent(n, so.getId());
		
	}

	@Transactional
	private void raiseServiceStateChangedNotification(Service so) {
		ServiceStateChangeNotification n = new ServiceStateChangeNotification();
		ServiceStateChangeEvent event = new ServiceStateChangeEvent();
		event.service( so );
		n.setEvent(event );
		serviceApiRouteBuilder.publishEvent(n, so.getId());
		
	}

	@Transactional
	private void raiseServiceAttributeValueChangedNotification(Service so) {
		ServiceAttributeValueChangeNotification n = new ServiceAttributeValueChangeNotification();
		ServiceAttributeValueChangeEvent event = new ServiceAttributeValueChangeEvent();
		event.service( so );
		n.setEvent(event );
		serviceApiRouteBuilder.publishEvent(n, so.getId());
	
	}
	
	
	/**
	 * @return
	 */
    @Transactional  
	public List<ServiceActionQueueItem> findAllServiceActionQueueItems() {

		return (List<ServiceActionQueueItem>) this.serviceActionQueueRepo.findFirst10ByOrderByInsertedDate();
	}
	
	public ServiceActionQueueItem  addServiceActionQueueItem(Service service, @Valid ServiceActionQueueItem item) {
		logger.debug("Will add ServiceActionQueueItem ServiceRefId: " + item.getServiceRefId() );
		
		Characteristic lcmchar = service.getServiceCharacteristicByName("_LCM_CHARACTERISTICS_");
		if ( lcmchar == null || lcmchar.getValue() == null) {
		    //find any similar action inqueue and delete them, so to keep this one as the most recent
	        List<ServiceActionQueueItem> result = this.serviceActionQueueRepo.findByServiceRefIdAndAction(item.getServiceRefId(), item.getAction());
	        logger.debug("Will add ServiceActionQueueItem ServiceRefId result: " +result.size() );
	        if (result.size()>0) { //essentially it will not delete any requests, but just return with not adding the new action since it is already exists
	          return item;
	        }
	        this.serviceActionQueueRepo.deleteByServiceRefIdAndAction(item.getServiceRefId(), item.getAction());
	          
		}

        return this.serviceActionQueueRepo.save( item);
		
	}

	/**
	 * @param item
	 * @return
	 */
	@Transactional
	public ServiceActionQueueItem  updateServiceActionQueueItem(@Valid ServiceActionQueueItem item) {
		logger.debug("Will update ServiceActionQueueItem ServiceRefId: " + item.getServiceRefId() );
		return this.serviceActionQueueRepo.save( item);
	}
	
	/**
	 * @param id
	 * @return
	 */
    @Transactional  
	public Void deleteServiceActionQueueItemByUuid(String id) {
		
		Optional<ServiceActionQueueItem> optso = this.serviceActionQueueRepo.findByUuid(id);
		if ( optso.isEmpty() ) {
			return null;
		}
		ServiceActionQueueItem so = optso.get();
		if ( so == null ) {
			return null;
		}
		
		this.serviceActionQueueRepo.delete(so);
		return null;
	}

	

	@Transactional
	public List<String> findAllActiveServicesToTerminate(){

		List<String> result = new ArrayList<>();
		List<Service> srvs = this.serviceRepo.findActiveToTerminate();
		for (Service service : srvs) {
			result.add(  service.getId());
		}
		
		return result;
	}

	/**
	 * @return UUIDs of Services and put them in a List
	 */
	@Transactional
	public List<String> findAllActiveAndReservedServicesOfPartners(){

		List<String> result = new ArrayList<>();
		List<Service> srvs = this.serviceRepo.findActiveAndReservedServicesOfPartners();
		for (Service service : srvs) {
			result.add(  service.getId());
		}
		
		return result;
	}
	
	

	/**
	 * Given a DeploymentRequestID (which is provided by OSM in our case)
	 * we locate the equivalent service
	 * @param aDeploymentRequestID
	 * @return
	 */
	@Transactional
	public List<Service> findDeploymentRequestID( String aDeploymentRequestID){

		return (List<Service>) this.serviceRepo.findByDeploymentRequestID( aDeploymentRequestID );

	}
	
	
	 /**
     * Given a Resource (which for example might be provided by a CR update from a cluster )
     * we locate the equivalent services
     * @param resourceID
     * @return
     */
    @Transactional
    public List<Service> findServicesHavingThisSupportingResourceID( String resourceID){

        return (List<Service>) this.serviceRepo.findServicesHavingThisSupportingResourceID( resourceID );

    }
	
	
	
	/**
	 * @param item
	 * @return
	 */
	@Transactional
	public void  nfvCatalogNSResourceChanged(@Valid DeploymentDescriptor dd) {
		String deploymentRequestID = dd.getId() + "";
		logger.info("Will update nfvCatalogNSResourceChanged for deploymentRequestID = " + deploymentRequestID );
		
		var aservices = findDeploymentRequestID( deploymentRequestID );
		for (Service as : aservices) {
			
			Service aService = findByUuid(as.getId()); 
			
			if ( aService.getState().equals( ServiceStateType.ACTIVE )  ) {
				

				ServiceUpdate supd = new ServiceUpdate();
				
				Characteristic cNewLCM = new Characteristic();
				cNewLCM.setName("NSLCM" );
				cNewLCM.value( new Any( dd.getNs_nslcm_details()  ));
				supd.addServiceCharacteristicItem( cNewLCM );
				
				Characteristic cNewNSR = new Characteristic();
				//cNewNSR.setUuid(null);
				cNewNSR.setName("NSR" );
				cNewNSR.value( new Any( dd.getNsr()  ));
				supd.addServiceCharacteristicItem( cNewNSR );
				
				
				
				if ( dd.getDeploymentDescriptorVxFInstanceInfo() !=null ) {
					for ( DeploymentDescriptorVxFInstanceInfo vnfinfo : dd.getDeploymentDescriptorVxFInstanceInfo() ) {							
							Characteristic cNewMember = new Characteristic();
							cNewMember.setName(  "VNFINDEXREF_INFO_" + vnfinfo.getMemberVnfIndexRef() );
							cNewMember.value( new Any( vnfinfo.getVxfInstanceInfo()  + "" ));
							supd.addServiceCharacteristicItem( cNewMember );
					}					
				}
				
				Note n = new Note();
				n.setText("NS Resource LCM Changed"  );
				n.setAuthor( "SIM638-API" );
				n.setDate( OffsetDateTime.now(ZoneOffset.UTC).toString() );
				supd.addNoteItem( n );					
				
				this.updateService( aService.getId(), supd , true, null, null); //update the service			
			}
		}
	}


	@Transactional
	public List<String> getServicesFromOrderID(String orderid){

		List<String> result = new ArrayList<>();
		List<Service> srvs = this.serviceRepo.findServicesFromOrderID( orderid );
		for (Service service : srvs) {
			result.add(  service.getId());
		}
		
		return result;
	}
	

    @Transactional	
	public void  updateServicesHavingThisSupportingResource(@Valid Resource res) {
      try {
        
        logger.debug("Will update services related to this resource with id = " + res.getId() );
        
        var aservices = findServicesHavingThisSupportingResourceID(  res.getId() );

        logger.debug("services.found = " + aservices.size() );
        
        for (Service as : aservices) {
            
              Service aService = findByUuid(as.getId()); 
              
              List<Resource> rlist = new ArrayList<Resource>();
              for (ResourceRef rref : aService.getSupportingResource()) {
                Optional<Resource> result = resourceRepo.findByUuid(rref.getId());
                if (result.isPresent()) {
                  rlist.add( result.get() );
                }
              }
  
              rlist.add(res); //add also this one
              
              ServiceStateType nextState = aService.findNextStateBasedOnSupportingResources(rlist);
  
                ServiceUpdate supd = new ServiceUpdate();
                supd.setState(nextState);
                String stateText="";
                if ( !aService.getState().equals(nextState)) {
                  stateText = "State changed from " + aService.getState() + " to " + nextState + ".";
                }
                
                
                //copy characteristics, from resource to service
                
                for (org.etsi.osl.tmf.ri639.model.Characteristic rChar : res.getResourceCharacteristic()) {
                    Characteristic cNew = new Characteristic();
                    cNew.setName( rChar.getName());
                    cNew.value( new Any( rChar.getValue() ));                
                    supd.addServiceCharacteristicItem( cNew );  
                }
                
                Characteristic noteCheck = as.getServiceCharacteristicByName("_DETAILED_NOTES_");
                if ( noteCheck!= null 
                    && noteCheck.getValue() != null
                    && noteCheck.getValue().getValue() != null
                    && !noteCheck.getValue().getValue().equals("")) {
                  Note n = new Note();
                  n.setText(stateText + "Supporting Resource changed with id: " + res.getId());
                  n.setAuthor( "SIM638-API" );
                  n.setDate( OffsetDateTime.now(ZoneOffset.UTC).toString() );
                  supd.addNoteItem( n );                  
                }                  
                
                this.updateService( aService.getId(), supd , true, null, null); //update the service 

        }
      

      }catch (Exception e) {
        e.printStackTrace();
      }
      
    }
    
    
    @Transactional  
    public void  resourceCreatedEvent(@Valid ResourceCreateNotification resNotif) {  
      try {
        Resource res = resNotif.getEvent().getEvent().getResource();    
        logger.debug("resourceCreatedEvent for: " + res.getName()); 
        updateServiceFromresourceChange(res);
      }catch (Exception e) {
        e.printStackTrace();
      }
      
    }
    

    @Transactional
    public void resourceStateChangedEvent(@Valid ResourceStateChangeNotification resNotif) {
      try {
        
        Resource res = resNotif.getEvent().getEvent().getResource();
        logger.debug("resourceStateChangedEvent for: " + res.getName()); 
        updateServiceFromresourceChange(res);

      }catch (Exception e) {
        e.printStackTrace();
      }
    
    }

    @Transactional  
    private void updateServiceFromresourceChange(Resource res) {

      updateServicesHavingThisSupportingResource(res);

      addAnyNewRelatedResourcesFromKubernetesLabel(res);


    }

    /**
     * This function will try to identify if the resource contains
     * a characteristic called "org.etsi.osl.serviceId" and will check if there is a related service.
     * If it is not it's add the resource back to the service. This is useful in kubernetes deployments,
     * in cases of new resources in a namespace that are related to this service
     * @param res
     */
    @Transactional  
    private void addAnyNewRelatedResourcesFromKubernetesLabel(Resource res) {
      logger.debug("updateResourceFromKubernetesLabel for: " + res.getName() + ", version" + res.getResourceVersion()); 
      
      if (res.getResourceCharacteristicByName("org.etsi.osl.serviceId") != null) {


        logger.debug("org.etsi.osl.serviceId found ");
        String serviceId = res.getResourceCharacteristicByName("org.etsi.osl.serviceId").getValue().getValue();
        logger.debug("rserviceId: " + serviceId); 
        
        Service aService = findByUuid( serviceId ); 
        if ( aService !=null ) {
          logger.debug("aService found "); 
          Boolean resourceFoundInSupportedResourcesOfService = false; 
          for (ResourceRef as : aService.getSupportingResource()) {
            if ( as.getId().equals( res.getId() )) {
              resourceFoundInSupportedResourcesOfService = true;
              break;
            }
          }

          logger.debug("resourceFoundInSupportedResourcesOfService= " + resourceFoundInSupportedResourcesOfService);
          if ( !resourceFoundInSupportedResourcesOfService ) {
            ServiceUpdate supd = new ServiceUpdate();
            
            ResourceRef rref = new ResourceRef();
            rref.id(res.getId()).name(res.getName());
            supd.addSupportingResourceItem(rref );
            
            //copy characteristics from resource to service
            for (org.etsi.osl.tmf.ri639.model.Characteristic rChar : res.getResourceCharacteristic()) {
              Characteristic cNew = new Characteristic();
              cNew.setName( rChar.getName());
              cNew.value( new Any( rChar.getValue() ));                
              supd.addServiceCharacteristicItem( cNew );  
            }
            
            
            Note n = new Note();
            n.setText("Supporting Resource "+ res.getId() + " Added in service" );
            n.setAuthor( "SIM638-API" );
            n.setDate( OffsetDateTime.now(ZoneOffset.UTC).toString() );
            supd.addNoteItem( n );                  
            
            this.updateService( aService.getId(), supd , true, null, null); //update the service
          }
         
        }
        
      }
      
    }
    
	
}
