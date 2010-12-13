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
package com.alibaba.citrus.service.requestcontext.session.encrypter.impl;

import static com.alibaba.citrus.springext.util.SpringExtUtil.*;
import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.StringUtil.*;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.alibaba.citrus.service.requestcontext.session.encrypter.AbstractJceEncrypter;
import com.alibaba.citrus.springext.support.parser.AbstractSingleBeanDefinitionParser;

public class AesEncrypter extends AbstractJceEncrypter {
    public final static String ALG_NAME = "AES";
    public final static int DEFAULT_KEY_SIZE = 128;
    private String key;
    private int keySize;
    private SecretKeySpec keySpec;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = trimToNull(key);
    }

    public int getKeySize() {
        return keySize <= 0 ? DEFAULT_KEY_SIZE : keySize;
    }

    public void setKeySize(int keySize) {
        this.keySize = keySize;
    }

    @Override
    protected void init() throws Exception {
        assertNotNull(key, "no key");

        byte[] raw = key.getBytes("UTF-8");
        int keySize = getKeySize();
        int actualKeySize = raw.length * 8;

        assertTrue(keySize == actualKeySize, "Illegal key: expected size=%d, actual size is %d", keySize, actualKeySize);

        keySpec = new SecretKeySpec(raw, ALG_NAME);
    }

    @Override
    protected Cipher createCipher(int mode) throws Exception {
        Cipher cipher = Cipher.getInstance(ALG_NAME);
        cipher.init(mode, keySpec);
        return cipher;
    }

    @Override
    public String toString() {
        return "AES(keySize=" + getKeySize() + ")";
    }

    public static class DefinitionParser extends AbstractSingleBeanDefinitionParser<AesEncrypter> {
        @Override
        protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
            attributesToProperties(element, builder, "keySize", "key");
        }
    }
}
