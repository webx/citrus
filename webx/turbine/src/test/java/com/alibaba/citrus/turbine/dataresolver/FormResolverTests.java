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
package com.alibaba.citrus.turbine.dataresolver;

import static com.alibaba.citrus.test.TestUtil.*;
import static org.junit.Assert.*;

import org.junit.Test;

import com.alibaba.citrus.service.dataresolver.DataResolverContext;
import com.alibaba.citrus.service.form.Form;
import com.alibaba.citrus.turbine.dataresolver.impl.FormResolverFactory;

public class FormResolverTests extends AbstractDataResolverTests {
    private Form form;

    @Test
    public void nodeps() {
        FormResolverFactory resolverFactory = new FormResolverFactory(null);

        try {
            resolverFactory.getDataResolver(new DataResolverContext(String.class, null, null));
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("no FormService defined"));
        }
    }

    @Test
    public void getFormWrongType() throws Exception {
        try {
            execute("action", "form.myActionWrongType", "doGetForm", "_fm.m._0.f=&_fm.m._0.fi=");
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("Parameter type annotated with @FormData should be Form"));
        }
    }

    @Test
    public void getFormDefault() throws Exception {
        getFormDefault("doGetFormNoAnnotation");
        getFormDefault("doGetFormDefaultAnnotation");
    }

    private void getFormDefault(String event) throws Exception {
        // 默认值：skipIfInvalid=true

        // GET, empty form
        execute("action", "form.myAction", event, "");
        assertNull(newRequest.getAttribute("actionLog"));

        // GET, invalid
        execute("action", "form.myAction", event, "_fm.m._0.f=&_fm.m._0.fi=");
        assertNull(newRequest.getAttribute("actionLog"));

        // GET, valid
        execute("action", "form.myAction", event, "_fm.m._0.f=a&_fm.m._0.fi=b");
        form = (Form) newRequest.getAttribute("actionLog");
        assertNotNull(form);
        assertTrue(form.isValid());

        // GET, invalid, but screen不支持skip
        execute("screen", "form.myScreen", event, "_fm.m._0.f=&_fm.m._0.fi=");
        form = (Form) newRequest.getAttribute("screenLog");
        assertNotNull(form);
        assertFalse(form.isValid());
    }

    @Test
    public void getFormDontSkipAction() throws Exception {
        // skipIfInvalid=false

        // GET, invalid
        execute("action", "form.myAction", "doGetFormDontSkipAction", "_fm.m._0.f=&_fm.m._0.fi=");
        form = (Form) newRequest.getAttribute("actionLog");
        assertNotNull(form);
        assertFalse(form.isValid());

        // GET, valid
        execute("action", "form.myAction", "doGetFormDontSkipAction", "_fm.m._0.f=a&_fm.m._0.fi=b");
        form = (Form) newRequest.getAttribute("actionLog");
        assertNotNull(form);
        assertTrue(form.isValid());
    }
}
