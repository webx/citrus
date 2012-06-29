/*
 * Copyright 2012 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.citrus.service.moduleloader.impl.adapter;

import static com.alibaba.citrus.springext.util.SpringExtUtil.autowireAndInitialize;
import static com.alibaba.citrus.util.CollectionUtil.createHashMap;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;

import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.alibaba.citrus.service.moduleloader.Module;
import com.alibaba.citrus.service.moduleloader.ModuleInfo;
import com.alibaba.citrus.service.moduleloader.ModuleLoaderException;
import com.alibaba.citrus.util.StringUtil;

/**
 * 类似<code>ActionEventAdapterFactory</code>,区别地方是Handler的定位
 * 
 * @author qianchao 2012-6-29 上午10:52:26
 */
public class ScreenEventAdapterFactory extends AbstractDataBindingAdapterFactory implements ApplicationContextAware {

    private ApplicationContext context;

    @Override
    public Module adapt(String type, String name, Object moduleObject) throws ModuleLoaderException {
        if (StringUtil.indexOf(type, "screen") == -1) {
            return null;
        }
        Class<?> moduleClass = moduleObject.getClass();
        Map<String, Method> handlers = getEventHandlers(moduleClass);

        if (handlers == null) {
            return null;
        }

        ModuleInfo moduleInfo = new ModuleInfo(type, name);
        FastClass fc = FastClass.create(moduleClass);
        Map<String, MethodInvoker> fastHandlers = createHashMap(handlers.size());

        for (Map.Entry<String, Method> entry : handlers.entrySet()) {
            FastMethod fm = fc.getMethod(entry.getValue());
            fastHandlers.put(entry.getKey(), getMethodInvoker(fm, moduleInfo, false));
        }
        ScreenEventAdapter adapter = new ScreenEventAdapter(moduleObject, fastHandlers);
        try {
            // 注入并初始化adapter（不是注入moduleObject，后者取决于factory的设置）
            autowireAndInitialize(adapter, context, AbstractBeanDefinition.AUTOWIRE_AUTODETECT, type + "." + name);
        } catch (Exception e) {
            throw new ModuleLoaderException("Failed to configure module adapter", e);
        }

        return adapter;
    }

    private Map<String, Method> getEventHandlers(Class<?> moduleClass) {
        Map<String, Method> handlers = null;

        for (Method method : moduleClass.getMethods()) {
            if (checkMethod(method)) {
                String methodName = method.getName();

                if (handlers == null) {
                    handlers = createHashMap();
                }

                handlers.put(methodName, method);
            }
        }
        return handlers;
    }
    
    /**为了防止类似wait方法出现 */
    protected boolean checkMethod(Method method) {
        int modifiers = method.getModifiers();

        if (Modifier.isStatic(modifiers) || Modifier.isFinal(modifiers) || Modifier.isNative(modifiers)) {
            return false;
        }
        
        String methodName = method.getName();
        if("toString".equals(methodName) || "equals".equals(methodName)){
        	return false;
        }

        if (Modifier.isPublic(modifiers) || Modifier.isProtected(modifiers)) {
            return true;
        }

        return false;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;

    }

}
