package com.alibaba.citrus.dev.handler.component;

import static com.alibaba.citrus.util.StringEscapeUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.io.PrintWriter;
import java.util.BitSet;

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

    public void visitTemplate(RequestHandlerContext context, Element element) {
        getTemplate().accept(new ElementVisitor(context, element));
    }

    @SuppressWarnings("unused")
    private class ElementVisitor extends AbstractVisitor {
        private final Element element;
        private Attribute attr;

        public ElementVisitor(RequestHandlerContext context, Element element) {
            super(context, DomComponent.this);
            this.element = element;
        }

        public void visitElement(Template elementWithSubElementsTemplate, Template elementSelfClosedTemplate,
                                 Template elementWithTextTemplate) {
            if (element.hasSubElements()) {
                elementWithSubElementsTemplate.accept(this);
            } else if (element.getText() != null) {
                elementWithTextTemplate.accept(this);
            } else {
                elementSelfClosedTemplate.accept(this);
            }
        }

        public void visitSubElements(Template elementWithSubElementsTemplate, Template elementSelfClosedTemplate,
                                     Template elementWithTextTemplate) {
            for (Element subElement : element.subElements()) {
                new ElementVisitor(context, subElement).visitElement(elementWithSubElementsTemplate,
                        elementSelfClosedTemplate, elementWithTextTemplate);
            }
        }

        public void visitElementId() {
            out().print(escapeHtml(element.getId()));
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
        private boolean withSep;

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

        private String toId(String name) {
            if (name != null) {
                StringBuilder buf = new StringBuilder(name.length());

                for (int i = 0; i < name.length(); i++) {
                    char c = name.charAt(i);

                    if (!bs.get(c)) {
                        c = '_';
                    }

                    buf.append(c);
                }

                name = buf.toString();
            }

            return name;
        }
    }

    private final static BitSet bs;

    static {
        bs = new BitSet();

        // 根据<a href="http://www.w3.org/TR/REC-xml/#id">http://www.w3.org/TR/REC-xml/#id</a>所指示的标准，将非id字符转成_。
        bs.set(':');
        bs.set('-');
        bs.set('.');
        bs.set('_');
        bs.set('0', '9');
        bs.set('A', 'Z');
        bs.set('a', 'z');
        bs.set('\u00C0', '\u00D6');
        bs.set('\u00D8', '\u00F6');
        bs.set('\u00F8', '\u02FF');
        bs.set('\u0370', '\u037D');
        bs.set('\u037F', '\u1FFF');
        bs.set('\u200C', '\u200D');
        bs.set('\u2070', '\u218F');
        bs.set('\u2C00', '\u2FEF');
        bs.set('\u3001', '\uD7FF');
        bs.set('\uF900', '\uFDCF');
        bs.set('\uFDF0', '\uFFFD');
        bs.set('\u00B7');
        bs.set('\u0300', '\u036F');
        bs.set('\u203F', '\u2040');
    }
}
