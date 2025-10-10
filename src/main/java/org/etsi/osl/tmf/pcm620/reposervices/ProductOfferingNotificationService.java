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
import org.etsi.osl.tmf.pcm620.model.ProductOffering;
import org.etsi.osl.tmf.pcm620.model.ProductOfferingCreateEvent;
import org.etsi.osl.tmf.pcm620.model.ProductOfferingCreateEventPayload;
import org.etsi.osl.tmf.pcm620.model.ProductOfferingCreateNotification;
import org.etsi.osl.tmf.pcm620.model.ProductOfferingDeleteEvent;
import org.etsi.osl.tmf.pcm620.model.ProductOfferingDeleteEventPayload;
import org.etsi.osl.tmf.pcm620.model.ProductOfferingDeleteNotification;
import org.etsi.osl.tmf.pcm620.model.ProductOfferingAttributeValueChangeEvent;
import org.etsi.osl.tmf.pcm620.model.ProductOfferingAttributeValueChangeEventPayload;
import org.etsi.osl.tmf.pcm620.model.ProductOfferingAttributeValueChangeNotification;
import org.etsi.osl.tmf.pcm620.model.ProductOfferingStateChangeEvent;
import org.etsi.osl.tmf.pcm620.model.ProductOfferingStateChangeEventPayload;
import org.etsi.osl.tmf.pcm620.model.ProductOfferingStateChangeNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProductOfferingNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(ProductOfferingNotificationService.class);

    @Autowired
    private ProductCatalogApiRouteBuilderEvents eventPublisher;

    @Autowired
    private ProductOfferingCallbackService productOfferingCallbackService;

    /**
     * Publish a product offering create notification
     * @param productOffering The created product offering
     */
    public void publishProductOfferingCreateNotification(ProductOffering productOffering) {
        try {
            ProductOfferingCreateNotification notification = createProductOfferingCreateNotification(productOffering);
            eventPublisher.publishEvent(notification, productOffering.getUuid());
            
            // Send callbacks to registered subscribers
            productOfferingCallbackService.sendProductOfferingCreateCallback(notification.getEvent());
            
            logger.info("Published product offering create notification for product offering ID: {}", productOffering.getUuid());
        } catch (Exception e) {
            logger.error("Error publishing product offering create notification for product offering ID: {}", productOffering.getUuid(), e);
        }
    }

    /**
     * Publish a product offering delete notification
     * @param productOffering The deleted product offering
     */
    public void publishProductOfferingDeleteNotification(ProductOffering productOffering) {
        try {
            ProductOfferingDeleteNotification notification = createProductOfferingDeleteNotification(productOffering);
            eventPublisher.publishEvent(notification, productOffering.getUuid());
            
            // Send callbacks to registered subscribers
            productOfferingCallbackService.sendProductOfferingDeleteCallback(notification.getEvent());
            
            logger.info("Published product offering delete notification for product offering ID: {}", productOffering.getUuid());
        } catch (Exception e) {
            logger.error("Error publishing product offering delete notification for product offering ID: {}", productOffering.getUuid(), e);
        }
    }

    /**
     * Publish a product offering attribute value change notification
     * @param productOffering The product offering with changed attributes
     */
    public void publishProductOfferingAttributeValueChangeNotification(ProductOffering productOffering) {
        try {
            ProductOfferingAttributeValueChangeNotification notification = createProductOfferingAttributeValueChangeNotification(productOffering);
            eventPublisher.publishEvent(notification, productOffering.getUuid());
            
            // Send callbacks to registered subscribers
            productOfferingCallbackService.sendProductOfferingAttributeValueChangeCallback(notification.getEvent());
            
            logger.info("Published product offering attribute value change notification for product offering ID: {}", productOffering.getUuid());
        } catch (Exception e) {
            logger.error("Error publishing product offering attribute value change notification for product offering ID: {}", productOffering.getUuid(), e);
        }
    }

    /**
     * Publish a product offering state change notification
     * @param productOffering The product offering with changed state
     */
    public void publishProductOfferingStateChangeNotification(ProductOffering productOffering) {
        try {
            ProductOfferingStateChangeNotification notification = createProductOfferingStateChangeNotification(productOffering);
            eventPublisher.publishEvent(notification, productOffering.getUuid());
            
            // Send callbacks to registered subscribers
            productOfferingCallbackService.sendProductOfferingStateChangeCallback(notification.getEvent());
            
            logger.info("Published product offering state change notification for product offering ID: {}", productOffering.getUuid());
        } catch (Exception e) {
            logger.error("Error publishing product offering state change notification for product offering ID: {}", productOffering.getUuid(), e);
        }
    }

    /**
     * Create a product offering create notification
     * @param productOffering The created product offering
     * @return ProductOfferingCreateNotification
     */
    private ProductOfferingCreateNotification createProductOfferingCreateNotification(ProductOffering productOffering) {
        ProductOfferingCreateNotification notification = new ProductOfferingCreateNotification();
        
        // Set common notification properties
        notification.setEventId(UUID.randomUUID().toString());
        notification.setEventTime(OffsetDateTime.now(ZoneOffset.UTC));
        notification.setEventType(ProductOfferingCreateNotification.class.getName());
        notification.setResourcePath("/productCatalogManagement/v4/productOffering/" + productOffering.getUuid());

        // Create event
        ProductOfferingCreateEvent event = new ProductOfferingCreateEvent();
        event.setEventId(notification.getEventId());
        event.setEventTime(notification.getEventTime());
        event.setEventType("ProductOfferingCreateEvent");
        event.setTitle("Product Offering Create Event");
        event.setDescription("A product offering has been created");
        event.setTimeOcurred(notification.getEventTime());

        // Create event payload
        ProductOfferingCreateEventPayload payload = new ProductOfferingCreateEventPayload();
        payload.setProductOffering(productOffering);
        event.setEvent(payload);

        notification.setEvent(event);
        return notification;
    }

    /**
     * Create a product offering delete notification
     * @param productOffering The deleted product offering
     * @return ProductOfferingDeleteNotification
     */
    private ProductOfferingDeleteNotification createProductOfferingDeleteNotification(ProductOffering productOffering) {
        ProductOfferingDeleteNotification notification = new ProductOfferingDeleteNotification();
        
        // Set common notification properties
        notification.setEventId(UUID.randomUUID().toString());
        notification.setEventTime(OffsetDateTime.now(ZoneOffset.UTC));
        notification.setEventType(ProductOfferingDeleteNotification.class.getName());
        notification.setResourcePath("/productCatalogManagement/v4/productOffering/" + productOffering.getUuid());

        // Create event
        ProductOfferingDeleteEvent event = new ProductOfferingDeleteEvent();
        event.setEventId(notification.getEventId());
        event.setEventTime(notification.getEventTime());
        event.setEventType("ProductOfferingDeleteEvent");
        event.setTitle("Product Offering Delete Event");
        event.setDescription("A product offering has been deleted");
        event.setTimeOcurred(notification.getEventTime());

        // Create event payload
        ProductOfferingDeleteEventPayload payload = new ProductOfferingDeleteEventPayload();
        payload.setProductOffering(productOffering);
        event.setEvent(payload);

        notification.setEvent(event);
        return notification;
    }

    /**
     * Create a product offering attribute value change notification
     * @param productOffering The product offering with changed attributes
     * @return ProductOfferingAttributeValueChangeNotification
     */
    private ProductOfferingAttributeValueChangeNotification createProductOfferingAttributeValueChangeNotification(ProductOffering productOffering) {
        ProductOfferingAttributeValueChangeNotification notification = new ProductOfferingAttributeValueChangeNotification();
        
        // Set common notification properties
        notification.setEventId(UUID.randomUUID().toString());
        notification.setEventTime(OffsetDateTime.now(ZoneOffset.UTC));
        notification.setEventType(ProductOfferingAttributeValueChangeNotification.class.getName());
        notification.setResourcePath("/productCatalogManagement/v4/productOffering/" + productOffering.getUuid());

        // Create event
        ProductOfferingAttributeValueChangeEvent event = new ProductOfferingAttributeValueChangeEvent();
        event.setEventId(notification.getEventId());
        event.setEventTime(notification.getEventTime());
        event.setEventType("ProductOfferingAttributeValueChangeEvent");
        event.setTitle("Product Offering Attribute Value Change Event");
        event.setDescription("A product offering attribute value has been changed");
        event.setTimeOcurred(notification.getEventTime());

        // Create event payload
        ProductOfferingAttributeValueChangeEventPayload payload = new ProductOfferingAttributeValueChangeEventPayload();
        payload.setProductOffering(productOffering);
        event.setEvent(payload);

        notification.setEvent(event);
        return notification;
    }

    /**
     * Create a product offering state change notification
     * @param productOffering The product offering with changed state
     * @return ProductOfferingStateChangeNotification
     */
    private ProductOfferingStateChangeNotification createProductOfferingStateChangeNotification(ProductOffering productOffering) {
        ProductOfferingStateChangeNotification notification = new ProductOfferingStateChangeNotification();
        
        // Set common notification properties
        notification.setEventId(UUID.randomUUID().toString());
        notification.setEventTime(OffsetDateTime.now(ZoneOffset.UTC));
        notification.setEventType(ProductOfferingStateChangeNotification.class.getName());
        notification.setResourcePath("/productCatalogManagement/v4/productOffering/" + productOffering.getUuid());

        // Create event
        ProductOfferingStateChangeEvent event = new ProductOfferingStateChangeEvent();
        event.setEventId(notification.getEventId());
        event.setEventTime(notification.getEventTime());
        event.setEventType("ProductOfferingStateChangeEvent");
        event.setTitle("Product Offering State Change Event");
        event.setDescription("A product offering state has been changed");
        event.setTimeOcurred(notification.getEventTime());

        // Create event payload
        ProductOfferingStateChangeEventPayload payload = new ProductOfferingStateChangeEventPayload();
        payload.setProductOffering(productOffering);
        event.setEvent(payload);

        notification.setEvent(event);
        return notification;
    }
}