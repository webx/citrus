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
package com.alibaba.citrus.util;

import static com.alibaba.citrus.test.TestUtil.*;
import static org.junit.Assert.*;

import org.junit.Test;

/**
 * ≤‚ ‘<code>Assert</code>¿‡°£
 * 
 * @author Michael Zhou
 */
public class AssertTests {
    private static final String DEFAULT_HEADER = "[Assertion";
    private static final String MESSAGE = "test message %d";
    private static final String MESSAGE_1 = "test message 1";
    private static final Object OBJECT = new Object();

    @Test
    public void _assertNotNull() {
        // form 1
        Assert.assertNotNull(OBJECT);

        try {
            Assert.assertNotNull(null);
        } catch (IllegalArgumentException e) {
            assertThat(e, exception(IllegalArgumentException.class, DEFAULT_HEADER, "must not be null"));
        }

        // form 2
        Assert.assertNotNull(OBJECT, MESSAGE);

        try {
            Assert.assertNotNull(null, MESSAGE);
        } catch (IllegalArgumentException e) {
            assertThat(e, exception(IllegalArgumentException.class, MESSAGE));
        }

        try {
            Assert.assertNotNull(null, MESSAGE, 1);
        } catch (IllegalArgumentException e) {
            assertThat(e, exception(IllegalArgumentException.class, MESSAGE_1));
        }

        // form 3
        Assert.assertNotNull(OBJECT, Assert.ExceptionType.ILLEGAL_STATE, MESSAGE);

        try {
            Assert.assertNotNull(null, Assert.ExceptionType.ILLEGAL_STATE, MESSAGE);
        } catch (IllegalStateException e) {
            assertThat(e, exception(IllegalStateException.class, MESSAGE));
        }

        try {
            Assert.assertNotNull(null, Assert.ExceptionType.ILLEGAL_STATE, MESSAGE, 1);
        } catch (IllegalStateException e) {
            assertThat(e, exception(IllegalStateException.class, MESSAGE_1));
        }
    }

    @Test
    public void _assertNull() {
        // form 1
        Assert.assertNull(null);

        try {
            Assert.assertNull(OBJECT);
        } catch (IllegalArgumentException e) {
            assertThat(e, exception(IllegalArgumentException.class, DEFAULT_HEADER, "must be null"));
        }

        // form 2
        Assert.assertNull(null, MESSAGE);

        try {
            Assert.assertNull(OBJECT, MESSAGE);
        } catch (IllegalArgumentException e) {
            assertThat(e, exception(IllegalArgumentException.class, MESSAGE));
        }

        try {
            Assert.assertNull(OBJECT, MESSAGE, 1);
        } catch (IllegalArgumentException e) {
            assertThat(e, exception(IllegalArgumentException.class, MESSAGE_1));
        }

        // form 3
        Assert.assertNull(null, Assert.ExceptionType.ILLEGAL_STATE, MESSAGE);

        try {
            Assert.assertNull(OBJECT, Assert.ExceptionType.ILLEGAL_STATE, MESSAGE);
        } catch (IllegalStateException e) {
            assertThat(e, exception(IllegalStateException.class, MESSAGE));
        }

        try {
            Assert.assertNull(OBJECT, Assert.ExceptionType.ILLEGAL_STATE, MESSAGE, 1);
        } catch (IllegalStateException e) {
            assertThat(e, exception(IllegalStateException.class, MESSAGE_1));
        }
    }

    @Test
    public void _assertTrue() {
        // form 1
        Assert.assertTrue(true);

        try {
            Assert.assertTrue(false);
        } catch (IllegalArgumentException e) {
            assertThat(e, exception(IllegalArgumentException.class, DEFAULT_HEADER, "must be true"));
        }

        // form 2
        Assert.assertTrue(true, MESSAGE);

        try {
            Assert.assertTrue(false, MESSAGE);
        } catch (IllegalArgumentException e) {
            assertThat(e, exception(IllegalArgumentException.class, MESSAGE));
        }

        try {
            Assert.assertTrue(false, MESSAGE, 1);
        } catch (IllegalArgumentException e) {
            assertThat(e, exception(IllegalArgumentException.class, MESSAGE_1));
        }

        // form 3
        Assert.assertTrue(true, Assert.ExceptionType.ILLEGAL_STATE, MESSAGE);

        try {
            Assert.assertTrue(false, Assert.ExceptionType.ILLEGAL_STATE, MESSAGE);
        } catch (IllegalStateException e) {
            assertThat(e, exception(IllegalStateException.class, MESSAGE));
        }

        try {
            Assert.assertTrue(false, Assert.ExceptionType.ILLEGAL_STATE, MESSAGE, 1);
        } catch (IllegalStateException e) {
            assertThat(e, exception(IllegalStateException.class, MESSAGE_1));
        }
    }

    @Test
    public void _unreachableCode() {
        try {
            Assert.unreachableCode();
        } catch (UnreachableCodeException e) {
            assertThat(e, exception(UnreachableCodeException.class, DEFAULT_HEADER, "unreachable"));
        }

        try {
            Assert.unreachableCode(MESSAGE);
        } catch (UnreachableCodeException e) {
            assertThat(e, exception(UnreachableCodeException.class, MESSAGE));
        }

        try {
            Assert.unreachableCode(MESSAGE, 1);
        } catch (UnreachableCodeException e) {
            assertThat(e, exception(UnreachableCodeException.class, MESSAGE_1));
        }
    }

    @Test
    public void _unexpectedException() {
        final Throwable e = new Throwable();

        try {
            Assert.unexpectedException(e);
        } catch (UnexpectedFailureException ee) {
            assertThat(ee, exception(UnexpectedFailureException.class, DEFAULT_HEADER, "unexpected"));
        }

        try {
            Assert.unexpectedException(e, MESSAGE);
        } catch (UnexpectedFailureException ee) {
            assertThat(ee, exception(UnexpectedFailureException.class, MESSAGE));
        }

        try {
            Assert.unexpectedException(e, MESSAGE, 1);
        } catch (UnexpectedFailureException ee) {
            assertThat(ee, exception(UnexpectedFailureException.class, MESSAGE_1));
        }

        try {
            Assert.unexpectedException(e);
            fail();
        } catch (UnexpectedFailureException ee) {
            assertSame(e, ee.getCause());
        }

        try {
            Assert.unexpectedException(e, MESSAGE);
            fail();
        } catch (UnexpectedFailureException ee) {
            assertSame(e, ee.getCause());
        }
    }

    @Test
    public void _fail() {
        try {
            Assert.fail();
        } catch (UnexpectedFailureException e) {
            assertThat(e, exception(UnexpectedFailureException.class, DEFAULT_HEADER, "unexpected"));
        }

        try {
            Assert.fail(MESSAGE);
        } catch (UnexpectedFailureException e) {
            assertThat(e, exception(UnexpectedFailureException.class, MESSAGE));
        }

        try {
            Assert.fail(MESSAGE, 1);
        } catch (UnexpectedFailureException e) {
            assertThat(e, exception(UnexpectedFailureException.class, MESSAGE_1));
        }
    }

    @Test
    public void _unsupportedOperation() {
        try {
            Assert.unsupportedOperation();
        } catch (UnsupportedOperationException e) {
            assertThat(e, exception(UnsupportedOperationException.class, DEFAULT_HEADER, "unsupported"));
        }

        try {
            Assert.unsupportedOperation(MESSAGE);
        } catch (UnsupportedOperationException e) {
            assertThat(e, exception(UnsupportedOperationException.class, MESSAGE));
        }

        try {
            Assert.unsupportedOperation(MESSAGE, 1);
        } catch (UnsupportedOperationException e) {
            assertThat(e, exception(UnsupportedOperationException.class, MESSAGE_1));
        }
    }
}
