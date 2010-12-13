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
package com.alibaba.citrus.service.mail.impl;

import static com.alibaba.citrus.springext.util.DomUtil.*;
import static com.alibaba.citrus.springext.util.SpringExtUtil.*;
import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.alibaba.citrus.service.mail.builder.MailAddressType;
import com.alibaba.citrus.service.mail.session.MailStore;
import com.alibaba.citrus.service.mail.session.MailTransport;
import com.alibaba.citrus.springext.ConfigurationPoint;
import com.alibaba.citrus.springext.Contribution;
import com.alibaba.citrus.springext.ContributionAware;
import com.alibaba.citrus.springext.support.parser.AbstractNamedBeanDefinitionParser;
import com.alibaba.citrus.springext.util.DomUtil.ElementSelector;

public class MailServiceDefinitionParser extends AbstractNamedBeanDefinitionParser<MailServiceImpl> implements
        ContributionAware {
    private ConfigurationPoint contentsConfigurationPoint;

    public void setContribution(Contribution contrib) {
        this.contentsConfigurationPoint = getSiblingConfigurationPoint("services/mails/contents", contrib);
    }

    @Override
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        parseBeanDefinitionAttributes(element, parserContext, builder);

        // default settings
        Element defaultSettingsElement = theOnlySubElement(element, and(sameNs(element), name("default-settings")));

        if (defaultSettingsElement != null) {
            parseDefaultSettings(defaultSettingsElement, parserContext, builder);
        }

        // mails/stores/transports
        ElementSelector mailSelector = and(sameNs(element), name("mail"));
        ElementSelector storeSelector = and(sameNs(element), name("mail-store"));
        ElementSelector transportSelector = and(sameNs(element), name("mail-transport"));

        Map<Object, Object> mails = createManagedMap(element, parserContext);
        Map<Object, Object> mailStores = createManagedMap(element, parserContext);
        Map<Object, Object> mailTransports = createManagedMap(element, parserContext);

        for (Element subElement : subElements(element)) {
            Map<Object, Object> container;
            BeanDefinitionHolder holder;

            if (mailSelector.accept(subElement)) {
                holder = parseMail(subElement, parserContext, builder.getRawBeanDefinition());
                container = mails;
            } else if (storeSelector.accept(subElement)) {
                holder = parseMailStore(subElement, parserContext, builder.getRawBeanDefinition());
                container = mailStores;
            } else if (transportSelector.accept(subElement)) {
                holder = parseMailTransport(subElement, parserContext, builder.getRawBeanDefinition());
                container = mailTransports;
            } else {
                continue;
            }

            container.put(holder.getBeanName(), holder);
        }

        builder.addPropertyValue("mails", mails);
        builder.addPropertyValue("mailStores", mailStores);
        builder.addPropertyValue("mailTransports", mailTransports);
    }

    private void parseDefaultSettings(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        List<Object> importedServices = createManagedList(element, parserContext);

        for (Element subElement : subElements(element, and(sameNs(element), name("import")))) {
            String serviceRef = assertNotNull(trimToNull(subElement.getAttribute("serviceRef")), "miss serviceRef");
            importedServices.add(new RuntimeBeanReference(serviceRef));
        }

        builder.addPropertyValue("importedServices", importedServices);
    }

    private BeanDefinitionHolder parseMail(Element element, ParserContext parserContext,
                                           AbstractBeanDefinition containingBean) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(MailBuilderFactory.class);

        // charset
        builder.addPropertyValue("charset", element.getAttribute("charset"));

        // addrs
        Map<Object, Object> addrs = createManagedMap(element, parserContext);

        for (MailAddressType addrType : MailAddressType.values()) {
            addrs.put(addrType.name(),
                    getAddresses(element, parserContext, and(sameNs(element), name(addrType.getTagName()))));
        }

        builder.addPropertyValue("addresses", addrs);

        // subject
        Element subjectElement = theOnlySubElement(element, and(sameNs(element), name("subject")));

        if (subjectElement != null) {
            builder.addPropertyValue("subject", subjectElement.getTextContent());
        }

        // content
        Element contentElement = theOnlySubElement(element, ns(contentsConfigurationPoint.getNamespaceUri()));

        if (contentElement != null) {
            builder.addPropertyValue("content",
                    parseConfigurationPointBean(contentElement, contentsConfigurationPoint, parserContext, builder));
        }

        return getBeanDefinitionHolder(element, builder);
    }

    private List<Object> getAddresses(Element element, ParserContext parserContext, ElementSelector selector) {
        List<Object> addrs = createManagedList(element, parserContext);

        for (Element subElement : subElements(element, selector)) {
            addrs.add(subElement.getTextContent());
        }

        return addrs;
    }

    private BeanDefinitionHolder parseMailStore(Element element, ParserContext parserContext,
                                                AbstractBeanDefinition containingBean) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(MailStore.class);

        attributesToProperties(element, builder, "protocol", "folder");
        parseMailSession(element, parserContext, builder);

        return getBeanDefinitionHolder(element, builder);
    }

    private BeanDefinitionHolder parseMailTransport(Element element, ParserContext parserContext,
                                                    AbstractBeanDefinition containingBean) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(MailTransport.class);

        attributesToProperties(element, builder, "protocol", "popBeforeSmtp");
        parseMailSession(element, parserContext, builder);

        return getBeanDefinitionHolder(element, builder);
    }

    private void parseMailSession(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        attributesToProperties(element, builder, "host", "port", "user", "password", "debug", "default");

        Map<Object, Object> props = createManagedMap(element, parserContext);

        for (Element subElement : subElements(element, and(sameNs(element), name("property")))) {
            String name = trimToNull(subElement.getAttribute("name"));
            String value = trimToNull(subElement.getAttribute("value"));

            if (value == null) {
                value = trimToNull(subElement.getTextContent());
            }

            props.put(name, value);
        }

        builder.addPropertyValue("properties", props);
    }

    private BeanDefinitionHolder getBeanDefinitionHolder(Element element, BeanDefinitionBuilder builder) {
        return new BeanDefinitionHolder(builder.getBeanDefinition(), trimToNull(element.getAttribute("id")));
    }

    @Override
    protected String getDefaultName() {
        return "mails, mailService";
    }
}
