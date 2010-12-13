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
package com.alibaba.citrus.service.requestcontext.session.valueencoder;

import static com.alibaba.citrus.util.StringUtil.*;

import java.beans.PropertyEditor;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.PropertyEditorRegistrar;
import org.springframework.beans.PropertyEditorRegistry;
import org.springframework.beans.SimpleTypeConverter;
import org.springframework.beans.TypeConverter;

import com.alibaba.citrus.service.configuration.support.PropertyEditorRegistrarsSupport;
import com.alibaba.citrus.service.requestcontext.session.SessionStore.StoreContext;
import com.alibaba.citrus.service.requestcontext.session.encoder.SessionEncoderException;
import com.alibaba.citrus.service.requestcontext.session.encrypter.Encrypter;
import com.alibaba.citrus.springext.support.BeanSupport;
import com.alibaba.citrus.util.StringEscapeUtil;
import com.alibaba.citrus.util.io.StreamUtil;

/**
 * <code>SessionEncoder</code>针对非串行化场景的抽象编码实现，加密，base64来编码、解码。
 * 
 * @author youqun.zhangyq
 * @author Michael Zhou
 */
public abstract class AbstractSessionValueEncoder extends BeanSupport implements SessionValueEncoder {
    private static final String DEFAULT_CHARSET = "UTF-8";
    private PropertyEditorRegistrarsSupport propertyEditorRegistrars = new PropertyEditorRegistrarsSupport();
    protected Encrypter encrypter;
    private String charset;

    public void setPropertyEditorRegistrars(PropertyEditorRegistrar[] registrars) {
        propertyEditorRegistrars.setPropertyEditorRegistrars(registrars);
    }

    public void setEncrypter(Encrypter encrypter) {
        this.encrypter = encrypter;
    }

    public String getCharset() {
        return charset == null ? DEFAULT_CHARSET : charset;
    }

    public void setCharset(String charset) {
        this.charset = trimToNull(charset);
    }

    protected abstract boolean doURLEncode();

    protected abstract boolean doCompress();

    public String encode(Object value, StoreContext storeContext) throws SessionValueEncoderException {
        try {
            String encodedValue = encodeValue(value);

            // 如果提供了encrypter，则压缩并加密之
            if (encrypter != null) {
                encodedValue = new String(
                        Base64.encodeBase64(encrypter.encrypt(compress(encodedValue.getBytes("UTF-8")))), "8859_1");
            }

            // 如果加密，则必须进行url encoding
            if (doURLEncode() || encrypter != null) {
                encodedValue = StringEscapeUtil.escapeURL(encodedValue, getCharset());
            }

            return encodedValue;
        } catch (Exception e) {
            throw new SessionEncoderException("Failed to encode single value", e);
        }
    }

    public Object decode(String encodedValue, StoreContext storeContext) throws SessionValueEncoderException {
        try {
            // 如果加密，则必须进行url decoding
            if (doURLEncode() || encrypter != null) {
                encodedValue = StringEscapeUtil.unescapeURL(encodedValue, getCharset());
            }

            // 如果提供了encrypter，则解密并解压缩之
            if (encrypter != null) {
                encodedValue = new String(decompress(encrypter.decrypt(Base64.decodeBase64(encodedValue
                        .getBytes("8859_1")))), "UTF-8");
            }

            return decodeValue(encodedValue);
        } catch (Exception e) {
            throw new SessionEncoderException("Failed to decode single value", e);
        }
    }

    private byte[] compress(byte[] data) throws SessionValueEncoderException {
        if (!doCompress()) {
            return data;
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Deflater def = new Deflater(Deflater.BEST_COMPRESSION, false);
        DeflaterOutputStream dos = new DeflaterOutputStream(baos, def);

        try {
            dos.write(data);
        } catch (Exception e) {
            throw new SessionValueEncoderException(e);
        } finally {
            try {
                dos.close();
            } catch (IOException e) {
            }

            def.end();
        }

        return baos.toByteArray();
    }

    private byte[] decompress(byte[] data) throws SessionValueEncoderException {
        if (!doCompress()) {
            return data;
        }

        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        Inflater inf = new Inflater(false);
        InflaterInputStream iis = new InflaterInputStream(bais, inf);

        try {
            return StreamUtil.readBytes(iis, true).toByteArray();
        } catch (Exception e) {
            throw new SessionValueEncoderException(e);
        } finally {
            inf.end();
        }
    }

    protected final TypeConverter getTypeConverter() {
        SimpleTypeConverter typeConverter = new SimpleTypeConverter();
        propertyEditorRegistrars.registerCustomEditors(typeConverter);
        return typeConverter;
    }

    protected final String convertToString(Class<?> type, Object value, TypeConverter converter) {
        if (value instanceof String) {
            return (String) value;
        } else {
            if (converter instanceof PropertyEditorRegistry && type != null && type.isInstance(value)) {
                PropertyEditor editor = ((PropertyEditorRegistry) converter).findCustomEditor(type, null);

                if (editor != null) {
                    editor.setValue(value);
                    return editor.getAsText();
                }
            }

            return (String) getTypeConverter().convertIfNecessary(value, String.class);
        }
    }

    protected final Object convertToType(Class<?> type, String encodedValue, TypeConverter converter) {
        if (type != null && !type.equals(String.class)) {
            return converter.convertIfNecessary(encodedValue, type);
        } else {
            return encodedValue;
        }
    }

    protected abstract String encodeValue(Object value) throws Exception;

    protected abstract Object decodeValue(String encodedValue) throws Exception;
}
