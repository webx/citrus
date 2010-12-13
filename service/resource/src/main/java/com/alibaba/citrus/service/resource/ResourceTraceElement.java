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
package com.alibaba.citrus.service.resource;

/**
 * 代表资源搜索过程中的一个步骤。
 * 
 * @author Michael Zhou
 */
public class ResourceTraceElement {
    private final String configLocation;
    private final String beanName;
    private final String patternName;
    private final String patternType;
    private final String resourceName;

    public ResourceTraceElement(String configLocation, String beanName, String patternType, String patternName,
                                String resourceName) {
        this.configLocation = configLocation;
        this.beanName = beanName;
        this.patternType = patternType;
        this.patternName = patternName;
        this.resourceName = resourceName;
    }

    public String getConfigLocation() {
        return configLocation;
    }

    public String getBeanName() {
        return beanName;
    }

    public String getPatternType() {
        return patternType;
    }

    public String getPatternName() {
        return patternName;
    }

    public String getResourceName() {
        return resourceName;
    }

    public String getShortLocation() {
        if (configLocation == null) {
            return "(unknown location)";
        } else {
            return configLocation.substring(configLocation.lastIndexOf("/") + 1);
        }
    }

    @Override
    public String toString() {
        return String.format("\"%s\" matched [%s pattern=\"%s\"], at \"%s\", beanName=\"%s\"", resourceName,
                patternType, patternName, getShortLocation(), beanName);
    }
}
