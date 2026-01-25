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
import java.util.UUID;

import org.etsi.osl.tmf.pcm620.api.ProductCatalogApiRouteBuilderEvents;
import org.etsi.osl.tmf.pcm620.model.Catalog;
import org.etsi.osl.tmf.pcm620.model.CatalogCreateEvent;
import org.etsi.osl.tmf.pcm620.model.CatalogCreateEventPayload;
import org.etsi.osl.tmf.pcm620.model.CatalogCreateNotification;
import org.etsi.osl.tmf.pcm620.model.CatalogDeleteEvent;
import org.etsi.osl.tmf.pcm620.model.CatalogDeleteEventPayload;
import org.etsi.osl.tmf.pcm620.model.CatalogDeleteNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CatalogNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(CatalogNotificationService.class);

    @Autowired
    private ProductCatalogApiRouteBuilderEvents eventPublisher;

    @Autowired
    private CatalogCallbackService catalogCallbackService;

    /**
     * Publish a catalog create notification
     * @param catalog The created catalog
     */
    public void publishCatalogCreateNotification(Catalog catalog) {
        try {
            CatalogCreateNotification notification = createCatalogCreateNotification(catalog);
            eventPublisher.publishEvent(notification, catalog.getUuid());
            
            // Send callbacks to registered subscribers
            if ( catalogCallbackService!=null )
              catalogCallbackService.sendCatalogCreateCallback(notification.getEvent());
            
            logger.info("Published catalog create notification for catalog ID: {}", catalog.getUuid());
        } catch (Exception e) {
            logger.error("Error publishing catalog create notification for catalog ID: {}", catalog.getUuid(), e);
        }
    }

    /**
     * Publish a catalog delete notification
     * @param catalog The deleted catalog
     */
    public void publishCatalogDeleteNotification(Catalog catalog) {
        try {
            CatalogDeleteNotification notification = createCatalogDeleteNotification(catalog);
            eventPublisher.publishEvent(notification, catalog.getUuid());
            
            // Send callbacks to registered subscribers
            if ( catalogCallbackService!=null )
              catalogCallbackService.sendCatalogDeleteCallback(notification.getEvent());
            
            logger.info("Published catalog delete notification for catalog ID: {}", catalog.getUuid());
        } catch (Exception e) {
            logger.error("Error publishing catalog delete notification for catalog ID: {}", catalog.getUuid(), e);
        }
    }

    /**
     * Create a catalog create notification
     * @param catalog The created catalog
     * @return CatalogCreateNotification
     */
    private CatalogCreateNotification createCatalogCreateNotification(Catalog catalog) {
        CatalogCreateNotification notification = new CatalogCreateNotification();
        
        // Set common notification properties
        notification.setEventId(UUID.randomUUID().toString());
        notification.setEventTime(OffsetDateTime.now(ZoneOffset.UTC));
        notification.setEventType(CatalogCreateNotification.class.getName());
        notification.setResourcePath("/productCatalogManagement/v4/catalog/" + catalog.getUuid());

        // Create event
        CatalogCreateEvent event = new CatalogCreateEvent();
        event.setEventId(notification.getEventId());
        event.setEventTime(notification.getEventTime());
        event.setEventType("CatalogCreateEvent");
        event.setTitle("Catalog Create Event");
        event.setDescription("A catalog has been created");
        event.setTimeOcurred(notification.getEventTime());

        // Create event payload
        CatalogCreateEventPayload payload = new CatalogCreateEventPayload();
        payload.setCatalog(catalog);
        event.setEvent(payload);

        notification.setEvent(event);
        return notification;
    }

    /**
     * Create a catalog delete notification
     * @param catalog The deleted catalog
     * @return CatalogDeleteNotification
     */
    private CatalogDeleteNotification createCatalogDeleteNotification(Catalog catalog) {
        CatalogDeleteNotification notification = new CatalogDeleteNotification();
        
        // Set common notification properties
        notification.setEventId(UUID.randomUUID().toString());
        notification.setEventTime(OffsetDateTime.now(ZoneOffset.UTC));
        notification.setEventType(CatalogDeleteNotification.class.getName());
        notification.setResourcePath("/productCatalogManagement/v4/catalog/" + catalog.getUuid());

        // Create event
        CatalogDeleteEvent event = new CatalogDeleteEvent();
        event.setEventId(notification.getEventId());
        event.setEventTime(notification.getEventTime());
        event.setEventType("CatalogDeleteEvent");
        event.setTitle("Catalog Delete Event");
        event.setDescription("A catalog has been deleted");
        event.setTimeOcurred(notification.getEventTime());

        // Create event payload
        CatalogDeleteEventPayload payload = new CatalogDeleteEventPayload();
        payload.setCatalog(catalog);
        event.setEvent(payload);

        notification.setEvent(event);
        return notification;
    }
}