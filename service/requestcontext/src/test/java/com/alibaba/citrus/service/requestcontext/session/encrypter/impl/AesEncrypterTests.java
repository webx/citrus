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

import static com.alibaba.citrus.test.TestUtil.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.alibaba.citrus.service.requestcontext.session.encrypter.AbstractEncrypterTests;

public class AesEncrypterTests extends AbstractEncrypterTests<AesEncrypter> {
    @Before
    public void init() throws Exception {
        encrypter = new AesEncrypter();
        encrypter.setKey("0123456789abcdef");
        encrypter.afterPropertiesSet();
    }

    @Test
    public void noKey() throws Exception {
        encrypter = new AesEncrypter();

        try {
            encrypter.afterPropertiesSet();
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("no key"));
        }
    }

    @Test
    public void wrongKeySize() throws Exception {
        encrypter = new AesEncrypter();
        encrypter.setKey("0123456789abcdefg");

        try {
            encrypter.afterPropertiesSet();
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("Illegal key: expected size=128, actual size is 136"));
        }
    }
}
