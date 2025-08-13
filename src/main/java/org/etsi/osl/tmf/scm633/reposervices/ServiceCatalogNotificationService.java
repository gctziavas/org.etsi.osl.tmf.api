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
package org.etsi.osl.tmf.scm633.reposervices;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import org.etsi.osl.tmf.scm633.api.ServiceCatalogApiRouteBuilderEvents;
import org.etsi.osl.tmf.scm633.model.ServiceCatalog;
import org.etsi.osl.tmf.scm633.model.ServiceCatalogCreateEvent;
import org.etsi.osl.tmf.scm633.model.ServiceCatalogCreateNotification;
import org.etsi.osl.tmf.scm633.model.ServiceCatalogDeleteEvent;
import org.etsi.osl.tmf.scm633.model.ServiceCatalogDeleteNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ServiceCatalogNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(ServiceCatalogNotificationService.class);

    @Autowired
    private ServiceCatalogApiRouteBuilderEvents eventPublisher;

    @Autowired
    private ServiceCatalogCallbackService serviceCatalogCallbackService;

    /**
     * Publish a service catalog create notification
     * @param serviceCatalog The created service catalog
     */
    public void publishServiceCatalogCreateNotification(ServiceCatalog serviceCatalog) {
        try {
            ServiceCatalogCreateNotification notification = createServiceCatalogCreateNotification(serviceCatalog);
            eventPublisher.publishEvent(notification, serviceCatalog.getUuid());
            
            // Send callbacks to registered subscribers
            serviceCatalogCallbackService.sendServiceCatalogCreateCallback(notification);
            
            logger.info("Published service catalog create notification for service catalog ID: {}", serviceCatalog.getUuid());
        } catch (Exception e) {
            logger.error("Error publishing service catalog create notification for service catalog ID: {}", serviceCatalog.getUuid(), e);
        }
    }

    /**
     * Publish a service catalog delete notification
     * @param serviceCatalog The deleted service catalog
     */
    public void publishServiceCatalogDeleteNotification(ServiceCatalog serviceCatalog) {
        try {
            ServiceCatalogDeleteNotification notification = createServiceCatalogDeleteNotification(serviceCatalog);
            eventPublisher.publishEvent(notification, serviceCatalog.getUuid());
            
            // Send callbacks to registered subscribers
            serviceCatalogCallbackService.sendServiceCatalogDeleteCallback(notification);
            
            logger.info("Published service catalog delete notification for service catalog ID: {}", serviceCatalog.getUuid());
        } catch (Exception e) {
            logger.error("Error publishing service catalog delete notification for service catalog ID: {}", serviceCatalog.getUuid(), e);
        }
    }

    /**
     * Create a service catalog create notification
     * @param serviceCatalog The created service catalog
     * @return ServiceCatalogCreateNotification
     */
    private ServiceCatalogCreateNotification createServiceCatalogCreateNotification(ServiceCatalog serviceCatalog) {
        ServiceCatalogCreateNotification notification = new ServiceCatalogCreateNotification();
        
        // Set common notification properties
        notification.setEventId(UUID.randomUUID().toString());
        notification.setEventTime(OffsetDateTime.now(ZoneOffset.UTC));
        notification.setEventType(ServiceCatalogCreateNotification.class.getName());
        notification.setResourcePath("/serviceCatalogManagement/v4/serviceCatalog/" + serviceCatalog.getUuid());

        // Create event
        ServiceCatalogCreateEvent event = new ServiceCatalogCreateEvent();
        event.setServiceCatalog(serviceCatalog);

        notification.setEvent(event);
        return notification;
    }

    /**
     * Create a service catalog delete notification
     * @param serviceCatalog The deleted service catalog
     * @return ServiceCatalogDeleteNotification
     */
    private ServiceCatalogDeleteNotification createServiceCatalogDeleteNotification(ServiceCatalog serviceCatalog) {
        ServiceCatalogDeleteNotification notification = new ServiceCatalogDeleteNotification();
        
        // Set common notification properties
        notification.setEventId(UUID.randomUUID().toString());
        notification.setEventTime(OffsetDateTime.now(ZoneOffset.UTC));
        notification.setEventType(ServiceCatalogDeleteNotification.class.getName());
        notification.setResourcePath("/serviceCatalogManagement/v4/serviceCatalog/" + serviceCatalog.getUuid());

        // Create event
        ServiceCatalogDeleteEvent event = new ServiceCatalogDeleteEvent();
        event.setServiceCatalog(serviceCatalog);

        notification.setEvent(event);
        return notification;
    }
}