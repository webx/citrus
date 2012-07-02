/**
 * 
 */
package com.alibaba.citrus.service.mappingrule;

import static junit.framework.Assert.assertEquals;

import org.junit.Test;

/**
 * @author qianchao
 *
 */
public class RestfulModuleTests extends AbstractMappingRuleServiceTests{

	@Test
	public void testOldRulePrior(){
		mappingRules = (MappingRuleService) factory.getBean("restful1");
		String name = mappingRules.getMappedName("screen", "ccc/hello/say.vm");
		assertEquals("ccc.Default", name);
	}
	
	@Test
	public void testRestFulPrior(){
		mappingRules = (MappingRuleService) factory.getBean("restful2");
		String name = mappingRules.getMappedName("screen", "ccc/hello/say.vm");
		assertEquals("ccc.Hello", name);
	}


}
