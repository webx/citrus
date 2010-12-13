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
package com.alibaba.citrus.springext.support;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * 一个通用基类，方便实现contribution类。包含了以下特性：
 * <ul>
 * <li>子类可覆盖<code>resolveBeanInterface()</code>方法，以取得接口。</li>
 * <li>实现默认的<code>toString()</code>方法。</li>
 * <li>实现了spring的初始化、销毁等生命期方法。</li>
 * <li>可取得在spring中注册的bean name。</li>
 * </ul>
 * 
 * @author Michael Zhou
 */
public abstract class BeanSupport implements InitializingBean, DisposableBean, BeanNameAware {
    private Class<?> beanInterface;
    private String beanName;
    private boolean initialized;

    /**
     * 取得bean接口。
     */
    public final Class<?> getBeanInterface() {
        if (beanInterface == null) {
            beanInterface = resolveBeanInterface();
        }

        return beanInterface;
    }

    protected Class<?> resolveBeanInterface() {
        return getClass();
    }

    /**
     * 是否已经初始化。
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * 检查服务已经被初始化，若否，则抛出<code>IllegalStateException</code>异常。
     */
    public void assertInitialized() {
        if (!initialized) {
            throw new IllegalStateException(String.format("Bean instance of %s has not been initialized yet.",
                    getBeanInterface().getName()));
        }
    }

    /**
     * 初始化前执行。
     */
    protected void preInit() throws Exception {
    }

    /**
     * 初始化bean。
     */
    protected void init() throws Exception {
    }

    /**
     * 初始化后执行。
     */
    protected void postInit() throws Exception {
    }

    public final void afterPropertiesSet() throws Exception {
        preInit();
        init();
        initialized = true;
        postInit();
    }

    /**
     * 销毁bean。
     */
    protected void dispose() {
    }

    /**
     * 销毁前执行。
     */
    protected void preDispose() {
    }

    /**
     * 销毁后执行。
     */
    protected void postDispose() {
    }

    public final void destroy() {
        preDispose();
        dispose();
        initialized = false;
        postDispose();
    }

    /**
     * 取得spring容器中的bean名称，仅用于调试。
     */
    public String getBeanName() {
        return beanName;
    }

    /**
     * 设置spring容器中的bean名称，仅用于调试。
     */
    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    /**
     * 转换成字符串。
     */
    @Override
    public String toString() {
        return getBeanDescription();
    }

    /**
     * 取得bean的描述：<code>beanName + ":" + beanInterfaceName</code>。
     */
    protected String getBeanDescription() {
        return getBeanDescription(true);
    }

    /**
     * 取得bean的描述：<code>beanName + ":" + beanInterfaceName</code>。
     */
    protected String getBeanDescription(boolean simpleName) {
        String interfaceDesc = simpleName ? getBeanInterface().getSimpleName() : getBeanInterface().getName();
        return beanName == null || beanName.contains("(inner bean)") ? interfaceDesc : beanName + ":" + interfaceDesc;
    }
}
