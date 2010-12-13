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

import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.StringEscapeUtil.*;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.alibaba.citrus.util.internal.templatelite.Template;
import com.alibaba.citrus.util.internal.webpagelite.PageComponent;
import com.alibaba.citrus.util.internal.webpagelite.PageComponentRegistry;
import com.alibaba.citrus.webx.handler.RequestHandlerContext;
import com.alibaba.citrus.webx.handler.support.AbstractVisitor;

/**
 * 用来显示key-values对的组件。
 * 
 * @author Michael Zhou
 */
public class KeyValuesComponent extends PageComponent {
    public KeyValuesComponent(PageComponentRegistry registry, String componentPath) {
        super(registry, componentPath);
    }

    public void visitTemplate(RequestHandlerContext context, Map<String, ?> keyValues) {
        getTemplate().accept(new KeyValuesVisitor(context, keyValues));
    }

    @SuppressWarnings("unused")
    private class KeyValuesVisitor extends AbstractVisitor {
        private final Map<String, ?> keyValues;
        private String key;
        private Object values;
        private Object value;

        public KeyValuesVisitor(RequestHandlerContext context, Map<String, ?> keyValues) {
            super(context, KeyValuesComponent.this);
            this.keyValues = assertNotNull(keyValues, "keyValues");
        }

        public void visitRow(Template emptyTemplate, Template rowTemplate) {
            if (keyValues.isEmpty()) {
                emptyTemplate.accept(this);
            } else {
                for (String key : keyValues.keySet()) {
                    this.key = key;
                    this.values = keyValues.get(key);

                    if (this.values != null) {
                        if (this.values.getClass().isArray()) {
                            this.values = arrayToCollection(this.values);
                        } else if (this.values instanceof Map<?, ?>) {
                            this.values = ((Map<?, ?>) this.values).entrySet();
                        }
                    }

                    rowTemplate.accept(this);
                }
            }
        }

        public void visitKey() {
            out().print(escapeHtml(key));
        }

        public void visitValue() {
            out().print(escapeHtml(String.valueOf(value)));
        }

        public void visitEntryKey() {
            out().print(escapeHtml(String.valueOf(((Map.Entry<?, ?>) value).getKey())));
        }

        public void visitEntryValue() {
            out().print(escapeHtml(String.valueOf(((Map.Entry<?, ?>) value).getValue())));
        }

        public void visitValues(Template singleValueTemplate, Template multiValuesTemplate) {
            if (values instanceof Collection<?>) {
                Collection<?> valueCollection = (Collection<?>) values;

                switch (valueCollection.size()) {
                    case 0:
                        value = null;
                        singleValueTemplate.accept(this);
                        break;

                    case 1:
                        value = valueCollection.iterator().next();
                        singleValueTemplate.accept(this);
                        break;

                    default:
                        multiValuesTemplate.accept(this);
                        break;
                }
            } else {
                value = values;
                singleValueTemplate.accept(this);
            }
        }

        public void visitValueCount() {
            out().print(((Collection<?>) values).size());
        }

        public void visitValueItem(Template valueItemTemplate, Template entryItemTemplate) {
            for (Iterator<?> i = ((Collection<?>) values).iterator(); i.hasNext();) {
                value = i.next();

                if (value instanceof Map.Entry<?, ?>) {
                    entryItemTemplate.accept(this);
                } else {
                    valueItemTemplate.accept(this);
                }
            }
        }

        private Collection<?> arrayToCollection(Object array) {
            int length = Array.getLength(array);
            List<Object> list = createArrayList(length);

            for (int i = 0; i < length; i++) {
                list.add(Array.get(array, i));
            }

            return list;
        }
    }
}
