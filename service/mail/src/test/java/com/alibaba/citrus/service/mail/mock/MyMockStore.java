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
package com.alibaba.citrus.service.mail.mock;

import static org.junit.Assert.*;

import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.URLName;

import org.jvnet.mock_javamail.MockStore;

public class MyMockStore extends MockStore {
    public final static ThreadLocal<String> stateHolder = new ThreadLocal<String>();
    private static boolean err;

    public MyMockStore(Session session, URLName urlname) {
        super(session, urlname);
    }

    @Override
    public Folder getFolder(String name) throws MessagingException {
        if (err) {
            throw new MessagingException();
        }

        return super.getFolder(name);
    }

    @Override
    protected boolean protocolConnect(String host, int port, String user, String password) throws MessagingException {
        if (err) {
            throw new MessagingException();
        }

        if (stateHolder.get() != null) {
            stateHolder.set("connected");
        }

        return super.protocolConnect(host, port, user, password);
    }

    @Override
    public void close() throws MessagingException {
        if (err) {
            throw new MessagingException();
        }

        if (stateHolder.get() != null) {
            assertEquals("connected", stateHolder.get());
            stateHolder.set("closed");
        }

        super.close();
    }

    public static void setError(boolean e) {
        err = e;
    }
}
