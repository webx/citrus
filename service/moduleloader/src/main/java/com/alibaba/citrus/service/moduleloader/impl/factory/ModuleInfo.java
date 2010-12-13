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
package com.alibaba.citrus.service.moduleloader.impl.factory;

import com.alibaba.citrus.service.moduleloader.impl.ModuleKey;
import com.alibaba.citrus.util.internal.ToStringBuilder;
import com.alibaba.citrus.util.internal.ToStringBuilder.MapBuilder;

/**
 * 代表一个module的信息。
 * 
 * @author Michael Zhou
 */
class ModuleInfo {
    private final ModuleKey key;
    private final String beanName;
    private final String source;

    public ModuleInfo(ModuleKey key, String beanName, String source) {
        this.key = key;
        this.beanName = beanName;
        this.source = source;
    }

    public ModuleKey getKey() {
        return key;
    }

    public String getBeanName() {
        return beanName;
    }

    public String getSource() {
        return source;
    }

    @Override
    public String toString() {
        MapBuilder mb = new MapBuilder();

        mb.append("type:name", key);
        mb.append("beanName", beanName);
        mb.append("source", source);

        return new ToStringBuilder().append("Module").append(mb).toString();
    }
}
