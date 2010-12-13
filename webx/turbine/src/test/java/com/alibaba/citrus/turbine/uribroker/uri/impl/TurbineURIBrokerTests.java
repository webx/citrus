/*
 * Copyright 2010 Alibaba Group Holding Limited.
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.alibaba.citrus.turbine.uribroker.uri.impl;

import static com.alibaba.citrus.test.TestEnvStatic.*;
import static org.easymock.EasyMock.*;
import static org.easymock.classextension.EasyMock.*;
import static org.junit.Assert.*;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.FileSystemResource;

import com.alibaba.citrus.service.mappingrule.MappingRuleService;
import com.alibaba.citrus.service.uribroker.URIBrokerService;
import com.alibaba.citrus.service.uribroker.uri.URIBroker;
import com.alibaba.citrus.springext.support.context.XmlApplicationContext;
import com.alibaba.citrus.turbine.uribroker.uri.TurbineURIBroker;

public class TurbineURIBrokerTests {
    private TurbineURIBroker broker;
    private MappingRuleService mappingRuleService;
    private MappingRuleService mappingRuleService2;

    @Before
    public void init() {
        broker = new TurbineURIBroker();

        mappingRuleService = createMock(MappingRuleService.class);
        expect(mappingRuleService.getMappedName("mymapper", "myTarget.vm")).andReturn("myTarget.htm").anyTimes();
        expect(mappingRuleService.getMappedName("mymapper", "my/pageXyz.vm")).andReturn("my/pageXyz.htm").anyTimes();
        replay(mappingRuleService);

        mappingRuleService2 = createMock(MappingRuleService.class);
        expect(mappingRuleService2.getMappedName("mymapper2", "myTarget.vm")).andReturn("myTarget.htm").anyTimes();
        expect(mappingRuleService2.getMappedName("mymapper2", "myTarget2.vm")).andReturn("myTarget2.htm").anyTimes();
        replay(mappingRuleService2);
    }

    private TurbineURIBroker createParentBroker() {
        TurbineURIBroker parent = new TurbineURIBroker();

        parent.setMappingRuleService(mappingRuleService);
        parent.setActionParam("myaction");
        parent.setConvertTargetCase(true);
        parent.setTargetMappingRule("mymapper");
        parent.setAction("myAction");
        parent.setTarget("myTarget.vm");
        parent.setComponentPath("myComponent");

        return parent;
    }

    private TurbineURIBroker createSubBroker(URIBroker parent, boolean overrideActionAndTarget) {
        TurbineURIBroker broker = new TurbineURIBroker();

        broker.setParent(parent);
        broker.setMappingRuleService(mappingRuleService2);
        broker.setActionParam("myaction2");
        broker.setConvertTargetCase(false);
        broker.setTargetMappingRule("mymapper2");
        broker.setComponentPath("myComponent2");

        if (overrideActionAndTarget) {
            broker.setAction("myAction2");
            broker.setTarget("myTarget2.vm");
        }

        return broker;
    }

    @Test
    public void getMappingRuleService() {
        // init value
        assertNull(broker.getMappingRuleService());

        // set value
        broker.setMappingRuleService(mappingRuleService);
        assertSame(mappingRuleService, broker.getMappingRuleService());
    }

    @Test
    public void getComponentPath() {
        // init value
        assertNull(broker.getComponentPath());

        // set empty
        broker.setComponentPath(null);
        assertEquals("", broker.getComponentPath());
        assertEquals("", broker.getPathInfo());
        assertEquals("http:///", broker.toString());

        broker.setComponentPath(" ");
        assertEquals("", broker.getComponentPath());
        assertEquals("", broker.getPathInfo());
        assertEquals("http:///", broker.toString());

        // set value
        broker.setComponentPath(" mycomp ");
        assertEquals("/mycomp", broker.getComponentPath());
        assertEquals("/mycomp", broker.getPathInfo());
        assertEquals("http:///mycomp", broker.toString());
    }

    @Test
    public void getTarget() {
        // init value
        assertNull(broker.getTarget());

        // set empty
        broker.setTarget(null);
        assertEquals(null, broker.getTarget());

        broker.setTarget(" ");
        assertEquals(null, broker.getTarget());

        // set value
        broker.setTarget("test.vm");
        assertEquals("test.vm", broker.getTarget());
    }

    @Test
    public void getTargetMappingRule() {
        // default value
        assertEquals("extension.output", broker.getTargetMappingRule());

        // set empty
        broker.setTargetMappingRule(null);
        assertEquals("extension.output", broker.getTargetMappingRule());

        broker.setTargetMappingRule(" ");
        assertEquals("extension.output", broker.getTargetMappingRule());

        // set value
        broker.setTargetMappingRule(" test ");
        assertEquals("test", broker.getTargetMappingRule());
    }

    @Test
    public void isConvertTargetCase() {
        // default value
        assertTrue(broker.isConvertTargetCase());

        // set value
        broker.setConvertTargetCase(false);
        assertFalse(broker.isConvertTargetCase());

        broker.setConvertTargetCase(true);
        assertTrue(broker.isConvertTargetCase());
    }

    @Test
    public void setTarget_withMappingRuleService_convertCase() {
        broker.setTargetMappingRule("mymapper");
        broker.setMappingRuleService(mappingRuleService);
        broker.setTarget(" my/pageXyz.vm ");

        assertEquals("my/pageXyz.vm", broker.getTarget());
        assertEquals("/my/page_xyz.htm", broker.getPathInfo());
        assertEquals("http:///my/page_xyz.htm", broker.toString());
    }

    @Test
    public void setTarget_withMappingRuleService_noConvertCase() {
        broker.setTargetMappingRule("mymapper");
        broker.setMappingRuleService(mappingRuleService);
        broker.setConvertTargetCase(false);
        broker.setTarget(" my/pageXyz.vm ");

        assertEquals("my/pageXyz.vm", broker.getTarget());
        assertEquals("/my/pageXyz.htm", broker.getPathInfo());
        assertEquals("http:///my/pageXyz.htm", broker.toString());
    }

    @Test
    public void getActionParam() {
        // default value
        broker = new TurbineURIBroker().setAction("myAction");
        assertEquals("action", broker.getActionParam());
        assertEquals("http:///?action=myAction", broker.toString());

        // set empty
        broker = new TurbineURIBroker().setActionParam(null).setAction("myAction");
        assertEquals("action", broker.getActionParam());
        assertEquals("http:///?action=myAction", broker.toString());

        broker = new TurbineURIBroker().setActionParam("  ").setAction("myAction");
        assertEquals("action", broker.getActionParam());
        assertEquals("http:///?action=myAction", broker.toString());

        // set value
        broker = new TurbineURIBroker().setActionParam(" myaction ").setAction("myAction");
        assertEquals("myaction", broker.getActionParam());
        assertEquals("http:///?myaction=myAction", broker.toString());
    }

    @Test
    public void getAction() {
        // default value
        assertEquals(null, broker.getAction());

        // set empty
        broker.setAction(null);
        assertEquals("", broker.getAction());

        broker.setAction("  ");
        assertEquals("", broker.getAction());

        // set value
        broker.setAction(" myaction ");
        assertEquals("myaction", broker.getAction());

        // set twice
        broker = broker.setAction("myAction2");
        assertEquals("http:///?action=myAction2", broker.toString());
    }

    @Test
    public void init_withParent() {
        TurbineURIBroker parent = createParentBroker();

        // empty broker
        broker.setParent(parent);
        broker.init();

        assertSame(mappingRuleService, broker.getMappingRuleService());
        assertEquals("myaction", broker.getActionParam());
        assertEquals(true, broker.isConvertTargetCase());
        assertEquals("mymapper", broker.getTargetMappingRule());
        assertEquals("myAction", broker.getAction());
        assertEquals("myTarget.vm", broker.getTarget());
        assertEquals("/myComponent", broker.getComponentPath());

        assertEquals("/myComponent/my_target.htm", broker.getPathInfo());
        assertEquals("http:///myComponent/my_target.htm?myaction=myAction", broker.toString());

        // override
        broker = createSubBroker(parent, true);
        broker.init();

        assertSame(mappingRuleService2, broker.getMappingRuleService());
        assertEquals("myaction2", broker.getActionParam());
        assertEquals(false, broker.isConvertTargetCase());
        assertEquals("mymapper2", broker.getTargetMappingRule());
        assertEquals("myAction2", broker.getAction());
        assertEquals("myTarget2.vm", broker.getTarget());
        assertEquals("/myComponent2", broker.getComponentPath());

        assertEquals("http:///myComponent2/myTarget2.htm?myaction2=myAction2", broker.toString());

        broker.setComponentPath("");
        assertEquals("/myTarget2.htm", broker.getPathInfo());
        assertEquals("http:///myTarget2.htm?myaction2=myAction2", broker.toString());

        // inherit action and target
        broker = createSubBroker(parent, false);
        broker.init();

        assertSame(mappingRuleService2, broker.getMappingRuleService());
        assertEquals("myaction2", broker.getActionParam());
        assertEquals(false, broker.isConvertTargetCase());
        assertEquals("mymapper2", broker.getTargetMappingRule());
        assertEquals("myAction", broker.getAction());
        assertEquals("myTarget.vm", broker.getTarget());
        assertEquals("/myComponent2", broker.getComponentPath());

        assertEquals("/myComponent2/myTarget.htm", broker.getPathInfo());
        assertEquals("http:///myComponent2/myTarget.htm?myaction2=myAction", broker.toString());
    }

    @Test
    public void reset_withParent() {
        TurbineURIBroker parent = createParentBroker();
        broker = createSubBroker(parent, true);
        broker.init();
        broker.reset();

        assertSame(mappingRuleService, broker.getMappingRuleService());
        assertEquals("myaction", broker.getActionParam());
        assertEquals(true, broker.isConvertTargetCase());
        assertEquals("mymapper", broker.getTargetMappingRule());
        assertEquals("myAction", broker.getAction());
        assertEquals("myTarget.vm", broker.getTarget());
        assertEquals("/myComponent", broker.getComponentPath());

        assertEquals("/myComponent/my_target.htm", broker.getPathInfo());
        assertEquals("http:///myComponent/my_target.htm?myaction=myAction", broker.toString());
    }

    @Test
    public void reset_withoutParent() {
        broker = createSubBroker(null, true);
        broker.init();
        broker.reset();

        assertEquals(null, broker.getMappingRuleService());
        assertEquals(null, broker.getAction());
        assertEquals(null, broker.getTarget());
        assertEquals(null, broker.getComponentPath());
        assertEquals(true, broker.isConvertTargetCase());
        assertEquals("extension.output", broker.getTargetMappingRule());
        assertEquals("action", broker.getActionParam());
    }

    @Test
    public void config_withMappings() {
        ApplicationContext factory = new XmlApplicationContext(new FileSystemResource(new File(srcdir,
                "services-uris.xml")));

        URIBrokerService uris = (URIBrokerService) factory.getBean("uris");

        URIBroker link1 = uris.getURIBroker("link1");
        assertEquals("http://taobao.com/hello", link1.toString());

        TurbineURIBroker link2 = (TurbineURIBroker) uris.getURIBroker("link2");
        assertSame(factory.getBean("mappingRuleService"), link2.getMappingRuleService());
        assertEquals("http://taobao.com/mycontext/myservlet/mycomponent/my_target.htm?action=myAction",
                link2.toString());

        TurbineURIBroker link3 = (TurbineURIBroker) uris.getURIBroker("link3");
        assertSame(factory.getBean("mapping2"), link3.getMappingRuleService());
        assertEquals("http://taobao.com/mycontext/myservlet/mycomponent/myTarget.vhtml?myaction=myAction",
                link3.toString());
    }

    @Test
    public void config_withoutMappings() {
        ApplicationContext factory = new XmlApplicationContext(new FileSystemResource(new File(srcdir,
                "services-uris-no-mappings.xml")));

        URIBrokerService uris = (URIBrokerService) factory.getBean("uris");

        URIBroker link1 = uris.getURIBroker("link1");
        assertEquals("http://taobao.com/hello", link1.toString());

        TurbineURIBroker link2 = (TurbineURIBroker) uris.getURIBroker("link2");
        assertNull(link2.getMappingRuleService());
        assertEquals("http://taobao.com/mycontext/myservlet/mycomponent/my_target.vm?action=myAction", link2.toString());

        TurbineURIBroker link3 = (TurbineURIBroker) uris.getURIBroker("link3");
        assertNull(link3.getMappingRuleService());
        assertEquals("http://taobao.com/mycontext/myservlet/mycomponent/myTarget.vm?myaction=myAction",
                link3.toString());
    }
}
