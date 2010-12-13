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
package com.alibaba.citrus.service.freemarker.impl;

import static com.alibaba.citrus.service.freemarker.FreeMarkerConfiguration.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Locale;

import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ResourceLoader;

import com.alibaba.citrus.service.AbstractService;
import com.alibaba.citrus.service.configuration.ProductionModeAware;
import com.alibaba.citrus.service.freemarker.FreeMarkerEngine;
import com.alibaba.citrus.service.freemarker.impl.log.LoggerHacker;
import com.alibaba.citrus.service.template.TemplateContext;
import com.alibaba.citrus.service.template.TemplateException;
import com.alibaba.citrus.util.i18n.LocaleUtil;

import freemarker.core.Environment;
import freemarker.core.ParseException;
import freemarker.template.Template;

/**
 * FreeMarker模板引擎。
 * 
 * @author Michael Zhou
 */
public class FreeMarkerEngineImpl extends AbstractService<FreeMarkerEngine> implements FreeMarkerEngine,
        ResourceLoaderAware, ProductionModeAware {
    private final FreeMarkerConfigurationImpl configuration = new FreeMarkerConfigurationImpl(getLogger());

    // 初始化slf4j日志。
    static {
        String prefix = FreeMarkerEngine.class.getName();
        int index = prefix.indexOf("freemarker");

        if (index < 0) {
            index = prefix.length();
        }

        prefix = prefix.substring(0, index);

        LoggerHacker.hackLogger(prefix);
    }

    public FreeMarkerConfigurationImpl getConfiguration() {
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
    protected void init() {
        configuration.init();

        getLogger().debug("FreeMarker Engine Configurations: {}", configuration);
    }

    /**
     * 取得默认的模板名后缀列表。
     * <p>
     * 当<code>TemplateService</code>没有指定到当前engine的mapping时，将取得本方法所返回的后缀名列表。
     * </p>
     */
    public String[] getDefaultExtensions() {
        return new String[] { "ftl" };
    }

    /**
     * 判定模板是否存在。
     */
    public boolean exists(String templateName) {
        try {
            return configuration.getTemplateLoader().findTemplateSource(templateName) != null;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 渲染模板，并以字符串的形式取得渲染的结果。
     */
    public String getText(String templateName, TemplateContext context) throws TemplateException, IOException {
        StringWriter out = new StringWriter();
        render(templateName, context, out, null, null, null);
        return out.toString();
    }

    /**
     * 渲染模板，并将渲染的结果送到字节输出流中。
     */
    public void writeTo(String templateName, TemplateContext context, OutputStream ostream) throws TemplateException,
            IOException {
        render(templateName, context, null, ostream, null, null);
    }

    /**
     * 渲染模板，并将渲染的结果送到字符输出流中。
     */
    public void writeTo(String templateName, TemplateContext context, Writer writer) throws TemplateException,
            IOException {
        render(templateName, context, writer, null, null, null);
    }

    public String mergeTemplate(String templateName, Object context, String inputCharset) throws TemplateException,
            IOException {
        StringWriter out = new StringWriter();
        render(templateName, context, out, null, inputCharset, null);
        return out.toString();
    }

    public void mergeTemplate(String templateName, Object context, OutputStream ostream, String inputCharset,
                              String outputCharset) throws TemplateException, IOException {
        render(templateName, context, null, ostream, inputCharset, outputCharset);
    }

    public void mergeTemplate(String templateName, Object context, Writer out, String inputCharset)
            throws TemplateException, IOException {
        render(templateName, context, out, null, inputCharset, null);
    }

    /**
     * 渲染模板到指定输出流。
     */
    private void render(String templateName, Object context, Writer writer, OutputStream ostream, String inputCharset,
                        String outputCharset) throws TemplateException, IOException {
        Locale locale = LocaleUtil.getContext().getLocale();

        // inputCharset依次为：指定值、engine初始化时的默认值、DEFAULT_CHARSET
        if (isEmpty(inputCharset)) {
            inputCharset = configuration.getConfiguration().getDefaultEncoding();
        }

        inputCharset = defaultIfEmpty(inputCharset, DEFAULT_CHARSET);

        // outputCharset依次为：指定值、engine初始化时的默认值、DEFAULT_CHARSET
        if (isEmpty(outputCharset)) {
            outputCharset = configuration.getConfiguration().getOutputEncoding();
        }

        outputCharset = defaultIfEmpty(outputCharset, DEFAULT_CHARSET);

        if (writer == null) {
            if (ostream == null) {
                throw new IllegalArgumentException("missing output writer");
            }

            writer = new OutputStreamWriter(ostream, outputCharset);
        }

        try {
            Template template = configuration.getConfiguration().getTemplate(templateName, locale, inputCharset);
            Environment env = template.createProcessingEnvironment(context, writer);

            env.setLocale(locale);
            env.setOutputEncoding(outputCharset);

            env.process();
        } catch (freemarker.template.TemplateException e) {
            error(templateName, e);
        } catch (ParseException e) {
            error(templateName, e);
        }
    }

    private void error(String templateName, Throwable e) {
        throw new TemplateException("Error rendering FreeMarker template: " + templateName, e);
    }
}
