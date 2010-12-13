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
package com.alibaba.citrus.webx.context;

import static com.alibaba.citrus.webx.WebxConstant.*;

import com.alibaba.citrus.service.resource.support.context.ResourceLoadingXmlWebApplicationContext;

/**
 * 用于webx框架的application context。
 * <ul>
 * <li>扩展了Spring的
 * {@link org.springframework.web.context.support.XmlWebApplicationContext}
 * ，添加了SpringExt的支持，包括configuration point以及resource loading扩展。</li>
 * <li>修改了默认的配置文件名：<code>/WEB-INF/webx-*.xml</code>。</li>
 * </ul>
 * 
 * @author Michael Zhou
 */
public class WebxApplicationContext extends ResourceLoadingXmlWebApplicationContext {
    /**
     * 取得默认的Spring配置文件名。
     * <ul>
     * <li>Root context默认配置文件为<code>/WEB-INF/webx.xml</code>；</li>
     * <li>假设component名称为<code>"test"</code> ，其默认的配置文件是
     * <code>/WEB-INF/webx-test.xml</code>。</li>
     * </ul>
     */
    @Override
    protected String[] getDefaultConfigLocations() {
        if (getNamespace() != null) {
            return new String[] { WEBX_COMPONENT_CONFIGURATION_LOCATION_PATTERN.replace("*", getNamespace()) };
        } else {
            return new String[] { WEBX_CONFIGURATION_LOCATION };
        }
    }
}
