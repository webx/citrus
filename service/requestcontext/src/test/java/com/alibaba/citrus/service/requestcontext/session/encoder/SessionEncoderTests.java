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
package com.alibaba.citrus.service.requestcontext.session.encoder;

import static com.alibaba.citrus.test.TestEnvStatic.*;
import static com.alibaba.citrus.test.TestUtil.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static org.junit.Assert.*;

import java.io.File;
import java.util.Map;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.io.FileSystemResource;

import com.alibaba.citrus.service.requestcontext.session.encrypter.impl.AesEncrypter;
import com.alibaba.citrus.service.requestcontext.session.serializer.impl.HessianSerializer;
import com.alibaba.citrus.service.requestcontext.session.serializer.impl.JavaSerializer;
import com.alibaba.citrus.springext.support.context.XmlApplicationContext;
import com.alibaba.citrus.test.runner.Prototyped;
import com.alibaba.citrus.test.runner.Prototyped.Prototypes;
import com.alibaba.citrus.test.runner.Prototyped.TestData;
import com.alibaba.citrus.test.runner.Prototyped.TestName;

/**
 * ≤‚ ‘session encoder°£
 * 
 * @author Michael Zhou
 */
@RunWith(Prototyped.class)
public class SessionEncoderTests implements Cloneable {
    private static BeanFactory factory;
    private String beanName;
    private Class<?> serializerType;
    private Class<?> encrypterType;
    private String toString;
    private AbstractSerializationEncoder encoder;
    private boolean skipEncoding;

    @BeforeClass
    public static void initFactory() {
        factory = new XmlApplicationContext(new FileSystemResource(new File(srcdir, "encoders.xml")));
    }

    @Before
    public void init() {
        encoder = (AbstractSerializationEncoder) factory.getBean(beanName);
    }

    @TestName
    public String testName() {
        return beanName;
    }

    @Prototypes
    public static TestData<SessionEncoderTests> data() {
        TestData<SessionEncoderTests> data = TestData.getInstance(SessionEncoderTests.class);
        SessionEncoderTests tests;

        tests = new SessionEncoderTests();
        tests.beanName = "hessian_noEncrypt";
        tests.serializerType = HessianSerializer.class;
        tests.encrypterType = null;
        tests.toString = "SerializationEncoder[HessianSerializer, no encrypter]";
        data.add(tests);

        tests = new SessionEncoderTests();
        tests.beanName = "java_noEncrypt";
        tests.serializerType = JavaSerializer.class;
        tests.encrypterType = null;
        tests.toString = "SerializationEncoder[JavaSerializer, no encrypter]";
        data.add(tests);

        tests = new SessionEncoderTests();
        tests.beanName = "hessian_aes";
        tests.serializerType = HessianSerializer.class;
        tests.encrypterType = AesEncrypter.class;
        tests.toString = "SerializationEncoder[HessianSerializer, AES(keySize=128)]";
        data.add(tests);

        tests = new SessionEncoderTests();
        tests.beanName = "java_aes";
        tests.serializerType = JavaSerializer.class;
        tests.encrypterType = AesEncrypter.class;
        tests.toString = "SerializationEncoder[JavaSerializer, AES(keySize=128)]";
        data.add(tests);

        tests = new SessionEncoderTests();
        tests.beanName = "hessian_aes_strong";
        tests.serializerType = HessianSerializer.class;
        tests.encrypterType = AesEncrypter.class;
        tests.toString = "SerializationEncoder[HessianSerializer, AES(keySize=256)]";
        tests.skipEncoding = true;
        data.add(tests);

        return data;
    }

    @Test
    public void encode_null() {
        try {
            encoder.encode(null, null);
            fail();
        } catch (SessionEncoderException e) {
            assertThat(e, exception(IllegalArgumentException.class, "objectToEncode is null"));
        }
    }

    @Test
    public void decode_null() {
        try {
            encoder.decode(null, null);
            fail();
        } catch (SessionEncoderException e) {
            assertThat(e, exception(IllegalArgumentException.class, "encodedValue is null"));
        }
    }

    @Test
    public void serializer() {
        if (serializerType == null) {
            assertNull(encoder.serializer);
        } else {
            assertTrue(serializerType.isInstance(encoder.serializer));
        }
    }

    @Test
    public void encrypter() {
        if (encrypterType == null) {
            assertNull(encoder.encrypter);
        } else {
            assertTrue(encrypterType.isInstance(encoder.encrypter));
        }
    }

    @Test
    public void encode_decode() {
        if (skipEncoding) {
            return;
        }

        Map<String, Object> obj = createHashMap();

        obj.put("int", 123);
        obj.put("String", "test");
        obj.put("Array", new String[] { "11", "22", "33" });

        String encoded = encoder.encode(obj, null);

        System.out.printf("%20s: length=%4d,  encoded=%s%n", beanName, encoded.length(), encoded);
        assertNotNull(encoded);

        Map<String, Object> newobj = encoder.decode(encoded, null);

        assertEquals(obj.size(), newobj.size());
        assertEquals(obj.get("int"), newobj.get("int"));
        assertEquals(obj.get("String"), newobj.get("String"));
        assertArrayEquals((String[]) obj.get("Array"), (String[]) newobj.get("Array"));
    }

    @Test
    public void toString_() {
        assertEquals(toString, encoder.toString());
    }
}
