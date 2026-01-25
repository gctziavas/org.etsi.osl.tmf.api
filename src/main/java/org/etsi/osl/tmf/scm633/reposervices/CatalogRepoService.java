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
import java.util.List;
import java.util.Optional;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.hibernate5.jakarta.Hibernate5JakartaModule;
import org.etsi.osl.tmf.common.model.ELifecycle;
import org.etsi.osl.tmf.common.model.TimePeriod;
import org.etsi.osl.tmf.scm633.model.ServiceCatalog;
import org.etsi.osl.tmf.scm633.model.ServiceCatalogCreate;
import org.etsi.osl.tmf.scm633.model.ServiceCatalogUpdate;
import org.etsi.osl.tmf.scm633.model.ServiceCategory;
import org.etsi.osl.tmf.scm633.model.ServiceCategoryRef;
import org.etsi.osl.tmf.scm633.model.ServiceSpecCharacteristic;
import org.etsi.osl.tmf.scm633.model.ServiceSpecification;
import org.etsi.osl.tmf.scm633.repo.CatalogRepository;
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
@Transactional
public class CatalogRepoService {


	@Autowired
	CatalogRepository catalogRepo;

	@Autowired
	CategoryRepoService categRepoService;

	@Autowired
	ServiceSpecificationRepoService specRepoService;	    

	@Autowired
	ServiceCatalogNotificationService serviceCatalogNotificationService;

	public ServiceCatalog addCatalog(ServiceCatalog c) {
		ServiceCatalog savedCatalog = this.catalogRepo.save(c);
		serviceCatalogNotificationService.publishServiceCatalogCreateNotification(savedCatalog);
		return savedCatalog;
	}

	public ServiceCatalog addCatalog(@Valid ServiceCatalogCreate serviceCat) {

		ServiceCatalog sc = new ServiceCatalog();

		sc = updateCatalogDataFromAPICall(sc, serviceCat);
		ServiceCatalog savedCatalog = this.catalogRepo.save(sc);
		serviceCatalogNotificationService.publishServiceCatalogCreateNotification(savedCatalog);
		return savedCatalog;
	}

	public String findAllEager() {
	  
	  List<ServiceCatalog> oids = (List<ServiceCatalog>) this.catalogRepo.findByOrderByName();
	  ObjectMapper mapper = new ObjectMapper();
      // Registering Hibernate4Module to support lazy objects
      // this will fetch all lazy objects before marshaling
      mapper.registerModule(new Hibernate5JakartaModule());
      String res;
      try {
        res = mapper.writeValueAsString( oids );
      } catch (JsonProcessingException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
        return "{}";
      }
      
	  return res;
	}

	   
	   
	public List<ServiceCatalog> findAll() {
      this.catalogRepo.findByOrderByName().stream().forEach(s->s.getCategoryObj().size());
      this.catalogRepo.findByOrderByName().stream().forEach(s->s.getCategoryObj().stream().forEach( c -> c.getServiceCandidateObj().size()) );
		return (List<ServiceCatalog>) this.catalogRepo.findByOrderByName();
	}

	
	 public String findByUuidEager(String id) {
       
	      ServiceCatalog sc = this.findById(id);

	      ObjectMapper mapper = new ObjectMapper();
	      // Registering Hibernate4Module to support lazy objects
	      // this will fetch all lazy objects before marshaling
	      mapper.registerModule(new Hibernate5JakartaModule());
	      String res;
	      try {
	        res = mapper.writeValueAsString( sc );
	      } catch (JsonProcessingException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
	        return "{}";
	      }
	      
	      return res;
	 }
	 
	 
	 
	public String findByNameEager(String aname) {
	  ServiceCatalog sc = this.findByName(aname);

	  ObjectMapper mapper = new ObjectMapper();
      // Registering Hibernate4Module to support lazy objects
      // this will fetch all lazy objects before marshaling
      mapper.registerModule(new Hibernate5JakartaModule());
      String res;
      try {
        res = mapper.writeValueAsString( sc );
      } catch (JsonProcessingException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
        return "{}";
      }
      
      return res;
	       
	}
  
	
	public ServiceCatalog findById(String id) {
		Optional<ServiceCatalog> optionalCat = this.catalogRepo.findByUuid(id);
		return optionalCat.orElse(null);
	}
	

	public ServiceCatalog findByName(String aName) {
		Optional<ServiceCatalog> optionalCat = this.catalogRepo.findByName( aName );
		if ( optionalCat.isPresent() ) {
          optionalCat.get().getCategoryObj().size();
          optionalCat.get().getCategoryRefs().size();
		}
		return optionalCat.orElse(null);
	}

	public Void deleteById(String id) {
		Optional<ServiceCatalog> optionalCat = this.catalogRepo.findByUuid(id);
		if (optionalCat.isPresent()) {
			ServiceCatalog catalogToDelete = optionalCat.get();
			serviceCatalogNotificationService.publishServiceCatalogDeleteNotification(catalogToDelete);
			this.catalogRepo.delete(catalogToDelete);
		}
		return null;
	}

	public ServiceCatalog updateCatalog(String id, ServiceCatalogUpdate serviceCatalog) {

		Optional<ServiceCatalog> optSC = catalogRepo.findByUuid(id);
		if (optSC == null) {
			return null;
		}
		ServiceCatalog sc = optSC.get();
		sc = updateCatalogDataFromAPICall(sc, serviceCatalog);
		return this.catalogRepo.save(sc);
	}

	public ServiceCatalog updateCatalogDataFromAPICall(ServiceCatalog sc, ServiceCatalogUpdate serviceCatalog) {
		
		if (serviceCatalog.getName()!=null){
			sc.setName(serviceCatalog.getName());			
		}
		if (serviceCatalog.getDescription()!=null){
			sc.setDescription(serviceCatalog.getDescription());			
		}
		if (serviceCatalog.getLifecycleStatus() != null) {
			sc.setLifecycleStatusEnum(ELifecycle.getEnum(serviceCatalog.getLifecycleStatus()));
		}
		if (serviceCatalog.getVersion() != null) {
			sc.setVersion(serviceCatalog.getVersion());
		}
		sc.setLastUpdate(OffsetDateTime.now(ZoneOffset.UTC));
		TimePeriod tp = new TimePeriod();
		if (serviceCatalog.getValidFor() != null) {
			tp.setStartDateTime(serviceCatalog.getValidFor().getStartDateTime());
			tp.setEndDateTime(serviceCatalog.getValidFor().getEndDateTime());
			sc.setValidFor(tp);
		}

		// add any new category
		if (serviceCatalog.getCategory() != null) {

			sc.getCategoryObj().clear();
			for (ServiceCategoryRef scref : serviceCatalog.getCategory()) {
				ServiceCategory servcat = this.categRepoService.findByUuid(scref.getId());
				sc.addCategory(servcat);
			}
		}

		return sc;

	}

	public ServiceCatalog updateCatalog(ServiceCatalog scatalog) {
		return this.catalogRepo.save(scatalog);
	}
	
	
	/**
	 * return recursively all categories in catalog
	 * @param catalogName
	 */
	@Transactional
	public String findAllCategoriesByCatalogName(String catalogName) {
	  String res="[]";

	  Optional<ServiceCatalog> scopt = this.catalogRepo.findByName(catalogName);
	  
	  if (scopt.isEmpty() ) {
	      return res;	    
	  }
	  
	  ServiceCatalog sc = scopt.get();
	  
	  sc.getCategoryRefs();


	  List<ServiceCategory> allcategories = this.getCategories( sc.getCategoryRefs());
	  
	  ObjectMapper mapper = new ObjectMapper();
      // Registering Hibernate4Module to support lazy objects
      // this will fetch all lazy objects before marshaling
      mapper.registerModule(new Hibernate5JakartaModule());     
      
      
      try {
        res = mapper.writeValueAsString( allcategories );
      } catch (JsonProcessingException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      
      
      return res;
      
	}
	
	
	@Transactional
	  private List<ServiceCategory> getCategories( @Valid List<ServiceCategoryRef> list) {
	    List<ServiceCategory> categories = new ArrayList<ServiceCategory>();
	    
	    for (ServiceCategoryRef c : list ) {	      
	     ServiceCategory category = this.categRepoService.findByUuid( c.getId());
	     categories.add(category);
	     
	     if (category.getCategoryRefs()!=null && category.getCategoryRefs().size()>0) {
	         List<ServiceCategory> subcategories = this.getCategories( category.getCategoryRefs() );
	         categories.addAll(subcategories );//add children
	     }
	      
	    }
	    
	    return categories;
	  }

	


	

}
