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

import java.util.List;
import org.etsi.osl.tmf.scm633.model.EventSubscription;
import org.etsi.osl.tmf.scm633.model.ServiceCatalogCreateNotification;
import org.etsi.osl.tmf.scm633.model.ServiceCatalogDeleteNotification;
import org.etsi.osl.tmf.scm633.model.ServiceCategoryCreateNotification;
import org.etsi.osl.tmf.scm633.model.ServiceCategoryDeleteNotification;
import org.etsi.osl.tmf.scm633.model.ServiceSpecificationChangeNotification;
import org.etsi.osl.tmf.scm633.model.ServiceSpecificationCreateNotification;
import org.etsi.osl.tmf.scm633.model.ServiceSpecificationDeleteNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ServiceCatalogCallbackService {

    private static final Logger logger = LoggerFactory.getLogger(ServiceCatalogCallbackService.class);

    @Autowired
    @Qualifier("scm633EventSubscriptionRepoService")
    private EventSubscriptionRepoService notificationSubscriptionRepoService;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * Send service catalog create notification to all registered callback URLs
     * @param serviceCatalogCreateNotification The service catalog create notification to send
     */
    public void sendServiceCatalogCreateCallback(ServiceCatalogCreateNotification serviceCatalogCreateNotification) {
        List<EventSubscription> subscriptions = notificationSubscriptionRepoService.findAll();
        
        for (EventSubscription subscription : subscriptions) {
            if (shouldNotifySubscription(subscription, "serviceCatalogCreateNotification")) {
                sendServiceCatalogCreateNotificationToCallback(subscription.getCallback(), serviceCatalogCreateNotification);
            }
        }
    }

    /**
     * Send service catalog delete notification to all registered callback URLs
     * @param serviceCatalogDeleteEvent The service catalog delete notification to send
     */
    public void sendServiceCatalogDeleteCallback(ServiceCatalogDeleteNotification serviceCatalogDeleteNotification) {
        List<EventSubscription> subscriptions = notificationSubscriptionRepoService.findAll();
        
        for (EventSubscription subscription : subscriptions) {
            if (shouldNotifySubscription(subscription, "serviceCatalogDeleteNotification")) {
                sendServiceCatalogDeleteNotificationToCallback(subscription.getCallback(), serviceCatalogDeleteNotification);
            }
        }
    }

    /**
     * Send service catalog create notification to a specific callback URL
     * @param callbackUrl The callback URL to send to
     * @param notification The service catalog create notification
     */
    private void sendServiceCatalogCreateNotificationToCallback(String callbackUrl, ServiceCatalogCreateNotification notification) {
        try {
            String url = buildCallbackUrl(callbackUrl, "/listener/serviceCatalogCreateNotification");
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<ServiceCatalogCreateNotification> entity = new HttpEntity<>(notification, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, String.class);
            
            logger.info("Successfully sent service catalog create notification to callback URL: {} - Response: {}", 
                url, response.getStatusCode());
            
        } catch (Exception e) {
            logger.error("Failed to send service catalog create notification to callback URL: {}", callbackUrl, e);
        }
    }

    /**
     * Send service catalog delete notification to a specific callback URL
     * @param callbackUrl The callback URL to send to
     * @param notification The service catalog delete notification
     */
    private void sendServiceCatalogDeleteNotificationToCallback(String callbackUrl, ServiceCatalogDeleteNotification notification) {
        try {
            String url = buildCallbackUrl(callbackUrl, "/listener/serviceCatalogDeleteNotification");
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<ServiceCatalogDeleteNotification> entity = new HttpEntity<>(notification, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, String.class);
            
            logger.info("Successfully sent service catalog delete notification to callback URL: {} - Response: {}", 
                url, response.getStatusCode());
            
        } catch (Exception e) {
            logger.error("Failed to send service catalog delete notification to callback URL: {}", callbackUrl, e);
        }
    }

    /**
     * Build the full callback URL with the listener endpoint
     * @param baseUrl The base callback URL
     * @param listenerPath The listener path to append
     * @return The complete callback URL
     */
    private String buildCallbackUrl(String baseUrl, String listenerPath) {
        if (baseUrl.endsWith("/")) {
            return baseUrl.substring(0, baseUrl.length() - 1) + listenerPath;
        } else {
            return baseUrl + listenerPath;
        }
    }

    /**
     * Send service category create notification to all registered callback URLs
     * @param serviceCategoryCreateNotification The service category create notification to send
     */
    public void sendServiceCategoryCreateCallback(ServiceCategoryCreateNotification serviceCategoryCreateNotification) {
        List<EventSubscription> subscriptions = notificationSubscriptionRepoService.findAll();
        
        for (EventSubscription subscription : subscriptions) {
            if (shouldNotifySubscription(subscription, "serviceCategoryCreateNotification")) {
                sendServiceCategoryCreateNotificationToCallback(subscription.getCallback(), serviceCategoryCreateNotification);
            }
        }
    }

    /**
     * Send service category delete notification to all registered callback URLs
     * @param serviceCategoryDeleteNotification The service category delete notification to send
     */
    public void sendServiceCategoryDeleteCallback(ServiceCategoryDeleteNotification serviceCategoryDeleteNotification) {
        List<EventSubscription> subscriptions = notificationSubscriptionRepoService.findAll();
        
        for (EventSubscription subscription : subscriptions) {
            if (shouldNotifySubscription(subscription, "serviceCategoryDeleteNotification")) {
                sendServiceCategoryDeleteNotificationToCallback(subscription.getCallback(), serviceCategoryDeleteNotification);
            }
        }
    }

    /**
     * Send service specification create notification to all registered callback URLs
     * @param serviceSpecificationCreateNotification The service specification create notification to send
     */
    public void sendServiceSpecificationCreateCallback(ServiceSpecificationCreateNotification serviceSpecificationCreateNotification) {
        List<EventSubscription> subscriptions = notificationSubscriptionRepoService.findAll();
        
        for (EventSubscription subscription : subscriptions) {
            if (shouldNotifySubscription(subscription, "serviceSpecificationCreateNotification")) {
                sendServiceSpecificationCreateNotificationToCallback(subscription.getCallback(), serviceSpecificationCreateNotification);
            }
        }
    }

    /**
     * Send service specification delete notification to all registered callback URLs
     * @param serviceSpecificationDeleteNotification The service specification delete notification to send
     */
    public void sendServiceSpecificationDeleteCallback(ServiceSpecificationDeleteNotification serviceSpecificationDeleteNotification) {
        List<EventSubscription> subscriptions = notificationSubscriptionRepoService.findAll();
        
        for (EventSubscription subscription : subscriptions) {
            if (shouldNotifySubscription(subscription, "serviceSpecificationDeleteNotification")) {
                sendServiceSpecificationDeleteNotificationToCallback(subscription.getCallback(), serviceSpecificationDeleteNotification);
            }
        }
    }

    /**
     * Send service specification change notification to all registered callback URLs
     * @param serviceSpecificationChangeNotification The service specification change notification to send
     */
    public void sendServiceSpecificationChangeCallback(ServiceSpecificationChangeNotification serviceSpecificationChangeNotification) {
        List<EventSubscription> subscriptions = notificationSubscriptionRepoService.findAll();
        
        for (EventSubscription subscription : subscriptions) {
            if (shouldNotifySubscription(subscription, "serviceSpecificationChangeNotification")) {
                sendServiceSpecificationChangeNotificationToCallback(subscription.getCallback(), serviceSpecificationChangeNotification);
            }
        }
    }

    /**
     * Send service category create notification to a specific callback URL
     * @param callbackUrl The callback URL to send to
     * @param notification The service category create notification
     */
    private void sendServiceCategoryCreateNotificationToCallback(String callbackUrl, ServiceCategoryCreateNotification notification) {
        try {
            String url = buildCallbackUrl(callbackUrl, "/listener/serviceCategoryCreateNotification");
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<ServiceCategoryCreateNotification> entity = new HttpEntity<>(notification, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, String.class);
            
            logger.info("Successfully sent service category create notification to callback URL: {} - Response: {}", 
                url, response.getStatusCode());
            
        } catch (Exception e) {
            logger.error("Failed to send service category create notification to callback URL: {}", callbackUrl, e);
        }
    }

    /**
     * Send service category delete notification to a specific callback URL
     * @param callbackUrl The callback URL to send to
     * @param notification The service category delete notification
     */
    private void sendServiceCategoryDeleteNotificationToCallback(String callbackUrl, ServiceCategoryDeleteNotification notification) {
        try {
            String url = buildCallbackUrl(callbackUrl, "/listener/serviceCategoryDeleteNotification");
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<ServiceCategoryDeleteNotification> entity = new HttpEntity<>(notification, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, String.class);
            
            logger.info("Successfully sent service category delete notification to callback URL: {} - Response: {}", 
                url, response.getStatusCode());
            
        } catch (Exception e) {
            logger.error("Failed to send service category delete notification to callback URL: {}", callbackUrl, e);
        }
    }

    /**
     * Send service specification create notification to a specific callback URL
     * @param callbackUrl The callback URL to send to
     * @param notification The service specification create notification
     */
    private void sendServiceSpecificationCreateNotificationToCallback(String callbackUrl, ServiceSpecificationCreateNotification notification) {
        try {
            String url = buildCallbackUrl(callbackUrl, "/listener/serviceSpecificationCreateNotification");
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<ServiceSpecificationCreateNotification> entity = new HttpEntity<>(notification, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, String.class);
            
            logger.info("Successfully sent service specification create notification to callback URL: {} - Response: {}", 
                url, response.getStatusCode());
            
        } catch (Exception e) {
            logger.error("Failed to send service specification create notification to callback URL: {}", callbackUrl, e);
        }
    }

    /**
     * Send service specification delete notification to a specific callback URL
     * @param callbackUrl The callback URL to send to
     * @param notification The service specification delete notification
     */
    private void sendServiceSpecificationDeleteNotificationToCallback(String callbackUrl, ServiceSpecificationDeleteNotification notification) {
        try {
            String url = buildCallbackUrl(callbackUrl, "/listener/serviceSpecificationDeleteNotification");
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<ServiceSpecificationDeleteNotification> entity = new HttpEntity<>(notification, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, String.class);
            
            logger.info("Successfully sent service specification delete notification to callback URL: {} - Response: {}", 
                url, response.getStatusCode());
            
        } catch (Exception e) {
            logger.error("Failed to send service specification delete notification to callback URL: {}", callbackUrl, e);
        }
    }

    /**
     * Send service specification change notification to a specific callback URL
     * @param callbackUrl The callback URL to send to
     * @param notification The service specification change notification
     */
    private void sendServiceSpecificationChangeNotificationToCallback(String callbackUrl, ServiceSpecificationChangeNotification notification) {
        try {
            String url = buildCallbackUrl(callbackUrl, "/listener/serviceSpecificationChangeNotification");
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<ServiceSpecificationChangeNotification> entity = new HttpEntity<>(notification, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, String.class);
            
            logger.info("Successfully sent service specification change notification to callback URL: {} - Response: {}", 
                url, response.getStatusCode());
            
        } catch (Exception e) {
            logger.error("Failed to send service specification change notification to callback URL: {}", callbackUrl, e);
        }
    }

    /**
     * Check if a subscription should be notified for a specific notification type
     * @param subscription The notification subscription
     * @param notificationType The notification type to check
     * @return true if the subscription should be notified
     */
    private boolean shouldNotifySubscription(EventSubscription subscription, String notificationType) {
        // If no query is specified, notify all notifications
        if (subscription.getQuery() == null || subscription.getQuery().trim().isEmpty()) {
            return true;
        }
        
        // Check if the query contains the notification type
        String query = subscription.getQuery().toLowerCase();
        return query.contains("servicecatalog") || 
               query.contains("servicecategory") ||
               query.contains("servicespecification") ||
               query.contains(notificationType.toLowerCase()) ||
               query.contains("servicecatalog.create") ||
               query.contains("servicecatalog.delete") ||
               query.contains("servicecategory.create") ||
               query.contains("servicecategory.delete") ||
               query.contains("servicespecification.create") ||
               query.contains("servicespecification.delete") ||
               query.contains("servicespecification.change");
    }
}