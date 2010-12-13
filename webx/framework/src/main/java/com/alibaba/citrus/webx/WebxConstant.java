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

import org.springframework.web.servlet.FrameworkServlet;

/**
 * Webx相关常量。
 * 
 * @author Michael Zhou
 */
public final class WebxConstant {
    public final static String WEBX_CONFIGURATION_LOCATION = "/WEB-INF/webx.xml";
    public final static String WEBX_COMPONENT_CONFIGURATION_LOCATION_PATTERN = "/WEB-INF/webx-*.xml";

    /**
     * 用于在servlet context中保存component context的attribute
     * key前缀，兼容FrameworkServlet。
     */
    public final static String COMPONENT_CONTEXT_PREFIX = FrameworkServlet.SERVLET_CONTEXT_PREFIX;
}
