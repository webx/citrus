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
package com.alibaba.citrus.service.requestcontext.session.idgen.random.impl;

import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.ObjectUtil.*;

import java.security.SecureRandom;
import java.util.Random;

import org.apache.commons.codec.binary.Base64;

import com.alibaba.citrus.service.requestcontext.session.SessionIDGenerator;
import com.alibaba.citrus.springext.support.BeanSupport;
import com.alibaba.citrus.util.internal.ToStringBuilder;

/**
 * 用随机数生成session ID的机制。
 * 
 * @author Michael Zhou
 */
public class RandomIDGenerator extends BeanSupport implements SessionIDGenerator {
    private Integer length;
    private Random rnd;

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    @Override
    protected void init() {
        length = defaultIfNull(length, SESSION_ID_LENGTH_DEFAULT);

        try {
            rnd = new SecureRandom();
        } catch (Throwable e) {
            rnd = new Random();
        }
    }

    public String generateSessionID() {
        assertInitialized();

        byte[] bytes = new byte[(length + 3) / 4 * 3];

        rnd.nextBytes(bytes);

        byte[] b64Encoded = Base64.encodeBase64(bytes);
        StringBuilder buf = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            char ch = (char) b64Encoded[i];

            // 替换掉/和+，因为这两个字符在url中有特殊用处。
            switch (ch) {
                case '/':
                    ch = '$';
                    break;

                case '+':
                    ch = '-';
                    break;

                case '=':
                    unreachableCode();
            }

            buf.append(ch);
        }

        return buf.toString();
    }

    @Override
    public String toString() {
        return new ToStringBuilder().append("RandomSessionIDGenerator[length=").append(length).append("]").toString();
    }
}
