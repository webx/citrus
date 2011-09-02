package com.alibaba.citrus.springext.support.context;

import static com.alibaba.citrus.util.Assert.*;

import java.lang.reflect.Method;
import java.util.Collection;

import org.springframework.context.ApplicationListener;
import org.springframework.context.support.AbstractApplicationContext;

/**
 * 由于spring3和spring2的api不完全兼容，导致在spring2上编译的context.getApplicationListeners()
 * 命令在spring3中报NoSuchMethodError。此类用反射的方法来解决兼容性问题。将来完全迁移到spring3以后，可以删除这个实现。
 * 
 * @author Michael Zhou
 */
class GetApplicationListeners {
    private final AbstractApplicationContext context;
    private final Method getApplicationListenersMethod;

    public GetApplicationListeners(AbstractApplicationContext context) {
        this.context = context;

        Method method = null;

        for (Class<?> clazz = context.getClass(); clazz != null; clazz = clazz.getSuperclass()) {
            try {
                method = clazz.getDeclaredMethod("getApplicationListeners");
                break;
            } catch (Exception e) {
            }
        }

        getApplicationListenersMethod = assertNotNull(method,
                "Could not call method: context.getApplicationListeners()");
    }

    public Collection<ApplicationListener> invoke() {
        try {
            @SuppressWarnings("unchecked")
            Collection<ApplicationListener> listeners = (Collection<ApplicationListener>) getApplicationListenersMethod
                    .invoke(context);

            return listeners;
        } catch (Exception e) {
            throw new RuntimeException("Could not call method: context.getApplicationListeners()", e);
        }
    }
}
