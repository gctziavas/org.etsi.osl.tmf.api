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
import java.util.Optional;
import java.util.UUID;

import org.etsi.osl.tmf.scm633.model.EventSubscription;
import org.etsi.osl.tmf.scm633.model.EventSubscriptionInput;
import org.etsi.osl.tmf.scm633.repo.EventSubscriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.Valid;

@Service("scm633EventSubscriptionRepoService")
@Transactional
public class EventSubscriptionRepoService {

    @Autowired
    @Qualifier("scm633EventSubscriptionRepository")
    EventSubscriptionRepository eventSubscriptionRepo;

    public EventSubscription addEventSubscription(@Valid EventSubscriptionInput eventSubscriptionInput) {
        if (eventSubscriptionInput.getCallback() == null || eventSubscriptionInput.getCallback().trim().isEmpty()) {
            throw new IllegalArgumentException("Callback URL is required and cannot be empty");
        }
        
        EventSubscription eventSubscription = new EventSubscription();
        eventSubscription.setId(UUID.randomUUID().toString());
        eventSubscription.setCallback(eventSubscriptionInput.getCallback());
        eventSubscription.setQuery(eventSubscriptionInput.getQuery());
        
        return this.eventSubscriptionRepo.save(eventSubscription);
    }

    public EventSubscription findById(String id) {
        Optional<EventSubscription> optionalEventSubscription = this.eventSubscriptionRepo.findById(id);
        return optionalEventSubscription.orElse(null);
    }

    public void deleteById(String id) {
        Optional<EventSubscription> optionalEventSubscription = this.eventSubscriptionRepo.findById(id);
        if (optionalEventSubscription.isPresent()) {
            this.eventSubscriptionRepo.delete(optionalEventSubscription.get());
        }
    }

    public List<EventSubscription> findAll() {
        return (List<EventSubscription>) this.eventSubscriptionRepo.findAll();
    }
}