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
package com.alibaba.citrus.service.requestcontext.session.encrypter;

import static com.alibaba.citrus.test.TestUtil.*;
import static org.junit.Assert.*;

import java.util.Queue;

import javax.crypto.Cipher;

import org.junit.Test;

public abstract class AbstractEncrypterTests<T extends AbstractJceEncrypter> {
    protected T encrypter;

    @Test
    public void poolSize() {
        assertEquals(256, encrypter.getPoolSize());

        encrypter.setPoolSize(10);
        assertEquals(10, encrypter.getPoolSize());
    }

    @Test
    public void pool() throws Exception {
        @SuppressWarnings("unchecked")
        Queue<Cipher> pool = getFieldValue(encrypter, "eciphers", Queue.class);

        Cipher[] ciphers = new Cipher[512];

        for (int i = 0; i < ciphers.length; i++) {
            ciphers[i] = encrypter.getCipher(Cipher.ENCRYPT_MODE);
        }

        assertEquals(0, pool.size());

        for (Cipher cipher : ciphers) {
            encrypter.returnCipher(Cipher.ENCRYPT_MODE, cipher);
        }

        assertEquals(256, pool.size());
        assertEquals(encrypter.getPoolSize(), pool.size());
    }

    @Test
    public void encrypt_decrypt() throws Exception {
        String text = "hello, world";

        // warm up
        byte[] cryptotext = encrypter.encrypt(text.getBytes("8859_1"));
        byte[] plaintext = encrypter.decrypt(cryptotext);

        long loop = 10000;
        long start = System.currentTimeMillis();

        for (int i = 0; i < loop; i++) {
            cryptotext = encrypter.encrypt(text.getBytes("8859_1"));
            plaintext = encrypter.decrypt(cryptotext);

            assertEquals(text, new String(plaintext, "8859_1"));
        }

        long duration = System.currentTimeMillis() - start;

        System.out
                .printf("%s: Encrypt-decrypt text %d times take %,d ms%n", getClass().getSimpleName(), loop, duration);
    }
}
