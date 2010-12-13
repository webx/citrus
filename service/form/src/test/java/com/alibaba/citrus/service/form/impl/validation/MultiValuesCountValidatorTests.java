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

import static org.junit.Assert.*;

import org.junit.Test;

public class MultiValuesCountValidatorTests extends AbstractValidatorTests<MultiValuesCountValidator> {
    @Override
    protected String getGroupName() {
        return "q";
    }

    @Test
    public void validator_emptyConfig() throws Exception {
        request((Object) new String[] { "a", "b", "c" });
        assertEquals(true, field1.isValid());
        assertEquals(null, field1.getMessage());

        request((Object) new String[] {});
        assertEquals(true, field1.isValid());
        assertEquals(null, field1.getMessage());
    }

    /**
     * minCount=2
     */
    @Test
    public void validator_minCount() throws Exception {
        request(null, new String[] { "a", "b", "c" });
        assertEquals(true, field2.isValid());
        assertEquals(null, field2.getMessage());

        request(null, new String[] { "a", "b" });
        assertEquals(true, field2.isValid());
        assertEquals(null, field2.getMessage());

        request(null, new String[] { "a" });
        assertEquals(false, field2.isValid());
        assertEquals("field2 should have values count between 2 and -1", field2.getMessage());

        request(null, new String[] {});
        assertEquals(false, field2.isValid());
        assertEquals("field2 should have values count between 2 and -1", field2.getMessage());
    }

    /**
     * maxCount=3
     */
    @Test
    public void validator_maxCount() throws Exception {
        request(null, null, new String[] { "a", "b", "c", "d" });
        assertEquals(false, field3.isValid());
        assertEquals("field3 should have values count between 0 and 3", field3.getMessage());

        request(null, null, new String[] { "a", "b", "c" });
        assertEquals(true, field3.isValid());
        assertEquals(null, field3.getMessage());

        request(null, null, new String[] { "a", "b" });
        assertEquals(true, field3.isValid());
        assertEquals(null, field3.getMessage());

        request(null, null, new String[] { "a" });
        assertEquals(true, field3.isValid());
        assertEquals(null, field3.getMessage());

        request(null, null, new String[] {});
        assertEquals(true, field3.isValid());
        assertEquals(null, field3.getMessage());
    }

    /**
     * minCount=2, maxCount=3
     */
    @Test
    public void validator_min_maxCount() throws Exception {
        request(null, null, null, new String[] { "a", "b", "c", "d" });
        assertEquals(false, field4.isValid());
        assertEquals("field4 should have values count between 2 and 3", field4.getMessage());

        request(null, null, null, new String[] { "a", "b", "c" });
        assertEquals(true, field4.isValid());
        assertEquals(null, field4.getMessage());

        request(null, null, null, new String[] { "a", "b" });
        assertEquals(true, field4.isValid());
        assertEquals(null, field4.getMessage());

        request(null, null, null, new String[] { "a" });
        assertEquals(false, field4.isValid());
        assertEquals("field4 should have values count between 2 and 3", field4.getMessage());

        request(null, null, null, new String[] {});
        assertEquals(false, field4.isValid());
        assertEquals("field4 should have values count between 2 and 3", field4.getMessage());
    }

    /**
     * minCount=0, maxCount=0
     */
    @Test
    public void validator_forceNoValues() throws Exception {
        request(null, null, null, null, new String[] { "a", "b", "c", "d" });
        assertEquals(false, field5.isValid());
        assertEquals("field5 should have values count between 0 and 0", field5.getMessage());

        request(null, null, null, null, new String[] { "a", "b", "c" });
        assertEquals(false, field5.isValid());
        assertEquals("field5 should have values count between 0 and 0", field5.getMessage());

        request(null, null, null, null, new String[] { "a", "b" });
        assertEquals(false, field5.isValid());
        assertEquals("field5 should have values count between 0 and 0", field5.getMessage());

        request(null, null, null, null, new String[] { "a" });
        assertEquals(false, field5.isValid());
        assertEquals("field5 should have values count between 0 and 0", field5.getMessage());

        request(null, null, null, null, new String[] {});
        assertEquals(true, field5.isValid());
        assertEquals(null, field5.getMessage());
    }
}
