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
package com.alibaba.citrus.service.requestcontext;

/**
 * 代表一个通用request context中发生的异常。
 * 
 * @author Michael Zhou
 */
public class RequestContextException extends RuntimeException {
    private static final long serialVersionUID = 8129627799406228080L;

    /**
     * 创建一个异常。
     */
    public RequestContextException() {
        super();
    }

    /**
     * 创建一个异常。
     * 
     * @param message 异常信息
     */
    public RequestContextException(String message) {
        super(message);
    }

    /**
     * 创建一个异常。
     * 
     * @param message 异常信息
     * @param cause 异常原因
     */
    public RequestContextException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 创建一个异常。
     * 
     * @param cause 异常原因
     */
    public RequestContextException(Throwable cause) {
        super(cause);
    }
}
