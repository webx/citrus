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
package com.alibaba.citrus.service.velocity;

import org.apache.commons.collections.ExtendedProperties;
import org.springframework.core.io.ResourceLoader;

/**
 * ´ú±ívelocityµÄÅäÖÃ¡£
 * 
 * @author Michael Zhou
 */
public interface VelocityConfiguration {
    String DEFAULT_CHARSET = "UTF-8";

    ExtendedProperties getProperties();

    ResourceLoader getResourceLoader();

    boolean isProductionMode();
}
