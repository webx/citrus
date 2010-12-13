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
package com.alibaba.citrus.webx.handler.impl.error;

import com.alibaba.citrus.webx.handler.RequestHandlerContext;
import com.alibaba.citrus.webx.handler.component.EnvironmentVariablesComponent;
import com.alibaba.citrus.webx.handler.component.ExceptionComponent;
import com.alibaba.citrus.webx.handler.component.KeyValuesComponent;
import com.alibaba.citrus.webx.handler.component.RequestComponent;
import com.alibaba.citrus.webx.handler.component.SystemInfoComponent;
import com.alibaba.citrus.webx.handler.component.SystemPropertiesComponent;
import com.alibaba.citrus.webx.handler.support.AbstractVisitor;
import com.alibaba.citrus.webx.handler.support.LayoutRequestProcessor;
import com.alibaba.citrus.webx.util.ErrorHandlerHelper;

/**
 * 用来显示详细出错页面及相关资源的handler。
 * 
 * @author Michael Zhou
 */
public class DetailedErrorHandler extends LayoutRequestProcessor {
    private final KeyValuesComponent keyValuesComponent = new KeyValuesComponent(this, "keyValues");
    private final ExceptionComponent exceptionComponent = new ExceptionComponent(this, "exception");
    private final RequestComponent requestComponent = new RequestComponent(this, "request", keyValuesComponent);
    private final SystemPropertiesComponent systemPropertiesComponent = new SystemPropertiesComponent(this, "sysprops",
            keyValuesComponent);
    private final EnvironmentVariablesComponent environmentVariablesComponent = new EnvironmentVariablesComponent(this,
            "env", keyValuesComponent);
    private final SystemInfoComponent systemInfoComponent = new SystemInfoComponent(this, "sysinfo", keyValuesComponent);;

    @Override
    protected Object getBodyVisitor(RequestHandlerContext context) {
        return new DetailedErrorPageVisitor(context, ErrorHandlerHelper.getInstance(context.getRequest()));
    }

    @Override
    protected String getTitle(Object visitor) {
        ErrorHandlerHelper helper = ((DetailedErrorPageVisitor) visitor).helper;
        StringBuilder title = new StringBuilder();

        if (helper.getMessage() != null) {
            title.append(helper.getMessage());
        }

        title.append(": ").append(helper.getRequestURI());

        return title.toString();
    }

    @SuppressWarnings("unused")
    private class DetailedErrorPageVisitor extends AbstractVisitor {
        private final ErrorHandlerHelper helper;
        private String componentResource;

        public DetailedErrorPageVisitor(RequestHandlerContext context, ErrorHandlerHelper helper) {
            super(context);
            this.helper = helper;
        }

        public void visitException() {
            if (helper.getException() != null) {
                exceptionComponent.visitTemplate(context, helper.getException());
            }
        }

        public void visitRequest() {
            requestComponent.visitTemplate(context);
        }

        public void visitSystemProperties() {
            systemPropertiesComponent.visitTemplate(context);
        }

        public void visitSystemInfo() {
            systemInfoComponent.visitTemplate(context);
        }

        public void visitEnv() {
            environmentVariablesComponent.visitTemplate(context);
        }
    }
}
