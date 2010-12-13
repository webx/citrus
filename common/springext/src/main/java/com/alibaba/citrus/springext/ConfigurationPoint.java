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
package com.alibaba.citrus.springext;

import java.util.Collection;

import org.springframework.beans.factory.xml.NamespaceHandler;

public interface ConfigurationPoint {
    /**
     * 取得当前configuration point所在的所有configuration points注册表。
     */
    ConfigurationPoints getConfigurationPoints();

    /**
     * 取得在XML配置文件中，用来代表当前configuration point的名字空间。
     */
    String getNamespaceUri();

    /**
     * 取得spring <code>NamespaceHandler</code>对象。
     */
    NamespaceHandler getNamespaceHandler();

    /**
     * 取得configuration point的名称。
     */
    String getName();

    /**
     * 取得默认的element名称。
     */
    String getDefaultElementName();

    /**
     * 取得建议的ns前缀名。
     */
    String getPreferredNsPrefix();

    /**
     * 取得指定名称和类型的contribution。
     */
    Contribution getContribution(String name, ContributionType type);

    /**
     * 取得所有的contributions。
     */
    Collection<Contribution> getContributions();

    /**
     * 取得schemas。
     */
    VersionableSchemas getSchemas();

    /**
     * 取得描述。
     */
    String getDescription();
}
