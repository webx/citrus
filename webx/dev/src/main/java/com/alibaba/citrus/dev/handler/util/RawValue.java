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

package com.alibaba.citrus.dev.handler.util;

import static com.alibaba.citrus.util.BasicConstant.*;
import static com.alibaba.citrus.util.ObjectUtil.*;

public class RawValue extends StyledValue {
    private final Class<?> rawType;
    private final String   rawToString;

    public RawValue(Class<?> rawType, String rawToString) {
        super(rawToString);
        this.rawType = rawType;
        this.rawToString = defaultIfNull(rawToString, EMPTY_STRING);
    }

    public Class<?> getRawType() {
        return rawType == null ? Object.class : rawType;
    }

    public String getRawToString() {
        return rawToString;
    }
}
