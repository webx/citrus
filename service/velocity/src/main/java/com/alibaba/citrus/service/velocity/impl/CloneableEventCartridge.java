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
package com.alibaba.citrus.service.velocity.impl;

import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.Assert.ExceptionType.*;
import static com.alibaba.citrus.util.CollectionUtil.*;

import java.lang.reflect.Method;
import java.util.List;

import org.apache.velocity.app.event.EventCartridge;
import org.apache.velocity.app.event.EventHandler;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.util.ContextAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.citrus.service.template.TemplateException;
import com.alibaba.citrus.service.velocity.FastCloneable;

/**
 * 扩展<code>EventCartridge</code>，增加如下功能：
 * <ul>
 * <li>只在系统初始化的时候初始化一次。</li>
 * <li>假如event handler实现了<code>ContextAware</code>接口，则每次执行时复制一份。</li>
 * </ul>
 * 
 * @author Michael Zhou
 */
public class CloneableEventCartridge extends EventCartridge {
    private final static Logger log = LoggerFactory.getLogger(CloneableEventCartridge.class);
    private final static Method cloneMethod = getCloneMethod();
    private final List<EventHandler> allHandlers = createLinkedList();
    private boolean initialized;
    private boolean needsClone = false;

    private static Method getCloneMethod() {
        Method method = null;

        try {
            method = Object.class.getDeclaredMethod("clone");
            method.setAccessible(true);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return method;
    }

    /**
     * 在系统启动时初始化一次。
     */
    public void initOnce(RuntimeServices rs) throws Exception {
        if (!initialized) {
            super.initialize(rs);
            initialized = true;
        }
    }

    @Override
    public void initialize(RuntimeServices rs) throws Exception {
        assertTrue(initialized, ILLEGAL_STATE, "not initialized yet");
    }

    @Override
    public boolean addEventHandler(EventHandler ev) {
        boolean supported = super.addEventHandler(ev);

        if (supported) {
            allHandlers.add(ev);

            if (ev instanceof ContextAware) {
                needsClone = true;

                if (!(ev instanceof FastCloneable)) {
                    if (ev instanceof Cloneable) {
                        log.warn("EventHandler which implements ContextAware and Cloneable "
                                + "may slow down the velocity rendering process: {}", ev.getClass().getName());
                    } else {
                        throw new IllegalArgumentException(
                                "EventHandler which implements ContextAware should also implements FastCloneable or Cloneable: "
                                        + ev.getClass().getName());
                    }
                }
            }
        }

        return supported;
    }

    public EventCartridge getRuntimeInstance() {
        EventCartridge runtimeInstance = this;

        if (needsClone) {
            runtimeInstance = new EventCartridge();

            for (EventHandler ev : allHandlers) {
                if (ev instanceof ContextAware) {
                    if (ev instanceof FastCloneable) {
                        ev = (EventHandler) ((FastCloneable) ev).createCopy();
                    } else {
                        try {
                            ev = (EventHandler) cloneMethod.invoke(ev);
                        } catch (Exception e) {
                            throw new TemplateException("Could not clone a ContextAware event handler: "
                                    + ev.getClass().getName(), e);
                        }
                    }
                }

                runtimeInstance.addEventHandler(ev);
            }
        }

        return runtimeInstance;
    }
}
