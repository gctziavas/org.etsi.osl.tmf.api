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
import org.etsi.osl.tmf.pcm620.model.ProductOfferingPrice;
import org.etsi.osl.tmf.pcm620.model.ProductOfferingPriceCreateEvent;
import org.etsi.osl.tmf.pcm620.model.ProductOfferingPriceCreateEventPayload;
import org.etsi.osl.tmf.pcm620.model.ProductOfferingPriceCreateNotification;
import org.etsi.osl.tmf.pcm620.model.ProductOfferingPriceDeleteEvent;
import org.etsi.osl.tmf.pcm620.model.ProductOfferingPriceDeleteEventPayload;
import org.etsi.osl.tmf.pcm620.model.ProductOfferingPriceDeleteNotification;
import org.etsi.osl.tmf.pcm620.model.ProductOfferingPriceAttributeValueChangeEvent;
import org.etsi.osl.tmf.pcm620.model.ProductOfferingPriceAttributeValueChangeEventPayload;
import org.etsi.osl.tmf.pcm620.model.ProductOfferingPriceAttributeValueChangeNotification;
import org.etsi.osl.tmf.pcm620.model.ProductOfferingPriceStateChangeEvent;
import org.etsi.osl.tmf.pcm620.model.ProductOfferingPriceStateChangeEventPayload;
import org.etsi.osl.tmf.pcm620.model.ProductOfferingPriceStateChangeNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProductOfferingPriceNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(ProductOfferingPriceNotificationService.class);

    @Autowired
    private ProductCatalogApiRouteBuilderEvents eventPublisher;

    @Autowired
    private ProductOfferingPriceCallbackService productOfferingPriceCallbackService;

    /**
     * Publish a product offering price create notification
     * @param productOfferingPrice The created product offering price
     */
    public void publishProductOfferingPriceCreateNotification(ProductOfferingPrice productOfferingPrice) {
        try {
            ProductOfferingPriceCreateNotification notification = createProductOfferingPriceCreateNotification(productOfferingPrice);
            eventPublisher.publishEvent(notification, productOfferingPrice.getId());
            
            // Send callbacks to registered subscribers
            productOfferingPriceCallbackService.sendProductOfferingPriceCreateCallback(notification.getEvent());
            
            logger.info("Published product offering price create notification for product offering price ID: {}", productOfferingPrice.getId());
        } catch (Exception e) {
            logger.error("Error publishing product offering price create notification for product offering price ID: {}", productOfferingPrice.getId(), e);
        }
    }

    /**
     * Publish a product offering price delete notification
     * @param productOfferingPrice The deleted product offering price
     */
    public void publishProductOfferingPriceDeleteNotification(ProductOfferingPrice productOfferingPrice) {
        try {
            ProductOfferingPriceDeleteNotification notification = createProductOfferingPriceDeleteNotification(productOfferingPrice);
            eventPublisher.publishEvent(notification, productOfferingPrice.getId());
            
            // Send callbacks to registered subscribers
            productOfferingPriceCallbackService.sendProductOfferingPriceDeleteCallback(notification.getEvent());
            
            logger.info("Published product offering price delete notification for product offering price ID: {}", productOfferingPrice.getId());
        } catch (Exception e) {
            logger.error("Error publishing product offering price delete notification for product offering price ID: {}", productOfferingPrice.getId(), e);
        }
    }

    /**
     * Publish a product offering price attribute value change notification
     * @param productOfferingPrice The product offering price with changed attributes
     */
    public void publishProductOfferingPriceAttributeValueChangeNotification(ProductOfferingPrice productOfferingPrice) {
        try {
            ProductOfferingPriceAttributeValueChangeNotification notification = createProductOfferingPriceAttributeValueChangeNotification(productOfferingPrice);
            eventPublisher.publishEvent(notification, productOfferingPrice.getId());
            
            // Send callbacks to registered subscribers
            productOfferingPriceCallbackService.sendProductOfferingPriceAttributeValueChangeCallback(notification.getEvent());
            
            logger.info("Published product offering price attribute value change notification for product offering price ID: {}", productOfferingPrice.getId());
        } catch (Exception e) {
            logger.error("Error publishing product offering price attribute value change notification for product offering price ID: {}", productOfferingPrice.getId(), e);
        }
    }

    /**
     * Publish a product offering price state change notification
     * @param productOfferingPrice The product offering price with changed state
     */
    public void publishProductOfferingPriceStateChangeNotification(ProductOfferingPrice productOfferingPrice) {
        try {
            ProductOfferingPriceStateChangeNotification notification = createProductOfferingPriceStateChangeNotification(productOfferingPrice);
            eventPublisher.publishEvent(notification, productOfferingPrice.getId());
            
            // Send callbacks to registered subscribers
            productOfferingPriceCallbackService.sendProductOfferingPriceStateChangeCallback(notification.getEvent());
            
            logger.info("Published product offering price state change notification for product offering price ID: {}", productOfferingPrice.getId());
        } catch (Exception e) {
            logger.error("Error publishing product offering price state change notification for product offering price ID: {}", productOfferingPrice.getId(), e);
        }
    }

    /**
     * Create a product offering price create notification
     * @param productOfferingPrice The created product offering price
     * @return ProductOfferingPriceCreateNotification
     */
    private ProductOfferingPriceCreateNotification createProductOfferingPriceCreateNotification(ProductOfferingPrice productOfferingPrice) {
        ProductOfferingPriceCreateNotification notification = new ProductOfferingPriceCreateNotification();
        
        // Set common notification properties
        notification.setEventId(UUID.randomUUID().toString());
        notification.setEventTime(OffsetDateTime.now(ZoneOffset.UTC));
        notification.setEventType(ProductOfferingPriceCreateNotification.class.getName());
        notification.setResourcePath("/productCatalogManagement/v4/productOfferingPrice/" + productOfferingPrice.getId());

        // Create event
        ProductOfferingPriceCreateEvent event = new ProductOfferingPriceCreateEvent();
        event.setEventId(notification.getEventId());
        event.setEventTime(notification.getEventTime());
        event.setEventType("ProductOfferingPriceCreateEvent");
        event.setTitle("Product Offering Price Create Event");
        event.setDescription("A product offering price has been created");
        event.setTimeOcurred(notification.getEventTime());

        // Create event payload
        ProductOfferingPriceCreateEventPayload payload = new ProductOfferingPriceCreateEventPayload();
        payload.setProductOfferingPrice(productOfferingPrice);
        event.setEvent(payload);

        notification.setEvent(event);
        return notification;
    }

    /**
     * Create a product offering price delete notification
     * @param productOfferingPrice The deleted product offering price
     * @return ProductOfferingPriceDeleteNotification
     */
    private ProductOfferingPriceDeleteNotification createProductOfferingPriceDeleteNotification(ProductOfferingPrice productOfferingPrice) {
        ProductOfferingPriceDeleteNotification notification = new ProductOfferingPriceDeleteNotification();
        
        // Set common notification properties
        notification.setEventId(UUID.randomUUID().toString());
        notification.setEventTime(OffsetDateTime.now(ZoneOffset.UTC));
        notification.setEventType(ProductOfferingPriceDeleteNotification.class.getName());
        notification.setResourcePath("/productCatalogManagement/v4/productOfferingPrice/" + productOfferingPrice.getId());

        // Create event
        ProductOfferingPriceDeleteEvent event = new ProductOfferingPriceDeleteEvent();
        event.setEventId(notification.getEventId());
        event.setEventTime(notification.getEventTime());
        event.setEventType("ProductOfferingPriceDeleteEvent");
        event.setTitle("Product Offering Price Delete Event");
        event.setDescription("A product offering price has been deleted");
        event.setTimeOcurred(notification.getEventTime());

        // Create event payload
        ProductOfferingPriceDeleteEventPayload payload = new ProductOfferingPriceDeleteEventPayload();
        payload.setProductOfferingPrice(productOfferingPrice);
        event.setEvent(payload);

        notification.setEvent(event);
        return notification;
    }

    /**
     * Create a product offering price attribute value change notification
     * @param productOfferingPrice The product offering price with changed attributes
     * @return ProductOfferingPriceAttributeValueChangeNotification
     */
    private ProductOfferingPriceAttributeValueChangeNotification createProductOfferingPriceAttributeValueChangeNotification(ProductOfferingPrice productOfferingPrice) {
        ProductOfferingPriceAttributeValueChangeNotification notification = new ProductOfferingPriceAttributeValueChangeNotification();
        
        // Set common notification properties
        notification.setEventId(UUID.randomUUID().toString());
        notification.setEventTime(OffsetDateTime.now(ZoneOffset.UTC));
        notification.setEventType(ProductOfferingPriceAttributeValueChangeNotification.class.getName());
        notification.setResourcePath("/productCatalogManagement/v4/productOfferingPrice/" + productOfferingPrice.getId());

        // Create event
        ProductOfferingPriceAttributeValueChangeEvent event = new ProductOfferingPriceAttributeValueChangeEvent();
        event.setEventId(notification.getEventId());
        event.setEventTime(notification.getEventTime());
        event.setEventType("ProductOfferingPriceAttributeValueChangeEvent");
        event.setTitle("Product Offering Price Attribute Value Change Event");
        event.setDescription("A product offering price attribute value has been changed");
        event.setTimeOcurred(notification.getEventTime());

        // Create event payload
        ProductOfferingPriceAttributeValueChangeEventPayload payload = new ProductOfferingPriceAttributeValueChangeEventPayload();
        payload.setProductOfferingPrice(productOfferingPrice);
        event.setEvent(payload);

        notification.setEvent(event);
        return notification;
    }

    /**
     * Create a product offering price state change notification
     * @param productOfferingPrice The product offering price with changed state
     * @return ProductOfferingPriceStateChangeNotification
     */
    private ProductOfferingPriceStateChangeNotification createProductOfferingPriceStateChangeNotification(ProductOfferingPrice productOfferingPrice) {
        ProductOfferingPriceStateChangeNotification notification = new ProductOfferingPriceStateChangeNotification();
        
        // Set common notification properties
        notification.setEventId(UUID.randomUUID().toString());
        notification.setEventTime(OffsetDateTime.now(ZoneOffset.UTC));
        notification.setEventType(ProductOfferingPriceStateChangeNotification.class.getName());
        notification.setResourcePath("/productCatalogManagement/v4/productOfferingPrice/" + productOfferingPrice.getId());

        // Create event
        ProductOfferingPriceStateChangeEvent event = new ProductOfferingPriceStateChangeEvent();
        event.setEventId(notification.getEventId());
        event.setEventTime(notification.getEventTime());
        event.setEventType("ProductOfferingPriceStateChangeEvent");
        event.setTitle("Product Offering Price State Change Event");
        event.setDescription("A product offering price state has been changed");
        event.setTimeOcurred(notification.getEventTime());

        // Create event payload
        ProductOfferingPriceStateChangeEventPayload payload = new ProductOfferingPriceStateChangeEventPayload();
        payload.setProductOfferingPrice(productOfferingPrice);
        event.setEvent(payload);

        notification.setEvent(event);
        return notification;
    }
}