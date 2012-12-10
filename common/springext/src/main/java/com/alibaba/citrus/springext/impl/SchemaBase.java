/*
 * Copyright (c) 2002-2012 Alibaba Group Holding Limited.
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
 */

package com.alibaba.citrus.springext.impl;

import static com.alibaba.citrus.springext.support.SchemaUtil.*;
import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.CollectionUtil.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;

import com.alibaba.citrus.springext.ConfigurationPointException;
import com.alibaba.citrus.springext.Schema;
import com.alibaba.citrus.util.io.ByteArrayInputStream;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamSource;

/**
 * 这是schema的基类，实现了schema生命周期中的几个步骤：
 * <ol>
 * <li>Parse - 将originalSource读出来，生成document文档。这一步可以被跳过，例如configuration point schema文档是直接在内存中创建的，所以不经过这一步。</li>
 * <li>Transform - 将document文档进行转换、修改。这一步可进行多次，且真正的转换会被延迟到要用到时，才进行。</li>
 * <li>Analyze - 分析document的内容，取得其中的信息。例如取得targetNamespace等。</li>
 * <li>Serialize - 将document转换回二进制流，以供外部程序读取。例如，供给schema exporter生成schema文件，或者供给resolver来验证SpringExt配置文件。
 * 每一次transform以后，需要重新进行serialize操作，以便反映最新的修改。</li>
 * </ol>
 *
 * @author Michael Zhou
 */
public abstract class SchemaBase implements Schema {
    protected final static Logger log = LoggerFactory.getLogger(Schema.class);
    private final InputStreamSource originalSource;

    private Document document;
    private boolean  parsed;

    private final LinkedList<Transformer> transformersQueue = createLinkedList();
    private byte[]  transformedData;
    private boolean everTransformed;

    private boolean analyzed;

    public SchemaBase(InputStreamSource originalSource) {
        this(originalSource, null, true);
    }

    public SchemaBase(Document originalDocument) {
        this(null, originalDocument, false);
    }

    protected SchemaBase(InputStreamSource originalSource, Document originalDocument, boolean isInputStreamSource) {
        if (isInputStreamSource) {
            this.originalSource = assertNotNull(originalSource, "no InputStreamSource provided");
        } else {
            this.originalSource = null;
            this.document = assertNotNull(originalDocument, "no Document provided");
            this.parsed = true;
        }
    }

    public final void transform(Transformer transformer) {
        transform(transformer, false);
    }

    public final void transform(Transformer transformer, boolean doNow) {
        transformersQueue.addLast(assertNotNull(transformer, "no Transformer provided"));

        if (doNow) {
            transform();
        }
    }

    public final Document getDocument() {
        parse();
        transform();
        return document;
    }

    public final InputStream getInputStream() {
        serialize();

        if (transformedData != null) {
            return new ByteArrayInputStream(transformedData);
        }

        if (originalSource != null) {
            return getOriginalInputStream();
        } else {
            fail("Found a BUG: OriginalSource can't be null here");
            return null;
        }
    }

    private void serialize() {
        transform();

        if (transformedData == null && document != null && (everTransformed || originalSource == null)) {
            transformedData = getDocumentContent(document);
        }
    }

    private void transform() {
        if (!transformersQueue.isEmpty()) {
            parse();

            transformedData = null;
            everTransformed = true;

            while (!transformersQueue.isEmpty()) {
                Transformer transformer = transformersQueue.removeFirst();

                // 有可能因格式非法，不能生成document文件（document==null）。忽略这种情况。
                if (document != null) {
                    transformer.transform(document, getName());
                }
            }
        }
    }

    private void parse() {
        if (!parsed) {
            parsed = true;

            try {
                document = readDocument(getOriginalInputStream(), getName(), true);
            } catch (DocumentException e) {
                log.warn("Not a valid XML doc: {}, source={},\n{}", new Object[] { getName(), originalSource, e.getMessage() });
                document = null;
            }
        }
    }

    private InputStream getOriginalInputStream() {
        try {
            return originalSource.getInputStream();
        } catch (IOException e) {
            throw new ConfigurationPointException("Failed to read text of schema file: " + getName() + ", source=" + originalSource, e);
        }
    }

    protected final void analyze() {
        if (!analyzed) {
            analyzed = true;

            parse();
            transform();

            if (document != null) {
                doAnalyze();
            }
        }
    }

    protected abstract void doAnalyze();

    @Override
    public String toString() {
        if (originalSource != null) {
            return originalSource.toString();
        } else {
            return "generated-content";
        }
    }
}
