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

import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.ContextLoaderListener;

/**
 * 用来启动root context的listener。
 * <p>
 * 和Spring {@link ContextLoaderListener}类似，listener将读取
 * <code>/WEB-INF/web.xml</code>中context param <code>contextClass</code>
 * 所指定的类名，作为root <code>ApplicationContext</code>的实现类。假如未明确指定，则使用默认值
 * {@link WebxApplicationContext}。
 * </p>
 * <p>
 * 默认值可以通过覆盖<code>getDefaultContextClass()</code>来改变。
 * </p>
 * 
 * @author Michael Zhou
 */
public class WebxContextLoaderListener extends ContextLoaderListener {
    @Override
    protected final ContextLoader createContextLoader() {
        return new WebxComponentsLoader() {

            @Override
            protected Class<?> getDefaultContextClass() {
                Class<?> defaultContextClass = WebxContextLoaderListener.this.getDefaultContextClass();

                if (defaultContextClass == null) {
                    defaultContextClass = super.getDefaultContextClass();
                }

                return defaultContextClass;
            }
        };
    }

    protected Class<?> getDefaultContextClass() {
        return null;
    }
}
