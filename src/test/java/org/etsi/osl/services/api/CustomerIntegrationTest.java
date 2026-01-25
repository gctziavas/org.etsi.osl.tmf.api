/*-
 * ========================LICENSE_START=================================
 * org.etsi.osl.tmf.api
 * %%
 * Copyright (C) 2019 - 2020 openslice.io
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
package org.etsi.osl.services.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etsi.osl.tmf.JsonUtils;
import org.etsi.osl.tmf.cm629.model.Customer;
import org.etsi.osl.tmf.cm629.model.CustomerCreate;
import org.etsi.osl.tmf.cm629.model.CustomerUpdate;
import org.etsi.osl.tmf.cm629.service.CustomerRepoService;
import org.etsi.osl.tmf.pm632.model.ContactMedium;
import org.etsi.osl.tmf.pm632.model.MediumCharacteristic;
import org.etsi.osl.tmf.prm669.model.RelatedParty;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

public class CustomerIntegrationTest extends BaseIT {


	private static final transient Log logger = LogFactory.getLog( CustomerIntegrationTest.class.getName());
	

    @Autowired
    private MockMvc mvc;
    

	@Autowired
	CustomerRepoService customerRepoService;
	

//    @Autowired
//    private WebApplicationContext context;
//    
// 
//    @BeforeEach
//    public void setup() {
//        mvc = MockMvcBuilders
//          .webAppContextSetup(context).dispatchOptions(true)
//          .apply( SecurityMockMvcConfigurers.springSecurity())
//          .build();
//    }
    
    
	@WithMockUser(username="osadmin", roles = {"ADMIN","USER"})
	@Test
	public void addCustomer() throws Exception {
		
		CustomerCreate cc = new CustomerCreate();
		cc.setName("A Customer");
		RelatedParty engagedParty = new RelatedParty();
		engagedParty.name("An Organization");
		engagedParty.setUuid(UUID.randomUUID().toString());
		cc.setEngagedParty(engagedParty );
		
		List<ContactMedium> contactMediums = new ArrayList<>();
		ContactMedium cm = new ContactMedium();
		contactMediums.add(cm);
		cm.setMediumType("email");
		cm.setPreferred(true);
		MediumCharacteristic medChar = new MediumCharacteristic();
		medChar.setEmailAddress( "test@openslice.io" );
		cm.setCharacteristic(medChar);
		cc.setContactMedium(contactMediums );
		
		
				
		String response = mvc.perform(MockMvcRequestBuilders.post("/customerManagement/v4/customer")
	            .with( SecurityMockMvcRequestPostProcessors.csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content( JsonUtils.toJson( cc ) ))
			    .andExpect(status().isOk())
			    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			    .andExpect(jsonPath("name", is("A Customer")))								 
	    	    .andExpect(status().isOk())
	    	    .andReturn().getResponse().getContentAsString();
		

		assertThat( customerRepoService.findAll().size() ).isEqualTo( 1 );
		
		Customer respc = JsonUtils.toJsonObj( response, Customer.class);
		assertThat( respc.getContactMedium().stream().findFirst().get().getMediumType() ).isEqualTo("email");
		assertThat( respc.getContactMedium().stream().findFirst().get().getCharacteristic().getEmailAddress()  ).isEqualTo("test@openslice.io");
		
		
		CustomerUpdate cu = new CustomerUpdate();
		cu.setEngagedParty( respc.getEngagedParty() );
		cm = new ContactMedium();
		cm.setMediumType("phone");
		medChar = new MediumCharacteristic();
		medChar.setPhoneNumber("555000");
		cm.setCharacteristic(medChar);
		contactMediums = new ArrayList<>();
		for (ContactMedium contactMedium : respc.getContactMedium()) {
			contactMediums.add(contactMedium);			
		}
		contactMediums.add(cm);
		cu.setContactMedium(contactMediums);
		
		String responseUpd = mvc.perform(MockMvcRequestBuilders.patch("/customerManagement/v4/customer/" + respc.getId() )
	            .with( SecurityMockMvcRequestPostProcessors.csrf())
	            .contentType(MediaType.APPLICATION_JSON)
				.content( JsonUtils.toJson( cu ) ))
			    .andExpect(status().isOk())
			    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			    .andExpect(jsonPath("name", is("A Customer")))								 
	    	    .andExpect(status().isOk())
	    	    .andReturn().getResponse().getContentAsString();
		

		assertThat( customerRepoService.findAll().size() ).isEqualTo( 1 );
		
		respc = JsonUtils.toJsonObj( responseUpd, Customer.class);

		assertThat( respc.getContactMedium().size()  ).isEqualTo(2);

		String responseGet = mvc.perform(MockMvcRequestBuilders.get("/customerManagement/v4/customer")
	            .with( SecurityMockMvcRequestPostProcessors.csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content( JsonUtils.toJson( cc ) ))
			    .andExpect(status().isOk())
			    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
	    	    .andReturn().getResponse().getContentAsString();
		

		List<Customer> respcs = JsonUtils.toJsonObj( responseGet, ArrayList.class);

		assertThat( respcs.size()  ).isEqualTo( 1 );
		
		
		responseGet = mvc.perform(MockMvcRequestBuilders.get("/customerManagement/v4/customer/" + respc.getId() )
	            .with( SecurityMockMvcRequestPostProcessors.csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content( JsonUtils.toJson( cc ) ))
			    .andExpect(status().isOk())
			    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			    .andExpect(jsonPath("name", is("A Customer")))								 
	    	    .andReturn().getResponse().getContentAsString();


		respc = JsonUtils.toJsonObj( responseGet, Customer.class);
		
		

		String responseDel = mvc.perform(MockMvcRequestBuilders
				.delete("/customerManagement/v4/customer/" + respc.getId() )
	            .with( SecurityMockMvcRequestPostProcessors.csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content( JsonUtils.toJson( cu ) ))
	    	    .andExpect(status().isOk())
	    	    .andReturn().getResponse().getContentAsString();
		

		assertThat( customerRepoService.findAll().size() ).isEqualTo( 0 );
		
	}
	
}
