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
package com.alibaba.citrus.service.velocity;

import static com.alibaba.citrus.test.TestUtil.*;
import static org.junit.Assert.*;

import java.util.Iterator;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.velocity.app.event.EventCartridge;
import org.apache.velocity.app.event.EventHandler;
import org.apache.velocity.app.event.MethodExceptionEventHandler;
import org.apache.velocity.app.event.ReferenceInsertionEventHandler;
import org.apache.velocity.context.Context;
import org.apache.velocity.runtime.RuntimeInstance;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.util.ContextAware;
import org.apache.velocity.util.RuntimeServicesAware;
import org.junit.Before;
import org.junit.Test;

import com.alibaba.citrus.service.velocity.impl.CloneableEventCartridge;

public class CloneableEventCartridgeTests {
    private RuntimeServices rsvc;
    private CloneableEventCartridge eventCartridge;
    private MyHandler handler;

    @Before
    public void init() throws Exception {
        ExtendedProperties config = new ExtendedProperties();

        rsvc = new RuntimeInstance();
        rsvc.setConfiguration(config);
        rsvc.init();

        eventCartridge = new CloneableEventCartridge();
        handler = new MyHandler();
        eventCartridge.addEventHandler(handler);
    }

    @Test
    public void initOnce() throws Exception {
        assertNull(handler.getRuntimeServices());

        eventCartridge.initOnce(rsvc);
        assertSame(rsvc, handler.getRuntimeServices());

        // 第二次初始化无效
        eventCartridge.initOnce(new RuntimeInstance());
        assertSame(rsvc, handler.getRuntimeServices());
    }

    @Test
    public void initialize() throws Exception {
        try {
            eventCartridge.initialize(rsvc);
            fail();
        } catch (IllegalStateException e) {
            assertThat(e, exception("not initialized yet"));
        }

        eventCartridge.initOnce(rsvc);
        eventCartridge.initialize(rsvc);
        assertSame(rsvc, handler.getRuntimeServices());
    }

    @Test
    public void addEventHandlers() {
        // 正常handler
        assertTrue(eventCartridge.addEventHandler(new MyHandler()));

        // not supported handler
        assertFalse(eventCartridge.addEventHandler(new NotSupportedContextAwareHandler()));

        // context aware but not cloneable
        try {
            eventCartridge.addEventHandler(new ContextAwareButNotCloneable());
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(
                    e,
                    exception("EventHandler which implements ContextAware "
                            + "should also implements FastCloneable or Cloneable: "
                            + ContextAwareButNotCloneable.class.getName()));
        }

        // context aware and fast cloneable
        assertTrue(eventCartridge.addEventHandler(new MyFastLocalHandler()));
    }

    @Test
    public void getRuntimeInstance_needNotClone() {
        eventCartridge.addEventHandler(new MyHandler());

        assertSame(eventCartridge, eventCartridge.getRuntimeInstance());

        Iterator<?> i = eventCartridge.getReferenceInsertionEventHandlers();

        assertSame(handler, i.next());
        i.next();
        assertFalse(i.hasNext());
    }

    @Test
    public void getRuntimeInstance_needsClone() {
        MyLocalHandler localHandler = new MyLocalHandler();
        MyFastLocalHandler localHandler2 = new MyFastLocalHandler();
        eventCartridge.addEventHandler(localHandler);
        eventCartridge.addEventHandler(localHandler2);

        // cloned instance
        EventCartridge ecruntime = eventCartridge.getRuntimeInstance();
        assertNotSame(eventCartridge, ecruntime);

        Iterator<?> i = ecruntime.getReferenceInsertionEventHandlers();

        // 1. same as handler
        assertSame(handler, i.next());

        // 2. cloned localHandler
        MyLocalHandler newLocalHandler = (MyLocalHandler) i.next();
        assertNotNull(newLocalHandler);
        assertNotSame(localHandler, newLocalHandler);

        // 3. fast cloned localHandler
        MyFastLocalHandler newLocalHandler2 = (MyFastLocalHandler) i.next();
        assertNotNull(newLocalHandler2);
        assertNotSame(localHandler2, newLocalHandler2);

        assertFalse(i.hasNext());

        // other handlers
        i = ecruntime.getMethodExceptionEventHandlers();

        // same as newLocalHandler
        assertSame(newLocalHandler, i.next());

        // same as newLocalHandler
        assertSame(newLocalHandler2, i.next());

        assertFalse(i.hasNext());
    }

    @Test
    public void getRuntimeInstance_cloneFailed() {
        eventCartridge.addEventHandler(new MyHandler());
        eventCartridge.addEventHandler(new CloneableButFailed());

        try {
            eventCartridge.getRuntimeInstance();
            fail();
        } catch (RuntimeException e) {
            assertThat(e, exception(UnsupportedOperationException.class, "failed to clone object"));
        }
    }

    @Test
    public void getRuntimeInstance_fastCloneFailed() {
        eventCartridge.addEventHandler(new MyHandler());
        eventCartridge.addEventHandler(new FastCloneableButFailed());

        try {
            eventCartridge.getRuntimeInstance();
            fail();
        } catch (RuntimeException e) {
            assertThat(e, exception(UnsupportedOperationException.class, "failed to copy object"));
        }
    }

    public static class MyHandler implements ReferenceInsertionEventHandler, RuntimeServicesAware {
        private RuntimeServices rs;

        public RuntimeServices getRuntimeServices() {
            return rs;
        }

        public void setRuntimeServices(RuntimeServices rs) {
            this.rs = rs;
        }

        public Object referenceInsert(String reference, Object value) {
            return value;
        }
    }

    public static class MyLocalHandler implements ReferenceInsertionEventHandler, MethodExceptionEventHandler,
            ContextAware, Cloneable {
        private Context context;

        public void setContext(Context context) {
            this.context = context;
        }

        public Object referenceInsert(String reference, Object value) {
            return String.valueOf(context.get("mark")) + value;
        }

        @SuppressWarnings("rawtypes")
        public Object methodException(Class claz, String method, Exception e) throws Exception {
            return e.getMessage();
        }

        @Override
        public Object clone() {
            try {
                return super.clone();
            } catch (CloneNotSupportedException e) {
                fail();
                return null;
            }
        }
    }

    public static class MyFastLocalHandler extends MyLocalHandler implements FastCloneable {
        public Object createCopy() {
            return super.clone();
        }
    }

    public static class NotSupportedContextAwareHandler implements ContextAware, EventHandler {
        public void setContext(Context context) {
        }
    }

    public static class ContextAwareButNotCloneable implements ContextAware, ReferenceInsertionEventHandler {
        public void setContext(Context context) {
        }

        public Object referenceInsert(String reference, Object value) {
            return null;
        }
    }

    public static class CloneableButFailed implements ContextAware, ReferenceInsertionEventHandler, Cloneable {
        public void setContext(Context context) {
        }

        public Object referenceInsert(String reference, Object value) {
            return null;
        }

        @Override
        public Object clone() {
            throw new UnsupportedOperationException("failed to clone object");
        }
    }

    public static class FastCloneableButFailed implements ContextAware, ReferenceInsertionEventHandler, FastCloneable {
        public void setContext(Context context) {
        }

        public Object referenceInsert(String reference, Object value) {
            return null;
        }

        public Object createCopy() {
            throw new UnsupportedOperationException("failed to copy object");
        }
    }
}
