package org.etsi.osl.tmf.gsm674.reposervices;

import org.etsi.osl.tmf.gsm674.model.GeographicSite;
import org.etsi.osl.tmf.gsm674.repo.GeographicSiteManagementRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class GeographicSiteManagementService {
    private static final Logger log = LoggerFactory.getLogger(GeographicSiteManagementService.class);


    private final GeographicSiteManagementRepository geographicSiteManagementRepository;

    @Autowired
    public GeographicSiteManagementService(GeographicSiteManagementRepository geographicSiteManagementRepository) {
        this.geographicSiteManagementRepository = geographicSiteManagementRepository;
    }

    public List<GeographicSite> findAllGeographicSites(){
        return (List<GeographicSite>) geographicSiteManagementRepository.findAll();
    }

    public Optional<GeographicSite> findGeographicSiteByUUID(String uuid){
        return geographicSiteManagementRepository.findByUuid(uuid);
    }

   public GeographicSite createGeographicSite(GeographicSite geographicSite){
        log.info("Add another geographic site: {}",geographicSite);
        return geographicSiteManagementRepository.save(geographicSite);

   }

   public GeographicSite updateGeographicSite(String id,GeographicSite geographicSite){
        Optional<GeographicSite> gs=geographicSiteManagementRepository.findByUuid(id);
       return gs.map(site -> updateFields(geographicSite, site)).orElse(null);
   }

   public Void deleteGeographicSiteById(String id){
        GeographicSite gs=geographicSiteManagementRepository.findByUuid(id).orElseThrow();
        geographicSiteManagementRepository.delete(gs);
       return null;
   }

   private GeographicSite updateFields(GeographicSite newSite, GeographicSite existingSite){


       geographicSiteManagementRepository.save(existingSite);
       return existingSite;
   }

}
