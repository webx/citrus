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

import static com.alibaba.citrus.util.ClassUtil.*;
import static com.alibaba.citrus.util.ExceptionUtil.*;
import static com.alibaba.citrus.util.StringEscapeUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.alibaba.citrus.util.internal.templatelite.Template;
import com.alibaba.citrus.util.internal.webpagelite.PageComponent;
import com.alibaba.citrus.util.internal.webpagelite.PageComponentRegistry;
import com.alibaba.citrus.webx.handler.RequestHandlerContext;
import com.alibaba.citrus.webx.handler.support.AbstractVisitor;

/**
 * 用来显示异常信息的页面组件。
 * 
 * @author Michael Zhou
 */
public class ExceptionComponent extends PageComponent {
    public ExceptionComponent(PageComponentRegistry registry, String componentPath) {
        super(registry, componentPath);
    }

    public void visitTemplate(RequestHandlerContext context, Throwable exception) {
        getTemplate().accept(new ExceptionVisitor(context, exception));
    }

    @SuppressWarnings("unused")
    private class ExceptionVisitor extends AbstractVisitor {
        private final List<Throwable> exceptions;
        private Throwable exception;
        private int exceptionId;
        private boolean defaultOpen;
        private StackTraceElement stackTraceElement;
        private String location;
        private boolean firstException = true;

        public ExceptionVisitor(RequestHandlerContext context, Throwable exception) {
            super(context, ExceptionComponent.this);
            this.exceptions = getCauses(exception);
        }

        public void visitException(Template exceptionTemplate) {
            for (Iterator<Throwable> i = exceptions.iterator(); i.hasNext();) {
                this.exception = i.next();
                this.exceptionId++;
                this.defaultOpen = !i.hasNext();
                exceptionTemplate.accept(this);
                this.firstException = false;
            }
        }

        public void visitExceptionId() {
            out().print(exceptionId);
        }

        public void visitShowHideHandleImage(String openImage, String closeImage) {
            visitComponentUrl(defaultOpen ? openImage : closeImage);
        }

        public void visitCausedBy(Template causedByTemplate) {
            if (!firstException) {
                causedByTemplate.accept(this);
            }
        }

        public void visitExceptionType() {
            out().append(exception.getClass().getCanonicalName());
        }

        public void visitExceptionMessage() {
            if (isEmpty(exception.getMessage())) {
                out().append("No Message");
            } else {
                out().append(escapeHtml(exception.getMessage()));
            }
        }

        public void visitStackTraceElement(Template stackTraceElementTemplate) {
            for (StackTraceElement element : exception.getStackTrace()) {
                this.stackTraceElement = element;
                this.location = locateClass(stackTraceElement.getClassName());
                stackTraceElementTemplate.accept(this);
            }
        }

        public void visitStackTraceElement_Class() {
            out().append(stackTraceElement.getClassName());
        }

        public void visitStackTraceElement_PackageName() {
            String className = stackTraceElement.getClassName();
            int index = className.lastIndexOf(".");

            if (index > 0) {
                out().append(className.substring(0, index));
            } else {
                out().append("(default package)");
            }
        }

        public void visitStackTraceElement_SimpleClassName() {
            String className = stackTraceElement.getClassName();
            out().append(className.substring(className.lastIndexOf(".") + 1));
        }

        public void visitStackTraceElement_ClassLocation() {
            if (location != null) {
                out().append(location);
            } else {
                out().append("Could not locate class " + stackTraceElement.getClassName());
            }
        }

        public void visitStackTraceElement_ClassLocationShort() {
            String shortLocation = null;

            if (location != null) {
                Matcher m = Pattern.compile("[^/]+/?$").matcher(location);

                if (m.find()) {
                    shortLocation = m.group();
                }
            }

            if (shortLocation != null) {
                out().append(shortLocation);
            } else {
                out().append("&lt;unknown location&gt;");
            }
        }

        public void visitStackTraceElement_Method() {
            out().append(stackTraceElement.getMethodName());
        }

        public void visitStackTraceElement_File() {
            out().append(stackTraceElement.getFileName());
        }

        public void visitStackTraceElement_Line() {
            out().print(stackTraceElement.getLineNumber());
        }
    }
}
