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

package com.alibaba.citrus.async.support;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

public class FakeAsyncExecutor extends SimpleAsyncTaskExecutor {
    private final static Logger                   log            = LoggerFactory.getLogger(FakeAsyncExecutor.class);
    private final        ThreadLocal<Callable<?>> callableHolder = new ThreadLocal<Callable<?>>();

    public Callable<?> getCallable() {
        return callableHolder.get();
    }

    public <T> Future<T> submit(Callable<T> task) {
        FutureTask<T> future = (FutureTask<T>) super.submit(task);
        callableHolder.set(task);
        return future;
    }

    @Override
    protected void doExecute(Runnable task) {
    }
}
