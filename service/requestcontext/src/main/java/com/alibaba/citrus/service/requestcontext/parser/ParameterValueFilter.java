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
package com.alibaba.citrus.service.requestcontext.parser;

/**
 * 过滤用户输入的参数值。
 * 
 * @author Michael Zhou
 */
public interface ParameterValueFilter extends ParameterParserFilter {
    /**
     * 过滤指定值，如果返回<code>null</code>表示忽略该值。
     * <p>
     * 注意，<code>value</code>可能是<code>null</code>。
     * </p>
     */
    String filter(String key, String value, boolean isHtml);
}
