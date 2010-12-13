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
package com.alibaba.citrus.service.form.impl;

import static com.alibaba.citrus.test.TestUtil.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.alibaba.citrus.service.form.AbstractFormServiceTests;
import com.alibaba.citrus.service.form.MessageContext;
import com.alibaba.citrus.service.form.impl.validation.MyValidator;
import com.alibaba.citrus.util.StringUtil;

public class MessageContextTests extends AbstractFormServiceTests {
    private FormImpl form;
    private GroupImpl group;
    private FieldImpl field1;
    private FieldImpl field2;

    @BeforeClass
    public static void initFactory() {
        factory = createContext("services-form.xml", true);
    }

    @Before
    public void init() throws Exception {
        invokePost(null);
        getFormService("form1");

        form = (FormImpl) formService.getForm();
        group = (GroupImpl) form.getGroup("group1");
        field1 = (FieldImpl) group.getField("field1");
        field2 = (FieldImpl) group.getField("field2");
    }

    @Test
    public void formMessageContext() throws Exception {
        MessageContext formContext = form.getMessageContext();

        assertGenericContext(formContext);
        assertFormContext(formContext);
    }

    @Test
    public void groupMessageContext() throws Exception {
        MessageContext groupContext = group.getMessageContext();
        assertSame(form.getMessageContext(), groupContext.getParentContext());

        assertGenericContext(groupContext);
        assertFormContext(groupContext);
        assertGroupContext(groupContext);
    }

    @Test
    public void fieldMessageContext() throws Exception {
        MessageContext fieldContext = field1.getMessageContext();
        assertSame(group.getMessageContext(), fieldContext.getParentContext());

        assertGenericContext(fieldContext);
        assertFormContext(fieldContext);
        assertGroupContext(fieldContext);
        assertFieldContext(fieldContext);
    }

    @Test
    public void validatorMessageContext() throws Exception {
        MessageContext validatorContext = MessageContextFactory.newInstance(field1, new MyValidator(true));

        assertSame(field1.getMessageContext(), validatorContext.getParentContext());
        assertGenericContext(validatorContext);
        assertFormContext(validatorContext);
        assertGroupContext(validatorContext);
        assertFieldContext(validatorContext);
        assertValidatorContext(validatorContext);
    }

    private void assertGenericContext(MessageContext ctx) {
        // get non-exist key
        assertEquals(null, ctx.get("notExist"));

        // put null
        for (MessageContext c = ctx; c != null; c = (MessageContext) c.getParentContext()) {
            c.put("tempKey", "tempValue");
            assertEquals("tempValue", c.get("tempKey"));
        }

        ctx.put("tempKey", null);

        for (MessageContext c = ctx; c != null; c = (MessageContext) c.getParentContext()) {
            assertEquals(null, c.get("tempKey"));
        }

        // put array
        ctx.put("myArray", new int[] { 1, 2, 3 });
        assertThat(ctx.get("myArray"), instanceOf(List.class));
        assertEquals("[1, 2, 3]", ctx.get("myArray").toString());
    }

    private void assertFormContext(MessageContext ctx) {
        // system props
        assertEquals(System.getProperty("java.home"), ctx.get("java.home"));

        // utils
        assertThat(ctx.get("stringUtil"), instanceOf(StringUtil.class));

        // any value
        ctx.put("testKey", "testValue");
        assertEquals("testValue", ctx.get("testKey"));

        // toString
        String str = ctx.toString();

        assertThat(str, containsString("FormMessageContext {"));
        assertThat(str, containsRegex("form\\s+= Form\\[groups: 3,"));
        assertThat(str, containsRegex("context\\s+= \\{"));
    }

    private void assertGroupContext(MessageContext ctx) {
        // fields
        assertSame(field1, ctx.get("field1"));
        assertSame(field2, ctx.get("field2"));

        assertNotNull(field1);
        assertNotNull(field2);
        assertNull(ctx.get("field3"));

        assertEquals("field1", field1.getName());
        assertEquals("field2", field2.getName());

        // form
        assertSame(form, ctx.get("form"));

        // toString
        String str = ctx.toString();

        assertThat(str, containsString("GroupMessageContext {"));
        assertThat(str, containsRegex("group\\s+= Group\\[name: group1._0, fields: 2,"));
        assertThat(str, containsRegex("context\\s+= \\{"));
    }

    @SuppressWarnings("unchecked")
    private void assertFieldContext(MessageContext ctx) {
        // field properties
        assertEquals("field1", ctx.get("name"));
        assertEquals("ÎÒµÄ×Ö¶Î1", ctx.get("displayName"));
        assertEquals("default1", ctx.get("value"));
        assertArrayEquals(new Object[] { "default1" }, ((List<Object>) ctx.get("values")).toArray());
        assertEquals("default1", ctx.get("defaultValue"));
        assertArrayEquals(new Object[] { "default1" }, ((List<Object>) ctx.get("defaultValues")).toArray());

        // group
        assertSame(group, ctx.get("group"));

        // toString
        String str = ctx.toString();

        assertThat(str, containsString("FieldMessageContext {"));
        assertThat(str, containsRegex("field\\s+= Field\\[group: group1._0, name: field1,"));
        assertThat(str, containsRegex("context\\s+= \\{"));
    }

    private void assertValidatorContext(MessageContext ctx) {
        // validator properties
        assertEquals(true, ctx.get("success"));

        // toString
        String str = ctx.toString();

        assertThat(str, containsString("ValidatorMessageContext {"));
        assertThat(str, containsRegex("validator\\s+= MyValidator\\[true\\]"));
        assertThat(str, containsRegex("context\\s+= \\{"));
    }
}
