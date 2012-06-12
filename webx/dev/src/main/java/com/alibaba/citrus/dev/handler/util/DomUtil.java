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

package com.alibaba.citrus.dev.handler.util;

import static com.alibaba.citrus.util.StringUtil.*;

import java.io.IOException;
import java.net.URL;
import java.util.BitSet;

import com.alibaba.citrus.springext.support.SchemaUtil;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Namespace;

public class DomUtil {
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

    public static String toId(String name) {
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

    /** 读取xml文件，转换成dom。 */
    public static Element readDocument(String name, URL url, ElementFilter filter) throws Exception, IOException {
        Document doc = SchemaUtil.readDocument(url.openStream(), name, true);
        org.dom4j.Element dom4jRootElement = doc.getRootElement();

        return copy(dom4jRootElement, filter);
    }

    private static Element copy(org.dom4j.Element dom4jElement, ElementFilter filter) throws Exception {
        dom4jElement = filter.filter(dom4jElement);

        if (dom4jElement == null) {
            return null;
        }

        Element element = new Element(dom4jElement.getQualifiedName(), dom4jElement.getNamespaceURI());

        for (Object attr : dom4jElement.attributes()) {
            String name = ((Attribute) attr).getQualifiedName();
            String value = ((Attribute) attr).getValue();

            element.addAttribute(name, value);
        }

        for (Object ns : dom4jElement.declaredNamespaces()) {
            String name = ((Namespace) ns).getPrefix();
            String value = ((Namespace) ns).getURI();

            if (isEmpty(name)) {
                name = "xmlns";
            } else {
                name = "xmlns:" + name;
            }

            element.addAttribute(name, value);
        }

        for (Object e : dom4jElement.elements()) {
            Element subElement = copy((org.dom4j.Element) e, filter);

            if (subElement != null) {
                element.addSubElement(subElement);
            }
        }

        if (dom4jElement.elements().isEmpty()) {
            String text = trimToNull(dom4jElement.getText());

            if (text != null) {
                element.setText(text);
            }
        }

        return element;
    }

    public interface ElementFilter {
        org.dom4j.Element filter(org.dom4j.Element e) throws Exception;
    }
}
