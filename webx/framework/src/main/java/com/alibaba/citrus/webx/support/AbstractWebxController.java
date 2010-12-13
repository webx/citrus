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
package com.alibaba.citrus.webx.support;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;

import com.alibaba.citrus.webx.WebxComponent;
import com.alibaba.citrus.webx.WebxController;
import com.alibaba.citrus.webx.config.WebxConfiguration;

public abstract class AbstractWebxController implements WebxController {
    protected final Logger log = LoggerFactory.getLogger(getClass());
    private WebxComponent component;

    public WebxComponent getComponent() {
        return component;
    }

    public WebxConfiguration getWebxConfiguration() {
        return getComponent().getWebxConfiguration();
    }

    public ServletContext getServletContext() {
        return getComponent().getApplicationContext().getServletContext();
    }

    /**
     * 此方法在创建controller时被调用。
     */
    public void init(WebxComponent component) {
        this.component = component;
    }

    /**
     * 此方法在创建或刷新WebApplicationContext时被调用。
     */
    public void onRefreshContext() throws BeansException {
        initWebxConfiguration();
    }

    private void initWebxConfiguration() {
        WebxConfiguration webxConfiguration = getWebxConfiguration();

        log.debug("Initializing WebxComponent \"{}\" in {} mode, according to <webx-configuration>", getComponent()
                .getName(), webxConfiguration.isProductionMode() ? "production" : "development");
    }

    public void onFinishedProcessContext() {
    }
}
