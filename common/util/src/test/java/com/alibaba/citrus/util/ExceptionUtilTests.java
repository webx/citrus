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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

/**
 * ≤‚ ‘<code>ExceptionUtil</code>°£
 * 
 * @author Michael Zhou
 */
public class ExceptionUtilTests {
    @Test(expected = IllegalArgumentException.class)
    public void causedBy_causeTypeIsNull() {
        ExceptionUtil.causedBy(new Exception(), null);
    }

    @Test
    public void causedBy() {
        // null
        assertFalse(ExceptionUtil.causedBy(null, Exception.class));

        // 1 exception
        assertTrue(ExceptionUtil.causedBy(new IOException(), IOException.class));
        assertTrue(ExceptionUtil.causedBy(new IOException(), Exception.class));

        // 3 exceptions
        assertTrue(ExceptionUtil.causedBy(getException(), IOException.class));
        assertTrue(ExceptionUtil.causedBy(getException(), IllegalArgumentException.class));
        assertTrue(ExceptionUtil.causedBy(getException(), IllegalStateException.class));
        assertTrue(ExceptionUtil.causedBy(getException(), RuntimeException.class));
        assertTrue(ExceptionUtil.causedBy(getException(), Exception.class));

        assertFalse(ExceptionUtil.causedBy(getException(), IllegalAccessException.class));
    }

    @Test
    public void getCauses() {
        // null
        assertTrue(ExceptionUtil.getCauses(null).isEmpty());

        // 1 exception
        List<Throwable> es = ExceptionUtil.getCauses(new IOException());

        assertEquals(1, es.size());

        Iterator<Throwable> i = es.iterator();

        assertThat(i.next(), instanceOf(IOException.class));

        // 3 exceptions
        es = ExceptionUtil.getCauses(getException());

        assertEquals(3, es.size());

        i = es.iterator();

        assertThat(i.next(), instanceOf(IOException.class));
        assertThat(i.next(), instanceOf(IllegalArgumentException.class));
        assertThat(i.next(), instanceOf(IllegalStateException.class));

        // 3 exceptions
        es = ExceptionUtil.getCauses(getException(), false);

        assertEquals(3, es.size());

        i = es.iterator();

        assertThat(i.next(), instanceOf(IOException.class));
        assertThat(i.next(), instanceOf(IllegalArgumentException.class));
        assertThat(i.next(), instanceOf(IllegalStateException.class));
    }

    @Test
    public void getCauses_reversed() {
        // 3 exceptions, reversed
        List<Throwable> es = ExceptionUtil.getCauses(getException(), true);

        assertEquals(3, es.size());

        Iterator<Throwable> i = es.iterator();

        assertThat(i.next(), instanceOf(IllegalStateException.class));
        assertThat(i.next(), instanceOf(IllegalArgumentException.class));
        assertThat(i.next(), instanceOf(IOException.class));
    }

    private IOException getException() {
        IllegalStateException e1 = new IllegalStateException();
        IllegalArgumentException e2 = new IllegalArgumentException();
        IOException e3 = new IOException();

        e1.initCause(e3);
        e2.initCause(e1);
        e3.initCause(e2);

        return e3;
    }

    @Test
    public void toRuntimeException() {
        // null
        assertNull(ExceptionUtil.toRuntimeException(null));

        // wrong class
        assertEquals(RuntimeException.class,
                ExceptionUtil.toRuntimeException(new Exception(), PrivateRuntimeException.class).getClass());

        IllegalArgumentException iae = new IllegalArgumentException();
        IOException ioe = new IOException();

        assertSame(iae, ExceptionUtil.toRuntimeException(iae));
        assertTrue(ExceptionUtil.toRuntimeException(ioe) instanceof RuntimeException);
        assertSame(ioe, ExceptionUtil.toRuntimeException(ioe).getCause());
    }

    private class PrivateRuntimeException extends RuntimeException {
        private static final long serialVersionUID = -7903623389794106652L;

        private PrivateRuntimeException() {
        }
    }

    @Test
    public void toRuntimeException2() {
        IllegalArgumentException iae = new IllegalArgumentException();
        IOException ioe = new IOException();

        assertSame(iae, ExceptionUtil.toRuntimeException(iae, IllegalStateException.class));
        assertTrue(ExceptionUtil.toRuntimeException(ioe, IllegalStateException.class) instanceof IllegalStateException);
        assertSame(ioe, ExceptionUtil.toRuntimeException(ioe, IllegalStateException.class).getCause());
    }

    @Test
    public void getStackTrace() {
        Throwable e = new Throwable();
        String stacktrace = ExceptionUtil.getStackTrace(e);

        assertTrue(stacktrace.indexOf(Throwable.class.getName()) >= 0);
        assertTrue(stacktrace.indexOf(ExceptionUtilTests.class.getName() + ".getStackTrace") > 0);
    }
}
