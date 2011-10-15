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
 */

package com.alibaba.citrus.service.form.impl.condition;

import static com.alibaba.citrus.test.TestUtil.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.SimpleTypeConverter;

import com.alibaba.citrus.expr.ExpressionParseException;
import com.alibaba.citrus.service.form.Field;
import com.alibaba.citrus.service.form.Form;
import com.alibaba.citrus.service.form.Group;
import com.alibaba.citrus.service.form.MessageContext;
import com.alibaba.citrus.service.form.Validator.Context;
import com.alibaba.citrus.service.form.impl.MessageContextFactory;

public class JexlConditionTests {
    private Context ctx;
    private Field field;
    private Group group;
    private Form form;
    private MessageContext mctx;

    @Before
    public void init() {
        ctx = createMock(Context.class);
        field = createMock(Field.class);
        group = createMock(Group.class);
        form = createMock(Form.class);
        mctx = MessageContextFactory.newInstance(form);

        expect(ctx.getField()).andReturn(field).anyTimes();
        expect(ctx.getMessageContext()).andReturn(mctx).anyTimes();
        expect(field.getGroup()).andReturn(group).anyTimes();
        expect(group.getForm()).andReturn(form).anyTimes();
        expect(form.getTypeConverter()).andReturn(new SimpleTypeConverter()).anyTimes();

        replay(ctx, field, group, form);
    }

    @Test
    public void init_expression() throws Exception {
        // empty expression
        JexlCondition cond = new JexlCondition(null);

        try {
            cond.afterPropertiesSet();
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("missing condition expression"));
        }

        cond = new JexlCondition("  ");

        try {
            cond.afterPropertiesSet();
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("missing condition expression"));
        }

        // wrong expression
        cond = new JexlCondition("${");

        try {
            cond.afterPropertiesSet();
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception(ExpressionParseException.class, "Invalid if condition: \"${\""));
        }

        // null expression
        cond = new JexlCondition("key");
        cond.afterPropertiesSet();

        assertFalse(cond.isSatisfied(ctx));

        // boolean expression
        mctx.put("key", true);

        cond = new JexlCondition("key");
        cond.afterPropertiesSet();

        assertTrue(cond.isSatisfied(ctx));

        // str expression
        mctx.put("key", "true");

        cond = new JexlCondition("key");
        cond.afterPropertiesSet();

        assertTrue(cond.isSatisfied(ctx));
    }
}
