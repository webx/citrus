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
    private final long         defaultCancelingTimeout;
    private final Object       runnable;
    private final AsyncContext asyncContext;

    AsyncCallbackAdapter(Object runnable, AsyncContext asyncContext, long defaultTimeout, long defaultCancelingTimeout) {
        assertTrue(runnable instanceof Runnable || runnable instanceof Callable<?>, "runnable or callable");
        this.runnable = runnable;
        this.asyncContext = assertNotNull(asyncContext, "asyncContext");
        this.defaultTimeout = defaultTimeout;
        this.defaultCancelingTimeout = defaultCancelingTimeout;
    }

    public long getTimeout() {
        long timeout = -1;

        if (runnable instanceof AsyncCallback) {
            timeout = ((AsyncCallback) runnable).getTimeout();
        }

        if (timeout < 0) {
            timeout = defaultTimeout;
        }

        return timeout;
    }

    public long getCancelingTimeout() {
        long timeout = -1;

        if (runnable instanceof AsyncCallback) {
            timeout = ((AsyncCallback) runnable).getCancelingTimeout();
        }

        if (timeout < 0) {
            timeout = defaultCancelingTimeout;
        }

        return timeout;
    }

    public AsyncContext getAsyncContext() {
        return asyncContext;
    }

    @Override
    public Object call() throws Exception {
        if (runnable instanceof Runnable) {
            ((Runnable) runnable).run();
            return null;
        } else {
            return ((Callable<?>) runnable).call();
        }
    }
}