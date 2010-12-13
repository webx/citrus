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
package com.alibaba.citrus.service.pull.impl;

import static com.alibaba.citrus.test.TestUtil.*;
import static com.alibaba.citrus.util.BasicConstant.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static java.util.Collections.*;
import static org.easymock.EasyMock.*;
import static org.easymock.classextension.EasyMock.*;
import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.easymock.classextension.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

import com.alibaba.citrus.service.pull.PullContext;
import com.alibaba.citrus.service.pull.PullException;
import com.alibaba.citrus.service.pull.PullService;
import com.alibaba.citrus.service.pull.RuntimeToolSetFactory;
import com.alibaba.citrus.service.pull.ToolFactory;
import com.alibaba.citrus.service.pull.ToolNameAware;
import com.alibaba.citrus.service.pull.ToolSetFactory;
import com.alibaba.citrus.service.pull.impl.PullServiceImpl.ToolSetInfo;

public class PullServiceTests extends AbstractPullServiceTests {
    private PullContext context;
    private String[] staticToolNames;
    private String[] allNames;
    private String[] allValues;
    private String[] allNamesWithParent;
    private String[] allValuesWithParent;

    // service instance vars
    private Map<String, ToolFactory> tools;
    private Map<String, ToolSetInfo<ToolSetFactory>> toolsInSet;
    private Map<String, RuntimeToolSetFactory> toolsRuntime;
    private Map<String, Object> prePulledTools;
    private Set<String> toolNames;

    // context instance vars
    private Map<String, Object> context_pulledTools;
    private Map<String, RuntimeToolSetFactory> context_toolsRuntime;
    private Map<String, ToolSetInfo<RuntimeToolSetFactory>> context_toolsInRuntimeSet;
    private Set<String> context_toolNames;

    @Before
    public void init() {
        populateService();
        initService();
    }

    private void populateService() {
        Map<String, Object> factories = createHashMap();

        // --------------
        // singleton
        // --------------

        // - ToolFactory
        factories.put("singleton.1", newToolFactory(true, "singleton.1"));
        factories.put("singleton.2", newToolFactory(true, null));

        // - ToolSetFactory
        factories.put("singletons.1", newToolSetFactory(true, false, "singletons.1", "a", "1", "b", "2"));
        factories.put("singletons.2", newToolSetFactory(true, false, null, "c", "3", "d", null));

        // - ToolSetFactory & ToolFactory
        factories.put("singletons.3", newToolSetFactory(true, true, "singletons.3", "e", "5", "f", "6"));
        factories.put("singletons.4", newToolSetFactory(true, true, null, "g", "7", "h", null));

        // --------------
        // non-singleton
        // --------------

        // - ToolFactory
        factories.put("prototype.1", newToolFactory(false, "prototype.1"));
        factories.put("prototype.2", newToolFactory(false, null));

        // - ToolSetFactory
        factories.put("prototypes.1", newToolSetFactory(false, false, "prototypes.1", "i", "9", "j", "10"));
        factories.put("prototypes.2", newToolSetFactory(false, false, null, "k", "11", "l", null));

        // - ToolSetFactory & ToolFactory
        factories.put("prototypes.3", newToolSetFactory(false, true, "prototypes.3", "m", "13", "n", "14"));
        factories.put("prototypes.4", newToolSetFactory(false, true, null, "o", "15", "p", null));

        // - RuntimeToolSetFactory
        factories.put("runtime.1", newRuntimeToolSetFactory(false, false, "runtime.1", "q", "17", "r", "18"));
        factories.put("runtime.2", newRuntimeToolSetFactory(false, false, null, "s", "19", "t", null));

        // - RuntimeToolSetFactory & ToolFactory
        factories.put("runtime.3", newRuntimeToolSetFactory(false, true, "runtime.3", "u", "21", "v", "22"));
        factories.put("runtime.4", newRuntimeToolSetFactory(false, true, null, "w", "23", "x", null));

        service.setToolFactories(factories);

        staticToolNames = new String[] { "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o",
                "p", "prototype.1", "prototype.2", "prototypes.3", "prototypes.4", "runtime.3", "runtime.4",
                "singleton.1", "singleton.2", "singletons.3", "singletons.4" };

        // all names and values including runtime tools
        allNames = new String[] { "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p",
                "prototype.1", "prototype.2", "prototypes.3", "prototypes.4", "q", "r", "runtime.3", "runtime.4", "s",
                "singleton.1", "singleton.2", "singletons.3", "singletons.4", "t", "u", "v", "w", "x" };

        allValues = new String[] { "1", "2", "3", null, "5", "6", "7", null, "9", "10", "11", null, "13", "14", "15",
                null, "prototype.1", null, "prototypes.3", null, "17", "18", "runtime.3", null, "19", "singleton.1",
                null, "singletons.3", null, null, "21", "22", "23", null };
    }

    @SuppressWarnings("unchecked")
    private void initService() {
        try {
            service.afterPropertiesSet();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // service
        assertNull(getFieldValue(service, "toolFactories", Map.class));

        tools = sort(getFieldValue(service, "tools", Map.class));
        toolsInSet = sort(getFieldValue(service, "toolsInSet", Map.class));
        toolsRuntime = sort(getFieldValue(service, "toolsRuntime", Map.class));
        prePulledTools = sort(getFieldValue(service, "prePulledTools", Map.class));
        toolNames = createTreeSet(getFieldValue(service, "toolNames", Set.class));

        assertNotNull(tools);
        assertNotNull(toolsInSet);
        assertNotNull(toolsRuntime);
        assertNotNull(prePulledTools);
        assertNotNull(toolNames);

        // context
        context = service.getContext();
        assertNotNull(context);

        context_pulledTools = getFieldValue(context, "pulledTools", Map.class);
        context_toolsRuntime = getFieldValue(context, "toolsRuntime", Map.class);
        context_toolsInRuntimeSet = getFieldValue(context, "toolsInRuntimeSet", Map.class);
        context_toolNames = getFieldValue(context, "toolNames", Set.class);

        assertNotNull(context_pulledTools);
        assertNotNull(context_toolsRuntime);
        assertNotNull(context_toolsInRuntimeSet);
        assertNotNull(context_toolNames);
    }

    private <T> Map<String, T> sort(Map<String, T> map) {
        Map<String, T> sortedMap = createTreeMap();
        sortedMap.putAll(map);
        return sortedMap;
    }

    private void initServiceWithParent() {
        PullService parent = service;
        service = new PullServiceImpl();

        Map<String, Object> factories = createHashMap();

        factories.put("singleton.3", newToolFactory(true, "singleton.3"));
        factories.put("singletons.5", newToolSetFactory(true, true, "singletons.5", "z1", "11"));

        factories.put("prototype.3", newToolFactory(false, "prototype.3"));
        factories.put("prototypes.5", newToolSetFactory(false, true, "prototypes.5", "z2", "22"));

        factories.put("runtime.5", newRuntimeToolSetFactory(false, true, "runtime.5", "z3", "33"));

        service.setToolFactories(factories);
        service.setParent(parent);

        initService();

        // all names and values including runtime tools and parent's tools
        allNamesWithParent = new String[] { "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o",
                "p", "prototype.1", "prototype.2", "prototype.3", "prototypes.3", "prototypes.4", "prototypes.5", "q",
                "r", "runtime.3", "runtime.4", "runtime.5", "s", "singleton.1", "singleton.2", "singleton.3",
                "singletons.3", "singletons.4", "singletons.5", "t", "u", "v", "w", "x", "z1", "z2", "z3" };

        allValuesWithParent = new String[] { "1", "2", "3", null, "5", "6", "7", null, "9", "10", "11", null, "13",
                "14", "15", null, "prototype.1", null, "prototype.3", "prototypes.3", null, "prototypes.5", "17", "18",
                "runtime.3", null, "runtime.5", "19", "singleton.1", null, "singleton.3", "singletons.3", null,
                "singletons.5", null, "21", "22", "23", null, "11", "22", "33" };
    }

    @Test
    public void service_initParent() throws Exception {
        PullServiceImpl parent = new PullServiceImpl();
        PullServiceImpl sub;

        // no parent
        sub = new PullServiceImpl();
        sub.setApplicationContext(getApplicationContext("pullService", parent, false));
        sub.setBeanName("pullService");
        sub.afterPropertiesSet();

        assertNull(getFieldValue(sub, "parent", PullService.class));

        // with parent, beanName == pullService
        sub = new PullServiceImpl();
        sub.setApplicationContext(getApplicationContext("pullService", parent, true));
        sub.setBeanName("pullService");
        sub.afterPropertiesSet();

        assertSame(parent, getFieldValue(sub, "parent", PullService.class));

        // with parent, beanName == otherName
        sub = new PullServiceImpl();
        sub.setApplicationContext(getApplicationContext("pullService", parent, true));
        sub.setBeanName("otherName");
        sub.afterPropertiesSet();

        assertSame(parent, getFieldValue(sub, "parent", PullService.class));

        // with parent, beanName == otherName
        sub = new PullServiceImpl();
        sub.setApplicationContext(getApplicationContext("otherName", parent, true));
        sub.setBeanName("otherName");
        sub.afterPropertiesSet();

        assertSame(parent, getFieldValue(sub, "parent", PullService.class));

        // with parent, beanName == otherName2
        sub = new PullServiceImpl();
        sub.setApplicationContext(getApplicationContext("otherName", parent, true));
        sub.setBeanName("otherName2");
        sub.afterPropertiesSet();

        assertNull(getFieldValue(sub, "parent", PullService.class));
    }

    private ApplicationContext getApplicationContext(String beanName, PullService parent, boolean withParent) {
        ApplicationContext parentContext = createMock(ApplicationContext.class);
        ApplicationContext thisContext = createMock(ApplicationContext.class);

        if (withParent) {
            expect(thisContext.getParent()).andReturn(parentContext).anyTimes();
        } else {
            expect(thisContext.getParent()).andReturn(null).anyTimes();
        }

        expect(parentContext.containsBean(beanName)).andReturn(true).anyTimes();
        expect(parentContext.containsBean(EasyMock.<String> anyObject())).andReturn(false).anyTimes();
        expect(parentContext.getBean(beanName)).andReturn(parent).anyTimes();

        replay(parentContext, thisContext);

        return thisContext;
    }

    @Test
    public void service_init_noFactories() throws Exception {
        service = new PullServiceImpl();
        initService();

        assertTrue(tools.isEmpty());
        assertTrue(toolsInSet.isEmpty());
        assertTrue(toolsRuntime.isEmpty());
        assertTrue(prePulledTools.isEmpty());
        assertTrue(toolNames.isEmpty());
    }

    @Test
    public void service_init_emptyName() throws Exception {
        service = new PullServiceImpl();

        try {
            Map<String, Object> factories = createHashMap();
            factories.put(null, newToolFactory(true, "111"));

            service.setToolFactories(factories);
            service.afterPropertiesSet();

            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("tool name"));
        }

        try {
            Map<String, Object> factories = createHashMap();
            factories.put("  ", newToolFactory(true, "111"));

            service.setToolFactories(factories);
            service.afterPropertiesSet();

            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("tool name"));
        }
    }

    @Test
    public void service_init_unknwonFactory() throws Exception {
        service = new PullServiceImpl();

        try {
            Map<String, Object> factories = createHashMap();
            factories.put("unknwon", "not a factory");

            service.setToolFactories(factories);
            service.afterPropertiesSet();

            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("unknown pull tool factory type: java.lang.String"));
        }
    }

    @Test
    public void service_init_prePulling_preQueueing() throws Exception {
        List<Object> values;

        // toolNames - all tools and tools in set
        assertArrayEquals(staticToolNames, toolNames.toArray());

        // prePulledTools - pre-pulled singletons
        assertArrayEquals(new Object[] { "a", "b", "c", "d", "e", "f", "g", "h", "singleton.1", "singleton.2",
                "singletons.3", "singletons.4" }, prePulledTools.keySet().toArray());

        assertArrayEquals(new Object[] { "1", "2", "3", NULL_PLACEHOLDER, "5", "6", "7", NULL_PLACEHOLDER,
                "singleton.1", NULL_PLACEHOLDER, "singletons.3", NULL_PLACEHOLDER }, prePulledTools.values().toArray());

        // tools - non-singleton tools only
        assertArrayEquals(new Object[] { "prototype.1", "prototype.2", "prototypes.3", "prototypes.4", "runtime.3",
                "runtime.4" }, tools.keySet().toArray());

        values = createArrayList();

        for (ToolFactory factory : tools.values()) {
            values.add(factory.createTool());
        }

        assertArrayEquals(new Object[] { "prototype.1", null, "prototypes.3", null, "runtime.3", null },
                values.toArray());

        // toolsInSet - non-singleton tools in set only
        assertArrayEquals(new Object[] { "i", "j", "k", "l", "m", "n", "o", "p" }, toolsInSet.keySet().toArray());

        values = createArrayList();

        for (String nameInSet : toolsInSet.keySet()) {
            values.add(toolsInSet.get(nameInSet).getFactory().createTool(nameInSet));
        }

        assertArrayEquals(new Object[] { "9", "10", "11", null, "13", "14", "15", null }, values.toArray());

        // toolsRuntime - runtime tool sets only
        assertArrayEquals(new Object[] { "runtime.1", "runtime.2", "runtime.3", "runtime.4" }, toolsRuntime.keySet()
                .toArray());

        assertEquals(toolNames.size(), tools.size() + toolsInSet.size() + prePulledTools.size());
    }

    @Test
    public void service_init_ToolNameAware() throws Exception {
        service = new PullServiceImpl();

        ToolNameAwareToolFactory f1 = createMock(ToolNameAwareToolFactory.class);
        ToolNameAwareToolSetFactory f2 = createMock(ToolNameAwareToolSetFactory.class);
        ToolNameAwareRuntimeToolSetFactory f3 = createMock(ToolNameAwareRuntimeToolSetFactory.class);

        f1.setToolName("f1");
        f2.setToolName("f2");
        f3.setToolName("f3");

        expect(f1.isSingleton()).andReturn(false);
        expect(f2.isSingleton()).andReturn(false);
        expect(f2.getToolNames()).andReturn(null);

        replay(f1, f2, f3);

        Map<String, Object> factories = createHashMap();
        factories.put("f1", f1);
        factories.put("f2", f2);
        factories.put("f3", f3);
        service.setToolFactories(factories);

        service.afterPropertiesSet();

        verify(f1, f2, f3);
    }

    @Test
    public void service_getContext() {
        // 在web环境中，context被cache，否则不cache
        assertNotSame(service.getContext(), service.getContext());

        setRequestAttrs();
        assertSame(service.getContext(), service.getContext());
    }

    @Test
    public void service_getTools() {
        assertGetTools(service.getTools());

        // 在非web环境中，context不被cache
        assertNotSame(service.getTools(), service.getTools());

        // 在web环境中，context被cache
        setRequestAttrs();
        assertSame(service.getTools(), service.getTools());

        // unmodifiable
        try {
            service.getTools().clear();
            fail();
        } catch (UnsupportedOperationException e) {
        }
    }

    @Test
    public void service_getTools_withParent() {
        initServiceWithParent();

        Map<String, Object> tools = sort(service.getTools());

        assertArrayEquals(allNamesWithParent, tools.keySet().toArray());
        assertArrayEquals(allValuesWithParent, tools.values().toArray());

        // 在非web环境中，context不被cache
        assertNotSame(service.getTools(), service.getTools());

        // 在web环境中，context被cache
        setRequestAttrs();
        assertSame(service.getTools(), service.getTools());

        // unmodifiable
        try {
            service.getTools().clear();
            fail();
        } catch (UnsupportedOperationException e) {
        }
    }

    @Test
    public void service_toString() {
        // inited with tools
        String str = "";
        str += "PullTools [\n";
        str += "  [ 1/26] a\n";
        str += "  [ 2/26] b\n";
        str += "  [ 3/26] c\n";
        str += "  [ 4/26] d\n";
        str += "  [ 5/26] e\n";
        str += "  [ 6/26] f\n";
        str += "  [ 7/26] g\n";
        str += "  [ 8/26] h\n";
        str += "  [ 9/26] i\n";
        str += "  [10/26] j\n";
        str += "  [11/26] k\n";
        str += "  [12/26] l\n";
        str += "  [13/26] m\n";
        str += "  [14/26] n\n";
        str += "  [15/26] o\n";
        str += "  [16/26] p\n";
        str += "  [17/26] prototype.1\n";
        str += "  [18/26] prototype.2\n";
        str += "  [19/26] prototypes.3\n";
        str += "  [20/26] prototypes.4\n";
        str += "  [21/26] runtime.3\n";
        str += "  [22/26] runtime.4\n";
        str += "  [23/26] singleton.1\n";
        str += "  [24/26] singleton.2\n";
        str += "  [25/26] singletons.3\n";
        str += "  [26/26] singletons.4\n";
        str += "]";

        assertEquals(str, service.toString());

        // not inited
        service = new PullServiceImpl();
        assertEquals("PullTools[uninitialized]", service.toString());

        // inited empty
        initService();
        assertEquals("PullTools[]", service.toString());
    }

    @Test
    public void service_toString_withParent() {
        initServiceWithParent();

        String str = "";
        str += "PullTools [\n";
        str += "  [1/7] prototype.3\n";
        str += "  [2/7] prototypes.5\n";
        str += "  [3/7] runtime.5\n";
        str += "  [4/7] singleton.3\n";
        str += "  [5/7] singletons.5\n";
        str += "  [6/7] z1\n";
        str += "  [7/7] z2\n";
        str += "]\n";
        str += "Parent PullTools [\n";
        str += "  [ 1/26] a\n";
        str += "  [ 2/26] b\n";
        str += "  [ 3/26] c\n";
        str += "  [ 4/26] d\n";
        str += "  [ 5/26] e\n";
        str += "  [ 6/26] f\n";
        str += "  [ 7/26] g\n";
        str += "  [ 8/26] h\n";
        str += "  [ 9/26] i\n";
        str += "  [10/26] j\n";
        str += "  [11/26] k\n";
        str += "  [12/26] l\n";
        str += "  [13/26] m\n";
        str += "  [14/26] n\n";
        str += "  [15/26] o\n";
        str += "  [16/26] p\n";
        str += "  [17/26] prototype.1\n";
        str += "  [18/26] prototype.2\n";
        str += "  [19/26] prototypes.3\n";
        str += "  [20/26] prototypes.4\n";
        str += "  [21/26] runtime.3\n";
        str += "  [22/26] runtime.4\n";
        str += "  [23/26] singleton.1\n";
        str += "  [24/26] singleton.2\n";
        str += "  [25/26] singletons.3\n";
        str += "  [26/26] singletons.4\n";
        str += "]";

        assertEquals(str, service.toString());
    }

    @Test
    public void context_getToolNames() {
        assertArrayEquals(allNames, context.getToolNames().toArray());

        // cached
        assertSame(context.getToolNames(), context.getToolNames());

        // unmodifiable
        try {
            context.getToolNames().clear();
            fail();
        } catch (UnsupportedOperationException e) {
        }
    }

    @Test
    public void context_getToolNames_withParent() {
        initServiceWithParent();

        assertArrayEquals(allNamesWithParent, context.getToolNames().toArray());

        // cached
        assertSame(context.getToolNames(), context.getToolNames());

        // unmodifiable
        try {
            context.getToolNames().clear();
            fail();
        } catch (UnsupportedOperationException e) {
        }
    }

    @Test
    public void context_getTools() {
        assertGetTools(context.getTools());

        // cached
        assertSame(context.getTools(), context.getTools());

        // unmodifiable
        try {
            context.getTools().clear();
            fail();
        } catch (UnsupportedOperationException e) {
        }
    }

    @Test
    public void context_getTools_withParent() {
        initServiceWithParent();

        Map<String, Object> tools = createTreeMap();
        tools.putAll(context.getTools());

        assertArrayEquals(allNamesWithParent, tools.keySet().toArray());
        assertArrayEquals(allValuesWithParent, tools.values().toArray());

        // cached
        assertSame(context.getTools(), context.getTools());

        // unmodifiable
        try {
            context.getTools().clear();
            fail();
        } catch (UnsupportedOperationException e) {
        }
    }

    @Test
    public void context_pull() {
        assertContext(/* pulledTools */0, /* toolsRuntime */4, /* toolsInRuntimeSet */0, /* toolNames */26);

        // tool, singleton
        assertEquals("singleton.1", context.pull("singleton.1"));
        assertEquals(null, context.pull("singleton.2"));

        assertContext(/* pulledTools */0, /* toolsRuntime */4, /* toolsInRuntimeSet */0, /* toolNames */26);

        // tool-set, singleton
        assertEquals("singletons.3", context.pull("singletons.3"));
        assertEquals(null, context.pull("singletons.4"));

        assertContext(/* pulledTools */0, /* toolsRuntime */4, /* toolsInRuntimeSet */0, /* toolNames */26);

        assertEquals("1", context.pull("a"));
        assertEquals("2", context.pull("b"));
        assertEquals("3", context.pull("c"));
        assertEquals(null, context.pull("d"));

        assertEquals("5", context.pull("e"));
        assertEquals("6", context.pull("f"));
        assertEquals("7", context.pull("g"));
        assertEquals(null, context.pull("h"));

        assertContext(/* pulledTools */0, /* toolsRuntime */4, /* toolsInRuntimeSet */0, /* toolNames */26);

        // tool, non-singleton
        assertEquals("prototype.1", context.pull("prototype.1"));
        assertEquals(null, context.pull("prototype.2"));

        assertContext(/* pulledTools */2, /* toolsRuntime */4, /* toolsInRuntimeSet */0, /* toolNames */26);

        // tool-set, non-singleton
        assertEquals("prototypes.3", context.pull("prototypes.3"));
        assertEquals(null, context.pull("prototypes.4"));

        assertContext(/* pulledTools */4, /* toolsRuntime */4, /* toolsInRuntimeSet */0, /* toolNames */26);

        assertEquals("9", context.pull("i"));
        assertEquals("10", context.pull("j"));
        assertEquals("11", context.pull("k"));
        assertEquals(null, context.pull("l"));

        assertEquals("13", context.pull("m"));
        assertEquals("14", context.pull("n"));
        assertEquals("15", context.pull("o"));
        assertEquals(null, context.pull("p"));

        assertContext(/* pulledTools */12, /* toolsRuntime */4, /* toolsInRuntimeSet */0, /* toolNames */26);

        // tool-set, runtime
        assertEquals("runtime.3", context.pull("runtime.3"));
        assertEquals(null, context.pull("runtime.4"));

        assertContext(/* pulledTools */14, /* toolsRuntime */4, /* toolsInRuntimeSet */0, /* toolNames */26);

        assertEquals("17", context.pull("q"));
        assertContext(/* pulledTools */15, /* toolsRuntime */3, /* toolsInRuntimeSet */2, /* toolNames */28);
        assertEquals("18", context.pull("r"));
        assertContext(/* pulledTools */16, /* toolsRuntime */3, /* toolsInRuntimeSet */2, /* toolNames */28);

        assertEquals("19", context.pull("s"));
        assertContext(/* pulledTools */17, /* toolsRuntime */2, /* toolsInRuntimeSet */4, /* toolNames */30);
        assertEquals(null, context.pull("t"));
        assertContext(/* pulledTools */18, /* toolsRuntime */2, /* toolsInRuntimeSet */4, /* toolNames */30);

        assertEquals("21", context.pull("u"));
        assertContext(/* pulledTools */19, /* toolsRuntime */1, /* toolsInRuntimeSet */6, /* toolNames */32);
        assertEquals("22", context.pull("v"));
        assertContext(/* pulledTools */20, /* toolsRuntime */1, /* toolsInRuntimeSet */6, /* toolNames */32);

        assertEquals("23", context.pull("w"));
        assertContext(/* pulledTools */21, /* toolsRuntime */0, /* toolsInRuntimeSet */8, /* toolNames */34);
        assertEquals(null, context.pull("x"));
        assertContext(/* pulledTools */22, /* toolsRuntime */0, /* toolsInRuntimeSet */8, /* toolNames */34);
    }

    @Test
    public void context_pull_empty() {
        assertEquals(null, context.pull(null));
        assertEquals(null, context.pull("  "));

        assertContext(/* pulledTools */0, /* toolsRuntime */4, /* toolsInRuntimeSet */0, /* toolNames */26);
    }

    @Test
    public void context_pull_notExist() {
        assertEquals(null, context.pull("notexist"));
        assertContext(/* pulledTools */0, /* toolsRuntime */0, /* toolsInRuntimeSet */8, /* toolNames */34);
    }

    @Test
    public void context_pull_exceptions() throws Exception {
        // singleton
        try {
            createPullService("mysingleton", new ExceptionToolFactory(true, "my error"));
            fail();
        } catch (PullException e) {
            assertThat(e,
                    exception(IllegalArgumentException.class, "Could not create tool: \"mysingleton\"", "my error"));
        }

        try {
            createPullService("mysingletonSet", new ExceptionToolSetFactory(true, "my error"));
            fail();
        } catch (PullException e) {
            assertThat(
                    e,
                    exception(IllegalArgumentException.class, "Could not create tool: \"mysingletonSet.subname\"",
                            "my error"));
        }

        // non-singleton
        createPullService("myprototype", new ExceptionToolFactory(false, "my error"));

        try {
            service.getTools();
            fail();
        } catch (PullException e) {
            assertThat(e,
                    exception(IllegalArgumentException.class, "Could not create tool: \"myprototype\"", "my error"));
        }

        createPullService("myprototypeSet", new ExceptionToolSetFactory(false, "my error"));

        try {
            service.getTools();
            fail();
        } catch (PullException e) {
            assertThat(
                    e,
                    exception(IllegalArgumentException.class, "Could not create tool: \"myprototypeSet.subname\"",
                            "my error"));
        }

        // runtime
        createPullService("myruntime", new ExceptionRuntimeToolSetFactory(false, "my error", false));

        try {
            service.getTools();
            fail();
        } catch (PullException e) {
            assertThat(
                    e,
                    exception(IllegalArgumentException.class, "Could not create tool: \"myruntime.subname\"",
                            "my error"));
        }

        // runtime - failure on createTool()
        createPullService("myruntime", new ExceptionRuntimeToolSetFactory(false, "my error", true));

        try {
            service.getTools();
            fail();
        } catch (PullException e) {
            assertThat(
                    e,
                    exception(IllegalArgumentException.class, "Could not create runtime tool-set: \"myruntime\"",
                            "my error"));
        }
    }

    private void createPullService(String name, Object factory) throws Exception {
        service = new PullServiceImpl();

        Map<String, Object> factories = createHashMap();
        factories.put(name, factory);
        service.setToolFactories(factories);

        service.afterPropertiesSet();
    }

    @Test
    public void context_pull_withParent() {
        setRequestAttrs(); // 确保每次取得同一个context，以便于测试

        // init parent context
        Map<String, Object> parent_pulledTools;
        Map<String, RuntimeToolSetFactory> parent_toolsRuntime;
        Map<String, ToolSetInfo<RuntimeToolSetFactory>> parent_toolsInRuntimeSet;
        Set<String> parent_toolNames;

        populateService();
        initService();
        parent_pulledTools = context_pulledTools;
        parent_toolsRuntime = context_toolsRuntime;
        parent_toolsInRuntimeSet = context_toolsInRuntimeSet;
        parent_toolNames = context_toolNames;

        // init this context
        Map<String, Object> this_pulledTools;
        Map<String, RuntimeToolSetFactory> this_toolsRuntime;
        Map<String, ToolSetInfo<RuntimeToolSetFactory>> this_toolsInRuntimeSet;
        Set<String> this_toolNames;

        initServiceWithParent();
        this_pulledTools = context_pulledTools;
        this_toolsRuntime = context_toolsRuntime;
        this_toolsInRuntimeSet = context_toolsInRuntimeSet;
        this_toolNames = context_toolNames;

        assertContext(/* pulledTools */0, /* toolsRuntime */1, /* toolsInRuntimeSet */0, /* toolNames */7);

        // restore parent context and test it
        context_pulledTools = parent_pulledTools;
        context_toolsRuntime = parent_toolsRuntime;
        context_toolsInRuntimeSet = parent_toolsInRuntimeSet;
        context_toolNames = parent_toolNames;

        context_pull();

        // restore this context and test it
        context_pulledTools = this_pulledTools;
        context_toolsRuntime = this_toolsRuntime;
        context_toolsInRuntimeSet = this_toolsInRuntimeSet;
        context_toolNames = this_toolNames;

        assertContext(/* pulledTools */0, /* toolsRuntime */0, /* toolsInRuntimeSet */1, /* toolNames */8);

        assertEquals("singleton.3", context.pull("singleton.3"));
        assertEquals("singletons.5", context.pull("singletons.5"));
        assertEquals("prototype.3", context.pull("prototype.3"));
        assertEquals("prototypes.5", context.pull("prototypes.5"));
        assertEquals("runtime.5", context.pull("runtime.5"));
        assertEquals("11", context.pull("z1"));
        assertEquals("22", context.pull("z2"));
        assertEquals("33", context.pull("z3"));

        assertContext(/* pulledTools */5, /* toolsRuntime */0, /* toolsInRuntimeSet */1, /* toolNames */8);
    }

    private void assertGetTools(Map<String, Object> tools) {
        tools = createTreeMap();
        tools.putAll(context.getTools());

        assertArrayEquals(allNames, tools.keySet().toArray());
        assertArrayEquals(allValues, tools.values().toArray());
    }

    private void assertContext(int pulledTools, int toolsRuntime, int toolsInRuntimeSet, int toolNames) {
        assertEquals(pulledTools, context_pulledTools.size());
        assertEquals(toolsRuntime, context_toolsRuntime.size());
        assertEquals(toolsInRuntimeSet, context_toolsInRuntimeSet.size());
        assertEquals(toolNames, context_toolNames.size());
    }

    @Test
    public void context_toString() {
        String str;

        // has not pulled all
        str = "";
        str += "PullContext {\n";
        str += "  prePulledTools = {\n";
        str += "                     [ 1/12] a            = 1\n";
        str += "                     [ 2/12] b            = 2\n";
        str += "                     [ 3/12] c            = 3\n";
        str += "                     [ 4/12] d            = null\n";
        str += "                     [ 5/12] e            = 5\n";
        str += "                     [ 6/12] f            = 6\n";
        str += "                     [ 7/12] g            = 7\n";
        str += "                     [ 8/12] h            = null\n";
        str += "                     [ 9/12] singleton.1  = singleton.1\n";
        str += "                     [10/12] singleton.2  = null\n";
        str += "                     [11/12] singletons.3 = singletons.3\n";
        str += "                     [12/12] singletons.4 = null\n";
        str += "                   }\n";
        str += "  pulledTools    = {}\n";
        str += "}";

        assertEquals(str, context.toString());

        // pulled all
        context.getTools();

        str = "";
        str += "PullContext {\n";
        str += "  prePulledTools = {\n";
        str += "                     [ 1/12] a            = 1\n";
        str += "                     [ 2/12] b            = 2\n";
        str += "                     [ 3/12] c            = 3\n";
        str += "                     [ 4/12] d            = null\n";
        str += "                     [ 5/12] e            = 5\n";
        str += "                     [ 6/12] f            = 6\n";
        str += "                     [ 7/12] g            = 7\n";
        str += "                     [ 8/12] h            = null\n";
        str += "                     [ 9/12] singleton.1  = singleton.1\n";
        str += "                     [10/12] singleton.2  = null\n";
        str += "                     [11/12] singletons.3 = singletons.3\n";
        str += "                     [12/12] singletons.4 = null\n";
        str += "                   }\n";
        str += "  pulledTools    = {\n";
        str += "                     [ 1/22] i            = 9\n";
        str += "                     [ 2/22] j            = 10\n";
        str += "                     [ 3/22] k            = 11\n";
        str += "                     [ 4/22] l            = null\n";
        str += "                     [ 5/22] m            = 13\n";
        str += "                     [ 6/22] n            = 14\n";
        str += "                     [ 7/22] o            = 15\n";
        str += "                     [ 8/22] p            = null\n";
        str += "                     [ 9/22] prototype.1  = prototype.1\n";
        str += "                     [10/22] prototype.2  = null\n";
        str += "                     [11/22] prototypes.3 = prototypes.3\n";
        str += "                     [12/22] prototypes.4 = null\n";
        str += "                     [13/22] q            = 17\n";
        str += "                     [14/22] r            = 18\n";
        str += "                     [15/22] runtime.3    = runtime.3\n";
        str += "                     [16/22] runtime.4    = null\n";
        str += "                     [17/22] s            = 19\n";
        str += "                     [18/22] t            = null\n";
        str += "                     [19/22] u            = 21\n";
        str += "                     [20/22] v            = 22\n";
        str += "                     [21/22] w            = 23\n";
        str += "                     [22/22] x            = null\n";
        str += "                   }\n";
        str += "}";

        assertEquals(str, context.toString());
    }

    @Test
    public void context_toString_withParent() {
        initServiceWithParent();

        String str;

        // has not pulled all
        str = "";
        str += "PullContext {\n";
        str += "  prePulledTools = {\n";
        str += "                     [1/3] singleton.3  = singleton.3\n";
        str += "                     [2/3] singletons.5 = singletons.5\n";
        str += "                     [3/3] z1           = 11\n";
        str += "                   }\n";
        str += "  pulledTools    = {}\n";
        str += "}\n";
        str += "Parent PullContext {\n";
        str += "  prePulledTools = {\n";
        str += "                     [ 1/12] a            = 1\n";
        str += "                     [ 2/12] b            = 2\n";
        str += "                     [ 3/12] c            = 3\n";
        str += "                     [ 4/12] d            = null\n";
        str += "                     [ 5/12] e            = 5\n";
        str += "                     [ 6/12] f            = 6\n";
        str += "                     [ 7/12] g            = 7\n";
        str += "                     [ 8/12] h            = null\n";
        str += "                     [ 9/12] singleton.1  = singleton.1\n";
        str += "                     [10/12] singleton.2  = null\n";
        str += "                     [11/12] singletons.3 = singletons.3\n";
        str += "                     [12/12] singletons.4 = null\n";
        str += "                   }\n";
        str += "  pulledTools    = {}\n";
        str += "}";

        assertEquals(str, context.toString());

        // pulled all
        context.getTools();

        str = "";
        str += "PullContext {\n";
        str += "  prePulledTools = {\n";
        str += "                     [1/3] singleton.3  = singleton.3\n";
        str += "                     [2/3] singletons.5 = singletons.5\n";
        str += "                     [3/3] z1           = 11\n";
        str += "                   }\n";
        str += "  pulledTools    = {\n";
        str += "                     [1/5] prototype.3  = prototype.3\n";
        str += "                     [2/5] prototypes.5 = prototypes.5\n";
        str += "                     [3/5] runtime.5    = runtime.5\n";
        str += "                     [4/5] z2           = 22\n";
        str += "                     [5/5] z3           = 33\n";
        str += "                   }\n";
        str += "}\n";
        str += "Parent PullContext {\n";
        str += "  prePulledTools = {\n";
        str += "                     [ 1/12] a            = 1\n";
        str += "                     [ 2/12] b            = 2\n";
        str += "                     [ 3/12] c            = 3\n";
        str += "                     [ 4/12] d            = null\n";
        str += "                     [ 5/12] e            = 5\n";
        str += "                     [ 6/12] f            = 6\n";
        str += "                     [ 7/12] g            = 7\n";
        str += "                     [ 8/12] h            = null\n";
        str += "                     [ 9/12] singleton.1  = singleton.1\n";
        str += "                     [10/12] singleton.2  = null\n";
        str += "                     [11/12] singletons.3 = singletons.3\n";
        str += "                     [12/12] singletons.4 = null\n";
        str += "                   }\n";
        str += "  pulledTools    = {\n";
        str += "                     [ 1/22] i            = 9\n";
        str += "                     [ 2/22] j            = 10\n";
        str += "                     [ 3/22] k            = 11\n";
        str += "                     [ 4/22] l            = null\n";
        str += "                     [ 5/22] m            = 13\n";
        str += "                     [ 6/22] n            = 14\n";
        str += "                     [ 7/22] o            = 15\n";
        str += "                     [ 8/22] p            = null\n";
        str += "                     [ 9/22] prototype.1  = prototype.1\n";
        str += "                     [10/22] prototype.2  = null\n";
        str += "                     [11/22] prototypes.3 = prototypes.3\n";
        str += "                     [12/22] prototypes.4 = null\n";
        str += "                     [13/22] q            = 17\n";
        str += "                     [14/22] r            = 18\n";
        str += "                     [15/22] runtime.3    = runtime.3\n";
        str += "                     [16/22] runtime.4    = null\n";
        str += "                     [17/22] s            = 19\n";
        str += "                     [18/22] t            = null\n";
        str += "                     [19/22] u            = 21\n";
        str += "                     [20/22] v            = 22\n";
        str += "                     [21/22] w            = 23\n";
        str += "                     [22/22] x            = null\n";
        str += "                   }\n";
        str += "}";

        assertEquals(str, context.toString());
    }

    @Test
    public void context_sharing() throws Exception {
        setRequestAttrs();

        // init parent
        PullServiceImpl parent = new PullServiceImpl();

        Map<String, Object> factories = createHashMap();
        factories.put("parentSingleton", new MyToolFactory(true));
        factories.put("parentPrototype", new MyToolFactory(false));
        factories.put("parentRuntime", new MyRuntimeToolSetFactory("parentRuntime"));
        parent.setToolFactories(factories);
        parent.afterPropertiesSet();

        // init sub1
        PullServiceImpl sub1 = new PullServiceImpl();
        factories = createHashMap();
        factories.put("subSingleton", new MyToolFactory(true));
        factories.put("subPrototype", new MyToolFactory(false));
        factories.put("subRuntime", new MyRuntimeToolSetFactory("subRuntime"));
        sub1.setParent(parent);
        sub1.setToolFactories(factories);
        sub1.afterPropertiesSet();

        // init sub2
        PullServiceImpl sub2 = new PullServiceImpl();
        factories = createHashMap();
        factories.put("subSingleton", new MyToolFactory(true));
        factories.put("subPrototype", new MyToolFactory(false));
        factories.put("subRuntime", new MyRuntimeToolSetFactory("subRuntime"));
        sub2.setParent(parent);
        sub2.setToolFactories(factories);
        sub2.afterPropertiesSet();

        // parentObject is shared among subs
        assertSameAndNotNull(sub1.getContext().pull("parentSingleton"), sub2.getContext().pull("parentSingleton"));
        assertSameAndNotNull(sub1.getContext().pull("parentPrototype"), sub2.getContext().pull("parentPrototype"));
        assertSameAndNotNull(sub1.getContext().pull("parentRuntime"), sub2.getContext().pull("parentRuntime"));

        assertNotSameAndNotNull(sub1.getContext().pull("subSingleton"), sub2.getContext().pull("subSingleton"));
        assertNotSameAndNotNull(sub1.getContext().pull("subPrototype"), sub2.getContext().pull("subPrototype"));
        assertNotSameAndNotNull(sub1.getContext().pull("subRuntime"), sub2.getContext().pull("subRuntime"));
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

    private static class MyToolFactory implements ToolFactory {
        private boolean singleton;

        public MyToolFactory(boolean singleton) {
            this.singleton = singleton;
        }

        public boolean isSingleton() {
            return singleton;
        }

        public Object createTool() {
            return new Object();
        }
    }

    private static class MyRuntimeToolSetFactory implements RuntimeToolSetFactory {
        private List<String> keys;

        public MyRuntimeToolSetFactory(String name) {
            this.keys = singletonList(name);
        }

        public Object createToolSet() throws Exception {
            return null;
        }

        public Iterable<String> getToolNames(Object toolSet) {
            return keys;
        }

        public Object createTool(Object toolSet, String name) throws Exception {
            return new Object();
        }
    }

    private static class ExceptionBaseFactory {
        private boolean singleton;
        private String msg;
        private boolean failOnCreatingToolSet;

        public ExceptionBaseFactory(boolean singleton, String msg) {
            this(singleton, msg, true);
        }

        public ExceptionBaseFactory(boolean singleton, String msg, boolean failOnCreatingToolSet) {
            this.singleton = singleton;
            this.msg = msg;
            this.failOnCreatingToolSet = failOnCreatingToolSet;
        }

        public boolean isSingleton() {
            return singleton;
        }

        public Object createTool() throws Exception {
            throw new IllegalArgumentException(msg);
        }

        public Iterable<String> getToolNames() {
            return singletonList("subname");
        }

        public Object createTool(String name) throws Exception {
            throw new IllegalArgumentException(msg);
        }

        public Object createToolSet() throws Exception {
            if (failOnCreatingToolSet) {
                throw new IllegalArgumentException(msg);
            }

            return null;
        }

        public Iterable<String> getToolNames(Object tool) {
            return singletonList("subname");
        }

        public Object createTool(Object tool, String name) throws Exception {
            throw new IllegalArgumentException(msg);
        }
    }

    private static class ExceptionToolFactory extends ExceptionBaseFactory implements ToolFactory {
        public ExceptionToolFactory(boolean singleton, String msg) {
            super(singleton, msg);
        }
    }

    private static class ExceptionToolSetFactory extends ExceptionBaseFactory implements ToolSetFactory {
        public ExceptionToolSetFactory(boolean singleton, String msg) {
            super(singleton, msg);
        }
    }

    private static class ExceptionRuntimeToolSetFactory extends ExceptionBaseFactory implements RuntimeToolSetFactory {
        public ExceptionRuntimeToolSetFactory(boolean singleton, String msg, boolean failOnCreatingTool) {
            super(singleton, msg, failOnCreatingTool);
        }
    }

    private static interface ToolNameAwareToolFactory extends ToolFactory, ToolNameAware {
    }

    private static interface ToolNameAwareToolSetFactory extends ToolSetFactory, ToolNameAware {
    }

    private static interface ToolNameAwareRuntimeToolSetFactory extends RuntimeToolSetFactory, ToolNameAware {
    }
}
