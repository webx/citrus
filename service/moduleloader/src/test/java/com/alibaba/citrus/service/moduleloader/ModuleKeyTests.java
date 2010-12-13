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
package com.alibaba.citrus.service.moduleloader;

import static com.alibaba.citrus.test.TestUtil.*;
import static org.junit.Assert.*;

import org.junit.Test;

import com.alibaba.citrus.service.moduleloader.impl.ModuleKey;

public class ModuleKeyTests {
    private ModuleKey key;

    @Test
    public void emptyType() {
        try {
            new ModuleKey(null, null);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("moduleType"));
        }

        try {
            new ModuleKey(" ", null);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("moduleType"));
        }
    }

    @Test
    public void emptyName() {
        try {
            new ModuleKey("action", null);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("moduleName"));
        }

        try {
            new ModuleKey("action", "  ");
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("moduleName"));
        }
    }

    @Test
    public void normalize() {
        key = new ModuleKey(" action ", "  aaa_bbb/Ccc/dddEEE  ");

        assertEquals("action", key.getModuleType());
        assertEquals("aaaBbb.ccc.DddEee", key.getModuleName());
    }
}
