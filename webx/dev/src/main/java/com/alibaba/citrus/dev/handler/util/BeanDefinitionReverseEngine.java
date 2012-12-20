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

package com.alibaba.citrus.dev.handler.util;

import static com.alibaba.citrus.util.ArrayUtil.*;
import static com.alibaba.citrus.util.BasicConstant.*;
import static com.alibaba.citrus.util.ClassUtil.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;
import static org.springframework.beans.factory.support.AbstractBeanDefinition.*;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

import org.springframework.beans.PropertyValue;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.ConstructorArgumentValues.ValueHolder;
import org.springframework.beans.factory.config.RuntimeBeanNameReference;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.AutowireCandidateQualifier;
import org.springframework.beans.factory.support.LookupOverride;
import org.springframework.beans.factory.support.MethodOverride;
import org.springframework.beans.factory.support.ReplaceOverride;
import org.springframework.beans.factory.xml.BeanDefinitionParserDelegate;

/**
 * 将<code>BeanDefinition</code>转换成xml。
 *
 * @author Michael Zhou
 */
public class BeanDefinitionReverseEngine {
    private final AbstractBeanDefinition bd;
    private final String                 name;
    private final String[]               aliases;
    private final Element                beanElement;
    private final boolean                innerBean;

    public BeanDefinitionReverseEngine(AbstractBeanDefinition bd, String name, String[] aliases) {
        this(bd, name, aliases, null);
    }

    public BeanDefinitionReverseEngine(AbstractBeanDefinition bd, String name, String[] aliases, Element parentElement) {
        this.bd = bd;
        this.name = trimToNull(name);
        this.aliases = defaultIfEmptyArray(aliases, EMPTY_STRING_ARRAY);
        this.beanElement = new Element("bean");
        this.innerBean = parentElement != null;

        if (innerBean) {
            parentElement.subElements.add(beanElement);
        }

        doReverseEngineering();
    }

    private void doReverseEngineering() {
        beanAttributes();
        beanConstructorArgs();
        beanProperties();
        methodOverrides();
        qualifiers();
    }

    private void beanAttributes() {
        if (!innerBean && name != null) {
            beanElement.addAttribute("id", new AnchorValue(name));
        }

        if (!innerBean && aliases.length > 0) {
            beanElement.addAttribute("name", new AnchorValue(aliases));
        }

        if (bd.isAbstract()) {
            beanElement.addAttribute("abstract", "true");
        }

        if (bd.getParentName() != null) {
            beanElement.addAttribute("parent", new RefValue(bd.getParentName()));
        }

        if (bd.getBeanClassName() != null) {
            beanElement.addAttribute("class", new ClassValue(bd.getBeanClassName()));
        }

        if (bd.isPrimary()) {
            beanElement.addAttribute("primary", "true");
        }

        String autowireMode = null;

        switch (bd.getAutowireMode()) {
            case AUTOWIRE_AUTODETECT:
                autowireMode = BeanDefinitionParserDelegate.AUTOWIRE_AUTODETECT_VALUE;
                break;

            case AUTOWIRE_BY_NAME:
                autowireMode = BeanDefinitionParserDelegate.AUTOWIRE_BY_NAME_VALUE;
                break;

            case AUTOWIRE_BY_TYPE:
                autowireMode = BeanDefinitionParserDelegate.AUTOWIRE_BY_TYPE_VALUE;
                break;

            case AUTOWIRE_CONSTRUCTOR:
                autowireMode = BeanDefinitionParserDelegate.AUTOWIRE_CONSTRUCTOR_VALUE;
                break;
        }

        if (autowireMode != null) {
            beanElement.addAttribute("autowire", autowireMode);
        }

        if (!bd.isAutowireCandidate()) {
            beanElement.addAttribute("autowire-candidate", "false");
        }

        String dependencyCheck = null;

        switch (bd.getDependencyCheck()) {
            case DEPENDENCY_CHECK_ALL:
                dependencyCheck = BeanDefinitionParserDelegate.DEPENDENCY_CHECK_ALL_ATTRIBUTE_VALUE;
                break;

            case DEPENDENCY_CHECK_OBJECTS:
                dependencyCheck = BeanDefinitionParserDelegate.DEPENDENCY_CHECK_OBJECTS_ATTRIBUTE_VALUE;
                break;

            case DEPENDENCY_CHECK_SIMPLE:
                dependencyCheck = BeanDefinitionParserDelegate.DEPENDENCY_CHECK_SIMPLE_ATTRIBUTE_VALUE;
                break;
        }

        if (dependencyCheck != null) {
            beanElement.addAttribute("dependency-check", dependencyCheck);
        }

        if (!isEmptyArray(bd.getDependsOn())) {
            beanElement.addAttribute("depends-on", new RefValue(bd.getDependsOn()));
        }

        if (!isEmpty(bd.getScope()) && !"singleton".equals(bd.getScope())) {
            beanElement.addAttribute("scope", bd.getScope());
        }

        if (bd.getInitMethodName() != null) {
            beanElement.addAttribute("init-method", bd.getInitMethodName());
        }

        if (bd.getDestroyMethodName() != null) {
            beanElement.addAttribute("destroy-method", bd.getDestroyMethodName());
        }

        if (bd.isLazyInit()) {
            beanElement.addAttribute("lazy-init", "true");
        }

        if (bd.getFactoryBeanName() != null) {
            beanElement.addAttribute("factory-bean", new RefValue(bd.getFactoryBeanName()));
        }

        if (bd.getFactoryMethodName() != null) {
            beanElement.addAttribute("factory-method", bd.getFactoryMethodName());
        }
    }

    private void beanConstructorArgs() {
        ConstructorArgumentValues values = bd.getConstructorArgumentValues();

        // indexed args
        @SuppressWarnings("unchecked")
        Map<Integer, ValueHolder> indexedArgs = new TreeMap<Integer, ValueHolder>(values.getIndexedArgumentValues());

        for (Map.Entry<Integer, ValueHolder> entry : indexedArgs.entrySet()) {
            Integer index = entry.getKey();
            ValueHolder valueHolder = entry.getValue();

            Element constructorArgElement = beanElement.newSubElement("constructor-arg");
            constructorArgElement.addAttribute("index", index.toString());

            beanConstructorArg(constructorArgElement, valueHolder);
        }

        // generic args
        @SuppressWarnings("unchecked")
        List<ValueHolder> genericArgs = values.getGenericArgumentValues();

        for (ValueHolder valueHolder : genericArgs) {
            Element constructorArgElement = beanElement.newSubElement("constructor-arg");
            beanConstructorArg(constructorArgElement, valueHolder);
        }
    }

    private void beanConstructorArg(Element constructorArgElement, ValueHolder valueHolder) {
        if (valueHolder.getType() != null) {
            constructorArgElement.addAttribute("type", new ClassValue(valueHolder.getType()));
        }

        value(constructorArgElement, valueHolder.getValue(), true, true, null, null);
    }

    private void beanProperties() {
        PropertyValues values = bd.getPropertyValues();
        List<PropertyValue> props = createArrayList(values.getPropertyValues());

        Collections.sort(props, new Comparator<PropertyValue>() {
            public int compare(PropertyValue o1, PropertyValue o2) {
                String n1 = o1.getName();
                String n2 = o2.getName();

                if (n1 != null && n2 != null) {
                    return n1.compareTo(n2);
                }

                if (n1 == null && n2 == null) {
                    return 0;
                }

                if (n1 == null) {
                    return -1;
                } else {
                    return 1;
                }
            }
        });

        for (PropertyValue prop : props) {
            Element propertyElement = beanElement.newSubElement("property");

            if (prop.getName() != null) {
                propertyElement.addAttribute("name", prop.getName());
            }

            value(propertyElement, prop.getValue(), true, true, null, null);
        }
    }

    private void value(Element element, Object value, boolean supportValueAttribute, boolean supportRefAttribute,
                       Element containerElement, String typeAttrName) {
        // null
        if (value == null || value instanceof TypedStringValue && ((TypedStringValue) value).getValue() == null) {
            element.newSubElement("null");
            return;
        }

        // ref
        if (value instanceof RuntimeBeanReference) {
            String refto = ((RuntimeBeanReference) value).getBeanName();

            if (((RuntimeBeanReference) value).isToParent()) {
                element.newSubElement("ref").addAttribute("parent", new RefValue(refto));
            } else if (supportRefAttribute) {
                element.addAttribute("ref", new RefValue(refto));
            } else {
                element.newSubElement("ref").addAttribute("bean", new RefValue(refto));
            }

            return;
        }

        // idref
        if (value instanceof RuntimeBeanNameReference) {
            element.newSubElement("idref").addAttribute("bean",
                                                        new RefValue(((RuntimeBeanNameReference) value).getBeanName()));
            return;
        }

        // bean
        BeanDefinition innerBd = null;
        String innerName = null;
        String[] innerAliases = null;

        if (value instanceof BeanDefinitionHolder) {
            innerBd = ((BeanDefinitionHolder) value).getBeanDefinition();
            innerName = ((BeanDefinitionHolder) value).getBeanName();
            innerAliases = ((BeanDefinitionHolder) value).getAliases();
        } else if (value instanceof BeanDefinition) {
            innerBd = (BeanDefinition) value;
        }

        if (innerBd != null) {
            if (innerBd instanceof AbstractBeanDefinition) {
                new BeanDefinitionReverseEngine((AbstractBeanDefinition) innerBd, innerName, innerAliases, element);
            } else {
                element.newSubElement("bean").addAttribute("unknownBeanDefinitionType",
                                                           new ClassValue(innerBd.getClass().getName()));
            }

            return;
        }

        // list or array
        if (value.getClass().isArray()) {
            value = createLinkedList(arrayAsIterable(Object.class, value));
        }

        if (value instanceof List<?>) {
            Element listElement = element.newSubElement("list");

            for (Object itemValue : (List<?>) value) {
                value(listElement, itemValue, false, false, listElement, "value-type");
            }

            return;
        }

        // set
        if (value instanceof Set<?>) {
            Element setElement = element.newSubElement("set");

            for (Object itemValue : (Set<?>) value) {
                value(setElement, itemValue, false, false, setElement, "value-type");
            }

            return;
        }

        // props
        if (value instanceof Properties) {
            Element propsElement = element.newSubElement("props");

            for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
                Element propElement = propsElement.newSubElement("prop");

                propElement.addAttribute("key", String.valueOf(entry.getKey()));
                propElement.setText(String.valueOf(entry.getValue()));
            }

            return;
        }

        // map
        if (value instanceof Map<?, ?>) {
            Element mapElement = element.newSubElement("map");

            for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
                Element entryElement = mapElement.newSubElement("entry");

                mapEntryKeyValue(entryElement, entry.getKey(), "key-ref", "key", "key", mapElement, "key-type");
                mapEntryKeyValue(entryElement, entry.getValue(), "value-ref", "value", null, mapElement, "value-type");
            }

            return;
        }

        // typed string
        if (value instanceof TypedStringValue) {
            simpleValue(element, new TextValue(((TypedStringValue) value).getValue()),
                        ((TypedStringValue) value).getTargetTypeName(), supportValueAttribute, containerElement,
                        typeAttrName);
            return;
        }

        // simple raw data
        if (value instanceof String || getPrimitiveType(value.getClass()) != null) {
            simpleValue(element, new TextValue(value.toString()), null, supportValueAttribute, containerElement,
                        typeAttrName);
            return;
        }

        // raw data
        simpleValue(element, new RawValue(value.getClass(), value.toString()), null, supportValueAttribute,
                    containerElement, typeAttrName);
    }

    private void mapEntryKeyValue(Element entryElement, Object keyOrValue, String refAttrName, String valueAttrName,
                                  String valueElementName, Element mapElement, String typeAttrName) {
        if (keyOrValue != null) {
            // ref
            if (keyOrValue instanceof RuntimeBeanReference) {
                String refto = ((RuntimeBeanReference) keyOrValue).getBeanName();

                if (!((RuntimeBeanReference) keyOrValue).isToParent()) {
                    entryElement.addAttribute(refAttrName, new RefValue(refto));
                    return;
                }
            }

            String simpleValue = null;

            // typed string
            if (keyOrValue instanceof TypedStringValue) {
                String type = ((TypedStringValue) keyOrValue).getTargetTypeName();

                if (type != null) {
                    // 如果<map>中没有type attribute，则把type设置到map中，作为默认的type。
                    if (mapElement != null && typeAttrName != null && !mapElement.hasAttribute(typeAttrName)) {
                        mapElement.addAttribute(typeAttrName, new ClassValue(type));
                    }

                    // 如果type和<map>中的默认type相同，则不显示
                    if (type.equals(mapElement.getAttribute(typeAttrName))) {
                        type = null;
                    }
                }

                if (type == null) {
                    simpleValue = ((TypedStringValue) keyOrValue).getValue();
                }
            }

            // simple raw data
            if (keyOrValue instanceof String || getPrimitiveType(keyOrValue.getClass()) != null) {
                simpleValue = keyOrValue.toString();
            }

            if (simpleValue != null && !hasControlChars(simpleValue)) {
                entryElement.addAttribute(valueAttrName, simpleValue);
                return;
            }
        }

        // complex key or value
        if (valueElementName != null) {
            value(entryElement.newSubElement(valueElementName), keyOrValue, false, false, null, null);
        } else {
            value(entryElement, keyOrValue, false, false, null, null);
        }
    }

    private void simpleValue(Element element, StyledValue value, String type, boolean supportValueAttribute,
                             Element containerElement, String typeAttrName) {
        if (type != null) {
            // 如果<list>中没有type attribute，则把type设置到map中，作为默认的type。
            if (containerElement != null && typeAttrName != null && !containerElement.hasAttribute(typeAttrName)) {
                containerElement.addAttribute(typeAttrName, new ClassValue(type));
            }

            // 如果type和<list>中的默认type相同，则不显示
            if (type.equals(containerElement.getAttribute(typeAttrName))) {
                type = null;
            }
        }

        if (type == null && supportValueAttribute && !hasControlChars(value.getText())) {
            element.addAttribute("value", value);
            return;
        }

        Element valueElement = element.newSubElement("value").setText(value);

        if (type != null) {
            valueElement.addAttribute("type", new ClassValue(type));
        }
    }

    private void methodOverrides() {
        @SuppressWarnings("unchecked")
        Set<MethodOverride> overrides = bd.getMethodOverrides().getOverrides();

        for (MethodOverride override : overrides) {
            if (override instanceof LookupOverride) {
                Element lookupMethodElement = beanElement.newSubElement("lookup-method");
                String bean = ((LookupOverride) override).getBeanName();
                String name = override.getMethodName();

                if (bean != null) {
                    lookupMethodElement.addAttribute("bean", new RefValue(bean));
                }

                if (name != null) {
                    lookupMethodElement.addAttribute("name", name);
                }

                continue;
            }

            if (override instanceof ReplaceOverride) {
                Element replacedMethodElement = beanElement.newSubElement("replaced-method");
                String bean = ((ReplaceOverride) override).getMethodReplacerBeanName();
                String name = override.getMethodName();

                if (bean != null) {
                    replacedMethodElement.addAttribute("replacer", new RefValue(bean));
                }

                if (name != null) {
                    replacedMethodElement.addAttribute("name", name);
                }

                continue;
            }
        }
    }

    private void qualifiers() {
        @SuppressWarnings("unchecked")
        Set<AutowireCandidateQualifier> qualifiers = bd.getQualifiers();

        for (AutowireCandidateQualifier q : qualifiers) {
            Element qualifierElement = beanElement.newSubElement("qualifier");

            if (q.getTypeName() != null) {
                qualifierElement.addAttribute("type", new ClassValue(q.getTypeName()));
            }

            if (q.getAttribute("value") != null) {
                qualifierElement.addAttribute("value", String.valueOf(q.getAttribute("value")));
            }

            for (String attrName : q.attributeNames()) {
                if (!"value".equals(attrName)) {
                    Element attrElement = qualifierElement.newSubElement("attribute");

                    attrElement.addAttribute("key", String.valueOf(attrName));
                    attrElement.addAttribute("value", String.valueOf(q.getAttribute(attrName)));
                }
            }
        }
    }

    private final static char[] controlChars;

    static {
        StringBuilder buf = new StringBuilder();

        for (int i = 0; i < ' '; i++) {
            buf.append((char) i);
        }

        controlChars = buf.toString().toCharArray();
    }

    static boolean hasControlChars(String text) {
        return !containsNone(text, controlChars);
    }

    public final Element toDom() {
        return beanElement;
    }
}
