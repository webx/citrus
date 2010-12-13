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
package com.alibaba.citrus.service.form.impl.validation;

import static com.alibaba.citrus.generictype.TypeInfoUtil.*;
import static com.alibaba.citrus.test.TestUtil.*;
import static com.alibaba.citrus.util.ArrayUtil.*;
import static com.alibaba.citrus.util.BasicConstant.*;
import static org.easymock.classextension.EasyMock.*;
import static org.junit.Assert.*;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.alibaba.citrus.service.form.AbstractFormServiceTests;
import com.alibaba.citrus.service.form.Field;
import com.alibaba.citrus.service.form.Form;
import com.alibaba.citrus.service.form.Group;
import com.alibaba.citrus.service.form.configuration.FieldConfig;
import com.alibaba.citrus.service.form.configuration.GroupConfig;
import com.alibaba.citrus.service.form.support.AbstractOptionalValidator;
import com.alibaba.citrus.service.form.support.AbstractValidator;

public abstract class AbstractValidatorTests<V extends AbstractValidator> extends AbstractFormServiceTests {
    private final Class<?> validatorClass = resolveParameter(getClass(), AbstractValidatorTests.class, 0).getRawType();;
    protected Form form;
    protected Group group;
    protected Field field1;
    protected Field field2;
    protected Field field3;
    protected Field field4;
    protected Field field5;
    protected Field field6;
    protected Field field7;
    protected Field field8;
    protected Field field9;
    protected Field field10;

    protected abstract String getGroupName();

    @BeforeClass
    public static void initFactory() {
        factory = createContext("services-form-validators.xml");
    }

    @Before
    public void init() throws Exception {
        getFormService("form1");
    }

    @Test
    public void init_noMessage() throws Exception {
        V v = newValidatorFor_AbstractValidatorTests();
        boolean requiresMessage = invokeMethod(v, "requiresMessage", EMPTY_CLASS_ARRAY, EMPTY_OBJECT_ARRAY,
                Boolean.class);

        if (requiresMessage) {
            assertInitError(v, exception(IllegalArgumentException.class, "no message"));
        }
    }

    /**
     * 测试约定，field1不加required-validator。
     */
    @Test
    public void validate_optional_noValue() throws Exception {
        if (newValidatorFor_AbstractValidatorTests() instanceof AbstractOptionalValidator) {
            request("");

            assertEquals(true, field1.isValid());
            assertEquals(null, field1.getMessage());
        }
    }

    protected void request(Object... values) throws Exception {
        requestWithExtra(null, values);
    }

    protected void requestWithExtra(Object[][] extraValues, Object... values) throws Exception {
        request(extraValues, false, values);
    }

    protected void requestWithUpload(Object... values) throws Exception {
        request(null, true, values);
    }

    private void request(Object[][] extraValues, boolean mime, Object[] values) throws Exception {
        int extraCount;

        if (isEmptyArray(extraValues)) {
            extraCount = 1;
        } else {
            extraCount = extraValues.length + 1;

            for (Object[] pair : extraValues) {
                String key = (String) pair[0];

                if (key != null && !key.contains(".")) {
                    pair[0] = "_fm." + getGroupName() + "._0." + key;
                }
            }
        }

        Object[][] args = new Object[values.length + extraCount][];
        args[0] = new String[] { "sumbit", "提交" };

        if (extraCount > 1) {
            System.arraycopy(extraValues, 0, args, 1, extraValues.length);
        }

        for (int i = 0; i < values.length; i++) {
            String key = "_fm." + getGroupName() + "._0.";

            switch (i) {
                case 0:
                    key += "f";
                    break;

                case 1:
                    key += "fi";
                    break;

                case 2:
                    key += "fie";
                    break;

                case 3:
                    key += "fiel";
                    break;

                case 4:
                    key += "field";
                    break;

                default:
                    key += "field" + (i + 1);
                    break;
            }

            args[i + extraCount] = new Object[] { key, values[i] };
        }

        if (mime) {
            invokePostMime(args);
        } else {
            invokePost(args);
        }

        form = formService.getForm();
        group = form.getGroup(getGroupName());
        field1 = group.getField("field1");
        field2 = group.getField("field2");
        field3 = group.getField("field3");
        field4 = group.getField("field4");
        field5 = group.getField("field5");
        field6 = group.getField("field6");
        field7 = group.getField("field7");
        field8 = group.getField("field8");
        field9 = group.getField("field9");
        field10 = group.getField("field10");
    }

    @SuppressWarnings("unchecked")
    protected final V newValidator() {
        try {
            return (V) validatorClass.newInstance();
        } catch (Exception e) {
            fail(e.toString());
            return null;
        }
    }

    private V newValidatorFor_AbstractValidatorTests() {
        V v = newValidator();
        initFor_AbstractValidatorTests(v);
        return v;
    }

    /**
     * 预处理实例，以便通过<code>AbstractValidatorTests</code>中的测试。
     */
    protected void initFor_AbstractValidatorTests(V validator) {
    }

    protected final void assertInitError(V v, Matcher<Throwable> matcher) throws Exception {
        try {
            v.afterPropertiesSet();
            v.init(createMock(FieldConfig.class));
            fail();
        } catch (Throwable e) {
            assertThat(e, matcher);
        }
    }

    protected GroupConfig getGroupConfig() {
        return formService.getFormConfig().getGroupConfig(getGroupName());
    }
}
