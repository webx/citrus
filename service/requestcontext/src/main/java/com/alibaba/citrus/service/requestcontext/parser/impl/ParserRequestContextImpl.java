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

import static com.alibaba.citrus.util.Assert.*;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.PropertyEditorRegistrar;

import com.alibaba.citrus.service.requestcontext.RequestContext;
import com.alibaba.citrus.service.requestcontext.parser.CookieParser;
import com.alibaba.citrus.service.requestcontext.parser.ParameterParser;
import com.alibaba.citrus.service.requestcontext.parser.ParameterParserFilter;
import com.alibaba.citrus.service.requestcontext.parser.ParserRequestContext;
import com.alibaba.citrus.service.requestcontext.support.AbstractRequestContextWrapper;
import com.alibaba.citrus.service.requestcontext.support.AbstractRequestWrapper;
import com.alibaba.citrus.service.upload.UploadService;
import com.alibaba.citrus.util.StringUtil;

/**
 * 自动解析request parameters和cookie parameters，并透明地处理upload请求的request context实现。
 */
public class ParserRequestContextImpl extends AbstractRequestContextWrapper implements ParserRequestContext {
    private PropertyEditorRegistrar propertyEditorRegistrar;
    private boolean converterQuiet;
    private String caseFolding;
    private boolean autoUpload;
    private boolean unescapeParameters;
    private boolean useServletEngineParser;
    private boolean useBodyEncodingForURI;
    private String uriEncoding;
    private boolean trimming;
    private UploadService upload;
    private ParameterParser parameters;
    private ParameterParserFilter[] filters;
    private String htmlFieldSuffix;
    private CookieParser cookies;

    /**
     * 包装一个<code>RequestContext</code>对象。
     * 
     * @param wrappedContext 被包装的<code>RequestContext</code>
     */
    public ParserRequestContextImpl(RequestContext wrappedContext) {
        super(wrappedContext);
        setRequest(new RequestWrapper(wrappedContext.getRequest()));
    }

    /**
     * 取得用来转换参数类型的propertyEditor注册器。
     */
    public PropertyEditorRegistrar getPropertyEditorRegistrar() {
        return propertyEditorRegistrar;
    }

    /**
     * 设置用来转换参数类型的propertyEditor注册器。
     */
    public void setPropertyEditorRegistrar(PropertyEditorRegistrar propertyEditorRegistrar) {
        this.propertyEditorRegistrar = propertyEditorRegistrar;
    }

    /**
     * 类型转换出错时，是否不报错，而是返回默认值。
     */
    public boolean isConverterQuiet() {
        return converterQuiet;
    }

    /**
     * 设置类型转换出错时，是否不报错，而是返回默认值。
     */
    public void setConverterQuiet(boolean converterQuiet) {
        this.converterQuiet = converterQuiet;
    }

    /**
     * 是否自动执行Upload。
     */
    public boolean isAutoUpload() {
        return autoUpload;
    }

    /**
     * 是否自动执行Upload。
     */
    public void setAutoUpload(boolean autoUpload) {
        this.autoUpload = autoUpload;
    }

    /**
     * 按照指定的风格转换parameters和cookies的名称，默认为“小写加下划线”。
     */
    public String getCaseFolding() {
        return caseFolding;
    }

    /**
     * 按照指定的风格转换parameters和cookies的名称，默认为“小写加下划线”。
     */
    public void setCaseFolding(String folding) {
        this.caseFolding = folding;
    }

    /**
     * 是否对参数进行HTML entities解码，默认为<code>false</code>。
     */
    public boolean isUnescapeParameters() {
        return unescapeParameters;
    }

    /**
     * 是否对参数进行HTML entities解码，默认为<code>false</code>。
     */
    public void setUnescapeParameters(boolean unescapeParameters) {
        this.unescapeParameters = unescapeParameters;
    }

    /**
     * 是否使用servlet引擎的parser，默认为<code>false</code>。
     */
    public void setUseServletEngineParser(boolean useServletEngineParser) {
        this.useServletEngineParser = useServletEngineParser;
    }

    /**
     * 是否使用servlet引擎的parser，默认为<code>false</code>。
     */
    public boolean isUseServletEngineParser() {
        return useServletEngineParser;
    }

    /**
     * 是否以request.setCharacterEncoding所指定的编码来解析query，默认为<code>true</code>。
     */
    public boolean isUseBodyEncodingForURI() {
        return useBodyEncodingForURI;
    }

    /**
     * 是否以request.setCharacterEncoding所指定的编码来解析query，默认为<code>true</code>。
     */
    public void setUseBodyEncodingForURI(boolean useBodyEncodingForURI) {
        this.useBodyEncodingForURI = useBodyEncodingForURI;
    }

    /**
     * 当<code>useServletEngineParser==false</code>并且
     * <code>useBodyEncodingForURI=false</code>时，用该编码来解释GET请求的参数。
     */
    public String getURIEncoding() {
        return uriEncoding;
    }

    /**
     * 当<code>useServletEngineParser==false</code>并且
     * <code>useBodyEncodingForURI=false</code>时，用该编码来解释GET请求的参数。
     */
    public void setURIEncoding(String uriEncoding) {
        this.uriEncoding = uriEncoding;
    }

    /**
     * 是否对输入参数进行trimming。默认为<code>true</code>。
     */
    public boolean isTrimming() {
        return trimming;
    }

    /**
     * 是否对输入参数进行trimming。默认为<code>true</code>。
     */
    public void setTrimming(boolean trimming) {
        this.trimming = trimming;
    }

    /**
     * 设置upload service。
     * 
     * @param upload <code>UploadService</code>对象
     */
    public void setUploadService(UploadService upload) {
        this.upload = upload;
    }

    /**
     * 设置用于过滤参数的filters。
     */
    public void setParameterParserFilters(ParameterParserFilter[] filters) {
        this.filters = filters;
    }

    /**
     * 取得代表HTML字段的后缀。
     */
    public String getHtmlFieldSuffix() {
        return htmlFieldSuffix;
    }

    /**
     * 设置代表HTML字段的后缀。
     */
    public void setHtmlFieldSuffix(String htmlFieldSuffix) {
        this.htmlFieldSuffix = htmlFieldSuffix;
    }

    /**
     * 取得所有query参数。第一次执行此方法时，将会解析request，从中取得所有的参数。
     * 
     * @return <code>ParameterParser</code>实例
     */
    public ParameterParser getParameters() {
        if (parameters == null) {
            parameters = new ParameterParserImpl(this, upload, trimming, filters, htmlFieldSuffix);
        }

        return parameters;
    }

    /**
     * 取得所有cookie。第一次执行此方法时，将会解析request，从中取得所有cookies。
     * 
     * @return <code>CookieParser</code>实例
     */
    public CookieParser getCookies() {
        if (cookies == null) {
            cookies = new CookieParserImpl(this);
        }

        return cookies;
    }

    /**
     * 将指定的字符串根据<code>getCaseFolding()</code>的设置，转换成指定大小写形式。
     * 
     * @param str 要转换的字符串
     * @return 转换后的字符串
     */
    public String convertCase(String str) {
        String folding = getCaseFolding();

        str = StringUtil.trimToEmpty(str);

        if (URL_CASE_FOLDING_LOWER.equals(folding)) {
            str = StringUtil.toLowerCase(str);
        } else if (URL_CASE_FOLDING_LOWER_WITH_UNDERSCORES.equals(folding)) {
            str = StringUtil.toLowerCaseWithUnderscores(str);
        } else if (URL_CASE_FOLDING_UPPER.equals(folding)) {
            str = StringUtil.toUpperCase(str);
        } else if (URL_CASE_FOLDING_UPPER_WITH_UNDERSCORES.equals(folding)) {
            str = StringUtil.toUpperCaseWithUnderscores(str);
        }

        return str;
    }

    /**
     * 包装request。
     */
    private class RequestWrapper extends AbstractRequestWrapper {
        private final ParameterMap parameterMap = new ParameterMap();

        public RequestWrapper(HttpServletRequest request) {
            super(ParserRequestContextImpl.this, request);
        }

        @Override
        public String getParameter(String key) {
            return getParameters().getString(key);
        }

        @Override
        public Map<String, String[]> getParameterMap() {
            return parameterMap;
        }

        @Override
        public Enumeration<String> getParameterNames() {
            return new IteratorEnumeration<String>(getParameters().keySet().iterator());
        }

        @Override
        public String[] getParameterValues(String key) {
            return getParameters().getStrings(key);
        }
    }

    private class IteratorEnumeration<E> implements Enumeration<E> {
        private Iterator<E> iterator;

        public IteratorEnumeration(Iterator<E> iterator) {
            this.iterator = iterator;
        }

        public boolean hasMoreElements() {
            return iterator.hasNext();
        }

        public E nextElement() {
            return iterator.next();
        }
    }

    /**
     * 一个以ParameterParser为基础的map。
     */
    private class ParameterMap extends AbstractMap<String, String[]> {
        private final ParameterEntrySet entrySet = new ParameterEntrySet();

        @Override
        public boolean containsKey(Object key) {
            try {
                return getParameters().containsKey((String) key);
            } catch (ClassCastException e) {
                return false;
            }
        }

        @Override
        public String[] get(Object key) {
            try {
                return getParameters().getStrings((String) key);
            } catch (ClassCastException e) {
                return null;
            }
        }

        @Override
        public Set<java.util.Map.Entry<String, String[]>> entrySet() {
            return entrySet;
        }
    }

    private class ParameterEntrySet extends AbstractSet<Map.Entry<String, String[]>> {
        @Override
        public Iterator<Map.Entry<String, String[]>> iterator() {
            final Iterator<String> i = getParameters().keySet().iterator();

            return new Iterator<Map.Entry<String, String[]>>() {
                public boolean hasNext() {
                    return i.hasNext();
                }

                public Entry<String, String[]> next() {
                    return new ParameterEntry(i.next());
                }

                public void remove() {
                    unsupportedOperation();
                }
            };
        }

        @Override
        public int size() {
            return getParameters().size();
        }
    }

    private class ParameterEntry implements Map.Entry<String, String[]> {
        private final String key;

        private ParameterEntry(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }

        public String[] getValue() {
            return getParameters().getStrings(key);
        }

        public String[] setValue(String[] value) {
            unsupportedOperation();
            return null;
        }
    }
}
