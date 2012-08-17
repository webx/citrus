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

package com.alibaba.citrus.service.moduleloader.impl.adapter;

import static com.alibaba.citrus.util.Assert.*;

import com.alibaba.citrus.service.moduleloader.ModuleReturningValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractDataBindingAdapter implements ModuleReturningValue {
    protected final Logger log;
    protected final Object moduleObject;

    AbstractDataBindingAdapter(Object moduleObject) {
        this.log = LoggerFactory.getLogger(moduleObject.getClass());
        this.moduleObject = assertNotNull(moduleObject, "moduleObject");
    }
}
