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

import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.FileUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

import org.apache.commons.collections.ExtendedProperties;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

/**
 * 让velocity模板系统使用spring resource loader装载模板的适配器.
 * 
 * @author Michael Zhou
 */
public class SpringResourceLoaderAdapter extends AbstractResourceLoader {
    public static final String SPRING_RESOURCE_LOADER_KEY = "_spring_resource_loader";
    private String path;
    private ResourceLoader springLoader;

    /**
     * 初始化resource loader.
     */
    @Override
    public void init(ExtendedProperties configuration) {
        rsvc.getLog().info(getLogID() + " : initialization starting.");

        springLoader = assertNotNull((ResourceLoader) rsvc.getApplicationAttribute(SPRING_RESOURCE_LOADER_KEY),
                SPRING_RESOURCE_LOADER_KEY);

        path = normalizeAbsolutePath(configuration.getString("path"), true);

        assertTrue(!isEmpty(path), "path");

        path += "/";

        rsvc.getLog().info(getLogID() + " : set path '" + path + "'");
        rsvc.getLog().info(getLogID() + " : initialization complete.");
    }

    /**
     * 取得资源。
     */
    @Override
    protected Resource getResource(String templateName) {
        return springLoader.getResource(path + normalizeTemplateName(templateName));
    }

    /**
     * 取得用于日志记录的ID。
     */
    @Override
    protected String getLogID() {
        return getClass().getSimpleName();
    }

    @Override
    protected String getDesc() {
        return path;
    }
}
