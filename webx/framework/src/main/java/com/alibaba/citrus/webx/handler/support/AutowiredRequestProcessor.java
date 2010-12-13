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
package com.alibaba.citrus.webx.handler.support;

import static com.alibaba.citrus.util.StringUtil.*;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.alibaba.citrus.util.internal.webpagelite.PageComponent;
import com.alibaba.citrus.util.internal.webpagelite.RequestProcessor;
import com.alibaba.citrus.webx.handler.RequestHandler;
import com.alibaba.citrus.webx.handler.RequestHandlerContext;
import com.alibaba.citrus.webx.handler.RequestHandlerNameAware;

/**
 * 为所有的components提供注入spring beans的功能。
 * 
 * @author Michael Zhou
 */
public abstract class AutowiredRequestProcessor extends RequestProcessor<RequestHandlerContext> implements
        RequestHandler, RequestHandlerNameAware, ApplicationContextAware, InitializingBean {
    private ApplicationContext context;
    private String handlerName;

    public ApplicationContext getApplicationContext() {
        return context;
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }

    public final void afterPropertiesSet() throws Exception {
        // 对所有的component进行spring注入。
        for (String path : getComponentPaths()) {
            PageComponent pc = getComponent(path, PageComponent.class);

            getApplicationContext().getAutowireCapableBeanFactory().autowireBeanProperties(pc,
                    AbstractBeanDefinition.AUTOWIRE_NO, false);

            getApplicationContext().getAutowireCapableBeanFactory().initializeBean(pc, path);
        }

        init();
    }

    protected void init() throws Exception {
    }

    public String getName() {
        return handlerName;
    }

    public void setName(String handlerName) {
        this.handlerName = handlerName;
    }

    public void handleRequest(RequestHandlerContext context) throws Exception {
        processRequest(context);
    }

    @Override
    protected boolean resourceExists(String resourceName) {
        return isEmpty(resourceName); // 对于单页面的processor，只接受自身，即resourceName为空
    }
}
