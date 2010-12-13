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
package com.alibaba.citrus.service.mappingrule;

/**
 * 将一个名称映射到另一个名称的服务。
 * <p>
 * 在webx中，需要将模板名转换成相应的module类名或layout模板名等。
 * </p>
 * <p>
 * <code>MappingRuleService</code>提供了一个可扩展的通用方法来定义这些转换规则。
 * </p>
 * 
 * @author Michael Zhou
 */
public interface MappingRuleService {
    /**
     * 将指定名称映射成指定类型的名称。
     * <p>
     * 假如<code>ruleType</code>不存在，则抛出异常。
     * </p>
     */
    String getMappedName(String ruleType, String name) throws MappingRuleNotFoundException, MappingRuleException;
}
