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
package com.alibaba.citrus.service.mail.support;

import static com.alibaba.citrus.util.StringUtil.*;

import java.io.IOException;
import java.io.OutputStream;

import javax.activation.DataSource;

/**
 * 便于实现的<code>DataSource</code>基类。
 * 
 * @author Michael Zhou
 */
public abstract class AbstractDataSource implements DataSource {
    private String name;
    private String contentType;

    public AbstractDataSource() {
        this(null, null);
    }

    public AbstractDataSource(String name) {
        this(name, null);
    }

    public AbstractDataSource(String name, String contentType) {
        this.name = trimToNull(name);
        this.contentType = trimToNull(contentType);

        if (this.contentType == null) {
            this.contentType = "application/octet-stream";
        }
    }

    public String getName() {
        return name;
    }

    public String getContentType() {
        return contentType;
    }

    public OutputStream getOutputStream() throws IOException {
        return null;
    }
}
