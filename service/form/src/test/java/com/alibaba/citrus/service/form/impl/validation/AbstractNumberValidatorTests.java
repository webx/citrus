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

import static com.alibaba.citrus.service.form.support.NumberSupport.Type.*;
import static com.alibaba.citrus.test.TestUtil.*;
import static org.junit.Assert.*;

import org.junit.Test;

import com.alibaba.citrus.service.form.support.AbstractNumberValidator;

public abstract class AbstractNumberValidatorTests<V extends AbstractNumberValidator> extends AbstractValidatorTests<V> {
    @Test
    public void init_numberType() throws Exception {
        AbstractNumberValidator v = newValidatorFor_AbstractNumberValidatorTests();
        v.setMessage("message");

        // default value
        assertEquals(INT, v.getNumberType());

        // set null
        try {
            v.setNumberType(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("numberType"));
        }

        // set numberType
        v.setNumberType(LONG);
        assertEquals(LONG, v.getNumberType());
    }

    private V newValidatorFor_AbstractNumberValidatorTests() {
        V v = newValidator();
        initFor_AbstractNumberValidatorTests(v);
        return v;
    }

    /**
     * 预处理实例，以便通过<code>AbstractNumberValidatorTests</code>中的测试。
     */
    protected void initFor_AbstractNumberValidatorTests(V validator) {
    }
}
