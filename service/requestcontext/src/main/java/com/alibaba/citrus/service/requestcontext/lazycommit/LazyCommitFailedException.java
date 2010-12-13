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
package com.alibaba.citrus.service.requestcontext.lazycommit;

import com.alibaba.citrus.service.requestcontext.RequestContextException;

/**
 * 代表一个lazy commit失败的异常。
 * 
 * @author Michael Zhou
 */
public class LazyCommitFailedException extends RequestContextException {
    private static final long serialVersionUID = -7855934887908937875L;

    /**
     * 创建一个异常。
     */
    public LazyCommitFailedException() {
        super();
    }

    /**
     * 创建一个异常。
     * 
     * @param message 异常信息
     */
    public LazyCommitFailedException(String message) {
        super(message);
    }

    /**
     * 创建一个异常。
     * 
     * @param message 异常信息
     * @param cause 异常原因
     */
    public LazyCommitFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 创建一个异常。
     * 
     * @param cause 异常原因
     */
    public LazyCommitFailedException(Throwable cause) {
        super(cause);
    }
}
