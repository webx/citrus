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
package com.alibaba.citrus.service.freemarker;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import com.alibaba.citrus.service.template.TemplateEngine;
import com.alibaba.citrus.service.template.TemplateException;

/**
 * FreeMarker模板引擎。
 * 
 * @author Michael Zhou
 */
public interface FreeMarkerEngine extends TemplateEngine {
    /**
     * 渲染模板，并以字符串的形式取得渲染的结果。
     */
    String mergeTemplate(String templateName, Object context, String inputCharset) throws TemplateException,
            IOException;

    /**
     * 渲染模板，并将渲染的结果送到字节输出流中。
     */
    void mergeTemplate(String templateName, Object context, OutputStream ostream, String inputCharset,
                       String outputCharset) throws TemplateException, IOException;

    /**
     * 渲染模板，并将渲染的结果送到字符输出流中。
     */
    void mergeTemplate(String templateName, Object context, Writer out, String inputCharset) throws TemplateException,
            IOException;
}
