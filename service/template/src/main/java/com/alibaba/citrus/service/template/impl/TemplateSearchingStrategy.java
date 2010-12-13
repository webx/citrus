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
package com.alibaba.citrus.service.template.impl;

/**
 * 由<code>TemplateService</code>调用的，用来查找template的strategy。
 * 
 * @author Michael Zhou
 */
public interface TemplateSearchingStrategy {
    /**
     * 取得用来缓存模板搜索结果的key。
     */
    Object getKey(String templateName);

    /**
     * 查找template，如果找到，则返回<code>true</code>。
     * <p>
     * 可更改matcher参数中的模板名称和后缀。
     * </p>
     */
    boolean findTemplate(TemplateMatcher matcher);
}
