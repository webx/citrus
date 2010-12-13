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

public interface ConfigurationPoints extends Schemas {
    String DEFAULT_CONFIGURATION_POINTS_LOCATION = "META-INF/spring.configuration-points";

    Collection<ConfigurationPoint> getConfigurationPoints();

    ConfigurationPoint getConfigurationPointByName(String name);

    ConfigurationPoint getConfigurationPointByNamespaceUri(String namespaceUri);
}
