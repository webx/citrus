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
package com.alibaba.citrus.service.requestcontext.session;

/**
 * 代表session框架的异常。
 * 
 * @author Michael Zhou
 */
public class SessionFrameworkException extends RuntimeException {
    private static final long serialVersionUID = 7308344005371211443L;

    public SessionFrameworkException() {
        super();
    }

    public SessionFrameworkException(String message, Throwable cause) {
        super(message, cause);
    }

    public SessionFrameworkException(String message) {
        super(message);
    }

    public SessionFrameworkException(Throwable cause) {
        super(cause);
    }
}
