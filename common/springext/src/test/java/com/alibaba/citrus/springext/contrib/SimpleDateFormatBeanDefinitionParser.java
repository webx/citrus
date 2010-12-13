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
package com.alibaba.citrus.springext.contrib;

import java.text.SimpleDateFormat;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

import com.alibaba.citrus.springext.support.parser.AbstractNamedBeanDefinitionParser;

public class SimpleDateFormatBeanDefinitionParser extends AbstractNamedBeanDefinitionParser<SimpleDateFormat> {
    @Override
    protected void doParse(Element element, BeanDefinitionBuilder bean) {
        String pattern = element.getAttribute("pattern");
        String lenient = element.getAttribute("lenient");

        bean.addConstructorArgValue(pattern);

        if (StringUtils.hasText(lenient)) {
            bean.addPropertyValue("lenient", lenient);
        }
    }

    @Override
    protected String getDefaultName() {
        return "dateFormat";
    }
}
