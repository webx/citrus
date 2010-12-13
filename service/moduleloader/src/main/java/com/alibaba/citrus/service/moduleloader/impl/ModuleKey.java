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
package com.alibaba.citrus.service.moduleloader.impl;

import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.StringUtil.*;
import static com.alibaba.citrus.util.internal.regex.ClassNameWildcardCompiler.*;

import com.alibaba.citrus.util.StringUtil;

/**
 * 用来索引一个module的key。
 * 
 * @author Michael Zhou
 */
public final class ModuleKey {
    private final String moduleType;
    private final String moduleName;

    public ModuleKey(String moduleType, String moduleName) {
        this.moduleType = assertNotNull(trimToNull(moduleType), "moduleType");
        this.moduleName = assertNotNull(normalizeModuleName(moduleName), "moduleName");
    }

    public String getModuleType() {
        return moduleType;
    }

    public String getModuleName() {
        return moduleName;
    }

    /**
     * 规格化module名称，将之转换成符合java类命名规范的形式， 例如：<code>member.edit</code>将转换成
     * <code>member.Edit</code>
     */
    private String normalizeModuleName(String moduleName) {
        moduleName = StringUtil.toCamelCase(normalizeClassName(moduleName));

        if (isEmpty(moduleName)) {
            return null;
        }

        int index = moduleName.lastIndexOf(".") + 1;

        if (index < moduleName.length() && Character.isLowerCase(moduleName.charAt(index))) {
            StringBuilder buf = new StringBuilder(moduleName);
            buf.setCharAt(index, Character.toUpperCase(moduleName.charAt(index)));
            moduleName = buf.toString();
        }

        return moduleName;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;

        result = prime * result + (moduleName == null ? 0 : moduleName.hashCode());
        result = prime * result + (moduleType == null ? 0 : moduleType.hashCode());

        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        ModuleKey other = (ModuleKey) obj;

        if (moduleName == null) {
            if (other.moduleName != null) {
                return false;
            }
        } else if (!moduleName.equals(other.moduleName)) {
            return false;
        }

        if (moduleType == null) {
            if (other.moduleType != null) {
                return false;
            }
        } else if (!moduleType.equals(other.moduleType)) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return moduleType + ":" + moduleName;
    }
}
