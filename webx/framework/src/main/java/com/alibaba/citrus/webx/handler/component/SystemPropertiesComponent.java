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

import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.StringEscapeUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.alibaba.citrus.util.internal.webpagelite.PageComponent;
import com.alibaba.citrus.util.internal.webpagelite.PageComponentRegistry;
import com.alibaba.citrus.webx.handler.RequestHandlerContext;
import com.alibaba.citrus.webx.handler.support.AbstractVisitor;

/**
 * 用来显示系统properties的组件。
 * 
 * @author Michael Zhou
 */
public class SystemPropertiesComponent extends PageComponent {
    private final static Set<String> PATH_LIKE_KEYS = createHashSet("java.class.path", "java.endorsed.dirs",
            "java.ext.dirs", "java.library.path", "sun.boot.class.path", "sun.boot.library.path");

    private final KeyValuesComponent keyValuesComponent;

    public SystemPropertiesComponent(PageComponentRegistry registry, String componentPath,
                                     KeyValuesComponent keyValuesComponent) {
        super(registry, componentPath);
        this.keyValuesComponent = keyValuesComponent;
    }

    public void visitTemplate(RequestHandlerContext context) {
        getTemplate().accept(new SystemPropertiesVisitor(context));
    }

    @SuppressWarnings("unused")
    private class SystemPropertiesVisitor extends AbstractVisitor {
        private final Properties props;

        public SystemPropertiesVisitor(RequestHandlerContext context) {
            super(context, SystemPropertiesComponent.this);
            this.props = System.getProperties();
        }

        public void visitProperties() {
            Map<String, Object> keyValues = createTreeMap();
            String pathSep = props.getProperty("path.separator");

            for (Object key : props.keySet()) {
                Object value = escapeJava(props.getProperty(String.valueOf(key)));

                if (PATH_LIKE_KEYS.contains(key)) {
                    value = createArrayList(split((String) value, pathSep));
                }

                keyValues.put(String.valueOf(key), value);
            }

            keyValuesComponent.visitTemplate(context, keyValues);
        }
    }
}
