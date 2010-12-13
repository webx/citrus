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
package com.alibaba.citrus.service.requestcontext.util;

import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.BasicConstant.*;
import static com.alibaba.citrus.util.ObjectUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import com.alibaba.citrus.util.StringEscapeUtil;
import com.alibaba.citrus.util.StringUtil;
import com.alibaba.citrus.util.i18n.LocaleUtil;

/**
 * 解析和组装query string。
 * 
 * @author Michael Zhou
 */
public class QueryStringParser {
    private final String charset;
    private final StringBuilder queryStringBuffer;
    private String equalSign = "=";
    private String andSign = "&";

    public QueryStringParser() {
        this(null, null);
    }

    public QueryStringParser(String charset) {
        this(charset, null);
    }

    public QueryStringParser(String charset, String defaultCharset) {
        defaultCharset = defaultIfNull(trimToNull(defaultCharset), LocaleUtil.getContext().getCharset().name());

        this.charset = defaultIfNull(trimToNull(charset), defaultCharset);
        this.queryStringBuffer = new StringBuilder();
    }

    public String getCharacterEncoding() {
        return charset;
    }

    public String getEqualSign() {
        return equalSign;
    }

    public String getAndSign() {
        return andSign;
    }

    /**
     * 设置用来替代“=”的字符。
     */
    public QueryStringParser setEqualSign(String equalSign) {
        this.equalSign = defaultIfNull(equalSign, "=");
        return this;
    }

    /**
     * 设置用来替代“&”的字符。
     */
    public QueryStringParser setAndSign(String andSign) {
        this.andSign = defaultIfNull(andSign, "&");
        return null;
    }

    public QueryStringParser append(String key, String value) {
        try {
            key = StringEscapeUtil.escapeURL(defaultIfNull(key, EMPTY_STRING), charset);
            value = StringEscapeUtil.escapeURL(defaultIfNull(value, EMPTY_STRING), charset);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("invalid charset: " + charset, e);
        }

        if (queryStringBuffer.length() > 0) {
            queryStringBuffer.append(andSign);
        }

        queryStringBuffer.append(key).append(equalSign).append(value);

        return this;
    }

    public QueryStringParser append(Map<String, String> values) {
        for (Map.Entry<String, String> entry : values.entrySet()) {
            append(entry.getKey(), entry.getValue());
        }

        return this;
    }

    public String toQueryString() {
        String queryString = queryStringBuffer.toString();
        queryStringBuffer.setLength(0);
        return queryString.length() == 0 ? null : queryString;
    }

    /**
     * 解析query string。
     */
    public void parse(String queryString) {
        queryString = trimToNull(queryString);

        if (queryString == null) {
            return;
        }

        int startIndex = 0;
        int ampIndex = queryString.indexOf(andSign);

        while (ampIndex >= 0) {
            addKeyValue(queryString.substring(startIndex, ampIndex));

            startIndex = ampIndex + 1;
            ampIndex = queryString.indexOf(andSign, startIndex);
        }

        addKeyValue(queryString.substring(startIndex));
    }

    protected void add(String key, String value) {
        unsupportedOperation("You should extend class " + getClass().getSimpleName()
                + " and override method add(String, String)");
    }

    private void addKeyValue(String keyValue) {
        int index = keyValue.indexOf(equalSign);
        String key;
        String value;

        if (index < 0) {
            key = keyValue;
            value = null;
        } else {
            key = keyValue.substring(0, index).trim();
            value = keyValue.substring(index + 1).trim();
        }

        if (!StringUtil.isEmpty(key)) {
            key = decode(key);
            value = decode(value);

            add(key, defaultIfNull(value, EMPTY_STRING));
        }
    }

    private String decode(String str) {
        if (str != null) {
            try {
                // 对str进行URL decode，此时str中可能包含unicode字符，也可能包含bytes。
                str = StringEscapeUtil.unescapeURL(str, charset);

                StringBuilder buf = new StringBuilder();
                byte[] bytes = new byte[str.length()];

                int p = 0;
                for (int i = 0; i < str.length(); i++) {
                    int ch = str.charAt(i);

                    if ((ch & 0xFF) == ch) {
                        bytes[p++] = (byte) ch;
                    } else {
                        buf.append(new String(bytes, 0, p, charset));
                        buf.append((char) ch);
                        p = 0;
                    }
                }

                buf.append(new String(bytes, 0, p, charset));
                str = buf.toString();
            } catch (UnsupportedEncodingException e) {
                // ignore
            }
        }

        return str;
    }
}
