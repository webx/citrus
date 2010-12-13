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
package com.alibaba.citrus.util.internal.templatelite;

/**
 * 代表执行模板时发生的错误。
 * 
 * @author Michael Zhou
 */
public class TemplateRuntimeException extends TemplateException {
    private static final long serialVersionUID = -1675637580195040129L;

    public TemplateRuntimeException() {
        super();
    }

    public TemplateRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public TemplateRuntimeException(String message) {
        super(message);
    }

    public TemplateRuntimeException(Throwable cause) {
        super(cause);
    }
}
