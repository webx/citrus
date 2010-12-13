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

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.crypto.Cipher;

import com.alibaba.citrus.service.requestcontext.session.encoder.SessionEncoderException;
import com.alibaba.citrus.springext.support.BeanSupport;

public abstract class AbstractJceEncrypter extends BeanSupport implements Encrypter {
    public static final int DEFAULT_POOL_SIZE = 256;
    private final Queue<Cipher> eciphers = new ConcurrentLinkedQueue<Cipher>();
    private final Queue<Cipher> dciphers = new ConcurrentLinkedQueue<Cipher>();
    private int poolSize;

    public int getPoolSize() {
        return poolSize <= 0 ? DEFAULT_POOL_SIZE : poolSize;
    }

    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }

    protected final Cipher getCipher(int mode) throws Exception {
        Queue<Cipher> pool = mode == Cipher.ENCRYPT_MODE ? eciphers : dciphers;
        Cipher cipher = pool.poll();

        if (cipher == null) {
            cipher = createCipher(mode);
        }

        return cipher;
    }

    protected final void returnCipher(int mode, Cipher cipher) {
        Queue<Cipher> pool = mode == Cipher.ENCRYPT_MODE ? eciphers : dciphers;

        if (cipher != null) {
            pool.offer(cipher);
        }

        while (pool.size() > getPoolSize()) {
            pool.poll();
        }
    }

    protected abstract Cipher createCipher(int mode) throws Exception;

    public byte[] encrypt(byte[] plaintext) throws SessionEncrypterException {
        Cipher cipher = null;

        try {
            cipher = getCipher(Cipher.ENCRYPT_MODE);
            return cipher.doFinal(plaintext);
        } catch (Exception e) {
            throw new SessionEncoderException("Failed to encrypt object", e);
        } finally {
            returnCipher(Cipher.ENCRYPT_MODE, cipher);
        }
    }

    public byte[] decrypt(byte[] cryptotext) throws SessionEncrypterException {
        Cipher cipher = null;

        try {
            cipher = getCipher(Cipher.DECRYPT_MODE);
            return cipher.doFinal(cryptotext);
        } catch (Exception e) {
            throw new SessionEncoderException("Failed to decrypt object", e);
        } finally {
            returnCipher(Cipher.DECRYPT_MODE, cipher);
        }
    }
}
