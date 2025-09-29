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
import org.etsi.osl.tmf.pcm620.model.ProductSpecification;
import org.etsi.osl.tmf.pcm620.model.ProductSpecificationCreateEvent;
import org.etsi.osl.tmf.pcm620.model.ProductSpecificationCreateEventPayload;
import org.etsi.osl.tmf.pcm620.model.ProductSpecificationCreateNotification;
import org.etsi.osl.tmf.pcm620.model.ProductSpecificationDeleteEvent;
import org.etsi.osl.tmf.pcm620.model.ProductSpecificationDeleteEventPayload;
import org.etsi.osl.tmf.pcm620.model.ProductSpecificationDeleteNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProductSpecificationNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(ProductSpecificationNotificationService.class);

    @Autowired
    private ProductCatalogApiRouteBuilderEvents eventPublisher;

    @Autowired
    private ProductSpecificationCallbackService productSpecificationCallbackService;

    /**
     * Publish a product specification create notification
     * @param productSpecification The created product specification
     */
    public void publishProductSpecificationCreateNotification(ProductSpecification productSpecification) {
        try {
            ProductSpecificationCreateNotification notification = createProductSpecificationCreateNotification(productSpecification);
            eventPublisher.publishEvent(notification, productSpecification.getUuid());
            
            // Send callbacks to registered subscribers
            productSpecificationCallbackService.sendProductSpecificationCreateCallback(notification.getEvent());
            
            logger.info("Published product specification create notification for product spec ID: {}", productSpecification.getUuid());
        } catch (Exception e) {
            logger.error("Error publishing product specification create notification for product spec ID: {}", productSpecification.getUuid(), e);
        }
    }

    /**
     * Publish a product specification delete notification
     * @param productSpecification The deleted product specification
     */
    public void publishProductSpecificationDeleteNotification(ProductSpecification productSpecification) {
        try {
            ProductSpecificationDeleteNotification notification = createProductSpecificationDeleteNotification(productSpecification);
            eventPublisher.publishEvent(notification, productSpecification.getUuid());
            
            // Send callbacks to registered subscribers
            productSpecificationCallbackService.sendProductSpecificationDeleteCallback(notification.getEvent());
            
            logger.info("Published product specification delete notification for product spec ID: {}", productSpecification.getUuid());
        } catch (Exception e) {
            logger.error("Error publishing product specification delete notification for product spec ID: {}", productSpecification.getUuid(), e);
        }
    }

    /**
     * Create a product specification create notification
     * @param productSpecification The created product specification
     * @return ProductSpecificationCreateNotification
     */
    private ProductSpecificationCreateNotification createProductSpecificationCreateNotification(ProductSpecification productSpecification) {
        ProductSpecificationCreateNotification notification = new ProductSpecificationCreateNotification();
        
        // Set common notification properties
        notification.setEventId(UUID.randomUUID().toString());
        notification.setEventTime(OffsetDateTime.now(ZoneOffset.UTC));
        notification.setEventType(ProductSpecificationCreateNotification.class.getName());
        notification.setResourcePath("/productCatalogManagement/v4/productSpecification/" + productSpecification.getUuid());

        // Create event
        ProductSpecificationCreateEvent event = new ProductSpecificationCreateEvent();
        event.setEventId(notification.getEventId());
        event.setEventTime(notification.getEventTime());
        event.setEventType("ProductSpecificationCreateEvent");
        event.setTitle("Product Specification Create Event");
        event.setDescription("A product specification has been created");
        event.setTimeOcurred(notification.getEventTime());

        // Create event payload
        ProductSpecificationCreateEventPayload payload = new ProductSpecificationCreateEventPayload();
        payload.setProductSpecification(productSpecification);
        event.setEvent(payload);

        notification.setEvent(event);
        return notification;
    }

    /**
     * Create a product specification delete notification
     * @param productSpecification The deleted product specification
     * @return ProductSpecificationDeleteNotification
     */
    private ProductSpecificationDeleteNotification createProductSpecificationDeleteNotification(ProductSpecification productSpecification) {
        ProductSpecificationDeleteNotification notification = new ProductSpecificationDeleteNotification();
        
        // Set common notification properties
        notification.setEventId(UUID.randomUUID().toString());
        notification.setEventTime(OffsetDateTime.now(ZoneOffset.UTC));
        notification.setEventType(ProductSpecificationDeleteNotification.class.getName());
        notification.setResourcePath("/productCatalogManagement/v4/productSpecification/" + productSpecification.getUuid());

        // Create event
        ProductSpecificationDeleteEvent event = new ProductSpecificationDeleteEvent();
        event.setEventId(notification.getEventId());
        event.setEventTime(notification.getEventTime());
        event.setEventType("ProductSpecificationDeleteEvent");
        event.setTitle("Product Specification Delete Event");
        event.setDescription("A product specification has been deleted");
        event.setTimeOcurred(notification.getEventTime());

        // Create event payload
        ProductSpecificationDeleteEventPayload payload = new ProductSpecificationDeleteEventPayload();
        payload.setProductSpecification(productSpecification);
        event.setEvent(payload);

        notification.setEvent(event);
        return notification;
    }
}