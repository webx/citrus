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
package com.alibaba.citrus.service.form.impl;

import static com.alibaba.citrus.service.configuration.support.PropertyEditorRegistrarsSupport.*;
import static com.alibaba.citrus.springext.util.DomUtil.*;
import static com.alibaba.citrus.springext.util.SpringExtUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.alibaba.citrus.service.form.impl.configuration.FieldConfigImpl;
import com.alibaba.citrus.service.form.impl.configuration.FormConfigImpl;
import com.alibaba.citrus.service.form.impl.configuration.GroupConfigImpl;
import com.alibaba.citrus.service.form.impl.configuration.GroupConfigImpl.ImportImpl;
import com.alibaba.citrus.springext.ConfigurationPoint;
import com.alibaba.citrus.springext.Contribution;
import com.alibaba.citrus.springext.ContributionAware;
import com.alibaba.citrus.springext.support.parser.AbstractNamedBeanDefinitionParser;
import com.alibaba.citrus.springext.util.DomUtil.ElementSelector;
import com.alibaba.citrus.util.StringUtil;

public class FormServiceDefinitionParser extends AbstractNamedBeanDefinitionParser<FormServiceImpl> implements
        ContributionAware {
    private ConfigurationPoint validatorConfigurationPoint;

    public void setContribution(Contribution contrib) {
        this.validatorConfigurationPoint = getSiblingConfigurationPoint("services/form/validators", contrib);
    }

    @Override
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder formServiceBuilder) {
        // bean attributes
        parseBeanDefinitionAttributes(element, parserContext, formServiceBuilder);

        // injecting HttpServletRequest in constructor, required=true
        addConstructorArg(formServiceBuilder, true, HttpServletRequest.class);

        // new FormConfigImp()
        BeanDefinitionBuilder formConfigBuilder = BeanDefinitionBuilder.genericBeanDefinition(FormConfigImpl.class);

        attributesToProperties(element, formConfigBuilder, "converterQuiet", "postOnlyByDefault", "messageCodePrefix");

        // registrars
        formConfigBuilder.addPropertyValue("propertyEditorRegistrars",
                parseRegistrars(element, parserContext, formConfigBuilder));

        // groups
        List<Object> groups = createManagedList(element, parserContext);

        for (Element subElement : subElements(element, and(sameNs(element), name("group")))) {
            groups.add(parseGroup(subElement, parserContext, formConfigBuilder));
        }

        formConfigBuilder.addPropertyValue("groupConfigImplList", groups);

        formServiceBuilder.addPropertyValue("formConfigImpl", formConfigBuilder.getBeanDefinition());
    }

    private Object parseGroup(Element element, ParserContext parserContext, BeanDefinitionBuilder formConfigBuilder) {
        BeanDefinitionBuilder groupConfigBuilder = BeanDefinitionBuilder.genericBeanDefinition(GroupConfigImpl.class);

        // set attributes
        attributesToProperties(element, groupConfigBuilder, "name", "trimmingByDefault", "postOnly");

        String parentGroup = trimToNull(element.getAttribute("extends"));

        if (parentGroup != null) {
            groupConfigBuilder.addPropertyValue("parentGroup", parentGroup);
        }

        // import & field
        ElementSelector importSelector = and(sameNs(element), name("import"));
        ElementSelector fieldSelector = and(sameNs(element), name("field"));

        List<Object> imports = createManagedList(element, parserContext);
        List<Object> fields = createManagedList(element, parserContext);

        for (Element subElement : subElements(element)) {
            if (importSelector.accept(subElement)) {
                imports.add(parseImport(subElement, parserContext, groupConfigBuilder));
            } else if (fieldSelector.accept(subElement)) {
                fields.add(parseField(subElement, parserContext, groupConfigBuilder));
            }
        }

        groupConfigBuilder.addPropertyValue("imports", imports);
        groupConfigBuilder.addPropertyValue("fieldConfigImplList", fields);

        return groupConfigBuilder.getBeanDefinition();
    }

    private Object parseImport(Element element, ParserContext parserContext, BeanDefinitionBuilder groupConfigBuilder) {
        BeanDefinitionBuilder importBuilder = BeanDefinitionBuilder.genericBeanDefinition(ImportImpl.class);

        importBuilder.addConstructorArgValue(element.getAttribute("group"));
        importBuilder.addConstructorArgValue(element.getAttribute("field"));

        return importBuilder.getBeanDefinition();
    }

    private Object parseField(Element element, ParserContext parserContext, BeanDefinitionBuilder groupConfigBuilder) {
        BeanDefinitionBuilder fieldConfigBuilder = BeanDefinitionBuilder.genericBeanDefinition(FieldConfigImpl.class);

        // set attributes
        attributesToProperties(element, fieldConfigBuilder, "name", "propertyName", "displayName", "trimming");

        String[] defaultValues = StringUtil.split(element.getAttribute("defaultValue"), ", ");
        fieldConfigBuilder.addPropertyValue("defaultValues", defaultValues);

        // validators
        List<Object> validators = createManagedList(element, parserContext);

        for (Element subElement : subElements(element)) {
            Object validator = parseConfigurationPointBean(subElement, validatorConfigurationPoint, parserContext,
                    fieldConfigBuilder);

            if (validator != null) {
                validators.add(validator);
            }
        }

        fieldConfigBuilder.addPropertyValue("validators", validators);

        return fieldConfigBuilder.getBeanDefinition();
    }

    @Override
    protected String getDefaultName() {
        return "formService";
    }
}
