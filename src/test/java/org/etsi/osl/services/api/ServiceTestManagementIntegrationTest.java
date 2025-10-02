
package org.etsi.osl.services.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etsi.osl.tmf.JsonUtils;
import org.etsi.osl.tmf.sim638.service.ServiceRepoService;
import org.etsi.osl.tmf.stm653.model.ServiceTestCreate;
import org.etsi.osl.tmf.stm653.model.ServiceTestSpecificationCreate;
import org.etsi.osl.tmf.stm653.reposervices.ServiceTestRepoService;
import org.etsi.osl.tmf.stm653.reposervices.ServiceTestSpecificationRepoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

public class ServiceTestManagementIntegrationTest extends BaseIT {

	private static final transient Log logger = LogFactory.getLog(ServiceTestManagementIntegrationTest.class.getName());

	@Autowired
	private MockMvc mvc;

	@Autowired
	ServiceTestSpecificationRepoService aServiceTestSpecRpoService;	

	@Autowired
	ServiceTestRepoService aServiceTestRpoService;

	@Autowired
	ServiceRepoService serviceRepoService;

	@Autowired
	private WebApplicationContext context;

	@BeforeEach
	public void setup() {
		mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
	}

	@WithMockUser(username = "osadmin", roles = { "ADMIN","USER" })
	@Test
	public void testServiceTestSpecCreateAndUpdate() throws UnsupportedEncodingException, IOException, Exception {

		/**
		 * first add 2 specs
		 */
//		/testServiceTestSpec
		
		

		File sspec = new File( "src/test/resources/testServiceTestSpec.json" );
		InputStream in = new FileInputStream( sspec );
		String sspectext = IOUtils.toString(in, "UTF-8");
		ServiceTestSpecificationCreate spec = JsonUtils.toJsonObj( sspectext,  ServiceTestSpecificationCreate.class);  
		
		
		String response = mvc.perform(MockMvcRequestBuilders.post("/serviceTestManagement/v4/serviceTestSpecification")
		            .with( SecurityMockMvcRequestPostProcessors.csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content( JsonUtils.toJson( spec ) ))
			    .andExpect(status().isOk())
			    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			    .andExpect(jsonPath("name", is("A test name")))								 
	    	    .andExpect(status().isOk())
	    	    .andReturn().getResponse().getContentAsString();
		

		assertThat( aServiceTestSpecRpoService.findAll().size() ).isEqualTo( 1 );
		
		
		sspec = new File( "src/test/resources/testServiceTest.json" );
		in = new FileInputStream( sspec );
		sspectext = IOUtils.toString(in, "UTF-8");
		ServiceTestCreate stest = JsonUtils.toJsonObj( sspectext,  ServiceTestCreate.class);  
		
		
		response = mvc.perform(MockMvcRequestBuilders.post("/serviceTestManagement/v4/serviceTest")
		            .with( SecurityMockMvcRequestPostProcessors.csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content( JsonUtils.toJson( stest ) ))
			    .andExpect(status().isOk())
			    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			    .andExpect(jsonPath("name", is("A test name")))								 
	    	    .andExpect(status().isOk())
	    	    .andReturn().getResponse().getContentAsString();
		

		assertThat( aServiceTestRpoService.findAll().size() ).isEqualTo( 1 );
		

	}
	

}
