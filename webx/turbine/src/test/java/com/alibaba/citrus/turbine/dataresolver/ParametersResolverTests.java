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
package com.alibaba.citrus.turbine.dataresolver;

import static com.alibaba.citrus.test.TestUtil.*;
import static org.junit.Assert.*;

import org.junit.Test;
import org.springframework.beans.TypeMismatchException;

import com.alibaba.citrus.service.moduleloader.ActionEventException;
import com.alibaba.test2.module.action.params.MyAction;
import com.alibaba.test2.module.action.params.MyAction.MyData;

public class ParametersResolverTests extends AbstractDataResolverTests {
    @Test
    public void setData() throws Exception {
        execute("action", "params.myAction", "doSetData", "names=hello&names=world&value=123&otherValue=456");

        MyAction.MyData data = (MyData) newRequest.getAttribute("actionLog");

        assertArrayEquals(new Object[] { "hello", "world" }, data.getNames().toArray());
        assertEquals(123, data.getValue());
    }

    @Test
    public void setData_convertFailed() throws Exception {
        try {
            execute("action", "params.myAction", "doSetData", "names=hello&names=world&value=notInteger&otherValue=456");
            fail();
        } catch (ActionEventException e) {
            assertThat(e, exception(TypeMismatchException.class, "notInteger"));
        }
    }

    @Test
    public void setData_abstract() throws Exception {
        try {
            execute("action", "params.myActionAbstract", "doSetData", "");
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("Class to set properties should be public and concrete: "));
        }
    }
}
