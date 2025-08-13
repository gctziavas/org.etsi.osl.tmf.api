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
import org.etsi.osl.tmf.scm633.model.ServiceCategory;
import org.etsi.osl.tmf.scm633.model.ServiceCategoryCreateEvent;
import org.etsi.osl.tmf.scm633.model.ServiceCategoryCreateNotification;
import org.etsi.osl.tmf.scm633.model.ServiceCategoryDeleteEvent;
import org.etsi.osl.tmf.scm633.model.ServiceCategoryDeleteNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ServiceCategoryNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(ServiceCategoryNotificationService.class);

    @Autowired
    private ServiceCatalogApiRouteBuilderEvents eventPublisher;

    @Autowired
    private ServiceCatalogCallbackService serviceCatalogCallbackService;

    /**
     * Publish a service category create notification
     * @param serviceCategory The created service category
     */
    public void publishServiceCategoryCreateNotification(ServiceCategory serviceCategory) {
        try {
            ServiceCategoryCreateNotification notification = createServiceCategoryCreateNotification(serviceCategory);
            eventPublisher.publishEvent(notification, serviceCategory.getUuid());
            
            // Send callbacks to registered subscribers
            serviceCatalogCallbackService.sendServiceCategoryCreateCallback(notification.getEvent());
            
            logger.info("Published service category create notification for service category ID: {}", serviceCategory.getUuid());
        } catch (Exception e) {
            logger.error("Error publishing service category create notification for service category ID: {}", serviceCategory.getUuid(), e);
        }
    }

    /**
     * Publish a service category delete notification
     * @param serviceCategory The deleted service category
     */
    public void publishServiceCategoryDeleteNotification(ServiceCategory serviceCategory) {
        try {
            ServiceCategoryDeleteNotification notification = createServiceCategoryDeleteNotification(serviceCategory);
            eventPublisher.publishEvent(notification, serviceCategory.getUuid());
            
            // Send callbacks to registered subscribers
            serviceCatalogCallbackService.sendServiceCategoryDeleteCallback(notification.getEvent());
            
            logger.info("Published service category delete notification for service category ID: {}", serviceCategory.getUuid());
        } catch (Exception e) {
            logger.error("Error publishing service category delete notification for service category ID: {}", serviceCategory.getUuid(), e);
        }
    }

    /**
     * Create a service category create notification
     * @param serviceCategory The created service category
     * @return ServiceCategoryCreateNotification
     */
    private ServiceCategoryCreateNotification createServiceCategoryCreateNotification(ServiceCategory serviceCategory) {
        ServiceCategoryCreateNotification notification = new ServiceCategoryCreateNotification();
        
        // Set common notification properties
        notification.setEventId(UUID.randomUUID().toString());
        notification.setEventTime(OffsetDateTime.now(ZoneOffset.UTC));
        notification.setEventType(ServiceCategoryCreateNotification.class.getName());
        notification.setResourcePath("/serviceCatalogManagement/v4/serviceCategory/" + serviceCategory.getUuid());

        // Create event
        ServiceCategoryCreateEvent event = new ServiceCategoryCreateEvent();
        event.setServiceCategory(serviceCategory);

        notification.setEvent(event);
        return notification;
    }

    /**
     * Create a service category delete notification
     * @param serviceCategory The deleted service category
     * @return ServiceCategoryDeleteNotification
     */
    private ServiceCategoryDeleteNotification createServiceCategoryDeleteNotification(ServiceCategory serviceCategory) {
        ServiceCategoryDeleteNotification notification = new ServiceCategoryDeleteNotification();
        
        // Set common notification properties
        notification.setEventId(UUID.randomUUID().toString());
        notification.setEventTime(OffsetDateTime.now(ZoneOffset.UTC));
        notification.setEventType(ServiceCategoryDeleteNotification.class.getName());
        notification.setResourcePath("/serviceCatalogManagement/v4/serviceCategory/" + serviceCategory.getUuid());

        // Create event
        ServiceCategoryDeleteEvent event = new ServiceCategoryDeleteEvent();
        event.setServiceCategory(serviceCategory);

        notification.setEvent(event);
        return notification;
    }
}