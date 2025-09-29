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
package org.etsi.osl.tmf.pcm620.reposervices;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.hibernate5.jakarta.Hibernate5JakartaModule;
import org.etsi.osl.tmf.common.model.ELifecycle;
import org.etsi.osl.tmf.common.model.TimePeriod;
import org.etsi.osl.tmf.common.model.service.ServiceSpecificationRef;
import org.etsi.osl.tmf.pcm620.model.Category;
import org.etsi.osl.tmf.pcm620.model.CategoryCreate;
import org.etsi.osl.tmf.pcm620.model.CategoryRef;
import org.etsi.osl.tmf.pcm620.model.CategoryUpdate;
import org.etsi.osl.tmf.pcm620.model.ProductOffering;
import org.etsi.osl.tmf.pcm620.model.ProductOfferingRef;
import org.etsi.osl.tmf.pcm620.model.ProductSpecificationRef;
import org.etsi.osl.tmf.pcm620.repo.ProductCatalogRepository;
import org.etsi.osl.tmf.pcm620.repo.ProductCategoriesRepository;
import org.etsi.osl.tmf.pcm620.repo.ProductOfferingRepository;
import org.etsi.osl.tmf.scm633.model.ServiceCandidate;
import org.etsi.osl.tmf.scm633.model.ServiceCategory;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManagerFactory;
import jakarta.validation.Valid;

@Service
public class ProductCategoryRepoService {

	private final ProductCategoriesRepository categsRepo;
	
	private final ProductOfferingRepository prodsOfferingRepo;
	
	@Autowired
	private CategoryNotificationService categoryNotificationService;

	/**
	 * from
	 * https://stackoverflow.com/questions/25063995/spring-boot-handle-to-hibernate-sessionfactory
	 * 
	 * @param factory
	 */
	@Autowired
	public ProductCategoryRepoService( ProductCategoriesRepository categsRepo, 
	    ProductOfferingRepository prodsOfferingRepo) {
	  
	  this.categsRepo = categsRepo;
	  this.prodsOfferingRepo = prodsOfferingRepo;
	}
	
	
	public Category addCategory(Category c) {
		Category savedCategory = this.categsRepo.save( c );
		
		// Publish category create notification
		if (categoryNotificationService != null) {
			categoryNotificationService.publishCategoryCreateNotification(savedCategory);
		}
		
		return savedCategory;
	}

	public Category addCategory(@Valid CategoryCreate Category) {	
		
		
		Category sc = new Category() ;
		sc = updateCategoryDataFromAPICall(sc, Category);
		Category savedCategory = this.categsRepo.save( sc );
		
		// Publish category create notification
		if (categoryNotificationService != null) {
			categoryNotificationService.publishCategoryCreateNotification(savedCategory);
		}
		
		return savedCategory;
		
	}

	public List<Category> findAll() {
		return (List<Category>) this.categsRepo.findByOrderByName();
	}

	public Category findByUuid(String id) {
		Optional<Category> optionalCat = this.categsRepo.findByUuid( id );
		return optionalCat
				.orElse(null);
	}
	

    @Transactional
    public String findByIdEager(String id) {
        Category sc = this.findByUuid( id );

        String res= "{}";
        
        if ( sc == null ) {
          return res;
        }
          
        
        ObjectMapper mapper = new ObjectMapper();
          // Registering Hibernate4Module to support lazy objects
          // this will fetch all lazy objects before marshaling
          mapper.registerModule(new Hibernate5JakartaModule());     
          
          try {
            res = mapper.writeValueAsString( sc );
          } catch (JsonProcessingException e) {
            e.printStackTrace();
          }
          
          
          return res;
    }
	
	
	
	

	public boolean deleteById(String id) {
		Optional<Category> optionalCat = this.categsRepo.findByUuid( id );
		if ( optionalCat.get().getCategoryObj().size()>0 ) {
			return false; //has children
		}
		
		Category categoryToDelete = optionalCat.get();
		
		// Trigger lazy loading of associations before deletion to avoid lazy initialization exception
		categoryToDelete.getProductOfferingObj().size(); // This will initialize the lazy collection
		categoryToDelete.getCategoryObj().size(); // This will initialize the lazy collection
		
		// Publish category delete notification BEFORE deletion to ensure session is still active
		if (categoryNotificationService != null) {
			categoryNotificationService.publishCategoryDeleteNotification(categoryToDelete);
		}
		
		if ( categoryToDelete.getParentId() != null ) {
			Category parentCat = (this.categsRepo.findByUuid( categoryToDelete.getParentId() )).get();
			
			//remove from parent category
			for (Category ss : parentCat.getCategoryObj()) {
				if  ( ss.getId()  == categoryToDelete.getId() ) {
					 parentCat.getCategoryObj().remove(ss);
					 break;
				}
			}			
			parentCat = this.categsRepo.save(parentCat);
		}
		
		this.categsRepo.delete( categoryToDelete);
		
		return true;
		
	}

	public Category updateCategory(String id, @Valid CategoryUpdate Category) {
		Optional<Category> optionalCat = this.categsRepo.findByUuid( id );
		if ( optionalCat == null ) {
			return null;
		}
		
		Category sc = optionalCat.get();
		sc = updateCategoryDataFromAPICall(sc, Category);
		return this.categsRepo.save( sc );
	}
	
	public Category updateCategoryDataFromAPICall( Category acat, CategoryUpdate prodCatUpd )
	{		
		if (prodCatUpd.getName()!=null) {
			acat.setName( prodCatUpd.getName()  );			
		}
		if (prodCatUpd.getDescription()!=null) {
			acat.setDescription( prodCatUpd.getDescription());			
		}
		if ( prodCatUpd.isIsRoot() != null  ) {
			acat.setIsRoot( prodCatUpd.isIsRoot());			
		}
		
		if ( prodCatUpd.getLifecycleStatus() != null ) {
			acat.setLifecycleStatusEnum ( ELifecycle.getEnum( prodCatUpd.getLifecycleStatus() ) );
		}
		

		if ( prodCatUpd.getVersion() != null ) {
			acat.setVersion( prodCatUpd.getVersion() );		
		}
		acat.setLastUpdate( OffsetDateTime.now(ZoneOffset.UTC) );
		TimePeriod tp = new TimePeriod();
		if ( prodCatUpd.getValidFor() != null ) {
			tp.setStartDateTime( prodCatUpd.getValidFor().getStartDateTime() );
			tp.setEndDateTime( prodCatUpd.getValidFor().getEndDateTime() );
			acat.setValidFor( tp );
		} 
		
		if ( prodCatUpd.getProductOffering() != null ) {
			//reattach fromDB
			Map<String, Boolean> idAddedUpdated = new HashMap<>();
			for (ProductOfferingRef por : prodCatUpd.getProductOffering()) {
				//find  by id and reload it here.
				boolean idexists = false;
				for (ProductOfferingRef originalProfOffRef : acat.getProductOfferingRefs()) {
					if ( originalProfOffRef.getId().equals( por.getId())) {
						idexists = true;
						idAddedUpdated.put( originalProfOffRef.getId(), true);
						break;
					}					
				}
				if (!idexists) {
					Optional<ProductOffering> profOffToAdd = this.prodsOfferingRepo.findByUuid( por.getId() );
					if ( profOffToAdd.isPresent() ) {
						ProductOffering poffget = profOffToAdd.get();
						
							
						acat.getProductOfferingObj().add(poffget);						
						idAddedUpdated.put( poffget.getId(), true);
						
						
						
					}
				}				
			}
			List<ProductOfferingRef> toRemove = new ArrayList<>();
			for (ProductOfferingRef ss : acat.getProductOfferingRefs()) {
				if ( idAddedUpdated.get( ss.getId() ) == null ) {
					toRemove.add(ss);
				}
			}
			
			for (ProductOfferingRef ar : toRemove) {
				Optional<ProductOffering> profOffToDelete = this.prodsOfferingRepo.findByUuid( ar.getId() );
				if ( profOffToDelete.isPresent() ) {
					acat.getProductOfferingObj().remove(profOffToDelete.get());
					
				}
			}
			
		}
				
				
		if ( prodCatUpd.getSubCategory() !=null ) {
			//reattach fromDB
			Map<String, Boolean> idAddedUpdated = new HashMap<>();
			
			for (CategoryRef ref : prodCatUpd.getSubCategory() ) {
				//find  by id and reload it here.
				boolean idexists = false;
				for (Category originalSCat : acat.getCategoryObj()) {
					if ( originalSCat.getId().equals( ref.getId())) {
						idexists = true;
						idAddedUpdated.put( originalSCat.getId(), true);
						break;
					}					
				}
				if (!idexists) {
					Optional<Category> catToAdd = this.categsRepo.findByUuid( ref.getId() );
					if ( catToAdd.isPresent() ) {
						Category scatadd = catToAdd.get();
						acat.getCategoryObj().add( scatadd );
						idAddedUpdated.put( ref.getId(), true);		
						
						scatadd.setParentId( acat.getUuid());
						scatadd = this.categsRepo.save( scatadd );
					}
				}
			}
			List<Category> toRemove = new ArrayList<>();
			for (Category ss : acat.getCategoryObj()) {
				if ( idAddedUpdated.get( ss.getId() ) == null ) {
					toRemove.add(ss);
				}
			}
			
			for (Category ar : toRemove) {
				acat.getCategoryObj().remove(ar);
			}
		}
		
		
//		if ( prodCatUpd.getServiceCandidate() !=null ) {
//			//reattach fromDB
//			Map<String, Boolean> idAddedUpdated = new HashMap<>();
//			
//			for (ServiceCandidateRef ref : prodCatUpd.getServiceCandidate() ) {
//				//find  by id and reload it here.
//				boolean idexists = false;
//				for (ServiceCandidate originalSCat : acat.getServiceCandidateObj()) {
//					if ( originalSCat.getId().equals( ref.getId())) {
//						idexists = true;
//						idAddedUpdated.put( originalSCat.getId(), true);
//						break;
//					}					
//				}
//				if (!idexists) {
//					Optional<ServiceCandidate> catToAdd = this.candidateRepo.findByUuid( ref.getId() );
//					if ( catToAdd.isPresent() ) {
//						ServiceCandidate scatadd = catToAdd.get();
//						acat.getServiceCandidateObj().add( scatadd );
//						idAddedUpdated.put( ref.getId(), true);		
//												
//					}
//				}
//			}
//			List<ServiceCandidate> toRemove = new ArrayList<>();
//			for (ServiceCandidate ss : acat.getServiceCandidateObj()) {
//				if ( idAddedUpdated.get( ss.getId() ) == null ) {
//					toRemove.add(ss);
//				}
//			}
//			
//			for (ServiceCandidate ar : toRemove) {
//				acat.getServiceCandidateObj().remove(ar);
//			}
//		}
		
		return acat;
	}


	public Category findByName(String aName) {
		Optional<Category> optionalCat = this.categsRepo.findByName( aName );
		return optionalCat
				.orElse(null);
	}
	
	   @Transactional
	    public String findAllProductOfferingsByCategId(String categoryId) {
	      

	      String res="[]";
	      List<ProductSpecificationRef> productSpecificationRefList = new ArrayList<>();
	      Category category = this.findByUuid(categoryId);      
	      
	      if ( category == null ) {
	        return res;
	      }
	      
	      Set<ProductOffering> proffs = category.getProductOfferingObj();


	      ObjectMapper mapper = new ObjectMapper();
	      // Registering Hibernate4Module to support lazy objects
	      // this will fetch all lazy objects before marshaling
	      mapper.registerModule(new Hibernate5JakartaModule());     
	      
	      try {
	        res = mapper.writeValueAsString( proffs );
	      } catch (JsonProcessingException e) {
	        e.printStackTrace();
	      }
	      
	      
	      return res;
	      
	    }
}
