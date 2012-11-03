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

package com.alibaba.citrus.async.pipeline.valve;

import static com.alibaba.citrus.util.Assert.*;

import java.util.concurrent.Callable;
import javax.servlet.AsyncContext;

import com.alibaba.citrus.async.AsyncCallback;

class AsyncCallbackAdapter implements Callable<Object>, AsyncCallback {
    private final long         defaultTimeout;
    private final Object       callable;
    private final AsyncContext asyncContext;

    AsyncCallbackAdapter(Object callable, AsyncContext asyncContext, long defaultTimeout) {
        assertTrue(callable instanceof Runnable || callable instanceof Callable<?>, "callable or runnable");
        this.callable = callable;
        this.asyncContext = assertNotNull(asyncContext, "asyncContext");
        this.defaultTimeout = defaultTimeout;
    }

    public long getTimeout() {
        if (callable instanceof AsyncCallback) {
            return ((AsyncCallback) callable).getTimeout();
        } else {
            return defaultTimeout;
        }
    }

    public AsyncContext getAsyncContext() {
        return asyncContext;
    }

    @Override
    public Object call() throws Exception {
        if (callable instanceof Runnable) {
            ((Runnable) callable).run();
            return null;
        } else {
            return ((Callable<?>) callable).call();
        }
    }
}