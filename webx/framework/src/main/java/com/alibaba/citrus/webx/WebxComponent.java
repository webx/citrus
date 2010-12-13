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

public interface WebxComponent {
    /**
     * 取得当前component所属的components集合。
     */
    WebxComponents getWebxComponents();

    /**
     * 取得所有component的名称。
     */
    String getName();

    /**
     * 取得指定component的component path。如果是默认component，则返回空字符串。
     */
    String getComponentPath();

    /**
     * 取得webx configuration设置。
     */
    WebxConfiguration getWebxConfiguration();

    /**
     * 取得用来处理当前component请求的controller。
     */
    WebxController getWebxController();

    /**
     * 取得当前component对应的application context容器。
     */
    WebApplicationContext getApplicationContext();
}
