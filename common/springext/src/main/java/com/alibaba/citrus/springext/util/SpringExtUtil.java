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
package com.alibaba.citrus.springext.util;

import static com.alibaba.citrus.springext.util.DomUtil.*;
import static com.alibaba.citrus.util.ArrayUtil.*;
import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.BasicConstant.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.ObjectUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;
import static org.springframework.util.ReflectionUtils.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.cglib.core.DefaultNamingPolicy;
import net.sf.cglib.core.Predicate;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.CallbackFilter;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.BeanReference;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.support.ManagedMap;
import org.springframework.beans.factory.support.ManagedSet;
import org.springframework.beans.factory.xml.BeanDefinitionParserDelegate;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.context.ApplicationContext;
import org.springframework.core.MethodParameter;
import org.springframework.util.ObjectUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

import com.alibaba.citrus.springext.ConfigurationPoint;
import com.alibaba.citrus.springext.Contribution;
import com.alibaba.citrus.springext.util.DomUtil.ElementSelector;
import com.alibaba.citrus.util.Assert;

public class SpringExtUtil {
    private static final Logger log = LoggerFactory.getLogger(SpringExtUtil.class);

    public static <T> T getBeanOfType(BeanFactory beanFactory, Class<T> type) {
        if (beanFactory == null || !(beanFactory instanceof ListableBeanFactory)) {
            return null;
        }

        ListableBeanFactory listable = (ListableBeanFactory) beanFactory;

        @SuppressWarnings("unchecked")
        Map<String, T> beans = listable.getBeansOfType(type);

        if (beans == null || beans.isEmpty()) {
            return null;
        }

        return beans.values().iterator().next();
    }

    public static <T> T autowireAndInitialize(T existingBean, ApplicationContext context, int autowireMode,
                                              String beanName) {
        context.getAutowireCapableBeanFactory().autowireBeanProperties(existingBean, autowireMode, false);
        context.getAutowireCapableBeanFactory().initializeBean(existingBean, beanName);

        return existingBean;
    }

    public static ConfigurationPoint getSiblingConfigurationPoint(String configurationPointName, Contribution contrib) {
        assertNotNull(contrib, "contribution");
        return getSiblingConfigurationPoint(configurationPointName, contrib.getConfigurationPoint());
    }

    public static ConfigurationPoint getSiblingConfigurationPoint(String configurationPointName, ConfigurationPoint cp) {
        assertNotNull(configurationPointName, "configurationPointName");
        assertNotNull(cp, "configurationPoint");

        ConfigurationPoint siblingCp = cp.getConfigurationPoints().getConfigurationPointByName(configurationPointName);

        assertNotNull(siblingCp, "could not find configuration point of name: %s", configurationPointName);

        return siblingCp;
    }

    /**
     * 创建<code>ManagedList</code>，避免type safety警告。
     */
    @SuppressWarnings("unchecked")
    public static List<Object> createManagedList(Element element, ParserContext parserContext) {
        ManagedList list = new ManagedList();

        list.setSource(parserContext.getReaderContext().extractSource(element));

        return list;
    }

    /**
     * 创建<code>ManagedMap</code>，避免type safety警告。
     */
    @SuppressWarnings("unchecked")
    public static Map<Object, Object> createManagedMap(Element element, ParserContext parserContext) {
        ManagedMap map = new ManagedMap();

        map.setSource(parserContext.getReaderContext().extractSource(element));

        return map;
    }

    /**
     * 创建<code>ManagedSet</code>，避免type safety警告。
     */
    @SuppressWarnings("unchecked")
    public static Set<Object> createManagedSet(Element element, ParserContext parserContext) {
        ManagedSet set = new ManagedSet();

        set.setSource(parserContext.getReaderContext().extractSource(element));

        return set;
    }

    /**
     * 将子element的值设入properties。
     */
    public static void subElementsToProperties(Element element, BeanDefinitionBuilder builder) {
        subElementsToProperties(element, null, builder, null);
    }

    /**
     * 将子element的值设入properties。
     */
    public static void subElementsToProperties(Element element, BeanDefinitionBuilder builder, ElementSelector selector) {
        subElementsToProperties(element, null, builder, selector);
    }

    /**
     * 将子element的值设入properties。
     */
    public static void subElementsToProperties(Element element, String propertyPrefix, BeanDefinitionBuilder builder,
                                               ElementSelector selector) {
        for (Element subElement : subElements(element, selector)) {
            elementToProperty(subElement, propertyPrefix, builder);
        }
    }

    /**
     * 将一个element的值设入property。
     */
    public static void elementToProperty(Element element, BeanDefinitionBuilder builder) {
        elementToProperty(element, null, builder);
    }

    /**
     * 将一个element的值设入property。
     */
    public static void elementToProperty(Element element, String propertyPrefix, BeanDefinitionBuilder builder) {
        String propName = element.getLocalName();

        if (!isEmpty(propertyPrefix)) {
            propName = propertyPrefix + propName;
        }

        builder.addPropertyValue(propName, trimToNull(element.getTextContent()));
    }

    /**
     * 将attribute的值设入properties。
     */
    public static void attributesToProperties(Element element, BeanDefinitionBuilder builder, String... attrNames) {
        attributesToProperties(element, null, builder, attrNames);
    }

    /**
     * 将attribute的值设入properties。
     */
    public static void attributesToProperties(Element element, String propertyPrefix, BeanDefinitionBuilder builder,
                                              String... attrNames) {
        NamedNodeMap attrs = element.getAttributes();
        Set<String> attrNameSet = isEmptyArray(attrNames) ? null : createHashSet(attrNames);

        for (int i = 0; i < attrs.getLength(); i++) {
            Attr attr = (Attr) attrs.item(i);

            if (attrNameSet == null || attrNameSet.contains(attr.getNodeName())) {
                attributeToProperty(attr, propertyPrefix, builder);
            }
        }
    }

    /**
     * 将一个attribute的值设入properties。
     */
    public static void attributeToProperty(Attr attr, String propertyPrefix, BeanDefinitionBuilder builder) {
        String propName = attr.getNodeName();

        if (!isEmpty(propertyPrefix)) {
            propName = propertyPrefix + propName;
        }

        builder.addPropertyValue(propName, trimToNull(attr.getNodeValue()));
    }

    /**
     * 解析beans:bean的attributes。
     */
    public static void parseBeanDefinitionAttributes(Element element, ParserContext parserContext,
                                                     BeanDefinitionBuilder builder) {
        parserContext.getDelegate().parseBeanDefinitionAttributes(element, null, null, builder.getRawBeanDefinition());
    }

    /**
     * 从element创建指定configuration point的bean。如果element不属于该configuration
     * point的名字空间，则返回<code>null</code>。
     */
    public static BeanDefinitionHolder parseConfigurationPointBean(Element element, ConfigurationPoint cp,
                                                                   ParserContext parserContext,
                                                                   BeanDefinitionBuilder containingBeanBuilder) {
        assertNotNull(cp, "configurationPoint");

        BeanDefinitionParserDelegate delegate = parserContext.getDelegate();
        boolean isInnerBean = containingBeanBuilder != null;
        BeanDefinition containingBean = isInnerBean ? containingBeanBuilder.getRawBeanDefinition() : null;
        ElementSelector customSelector = ns(cp.getNamespaceUri());

        // 解析custom element。
        if (customSelector.accept(element)) {
            AbstractBeanDefinition bean = (AbstractBeanDefinition) delegate.parseCustomElement(element, containingBean);
            String beanName = trimToNull(element.getAttribute("id"));

            if (beanName == null) {
                beanName = BeanDefinitionReaderUtils.generateBeanName(bean, parserContext.getRegistry(), isInnerBean);
            }

            return new BeanDefinitionHolder(bean, beanName);
        }

        return null;
    }

    /**
     * 解析bean element。
     */
    public static Object parseBean(Element element, ParserContext parserContext,
                                   BeanDefinitionBuilder containingBeanBuilder) {
        return parseBean(element, parserContext,
                containingBeanBuilder == null ? null : containingBeanBuilder.getRawBeanDefinition());
    }

    /**
     * 解析bean element。
     */
    public static Object parseBean(Element element, ParserContext parserContext, BeanDefinition containingBean) {
        BeanDefinitionParserDelegate delegate = parserContext.getDelegate();
        String refName = trimToNull(element.getAttribute("ref"));

        // 如果是ref
        if (refName != null) {
            NamedBeanReference ref = new NamedBeanReference(refName, element.getAttribute("id"));
            ref.setSource(parserContext.extractSource(element));

            return ref;
        }

        // 如果是bean
        else {
            BeanDefinitionHolder beanHolder = delegate.parseBeanDefinitionElement(element, containingBean);

            if (beanHolder != null) {
                beanHolder = delegate.decorateBeanDefinitionIfRequired(element, beanHolder, containingBean);
            }

            return beanHolder;
        }
    }

    /**
     * 取得对象的beanName。如不支持，则返回<code>null</code>。
     */
    public static String getBeanName(Object bean) {
        if (bean instanceof BeanDefinitionHolder) {
            return ((BeanDefinitionHolder) bean).getBeanName();
        } else if (bean instanceof NamedBeanReference) {
            return ((NamedBeanReference) bean).getId();
        } else if (bean instanceof BeanReference) {
            return ((BeanReference) bean).getBeanName();
        } else {
            return null;
        }
    }

    /**
     * 根据baseName创建不重复的beanName。
     * <p>
     * 有别于 {@link BeanDefinitionReaderUtils.generateBeanName()}
     * 方法，这里使用指定的baseName，而不是使用类名作为baseName。
     * </p>
     */
    public static String generateBeanName(String baseName, BeanDefinitionRegistry registry) {
        return generateBeanName(baseName, registry, null, false);
    }

    /**
     * 根据baseName创建不重复的beanName。
     * <p>
     * 有别于 {@link BeanDefinitionReaderUtils.generateBeanName()}
     * 方法，这里使用指定的baseName，而不是使用类名作为baseName。
     * </p>
     * <p>
     * 如果是innerBean，则需要提供bean definition来生成id。
     * </p>
     */
    public static String generateBeanName(String baseName, BeanDefinitionRegistry registry, BeanDefinition definition,
                                          boolean isInnerBean) {
        baseName = assertNotNull(trimToNull(baseName), "baseName");
        String name;

        if (isInnerBean) {
            name = baseName + BeanDefinitionReaderUtils.GENERATED_BEAN_NAME_SEPARATOR
                    + ObjectUtils.getIdentityHexString(definition);
        } else {
            name = baseName;

            for (int i = 0; registry.containsBeanDefinition(name); i++) {
                name = baseName + BeanDefinitionReaderUtils.GENERATED_BEAN_NAME_SEPARATOR + i;
            }
        }

        return name;
    }

    /**
     * 添加一个constructor参数。
     */
    public static void addConstructorArg(BeanDefinitionBuilder builder, boolean required, Class<?> argType) {
        builder.addConstructorArgValue(createConstructorArg(builder.getRawBeanDefinition().getBeanClass(), required,
                argType));
    }

    /**
     * 添加一个constructor参数。
     * <p>
     * 假如未指定argTypes，则只能有一个constructor。
     * </p>
     */
    public static void addConstructorArg(BeanDefinitionBuilder builder, boolean required, int argIndex,
                                         Class<?>... argTypes) {
        builder.addConstructorArgValue(createConstructorArg(builder.getRawBeanDefinition().getBeanClass(), required,
                argIndex, argTypes));
    }

    /**
     * 本方法提供一个bean definition，可被<code>BeanDefinitionParser</code>
     * 调用，用来注入可选的constructor arg，并支持resolvableDependency对象以及optional选项。
     */
    public static BeanDefinition createConstructorArg(Class<?> beanType, boolean required, Class<?> argType) {
        return createConstructorArg(beanType, required, 0, argType);
    }

    /**
     * Spring有三种注入方法：
     * <ul>
     * <li>通过明确的bean definition声明来注入对象，缺点是无法注入
     * <code>ConfigurableListableBeanFactory.registerResolvableDependency()</code>
     * 中注册的对象，如<code>HttpServletRequest</code>。</li>
     * <li>通过autowire
     * byConstructor来注入对象，可以注入包括resolvableDependency在内的对象，缺点是无法将注入对象设成“optional”
     * ，即：对象不存在，则报错。</li>
     * <li>通过autowire
     * byName/byType来注入对象，相当于optional注入，缺点是不能注入包括resolvableDependency在内的对象
     * ，且会自动对所有property进行注入，无法精确控制，可能造成不确定情况。</li>
     * <li>通过<code>@Autowired</code>
     * annotation来注入对象，可以注入包括resolvableDependency在内的对象
     * ，也可以设置optional选项。缺点是有侵入性，必须修改bean class的代码。Springext试图将注入的多样性限定在
     * <code>BeanDefinitionParser</code>的范围中，因此使用annotation不是最好的方法。</li>
     * </ul>
     * <p>
     * 本方法提供一个bean definition，可被<code>BeanDefinitionParser</code>
     * 调用，用来注入到的constructor arg，并支持resolvableDependency对象以及optional选项。
     * </p>
     */
    public static BeanDefinition createConstructorArg(Class<?> beanType, boolean required, int argIndex,
                                                      Class<?>... argTypes) {
        assertNotNull(beanType, "beanType");
        argTypes = defaultIfNull(argTypes, EMPTY_CLASS_ARRAY);

        // 尝试取得constructor
        Constructor<?> constructor;

        if (isEmptyArray(argTypes)) {
            Constructor<?>[] constructors = beanType.getConstructors();

            assertTrue(constructors.length == 1, "%d constructors found, please specify argTypes", constructors.length);
            constructor = constructors[0];
            argTypes = constructor.getParameterTypes();
        } else {
            try {
                constructor = beanType.getConstructor(argTypes);
            } catch (NoSuchMethodException e) {
                throw new IllegalArgumentException("Could not find constructor", e);
            }
        }

        assertTrue(argIndex >= 0 && argIndex < argTypes.length, "argIndex is out of bound: %d", argIndex);

        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(ConstructorArg.class);

        builder.addConstructorArgValue(constructor);
        builder.addConstructorArgValue(argTypes[argIndex]);
        builder.addConstructorArgValue(argIndex);
        builder.addConstructorArgValue(required);

        return builder.getBeanDefinition();
    }

    /**
     * 添加一个bean引用，支持optional选项。
     */
    public static void addPropertyRef(BeanDefinitionBuilder builder, String propertyName, String beanName,
                                      Class<?> beanType, boolean required) {
        if (required) {
            builder.addPropertyReference(propertyName, beanName);
        } else {
            builder.addPropertyValue(propertyName, createOptionalPropertyRef(beanName, beanType));
        }
    }

    /**
     * 本方法提供一个bean definition，可被<code>BeanDefinitionParser</code>
     * 调用，用来注入到的property arg，不支持resolvableDependency对象，但支持optional注入。
     */
    public static BeanDefinition createOptionalPropertyRef(String beanName, Class<?> beanType) {
        beanName = assertNotNull(trimToNull(beanName), "beanName");
        assertNotNull(beanType, "beanType");

        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(OptionalPropertyRef.class);

        builder.addConstructorArgValue(beanName);
        builder.addConstructorArgValue(beanType);

        return builder.getBeanDefinition();
    }

    private final static DefaultInterceptor defaultInterceptor = new DefaultInterceptor();
    private final static ProxiedFilter proxiedFilter = new ProxiedFilter();

    /**
     * 创建指定interface的proxy，当proxy的方法被调用时，proxy将会从factory中取得实际对象，
     * 然后将调用delegate给实际对象来执行。
     */
    public static <T> T createProxy(final Class<T> intfs, final ObjectFactory factory) {
        return createProxy(intfs, null, factory);
    }

    /**
     * 创建指定interface的proxy，当proxy的方法被调用时，proxy将会从factory中取得实际对象，
     * 然后将调用delegate给实际对象来执行。
     */
    public static <T> T createProxy(Class<T> intfs, ClassLoader classLoader, final ObjectFactory factory) {
        assertNotNull(intfs, "no interface");
        assertNotNull(factory, "no ObjectFactory");

        try {
            intfs.getMethod("getObject");
            throw new IllegalArgumentException("Method name conflict: interface " + intfs.getName() + ".getObject()");
        } catch (NoSuchMethodException e) {
        }

        Enhancer generator = new Enhancer();

        generator.setClassLoader(classLoader);
        generator.setSuperclass(AbstractProxy.class);
        generator.setInterfaces(new Class<?>[] { intfs });

        generator.setCallbacks(new Callback[] { defaultInterceptor, new ProxiedInterceptor(factory) });
        generator.setCallbackFilter(proxiedFilter);
        generator.setNamingPolicy(new ProxiedNamingPolicy(intfs));

        return intfs.cast(generator.create(new Class<?>[] { Class.class, ObjectFactory.class }, new Object[] { intfs,
                factory }));
    }

    /**
     * 确保所得到的对象为代理对象。
     * <p>
     * <strong>注意</strong>，此方法可以接受<code>null</code>值。如果想确保非空，请结合
     * {@link Assert.assertNotNull}方法使用。
     * </p>
     */
    public static <T> T assertProxy(T object) {
        if (object != null) {
            assertTrue(object instanceof ObjectFactory,
                    "expects a proxy delegating to a real object, but got an object of type %s", object.getClass()
                            .getName());
        }

        return object;
    }

    /**
     * 取得proxy所指向的真实对象。
     */
    @SuppressWarnings("unchecked")
    public static <T> T getProxyTarget(T object) {
        if (object instanceof ObjectFactory) {
            try {
                return (T) ((ObjectFactory) object).getObject();
            } catch (Exception e) {
                log.warn("Could not get proxied object from ObjectFactory: {} {}", e.getClass().getSimpleName(),
                        e.getMessage());
                return null;
            }
        } else {
            return object;
        }
    }

    public static class ConstructorArg implements FactoryBean, BeanFactoryAware {
        private final Constructor<?> constructor;
        private final Class<?> argType;
        private final int argIndex;
        private final boolean required;
        private ConfigurableListableBeanFactory context;

        public ConstructorArg(Constructor<?> constructor, Class<?> argType, int argIndex, boolean required) {
            this.constructor = constructor;
            this.argType = argType;
            this.argIndex = argIndex;
            this.required = required;
        }

        public void setBeanFactory(BeanFactory context) throws BeansException {
            BeanFactory factory = null;

            if (context instanceof ApplicationContext) {
                factory = ((ApplicationContext) context).getAutowireCapableBeanFactory();
            } else {
                factory = context;
            }

            if (factory instanceof ConfigurableListableBeanFactory) {
                this.context = (ConfigurableListableBeanFactory) factory;
            }
        }

        public Class<?> getObjectType() {
            return argType;
        }

        public boolean isSingleton() {
            return false;
        }

        public Object getObject() {
            if (required && context == null) {
                throw new IllegalArgumentException("could not get object of " + argType.getName()
                        + ": no Application Context");
            }

            Object object = null;

            if (context != null) {
                DependencyDescriptor dd = new DependencyDescriptor(new MethodParameter(constructor, argIndex), required);
                object = context.resolveDependency(dd, null);
            }

            return object;
        }
    }

    public static class OptionalPropertyRef implements FactoryBean, BeanFactoryAware {
        private final String beanName;
        private final Class<?> beanType;
        private BeanFactory context;

        public OptionalPropertyRef(String beanName, Class<?> beanType) {
            this.beanName = beanName;
            this.beanType = beanType;
        }

        public void setBeanFactory(BeanFactory context) throws BeansException {
            this.context = context;
        }

        public Class<?> getObjectType() {
            return beanType;
        }

        public boolean isSingleton() {
            return false;
        }

        public Object getObject() {
            Object object = null;

            if (context != null) {
                try {
                    object = context.getBean(beanName);
                } catch (NoSuchBeanDefinitionException e) {
                    // ignore
                }
            }

            return object;
        }
    }

    /**
     * 携带id的bean ref。
     */
    private static class NamedBeanReference extends RuntimeBeanReference {
        private final String id;

        public NamedBeanReference(String beanName, String id) {
            this(beanName, id, false);
        }

        public NamedBeanReference(String beanName, String id, boolean toParent) {
            super(beanName, toParent);
            this.id = trimToNull(id);
        }

        public String getId() {
            return id == null ? getBeanName() : id;
        }

        @Override
        public String toString() {
            return '<' + (id == null ? "" : id + "=") + getBeanName() + '>';
        }
    }

    /**
     * 服务于createProxy()方法，Proxy的基类。
     */
    public static class AbstractProxy implements ObjectFactory {
        private final Class<?> intfs;
        private final ObjectFactory factory;

        public AbstractProxy(Class<?> intfs, ObjectFactory factory) {
            this.intfs = assertNotNull(intfs);
            this.factory = assertNotNull(factory, "objectFactory");
        }

        public Object getObject() throws BeansException {
            return factory.getObject();
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;

            result = prime * result + (factory == null ? 0 : factory.hashCode());

            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            if (obj == null) {
                return false;
            }

            if (getClass() != obj.getClass()) {
                return false;
            }

            AbstractProxy other = (AbstractProxy) obj;

            if (factory == null) {
                if (other.factory != null) {
                    return false;
                }
            } else if (!factory.equals(other.factory)) {
                return false;
            }

            return true;
        }

        @Override
        public String toString() {
            try {
                return factory.getObject().toString();
            } catch (Exception e) {
                return String.format("%s[%s: %s]", intfs.getSimpleName(), e.getClass().getSimpleName(), e.getMessage());
            }
        }
    }

    /**
     * 服务于createProxy()方法，将调用转发给proxy对象本身。
     */
    private static final class DefaultInterceptor implements MethodInterceptor {
        public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
            return proxy.invokeSuper(obj, args);
        }
    }

    /**
     * 服务于createProxy()方法，将调用委托给ObjectFactory所返回的对象。
     */
    private static final class ProxiedInterceptor implements MethodInterceptor {
        private final ObjectFactory factory;

        private ProxiedInterceptor(ObjectFactory factory) {
            this.factory = factory;
        }

        public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
            return proxy.invoke(factory.getObject(), args);
        }
    }

    /**
     * 服务于createProxy()方法，对于equals、hashCode、toString和ObjectFactory.getObject方法，
     * 执行defaultInterceptor，否则执行proxiedInterceptor。
     */
    private static final class ProxiedFilter implements CallbackFilter {
        public int accept(Method method) {
            if (isEqualsMethod(method) || isHashCodeMethod(method) || isToStringMethod(method)
                    || isObjectFactoryMethod(method)) {
                return 0; // invoke super
            } else {
                return 1; // invoke proxied object
            }
        }

        private boolean isObjectFactoryMethod(Method method) {
            return method != null && method.getName().equals("getObject") && method.getParameterTypes().length == 0;
        }
    }

    /**
     * 服务于createProxy()方法，返回以interface name作为前缀的proxy class name。
     */
    private static final class ProxiedNamingPolicy extends DefaultNamingPolicy {
        private final Class<?> intfs;

        private ProxiedNamingPolicy(Class<?> intfs) {
            this.intfs = intfs;
        }

        @Override
        public String getClassName(String prefix, String source, Object key, Predicate names) {
            if (AbstractProxy.class.getName().equals(prefix)) {
                prefix = intfs.getName();
            }

            return super.getClassName(prefix, source, key, names);
        }
    }
}
