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
package com.alibaba.citrus.webx.handler.impl.info;

import com.alibaba.citrus.webx.handler.RequestHandlerContext;
import com.alibaba.citrus.webx.handler.component.KeyValuesComponent;
import com.alibaba.citrus.webx.handler.component.SystemPropertiesComponent;
import com.alibaba.citrus.webx.handler.support.AbstractVisitor;
import com.alibaba.citrus.webx.handler.support.LayoutRequestProcessor;

/**
 * 用来显示当前JVM system properties的信息。
 * 
 * @author Michael Zhou
 */
public class SystemPropertiesHandler extends LayoutRequestProcessor {
    private final KeyValuesComponent keyValuesComponent = new KeyValuesComponent(this, "keyValues");
    private final SystemPropertiesComponent systemPropertiesComponent = new SystemPropertiesComponent(this, "sysprops",
            keyValuesComponent);

    @Override
    protected String getTitle(Object bodyVisitor) {
        return "System Properties";
    }

    @Override
    protected Object getBodyVisitor(RequestHandlerContext context) {
        return new SystemPropertiesPageVisitor(context);
    }

    @SuppressWarnings("unused")
    private class SystemPropertiesPageVisitor extends AbstractVisitor {
        public SystemPropertiesPageVisitor(RequestHandlerContext context) {
            super(context);
        }

        public void visitSystemProperties() {
            systemPropertiesComponent.visitTemplate(context);
        }
    }
}
