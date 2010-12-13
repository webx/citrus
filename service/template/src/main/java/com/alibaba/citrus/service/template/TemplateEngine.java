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
package com.alibaba.citrus.service.template;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

/**
 * 代表一个template引擎的服务。例如：Velocity、JSP等。
 * 
 * @author Michael Zhou
 */
public interface TemplateEngine {
    /**
     * 取得默认的模板名后缀列表。
     * <p>
     * 当<code>TemplateService</code>没有指定到当前engine的mapping时，将取得本方法所返回的后缀名列表。
     * </p>
     */
    String[] getDefaultExtensions();

    /**
     * 判定模板是否存在。
     */
    boolean exists(String templateName);

    /**
     * 渲染模板，并以字符串的形式取得渲染的结果。
     */
    String getText(String templateName, TemplateContext context) throws TemplateException, IOException;

    /**
     * 渲染模板，并将渲染的结果送到字节输出流中。
     */
    void writeTo(String templateName, TemplateContext context, OutputStream ostream) throws TemplateException,
            IOException;

    /**
     * 渲染模板，并将渲染的结果送到字符输出流中。
     */
    void writeTo(String templateName, TemplateContext context, Writer writer) throws TemplateException, IOException;
}
