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
package com.alibaba.citrus.service.velocity.impl;

import static com.alibaba.citrus.util.StringUtil.*;

import java.io.IOException;
import java.io.InputStream;

import org.apache.velocity.exception.ResourceNotFoundException;
import org.springframework.core.io.Resource;

/**
 * Velocity <code>ResourceLoader</code>的抽象实现。
 * 
 * @author Michael Zhou
 */
public abstract class AbstractResourceLoader extends org.apache.velocity.runtime.resource.loader.ResourceLoader {
    /**
     * 取得输入流。
     */
    @Override
    public final InputStream getResourceStream(String templateName) throws ResourceNotFoundException {
        Resource resource = getResource(templateName);
        Exception exception = null;

        if (resource != null && resource.exists()) {
            try {
                return resource.getInputStream();
            } catch (IOException e) {
                exception = e;
            }
        }

        throw new ResourceNotFoundException(getLogID() + " Error: could not find template: " + templateName, exception);
    }

    /**
     * 判断资源是否被改变。
     */
    @Override
    public final boolean isSourceModified(org.apache.velocity.runtime.resource.Resource templateResource) {
        Resource resource = getResource(templateResource.getName());

        // 1. 假如资源没找到，可能是被删除了，那么认为modified==true，模板将会在重新装载时报错。
        if (resource == null || !resource.exists()) {
            return true;
        }

        long lastModified;

        try {
            lastModified = resource.lastModified();
        } catch (IOException e) {
            lastModified = 0;
        }

        // 2. 假如资源找到了，但是不支持lastModified功能，则认为modified==false，模板不会重新装载。
        if (lastModified <= 0L) {
            return false;
        }

        // 3. 资源找到，并支持lastModified功能，则比较lastModified。
        return lastModified != templateResource.getLastModified();
    }

    /**
     * 取得最近被修改的时间。
     */
    @Override
    public final long getLastModified(org.apache.velocity.runtime.resource.Resource templateResource) {
        Resource resource = getResource(templateResource.getName());

        if (resource != null && resource.exists()) {
            try {
                return resource.lastModified();
            } catch (IOException e) {
            }
        }

        return 0;
    }

    /**
     * 规格化模板名。
     */
    protected final String normalizeTemplateName(String templateName) {
        if (isEmpty(templateName)) {
            throw new ResourceNotFoundException("Need to specify a template name!");
        }

        if (templateName.startsWith("/")) {
            templateName = templateName.substring(1);
        }

        return templateName;
    }

    /**
     * 取得资源。
     */
    protected abstract Resource getResource(String templateName);

    /**
     * 优化判断逻辑，尽量避免取得resource stream。
     */
    @Override
    public boolean resourceExists(String resourceName) {
        Resource resource = getResource(resourceName);
        return resource != null && resource.exists();
    }

    /**
     * 取得用于日志记录的ID。
     */
    protected abstract String getLogID();

    protected abstract String getDesc();

    @Override
    public String toString() {
        return getLogID() + "[" + getDesc() + "]";
    }
}
