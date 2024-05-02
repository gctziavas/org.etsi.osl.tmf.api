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

        if(newSite.getDescription()!=null) existingSite.setDescription(newSite.getDescription());
        if(newSite.getCode()!=null) existingSite.setCode(newSite.getCode());
        if (newSite.getStatus()!=null) existingSite.setStatus(newSite.getStatus());

        if(newSite.getSiteRelationship() !=null){
            for(GeographicSiteRelationship n : newSite.getSiteRelationship()){
                 if(n.getUuid()==null){
                    existingSite.addSiteRelationshipItem(n);
                }else {
                     for (GeographicSiteRelationship oldGeographicRelationship : existingSite.getSiteRelationship()){
                         if (n.getUuid().equals(oldGeographicRelationship.getUuid())){
                             if (n.getRole() !=null) oldGeographicRelationship.setRole(n.getRole());
                             if (n.getRelationshipType() !=null) oldGeographicRelationship.setRelationshipType(n.getRelationshipType());
                             if (n.getValidFor() !=null) oldGeographicRelationship.setValidFor(n.getValidFor());
                         }
                     }
                 }
            }
        }

        if(newSite.getCalendar()!=null){
           for(CalendarPeriod c: newSite.getCalendar()){
               if(c.getUuid()==null){
                   existingSite.addCalendarItem(c);
               } else {
                   for (CalendarPeriod oldCalendarPeriod: existingSite.getCalendar()){
                       if (c.getUuid().equals(oldCalendarPeriod.getUuid())){
                           if( c.getDay() !=null) oldCalendarPeriod.setDay(c.getDay());
                           if( c.getStatus() !=null) oldCalendarPeriod.setStatus(c.getStatus());
                           if( c.getTimeZone() !=null) oldCalendarPeriod.setTimeZone(c.getTimeZone());
                           if( c.getHourPeriod() !=null) oldCalendarPeriod.setHourPeriod(c.getHourPeriod());
                       }
                   }
               }
           }
        }

        if(newSite.getPlace()!=null){
            for(PlaceRefOrValue p: newSite.getPlace()){
                if (p.getUuid()==null){
                    existingSite.addPlaceItem(p);
                }
            }
        }

        if(newSite.getRelatedParty()!=null){
            for(RelatedParty party: newSite.getRelatedParty()){
                if(party.getUuid()==null){
                    existingSite.addRelatedPartyItem(party);
                } else {
                    for (RelatedParty rp: existingSite.getRelatedParty()){
                        if(party.getUuid().equals(rp.getUuid())){
                            if (party.getRole() !=null) rp.setRole(party.getRole());
                            if (party.getName() !=null) rp.setName(party.getName());
                        }
                    }
                }
            }
        }

       geographicSiteManagementRepository.save(existingSite);
       return existingSite;
   }

}
