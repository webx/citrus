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

import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.BasicConstant.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;
import static java.util.Collections.*;
import static org.springframework.web.context.request.RequestAttributes.*;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import com.alibaba.citrus.service.AbstractService;
import com.alibaba.citrus.service.pull.PullContext;
import com.alibaba.citrus.service.pull.PullException;
import com.alibaba.citrus.service.pull.PullService;
import com.alibaba.citrus.service.pull.RuntimeToolSetFactory;
import com.alibaba.citrus.service.pull.ToolFactory;
import com.alibaba.citrus.service.pull.ToolNameAware;
import com.alibaba.citrus.service.pull.ToolSetFactory;
import com.alibaba.citrus.util.internal.ToStringBuilder;
import com.alibaba.citrus.util.internal.ToStringBuilder.CollectionBuilder;
import com.alibaba.citrus.util.internal.ToStringBuilder.MapBuilder;

public class PullServiceImpl extends AbstractService<PullService> implements PullService, ApplicationContextAware {
    private final static String DEFAULT_BEAN_NAME = "pullService";

    private final static int SINGLETON = 0x1;
    private final static int TOOL_FACTORY = 0x2;
    private final static int TOOL_SET_FACTORY = 0x4;
    private final static int RUNTIME_TOOL_SET_FACTORY = 0x8;

    private final static AtomicInteger contextKeyCounter = new AtomicInteger();
    private ApplicationContext beanFactory;
    private PullService parent;
    private String contextKey;

    // toolName -> factory，包含所有类型的factories，初始化后即清除。
    // 可能包含：
    // 1. ToolFactory           - non-singleton or singleton
    // 2. ToolSetFactory        - non-singleton or singleton
    // 3. RuntimeToolSetFactory - non-singleton
    private Map<String, Object> toolFactories;

    // toolName -> toolFactory，non-singleton tool factories
    private Map<String, ToolFactory> tools;

    // toolName -> toolSetFactory，non-singleton tool set factories
    private Map<String, ToolSetInfo<ToolSetFactory>> toolsInSet;

    // toolName -> runtimeToolSetFactory
    private Map<String, RuntimeToolSetFactory> toolsRuntime;

    // 在初始化时，预先被pull的tools，有两种来源：
    // 1. singleton toolFactory
    // 2. singleton toolSetFactory
    private Map<String, Object> prePulledTools;

    // 所有非runtime tools的名称，包括：
    // 1. singleton or non-singleton tools
    // 1. singleton or non-singleton tools in set
    private Set<String> toolNames;

    public void setApplicationContext(ApplicationContext factory) {
        this.beanFactory = factory;
    }

    public void setParent(PullService parent) {
        this.parent = parent;
    }

    public void setToolFactories(Map<String, Object> factories) {
        this.toolFactories = factories;
    }

    @Override
    protected void init() {
        initParent();
        initContextKey();
        initToolFactories();

        getLogger().info(
                "Initialized pull service [key={}] "
                        + "with {} pre-pulled tools, {} pre-queued tools and {} runtime tools", new Object[] { //
                contextKey, prePulledTools.size(), tools.size() + toolsInSet.size(), toolsRuntime.size() });
    }

    /**
     * 初始化parent pull service。
     */
    private void initParent() {
        if (parent != null || beanFactory == null || beanFactory.getParent() == null) {
            return;
        }

        // 取得parent pull service，依次尝试：
        // 1. 在配置文件中明确设置parentRef
        // 2. parent context中同名的对象
        // 3. parent context中默认名称的对象
        String parentBeanName = null;

        if (beanFactory.getParent().containsBean(getBeanName())) {
            parentBeanName = getBeanName();
        } else if (beanFactory.getParent().containsBean(DEFAULT_BEAN_NAME)) {
            parentBeanName = DEFAULT_BEAN_NAME;
        }

        if (parentBeanName != null) {
            parent = (PullService) beanFactory.getParent().getBean(parentBeanName);
        }
    }

    /**
     * 创建一个在整个JVM中不重复的contextKey。
     */
    private void initContextKey() {
        int i = contextKeyCounter.getAndIncrement();
        contextKey = "PullService." + getBeanName() + (i > 0 ? "." + i : EMPTY_STRING);
    }

    /**
     * 初始化所有tool factories。
     */
    private void initToolFactories() {
        tools = createHashMap();
        toolsInSet = createHashMap();
        toolsRuntime = createHashMap();
        prePulledTools = createHashMap();
        toolNames = createHashSet();

        if (toolFactories != null) {
            for (Map.Entry<String, Object> e : toolFactories.entrySet()) {
                String name = assertNotNull(trimToNull(e.getKey()), "tool name");
                Object factory = e.getValue();

                if (factory instanceof ToolNameAware) {
                    ((ToolNameAware) factory).setToolName(name);
                }

                int type = getFactoryType(factory);

                if (testBit(type, SINGLETON)) {
                    // 将singleton tool预先取值
                    if (testBit(type, TOOL_FACTORY)) {
                        Object tool;

                        try {
                            tool = encode(((ToolFactory) factory).createTool());
                        } catch (Exception ex) {
                            throw new PullException("Could not create tool: \"" + name + "\"", ex);
                        }

                        toolNames.add(name);
                        prePulledTools.put(name, tool);

                        if (getLogger().isDebugEnabled()) {
                            getLogger().debug("Pre-pulled tool: {} = {}", new Object[] { name, tool });
                        }
                    }

                    // 将singleton tool set预先取得每一个值
                    if (testBit(type, TOOL_SET_FACTORY)) {
                        Iterable<String> names = ((ToolSetFactory) factory).getToolNames();

                        if (names != null) {
                            for (String nameInSet : names) {
                                nameInSet = trimToNull(nameInSet);

                                if (nameInSet != null) {
                                    Object tool;

                                    try {
                                        tool = encode(((ToolSetFactory) factory).createTool(nameInSet));
                                    } catch (Exception ex) {
                                        throw new PullException("Could not create tool: \"" + name + "." + nameInSet
                                                + "\"", ex);
                                    }

                                    toolNames.add(nameInSet);
                                    prePulledTools.put(nameInSet, tool);

                                    if (getLogger().isDebugEnabled()) {
                                        getLogger().debug("Pre-pulled tool: {}.{} = {}",
                                                new Object[] { name, nameInSet, tool });
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // 将non-singleton tool，预先取得其名称。
                    if (testBit(type, TOOL_FACTORY)) {
                        toolNames.add(name);
                        tools.put(name, (ToolFactory) factory);

                        getLogger().debug("Pre-queued tool: {}", name);
                    }

                    // 将non-singleton tool set，预先取得每一个名称。
                    if (testBit(type, TOOL_SET_FACTORY)) {
                        Iterable<String> names = ((ToolSetFactory) factory).getToolNames();

                        if (names != null) {
                            for (String nameInSet : names) {
                                nameInSet = trimToNull(nameInSet);

                                if (nameInSet != null) {
                                    toolNames.add(nameInSet);
                                    toolsInSet.put(nameInSet, new ToolSetInfo<ToolSetFactory>(name,
                                            (ToolSetFactory) factory, null));

                                    getLogger().debug("Pre-queued tool: {}.{}", name, nameInSet);
                                }
                            }
                        }
                    }

                    // 保存runtime tool
                    if (testBit(type, RUNTIME_TOOL_SET_FACTORY)) {
                        toolsRuntime.put(name, (RuntimeToolSetFactory) factory);
                    }
                }
            }
        }

        toolFactories = null;
    }

    public PullContext getContext() {
        RequestAttributes attrs = null;

        // 从request中取得context，假如当前不是在web环境中，则创建一个新的context，
        // 从而确保在非web环境中也可以使用pull service。
        try {
            attrs = RequestContextHolder.currentRequestAttributes();
        } catch (IllegalStateException e) {
            getLogger().debug("Getting pull context in non-WEB environment: {}", e.getMessage());
        }

        PullContext context;

        if (attrs == null) {
            context = new PullContextImpl();
        } else {
            context = (PullContext) attrs.getAttribute(contextKey, SCOPE_REQUEST);

            if (context == null) {
                context = new PullContextImpl();
                attrs.setAttribute(contextKey, context, SCOPE_REQUEST);
            }
        }

        return context;
    }

    public Map<String, Object> getTools() {
        return getContext().getTools();
    }

    private static int getFactoryType(Object factory) {
        int type = 0;

        if (factory instanceof RuntimeToolSetFactory) {
            type |= RUNTIME_TOOL_SET_FACTORY;

            if (factory instanceof ToolFactory) {
                type |= TOOL_FACTORY;
            }

            return type;
        }

        if (factory instanceof ToolFactory) {
            type |= TOOL_FACTORY;

            if (((ToolFactory) factory).isSingleton()) {
                type |= SINGLETON;
            }
        }

        if (factory instanceof ToolSetFactory) {
            type |= TOOL_SET_FACTORY;

            if (((ToolSetFactory) factory).isSingleton()) {
                type |= SINGLETON;
            }
        }

        assertTrue(type != 0, "unknown pull tool factory type: %s", factory == null ? null : factory.getClass()
                .getName());

        return type;
    }

    private static boolean testBit(int type, int mask) {
        return (type & mask) != 0;
    }

    private static Object encode(Object value) {
        return value == null ? NULL_PLACEHOLDER : value;
    }

    private static Object decode(Object value) {
        return value == NULL_PLACEHOLDER ? null : value;
    }

    @Override
    public String toString() {
        if (isInitialized()) {
            ToStringBuilder sb = new ToStringBuilder().append("PullTools").append(
                    new CollectionBuilder().appendAll(toolNames).setPrintCount(true).setSort(true));

            if (parent != null) {
                sb.append("Parent ").append(parent);
            }

            return sb.toString();
        } else {
            return "PullTools[uninitialized]";
        }
    }

    static class ToolSetInfo<F> {
        private final String toolSetName;
        private final F factory;
        private final Object tool;

        public ToolSetInfo(String toolSetName, F factory, Object tool) {
            this.toolSetName = toolSetName;
            this.factory = factory;
            this.tool = tool;
        }

        public String getToolSetName() {
            return toolSetName;
        }

        public F getFactory() {
            return factory;
        }

        public Object getTool() {
            return tool;
        }
    }

    private class PullContextImpl implements PullContext {
        private final PullContext parentContext;
        private final Map<String, Object> pulledTools;
        private final Map<String, RuntimeToolSetFactory> toolsRuntime;
        private final Map<String, ToolSetInfo<RuntimeToolSetFactory>> toolsInRuntimeSet;
        private final Set<String> toolNames;
        private Set<String> toolNamesIncludingParent;
        private Map<String, Object> toolsIncludingParent;

        private PullContextImpl() {
            if (parent == null) {
                parentContext = null;
            } else {
                parentContext = parent.getContext();
            }

            pulledTools = createHashMap();
            toolsRuntime = createTreeMap(); // 排序以保证单元测试的确定性
            toolsInRuntimeSet = createHashMap();
            toolNames = createTreeSet(); // 排序，使getToolNames()方法返回的对象有序

            // copy runtime tools
            toolsRuntime.putAll(PullServiceImpl.this.toolsRuntime);

            // copy tool names and sort
            toolNames.addAll(PullServiceImpl.this.toolNames);
        }

        public Object pull(String name) {
            name = trimToNull(name);

            if (name == null) {
                return null;
            }

            Object tool;

            // 如果name已经被pre-pulled，则直接返回
            tool = prePulledTools.get(name);

            if (tool == null) {
                // 检查本地缓存，如果name早已存在，则直接返回
                tool = pulledTools.get(name);

                if (tool == null) {
                    tool = doPulling(name); // encoded tool

                    if (tool != null) {
                        pulledTools.put(name, tool);
                    }
                }
            }

            // 如果有parent context，则试着从parent中取得
            if (tool == null && parentContext != null) {
                return parentContext.pull(name);
            } else {
                return decode(tool);
            }
        }

        private Object doPulling(String name) {
            // 如果存在于tools中，则pull之。
            ToolFactory toolFactory = tools.get(name);

            if (toolFactory != null) {
                Object tool;

                try {
                    tool = toolFactory.createTool();
                } catch (Exception ex) {
                    throw new PullException("Could not create tool: \"" + name + "\"", ex);
                }

                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("Pulled tool: {} = {}", name, tool);
                }

                return encode(tool);
            }

            // 如果存在于toolsInSet中，则pull之。
            ToolSetInfo<ToolSetFactory> toolSetInfo = toolsInSet.get(name);

            if (toolSetInfo != null) {
                Object tool;

                try {
                    tool = toolSetInfo.getFactory().createTool(name);
                } catch (Exception ex) {
                    throw new PullException("Could not create tool: \"" + toolSetInfo.getToolSetName() + "." + name
                            + "\"", ex);
                }

                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("Pulled tool: {}.{} = {}",
                            new Object[] { toolSetInfo.getToolSetName(), name, tool });
                }

                return encode(tool);
            }

            // 如果存在于toolsInRuntimeSet中，则pull之。
            pullToolsRuntime(name);

            ToolSetInfo<RuntimeToolSetFactory> runtimeToolSetInfo = toolsInRuntimeSet.get(name);

            if (runtimeToolSetInfo != null) {
                Object tool;

                try {
                    tool = runtimeToolSetInfo.getFactory().createTool(runtimeToolSetInfo.getTool(), name);
                } catch (Exception ex) {
                    throw new PullException("Could not create tool: \"" + runtimeToolSetInfo.getToolSetName() + "."
                            + name + "\"", ex);
                }

                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("Pulled tool: {}.{} = {}",
                            new Object[] { runtimeToolSetInfo.getToolSetName(), name, tool });
                }

                return encode(tool);
            }

            return null;
        }

        private void pullToolsRuntime(String stopOnName) {
            if (stopOnName != null && toolsInRuntimeSet.containsKey(stopOnName)) {
                return;
            }

            for (Iterator<Map.Entry<String, RuntimeToolSetFactory>> i = toolsRuntime.entrySet().iterator(); i.hasNext();) {
                Map.Entry<String, RuntimeToolSetFactory> entry = i.next();

                i.remove();
                String toolSetName = entry.getKey();
                RuntimeToolSetFactory factory = entry.getValue();

                int count = 0;
                Object tool;

                try {
                    tool = factory.createToolSet();
                } catch (Exception ex) {
                    throw new PullException("Could not create runtime tool-set: \"" + toolSetName + "\"", ex);
                }

                Iterable<String> names = factory.getToolNames(tool);

                if (names != null) {
                    for (String nameInSet : names) {
                        if (nameInSet != null) {
                            toolsInRuntimeSet.put(nameInSet, new ToolSetInfo<RuntimeToolSetFactory>(toolSetName,
                                    factory, tool));
                            toolNames.add(nameInSet);
                            count++;
                        }
                    }
                }

                getLogger().debug("Queued {} tools for runtime tool-set \"{}\"", count, toolSetName);

                // 假如已经找到了stopOnName，则立即返回
                if (stopOnName != null && toolsInRuntimeSet.containsKey(stopOnName)) {
                    break;
                }
            }
        }

        public Set<String> getToolNames() {
            if (toolNamesIncludingParent == null) {
                toolNamesIncludingParent = unmodifiableSet(populateToolNames());
            }

            return toolNamesIncludingParent;
        }

        private Set<String> populateToolNames() {
            Set<String> toolNamesIncludingParent;

            pullToolsRuntime(null);

            if (parentContext == null) {
                toolNamesIncludingParent = toolNames;
            } else {
                toolNamesIncludingParent = createTreeSet();

                toolNamesIncludingParent.addAll(parentContext.getToolNames());
                toolNamesIncludingParent.addAll(toolNames);
            }

            return toolNamesIncludingParent;
        }

        public Map<String, Object> getTools() {
            if (toolsIncludingParent == null) {
                for (String name : tools.keySet()) {
                    pull(name);
                }

                for (String name : toolsInSet.keySet()) {
                    pull(name);
                }

                pullToolsRuntime(null);

                for (String name : toolsInRuntimeSet.keySet()) {
                    pull(name);
                }

                toolsIncludingParent = createHashMap();

                if (parentContext != null) {
                    toolsIncludingParent.putAll(parentContext.getTools());
                }

                putAll(toolsIncludingParent, pulledTools);
                putAll(toolsIncludingParent, prePulledTools);

                toolsIncludingParent = unmodifiableMap(toolsIncludingParent);
            }

            return toolsIncludingParent;
        }

        private void putAll(Map<String, Object> tools, Map<String, Object> objects) {
            for (Map.Entry<String, Object> entry : objects.entrySet()) {
                tools.put(entry.getKey(), decode(entry.getValue()));
            }
        }

        @Override
        public String toString() {
            MapBuilder mb = new MapBuilder();

            mb.append("prePulledTools", new MapBuilder().appendAll(prePulledTools).setSortKeys(true)
                    .setPrintCount(true));
            mb.append("pulledTools", new MapBuilder().appendAll(pulledTools).setSortKeys(true).setPrintCount(true));

            ToStringBuilder sb = new ToStringBuilder().append("PullContext").append(mb);

            if (parentContext != null) {
                sb.append("Parent ").append(parentContext);
            }

            return sb.toString();
        }
    }
}
