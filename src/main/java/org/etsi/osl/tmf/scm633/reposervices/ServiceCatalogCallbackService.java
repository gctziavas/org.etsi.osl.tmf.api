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

import org.etsi.osl.tmf.scm633.model.ServiceCatalogCreateEvent;
import org.etsi.osl.tmf.scm633.model.ServiceCatalogDeleteEvent;
import org.etsi.osl.tmf.scm633.model.ServiceCategoryCreateEvent;
import org.etsi.osl.tmf.scm633.model.ServiceCategoryDeleteEvent;
import org.etsi.osl.tmf.scm633.model.ServiceSpecificationCreateEvent;
import org.etsi.osl.tmf.scm633.model.ServiceSpecificationDeleteEvent;
import org.etsi.osl.tmf.scm633.model.ServiceSpecificationChangeEvent;
import org.etsi.osl.tmf.scm633.model.EventSubscription;
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
    private EventSubscriptionRepoService eventSubscriptionRepoService;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * Send service catalog create event to all registered callback URLs
     * @param serviceCatalogCreateEvent The service catalog create event to send
     */
    public void sendServiceCatalogCreateCallback(ServiceCatalogCreateEvent serviceCatalogCreateEvent) {
        List<EventSubscription> subscriptions = eventSubscriptionRepoService.findAll();
        
        for (EventSubscription subscription : subscriptions) {
            if (shouldNotifySubscription(subscription, "serviceCatalogCreateEvent")) {
                sendServiceCatalogCreateEventToCallback(subscription.getCallback(), serviceCatalogCreateEvent);
            }
        }
    }

    /**
     * Send service catalog delete event to all registered callback URLs
     * @param serviceCatalogDeleteEvent The service catalog delete event to send
     */
    public void sendServiceCatalogDeleteCallback(ServiceCatalogDeleteEvent serviceCatalogDeleteEvent) {
        List<EventSubscription> subscriptions = eventSubscriptionRepoService.findAll();
        
        for (EventSubscription subscription : subscriptions) {
            if (shouldNotifySubscription(subscription, "serviceCatalogDeleteEvent")) {
                sendServiceCatalogDeleteEventToCallback(subscription.getCallback(), serviceCatalogDeleteEvent);
            }
        }
    }

    /**
     * Send service catalog create event to a specific callback URL
     * @param callbackUrl The callback URL to send to
     * @param event The service catalog create event
     */
    private void sendServiceCatalogCreateEventToCallback(String callbackUrl, ServiceCatalogCreateEvent event) {
        try {
            String url = buildCallbackUrl(callbackUrl, "/listener/serviceCatalogCreateEvent");
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<ServiceCatalogCreateEvent> entity = new HttpEntity<>(event, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, String.class);
            
            logger.info("Successfully sent service catalog create event to callback URL: {} - Response: {}", 
                url, response.getStatusCode());
            
        } catch (Exception e) {
            logger.error("Failed to send service catalog create event to callback URL: {}", callbackUrl, e);
        }
    }

    /**
     * Send service catalog delete event to a specific callback URL
     * @param callbackUrl The callback URL to send to
     * @param event The service catalog delete event
     */
    private void sendServiceCatalogDeleteEventToCallback(String callbackUrl, ServiceCatalogDeleteEvent event) {
        try {
            String url = buildCallbackUrl(callbackUrl, "/listener/serviceCatalogDeleteEvent");
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<ServiceCatalogDeleteEvent> entity = new HttpEntity<>(event, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, String.class);
            
            logger.info("Successfully sent service catalog delete event to callback URL: {} - Response: {}", 
                url, response.getStatusCode());
            
        } catch (Exception e) {
            logger.error("Failed to send service catalog delete event to callback URL: {}", callbackUrl, e);
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
     * Send service category create event to all registered callback URLs
     * @param serviceCategoryCreateEvent The service category create event to send
     */
    public void sendServiceCategoryCreateCallback(ServiceCategoryCreateEvent serviceCategoryCreateEvent) {
        List<EventSubscription> subscriptions = eventSubscriptionRepoService.findAll();
        
        for (EventSubscription subscription : subscriptions) {
            if (shouldNotifySubscription(subscription, "serviceCategoryCreateEvent")) {
                sendServiceCategoryCreateEventToCallback(subscription.getCallback(), serviceCategoryCreateEvent);
            }
        }
    }

    /**
     * Send service category delete event to all registered callback URLs
     * @param serviceCategoryDeleteEvent The service category delete event to send
     */
    public void sendServiceCategoryDeleteCallback(ServiceCategoryDeleteEvent serviceCategoryDeleteEvent) {
        List<EventSubscription> subscriptions = eventSubscriptionRepoService.findAll();
        
        for (EventSubscription subscription : subscriptions) {
            if (shouldNotifySubscription(subscription, "serviceCategoryDeleteEvent")) {
                sendServiceCategoryDeleteEventToCallback(subscription.getCallback(), serviceCategoryDeleteEvent);
            }
        }
    }

    /**
     * Send service specification create event to all registered callback URLs
     * @param serviceSpecificationCreateEvent The service specification create event to send
     */
    public void sendServiceSpecificationCreateCallback(ServiceSpecificationCreateEvent serviceSpecificationCreateEvent) {
        List<EventSubscription> subscriptions = eventSubscriptionRepoService.findAll();
        
        for (EventSubscription subscription : subscriptions) {
            if (shouldNotifySubscription(subscription, "serviceSpecificationCreateEvent")) {
                sendServiceSpecificationCreateEventToCallback(subscription.getCallback(), serviceSpecificationCreateEvent);
            }
        }
    }

    /**
     * Send service specification delete event to all registered callback URLs
     * @param serviceSpecificationDeleteEvent The service specification delete event to send
     */
    public void sendServiceSpecificationDeleteCallback(ServiceSpecificationDeleteEvent serviceSpecificationDeleteEvent) {
        List<EventSubscription> subscriptions = eventSubscriptionRepoService.findAll();
        
        for (EventSubscription subscription : subscriptions) {
            if (shouldNotifySubscription(subscription, "serviceSpecificationDeleteEvent")) {
                sendServiceSpecificationDeleteEventToCallback(subscription.getCallback(), serviceSpecificationDeleteEvent);
            }
        }
    }

    /**
     * Send service specification change event to all registered callback URLs
     * @param serviceSpecificationChangeEvent The service specification change event to send
     */
    public void sendServiceSpecificationChangeCallback(ServiceSpecificationChangeEvent serviceSpecificationChangeEvent) {
        List<EventSubscription> subscriptions = eventSubscriptionRepoService.findAll();
        
        for (EventSubscription subscription : subscriptions) {
            if (shouldNotifySubscription(subscription, "serviceSpecificationChangeEvent")) {
                sendServiceSpecificationChangeEventToCallback(subscription.getCallback(), serviceSpecificationChangeEvent);
            }
        }
    }

    /**
     * Send service category create event to a specific callback URL
     * @param callbackUrl The callback URL to send to
     * @param event The service category create event
     */
    private void sendServiceCategoryCreateEventToCallback(String callbackUrl, ServiceCategoryCreateEvent event) {
        try {
            String url = buildCallbackUrl(callbackUrl, "/listener/serviceCategoryCreateEvent");
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<ServiceCategoryCreateEvent> entity = new HttpEntity<>(event, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, String.class);
            
            logger.info("Successfully sent service category create event to callback URL: {} - Response: {}", 
                url, response.getStatusCode());
            
        } catch (Exception e) {
            logger.error("Failed to send service category create event to callback URL: {}", callbackUrl, e);
        }
    }

    /**
     * Send service category delete event to a specific callback URL
     * @param callbackUrl The callback URL to send to
     * @param event The service category delete event
     */
    private void sendServiceCategoryDeleteEventToCallback(String callbackUrl, ServiceCategoryDeleteEvent event) {
        try {
            String url = buildCallbackUrl(callbackUrl, "/listener/serviceCategoryDeleteEvent");
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<ServiceCategoryDeleteEvent> entity = new HttpEntity<>(event, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, String.class);
            
            logger.info("Successfully sent service category delete event to callback URL: {} - Response: {}", 
                url, response.getStatusCode());
            
        } catch (Exception e) {
            logger.error("Failed to send service category delete event to callback URL: {}", callbackUrl, e);
        }
    }

    /**
     * Send service specification create event to a specific callback URL
     * @param callbackUrl The callback URL to send to
     * @param event The service specification create event
     */
    private void sendServiceSpecificationCreateEventToCallback(String callbackUrl, ServiceSpecificationCreateEvent event) {
        try {
            String url = buildCallbackUrl(callbackUrl, "/listener/serviceSpecificationCreateEvent");
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<ServiceSpecificationCreateEvent> entity = new HttpEntity<>(event, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, String.class);
            
            logger.info("Successfully sent service specification create event to callback URL: {} - Response: {}", 
                url, response.getStatusCode());
            
        } catch (Exception e) {
            logger.error("Failed to send service specification create event to callback URL: {}", callbackUrl, e);
        }
    }

    /**
     * Send service specification delete event to a specific callback URL
     * @param callbackUrl The callback URL to send to
     * @param event The service specification delete event
     */
    private void sendServiceSpecificationDeleteEventToCallback(String callbackUrl, ServiceSpecificationDeleteEvent event) {
        try {
            String url = buildCallbackUrl(callbackUrl, "/listener/serviceSpecificationDeleteEvent");
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<ServiceSpecificationDeleteEvent> entity = new HttpEntity<>(event, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, String.class);
            
            logger.info("Successfully sent service specification delete event to callback URL: {} - Response: {}", 
                url, response.getStatusCode());
            
        } catch (Exception e) {
            logger.error("Failed to send service specification delete event to callback URL: {}", callbackUrl, e);
        }
    }

    /**
     * Send service specification change event to a specific callback URL
     * @param callbackUrl The callback URL to send to
     * @param event The service specification change event
     */
    private void sendServiceSpecificationChangeEventToCallback(String callbackUrl, ServiceSpecificationChangeEvent event) {
        try {
            String url = buildCallbackUrl(callbackUrl, "/listener/serviceSpecificationChangeEvent");
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<ServiceSpecificationChangeEvent> entity = new HttpEntity<>(event, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, String.class);
            
            logger.info("Successfully sent service specification change event to callback URL: {} - Response: {}", 
                url, response.getStatusCode());
            
        } catch (Exception e) {
            logger.error("Failed to send service specification change event to callback URL: {}", callbackUrl, e);
        }
    }

    /**
     * Check if a subscription should be notified for a specific event type
     * @param subscription The event subscription
     * @param eventType The event type to check
     * @return true if the subscription should be notified
     */
    private boolean shouldNotifySubscription(EventSubscription subscription, String eventType) {
        // If no query is specified, notify all events
        if (subscription.getQuery() == null || subscription.getQuery().trim().isEmpty()) {
            return true;
        }
        
        // Check if the query contains the event type
        String query = subscription.getQuery().toLowerCase();
        return query.contains("servicecatalog") || 
               query.contains("servicecategory") ||
               query.contains("servicespecification") ||
               query.contains(eventType.toLowerCase()) ||
               query.contains("servicecatalog.create") ||
               query.contains("servicecatalog.delete") ||
               query.contains("servicecategory.create") ||
               query.contains("servicecategory.delete") ||
               query.contains("servicespecification.create") ||
               query.contains("servicespecification.delete") ||
               query.contains("servicespecification.change");
    }
}