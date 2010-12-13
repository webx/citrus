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

import org.springframework.beans.factory.xml.BeanDefinitionDecorator;
import org.springframework.beans.factory.xml.BeanDefinitionParser;

/**
 * Contribution¿‡–Õ°£
 * 
 * @author Michael Zhou
 */
public enum ContributionType {
    BEAN_DEFINITION_PARSER(BeanDefinitionParser.class, ".bean-definition-parsers"),
    BEAN_DEFINITION_DECORATOR(BeanDefinitionDecorator.class, ".bean-definition-decorators"),
    BEAN_DEFINITION_DECORATOR_FOR_ATTRIBUTE(BeanDefinitionDecorator.class, ".bean-definition-decorators-for-attribute");

    private final Class<?> contributionInterface;
    private final String contributionsLocationSuffix;

    private ContributionType(Class<?> contributionInterface, String contributionsLocationPattern) {
        this.contributionsLocationSuffix = contributionsLocationPattern;
        this.contributionInterface = contributionInterface;
    }

    public Class<?> getContributionInterface() {
        return contributionInterface;
    }

    public String getContributionsLocationSuffix() {
        return contributionsLocationSuffix;
    }
}
