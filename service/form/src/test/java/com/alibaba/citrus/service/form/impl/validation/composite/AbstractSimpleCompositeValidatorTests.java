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
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

import com.alibaba.citrus.service.form.Validator;
import com.alibaba.citrus.service.form.impl.validation.MyValidator;
import com.alibaba.citrus.service.form.support.AbstractSimpleCompositeValidator;

public abstract class AbstractSimpleCompositeValidatorTests<V extends AbstractSimpleCompositeValidator> extends
        AbstractCompositeValidatorTests<V> {
    @Test
    public void init_hiddenAllOfValidator() throws Exception {
        // 2 validators
        V v = newValidatorFor_AbstractSimpleCompositeValidatorTests();
        v.setValidators(createArrayList((Validator) new MyValidator(), new MyValidator()));
        v.setMessage("message");
        v.afterPropertiesSet();

        assertEquals(1, v.getValidators().size());
        assertThat(v.getValidator(), instanceOf(AllOfValidator.class));
        assertSame(v.getValidator(), v.getValidators().get(0));

        // 1 validators
        MyValidator myValidator = new MyValidator();

        v = newValidatorFor_AbstractSimpleCompositeValidatorTests();
        v.setValidators(createArrayList((Validator) myValidator));
        v.setMessage("message");
        v.afterPropertiesSet();

        assertEquals(1, v.getValidators().size());
        assertSame(myValidator, v.getValidator());
        assertSame(v.getValidator(), v.getValidators().get(0));

        // no validators
        v = newValidatorFor_AbstractSimpleCompositeValidatorTests();
        v.setMessage("message");

        try {
            v.afterPropertiesSet();
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("no validators"));
        }
    }

    private V newValidatorFor_AbstractSimpleCompositeValidatorTests() {
        V v = newValidator();
        initFor_AbstractSimpleCompositeValidatorTests(v);
        return v;
    }

    /**
     * 预处理实例，以便通过<code>AbstractSimpleCompositeValidatorTests</code>中的测试。
     */
    protected void initFor_AbstractSimpleCompositeValidatorTests(V validator) {
    }
}
