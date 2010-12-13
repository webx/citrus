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
package com.alibaba.citrus.service.requestcontext.parser;

import org.springframework.beans.PropertyEditorRegistrar;

import com.alibaba.citrus.service.requestcontext.RequestContext;

/**
 * 自动解析request parameters和cookie parameters，并透明地处理upload请求的request context实现。
 * 
 * @author Michael Zhou
 */
public interface ParserRequestContext extends RequestContext {
    /** 配置文件属性可选项：不对parameters和cookies的名称进行大小写转换。 */
    String URL_CASE_FOLDING_NONE = "none";

    /** 配置文件属性可选项：将parameters和cookies的名称转换成小写。 */
    String URL_CASE_FOLDING_LOWER = "lower";

    /** 配置文件属性可选项：将parameters和cookies的名称转换成小写加下划线。 */
    String URL_CASE_FOLDING_LOWER_WITH_UNDERSCORES = "lower_with_underscores";

    /** 配置文件属性可选项：将parameters和cookies的名称转换成大写。 */
    String URL_CASE_FOLDING_UPPER = "upper";

    /** 配置文件属性可选项：将parameters和cookies的名称转换成大写加下划线。 */
    String URL_CASE_FOLDING_UPPER_WITH_UNDERSCORES = "upper_with_underscores";

    /** 默认的编码字符集。 */
    String DEFAULT_CHARSET_ENCODING = "ISO-8859-1";

    /** 在parameters中表示upload失败，请求被忽略。 */
    String UPLOAD_FAILED = "upload_failed";

    /** 在parameters中表示upload文件尺寸超过限制值，请求被忽略。 */
    String UPLOAD_SIZE_LIMIT_EXCEEDED = "upload_size_limit_exceeded";

    /**
     * 取得用来转换参数类型的propertyEditor注册器。
     */
    PropertyEditorRegistrar getPropertyEditorRegistrar();

    /**
     * 类型转换出错时，是否不报错，而是返回默认值。
     */
    boolean isConverterQuiet();

    /**
     * 是否自动执行Upload。
     */
    boolean isAutoUpload();

    /**
     * 取得代表HTML字段的后缀。
     */
    String getHtmlFieldSuffix();

    /**
     * 按照指定的风格转换parameters和cookies的名称，默认为“小写加下划线”。
     */
    String getCaseFolding();

    /**
     * 是否对参数进行HTML entities解码，默认为<code>true</code>。
     */
    boolean isUnescapeParameters();

    /**
     * 是否使用servlet引擎的parser，默认为<code>false</code>。
     */
    boolean isUseServletEngineParser();

    /**
     * 是否以request.setCharacterEncoding所指定的编码来解析query，默认为<code>true</code>。
     * <p>
     * 只有当<code>useServletEngineParser==false</code>时，此选项才有效。
     * </p>
     */
    boolean isUseBodyEncodingForURI();

    /**
     * 当<code>useServletEngineParser==false</code>并且
     * <code>useBodyEncodingForURI=false</code>时，用该编码来解释GET请求的参数。
     */
    String getURIEncoding();

    /**
     * 是否对输入参数进行trimming。默认为<code>true</code>。
     */
    boolean isTrimming();

    /**
     * 取得所有query参数。第一次执行此方法时，将会解析request，从中取得所有的参数。
     * 
     * @return <code>ParameterParser</code>实例
     */
    ParameterParser getParameters();

    /**
     * 取得所有cookie。第一次执行此方法时，将会解析request，从中取得所有cookies。
     * 
     * @return <code>CookieParser</code>实例
     */
    CookieParser getCookies();

    /**
     * 将指定的字符串根据<code>getCaseFolding()</code>的设置，转换成指定大小写形式。
     * 
     * @param str 要转换的字符串
     * @return 转换后的字符串
     */
    String convertCase(String str);
}
