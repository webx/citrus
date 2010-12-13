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

import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.nio.charset.Charset;
import java.util.Locale;

import com.alibaba.citrus.util.StringUtil;

/**
 * 代表一个locale信息。
 * 
 * @author Michael Zhou
 */
public final class LocaleInfo implements Cloneable, Externalizable {
    private static final long serialVersionUID = 3257847675461251635L;
    private Locale locale;
    private Charset charset;

    /**
     * 解析并创建locale信息。
     * 
     * @param name locale信息的字符串，包含locale和charset信息，以“:”分隔
     * @return locale信息
     */
    public static LocaleInfo parse(String name) {
        name = assertNotNull(trimToNull(name), "no locale name");

        int index = name.indexOf(":");
        String localePart = name;
        String charsetPart = null;

        if (index >= 0) {
            localePart = name.substring(0, index);
            charsetPart = name.substring(index + 1);
        }

        Locale locale = LocaleUtil.parseLocale(localePart);
        String charset = StringUtil.trimToNull(charsetPart);

        return new LocaleInfo(locale, charset);
    }

    /**
     * 创建系统默认的locale信息。
     */
    public LocaleInfo() {
        this.locale = assertNotNull(Locale.getDefault(), "system locale");
        this.charset = assertNotNull(Charset.defaultCharset(), "system charset");
    }

    /**
     * 创建locale信息。
     * 
     * @param locale 区域信息
     */
    public LocaleInfo(Locale locale) {
        this(locale, null, LocaleUtil.getDefault());
    }

    /**
     * 创建locale信息。
     * 
     * @param locale 区域信息
     * @param charset 编码字符集
     */
    public LocaleInfo(Locale locale, String charset) {
        this(locale, charset, LocaleUtil.getDefault());
    }

    /**
     * 创建locale信息。
     * 
     * @param locale 区域信息
     * @param charset 编码字符集
     * @param fallbackLocaleInfo 上一级locale信息，如果未提供locale和charset，则从上一级中取得。
     */
    LocaleInfo(Locale locale, String charset, LocaleInfo fallbackLocaleInfo) {
        assertNotNull(fallbackLocaleInfo, "fallbackLocaleInfo");
        charset = trimToNull(charset);

        if (locale == null) {
            locale = fallbackLocaleInfo.getLocale();

            if (charset == null) {
                charset = fallbackLocaleInfo.getCharset().name();
            }
        } else {
            if (charset == null) {
                charset = "UTF-8"; // 如果指定了locale，但未指定charset，则使用万能的UTF-8
            }
        }

        this.locale = locale;
        this.charset = Charset.forName(charset);
    }

    /**
     * 取得区域。
     * 
     * @return 区域
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * 取得编码字符集。
     * 
     * @return 编码字符集
     */
    public Charset getCharset() {
        return charset;
    }

    /**
     * 比较对象。
     * 
     * @param o 被比较的对象
     * @return 如果对象等效，则返回<code>true</code>
     */
    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }

        if (o == this) {
            return true;
        }

        if (!(o instanceof LocaleInfo)) {
            return false;
        }

        LocaleInfo otherLocaleInfo = (LocaleInfo) o;

        return locale.equals(otherLocaleInfo.locale) && charset.equals(otherLocaleInfo.charset);
    }

    @Override
    public int hashCode() {
        return charset.hashCode() * 31 + locale.hashCode();
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            unexpectedException(e);
            return null;
        }
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(toString());
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        LocaleInfo info = parse(in.readUTF());

        locale = info.getLocale();
        charset = info.getCharset();
    }

    @Override
    public String toString() {
        return locale + ":" + charset;
    }
}
