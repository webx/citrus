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
package com.alibaba.citrus.webx;

import org.springframework.web.context.WebApplicationContext;

import com.alibaba.citrus.webx.config.WebxConfiguration;

/**
 * 代表一组webx component的信息。
 * 
 * @author Michael Zhou
 */
public interface WebxComponents extends Iterable<WebxComponent> {
    /**
     * 取得所有components名称。
     */
    String[] getComponentNames();

    /**
     * 取得指定名称的component。
     */
    WebxComponent getComponent(String componentName);

    /**
     * 取得默认的component。如果未设置，则返回<code>null</code>。
     */
    WebxComponent getDefaultComponent();

    /**
     * 查找匹配的component。
     */
    WebxComponent findMatchedComponent(String path);

    /**
     * 取得用来处理请求的controller。
     */
    WebxRootController getWebxRootController();

    /**
     * 取得webx configuration设置。
     */
    WebxConfiguration getParentWebxConfiguration();

    /**
     * 取得所有component的父application context容器，如果没有，则返回<code>null</code>。
     */
    WebApplicationContext getParentApplicationContext();
}
