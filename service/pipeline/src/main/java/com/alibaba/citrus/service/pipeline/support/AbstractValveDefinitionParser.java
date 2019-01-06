/*
 * Copyright (c) 2002-2012 Alibaba Group Holding Limited.
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
 */

package com.alibaba.citrus.service.pipeline.support;

import static com.alibaba.citrus.springext.util.DomUtil.subElements;
import static com.alibaba.citrus.springext.util.SpringExtUtil.attributesToProperties;
import static com.alibaba.citrus.springext.util.SpringExtUtil.createManagedList;
import static com.alibaba.citrus.springext.util.SpringExtUtil.getSiblingConfigurationPoint;
import static com.alibaba.citrus.springext.util.SpringExtUtil.parseConfigurationPointBean;
import static com.alibaba.citrus.util.ObjectUtil.defaultIfNull;
import static com.alibaba.citrus.util.StringUtil.trimToNull;

import java.util.List;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.alibaba.citrus.service.pipeline.Valve;
import com.alibaba.citrus.service.pipeline.impl.PipelineImpl;
import com.alibaba.citrus.service.pipeline.impl.condition.JexlCondition;
import com.alibaba.citrus.springext.ConfigurationPoint;
import com.alibaba.citrus.springext.Contribution;
import com.alibaba.citrus.springext.ContributionAware;
import com.alibaba.citrus.springext.support.parser.AbstractSingleBeanDefinitionParser;

/**
 * Valve解析器的基类。
 *
 * @author Michael Zhou
 */
public abstract class AbstractValveDefinitionParser<V extends Valve> extends AbstractSingleBeanDefinitionParser<V>
        implements ContributionAware {
    private ConfigurationPoint valvesConfigurationPoint;
    private ConfigurationPoint conditionsConfigurationPoint;

    public void setContribution(Contribution contrib) {
        valvesConfigurationPoint = getSiblingConfigurationPoint("services/pipeline/valves", contrib);
        conditionsConfigurationPoint = getSiblingConfigurationPoint("services/pipeline/conditions", contrib);
    }

    /** 取得pipeline bean。 */
    protected final Object parsePipeline(Element element, ParserContext parserContext) {
        return parsePipeline(element, null, parserContext);
    }

    protected final Object parsePipeline(Element element, Element labelElement, ParserContext parserContext) {
        return parsePipeline(element, labelElement, parserContext, null, false);
    }

    protected final Object parsePipeline(Element element, Element labelElement, ParserContext parserContext,
                                         String refAttribute) {
        return parsePipeline(element, labelElement, parserContext, refAttribute, false);
    }

    protected final Object parsePipeline(Element element, Element labelElement, ParserContext parserContext,
                                         String refAttribute, boolean returnNullIfNoValves) {
        // pipeline可以是引用或作为inner-bean。
        // 引用pipeline支持注入不同scope的pipeline，例如：注入一个request scope的pipeline scoped proxy。
        String pipelineRef = trimToNull(element.getAttribute(defaultIfNull(trimToNull(refAttribute), "pipeline-ref")));

        if (pipelineRef != null) {
            /*RuntimeBeanReference beanReference= new RuntimeBeanReference(pipelineRef);
            beanReference.setSource(parserContext.extractSource(element));
            return beanReference;
        	*///return new RuntimeBeanReference(pipelineRef);
        	//return new BeanDefinitionHolder(parserContext.getRegistry().getBeanDefinition(pipelineRef),pipelineRef);
        	//return new BeanDefinitionHolder(parserContext.getRegistry().getBeanDefinition(pipelineRef),pipelineRef);
        	//return parserContext.getRegistry().getBeanDefinition(pipelineRef);
        	BeanDefinition bean = parserContext.getRegistry().getBeanDefinition(pipelineRef);
        	return parserContext.getDelegate().parseBeanDefinitionElement(element);
//        	BeanDefinition beanDefinition = parserContext.getDelegate().parseCustomElement(element);
//        	return new BeanDefinitionHolder(beanDefinition,pipelineRef);
           
        }

        // parse pipeline as an inner-bean
        BeanDefinitionBuilder pipelineBuilder = BeanDefinitionBuilder.genericBeanDefinition(PipelineImpl.class);

        // label attribute所在的element通常是和定义子pipeline的element相同的，
        // 但是对于多分支的子pipeline，例如：try-catch-finally，choose-when-otherwise等，可以把label定义在顶层element中。
        if (labelElement == null) {
            labelElement = element;
        }

        attributesToProperties(labelElement, pipelineBuilder, "label");

        List<Object> valves = createManagedList(element, parserContext);

        for (Element subElement : subElements(element)) {
            Object valve = parseConfigurationPointBean(subElement, valvesConfigurationPoint, parserContext,
                                                       pipelineBuilder);

            if (valve != null) {
                valves.add(valve);
            }
        }

        if (returnNullIfNoValves && valves.isEmpty()) {
            return null;
        }

        pipelineBuilder.addPropertyValue("valves", valves);

        return pipelineBuilder.getBeanDefinition();
    }

    /** 取得condition bean。 */
    protected final Object parseCondition(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        // 先试从test attribute中创建jexl condition
        String jexl = trimToNull(element.getAttribute("test"));

        if (jexl != null) {
            BeanDefinitionBuilder conditionBuilder = BeanDefinitionBuilder.genericBeanDefinition(JexlCondition.class);
            conditionBuilder.addPropertyValue("expression", jexl);
            return conditionBuilder.getBeanDefinition();
        }

        // 再试从condition element中取得condition
        for (Element subElement : subElements(element)) {
            Object condition = parseConfigurationPointBean(subElement, conditionsConfigurationPoint, parserContext,
                                                           builder);

            if (condition != null) {
                return condition;
            }
        }

        return null;
    }
}
