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
import static com.alibaba.citrus.util.BasicConstant.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.fileupload.FileItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.citrus.service.requestcontext.parser.AbstractValueParser;
import com.alibaba.citrus.service.requestcontext.parser.ParameterParser;
import com.alibaba.citrus.service.requestcontext.parser.ParameterParserFilter;
import com.alibaba.citrus.service.requestcontext.parser.ParameterValueFilter;
import com.alibaba.citrus.service.requestcontext.parser.ParserRequestContext;
import com.alibaba.citrus.service.requestcontext.parser.UploadedFileFilter;
import com.alibaba.citrus.service.requestcontext.util.QueryStringParser;
import com.alibaba.citrus.service.requestcontext.util.ValueList;
import com.alibaba.citrus.service.upload.UploadException;
import com.alibaba.citrus.service.upload.UploadParameters;
import com.alibaba.citrus.service.upload.UploadService;
import com.alibaba.citrus.service.upload.UploadSizeLimitExceededException;
import com.alibaba.citrus.util.StringEscapeUtil;
import com.alibaba.citrus.util.StringUtil;

/**
 * 用来解析HTTP请求中GET或POST的参数的接口<code>ParameterParser</code>的默认实现。
 */
public class ParameterParserImpl extends AbstractValueParser implements ParameterParser {
    private final static Logger log = LoggerFactory.getLogger(ParameterParser.class);
    private final UploadService upload;
    private final boolean trimming;
    private boolean uploadProcessed;
    private final ParameterParserFilter[] filters;
    private final String htmlFieldSuffix;

    /**
     * 从request中创建新的parameters，如果是multipart-form，则自动解析之。
     */
    public ParameterParserImpl(ParserRequestContext requestContext, UploadService upload, boolean trimming,
                               ParameterParserFilter[] filters, String htmlFieldSuffix) {
        super(requestContext);

        this.upload = upload;
        this.trimming = trimming;
        this.filters = filters;
        this.htmlFieldSuffix = htmlFieldSuffix;

        HttpServletRequestWrapper wrapper = (HttpServletRequestWrapper) requestContext.getRequest();
        HttpServletRequest wrappedRequest = (HttpServletRequest) wrapper.getRequest();
        boolean isMultipart = false;

        // 自动upload
        if (requestContext.isAutoUpload() && upload != null) {
            // 如果是multipart/*请求，则调用upload service来解析。
            isMultipart = upload.isMultipartContent(wrappedRequest);

            if (isMultipart) {
                try {
                    parseUpload();
                } catch (UploadSizeLimitExceededException e) {
                    add(ParserRequestContext.UPLOAD_FAILED, Boolean.TRUE);
                    add(ParserRequestContext.UPLOAD_SIZE_LIMIT_EXCEEDED, Boolean.TRUE);
                    log.warn("File upload exceeds the size limit", e);
                } catch (UploadException e) {
                    add(ParserRequestContext.UPLOAD_FAILED, Boolean.TRUE);
                    log.warn("Upload failed", e);
                }
            }
        }

        // 从request中取参数
        if (!isMultipart) {
            String method = wrappedRequest.getMethod();

            // 按照标准，URL中只能出现US-ASCII字符，假如有其它类型的字符，必须对其进行URL编码。
            // 很不幸，这个编码也没有统一的标准。客户端和服务端必须达成某个共识。
            //
            // 对于客户端，有以下几种情况：
            // 1. 浏览器所提交的表单，均以当前页面的字符集编码。
            // 例如，一个GBK编码的页面所提交的表单，是以GBK编码的，无论其method为GET还是POST。
            //
            // 2. 直接输入在浏览器地址栏里的URL，根据浏览器的设置和操作系统的设置来确定编码。
            // 例如，中文Windows中，无论ie还是firefox，经试验默认都是GBK。
            // 而在mac系统中，无论safari还是firefox，经试验默认都是UTF-8。
            //
            // 对于服务端，有以下几种情况：
            // 1. Tomcat总是以server.xml中，以<Connector URIEncoding="xxx">中指定的编码，来解释GET请求的参数。如未指定，就是8859_1。
            // 2. Jetty总是以UTF-8来解码GET请求的参数。
            // 3. 对于POST请求，则以request.setCharacterEncoding("xxx")的编码为准，如未指定，就是8859_1。
            // 4. 如果设置了Tomcat5参数：<Connector useBodyEncodingForURI="true">，那么GET请求也以request.setCharacterEncoding("xxx")的编码为准。
            //
            // 可见如果不加任何设置，Tomcat/Jetty总是以8859_1或UTF-8来解码URL query，导致解码错误。
            //
            // 为了使应用对服务器的配置依赖较少，对所有非POST/PUT请求（一般是GET请求）进行手工解码，而不依赖于servlet engine的解码机制，
            // 除非你设置了useServletEngineParser=true。
            if (requestContext.isUseServletEngineParser() || "post".equalsIgnoreCase(method)
                    || "put".equalsIgnoreCase(method)) {
                // 用servlet engine来解析参数
                @SuppressWarnings("unchecked")
                Map<String, String[]> parameters = wrappedRequest.getParameterMap();

                if (parameters != null && parameters.size() > 0) {
                    for (Map.Entry<String, String[]> entry : parameters.entrySet()) {
                        String key = entry.getKey();
                        String[] values = entry.getValue();

                        for (String value : values) {
                            add(key, value);
                        }
                    }
                }
            } else {
                // 自己解析query string。
                //
                // 当useBodyEncodingForURI=true时，用request.setCharacterEncoding()所指定的值来解码，否则使用URIEncoding，默认为UTF-8。
                // useBodyEncodingForURI默认值就是true。
                // 该行为和tomcat的风格一致。（不过tomcat默认是8859_1，这个没关系）
                String charset = requestContext.isUseBodyEncodingForURI() ? wrappedRequest.getCharacterEncoding()
                        : requestContext.getURIEncoding();

                QueryStringParser parser = new QueryStringParser(charset, DEFAULT_CHARSET_ENCODING) {
                    @Override
                    protected void add(String key, String value) {
                        ParameterParserImpl.this.add(key, value);
                    }
                };

                parser.parse(wrappedRequest.getQueryString());
            }

            postProcessParams();
        }
    }

    @Override
    protected Logger getLogger() {
        return log;
    }

    /**
     * 处理所有参数。
     * <p>
     * 如果参数名为.~html结尾的，则按HTML规则处理，否则按普通规则处理。
     * </p>
     */
    private void postProcessParams() {
        HttpServletRequestWrapper wrapper = (HttpServletRequestWrapper) requestContext.getRequest();
        HttpServletRequest wrappedRequest = (HttpServletRequest) wrapper.getRequest();
        boolean[] filtering = null;

        if (!isEmptyArray(filters)) {
            filtering = new boolean[filters.length];

            for (int i = 0; i < filters.length; i++) {
                filtering[i] = filters[i].isFiltering(wrappedRequest);
            }
        }

        String[] keys = getKeys();
        List<String> keysToRemove = createLinkedList();

        for (String key : keys) {
            if (key.endsWith(htmlFieldSuffix)) {
                keysToRemove.add(key);
                key = key.substring(0, key.length() - htmlFieldSuffix.length());

                if (!containsKey(key)) {
                    setObjects(key, processValues(key, true, filtering));
                }

                continue;
            }

            boolean isHtml = !StringUtil.isBlank(getString(key + htmlFieldSuffix));
            setObjects(key, processValues(key, isHtml, filtering));
        }

        for (String key : keysToRemove) {
            remove(key);
        }
    }

    private Object[] processValues(String key, boolean isHtmlField, boolean[] filtering) {
        Object[] values = getObjects(key);

        for (int i = 0; i < values.length; i++) {
            Object value = values[i];

            if (value instanceof String) {
                // 将非HTML字段的&#12345;转换成unicode。
                if (!isHtmlField && requestContext.isUnescapeParameters()) {
                    value = StringEscapeUtil.unescapeEntities(null, (String) value);
                }

                // 过滤字符串值
                if (filtering != null) {
                    for (int j = 0; j < filters.length; j++) {
                        ParameterParserFilter filter = filters[j];

                        if (filter instanceof ParameterValueFilter && filtering[j]) {
                            value = ((ParameterValueFilter) filter).filter(key, (String) value, isHtmlField);
                        }
                    }
                }
            } else if (value instanceof FileItem) {
                // 过滤上传文件
                if (filtering != null) {
                    for (int j = 0; j < filters.length; j++) {
                        ParameterParserFilter filter = filters[j];

                        if (filter instanceof UploadedFileFilter && filtering[j]) {
                            value = ((UploadedFileFilter) filter).filter(key, (FileItem) value);
                        }
                    }
                }
            }

            values[i] = value;
        }

        return values;
    }

    /**
     * 取得指定名称的<code>FileItem</code>对象，如果不存在，则返回<code>null</code>。
     * 
     * @param key 参数名
     * @return <code>FileItem</code>对象
     */
    public FileItem getFileItem(String key) {
        ValueList container = getValueList(key, false);

        return container == null ? null : container.getFileItem();
    }

    /**
     * 取得指定名称的<code>FileItem</code>对象，如果不存在，则返回<code>null</code>。
     * 
     * @param key 参数名
     * @return <code>FileItem</code>对象的数组
     */
    public FileItem[] getFileItems(String key) {
        ValueList container = getValueList(key, false);

        return container == null ? new FileItem[0] : container.getFileItems();
    }

    /**
     * 添加<code>FileItem</code>。
     * 
     * @param key 参数名
     * @param value 参数值
     */
    public void add(String key, FileItem value) {
        if (value.isFormField()) {
            add(key, value.getString());
        } else {
            // 忽略空的上传项。
            if (!StringUtil.isEmpty(value.getName()) || value.getSize() > 0) {
                add(key, (Object) value);
            }
        }
    }

    /**
     * 添加参数名/参数值。
     * 
     * @param key 参数名
     * @param value 参数值
     */
    @Override
    public void add(String key, Object value) {
        if (value == null) {
            value = EMPTY_STRING;
        }

        if (trimming && value instanceof String) {
            value = trimToEmpty((String) value);
        }

        getValueList(key, true).addValue(value);
    }

    /**
     * 解析符合<a href="http://www.ietf.org/rfc/rfc1867.txt">RFC 1867</a>标准的
     * <code>multipart/form-data</code>类型的HTTP请求。
     * <p>
     * 要执行此方法，须将<code>UploadService.automatic</code>配置参数设置成<code>false</code>。
     * 此方法覆盖了service的默认设置，适合于在action或servlet中手工执行。
     * </p>
     * 
     * @throws UploadException 如果解析时出错
     */
    public void parseUpload() throws UploadException {
        parseUpload(null);
    }

    /**
     * 解析符合<a href="http://www.ietf.org/rfc/rfc1867.txt">RFC 1867</a>标准的
     * <code>multipart/form-data</code>类型的HTTP请求。
     * <p>
     * 要执行此方法，须将<code>UploadService.automatic</code>配置参数设置成<code>false</code>。
     * 此方法覆盖了service的默认设置，适合于在action或servlet中手工执行。
     * </p>
     * 
     * @param sizeThreshold 文件放在内存中的阈值，小于此值的文件被保存在内存中。如果此值小于0，则使用预设的值
     * @param sizeMax HTTP请求的最大尺寸，超过此尺寸的请求将被抛弃。
     * @param repositoryPath 暂存上载文件的绝对路径
     * @throws UploadException 如果解析时出错
     */
    public void parseUpload(UploadParameters params) throws UploadException {
        if (uploadProcessed || upload == null) {
            return;
        }

        FileItem[] items = upload.parseRequest(requestContext.getRequest(), params);

        for (FileItem item : items) {
            add(item.getFieldName(), item);
        }

        uploadProcessed = true;

        postProcessParams();
    }

    /**
     * 取得用于解析参数的编码字符集。不同的实现取得编码字符集的方法也不同，例如，对于<code>ParameterParser</code>，
     * 此编码字符集是由<code>request.getCharacterEncoding()</code>决定的。
     * <p>
     * 如果未指定，默认返回<code>ISO-8859-1</code>。
     * </p>
     * 
     * @return 编码字符集
     */
    @Override
    protected String getCharacterEncoding() {
        String charset = requestContext.getRequest().getCharacterEncoding();

        return charset == null ? ParserRequestContext.DEFAULT_CHARSET_ENCODING : charset;
    }

    /**
     * 将parameters重新组装成query string。
     * 
     * @return query string，如果没有参数，则返回<code>null</code>
     */
    public String toQueryString() {
        QueryStringParser parser = new QueryStringParser();

        for (Object element : keySet()) {
            String key = (String) element;
            Object[] values = getObjects(key);

            if (isEmptyArray(values)) {
                continue;
            }

            for (Object valueObject : values) {
                if (valueObject == null || valueObject instanceof String) {
                    parser.append(key, (String) valueObject);
                }
            }
        }

        return parser.toQueryString();
    }
}
