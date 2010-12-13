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
 * 代表一个form service的运行时异常。
 * 
 * @author Michael Zhou
 */
public class FormServiceException extends RuntimeException {
    private static final long serialVersionUID = -2930185032819558088L;

    public FormServiceException() {
    }

    public FormServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public FormServiceException(String message) {
        super(message);
    }

    public FormServiceException(Throwable cause) {
        super(cause);
    }
}
