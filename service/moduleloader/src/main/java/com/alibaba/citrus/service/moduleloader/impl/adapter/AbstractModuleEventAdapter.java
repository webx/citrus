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

package com.alibaba.citrus.service.moduleloader.impl.adapter;

import static com.alibaba.citrus.springext.util.SpringExtUtil.*;
import static com.alibaba.citrus.util.Assert.*;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;

import com.alibaba.citrus.service.moduleloader.ModuleEvent;
import com.alibaba.citrus.service.moduleloader.ModuleEventException;
import com.alibaba.citrus.service.moduleloader.ModuleEventNotFoundException;
import com.alibaba.citrus.util.ToStringBuilder;
import com.alibaba.citrus.util.ToStringBuilder.MapBuilder;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractModuleEventAdapter extends AbstractDataBindingAdapter
        implements InitializingBean, ModuleEvent {
    private final Map<String, MethodInvoker> handlers;
    private final MethodInvoker              preHandler;
    private final MethodInvoker              postHandler;

    @Autowired
    private HttpServletRequest request;

    protected AbstractModuleEventAdapter(Object moduleObject, Map<String, MethodInvoker> handlers, MethodInvoker preHandler, MethodInvoker postHandler) {
        super(moduleObject);
        this.handlers = assertNotNull(handlers, "handlers");
        this.preHandler = preHandler;
        this.postHandler = postHandler;

        assertTrue(!handlers.isEmpty(), "handlers");
    }

    public void afterPropertiesSet() {
        assertProxy(assertNotNull(request, "missing HttpServletRequest object"));
    }

    /** 执行一个module。 */
    public void execute() throws ModuleEventException, ModuleEventNotFoundException {
        executeAndReturn();
    }

    /** 执行一个module，并返回值。 */
    public Object executeAndReturn() throws ModuleEventException, ModuleEventNotFoundException {
        Object result = null;
        String event = getEventName(request);
        MethodInvoker handler = null;

        // 查找精确匹配的方法
        if (event != null) {
            handler = handlers.get(event);
        }

        // 查找fallback method
        if (handler == null) {
            handler = handlers.get(null);
        }

        // 未找到合适的handler method，报错
        if (handler == null) {
            throw new ModuleEventNotFoundException("Could not find handler method for event: " + event);
        }

        // 执行preHandler
        if (preHandler != null) {
            log.debug("Invoking pre-handler for event {}: {}", event, preHandler);

            try {
                preHandler.invoke(moduleObject, log);
            } catch (Exception e) {
                throw new ModuleEventException("Failed to execute pre-handler: " + preHandler, e);
            }
        }

        ModuleEventException exception = null;

        try {
            // 执行event handler
            log.debug("Invoking handler for event {}: {}", event, handler);

            try {
                result = handler.invoke(moduleObject, log);
            } catch (Exception e) {
                exception = new ModuleEventException("Failed to execute handler: " + handler, e);
            }
        } finally {
            // 执行postHandler
            if (postHandler != null) {
                log.debug("Invoking post-handler for event {}: {}", event, postHandler);

                try {
                    postHandler.invoke(moduleObject, log);
                } catch (Exception e) {
                    if (exception == null) {
                        exception = new ModuleEventException("Failed to execute post-handler: " + postHandler, e);
                    }
                }
            }
        }

        if (exception != null) {
            throw exception;
        }

        return result;
    }

    /** 取得event名称。 */
    protected abstract String getEventName(HttpServletRequest request);

    @Override
    public String toString() {
        MapBuilder mb = new MapBuilder();

        mb.append("moduleClass", moduleObject.getClass().getName());
        mb.append("handlers", new MapBuilder().appendAll(handlers).setPrintCount(true).setSortKeys(true));
        mb.append("preHandler", preHandler);
        mb.append("postHandler", postHandler);

        return new ToStringBuilder().append(getClass().getSimpleName()).append(mb).toString();
    }
}
