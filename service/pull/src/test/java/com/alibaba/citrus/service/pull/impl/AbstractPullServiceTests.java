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

import static com.alibaba.citrus.util.CollectionUtil.*;
import static org.junit.Assert.*;

import java.util.Collections;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import com.alibaba.citrus.service.pull.RuntimeToolSetFactory;
import com.alibaba.citrus.service.pull.ToolFactory;
import com.alibaba.citrus.service.pull.ToolSetFactory;
import com.alibaba.citrus.test.TestEnvStatic;
import com.alibaba.citrus.test.runner.TestNameAware;

@RunWith(TestNameAware.class)
public abstract class AbstractPullServiceTests {
    protected PullServiceImpl service;

    // request context
    protected Map<String, Object> attrs;

    @BeforeClass
    public static void initEnv() {
        TestEnvStatic.init();
    }

    @Before
    public final void initBasic() {
        attrs = createHashMap();
        service = new PullServiceImpl();
    }

    @After
    public final void clearRequestAttrs() {
        RequestContextHolder.resetRequestAttributes();
    }

    protected final void setRequestAttrs() {
        RequestContextHolder.setRequestAttributes(new RequestAttributes() {
            public Object getAttribute(String name, int scope) {
                return attrs.get(name);
            }

            public void setAttribute(String name, Object value, int scope) {
                attrs.put(name, value);
            }

            public String[] getAttributeNames(int scope) {
                throw new UnsupportedOperationException();
            }

            public String getSessionId() {
                throw new UnsupportedOperationException();
            }

            public Object getSessionMutex() {
                throw new UnsupportedOperationException();
            }

            public void registerDestructionCallback(String name, Runnable callback, int scope) {
                throw new UnsupportedOperationException();
            }

            public void removeAttribute(String name, int scope) {
                throw new UnsupportedOperationException();
            }
        });
    }

    /**
     * 创建<code>ToolFactory</code>。
     */
    protected final ToolFactory newToolFactory(boolean isSingleton, Object object) {
        return new ToolFactoryImpl(isSingleton, object, null);
    }

    /**
     * 创建<code>ToolSetFactory</code>。
     */
    protected final ToolSetFactory newToolSetFactory(boolean isSingleton, boolean isToolFactory, Object object,
                                                     Object... namesAndObjects) {
        Map<String, Object> tools = createLinkedHashMap();

        for (int i = 0; i < namesAndObjects.length; i += 2) {
            String name = (String) namesAndObjects[i];
            Object obj = namesAndObjects[i + 1];

            tools.put(name, obj);
        }

        if (isToolFactory) {
            return new ToolSetFactoryImpl2(isSingleton, object, tools);
        } else {
            return new ToolSetFactoryImpl(isSingleton, object, tools);
        }
    }

    /**
     * 创建<code>RuntimeToolSetFactory</code>。
     */
    protected final RuntimeToolSetFactory newRuntimeToolSetFactory(boolean isSingleton, boolean isToolFactory,
                                                                   Object object, Object... namesAndObjects) {
        Map<String, Object> tools = createLinkedHashMap();

        for (int i = 0; i < namesAndObjects.length; i += 2) {
            String name = (String) namesAndObjects[i];
            Object obj = namesAndObjects[i + 1];

            tools.put(name, obj);
        }

        if (isToolFactory) {
            return new RuntimeToolSetFactoryImpl2(isSingleton, object, tools);
        } else {
            return new RuntimeToolSetFactoryImpl(isSingleton, object, tools);
        }
    }

    private static class ToolFactoryImpl extends BaseFactory implements ToolFactory {
        public ToolFactoryImpl(boolean singleton, Object tool, Map<String, Object> tools) {
            super(singleton, tool, tools);
        }
    }

    private static class ToolSetFactoryImpl extends BaseFactory implements ToolSetFactory {
        public ToolSetFactoryImpl(boolean singleton, Object tool, Map<String, Object> tools) {
            super(singleton, tool, tools);
        }
    }

    private static class ToolSetFactoryImpl2 extends BaseFactory implements ToolFactory, ToolSetFactory {
        public ToolSetFactoryImpl2(boolean singleton, Object tool, Map<String, Object> tools) {
            super(singleton, tool, tools);
        }
    }

    private static class RuntimeToolSetFactoryImpl extends BaseFactory implements RuntimeToolSetFactory {
        public RuntimeToolSetFactoryImpl(boolean singleton, Object tool, Map<String, Object> tools) {
            super(singleton, tool, tools);
        }
    }

    private static class RuntimeToolSetFactoryImpl2 extends BaseFactory implements ToolFactory, RuntimeToolSetFactory {
        public RuntimeToolSetFactoryImpl2(boolean singleton, Object tool, Map<String, Object> tools) {
            super(singleton, tool, tools);
        }
    }

    private static class BaseFactory {
        private final boolean singleton;
        private final Object tool;
        private final Map<String, Object> tools;

        public BaseFactory(boolean singleton, Object tool, Map<String, Object> tools) {
            this.singleton = singleton;
            this.tool = tool;
            this.tools = tools == null ? Collections.<String, Object> emptyMap() : tools;
        }

        public boolean isSingleton() {
            return singleton;
        }

        public Object createTool() {
            return tool;
        }

        public Iterable<String> getToolNames() {
            if (tools.isEmpty()) {
                return null;
            } else {
                return tools.keySet();
            }
        }

        public Object createTool(String name) {
            return tools.get(name);
        }

        public Object createToolSet() {
            return tool;
        }

        public Iterable<String> getToolNames(Object toolSet) {
            assertSame(this.tool, tool);
            return getToolNames();
        }

        public Object createTool(Object toolSet, String name) {
            assertSame(this.tool, tool);
            return tools.get(name);
        }
    }
}
