package com.alibaba.citrus.util.i18n;

import static com.alibaba.citrus.test.TestUtil.*;
import static org.junit.Assert.*;

import org.junit.Test;

public class UnknownCharsetTests {
    private UnknownCharset charset;

    @Test
    public void constructor() {
        try {
            new UnknownCharset(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("charset name"));
        }
    }

    @Test
    public void name() {
        charset = new UnknownCharset("test");
        assertEquals("test", charset.name());
    }

    @Test
    public void toString_() {
        charset = new UnknownCharset("test");
        assertEquals("test", charset.name());
    }

    @Test
    public void newEncoder() {
        charset = new UnknownCharset("test");

        try {
            charset.newEncoder();
            fail();
        } catch (UnsupportedOperationException e) {
            assertThat(e, exception("Could not create encoder for unknown charset: test"));
        }
    }

    @Test
    public void newDecoder() {
        charset = new UnknownCharset("test");

        try {
            charset.newDecoder();
            fail();
        } catch (UnsupportedOperationException e) {
            assertThat(e, exception("Could not create decoder for unknown charset: test"));
        }
    }
}
