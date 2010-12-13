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
package com.alibaba.citrus.service.resource.loader.impl;

import static com.alibaba.citrus.springext.util.DomUtil.*;
import static com.alibaba.citrus.springext.util.SpringExtUtil.*;
import static com.alibaba.citrus.util.ObjectUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.alibaba.citrus.service.resource.loader.FileResourceLoader;
import com.alibaba.citrus.service.resource.loader.FileResourceLoader.SearchPath;
import com.alibaba.citrus.springext.support.parser.AbstractSingleBeanDefinitionParser;
import com.alibaba.citrus.springext.util.DomUtil.ElementSelector;

/**
 * ”√¿¥Ω‚Œˆfile-loader°£
 * 
 * @author Michael Zhou
 */
public class FileResourceLoaderDefinitionParser extends AbstractSingleBeanDefinitionParser<FileResourceLoader> {
    @Override
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        attributesToProperties(element, builder, "basedir");

        URL configFileURL;

        try {
            configFileURL = parserContext.getReaderContext().getResource().getURL();
        } catch (IOException e) {
            configFileURL = null;
        }

        if (configFileURL != null) {
            builder.addPropertyValue("configFileURL", configFileURL);
        }

        ElementSelector pathSelector = and(sameNs(element), name("path"));
        List<Object> paths = createManagedList(element, parserContext);

        for (Element subElement : subElements(element, pathSelector)) {
            paths.add(parsePath(subElement, parserContext));
        }

        builder.addPropertyValue("paths", paths);
    }

    private BeanDefinition parsePath(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(SearchPath.class);
        String type = defaultIfNull(trimToNull(element.getAttribute("type")), "relative");
        String path = trimToEmpty(element.getTextContent());

        builder.addConstructorArgValue(path);
        builder.addConstructorArgValue("relative".equals(type));

        return builder.getBeanDefinition();
    }
}
