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
package org.etsi.osl.tmf.scm633.reposervices;

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
import org.etsi.osl.tmf.scm633.model.ServiceCandidate;
import org.etsi.osl.tmf.scm633.model.ServiceCandidateRef;
import org.etsi.osl.tmf.scm633.model.ServiceCategory;
import org.etsi.osl.tmf.scm633.model.ServiceCategoryCreate;
import org.etsi.osl.tmf.scm633.model.ServiceCategoryRef;
import org.etsi.osl.tmf.scm633.model.ServiceCategoryUpdate;
import org.etsi.osl.tmf.scm633.repo.CandidateRepository;
import org.etsi.osl.tmf.scm633.repo.CategoriesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.Valid;

@Service
@Transactional
public class CategoryRepoService {



	private final CategoriesRepository categsRepo;
	
	private final CandidateRepository candidateRepo;

	
    @Autowired
    public CategoryRepoService(CategoriesRepository categsRepo, CandidateRepository candidateRepo) {
        this.categsRepo = categsRepo;
        this.candidateRepo = candidateRepo;
    }
	
	public ServiceCategory addCategory(ServiceCategory c) {

		return this.getCategsRepo().save( c );
	}

	public ServiceCategory addCategory(@Valid ServiceCategoryCreate serviceCategory) {	
		
		
		ServiceCategory sc = new ServiceCategory() ;
		sc = updateCategoryDataFromAPICall(sc, serviceCategory);
		return this.getCategsRepo().save( sc );
		
	}

    @Transactional
	public List<ServiceCategory> findAll() {
		return (List<ServiceCategory>) this.getCategsRepo().findByOrderByName();
	}

    @Transactional
	public ServiceCategory findByUuid(String id) {
		Optional<ServiceCategory> optionalCat = this.getCategsRepo().findByUuid( id );
		return optionalCat
				.orElse(null);
	}
	

    @Transactional
	public String findByIdEager(String id) {
		ServiceCategory sc = this.findByUuid( id );

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
	
	


	@Transactional
    public String findAllServiceSpecRefsByCategId(String categoryId) {
      

      String res="[]";
      List<ServiceSpecificationRef> serviceSpecificationRefList = new ArrayList<>();
      ServiceCategory category = this.findByUuid(categoryId);      
      
      if ( category == null ) {
        return res;
      }
      
      Set<ServiceCandidate> serviceCands = category.getServiceCandidateObj();
      
      for (ServiceCandidate serviceCandidate : serviceCands) {
        @Valid
        ServiceSpecificationRef specRef = serviceCandidate.getServiceSpecificationRef();
        serviceSpecificationRefList.add(specRef);
      }
      

      ObjectMapper mapper = new ObjectMapper();
      // Registering Hibernate4Module to support lazy objects
      // this will fetch all lazy objects before marshaling
      mapper.registerModule(new Hibernate5JakartaModule());     
      
      try {
        res = mapper.writeValueAsString( serviceSpecificationRefList );
      } catch (JsonProcessingException e) {
        e.printStackTrace();
      }
      
      
      return res;
      
    }
	

	public boolean deleteById(String id) {
		Optional<ServiceCategory> optionalCat = this.getCategsRepo().findByUuid( id );
		if ( optionalCat.get().getCategoryObj().size()>0 ) {
			return false; //has children
		}
		
		
		if ( optionalCat.get().getParentId() != null ) {
			ServiceCategory parentCat = (this.getCategsRepo().findByUuid( optionalCat.get().getParentId() )).get();
			
			//remove from parent category
			for (ServiceCategory ss : parentCat.getCategoryObj()) {
				if  ( ss.getId()  == optionalCat.get().getId() ) {
					 parentCat.getCategoryObj().remove(ss);
					 break;
				}
			}			
			parentCat = this.getCategsRepo().save(parentCat);
		}
		
		
		this.getCategsRepo().delete( optionalCat.get());
		return true;
		
	}

	public ServiceCategory updateCategory(String id, @Valid ServiceCategoryUpdate serviceCategory) {
		Optional<ServiceCategory> optionalCat = this.getCategsRepo().findByUuid( id );
		if ( optionalCat == null ) {
			return null;
		}
		
		ServiceCategory sc = optionalCat.get();
		sc = updateCategoryDataFromAPICall(sc, serviceCategory);
		return this.getCategsRepo().save( sc );
	}
	
	public ServiceCategory updateCategoryDataFromAPICall( ServiceCategory sc, ServiceCategoryUpdate serviceCatUpd )
	{		
		if (serviceCatUpd.getName()!=null) {
			sc.setName( serviceCatUpd.getName()  );			
		}
		if (serviceCatUpd.getDescription()!=null) {
			sc.setDescription( serviceCatUpd.getDescription());			
		}
		if ( serviceCatUpd.isIsRoot() != null  ) {
			sc.setIsRoot( serviceCatUpd.isIsRoot());			
		}
		
		if ( serviceCatUpd.getLifecycleStatus() != null ) {
			sc.setLifecycleStatusEnum ( ELifecycle.getEnum( serviceCatUpd.getLifecycleStatus() ) );
		}
		

		if ( serviceCatUpd.getVersion() != null ) {
			sc.setVersion( serviceCatUpd.getVersion() );		
		}
		sc.setLastUpdate( OffsetDateTime.now(ZoneOffset.UTC) );
		TimePeriod tp = new TimePeriod();
		if ( serviceCatUpd.getValidFor() != null ) {
			tp.setStartDateTime( serviceCatUpd.getValidFor().getStartDateTime() );
			tp.setEndDateTime( serviceCatUpd.getValidFor().getEndDateTime() );
			sc.setValidFor( tp );
		} 
		
		if ( serviceCatUpd.getCategory() !=null ) {
			//reattach fromDB
			Map<String, Boolean> idAddedUpdated = new HashMap<>();
			
			for (ServiceCategoryRef ref : serviceCatUpd.getCategory() ) {
				//find  by id and reload it here.
				boolean idexists = false;
				for (ServiceCategory originalSCat : sc.getCategoryObj()) {
					if ( originalSCat.getId().equals( ref.getId())) {
						idexists = true;
						idAddedUpdated.put( originalSCat.getId(), true);
						break;
					}					
				}
				if (!idexists) {
					Optional<ServiceCategory> catToAdd = this.getCategsRepo().findByUuid( ref.getId() );
					if ( catToAdd.isPresent() ) {
						ServiceCategory scatadd = catToAdd.get();
						sc.getCategoryObj().add( scatadd );
						idAddedUpdated.put( ref.getId(), true);		
						
						scatadd.setParentId( sc.getUuid());
						scatadd = this.getCategsRepo().save( scatadd );
					}
				}
			}
			List<ServiceCategory> toRemove = new ArrayList<>();
			for (ServiceCategory ss : sc.getCategoryObj()) {
				if ( idAddedUpdated.get( ss.getId() ) == null ) {
					toRemove.add(ss);
				}
			}
			
			for (ServiceCategory ar : toRemove) {
				sc.getCategoryObj().remove(ar);
			}
		}
		
		
		if ( serviceCatUpd.getServiceCandidate() !=null ) {
			//reattach fromDB
			Map<String, Boolean> idAddedUpdated = new HashMap<>();
			
			for (ServiceCandidateRef ref : serviceCatUpd.getServiceCandidate() ) {
				//find  by id and reload it here.
				boolean idexists = false;
				for (ServiceCandidate originalSCat : sc.getServiceCandidateObj()) {
					if ( originalSCat.getId().equals( ref.getId())) {
						idexists = true;
						idAddedUpdated.put( originalSCat.getId(), true);
						break;
					}					
				}
				if (!idexists) {
					Optional<ServiceCandidate> catToAdd = this.candidateRepo.findByUuid( ref.getId() );
					if ( catToAdd.isPresent() ) {
						ServiceCandidate scatadd = catToAdd.get();
						sc.getServiceCandidateObj().add( scatadd );
						idAddedUpdated.put( ref.getId(), true);		
												
					}
				}
			}
			List<ServiceCandidate> toRemove = new ArrayList<>();
			for (ServiceCandidate ss : sc.getServiceCandidateObj()) {
				if ( idAddedUpdated.get( ss.getId() ) == null ) {
					toRemove.add(ss);
				}
			}
			
			for (ServiceCandidate ar : toRemove) {
				sc.getServiceCandidateObj().remove(ar);
			}
		}
		
		return sc;
	}


	public ServiceCategory findByName(String aName) {
		Optional<ServiceCategory> optionalCat = this.getCategsRepo().findByName( aName );
		return optionalCat
				.orElse(null);
	}

  public CategoriesRepository getCategsRepo() {
    return categsRepo;
    
  }


}
