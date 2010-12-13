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
package com.alibaba.citrus.service.form.support;

import static com.alibaba.citrus.service.form.support.CompareOperator.*;
import static org.junit.Assert.*;

import org.junit.Test;

public class CompareOperatorTests {
    @Test
    public void equalTo() {
        assertEquals(true, equalTo.accept(1 - 1));
        assertEquals(false, equalTo.accept(1 - 2));
        assertEquals(false, equalTo.accept(2 - 1));
    }

    @Test
    public void notEqualTo() {
        assertEquals(true, notEqualTo.accept(1 - 2));
        assertEquals(true, notEqualTo.accept(2 - 1));
        assertEquals(false, notEqualTo.accept(1 - 1));
    }

    @Test
    public void lessThan() {
        assertEquals(true, lessThan.accept(1 - 2));
        assertEquals(false, lessThan.accept(2 - 1));
        assertEquals(false, lessThan.accept(1 - 1));
    }

    @Test
    public void greaterThan() {
        assertEquals(true, greaterThan.accept(2 - 1));
        assertEquals(false, greaterThan.accept(1 - 2));
        assertEquals(false, greaterThan.accept(1 - 1));
    }

    @Test
    public void lessThanOrEqualTo() {
        assertEquals(true, lessThanOrEqualTo.accept(1 - 2));
        assertEquals(true, lessThanOrEqualTo.accept(1 - 1));
        assertEquals(false, lessThanOrEqualTo.accept(2 - 1));
    }

    @Test
    public void greaterThanOrEqualTo() {
        assertEquals(true, greaterThanOrEqualTo.accept(2 - 1));
        assertEquals(true, greaterThanOrEqualTo.accept(1 - 1));
        assertEquals(false, greaterThanOrEqualTo.accept(1 - 2));
    }
}
