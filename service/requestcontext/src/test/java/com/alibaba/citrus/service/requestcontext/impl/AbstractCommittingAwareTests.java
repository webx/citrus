/*
 * Copyright (c) 2002-2012 Alibaba Group Holding Limited.
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
 */

package com.alibaba.citrus.service.requestcontext.impl;

import static com.alibaba.citrus.generictype.TypeInfoUtil.*;
import static com.alibaba.citrus.util.ClassUtil.*;
import static org.easymock.EasyMock.*;

import java.lang.reflect.Array;
import java.lang.reflect.Method;

import com.alibaba.citrus.test.runner.Prototyped;
import com.alibaba.citrus.test.runner.Prototyped.TestName;
import com.alibaba.citrus.util.internal.Servlet3Util;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Prototyped.class)
public abstract class AbstractCommittingAwareTests<O, T extends O> implements Cloneable {
    protected O               originalObject;
    protected T               testObject;
    protected Method          method;
    protected HeaderCommitter committer;
    protected Object[]        args;
    protected boolean         doCommitHeaders;

    @TestName
    public String testName() {
        return getSimpleMethodSignature(method).replaceAll(" ", "").replace('(', '_').replace(')', '_').replace(',', '_');
    }

    @Before
    public final void initSuper() throws Exception {
        // committer
        committer = createMock(HeaderCommitter.class);

        if (doCommitHeaders) {
            committer.commitHeaders();
            expectLastCall().once();
        }

        replay(committer);

        // args
        args = new Object[method.getParameterTypes().length];

        for (int i = 0; i < args.length; i++) {
            args[i] = getDefaultValue(method.getParameterTypes()[i]);
        }

        // call original object
        @SuppressWarnings("unchecked")
        Class<O> originalObjectClass = (Class<O>) resolveParameter(getClass(), AbstractCommittingAwareTests.class, 0).getRawType();

        originalObject = createMock(originalObjectClass);
        method.invoke(originalObject, args);

        if (method.getReturnType().equals(void.class)) {
            expectLastCall().once();
        } else {
            expectLastCall().andReturn(getDefaultValue(method.getReturnType())).once();
        }

        replay(originalObject);
    }

    private Object getDefaultValue(Class<?> type) {
        if (type.equals(boolean.class)) {
            return false;
        } else if (type.equals(int.class) || type.equals(long.class) || type.equals(short.class)
                   || type.equals(float.class) || type.equals(double.class)
                   || type.equals(byte.class)) {
            return 0;
        } else if (type.equals(char.class)) {
            return '\0';
        } else if (type.equals(String.class)) {
            return "";
        } else if (type.isArray()) {
            return Array.newInstance(type.getComponentType(), 0);
        } else {
            return null;
        }
    }

    @Test
    public void invoke() throws Exception {
        boolean disableServlet3Features = false;

        try {
            // 确保在servlet3下，isReady/setWriteListener可被调用
            disableServlet3Features = Servlet3Util.setDisableServlet3Features(false);

            // 对于需要调用commitHeaders()的方法，测试commitHeaders方法有没有被调用。
            // 对于所有方法，测试delegate有没有被调用。
            method.invoke(testObject, args);
            verify(committer, originalObject);
        } finally {
            Servlet3Util.setDisableServlet3Features(disableServlet3Features);
        }
    }
}
