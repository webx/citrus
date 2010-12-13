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
package com.alibaba.citrus.util;

/**
 * 代表非法的路径。
 * 
 * @author Michael Zhou
 */
public class IllegalPathException extends IllegalArgumentException {
    private static final long serialVersionUID = -3229134664162661189L;

    public IllegalPathException() {
        super();
    }

    public IllegalPathException(String message, Throwable cause) {
        super(message, cause);
    }

    public IllegalPathException(String s) {
        super(s);
    }

    public IllegalPathException(Throwable cause) {
        super(cause);
    }
}
