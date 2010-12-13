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
package com.alibaba.citrus.service.form.impl.validation.composite;

import static com.alibaba.citrus.test.TestUtil.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static org.easymock.EasyMock.*;
import static org.easymock.classextension.EasyMock.*;
import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import com.alibaba.citrus.service.form.Validator;
import com.alibaba.citrus.service.form.configuration.FieldConfig;
import com.alibaba.citrus.service.form.impl.validation.AbstractValidatorTests;
import com.alibaba.citrus.service.form.impl.validation.MyValidator;
import com.alibaba.citrus.service.form.support.AbstractCompositeValidator;

public abstract class AbstractCompositeValidatorTests<V extends AbstractCompositeValidator> extends
        AbstractValidatorTests<V> {
    @Test
    public void init_setValidators() {
        V v = newValidatorFor_AbstractCompositeValidatorTests();

        v.setValidators(createArrayList((Validator) new MyValidator(), new MyValidator(), new MyValidator()));

        assertEquals(3, v.getValidators().size());

        // unmodifiable
        try {
            v.getValidators().clear();
            fail();
        } catch (UnsupportedOperationException e) {
        }

        // set null list
        v.setValidators(null);
        assertEquals(0, v.getValidators().size());

        // set null validator
        try {
            v.setValidators(createArrayList((Validator) null));
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("validator"));
        }
    }

    @Test
    public void init_fieldConfig() throws Exception {
        V v = newValidatorFor_AbstractCompositeValidatorTests();
        FieldConfig fieldConfig = createMock(FieldConfig.class);
        Validator v1 = createMock(Validator.class);
        Validator v2 = createMock(Validator.class);
        Validator v3 = createMock(Validator.class);

        v1.init(fieldConfig);
        v2.init(fieldConfig);
        v3.init(fieldConfig);

        replay(fieldConfig, v1, v2, v3);

        v.setValidators(createArrayList(v1, v2, v3));

        v.init(fieldConfig);

        verify(fieldConfig, v1, v2, v3);
    }

    @Test
    public void init_clone() throws Exception {
        V v = newValidatorFor_AbstractCompositeValidatorTests();
        Validator v1 = createMock(Validator.class);
        Validator v2 = createMock(Validator.class);
        Validator v1copy = createMock(Validator.class);
        Validator v2copy = createMock(Validator.class);

        expect(v1.clone()).andReturn(v1copy);
        expect(v2.clone()).andReturn(v2copy);

        replay(v1, v2, v1copy, v2copy);

        v.setValidators(createArrayList(v1, v2));

        @SuppressWarnings("unchecked")
        V vcopy = (V) v.clone();
        List<Validator> validatorsCopy = vcopy.getValidators();

        assertArrayEquals(new Object[] { v1copy, v2copy }, validatorsCopy.toArray());
    }

    private V newValidatorFor_AbstractCompositeValidatorTests() {
        V v = newValidator();
        initFor_AbstractCompositeValidatorTests(v);
        return v;
    }

    /**
     * 预处理实例，以便通过<code>AbstractCompositeValidatorTests</code>中的测试。
     */
    protected void initFor_AbstractCompositeValidatorTests(V validator) {
        validator.setMessage("test");
    }

    @Override
    protected void initFor_AbstractValidatorTests(V validator) {
        Validator v1 = createMock(Validator.class);
        Validator v2 = createMock(Validator.class);
        Validator v3 = createMock(Validator.class);

        validator.setValidators(createArrayList(v1, v2, v3));
    }
}
