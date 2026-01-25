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
import org.etsi.osl.tmf.pcm620.model.Category;
import org.etsi.osl.tmf.pcm620.model.CategoryCreateEvent;
import org.etsi.osl.tmf.pcm620.model.CategoryCreateEventPayload;
import org.etsi.osl.tmf.pcm620.model.CategoryCreateNotification;
import org.etsi.osl.tmf.pcm620.model.CategoryDeleteEvent;
import org.etsi.osl.tmf.pcm620.model.CategoryDeleteEventPayload;
import org.etsi.osl.tmf.pcm620.model.CategoryDeleteNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CategoryNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(CategoryNotificationService.class);

    @Autowired
    private ProductCatalogApiRouteBuilderEvents eventPublisher;

    @Autowired
    private CategoryCallbackService categoryCallbackService;

    /**
     * Publish a category create notification
     * @param category The created category
     */
    public void publishCategoryCreateNotification(Category category) {
        try {
            CategoryCreateNotification notification = createCategoryCreateNotification(category);
            eventPublisher.publishEvent(notification, category.getUuid());
            
            // Send callbacks to registered subscribers
            categoryCallbackService.sendCategoryCreateCallback(notification.getEvent());
            
            logger.info("Published category create notification for category ID: {}", category.getUuid());
        } catch (Exception e) {
            logger.error("Error publishing category create notification for category ID: {}", category.getUuid(), e);
        }
    }

    /**
     * Publish a category delete notification
     * @param category The deleted category
     */
    public void publishCategoryDeleteNotification(Category category) {
        try {
            CategoryDeleteNotification notification = createCategoryDeleteNotification(category);
            eventPublisher.publishEvent(notification, category.getUuid());
            
            // Send callbacks to registered subscribers
            categoryCallbackService.sendCategoryDeleteCallback(notification.getEvent());
            
            logger.info("Published category delete notification for category ID: {}", category.getUuid());
        } catch (Exception e) {
            logger.error("Error publishing category delete notification for category ID: {}", category.getUuid(), e);
        }
    }

    /**
     * Create a category create notification
     * @param category The created category
     * @return CategoryCreateNotification
     */
    private CategoryCreateNotification createCategoryCreateNotification(Category category) {
        CategoryCreateNotification notification = new CategoryCreateNotification();
        
        // Set common notification properties
        notification.setEventId(UUID.randomUUID().toString());
        notification.setEventTime(OffsetDateTime.now(ZoneOffset.UTC));
        notification.setEventType(CategoryCreateNotification.class.getName());
        notification.setResourcePath("/productCatalogManagement/v4/category/" + category.getUuid());

        // Create event
        CategoryCreateEvent event = new CategoryCreateEvent();
        event.setEventId(notification.getEventId());
        event.setEventTime(notification.getEventTime());
        event.setEventType("CategoryCreateEvent");
        event.setTitle("Category Create Event");
        event.setDescription("A category has been created");
        event.setTimeOcurred(notification.getEventTime());

        // Create event payload
        CategoryCreateEventPayload payload = new CategoryCreateEventPayload();
        payload.setCategory(category);
        event.setEvent(payload);

        notification.setEvent(event);
        return notification;
    }

    /**
     * Create a category delete notification
     * @param category The deleted category
     * @return CategoryDeleteNotification
     */
    private CategoryDeleteNotification createCategoryDeleteNotification(Category category) {
        CategoryDeleteNotification notification = new CategoryDeleteNotification();
        
        // Set common notification properties
        notification.setEventId(UUID.randomUUID().toString());
        notification.setEventTime(OffsetDateTime.now(ZoneOffset.UTC));
        notification.setEventType(CategoryDeleteNotification.class.getName());
        notification.setResourcePath("/productCatalogManagement/v4/category/" + category.getUuid());

        // Create event
        CategoryDeleteEvent event = new CategoryDeleteEvent();
        event.setEventId(notification.getEventId());
        event.setEventTime(notification.getEventTime());
        event.setEventType("CategoryDeleteEvent");
        event.setTitle("Category Delete Event");
        event.setDescription("A category has been deleted");
        event.setTimeOcurred(notification.getEventTime());

        // Create event payload
        CategoryDeleteEventPayload payload = new CategoryDeleteEventPayload();
        payload.setCategory(category);
        event.setEvent(payload);

        notification.setEvent(event);
        return notification;
    }
}