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
package com.alibaba.citrus.service.resource.impl;

import com.alibaba.citrus.util.internal.ToStringBuilder;
import com.alibaba.citrus.util.internal.ToStringBuilder.MapBuilder;

/**
 * 代表一个resource name pattern到另一个resource name的别名。
 * 
 * @author Michael Zhou
 */
public class ResourceAlias extends ResourceMapping {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getPatternType() {
        return "resource-alias";
    }

    @Override
    protected void init() {
    }

    @Override
    public String toString() {
        MapBuilder mb = new MapBuilder();

        mb.append("pattern", getPatternName());
        mb.append("name", getName());

        if (isInternal()) {
            mb.append("internal", isInternal());
        }

        return new ToStringBuilder().append(getClass().getSimpleName()).append(mb).toString();
    }

}
