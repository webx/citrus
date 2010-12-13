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
package com.alibaba.citrus.webx.handler.support;

import static com.alibaba.citrus.util.StringUtil.*;

import java.io.IOException;

import com.alibaba.citrus.util.internal.templatelite.Template;
import com.alibaba.citrus.webx.handler.RequestHandlerContext;
import com.alibaba.citrus.webx.handler.component.FooterComponent;
import com.alibaba.citrus.webx.handler.component.MenuComponent;

/**
 * 为子类提供一个layout。
 * 
 * @author Michael Zhou
 */
public abstract class LayoutRequestProcessor extends AutowiredRequestProcessor {
    private final Template layoutTemplate = new Template(LayoutRequestProcessor.class.getResource("layout.htm"));
    protected final Template bodyTemplate = new Template(getClass().getResource(getTemplateName()));
    protected final MenuComponent menuComponent = new MenuComponent(this, "menu");
    protected final FooterComponent footerComponent = new FooterComponent(this, "footer");

    @Override
    protected final void renderPage(RequestHandlerContext context, String resourceName) throws IOException {
        layoutTemplate.accept(new LayoutVisitor(context, "text/html; charset=UTF-8"));
    }

    private String getTemplateName() {
        String name = getClass().getSimpleName();

        if (name.endsWith("Handler")) {
            name = name.substring(0, name.length() - "Handler".length());
        }

        return toCamelCase(name) + ".htm";
    }

    protected abstract Object getBodyVisitor(RequestHandlerContext context);

    protected abstract String getTitle(Object bodyVisitor);

    @SuppressWarnings("unused")
    private class LayoutVisitor extends AbstractVisitor {
        private final Object bodyVisitor;
        private final String contentTypeAndCharset;
        private String componentResource;

        public LayoutVisitor(RequestHandlerContext context, String contentTypeAndCharset) throws IOException {
            super(context, contentTypeAndCharset);
            this.contentTypeAndCharset = contentTypeAndCharset;
            this.bodyVisitor = getBodyVisitor(context);
        }

        public void visitTitle() {
            out().append(getTitle(bodyVisitor));
        }

        public void visitContentTypeAndCharset() {
            out().append(contentTypeAndCharset);
        }

        public void visitComponentCss(Template cssTemplate) {
            for (String css : getComponentResources("css")) {
                this.componentResource = context.getResourceURL(css);
                cssTemplate.accept(this);
            }
        }

        public void visitComponentJs(Template cssTemplate) {
            for (String js : getComponentResources("js")) {
                this.componentResource = context.getResourceURL(js);
                cssTemplate.accept(this);
            }
        }

        public void visitComponentResource() {
            out().append(componentResource);
        }

        public void visitMenu() {
            menuComponent.visitTemplate(context, getName());
        }

        public void visitFooter() {
            footerComponent.visitTemplate(context);
        }

        public void visitBody() {
            bodyTemplate.accept(bodyVisitor);
        }
    }
}
