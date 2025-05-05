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
public class CatalogRepoService {


	@Autowired
	CatalogRepository catalogRepo;

	@Autowired
	CategoryRepoService categRepoService;

	@Autowired
	ServiceSpecificationRepoService specRepoService;

	@Autowired
	CandidateRepoService candidateRepoService;
	

    private SessionFactory sessionFactory;
    
    @Autowired
    public CatalogRepoService(EntityManagerFactory factory) {
        if (factory.unwrap(SessionFactory.class) == null) {
            throw new NullPointerException("factory is not a hibernate factory");
        }
        this.sessionFactory = factory.unwrap(SessionFactory.class);
    }
    

	public ServiceCatalog addCatalog(ServiceCatalog c) {

		return this.catalogRepo.save(c);
	}

	public ServiceCatalog addCatalog(@Valid ServiceCatalogCreate serviceCat) {

		ServiceCatalog sc = new ServiceCatalog();

		sc = updateCatalogDataFromAPICall(sc, serviceCat);
		return this.catalogRepo.save(sc);
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
		return (List<ServiceCatalog>) this.catalogRepo.findByOrderByName();
	}

	
	 public ServiceCatalog findByUuidEager(String id) {
       

       Session session = sessionFactory.openSession();
       Transaction tx = session.beginTransaction(); // instead of begin transaction, is it possible to continue?
       try {
           ServiceCatalog dd = null;
           try {
               dd = session.get(ServiceCatalog.class, id);
               if (dd == null) {
                   return this.findById(id);// last resort
               }
               Hibernate.initialize(dd.getCategoryRefs() );
               Hibernate.initialize(dd.getRelatedParty() );
               Hibernate.initialize(dd.getCategoryObj() );

               tx.commit();
           } finally {
               session.close();
           }
           return dd;
           
       } catch (Exception e) {
           e.printStackTrace();
       }

       session.close();
       return null;
       
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
		return optionalCat.orElse(null);
	}

	public Void deleteById(String id) {
		Optional<ServiceCatalog> optionalCat = this.catalogRepo.findByUuid(id);
		this.catalogRepo.delete(optionalCat.get());
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
