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
package com.alibaba.citrus.service.requestcontext.basic;

/**
 * 代表一个cookie被拒绝的异常。
 * 
 * @author Michael Zhou
 */
public class CookieRejectedException extends ResponseHeaderRejectedException {
    private static final long serialVersionUID = -2667477249289081304L;

    public CookieRejectedException() {
    }

    public CookieRejectedException(String message, Throwable cause) {
        super(message, cause);
    }

    public CookieRejectedException(String message) {
        super(message);
    }

    public CookieRejectedException(Throwable cause) {
        super(cause);
    }
}
