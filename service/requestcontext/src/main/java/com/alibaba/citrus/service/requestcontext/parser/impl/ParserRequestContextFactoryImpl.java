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
package com.alibaba.citrus.service.requestcontext.parser.impl;

import static com.alibaba.citrus.service.requestcontext.parser.ParserRequestContext.*;
import static com.alibaba.citrus.util.ArrayUtil.*;
import static com.alibaba.citrus.util.ObjectUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

import org.springframework.beans.PropertyEditorRegistrar;

import com.alibaba.citrus.service.configuration.support.PropertyEditorRegistrarsSupport;
import com.alibaba.citrus.service.requestcontext.RequestContext;
import com.alibaba.citrus.service.requestcontext.parser.ParameterParserFilter;
import com.alibaba.citrus.service.requestcontext.parser.ParserRequestContext;
import com.alibaba.citrus.service.requestcontext.support.AbstractRequestContextFactory;
import com.alibaba.citrus.service.upload.UploadService;
import com.alibaba.citrus.util.internal.ToStringBuilder.MapBuilder;

/**
 * 创建<code>ParserRequestContext</code>的工厂。
 * 
 * @author Michael Zhou
 */
public class ParserRequestContextFactoryImpl extends AbstractRequestContextFactory<ParserRequestContext> {
    private final static boolean CONVERTER_QUIET_DEFAULT = true;
    private final static String URL_CASE_FOLDING_DEFAULT = URL_CASE_FOLDING_LOWER_WITH_UNDERSCORES;
    private final static boolean AUTO_UPLOAD_DEFAULT = true;
    private final static boolean UNESCAPE_PARAMETERS_DEFAULT = true;
    private final static boolean USE_SERVLET_ENGINE_PARSER_DEFAULT = false;
    private final static boolean USE_BODY_ENCODING_FOR_URI_DEFAULT = true;
    private final static String URI_ENCODING_DEFAULT = "UTF-8";
    private final static boolean TRIMMING_DEFAULT = true;
    private final static String HTML_FIELD_SUFFIX_DEFAULT = ".~html";

    private PropertyEditorRegistrarsSupport propertyEditorRegistrars = new PropertyEditorRegistrarsSupport();
    private Boolean converterQuiet;
    private String caseFolding;
    private Boolean autoUpload;
    private Boolean unescapeParameters;
    private Boolean useServletEngineParser;
    private Boolean useBodyEncodingForURI;
    private String uriEncoding;
    private Boolean trimming;
    private ParameterParserFilter[] filters;
    private String htmlFieldSuffix;
    private UploadService uploadService;

    public void setPropertyEditorRegistrars(PropertyEditorRegistrar[] registrars) {
        propertyEditorRegistrars.setPropertyEditorRegistrars(registrars);
    }

    public void setConverterQuiet(boolean converterQuiet) {
        this.converterQuiet = converterQuiet;
    }

    public void setCaseFolding(String caseFolding) {
        this.caseFolding = caseFolding;
    }

    public void setAutoUpload(boolean autoUpload) {
        this.autoUpload = autoUpload;
    }

    public void setUnescapeParameters(boolean unescapeParameters) {
        this.unescapeParameters = unescapeParameters;
    }

    public void setUseServletEngineParser(boolean useServletEngineParser) {
        this.useServletEngineParser = useServletEngineParser;
    }

    public void setUseBodyEncodingForURI(boolean useBodyEncodingForURI) {
        this.useBodyEncodingForURI = useBodyEncodingForURI;
    }

    public void setURIEncoding(String uriEncoding) {
        this.uriEncoding = uriEncoding;
    }

    public void setTrimming(boolean trimming) {
        this.trimming = trimming;
    }

    public void setParameterParserFilters(ParameterParserFilter[] filters) {
        this.filters = filters;
    }

    public void setHtmlFieldSuffix(String htmlFieldSuffix) {
        this.htmlFieldSuffix = htmlFieldSuffix;
    }

    public void setUploadService(UploadService uploadService) {
        this.uploadService = uploadService;
    }

    @Override
    protected void init() {
        // 类型转换出错时，是否不报错，而是返回默认值
        converterQuiet = defaultIfNull(converterQuiet, CONVERTER_QUIET_DEFAULT);

        // 参数和cookies名称的大小写转换选项
        caseFolding = defaultIfEmpty(caseFolding, URL_CASE_FOLDING_DEFAULT).toLowerCase();

        // 是否自动处理上传文件
        autoUpload = defaultIfNull(autoUpload, AUTO_UPLOAD_DEFAULT);

        // 是否对参数进行HTML entities解码，默认为true
        unescapeParameters = defaultIfNull(unescapeParameters, UNESCAPE_PARAMETERS_DEFAULT);

        // 是否让servlet engine来解析GET参数
        useServletEngineParser = defaultIfNull(useServletEngineParser, USE_SERVLET_ENGINE_PARSER_DEFAULT);

        // 是否以request.setCharacterEncoding所指定的编码来解析query
        useBodyEncodingForURI = defaultIfNull(useBodyEncodingForURI, USE_BODY_ENCODING_FOR_URI_DEFAULT);

        // 如果不以request.setCharacterEncoding所指定的编码来解析query，那么就用这个
        uriEncoding = defaultIfNull(uriEncoding, URI_ENCODING_DEFAULT);

        // 是否对参数值进行trimming
        trimming = defaultIfNull(trimming, TRIMMING_DEFAULT);

        // HTML类型的字段名后缀
        htmlFieldSuffix = defaultIfEmpty(htmlFieldSuffix, HTML_FIELD_SUFFIX_DEFAULT);
    }

    /**
     * 包装一个request context。
     * 
     * @param wrappedContext 被包装的<code>RequestContext</code>对象
     * @return request context
     */
    public ParserRequestContext getRequestContextWrapper(RequestContext wrappedContext) {
        ParserRequestContextImpl requestContext = new ParserRequestContextImpl(wrappedContext);

        requestContext.setPropertyEditorRegistrar(propertyEditorRegistrars);
        requestContext.setConverterQuiet(converterQuiet);
        requestContext.setAutoUpload(autoUpload);
        requestContext.setCaseFolding(caseFolding);
        requestContext.setUnescapeParameters(unescapeParameters);
        requestContext.setUseServletEngineParser(useServletEngineParser);
        requestContext.setUseBodyEncodingForURI(useBodyEncodingForURI);
        requestContext.setURIEncoding(uriEncoding);
        requestContext.setTrimming(trimming);

        if (autoUpload) {
            requestContext.setUploadService(uploadService);
        }

        if (!isEmptyArray(filters)) {
            requestContext.setParameterParserFilters(filters);
        }

        requestContext.setHtmlFieldSuffix(htmlFieldSuffix);

        return requestContext;
    }

    /**
     * 本类提供了解析参数和cookie的功能。
     */
    public String[] getFeatures() {
        return new String[] { "parseRequest" };
    }

    /**
     * 本类不依赖其它features。
     */
    public FeatureOrder[] featureOrders() {
        return null;
    }

    @Override
    protected Object dumpConfiguration() {
        MapBuilder mb = new MapBuilder();

        mb.append("Converter quiet", converterQuiet);
        mb.append("Case folding", caseFolding);
        mb.append("Auto upload", autoUpload);
        mb.append("Unescape HTML entities", unescapeParameters);
        mb.append("Use servlet engine's parser", useServletEngineParser);
        mb.append("Use body encoding for URI", useBodyEncodingForURI);
        mb.append("URI encoding if not use body encoding for URI", uriEncoding);
        mb.append("Trimming", trimming);
        mb.append("HTML field suffix", htmlFieldSuffix);
        mb.append("Upload Service", uploadService);
        mb.append("Parameter Parser Filters", filters);

        return mb;
    }
}
