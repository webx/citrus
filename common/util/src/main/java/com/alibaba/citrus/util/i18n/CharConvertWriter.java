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
package com.alibaba.citrus.util.i18n;

import java.io.FilterWriter;
import java.io.IOException;
import java.io.Writer;

public class CharConvertWriter extends FilterWriter {
    private CharConverter converter;

    public CharConvertWriter(Writer out, String converterName) {
        this(out, CharConverter.getInstance(converterName));
    }

    public CharConvertWriter(Writer out, CharConverter converter) {
        super(out);
        this.converter = converter;

        if (converter == null) {
            throw new NullPointerException("converter is null");
        }
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        char[] newbuf = new char[len];

        System.arraycopy(cbuf, off, newbuf, 0, len);

        converter.convert(newbuf, 0, len);

        super.write(newbuf, 0, len);
    }

    @Override
    public void write(int c) throws IOException {
        super.write(converter.convert((char) c));
    }

    @Override
    public void write(String str, int off, int len) throws IOException {
        super.write(converter.convert(str, off, len), 0, len);
    }
}
