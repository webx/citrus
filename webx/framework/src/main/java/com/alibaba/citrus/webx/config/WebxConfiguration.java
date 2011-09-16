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
package com.alibaba.citrus.webx.config;

import java.util.Map;

import com.alibaba.citrus.service.configuration.Configuration;
import com.alibaba.citrus.service.pipeline.Pipeline;
import com.alibaba.citrus.service.requestcontext.RequestContextChainingService;
import com.alibaba.citrus.webx.WebxController;
import com.alibaba.citrus.webx.WebxRootController;

/**
 * 代表一组webx的配置信息。
 * 
 * @author Michael Zhou
 */
public interface WebxConfiguration extends Configuration {
    /**
     * 内部链接URL的前缀。内部链接用来显示错误信息、开发者信息。
     */
    String getInternalPathPrefix();

    /**
     * Pipeline服务。
     */
    Pipeline getPipeline();

    /**
     * 用于异常处理的pipeline服务。
     * <p>
     * 可选。假如没有配置这个pipeline，在productionMode下，错误将被sendError，然后由servlet engine来处理。
     * </p>
     */
    Pipeline getExceptionPipeline();

    /**
     * Request contexts服务。
     */
    RequestContextChainingService getRequestContexts();

    /**
     * 取得一组关于components的配置。
     */
    ComponentsConfig getComponentsConfig();

    /**
     * 代表一组关于components的配置。
     */
    interface ComponentsConfig {
        Boolean isAutoDiscoverComponents();

        String getComponentConfigurationLocationPattern();

        Class<?> getDefaultControllerClass();

        Class<?> getRootControllerClass();

        String getDefaultComponent();

        Map<String, ComponentConfig> getComponents();

        WebxRootController getRootController();
    }

    interface ComponentConfig {
        String getName();

        String getPath();

        WebxController getController();
    }
}
