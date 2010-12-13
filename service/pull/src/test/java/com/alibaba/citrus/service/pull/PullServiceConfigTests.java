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
package com.alibaba.citrus.service.pull;

import static com.alibaba.citrus.test.TestUtil.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.parsing.BeanDefinitionParsingException;
import org.springframework.context.ApplicationContext;

import com.alibaba.citrus.service.pull.support.ConstantTool;
import com.alibaba.citrus.service.pull.support.ConstantToolSet;
import com.alibaba.citrus.service.pull.tool.SimpleTool;
import com.alibaba.citrus.service.pull.tool.SimpleToolSet;
import com.alibaba.citrus.util.Utils;

public class PullServiceConfigTests extends AbstractPullServiceConfigTests {
    private static final int constantCount;
    private static final int utilCount;
    private Map<String, Object> tools;

    static {
        ConstantToolSet myconst = new ConstantToolSet();
        myconst.setConstantClass(HttpServletResponse.class);
        constantCount = getFieldValue(myconst, "constants", Map.class).size();
        utilCount = Utils.getUtils().size();
    }

    @Test
    public void noId() {
        try {
            createContext("pull/services-pull-wrong-no-id-1.xml");
            fail();
        } catch (BeanDefinitionParsingException e) {
            assertThat(e, exception("Unnamed bean definition"));
        }

        try {
            createContext("pull/services-pull-wrong-no-id-2.xml");
            fail();
        } catch (FatalBeanException e) {
            assertThat(e, exception(IllegalArgumentException.class, "missing id for tool", "myString"));
        }
    }

    @Test
    public void dupId() {
        try {
            createContext("pull/services-pull-wrong-dup-id.xml");
            fail();
        } catch (FatalBeanException e) {
            assertThat(e, exception(IllegalArgumentException.class, "duplicated tool or tool-set ID: constant"));
        }
    }

    @Test
    public void toolNames() {
        factory = createContext("pull/services-pull-names.xml");
        pullService = (PullService) factory.getBean("toolNames");

        // 根据element自动生成toolName
        assertToolName("constants", ConstantTool.class); // <constants />
        assertToolName("myInner", InnerTool.class); // <my-inner-tool />

        // 根据类名自动生成toolName
        assertToolName("inner", InnerTool.class); // <factory class="...$InnerTool" />
        assertToolName("simple", SimpleTool.class); // <factory class="...SimpleTool" />
        assertToolName("simple", SimpleToolSet.class, "toolNames2"); // <factory class="...SimpleToolSet" />

        // 指定toolName
        assertToolName("myconst", ConstantTool.class); // <constants id="myconst" />
        assertToolName("inner3", InnerTool.class); // <my-inner-tool id="inner3" />
        assertToolName("simple2", SimpleTool.class); // <factory id="inner3" class="...SimpleTool" />

        // 根据ref生成toolName
        assertToolName("outterSimple", SimpleTool.class); // <factory ref="outterSimple" />

        // 根据class attribute生成toolName
        assertToolName("innerBean", InnerBean.class); // <bean-tool class="...$InnerBean" />
    }

    private void assertToolName(String toolName, Class<?> type) {
        assertToolName(toolName, type, "toolNames");
    }

    private void assertToolName(String toolName, Class<?> type, String beanName) {
        pullService = (PullService) factory.getBean(beanName);
        Object tool = pullService.getContext().pull(toolName);

        assertNotNull(beanName + "." + toolName, tool);
        assertThat(tool, instanceOf(type));
    }

    @Test
    public void toolSet() throws Exception {
        prepareWebEnvironment(null);

        pullService = (PullService) factory.getBean("pullService");
        tools = pullService.getTools();

        // constantToolSet
        ConstantToolSet myconst = (ConstantToolSet) tools.get("myconst");

        assertNotNull(myconst);
        assertNotNull(tools.get("SC_NOT_FOUND"));
        assertNotNull(myconst.get("SC_NOT_FOUND"));

        // utilToolSet
        assertNotNull(tools.get("utils"));
        assertNotNull(tools.get("stringUtil"));
        assertSame(Utils.getUtils().get("stringUtil"), tools.get("stringUtil"));
        assertSame(Utils.getUtils().get("utils"), tools.get("utils"));
    }

    @Test
    public void nonSingleton() throws Exception {
        pullService = (PullService) factory.getBean("pullService");

        // ------------------
        // request 1
        // ------------------
        prepareWebEnvironment("?id=runtime1");

        Object object1 = getTool("object1");
        Object object2 = getTool("object2");
        Object object3 = getTool("object3");
        Object objectInSet1 = getTool("inset1");
        Object objectInSet2 = getTool("runtime1"); // id由请求的参数决定

        assertNotNull(object1);
        assertNotNull(object2);
        assertNotNull(object3);
        assertNotNull(objectInSet1);
        assertNotNull(objectInSet2);

        assertSame(object1, getTool("object1"));
        assertSame(object2, getTool("object2"));
        assertSame(object3, getTool("object3"));
        assertSame(objectInSet1, getTool("inset1"));
        assertSame(objectInSet2, getTool("runtime1"));

        // ------------------
        // request 2
        // ------------------
        prepareWebEnvironment("?id=runtime2");

        assertNotNull(getTool("object1"));
        assertNotNull(getTool("object2"));
        assertNotNull(getTool("object3"));
        assertNotNull(getTool("inset1"));
        assertNull(getTool("runtime1"));
        assertNotNull(getTool("runtime2")); // id由请求的参数决定

        assertNotSame(object1, getTool("object1"));
        assertNotSame(object2, getTool("object2"));
        assertNotSame(object3, getTool("object3"));
        assertNotSame(objectInSet1, getTool("inset1"));
        assertNotSame(objectInSet2, getTool("runtime2"));
    }

    @Test
    public void sharing() throws Exception {
        ApplicationContext parentContext = createContext("pull/services-pull-parent.xml");
        ApplicationContext factory1 = createContext("pull/services-pull-sub.xml", parentContext);
        ApplicationContext factory2 = createContext("pull/services-pull-sub.xml", parentContext);

        PullService sub1 = (PullService) factory1.getBean("pullService");
        PullService sub2 = (PullService) factory2.getBean("pullService");

        assertNotNull(getFieldValue(sub1, "parent", null));
        assertNotNull(getFieldValue(sub2, "parent", null));

        prepareWebEnvironment("?parent=parentRuntime2&sub=subRuntime2");

        // parentObject is shared among subs
        assertSameAndNotNull(sub1.getContext().pull("parentSingleton1"), sub2.getContext().pull("parentSingleton1"));
        assertSameAndNotNull(sub1.getContext().pull("parentSingleton2"), sub2.getContext().pull("parentSingleton2"));
        assertSameAndNotNull(sub1.getContext().pull("parentSingleton3"), sub2.getContext().pull("parentSingleton3"));

        assertSameAndNotNull(sub1.getContext().pull("parentPrototype1"), sub2.getContext().pull("parentPrototype1"));
        assertSameAndNotNull(sub1.getContext().pull("parentPrototype2"), sub2.getContext().pull("parentPrototype2"));
        assertSameAndNotNull(sub1.getContext().pull("parentPrototype3"), sub2.getContext().pull("parentPrototype3"));

        assertSameAndNotNull(sub1.getContext().pull("parentRuntime1"), sub2.getContext().pull("parentRuntime1"));
        assertSameAndNotNull(sub1.getContext().pull("parentRuntime2"), sub2.getContext().pull("parentRuntime2"));

        // subs objects
        assertNotSameAndNotNull(sub1.getContext().pull("subSingleton1"), sub2.getContext().pull("subSingleton1"));
        assertNotSameAndNotNull(sub1.getContext().pull("subSingleton2"), sub2.getContext().pull("subSingleton2"));
        assertNotSameAndNotNull(sub1.getContext().pull("subSingleton3"), sub2.getContext().pull("subSingleton3"));

        assertNotSameAndNotNull(sub1.getContext().pull("subPrototype1"), sub2.getContext().pull("subPrototype1"));
        assertNotSameAndNotNull(sub1.getContext().pull("subPrototype2"), sub2.getContext().pull("subPrototype2"));
        assertNotSameAndNotNull(sub1.getContext().pull("subPrototype3"), sub2.getContext().pull("subPrototype3"));

        assertNotSameAndNotNull(sub1.getContext().pull("subRuntime1"), sub2.getContext().pull("subRuntime1"));
        assertNotSameAndNotNull(sub1.getContext().pull("subRuntime2"), sub2.getContext().pull("subRuntime2"));
    }

    private void assertSameAndNotNull(Object o1, Object o2) {
        assertNotNull(o1);
        assertNotNull(o2);
        assertSame(o1, o2);
    }

    private void assertNotSameAndNotNull(Object o1, Object o2) {
        assertNotNull(o1);
        assertNotNull(o2);
        assertNotSame(o1, o2);
    }

    @Test
    public void getTools() throws Exception {
        prepareWebEnvironment("?id=inset2");

        pullService = (PullService) factory.getBean("pullService");
        tools = pullService.getTools();

        assertNotNull(tools.get("myconst"));
        assertNotNull(tools.get("myconst2"));
        assertNotNull(tools.get("object1"));
        assertNotNull(tools.get("object2"));
        assertNotNull(tools.get("object3"));
        assertNotNull(tools.get("inset1"));
        assertNotNull(tools.get("inset2"));

        assertEquals(constantCount + utilCount + 8, tools.size());
    }

    private Object getTool(String name) {
        return pullService.getContext().pull(name);
    }
}
