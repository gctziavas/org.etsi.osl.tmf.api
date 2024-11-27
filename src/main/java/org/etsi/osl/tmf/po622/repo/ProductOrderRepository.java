/*-
 * ========================LICENSE_START=================================
 * org.etsi.osl.tmf.api
 * %%
 * Copyright (C) 2019 openslice.io
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
package org.etsi.osl.tmf.po622.repo;

import java.util.List;
import java.util.Optional;
import org.etsi.osl.tmf.common.model.UserPartRoleType;
import org.etsi.osl.tmf.po622.model.ProductOrder;
import org.etsi.osl.tmf.po622.model.ProductOrderStateType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ProductOrderRepository extends CrudRepository<ProductOrder, Long>, PagingAndSortingRepository<ProductOrder, Long> {


	Optional<ProductOrder> findByUuid(String id);
	
	Iterable<ProductOrder> findByState( ProductOrderStateType state);
	@Query("SELECT por FROM ProductOrder por JOIN FETCH por.relatedParty rp WHERE rp.name = ?1 AND  rp.role = ?2 ORDER BY por.orderDate DESC")	
	Iterable<ProductOrder> findByRolenameAndRoleType(String rolename, UserPartRoleType requester);
	@Query("SELECT por FROM ProductOrder por JOIN FETCH por.relatedParty rp WHERE rp.name = ?1")	
	Iterable<ProductOrder> findByRolename(String rolename);
	
	List<ProductOrder> findByOrderByOrderDateDesc();
	
	@Query("SELECT por FROM ProductOrder por JOIN FETCH por.relatedParty rp ORDER BY por.orderDate DESC")	
	List<ProductOrder> findAllOptimized();



	@Query("SELECT por FROM ProductOrder por JOIN FETCH por.note an "
			+ "WHERE por.uuid = ?1 "
			+ "ORDER BY an.date ASC")	
	Optional<ProductOrder> findNotesOfProdOrder(String id);
}
