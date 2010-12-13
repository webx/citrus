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
 * 这个服务提供了一个渲染模板的通用接口，通过这个服务。一个特定的模板系统，例如Velocity或JSP，只 需要实现
 * <code>TemplateEngine</code>接口，并向template service登记自己，就可以被统一的方法来调用。
 * 
 * @author Michael Zhou
 */
public interface TemplateService {
    /**
     * 取得所有被登记的模板名后缀。
     */
    String[] getSupportedExtensions();

    /**
     * 取得指定模板名后缀对应的模板引擎。
     */
    TemplateEngine getTemplateEngine(String extension);

    /**
     * 此方法代理到相应的模板引擎，并判定模板是否存在。
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
