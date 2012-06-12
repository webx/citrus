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

package com.alibaba.citrus.turbine.support;

import static com.alibaba.citrus.test.TestUtil.*;
import static org.junit.Assert.*;

import com.alibaba.citrus.turbine.AbstractWebxTests;
import com.alibaba.citrus.turbine.Context;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TurbineRunDataTests extends AbstractWebxTests {
    @BeforeClass
    public static void initWebx() throws Exception {
        prepareServlet();
    }

    @Before
    public void init() throws Exception {
        getInvocationContext("http://localhost/app1/1.html");
        initRequestContext();
    }

    @Test
    public void getAction() {
        assertNull(rundata.getAction());

        rundata.setAction(" test ");
        assertEquals("test", rundata.getAction());
    }

    @Test
    public void getActionEvent() {
        assertNull(rundata.getActionEvent());

        rundata.setActionEvent(" test ");
        assertEquals("test", rundata.getActionEvent());
    }

    @Test
    public void setRedirectTarget() {
        assertNull(rundata.getRedirectTarget());
        assertNull(rundata.getTarget());
        assertNull(rundata.getAction());

        // target=null, redirectTarget=null
        // target不变，所以action不清除
        rundata.setAction("myaction");
        rundata.setRedirectTarget(null);
        assertEquals("myaction", rundata.getAction());

        // target=null, redirectTarget=test
        rundata.setAction("myaction");
        rundata.setRedirectTarget("test");
        assertEquals(null, rundata.getAction());

        // target=test, redirectTarget=test
        // target不变，所以action不清除
        rundata.setAction("myaction");
        rundata.setTarget("test");
        rundata.setRedirectTarget("test");
        assertEquals("myaction", rundata.getAction());

        // target=test, redirectTarget=test
        rundata.setAction("myaction");
        rundata.setTarget("test");
        rundata.setRedirectTarget("test2");
        assertEquals(null, rundata.getAction());
    }

    @Test
    public void getContext() {
        Context context1 = rundata.getContext();
        Context context2 = rundata.getContext("app2");

        // getContext()取得当前component的context。
        assertSame(rundata.getContext("app1"), rundata.getContext());
        assertSame(rundata.getContext("app1"), context1);

        assertNotSame(context2, context1);
    }

    @Test
    public void getCurrentContext() {
        assertNull(rundata.getCurrentContext());

        try {
            rundata.popContext();
            fail();
        } catch (IllegalStateException e) {
            assertThat(e, exception("can't popContext without pushContext"));
        }

        Context context1 = rundata.getContext("app1");
        Context context2 = rundata.getContext("app2");

        try {
            rundata.pushContext(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("context"));
        }

        rundata.pushContext(context1);
        assertSame(context1, rundata.getCurrentContext());
        assertSame(context1, rundata.getCurrentContext());

        rundata.pushContext(context2);
        assertSame(context2, rundata.getCurrentContext());
        assertSame(context2, rundata.getCurrentContext());

        assertSame(context2, rundata.popContext());
        assertSame(context1, rundata.popContext());

        try {
            rundata.popContext();
            fail();
        } catch (IllegalStateException e) {
            assertThat(e, exception("can't popContext without pushContext"));
        }
    }

    @Test
    public void getControlTemplate() {
        assertNull(rundata.getControlTemplate());

        try {
            rundata.setControlTemplate("test");
            fail();
        } catch (IllegalStateException e) {
            assertThat(e, exception("can't setControlTemplate without pushContext"));
        }
    }

    @Test
    public void setLayout() {
        assertFalse(rundata.isLayoutEnabled());
        assertNull(rundata.getLayoutTemplateOverride());

        rundata.setLayoutEnabled(true);
        assertTrue(rundata.isLayoutEnabled());

        rundata.setLayoutEnabled(false);
        assertFalse(rundata.isLayoutEnabled());

        rundata.setLayout(" test ");
        assertTrue(rundata.isLayoutEnabled());
        assertEquals("test", rundata.getLayoutTemplateOverride());

        rundata.setLayout("  ");
        assertTrue(rundata.isLayoutEnabled());
        assertEquals(null, rundata.getLayoutTemplateOverride());

        rundata.setLayoutEnabled(false);
        assertFalse(rundata.isLayoutEnabled());

        rundata.setLayout(null);
        assertFalse(rundata.isLayoutEnabled());
        assertEquals(null, rundata.getLayoutTemplateOverride());
    }
}
