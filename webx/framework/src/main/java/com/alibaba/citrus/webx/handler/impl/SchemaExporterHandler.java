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
package com.alibaba.citrus.webx.handler.impl;

import java.io.IOException;

import com.alibaba.citrus.springext.export.SchemaExporterWEB;
import com.alibaba.citrus.springext.export.SchemaExporterWEB.MenuProvider;
import com.alibaba.citrus.util.internal.templatelite.Template;
import com.alibaba.citrus.util.internal.webpagelite.RequestContext;
import com.alibaba.citrus.webx.ResourceNotFoundException;
import com.alibaba.citrus.webx.handler.RequestHandlerContext;
import com.alibaba.citrus.webx.handler.component.MenuComponent;
import com.alibaba.citrus.webx.handler.support.AbstractVisitor;
import com.alibaba.citrus.webx.handler.support.AutowiredRequestProcessor;

/**
 * 用来展示schema页面的handler。
 * 
 * @author Michael Zhou
 */
public class SchemaExporterHandler extends AutowiredRequestProcessor {
    private final MenuComponent menuComponent = new MenuComponent(this, "menu");
    private final Template headTemplate = new Template(getClass().getResource("head.htm"));

    private final SchemaExporterWEB exporter = new SchemaExporterWEB(new MenuProvider() {
        public void renderMenuHead(RequestContext request) throws Exception {
            headTemplate.accept(new MenuVisitor((RequestHandlerContext) request));
        }

        public void renderMenu(RequestContext request) throws Exception {
            menuComponent.visitTemplate((RequestHandlerContext) request, getName());
        }
    });

    @SuppressWarnings("unused")
    private class MenuVisitor extends AbstractVisitor {
        private String componentResource;

        public MenuVisitor(RequestHandlerContext context) {
            super(context);
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
    }

    @Override
    public void handleRequest(RequestHandlerContext context) throws Exception {
        try {
            exporter.processRequest(context); // 先找exporter中的资源
        } catch (ResourceNotFoundException e) {
            super.handleRequest(context); // 如果没找到，再找当前handler上下文中的资源
        }
    }

    @Override
    protected void renderPage(RequestHandlerContext request, String resourceName) throws IOException {
        // 本页面是调用SchemaExporterWEB来输出，因此，没有自己的页面逻辑。
    }
}
