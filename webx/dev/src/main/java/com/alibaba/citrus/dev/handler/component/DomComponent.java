/*
 * Copyright (c) 2002-2012 Alibaba Group Holding Limited.
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
 */

package com.alibaba.citrus.dev.handler.component;

import static com.alibaba.citrus.dev.handler.util.DomUtil.*;
import static com.alibaba.citrus.util.StringEscapeUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.io.PrintWriter;
import java.util.Iterator;

import com.alibaba.citrus.dev.handler.util.AnchorValue;
import com.alibaba.citrus.dev.handler.util.Attribute;
import com.alibaba.citrus.dev.handler.util.ClassValue;
import com.alibaba.citrus.dev.handler.util.Element;
import com.alibaba.citrus.dev.handler.util.RawValue;
import com.alibaba.citrus.dev.handler.util.RefValue;
import com.alibaba.citrus.dev.handler.util.StyledValue;
import com.alibaba.citrus.dev.handler.util.TextValue;
import com.alibaba.citrus.util.ClassUtil;
import com.alibaba.citrus.util.internal.webpagelite.PageComponent;
import com.alibaba.citrus.util.internal.webpagelite.PageComponentRegistry;
import com.alibaba.citrus.util.templatelite.FallbackTextWriter;
import com.alibaba.citrus.util.templatelite.Template;
import com.alibaba.citrus.webx.handler.RequestHandlerContext;
import com.alibaba.citrus.webx.handler.support.AbstractVisitor;

public class DomComponent extends PageComponent {
    public DomComponent(PageComponentRegistry registry, String componentPath) {
        super(registry, componentPath);
    }

    public void visitTemplate(RequestHandlerContext context, Iterable<Element> elements) {
        visitTemplate(context, elements, null);
    }

    public void visitTemplate(RequestHandlerContext context, Iterable<Element> elements,
                              ControlBarCallback controlBarCallback) {
        getTemplate().accept(new ElementsVisitor(context, elements, controlBarCallback));
    }

    public interface ControlBarCallback {
        void renderControlBar();
    }

    @SuppressWarnings("unused")
    private class ElementsVisitor extends AbstractVisitor {
        private final Iterable<Element>  elements;
        private final ControlBarCallback controlBarCallback;
        private       Element            element;
        private       Attribute          attr;

        public ElementsVisitor(RequestHandlerContext context, Iterable<Element> elements,
                               ControlBarCallback controlBarCallback) {
            super(context, DomComponent.this);
            this.elements = elements;
            this.controlBarCallback = controlBarCallback;
        }

        public void visitControlBar(Template controlBarTemplate) {
            if (controlBarCallback == null) {
                controlBarTemplate.accept(this);
            } else {
                controlBarCallback.renderControlBar();
            }
        }

        public void visitElementList(Template orderedTemplate, Template unorderedTemplate) {
            Iterator<Element> i = elements.iterator();

            if (i.hasNext() && i.next() != null && i.hasNext()) {
                orderedTemplate.accept(this); // 如果有多个elements，则显示ol
            } else {
                unorderedTemplate.accept(this); // 否则显示ul
            }
        }

        public void visitElements(Template elementWithSubElementsTemplate, Template elementSelfClosedTemplate,
                                  Template elementWithTextTemplate) {
            for (Element element : elements) {
                this.element = element;

                if (element.hasSubElements()) {
                    elementWithSubElementsTemplate.accept(this);
                } else if (element.getText() != null) {
                    elementWithTextTemplate.accept(this);
                } else {
                    elementSelfClosedTemplate.accept(this);
                }
            }
        }

        public void visitSubElements(Template elementWithSubElementsTemplate, Template elementSelfClosedTemplate,
                                     Template elementWithTextTemplate) {
            new ElementsVisitor(context, element.subElements(), controlBarCallback).visitElements(
                    elementWithSubElementsTemplate, elementSelfClosedTemplate, elementWithTextTemplate);
        }

        public void visitElementId() {
            out().print(escapeHtml(element.getId()));
        }

        public void visitElementPrefix(Template prefixTemplate) {
            if (element.getPrefix() != null) {
                prefixTemplate.accept(this);
            }
        }

        public void visitElementPrefix() {
            out().print(escapeHtml(element.getPrefix()));
        }

        public void visitElementNs(Template nsTemplate) {
            if (element.getNs() != null) {
                nsTemplate.accept(this);
            }
        }

        public void visitElementNs() {
            out().print(escapeHtml(element.getNs()));
        }

        public void visitElementName() {
            out().print(escapeHtml(element.getLocalName()));
        }

        public void visitAttribute(Template attributeTemplate) {
            for (Attribute attr : element.attributes()) {
                this.attr = attr;
                attributeTemplate.accept(this);
            }
        }

        public void visitAttributeKey() {
            out().print(escapeHtml(attr.getKey()));
        }

        public void visitAttributeValue(Template[] styledTextTemplates) {
            new StyledValueVisitor(attr.getValue(), out()).visitStyledValue(styledTextTemplates);
        }

        public void visitElementTexts(Template singleLineTemplate, Template multiLineTemplate) {
            if (element.getText().hasControlChars()) {
                multiLineTemplate.accept(this);
            } else {
                singleLineTemplate.accept(this);
            }
        }

        public void visitElementText(Template[] styledTextTemplates) {
            new StyledValueVisitor(element.getText(), out()).visitStyledValue(styledTextTemplates);
        }
    }

    @SuppressWarnings("unused")
    private class StyledValueVisitor extends FallbackTextWriter<PrintWriter> {
        private final StyledValue value;
        private       boolean     withSep;

        public StyledValueVisitor(StyledValue value, PrintWriter out) {
            super(out);
            this.value = value;
        }

        private void visitStyledValue(Template[] styledTextTemplates) {
            Template template;

            // 纯文本
            if (value instanceof TextValue) {
                template = styledTextTemplates[0];
                context().put("value", value.getText());

                template.accept(this);
            }

            // raw data
            else if (value instanceof RawValue) {
                template = styledTextTemplates[1];
                context().put("packageName", ((RawValue) value).getRawType().getPackage().getName());
                context().put("className", ClassUtil.getSimpleClassName(((RawValue) value).getRawType()));
                context().put("value", ((RawValue) value).getRawToString());

                template.accept(this);
            }

            // class name
            else if (value instanceof ClassValue) {
                template = styledTextTemplates[2];
                context().put("packageName", ((ClassValue) value).getPackageName());
                context().put("className", ((ClassValue) value).getSimpleName());

                template.accept(this);
            }

            // anchor
            else if (value instanceof AnchorValue) {
                template = styledTextTemplates[3];

                int i = 0;
                for (String name : ((AnchorValue) value).getNames()) {
                    context().put("anchorName", toId(name));
                    context().put("anchorNameDisplay", name);
                    this.withSep = i++ > 0;

                    template.accept(this);
                }
            }

            // ref to anchor
            else if (value instanceof RefValue) {
                template = styledTextTemplates[4];

                int i = 0;
                for (String name : ((RefValue) value).getNames()) {
                    context().put("refName", toId(name));
                    context().put("refNameDisplay", name);
                    this.withSep = i++ > 0;

                    template.accept(this);
                }
            }

            // unknown value
            else {
                out().print("unknown value: " + value);
            }
        }

        public void visitSep(Template sepTemplate) {
            if (withSep) {
                sepTemplate.accept(this);
            }
        }

        public void visitPackageName() {
            String packageName = (String) context().get("packageName");

            if (!isEmpty(packageName)) {
                if (!packageName.endsWith(".")) {
                    packageName += ".";
                }
            }

            out().print(packageName);
        }
    }
}
