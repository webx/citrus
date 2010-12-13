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

import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.Assert.ExceptionType.*;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import com.alibaba.citrus.webx.WebxComponents;

/**
 * 用来初始化<code>WebxComponents</code>。
 * 
 * @author Michael Zhou
 */
public class WebxComponentsContext extends WebxApplicationContext {
    private WebxComponentsLoader componentsLoader;

    public WebxComponentsLoader getLoader() {
        return assertNotNull(componentsLoader, ILLEGAL_STATE, "no WebxComponentsLoader set");
    }

    public void setLoader(WebxComponentsLoader loader) {
        this.componentsLoader = loader;
    }

    /**
     * 取得所有的components。
     */
    public WebxComponents getWebxComponents() {
        return getLoader().getWebxComponents();
    }

    @Override
    protected void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
        super.postProcessBeanFactory(beanFactory);
        getLoader().postProcessBeanFactory(beanFactory);
    }

    @Override
    protected void finishRefresh() {
        super.finishRefresh();
        getLoader().finishRefresh();
    }
}
