package org.etsi.osl.tmf.gsm674.reposervices;

import org.etsi.osl.tmf.gsm674.model.CalendarPeriod;
import org.etsi.osl.tmf.gsm674.model.GeographicSite;
import org.etsi.osl.tmf.gsm674.model.GeographicSiteRelationship;
import org.etsi.osl.tmf.gsm674.model.PlaceRefOrValue;
import org.etsi.osl.tmf.gsm674.repo.GeographicSiteManagementRepository;
import org.etsi.osl.tmf.prm669.model.RelatedParty;
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

    public GeographicSite findGeographicSiteByUUID(String uuid){
        Optional<GeographicSite> gs=geographicSiteManagementRepository.findByUuid(uuid);
        return gs.orElse(null);

    }

   public GeographicSite createGeographicSite(GeographicSite geographicSite){
        log.info("Add another geographic site: {}",geographicSite);
        return geographicSiteManagementRepository.save(geographicSite);

   }

   public GeographicSite updateGeographicSite(String id,GeographicSite geographicSite){
        log.info("Update geographic site with id: {}",id);
        Optional<GeographicSite> gs=geographicSiteManagementRepository.findByUuid(id);
       return gs.map(site -> updateFields(geographicSite, site)).orElse(null);
   }

   public Void deleteGeographicSiteById(String id){
       log.info("Delete geographic site with id: {}",id);
        GeographicSite gs=geographicSiteManagementRepository.findByUuid(id).orElseThrow();
        geographicSiteManagementRepository.delete(gs);
       return null;
   }

   private GeographicSite updateFields(GeographicSite newSite, GeographicSite existingSite){

        if(newSite.getName()!=null) existingSite.setCalendar(newSite.getCalendar());
        if(newSite.getDescription()!=null) existingSite.setDescription(newSite.getDescription());
        if(newSite.getCode()!=null) existingSite.setCode(newSite.getCode());
        if (newSite.getStatus()!=null) existingSite.setStatus(newSite.getStatus());

        if(newSite.getGeographicSiteRelationship()!=null){
            for(GeographicSiteRelationship n : newSite.getGeographicSiteRelationship()){
                 if(n.getUuid()==null){
                    existingSite.addGeographicSiteRelationship(n);
                }
            }
        }

        if(newSite.getCalendar()!=null){
           for(CalendarPeriod c: newSite.getCalendar()){
               if(c.getUuid()==null){
                   existingSite.addCalendarPeriod(c);
               }
           }
        }

        if(newSite.getPlaceRefOrValue()!=null){
            for(PlaceRefOrValue p: newSite.getPlaceRefOrValue()){
                if (p.getUuid()==null){
                    existingSite.addPlaceRefOrValue(p);
                }
            }
        }

        if(newSite.getRelatedParties()!=null){
            for(RelatedParty party: newSite.getRelatedParties()){
                if(party.getUuid()==null){
                    existingSite.addRelatedParty(party);
                }
            }
        }

       geographicSiteManagementRepository.save(existingSite);
       return existingSite;
   }

}
