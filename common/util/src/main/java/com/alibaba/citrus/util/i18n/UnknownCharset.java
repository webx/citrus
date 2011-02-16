package com.alibaba.citrus.util.i18n;

import static com.alibaba.citrus.util.Assert.*;

import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

/**
 * 代表一个不能识别的charset。
 * 
 * @author Michael Zhou
 */
public class UnknownCharset extends Charset {
    public UnknownCharset(String name) {
        super(assertNotNull(name, "charset name"), null);
    }

    @Override
    public boolean contains(Charset cs) {
        return false;
    }

    @Override
    public CharsetDecoder newDecoder() {
        unsupportedOperation("Could not create decoder for unknown charset: " + name());
        return null;
    }

    @Override
    public CharsetEncoder newEncoder() {
        unsupportedOperation("Could not create encoder for unknown charset: " + name());
        return null;
    }
}
