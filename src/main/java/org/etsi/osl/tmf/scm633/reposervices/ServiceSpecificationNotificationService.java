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
import org.etsi.osl.tmf.scm633.model.ServiceSpecification;
import org.etsi.osl.tmf.scm633.model.ServiceSpecificationChangeEvent;
import org.etsi.osl.tmf.scm633.model.ServiceSpecificationChangeNotification;
import org.etsi.osl.tmf.scm633.model.ServiceSpecificationCreateEvent;
import org.etsi.osl.tmf.scm633.model.ServiceSpecificationCreateNotification;
import org.etsi.osl.tmf.scm633.model.ServiceSpecificationDeleteEvent;
import org.etsi.osl.tmf.scm633.model.ServiceSpecificationDeleteNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ServiceSpecificationNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(ServiceSpecificationNotificationService.class);

    @Autowired
    private ServiceCatalogApiRouteBuilderEvents eventPublisher;

    @Autowired
    private ServiceCatalogCallbackService serviceCatalogCallbackService;

    /**
     * Publish a service specification create notification
     * @param serviceSpecification The created service specification
     */
    public void publishServiceSpecificationCreateNotification(ServiceSpecification serviceSpecification) {
        try {
            ServiceSpecificationCreateNotification notification = createServiceSpecificationCreateNotification(serviceSpecification);
            eventPublisher.publishEvent(notification, serviceSpecification.getUuid());
            
            // Send callbacks to registered subscribers
            serviceCatalogCallbackService.sendServiceSpecificationCreateCallback(notification);
            
            logger.info("Published service specification create notification for service specification ID: {}", serviceSpecification.getUuid());
        } catch (Exception e) {
            logger.error("Error publishing service specification create notification for service specification ID: {}", serviceSpecification.getUuid(), e);
        }
    }

    /**
     * Publish a service specification delete notification
     * @param serviceSpecification The deleted service specification
     */
    public void publishServiceSpecificationDeleteNotification(ServiceSpecification serviceSpecification) {
        try {
            ServiceSpecificationDeleteNotification notification = createServiceSpecificationDeleteNotification(serviceSpecification);
            eventPublisher.publishEvent(notification, serviceSpecification.getUuid());
            
            // Send callbacks to registered subscribers
            serviceCatalogCallbackService.sendServiceSpecificationDeleteCallback(notification);
            
            logger.info("Published service specification delete notification for service specification ID: {}", serviceSpecification.getUuid());
        } catch (Exception e) {
            logger.error("Error publishing service specification delete notification for service specification ID: {}", serviceSpecification.getUuid(), e);
        }
    }

    /**
     * Publish a service specification change notification
     * @param serviceSpecification The changed service specification
     */
    public void publishServiceSpecificationChangeNotification(ServiceSpecification serviceSpecification) {
        try {
            ServiceSpecificationChangeNotification notification = createServiceSpecificationChangeNotification(serviceSpecification);
            eventPublisher.publishEvent(notification, serviceSpecification.getUuid());
            
            // Send callbacks to registered subscribers
            serviceCatalogCallbackService.sendServiceSpecificationChangeCallback(notification);
            
            logger.info("Published service specification change notification for service specification ID: {}", serviceSpecification.getUuid());
        } catch (Exception e) {
            logger.error("Error publishing service specification change notification for service specification ID: {}", serviceSpecification.getUuid(), e);
        }
    }

    /**
     * Create a service specification create notification
     * @param serviceSpecification The created service specification
     * @return ServiceSpecificationCreateNotification
     */
    private ServiceSpecificationCreateNotification createServiceSpecificationCreateNotification(ServiceSpecification serviceSpecification) {
        ServiceSpecificationCreateNotification notification = new ServiceSpecificationCreateNotification();
        
        // Set common notification properties
        notification.setEventId(UUID.randomUUID().toString());
        notification.setEventTime(OffsetDateTime.now(ZoneOffset.UTC));
        notification.setEventType(ServiceSpecificationCreateNotification.class.getName());
        notification.setResourcePath("/serviceCatalogManagement/v4/serviceSpecification/" + serviceSpecification.getUuid());

        // Create event
        ServiceSpecificationCreateEvent event = new ServiceSpecificationCreateEvent();
        event.setServiceSpecification(serviceSpecification);

        notification.setEvent(event);
        return notification;
    }

    /**
     * Create a service specification delete notification
     * @param serviceSpecification The deleted service specification
     * @return ServiceSpecificationDeleteNotification
     */
    private ServiceSpecificationDeleteNotification createServiceSpecificationDeleteNotification(ServiceSpecification serviceSpecification) {
        ServiceSpecificationDeleteNotification notification = new ServiceSpecificationDeleteNotification();
        
        // Set common notification properties
        notification.setEventId(UUID.randomUUID().toString());
        notification.setEventTime(OffsetDateTime.now(ZoneOffset.UTC));
        notification.setEventType(ServiceSpecificationDeleteNotification.class.getName());
        notification.setResourcePath("/serviceCatalogManagement/v4/serviceSpecification/" + serviceSpecification.getUuid());

        // Create event
        ServiceSpecificationDeleteEvent event = new ServiceSpecificationDeleteEvent();
        event.setServiceSpecification(serviceSpecification);

        notification.setEvent(event);
        return notification;
    }

    /**
     * Create a service specification change notification
     * @param serviceSpecification The changed service specification
     * @return ServiceSpecificationChangeNotification
     */
    private ServiceSpecificationChangeNotification createServiceSpecificationChangeNotification(ServiceSpecification serviceSpecification) {
        ServiceSpecificationChangeNotification notification = new ServiceSpecificationChangeNotification();
        
        // Set common notification properties
        notification.setEventId(UUID.randomUUID().toString());
        notification.setEventTime(OffsetDateTime.now(ZoneOffset.UTC));
        notification.setEventType(ServiceSpecificationChangeNotification.class.getName());
        notification.setResourcePath("/serviceCatalogManagement/v4/serviceSpecification/" + serviceSpecification.getUuid());

        // Create event
        ServiceSpecificationChangeEvent event = new ServiceSpecificationChangeEvent();
        event.setServiceSpecification(serviceSpecification);

        notification.setEvent(event);
        return notification;
    }
}