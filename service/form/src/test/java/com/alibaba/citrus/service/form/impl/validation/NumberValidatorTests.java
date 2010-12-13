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

import static com.alibaba.citrus.service.form.support.CompareOperator.*;
import static com.alibaba.citrus.service.form.support.NumberSupport.Type.*;
import static com.alibaba.citrus.test.TestUtil.*;
import static org.junit.Assert.*;

import org.junit.Test;

import com.alibaba.citrus.service.form.support.NumberSupport;
import com.alibaba.citrus.service.form.support.NumberSupport.Type;

public class NumberValidatorTests extends AbstractNumberValidatorTests<NumberValidator> {
    @Override
    protected String getGroupName() {
        return "d";
    }

    @Test
    public void init_operands() throws Exception {
        assertOperand(INT, "123.4", null);
        assertOperand(INT, "123", 123);
    }

    private void assertOperand(Type type, String value, Number result) throws Exception {
        for (int i = 0; i < 6; i++) {
            NumberValidator v = newValidator();
            v.setMessage("message");
            v.setNumberType(type);

            switch (i) {
                case 0:
                    v.setEqualTo(value);
                    break;

                case 1:
                    v.setNotEqualTo(value);
                    break;

                case 2:
                    v.setLessThan(value);
                    break;

                case 3:
                    v.setGreaterThan(value);
                    break;

                case 4:
                    v.setLessThanOrEqualTo(value);
                    break;

                case 5:
                    v.setGreaterThanOrEqualTo(value);
                    break;

                default:
                    fail();
            }

            if (result != null) {
                v.afterPropertiesSet();
                NumberSupport num = null;
                String numStr = null;

                switch (i) {
                    case 0:
                        num = v.getOperand(equalTo);
                        numStr = v.getEqualTo();
                        break;

                    case 1:
                        num = v.getOperand(notEqualTo);
                        numStr = v.getNotEqualTo();
                        break;

                    case 2:
                        num = v.getOperand(lessThan);
                        numStr = v.getLessThan();
                        break;

                    case 3:
                        num = v.getOperand(greaterThan);
                        numStr = v.getGreaterThan();
                        break;

                    case 4:
                        num = v.getOperand(lessThanOrEqualTo);
                        numStr = v.getLessThanOrEqualTo();
                        break;

                    case 5:
                        num = v.getOperand(greaterThanOrEqualTo);
                        numStr = v.getGreaterThanOrEqualTo();
                        break;

                    default:
                        fail();
                }

                assertEquals(result, num.getValue());
                assertEquals(result.toString(), numStr);
            } else {
                assertInitError(v, exception(NumberFormatException.class, value)); // For input string: value
            }
        }
    }

    @Test
    public void validate_equalTo() throws Exception {
        request("1");
        assertEquals(false, field1.isValid());
        assertEquals("field1 must equal to 123", field1.getMessage());

        request(" 123 ");
        assertEquals(true, field1.isValid());
        assertEquals(null, field1.getMessage());
    }

    @Test
    public void validate_notEqualTo() throws Exception {
        request(null, "123");
        assertEquals(false, field2.isValid());
        assertEquals("field2 must not equal to 123", field2.getMessage());

        request(null, " 1 ");
        assertEquals(true, field2.isValid());
        assertEquals(null, field2.getMessage());
    }

    @Test
    public void validate_lessThan() throws Exception {
        request(null, null, "123");
        assertEquals(false, field3.isValid());
        assertEquals("field3 must be less than 123", field3.getMessage());

        request(null, null, "234");
        assertEquals(false, field3.isValid());
        assertEquals("field3 must be less than 123", field3.getMessage());

        request(null, null, " 1 ");
        assertEquals(true, field3.isValid());
        assertEquals(null, field3.getMessage());
    }

    @Test
    public void validate_greaterThan() throws Exception {
        request(null, null, null, "123");
        assertEquals(false, field4.isValid());
        assertEquals("field4 must be greater than 123", field4.getMessage());

        request(null, null, null, "1");
        assertEquals(false, field4.isValid());
        assertEquals("field4 must be greater than 123", field4.getMessage());

        request(null, null, null, " 234 ");
        assertEquals(true, field4.isValid());
        assertEquals(null, field4.getMessage());
    }

    @Test
    public void validate_lessThanOrEqualTo() throws Exception {
        request(null, null, null, null, "234");
        assertEquals(false, field5.isValid());
        assertEquals("field5 must be less than or equal to 123", field5.getMessage());

        request(null, null, null, null, "123");
        assertEquals(true, field5.isValid());
        assertEquals(null, field5.getMessage());

        request(null, null, null, null, " 1 ");
        assertEquals(true, field5.isValid());
        assertEquals(null, field5.getMessage());
    }

    @Test
    public void validate_greaterThanOrEqualTo() throws Exception {
        request(null, null, null, null, null, "1");
        assertEquals(false, field6.isValid());
        assertEquals("field6 must be greater than or equal to 123", field6.getMessage());

        request(null, null, null, null, null, "123");
        assertEquals(true, field6.isValid());
        assertEquals(null, field6.getMessage());

        request(null, null, null, null, null, " 234 ");
        assertEquals(true, field6.isValid());
        assertEquals(null, field6.getMessage());
    }

    @Test
    public void validate_noLimit() throws Exception {
        request(null, null, null, null, null, null, "abc");
        assertEquals(false, field7.isValid());
        assertEquals("field7 must be of type INT", field7.getMessage());

        request(null, null, null, null, null, null, "123");
        assertEquals(true, field7.isValid());
        assertEquals(null, field7.getMessage());
    }

    @Test
    public void validate_combine() throws Exception {
        request(null, null, null, null, null, null, null, "100");
        assertEquals(false, field8.isValid());
        assertEquals("field8 must between 100 and 200", field8.getMessage());

        request(null, null, null, null, null, null, null, "123");
        assertEquals(true, field8.isValid());
        assertEquals(null, field8.getMessage());

        request(null, null, null, null, null, null, null, "200");
        assertEquals(true, field8.isValid());
        assertEquals(null, field8.getMessage());

        request(null, null, null, null, null, null, null, "201");
        assertEquals(false, field8.isValid());
        assertEquals("field8 must between 100 and 200", field8.getMessage());
    }
}
