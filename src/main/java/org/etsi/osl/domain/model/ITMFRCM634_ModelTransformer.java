package org.etsi.osl.domain.model;

import org.etsi.osl.tmf.rcm634.model.ResourceSpecification;
import org.etsi.osl.tmf.rcm634.model.ResourceSpecificationCreate;
import org.etsi.osl.tmf.rcm634.model.ResourceSpecificationUpdate;

/**
 * @author ctranoris
 * 
 * Transforms the PoJo class to/from the equivalent TMF model 
 */
public interface ITMFRCM634_ModelTransformer {
	
	
	ResourceSpecificationCreate toRSpecCreate_InitRepo();
	
	
	default ResourceSpecificationCreate toRSpecCreate() {
		return null;
	}
	
	default ResourceSpecificationUpdate toRSpecUpdate() {
		return null;
	}
	
	/**
	 * loads the class fields from this model
	 * @param rSpec
	 */
	DomainModelDefinition fromRSpec( ResourceSpecification rSpec ) ;
	
}
