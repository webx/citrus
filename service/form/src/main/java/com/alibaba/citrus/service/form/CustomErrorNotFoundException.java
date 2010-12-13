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
package com.alibaba.citrus.service.form;

/**
 * 代表一个field error未找到的运行时异常。
 * 
 * @author Michael Zhou
 */
public class CustomErrorNotFoundException extends FormServiceException {
    private static final long serialVersionUID = 8301102425032981900L;

    public CustomErrorNotFoundException() {
    }

    public CustomErrorNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public CustomErrorNotFoundException(String message) {
        super(message);
    }

    public CustomErrorNotFoundException(Throwable cause) {
        super(cause);
    }
}
