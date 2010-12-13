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
package com.alibaba.citrus.service.form.support;

import static com.alibaba.citrus.service.form.support.NumberSupport.Type.*;
import static com.alibaba.citrus.util.Assert.*;

import com.alibaba.citrus.service.form.support.NumberSupport.Type;

/**
 * 将field值看作指定数字类型的validator。
 * 
 * @author Michael Zhou
 */
public abstract class AbstractNumberValidator extends AbstractOptionalValidator {
    private Type numberType = INT;

    /**
     * 取得数字的类型。
     */
    public Type getNumberType() {
        return numberType;
    }

    /**
     * 设置数字的类型。
     */
    public void setNumberType(Type numberType) {
        this.numberType = assertNotNull(numberType, "numberType");
    }
}
