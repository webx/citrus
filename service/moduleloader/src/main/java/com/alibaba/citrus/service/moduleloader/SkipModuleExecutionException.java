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
package com.alibaba.citrus.service.moduleloader;

/**
 * 这是一个特殊的异常，action如果接到此异常，则必须跳过不执行。
 * 
 * @author Michael Zhou
 */
public class SkipModuleExecutionException extends RuntimeException {
    private static final long serialVersionUID = -3735904447838090746L;
    private final Object valueForNonSkippable;

    public SkipModuleExecutionException(String message, Object valueForNonSkippable) {
        super(message);
        this.valueForNonSkippable = valueForNonSkippable;
    }

    public SkipModuleExecutionException(String message, Object valueForNonSkippable, Throwable cause) {
        super(message, cause);
        this.valueForNonSkippable = valueForNonSkippable;
    }

    public Object getValueForNonSkippable() {
        return valueForNonSkippable;
    }
}
