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
package com.alibaba.citrus.service.form.impl;

import static com.alibaba.citrus.service.form.FormConstant.*;
import static com.alibaba.citrus.service.requestcontext.util.RequestContextUtil.*;
import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.BasicConstant.*;
import static com.alibaba.citrus.util.ObjectUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.citrus.service.form.CustomErrorNotFoundException;
import com.alibaba.citrus.service.form.Field;
import com.alibaba.citrus.service.form.Group;
import com.alibaba.citrus.service.form.MessageContext;
import com.alibaba.citrus.service.form.Validator;
import com.alibaba.citrus.service.form.Validator.Context;
import com.alibaba.citrus.service.form.configuration.FieldConfig;
import com.alibaba.citrus.service.requestcontext.parser.ParserRequestContext;
import com.alibaba.citrus.service.requestcontext.support.ValueListSupport;
import com.alibaba.citrus.util.ArrayUtil;
import com.alibaba.citrus.util.ObjectUtil;
import com.alibaba.citrus.util.StringEscapeUtil;
import com.alibaba.citrus.util.io.ByteArrayInputStream;
import com.alibaba.citrus.util.io.ByteArrayOutputStream;

/**
 * 代表用户所提交表单中的一个field。
 * <p>
 * 注意：field对象不是线程安全的，不能被多线程共享。
 * </p>
 * 
 * @author Michael Zhou
 */
public class FieldImpl extends ValueListSupport implements Field {
    private final static Logger log = LoggerFactory.getLogger(Field.class);
    private final FieldConfig fieldConfig;
    private final Group group;
    private final String fieldKey;
    private final MessageContext messageContext;
    private boolean valid;
    private String message;
    private Attachment attachment;

    /**
     * 创建一个新field。
     */
    public FieldImpl(FieldConfig fieldConfig, Group group) {
        super( //
                assertNotNull(group, "group").getForm().getTypeConverter(), // converter
                fieldConfig.getGroupConfig().getFormConfig().isConverterQuiet() // converter quiet
        );
        this.fieldConfig = assertNotNull(fieldConfig, "fieldConfig");
        this.group = group;
        this.fieldKey = group.getKey() + FIELD_KEY_SEPARATOR + fieldConfig.getKey();
        this.messageContext = MessageContextFactory.newInstance(this);
    }

    /**
     * 取得field的配置信息。
     */
    public FieldConfig getFieldConfig() {
        return fieldConfig;
    }

    /**
     * 取得包含此field的group。
     */
    public Group getGroup() {
        return group;
    }

    /**
     * 判定field是否通过验证。
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * 取得在form中唯一代表该field的key。
     * <p>
     * 由固定前缀<code>"_fm"</code>，加上group名的缩写，加上group instance
     * fieldKey，再加上field名的缩写构成。例如：<code>_fm.m._0.n</code>。
     * </p>
     */
    public String getKey() {
        return fieldKey;
    }

    /**
     * 取得在form中唯一代表该field的key，当用户提交的表单中未包含此field的信息时，取这个key的值作为该field的值。
     * <p>
     * 这对于checkbox之类的HTML控件特别有用。
     * </p>
     * <p>
     * Key的格式为：<code>_fm.groupKey.instanceKey.fieldKey.absent</code>。
     * </p>
     */
    public String getAbsentKey() {
        return getKey() + FORM_FIELD_ABSENT_KEY;
    }

    /**
     * 取得在form中和当前field绑定的附件的key。
     * <p>
     * Key的格式为：<code>_fm.groupKey.instanceKey.fieldKey.attach</code>。
     * </p>
     */
    public String getAttachmentKey() {
        return getKey() + FORM_FIELD_ATTACHMENT_KEY;
    }

    /**
     * 取得出错信息。
     */
    public String getMessage() {
        return message;
    }

    /**
     * 设置错误信息，同时置<code>isValid()</code>为<code>false</code>。
     * <p>
     * 对于<code>isValid()</code>已经是<code>false</code>的字段，该方法无效（不覆盖现有的错误信息）
     * </p>
     * <p>
     * id表示错误信息的ID，必须定义的form描述文件中。
     * </p>
     */
    public void setMessage(String id) {
        setMessage(id, null);
    }

    /**
     * 设置错误信息，同时置<code>isValid()</code>为<code>false</code>。
     * <p>
     * 对于<code>isValid()</code>已经是<code>false</code>的字段，该方法无效（不覆盖现有的错误信息）
     * </p>
     * <p>
     * id表示错误信息的ID，必须定义的form描述文件中。params表示生成错误信息的参数表。
     * </p>
     */
    public void setMessage(String id, Map<String, ?> params) {
        if (isValid()) {
            boolean found = false;

            for (Validator validator : getFieldConfig().getValidators()) {
                if (isEquals(validator.getId(), id)) {
                    MessageContext expressionContext = MessageContextFactory.newInstance(this, validator);
                    expressionContext.putAll(params);
                    valid = false;
                    found = true;
                    message = validator.getMessage(new ValidatorContextImpl(expressionContext, this));

                    if (message == null) {
                        throw new CustomErrorNotFoundException("No message specified for error ID \"" + id + "\" in "
                                + this);
                    }

                    break;
                }
            }

            if (found) {
                ((GroupImpl) getGroup()).setValid(valid);
            } else {
                throw new CustomErrorNotFoundException("Specified error ID \"" + id + "\" was not found in " + this);
            }
        }
    }

    /**
     * 初始化field值，但不验证表单字段。其中，<code>request</code>可以是<code>null</code>。
     */
    public void init(HttpServletRequest request) {
        valid = true;
        attachment = null;

        // request为null，表示是空表单（不是用户提交的），此时装载默认值
        if (request == null) {
            setValues(getFieldConfig().getDefaultValues());
        } else {
            ParserRequestContext prc = findRequestContext(request, ParserRequestContext.class);

            // 假如配置了ParserRequestContext，则取得objects，以便支持FileItem，否则只支持字符串值。
            if (prc != null) {
                setValues(prc.getParameters().getObjects(getKey()));
            } else {
                setValues(request.getParameterValues(getKey()));
            }

            // 如果field不存在，则检查absent fieldKey。
            if (size() == 0) {
                setValues(request.getParameterValues(getAbsentKey()));
            }

            // 如果存在attachment，则装入之
            String attachmentEncoded = trimToNull(request.getParameter(getAttachmentKey()));

            if (attachmentEncoded != null) {
                attachment = new Attachment(attachmentEncoded);
            }
        }
    }

    /**
     * 验证（或重新验证）字段。
     */
    protected void validate() {
        valid = true;

        for (Validator validator : getFieldConfig().getValidators()) {
            MessageContext expressionContext = MessageContextFactory.newInstance(this, validator);
            Context context = new ValidatorContextImpl(expressionContext, this);
            boolean passed = validator.validate(context);

            if (!passed) {
                valid = false;
                message = validator.getMessage(context);
                break;
            }
        }

        ((GroupImpl) getGroup()).setValid(valid);
    }

    /**
     * 取得field级别的错误信息表达式的context。
     */
    protected MessageContext getMessageContext() {
        return messageContext;
    }

    /**
     * 取得field name，相当于<code>getFieldConfig().getName()</code>。
     */
    public String getName() {
        return getFieldConfig().getName();
    }

    /**
     * 取得参数值，如果指定名称的参数不存在，则返回<code>""</code>。
     */
    @Override
    public String getStringValue() {
        return getStringValue(EMPTY_STRING);
    }

    /**
     * 取得用来显示field的名称，相当于<code>getFieldConfig().getDisplayName()</code>。
     */
    public String getDisplayName() {
        return getFieldConfig().getDisplayName();
    }

    /**
     * 取得默认值，相当于<code>getFieldConfig().getDefaultValue()</code>。
     */
    public String getDefaultValue() {
        return getFieldConfig().getDefaultValue();
    }

    /**
     * 取得默认值，相当于<code>getFieldConfig().getDefaultValues()</code>。
     */
    public String[] getDefaultValues() {
        return getFieldConfig().getDefaultValues();
    }

    /**
     * 添加参数名/参数值。
     */
    @Override
    public void addValue(Object value) {
        if (getFieldConfig().isTrimming() && value instanceof String) {
            value = trimToNull((String) value);
        }

        super.addValue(value);
    }

    /**
     * 设置附件。
     */
    public Object getAttachment() {
        return attachment == null ? null : attachment.getAttachment();
    }

    /**
     * 设置编码后的附件。
     */
    public String getAttachmentEncoded() {
        return attachment == null ? null : attachment.getAttachmentEncoded();
    }

    /**
     * 是否包含附件？
     */
    public boolean hasAttachment() {
        return attachment != null && attachment.getAttachment() != null;
    }

    /**
     * 设置附件。
     * <p>
     * 注意，当attachment已经存在时，该方法调用无效。欲强制设入，请先调用<code>clearAttachment()</code>。
     * </p>
     */
    public void setAttachment(Object attachment) {
        if (this.attachment == null) {
            this.attachment = new Attachment(attachment);
        }
    }

    /**
     * 清除附件。
     */
    public void clearAttachment() {
        this.attachment = null;
    }

    /**
     * 转换成易于阅读的字符串。
     */
    @Override
    public String toString() {
        return "Field[group: " + getGroup().getGroupConfig().getName() + "." + getGroup().getInstanceKey() + ", name: "
                + getFieldConfig().getName() + ", values: " + ObjectUtil.toString(getValues()) + ", valid: "
                + isValid() + "]";
    }

    /**
     * 代表一个附件。
     */
    private static class Attachment {
        private Object attachment;
        private String attachmentEncoded;

        public Attachment(Object attachment) {
            setAttachment(attachment);
        }

        public Attachment(String attachmentEncoded) {
            setAttachment(decode(attachmentEncoded));
        }

        public Object getAttachment() {
            return attachment;
        }

        public void setAttachment(Object attachment) {
            this.attachment = attachment;
            this.attachmentEncoded = null;
        }

        public String getAttachmentEncoded() {
            if (attachment != null && attachmentEncoded == null) {
                attachmentEncoded = encode(attachment);
            }

            return attachmentEncoded;
        }

        private String encode(Object attachment) {
            if (attachment == null) {
                return null;
            }

            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                // 1. 序列化
                // 2. 压缩
                Deflater def = new Deflater(Deflater.BEST_COMPRESSION, false);
                DeflaterOutputStream dos = new DeflaterOutputStream(baos, def);
                ObjectOutputStream oos = null;

                try {
                    oos = new ObjectOutputStream(dos);
                    oos.writeObject(attachment);
                } finally {
                    if (oos != null) {
                        try {
                            oos.close();
                        } catch (IOException e) {
                        }
                    }

                    def.end();
                }

                byte[] plaintext = baos.toByteArray().toByteArray();

                // 3. base64编码
                return StringEscapeUtil.escapeURL(new String(Base64.encodeBase64(plaintext, false), "ISO-8859-1"));
            } catch (Exception e) {
                log.error("Failed to encode field attachment", e);
                return "!Failure: " + e;
            }
        }

        private Object decode(String attachmentEncoded) {
            if (attachmentEncoded == null || attachmentEncoded.startsWith("!Failure:")) {
                return null;
            }

            // 1. base64解码
            byte[] plaintext = null;

            try {
                String encoded = StringEscapeUtil.unescapeURL(attachmentEncoded);
                plaintext = Base64.decodeBase64(encoded.getBytes("ISO-8859-1"));

                if (ArrayUtil.isEmptyArray(plaintext)) {
                    log.warn("Field attachment content is empty: " + encoded);
                    return null;
                }
            } catch (Exception e) {
                log.warn("Failed to decode field attachment: " + e);
                return null;
            }

            // 2. 解压缩
            ByteArrayInputStream bais = new ByteArrayInputStream(plaintext);
            Inflater inf = new Inflater(false);
            InflaterInputStream iis = new InflaterInputStream(bais, inf);

            // 3. 反序列化
            ObjectInputStream ois = null;

            try {
                ois = new ObjectInputStream(iis);
                return ois.readObject();
            } catch (Exception e) {
                log.warn("Failed to parse field attachment", e);
            } finally {
                if (ois != null) {
                    try {
                        ois.close();
                    } catch (IOException e) {
                    }
                }

                inf.end();
            }

            return null;
        }
    }
}
