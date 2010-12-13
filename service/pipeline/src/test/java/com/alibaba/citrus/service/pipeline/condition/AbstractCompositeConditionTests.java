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
package com.alibaba.citrus.service.pipeline.condition;

import static org.junit.Assert.*;

import org.junit.Test;

import com.alibaba.citrus.service.pipeline.Condition;
import com.alibaba.citrus.service.pipeline.support.AbstractCompositeCondition;

public abstract class AbstractCompositeConditionTests<C extends AbstractCompositeCondition> extends
        AbstractConditionTests<C> {
    @Test
    public void setConditions_() {
        // init value
        assertArrayEquals(new Condition[0], condition.getConditions());

        // set null
        condition.setConditions(null);
        assertArrayEquals(new Condition[0], condition.getConditions());

        // set empty
        condition.setConditions(new Condition[] {});
        assertArrayEquals(new Condition[0], condition.getConditions());
    }

    @Test
    public void toString_() {
        condition
                .setConditions(new Condition[] { new MyCondition(true), new MyCondition(false), new MyCondition(true) });

        String str = "";
        str += desc() + " [\n";
        str += "  [1/3] true\n";
        str += "  [2/3] false\n";
        str += "  [3/3] true\n";
        str += "]";

        assertEquals(str, condition.toString());
    }

    protected abstract String desc();

    protected final void setConditions(Condition... conditions) {
        condition.setConditions(conditions);
    }
}
