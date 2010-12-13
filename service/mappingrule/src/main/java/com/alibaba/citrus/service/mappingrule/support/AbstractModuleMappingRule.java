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
package com.alibaba.citrus.service.mappingrule.support;

import static com.alibaba.citrus.util.StringUtil.*;

import com.alibaba.citrus.service.moduleloader.ModuleLoaderService;

/**
 * 映射到模块名的<code>MappingRule</code>。
 * <p>
 * 该<code>MappingRule</code>有可能调用<code>ModuleLoaderService</code>以确定模块是否存在。
 * </p>
 * 
 * @author Michael Zhou
 */
public abstract class AbstractModuleMappingRule extends AbstractMappingRule {
    public static final String MODULE_NAME_SEPARATOR = ".";

    private ModuleLoaderService moduleLoaderService;
    private String moduleType;

    public ModuleLoaderService getModuleLoaderService() {
        return moduleLoaderService;
    }

    public void setModuleLoaderService(ModuleLoaderService moduleLoaderService) {
        this.moduleLoaderService = moduleLoaderService;
    }

    public String getModuleType() {
        return moduleType;
    }

    public void setModuleType(String moduleType) {
        this.moduleType = trimToNull(moduleType);
    }

    /**
     * 将指定名称规格化成符合class命名规范的名称：除去后缀，将首字符改为大写。
     * 
     * @param className 要规格化的类名
     * @return 规格化后的类名，如果类名非法，则返回<code>null</code>
     */
    protected static String normalizeClassName(String className) {
        className = trimToNull(className);

        if (className == null) {
            return null;
        }

        StringBuilder buf = new StringBuilder(className);

        // 除去后缀
        int index = className.lastIndexOf(EXTENSION_SEPARATOR);

        if (index != -1) {
            buf.setLength(index);
        }

        // 首字符大写
        if (buf.length() == 0) {
            return null;
        } else {
            buf.setCharAt(0, Character.toUpperCase(buf.charAt(0)));
        }

        return buf.toString();
    }
}
