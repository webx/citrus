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

import static com.alibaba.citrus.util.Assert.*;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.core.io.Resource;

/**
 * 从Spring resource中取得数据的<code>DataSource</code>实现。
 * 
 * @author Michael Zhou
 */
public class ResourceDataSource extends AbstractDataSource {
    private final Resource resource;

    public ResourceDataSource(Resource resource) {
        this(resource, null, null);
    }

    public ResourceDataSource(Resource resource, String name) {
        this(resource, name, null);
    }

    public ResourceDataSource(Resource resource, String name, String contentType) {
        super(name, contentType);
        this.resource = assertNotNull(resource, "resource");
    }

    public Resource getResource() {
        return resource;
    }

    @Override
    public String getName() {
        String name = super.getName();

        if (name == null) {
            try {
                name = resource.getURL().getPath();
            } catch (IOException e) {
            }
        }

        return name;
    }

    public InputStream getInputStream() throws IOException {
        return resource.getInputStream();
    }

    @Override
    public String toString() {
        return resource.toString();
    }
}
