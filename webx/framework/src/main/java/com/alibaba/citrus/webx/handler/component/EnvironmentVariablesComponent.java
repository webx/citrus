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
package com.alibaba.citrus.webx.handler.component;

import static com.alibaba.citrus.util.BasicConstant.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.ObjectUtil.*;
import static com.alibaba.citrus.util.StringEscapeUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.util.Map;
import java.util.Set;

import com.alibaba.citrus.util.internal.webpagelite.PageComponent;
import com.alibaba.citrus.util.internal.webpagelite.PageComponentRegistry;
import com.alibaba.citrus.webx.handler.RequestHandlerContext;
import com.alibaba.citrus.webx.handler.support.AbstractVisitor;

/**
 * 用来显示环境变量的组件。
 * 
 * @author Michael Zhou
 */
public class EnvironmentVariablesComponent extends PageComponent {
    private final static Set<String> PATH_LIKE_KEYS = createHashSet("PATH", "CLASSPATH", "LD_LIBRARY_PATH", "MANPATH");
    private final KeyValuesComponent keyValuesComponent;

    public EnvironmentVariablesComponent(PageComponentRegistry registry, String componentPath,
                                         KeyValuesComponent keyValuesComponent) {
        super(registry, componentPath);
        this.keyValuesComponent = keyValuesComponent;
    }

    public void visitTemplate(RequestHandlerContext context) {
        getTemplate().accept(new EnvironmentVariablesVisitor(context));
    }

    @SuppressWarnings("unused")
    private class EnvironmentVariablesVisitor extends AbstractVisitor {
        private final Map<String, String> env;

        public EnvironmentVariablesVisitor(RequestHandlerContext context) {
            super(context, EnvironmentVariablesComponent.this);
            env = System.getenv();
        }

        public void visitEnv() {
            Map<String, Object> keyValues = createTreeMap();
            String pathSep = System.getProperty("path.separator");

            for (String key : env.keySet()) {
                Object value = escapeJava(defaultIfNull(env.get(key), EMPTY_STRING));

                if (PATH_LIKE_KEYS.contains(key)) {
                    value = createArrayList(split((String) value, pathSep));
                }

                keyValues.put(String.valueOf(key), value);
            }

            keyValuesComponent.visitTemplate(context, keyValues);
        }
    }
}
