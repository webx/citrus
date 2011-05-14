package com.alibaba.citrus.dev.handler.component;

import static com.alibaba.citrus.util.StringEscapeUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.io.PrintWriter;

import com.alibaba.citrus.dev.handler.util.AnchorValue;
import com.alibaba.citrus.dev.handler.util.Attribute;
import com.alibaba.citrus.dev.handler.util.ClassValue;
import com.alibaba.citrus.dev.handler.util.Element;
import com.alibaba.citrus.dev.handler.util.RawValue;
import com.alibaba.citrus.dev.handler.util.RefValue;
import com.alibaba.citrus.dev.handler.util.StyledValue;
import com.alibaba.citrus.dev.handler.util.TextValue;
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

    public void visitTemplate(RequestHandlerContext context, Element element) {
        visitTemplate(context, element, false);
    }

    public void visitTemplate(RequestHandlerContext context, Element element, boolean withList) {
        getTemplate().accept(new ElementVisitor(context, element, withList));
    }

    @SuppressWarnings("unused")
    private class ElementVisitor extends AbstractVisitor {
        private final Element element;
        private final boolean withList;
        private Attribute attr;

        public ElementVisitor(RequestHandlerContext context, Element element, boolean withList) {
            super(context, DomComponent.this);
            this.element = element;
            this.withList = withList;
        }

        public void visitDom(Template withListTemplate, Template noListTemplate) {
            if (withList) {
                withListTemplate.accept(this);
            } else {
                noListTemplate.accept(this);
            }
        }

        public void visitElement(Template elementWithSubElementsTemplate, Template elementSelfClosedTemplate,
                                 Template elementWithTextTemplate) {
            if (element.hasSubElements()) {
                elementWithSubElementsTemplate.accept(this);
            } else if (!isEmpty(element.getText().getText())) {
                elementWithTextTemplate.accept(this);
            } else {
                elementSelfClosedTemplate.accept(this);
            }
        }

        public void visitSubElements(Template elementWithSubElementsTemplate, Template elementSelfClosedTemplate,
                                     Template elementWithTextTemplate) {
            for (Element subElement : element.subElements()) {
                new ElementVisitor(context, subElement, false).visitElement(elementWithSubElementsTemplate,
                        elementSelfClosedTemplate, elementWithTextTemplate);
            }
        }

        public void visitElementName() {
            out().print(escapeHtml(element.getName()));
        }

        public void visitAttributes(Template attributeTemplate) {
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

        public void visitElementText(Template[] styledTextTemplates) {
            new StyledValueVisitor(element.getText(), out()).visitStyledValue(styledTextTemplates);
        }
    }

    @SuppressWarnings("unused")
    private class StyledValueVisitor extends FallbackTextWriter<PrintWriter> {
        private final StyledValue value;
        private boolean withSep;

        public StyledValueVisitor(StyledValue value, PrintWriter out) {
            super(out);
            this.value = value;
        }

        private void visitStyledValue(Template[] styledTextTemplates) {
            Template template;

            // ´¿ÎÄ±¾
            if (value instanceof TextValue) {
                template = styledTextTemplates[0];
                context().put("value", value.getText());

                template.accept(this);
            }

            // raw data 
            else if (value instanceof RawValue) {
                template = styledTextTemplates[1];
                context().put("packageName", ((RawValue) value).getRawType().getPackage().getName() + ".");
                context().put("className", ((RawValue) value).getRawType().getSimpleName());
                context().put("value", ((RawValue) value).getRawToString());

                template.accept(this);
            }

            // class name
            else if (value instanceof ClassValue) {
                template = styledTextTemplates[2];
                context().put("packageName", ((ClassValue) value).getPackageName() + ".");
                context().put("className", ((ClassValue) value).getSimpleName());

                template.accept(this);
            }

            // anchor
            else if (value instanceof AnchorValue) {
                template = styledTextTemplates[3];

                int i = 0;
                for (String name : ((AnchorValue) value).getNames()) {
                    context().put("anchorName", name);
                    this.withSep = i++ > 0;

                    template.accept(this);
                }
            }

            // ref to anchor
            else if (value instanceof RefValue) {
                template = styledTextTemplates[4];

                int i = 0;
                for (String name : ((RefValue) value).getNames()) {
                    context().put("refName", name);
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
    }
}
