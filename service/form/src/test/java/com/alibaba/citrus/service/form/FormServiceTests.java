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
package com.alibaba.citrus.service.form;

import static com.alibaba.citrus.test.TestUtil.*;
import static org.easymock.classextension.EasyMock.*;
import static org.junit.Assert.*;

import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import com.alibaba.citrus.service.form.impl.FormServiceImpl;

/**
 * 测试form service配置及功能。
 * 
 * @author Michael Zhou
 */
public class FormServiceTests extends AbstractFormServiceTests {
    private Form form;
    private Group group;

    @BeforeClass
    public static void initFactory() {
        factory = createContext("services-form.xml", true);
    }

    @Before
    public void init() {
        getFormService("form1");
    }

    @Test
    public void initWithoutRequest() {
        try {
            createContext("services-form-no-request.xml");
            fail();
        } catch (BeanCreationException e) {
            assertThat(e, exception(NoSuchBeanDefinitionException.class, "HttpServletRequest"));
        }
    }

    @Test
    public void init_requestIsNotProxy() {
        try {
            new FormServiceImpl(createMock(HttpServletRequest.class));
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("expects a proxy delegating to a real object, but got an object of type "));
        }
    }

    @Test
    public void getFormConfig() {
        assertNotNull(formService.getFormConfig());
        assertSame(formService, formService.getFormConfig().getFormService());
    }

    @Test
    public void getForm_twice() throws Exception {
        invokeGet(null);
        form = formService.getForm();

        assertNotNull(form);
        assertSame(form, request.getAttribute("_FormService_form1_" + System.identityHashCode(formService)));
        assertSame(form, formService.getForm());
    }

    @Test
    public void getForm_no_args() throws Exception {
        invokeGet(null);

        form = formService.getForm();
        assertEquals(true, form.isValid());
        assertEquals(0, form.getGroups().size());
    }

    @Test
    public void getForm_notPostOnly() throws Exception {
        Object[][] args = new Object[][] { { "sumbit", "提交" }, //
                { "_fm.g._0.f", "aaa" }, // group1.field1
                { "_fm.g._0.fi", "bbb" }, // group1.field2
        };

        // 四种组合：GET/POST, getForm()/getForm(false)
        for (int i = 0; i < 4; i++) {
            if (i % 2 == 0) {
                invokeGet(args);
            } else {
                invokePost(args);
            }

            if (i < 3) {
                form = formService.getForm();
            } else {
                form = formService.getForm(false);
            }

            assertEquals(false, form.isForcePostOnly());
            assertEquals(true, form.isValid());
            assertEquals(1, form.getGroups().size());

            group = form.getGroup("group1");

            assertEquals(true, group.isValid());
            assertEquals("aaa", group.getField("field1").getStringValue());
            assertEquals("bbb", group.getField("field2").getStringValue());
        }
    }

    @Test
    public void getForm_forcePostOnly() throws Exception {
        Object[][] args = new Object[][] { { "sumbit", "提交" }, //
                { "_fm.g._0.f", "aaa" }, // group1.field1
                { "_fm.g._0.fi", "bbb" }, // group1.field2
        };

        // GET
        invokeGet(args);
        form = formService.getForm(true);

        assertEquals(false, form.isValid());
        assertEquals(0, form.getGroups().size());

        // POST
        invokePost(args);
        form = formService.getForm(true);

        assertEquals(true, form.isForcePostOnly());
        assertEquals(true, form.isValid());
        assertEquals(1, form.getGroups().size());

        group = form.getGroup("group1");

        assertEquals(true, group.isValid());
        assertEquals("aaa", group.getField("field1").getStringValue());
        assertEquals("bbb", group.getField("field2").getStringValue());
    }

    @Test
    public void getForm_postOnly() throws Exception {
        Object[][] args = new Object[][] { { "sumbit", "提交" }, //
                { "_fm.gr._0.f", "aaa" }, // group2.field1
                { "_fm.gr._0.fi", "bbb" }, // group2.field2
        };

        // GET
        invokeGet(args);
        form = formService.getForm();

        assertEquals(false, form.isValid());
        assertEquals(0, form.getGroups().size());

        // POST
        invokePost(args);
        form = formService.getForm();

        assertEquals(true, form.isValid());
        assertEquals(1, form.getGroups().size());

        group = form.getGroup("group2");

        assertEquals(true, group.getGroupConfig().isPostOnly());
        assertEquals(true, group.isValid());
        assertEquals("aaa", group.getField("field1").getStringValue());
        assertEquals("bbb", group.getField("field2").getStringValue());
    }

    @Test
    public void toString_() throws Exception {
        String str = "";
        str += "form1:FormService {\n";
        str += "  FormConfig[groups: 3]\n";
        str += "}";

        assertEquals(str, formService.toString());
    }
}
