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
package com.alibaba.citrus.service.moduleloader.impl.adapter;

import static com.alibaba.citrus.springext.util.SpringExtUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.alibaba.citrus.service.dataresolver.DataResolverService;
import com.alibaba.citrus.springext.support.parser.AbstractSingleBeanDefinitionParser;

public class AbstractDataBindingAdapterFactoryDefinitionParser<F extends AbstractDataBindingAdapterFactory> extends
        AbstractSingleBeanDefinitionParser<F> {
    @Override
    protected final void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        initAdapterBeanDefinition(element, builder);
        doParseFactory(element, parserContext, builder);
    }

    protected void doParseFactory(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
    }

    /**
     * 初始化adapter BD，可选注入dataResolverService。
     */
    public static void initAdapterBeanDefinition(Element element, BeanDefinitionBuilder builder) {
        String dataResolverRef = null;

        if (element != null) {
            dataResolverRef = trimToNull(element.getAttribute("dataResolverRef"));
        }

        if (dataResolverRef == null) {
            dataResolverRef = "dataResolverService";
        }

        addPropertyRef(builder, "dataResolverService", dataResolverRef, DataResolverService.class, false);
    }
}
