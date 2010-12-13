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
package com.alibaba.citrus.service.requestcontext.locale;

import com.alibaba.citrus.service.requestcontext.RequestContext;

/**
 * 实现了Servlet 2.4规范中的response的方法。包括：
 * <ul>
 * <li>response.<code>setCharacterEncoding()</code>方法，使之可以方便地设置输出字符编码，而不需要依赖于
 * <code>setContentType()</code>方法。</li>
 * <li>response.<code>getContentType()</code>方法，使之可以取得当前输出的content type。</li>
 * </ul>
 * 设置区域和编码字符集。包括：
 * <ul>
 * <li><code>LocaleUtil.setContextLocale()</code></li>
 * <li><code>request.setCharacterEncoding()</code></li>
 * <li><code>response.setLocale()</code></li>
 * <li><code>response.setCharacterEncoding()</code>。</li>
 * </ul>
 * 
 * @author Michael Zhou
 */
public interface SetLocaleRequestContext extends RequestContext {
    String INPUT_CHARSET_PARAM_DEFAULT = "_input_charset";
    String OUTPUT_CHARSET_PARAM_DEFAULT = "_output_charset";

    /** 用来保存locale的session key的名称。 */
    String SESSION_KEY_DEFAULT = "_lang";

    /** 用来设置locale的parameter key的名称。 */
    String PARAMETER_KEY_DEFAULT = "_lang";
    String PARAMETER_SET_TO_DEFAULT_VALUE = "default";

    /** 默认的locale。 */
    String LOCALE_DEFAULT = "en_US";

    /** 默认的charset。 */
    String CHARSET_DEFAULT = "UTF-8";

    /**
     * 取得content type。
     * 
     * @return content type，包括charset的定义
     */
    String getResponseContentType();

    /**
     * 设置content type。 如果content type不包含charset，并且
     * <code>getCharacterEncoding</code>被设置，则加上charset标记。
     * <p>
     * 如果<code>appendCharset</code>为<code>false</code>，则content
     * type中将不包含charset标记。
     * </p>
     * 
     * @param contentType content type
     * @param appendCharset 输出字符集
     */
    void setResponseContentType(String contentType, boolean appendCharset);

    /**
     * 设置response输出字符集。注意，此方法必须在第一次<code>getWriter</code>之前执行。
     * 
     * @param charset 输出字符集，如果charset为<code>null</code>
     *            ，则从contentType中删除charset标记
     */
    void setResponseCharacterEncoding(String charset);
}
