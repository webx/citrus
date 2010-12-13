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
package com.alibaba.citrus.springext;

import java.io.IOException;
import java.io.InputStream;

import org.dom4j.Document;

public interface Schema {
    String XML_SCHEMA_EXTENSION = "xsd";

    String getName();

    String getVersion();

    String getTargetNamespace();

    String getPreferredNsPrefix();

    String[] getIncludes();

    String[] getElements();

    String getNamespacePrefix();

    String getSourceDescription();

    InputStream getInputStream() throws IOException;

    /**
     * 取得dom文档。
     * <p>
     * 假如文档读取失败，则返回<code>null</code>
     * ，但会打印警告日志。这样是为了避免因一个schema的错误，导致所有schema均不能装入。
     * </p>
     */
    Document getDocument();

    String getText() throws IOException;

    String getText(String charset) throws IOException;

    String getText(String charset, DocumentFilter filter) throws IOException;
}
