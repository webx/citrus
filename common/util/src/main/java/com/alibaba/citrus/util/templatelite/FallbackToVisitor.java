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

package com.alibaba.citrus.util.templatelite;

import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.StringUtil.*;

/** 调用指定visitor中的visitPlaceholder()方法。 */
public class FallbackToVisitor {
    private final Object visitor;

    public FallbackToVisitor(Object visitor) {
        this.visitor = assertNotNull(visitor, "fallback to visitor");
    }

    public Object getVisitor() {
        return visitor;
    }

    public boolean visitPlaceholder(String name, Object[] params) throws Exception {
        try {
            visitor.getClass().getMethod("visit" + capitalize(name)).invoke(visitor);
        } catch (NoSuchMethodException e) {
            if (visitor instanceof FallbackVisitor) {
                return ((FallbackVisitor) visitor).visitPlaceholder(name, params);
            } else {
                return false;
            }
        }

        return true;
    }
}
