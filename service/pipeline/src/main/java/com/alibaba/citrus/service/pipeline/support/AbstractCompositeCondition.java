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
package com.alibaba.citrus.service.pipeline.support;

import com.alibaba.citrus.service.pipeline.Condition;
import com.alibaba.citrus.util.internal.ToStringBuilder;

/**
 * 组合式的condition基类。
 * 
 * @author Michael Zhou
 */
public abstract class AbstractCompositeCondition extends AbstractCondition {
    private static Condition[] EMPTY_CONDITIONS = new Condition[0];
    private Condition[] conditions;

    public Condition[] getConditions() {
        return conditions == null ? EMPTY_CONDITIONS : conditions;
    }

    public void setConditions(Condition[] conditions) {
        this.conditions = conditions;
    }

    @Override
    public final String toString() {
        return new ToStringBuilder().append(getDesc()).append(conditions).toString();
    }

    protected String getDesc() {
        return getClass().getSimpleName();
    }
}
