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

import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;

/**
 * 边读数据边转换的Reader。
 * 
 * @author Michael Zhou
 */
public class CharConvertReader extends FilterReader {
    private CharConverter converter;

    public CharConvertReader(Reader in, String converterName) {
        this(in, CharConverter.getInstance(converterName));
    }

    public CharConvertReader(Reader in, CharConverter converter) {
        super(in);
        this.converter = converter;

        if (converter == null) {
            throw new NullPointerException("converter is null");
        }
    }

    @Override
    public int read() throws IOException {
        int ch = super.read();

        if (ch < 0) {
            return ch;
        }

        return converter.convert((char) ch);
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        int count = super.read(cbuf, off, len);

        if (count > 0) {
            converter.convert(cbuf, off, count);
        }

        return count;
    }
}
