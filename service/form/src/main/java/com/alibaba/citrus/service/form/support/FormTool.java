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
package com.alibaba.citrus.service.form.support;

import static com.alibaba.citrus.springext.util.SpringExtUtil.*;
import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.BasicConstant.*;
import static com.alibaba.citrus.util.ObjectUtil.*;

import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;

import org.apache.ecs.xhtml.input;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.citrus.service.form.Field;
import com.alibaba.citrus.service.form.Form;
import com.alibaba.citrus.service.form.FormService;
import com.alibaba.citrus.service.form.Group;
import com.alibaba.citrus.service.pull.ToolFactory;
import com.alibaba.citrus.service.requestcontext.parser.ParserRequestContext;
import com.alibaba.citrus.service.requestcontext.util.RequestContextUtil;
import com.alibaba.citrus.springext.support.parser.AbstractSingleBeanDefinitionParser;
import com.alibaba.citrus.util.StringEscapeUtil;

/**
 * 便于模板使用的pull tool。
 * 
 * @author Michael Zhou
 */
public class FormTool implements ToolFactory {
    private FormService formService;
    private HttpServletRequest request;

    @Autowired
    public void setFormService(FormService formService) {
        this.formService = formService;
    }

    @Autowired
    public void setRequest(HttpServletRequest request) {
        this.request = assertProxy(request);
    }

    /**
     * 每请求返回相同的form tool。
     */
    public boolean isSingleton() {
        return true;
    }

    public Object createTool() throws Exception {
        return this;
    }

    /**
     * 取得指定名称的group helper。
     */
    public GroupHelper get(String groupName) {
        return new GroupHelper(groupName);
    }

    /**
     * 取得所有group实例。
     */
    public Iterator<GroupInstanceHelper> getGroups() {
        return new GroupInstanceHelperIterator(getForm().getGroups().iterator());
    }

    /**
     * 取得指定名称的group实例。
     */
    public Iterator<GroupInstanceHelper> getGroups(String groupName) {
        return new GroupInstanceHelperIterator(getForm().getGroups(groupName).iterator());
    }

    /**
     * 判断整个form是否通过验证。
     */
    public boolean isValid() {
        return getForm().isValid();
    }

    private Form getForm() {
        return formService.getForm();
    }

    @Override
    public String toString() {
        return formService == null ? "FormTool[no FormService]" : formService.toString();
    }

    /**
     * 便于模板使用的辅助类。
     */
    public class GroupHelper {
        private final String groupName;

        /**
         * 创建group helper。
         */
        public GroupHelper(String groupName) {
            this.groupName = groupName;
        }

        /**
         * 取得当前group的默认instance。
         */
        public GroupInstanceHelper getDefaultInstance() {
            Group group = getForm().getGroup(groupName);
            return group == null ? null : new GroupInstanceHelper(group);
        }

        /**
         * 取得当前group的指定instance。
         */
        public GroupInstanceHelper getInstance(String instanceName) {
            Group group = getForm().getGroup(groupName, instanceName);
            return group == null ? null : new GroupInstanceHelper(group);
        }

        /**
         * 取得当前group的指定instance。
         */
        public GroupInstanceHelper getInstance(String instanceName, boolean create) {
            Group group = getForm().getGroup(groupName, instanceName, create);
            return group == null ? null : new GroupInstanceHelper(group);
        }

        @Override
        public String toString() {
            return "Group[" + groupName + "]";
        }
    }

    /**
     * 便于模板使用的辅助类。
     */
    public class GroupInstanceHelper {
        private final Group group;

        /**
         * 创建group instance helper。
         */
        public GroupInstanceHelper(Group group) {
            this.group = group;
        }

        /**
         * 取得指定名称的field helper。
         */
        public FieldHelper get(String fieldName) {
            Field field = group.getField(fieldName);
            return field == null ? null : new FieldHelper(field);
        }

        /**
         * 取得所有的fields。
         */
        public Iterator<FieldHelper> getFields() {
            return new FieldHelperIterator(group.getFields().iterator());
        }

        /**
         * 判断整个group instance是否通过验证。
         */
        public boolean isValid() {
            return group.isValid();
        }

        /**
         * 判断这个group有没有被验证过。
         */
        public boolean isValidated() {
            return group.isValidated();
        }

        /**
         * 将对象中的值置入当前group instance中。
         */
        public void mapTo(Object object) {
            group.mapTo(object);
        }

        @Override
        public String toString() {
            return group.toString();
        }
    }

    /**
     * 取得group instance helper的遍历器。
     */
    private class GroupInstanceHelperIterator extends FilterIterator<GroupInstanceHelper, Group> {
        private GroupInstanceHelperIterator(Iterator<Group> i) {
            super(i);
        }

        public GroupInstanceHelper next() {
            return new GroupInstanceHelper(i.next());
        }
    }

    /**
     * 便于模板使用的辅助类。
     */
    public class FieldHelper {
        private final Field field;
        private String htmlFieldSuffix;

        /**
         * 创建field helper。
         */
        public FieldHelper(Field field) {
            this.field = field;
        }

        /**
         * 取得field的用于显示的名称。
         */
        public String getDisplayName() {
            return field.getFieldConfig().getDisplayName();
        }

        /**
         * 取得在form中唯一表示该field的key。
         */
        public String getKey() {
            return field.getKey();
        }

        /**
         * 取得在form中唯一表示该field的key。
         */
        public String getHtmlKey() {
            if (htmlFieldSuffix == null) {
                htmlFieldSuffix = RequestContextUtil.findRequestContext(request, ParserRequestContext.class)
                        .getHtmlFieldSuffix();
            }

            return field.getKey() + htmlFieldSuffix;
        }

        /**
         * 取得在form中唯一表示该field的key，当用户提交的表单中未包含此field的信息时，取这个key的值作为该field的值。
         */
        public String getAbsentKey() {
            return field.getAbsentKey();
        }

        /**
         * 取得在form中和当前field绑定的附件的key。
         */
        public String getAttachmentKey() {
            return field.getAttachmentKey();
        }

        /**
         * 取得field的值。
         */
        public String getValue() {
            return field.getStringValue();
        }

        /**
         * 取得field的值，并进行<code>escapeHtml</code>编码。
         */
        public String getEscapedValue() {
            return StringEscapeUtil.escapeHtml(field.getStringValue());
        }

        /**
         * 取得field的值。
         */
        public String[] getValues() {
            return field.getStringValues();
        }

        /**
         * 取得field的值，并进行<code>escapeHtml</code>编码。
         */
        public String[] getEscapedValues() {
            String[] values = field.getStringValues();
            String[] escapedValues = new String[values.length];

            for (int i = 0; i < values.length; i++) {
                escapedValues[i] = StringEscapeUtil.escapeHtml(values[i]);
            }

            return escapedValues;
        }

        /**
         * 取得absent字段的HTML hidden field。
         */
        public input getAbsentHiddenField(String value) {
            return new input("hidden", field.getAbsentKey(), defaultIfNull(value, EMPTY_STRING));
        }

        /**
         * 取得附件。
         */
        public Object getAttachment() {
            return field.getAttachment();
        }

        /**
         * 取得编码后的附件。
         */
        public String getAttachmentEncoded() {
            return defaultIfNull(field.getAttachmentEncoded(), EMPTY_STRING);
        }

        /**
         * 是否包含附件？
         */
        public boolean hasAttachment() {
            return field.hasAttachment();
        }

        /**
         * 设置附件。
         */
        public void setAttachment(Object attachment) {
            field.setAttachment(attachment);
        }

        /**
         * 取得代表附件的HTML hidden field。
         */
        public input getAttachmentHiddenField() {
            return new input("hidden", field.getAttachmentKey(), getAttachmentEncoded());
        }

        /**
         * 清除附件。
         */
        public void clearAttachment() {
            field.clearAttachment();
        }

        /**
         * 判断这个field是否是合法的。
         */
        public boolean isValid() {
            return field.isValid();
        }

        /**
         * 取得这个field相对应的出错信息。
         */
        public String getMessage() {
            return field.getMessage();
        }

        @Override
        public String toString() {
            return field.toString();
        }
    }

    /**
     * 取得field helper的遍历器。
     */
    private class FieldHelperIterator extends FilterIterator<FieldHelper, Field> {
        private FieldHelperIterator(Iterator<Field> i) {
            super(i);
        }

        public FieldHelper next() {
            return new FieldHelper(i.next());
        }
    }

    private static abstract class FilterIterator<E, F> implements Iterator<E> {
        protected final Iterator<F> i;

        public FilterIterator(Iterator<F> i) {
            this.i = assertNotNull(i);
        }

        public boolean hasNext() {
            return i.hasNext();
        }

        public void remove() {
            i.remove();
        }
    }

    public static class DefinitionParser extends AbstractSingleBeanDefinitionParser<FormTool> {
    }
}
