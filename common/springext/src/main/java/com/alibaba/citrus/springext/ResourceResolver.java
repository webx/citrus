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

package com.alibaba.citrus.springext;

import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.ClassUtil.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.core.io.InputStreamSource;

/**
 * 这个接口的目的是取得configuration point/contribution所需要的一切资源文件，例如定义文件、schema文件等。
 * 在不同的环境中，需要不同的实现。例如：
 * <ul>
 * <li>在应用程序的环境中，将从class loader来寻找和装载所有的资源文件。</li>
 * <li>在IDE开发环境中（IDE plugins），每个IDE有自己不同的装载资源的方法。
 * 每个IDE插件（例如Intellij IDE、Eclipse）需要实现自己的<code>ResourceResolver</code>来装载项目中的资源文件。</li>
 * </ul>
 * 默认的实现是用class loader来装载资源。
 *
 * @author Michael Zhou
 */
public abstract class ResourceResolver {
    /**
     * 取得指定位置的一个资源。
     * 如果资源不存在，则返回<code>null</code>。
     */
    @Nullable
    public abstract Resource getResource(@NotNull String location);

    /**
     * 找到所有符合名称的资源。支持同名资源的查找，例如，查找所有jar包中的/META-INF/spring.schemas文件。该方法还支持通配符。
     *
     * @throws IOException 如果在寻找过程中出错
     */
    @NotNull
    public abstract Resource[] getResources(@NotNull String locationPattern) throws IOException;

    /** 从<code>ResourceResolver</code>中读取所有指定名称的资源文件，将其中的<code>key=value</code>汇总成一个<code>Map</code>。 */
    @NotNull
    public final Map<String, String> loadAllProperties(String resourceName) throws IOException {
        assertNotNull(resourceName, "Resource name must not be null");

        Map<?, ?> props = new Properties();

        for (Resource resource : getResources(resourceName)) {
            InputStream is = null;

            try {
                is = resource.getInputStream();
                ((Properties) props).load(is);
            } finally {
                if (is != null) {
                    is.close();
                }
            }
        }

        @SuppressWarnings("unchecked")
        Map<String, String> result = (Map<String, String>) props;

        return result;
    }

    public static abstract class Resource implements InputStreamSource {
        public abstract String getName();

        public abstract InputStream getInputStream() throws IOException;

        @Override
        public final String toString() {
            String desc = getName();
            return getSimpleClassName(getClass()) + (desc == null ? "" : "[" + desc + "]");
        }
    }
}
