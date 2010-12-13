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
package com.alibaba.citrus.service.velocity.impl;

import static com.alibaba.citrus.service.velocity.VelocityConfiguration.*;
import static com.alibaba.citrus.service.velocity.impl.SpringResourceLoaderAdapter.*;
import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.ObjectUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;
import static org.apache.velocity.runtime.RuntimeConstants.*;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import org.apache.velocity.Template;
import org.apache.velocity.app.event.EventCartridge;
import org.apache.velocity.app.event.ReferenceInsertionEventHandler;
import org.apache.velocity.context.AbstractContext;
import org.apache.velocity.context.Context;
import org.apache.velocity.context.InternalEventContext;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.util.RuntimeServicesAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ResourceLoader;

import com.alibaba.citrus.service.AbstractService;
import com.alibaba.citrus.service.configuration.ProductionModeAware;
import com.alibaba.citrus.service.template.TemplateContext;
import com.alibaba.citrus.service.template.TemplateException;
import com.alibaba.citrus.service.template.TemplateNotFoundException;
import com.alibaba.citrus.service.velocity.VelocityEngine;

/**
 * Velocity模板引擎服务。
 * 
 * @author Michael Zhou
 */
public class VelocityEngineImpl extends AbstractService<VelocityEngine> implements VelocityEngine, ResourceLoaderAware,
        ProductionModeAware {
    private final static String RUNTIME_SERVICES_KEY = "_runtime_services";
    private final VelocityRuntimeInstance ri = new VelocityRuntimeInstance();
    private final VelocityConfigurationImpl configuration = new VelocityConfigurationImpl(getLogger());

    public RuntimeServices getRuntimeServices() {
        return ri;
    }

    public VelocityConfigurationImpl getConfiguration() {
        return configuration;
    }

    public void setResourceLoader(ResourceLoader loader) {
        configuration.setResourceLoader(loader);
    }

    public void setProductionMode(boolean productionMode) {
        configuration.setProductionMode(productionMode);
    }

    /**
     * 初始化engine。
     */
    @Override
    protected void init() throws Exception {
        configuration.init();

        getLogger().debug("Velocity Engine Configurations: {}", configuration);

        ri.setConfiguration(configuration.getProperties());
        ri.setApplicationAttribute(SPRING_RESOURCE_LOADER_KEY, configuration.getResourceLoader());
        ri.setProperty(EVENTHANDLER_REFERENCEINSERTION, RuntimeServicesExposer.class.getName());
        ri.init();

        // 初始化EventCartridge，以后不用再初始化了，以确保性能。
        CloneableEventCartridge eventCartridge = configuration.getEventCartridge();
        RuntimeServices rs = assertNotNull((RuntimeServices) ri.getProperty(RUNTIME_SERVICES_KEY), "RuntimeServices");

        eventCartridge.initOnce(rs);
    }

    /**
     * 取得默认的模板名后缀列表。
     * <p>
     * 当<code>TemplateService</code>没有指定到当前engine的mapping时，将取得本方法所返回的后缀名列表。
     * </p>
     */
    public String[] getDefaultExtensions() {
        return new String[] { "vm" };
    }

    /**
     * 判定模板是否存在。
     */
    public boolean exists(String templateName) {
        return ri.getLoaderNameForResource(templateName) != null;
    }

    /**
     * 渲染模板，并以字符串的形式取得渲染的结果。
     */
    public String getText(String templateName, TemplateContext context) throws TemplateException, IOException {
        return mergeTemplate(templateName, new TemplateContextAdapter(context), null);
    }

    /**
     * 渲染模板，并将渲染的结果送到字节输出流中。
     */
    public void writeTo(String templateName, TemplateContext context, OutputStream ostream) throws TemplateException,
            IOException {
        mergeTemplate(templateName, new TemplateContextAdapter(context), ostream, null, null);
    }

    /**
     * 渲染模板，并将渲染的结果送到字符输出流中。
     */
    public void writeTo(String templateName, TemplateContext context, Writer writer) throws TemplateException,
            IOException {
        mergeTemplate(templateName, new TemplateContextAdapter(context), writer, null);
    }

    /**
     * 渲染模板，并以字符串的形式取得渲染的结果。
     */
    public String mergeTemplate(String templateName, Context context, String inputEncoding) throws TemplateException,
            IOException {
        StringWriter writer = new StringWriter();
        mergeTemplate(templateName, context, writer, inputEncoding);
        return writer.toString();
    }

    /**
     * 渲染模板，并将渲染的结果送到字节输出流中。
     */
    public void mergeTemplate(String templateName, Context context, OutputStream ostream, String inputEncoding,
                              String outputEncoding) throws TemplateException, IOException {
        if (isEmpty(outputEncoding)) {
            outputEncoding = getDefaultOutputEncoding();
        }

        OutputStreamWriter writer = null;

        try {
            writer = new OutputStreamWriter(ostream, outputEncoding);
        } catch (UnsupportedEncodingException e) {
            error(templateName, e);
        }

        mergeTemplate(templateName, context, writer, inputEncoding);
        writer.flush(); // 确保内容被刷新到stream中。
    }

    /**
     * 渲染模板，并将渲染的结果送到字符输出流中。
     */
    public void mergeTemplate(String templateName, Context context, Writer writer, String inputEncoding)
            throws TemplateException, IOException {
        if (isEmpty(inputEncoding)) {
            inputEncoding = getDefaultInputEncoding();
        }

        try {
            Context eventContext = attachEventCartridge(context);

            mergeTemplate(templateName, inputEncoding, eventContext, writer);
        } catch (Exception e) {
            error(templateName, e);
        }
    }

    /**
     * Copied from org.apache.velocity.app.VelocityEngine。
     */
    private boolean mergeTemplate(String templateName, String encoding, Context context, Writer writer)
            throws ResourceNotFoundException, ParseErrorException, MethodInvocationException, Exception {
        Template template = ri.getTemplate(templateName, encoding);

        if (template == null) {
            String msg = "VelocityEngine.mergeTemplate() was unable to load template '" + templateName + "'";
            ri.getLog().error(msg);
            throw new ResourceNotFoundException(msg);
        } else {
            template.merge(context, writer);
            return true;
        }
    }

    private Context attachEventCartridge(Context context) {
        Context eventContext;

        if (context instanceof InternalEventContext) {
            eventContext = context;
        } else {
            // 将其包装成EventContext，确保event cartridge可以工作。
            // 模板中的修改将保留在context中。
            eventContext = new EventContext(context);
        }

        // 将event cartridge复制以后（如有必要）附到context中。
        EventCartridge ec = configuration.getEventCartridge().getRuntimeInstance();

        if (ec != null) {
            assertTrue(ec.attachToContext(eventContext), "Could not attach EventCartridge to velocity context");
        }

        return eventContext;
    }

    // 以下两个为性能优化
    private String defaultInputEncoding;
    private String defaultOutpuEncoding;

    /**
     * 取得解析模板时的默认编码字符集。
     */
    protected String getDefaultInputEncoding() {
        if (defaultInputEncoding == null) {
            defaultInputEncoding = defaultIfNull(trimToNull((String) ri.getProperty(INPUT_ENCODING)), DEFAULT_CHARSET);
        }

        return defaultInputEncoding;
    }

    /**
     * 取得输出模板时的默认编码字符集。
     */
    protected String getDefaultOutputEncoding() {
        if (defaultOutpuEncoding == null) {
            defaultOutpuEncoding = defaultIfNull(trimToNull((String) ri.getProperty(OUTPUT_ENCODING)), DEFAULT_CHARSET);
        }

        return defaultOutpuEncoding;
    }

    /**
     * 处理异常，显示额外的信息。
     */
    private final void error(String templateName, Throwable e) throws TemplateException {
        String err = "Error rendering Velocity template: " + templateName;

        getLogger().error(err + ": " + e.getMessage());

        if (e instanceof ResourceNotFoundException) {
            throw new TemplateNotFoundException(err, e);
        }

        if (e instanceof TemplateException) {
            throw (TemplateException) e;
        }

        throw new TemplateException(err, e);
    }

    /**
     * 一个hack，用来取得runtime services实例。
     */
    public static class RuntimeServicesExposer implements ReferenceInsertionEventHandler, RuntimeServicesAware {
        public Object referenceInsert(String reference, Object value) {
            return value;
        }

        public void setRuntimeServices(RuntimeServices rs) {
            rs.getConfiguration().setProperty(RUNTIME_SERVICES_KEY, rs);
        }
    }

    /**
     * 包装任意<code>Context</code>，使之支持EventCartridge。
     */
    private static class EventContext extends AbstractContext {
        private final Context context;

        public EventContext(Context context) {
            this.context = assertNotNull(context, "no context");
        }

        @Override
        public Object internalGet(String key) {
            return context.get(key);
        }

        @Override
        public Object internalPut(String key, Object value) {
            return context.put(key, value);
        }

        @Override
        public boolean internalContainsKey(Object key) {
            return context.containsKey(key);
        }

        @Override
        public Object[] internalGetKeys() {
            return context.getKeys();
        }

        @Override
        public Object internalRemove(Object key) {
            return context.remove(key);
        }

        @Override
        public String toString() {
            return context.toString();
        }
    }
}
