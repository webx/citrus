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
package com.alibaba.citrus.util.internal.webpagelite;

/**
 * 代表一个简单的组件注册表。
 * 
 * @author Michael Zhou
 */
public interface PageComponentRegistry {
    /**
     * 注册组件。
     */
    void register(String componentPath, PageComponent component);

    /**
     * 取得所有的componentPaths。
     */
    String[] getComponentPaths();

    /**
     * 取得指定名称的组件。
     */
    <PC extends PageComponent> PC getComponent(String componentPath, Class<PC> componentClass);
}
