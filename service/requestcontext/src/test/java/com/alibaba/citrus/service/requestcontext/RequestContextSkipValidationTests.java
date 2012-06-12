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

package com.alibaba.citrus.service.requestcontext;

import static com.alibaba.citrus.test.TestEnvStatic.*;
import static com.alibaba.citrus.test.TestUtil.*;
import static org.junit.Assert.*;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import com.alibaba.citrus.logconfig.support.ConfigurableLogger.Level;
import com.alibaba.citrus.service.requestcontext.impl.RequestContextChainingServiceImpl;
import com.alibaba.citrus.service.requestcontext.locale.impl.SetLocaleRequestContextFactoryImpl;
import com.alibaba.citrus.service.requestcontext.parser.impl.ParserRequestContextFactoryImpl;
import com.alibaba.citrus.service.requestcontext.session.SessionConfig;
import com.alibaba.citrus.service.requestcontext.session.SessionConfig.StoresConfig;
import com.alibaba.citrus.service.requestcontext.session.encoder.SessionEncoder;
import com.alibaba.citrus.service.requestcontext.session.encoder.impl.SerializationEncoder;
import com.alibaba.citrus.service.requestcontext.session.encrypter.impl.AesEncrypter;
import com.alibaba.citrus.service.requestcontext.session.idgen.random.impl.RandomIDGenerator;
import com.alibaba.citrus.service.requestcontext.session.impl.SessionRequestContextFactoryImpl;
import com.alibaba.citrus.service.requestcontext.session.interceptor.SessionLifecycleLogger;
import com.alibaba.citrus.service.requestcontext.session.store.cookie.impl.CookieStoreImpl;
import com.alibaba.citrus.service.requestcontext.session.store.cookie.impl.SingleValuedCookieStoreImpl;
import com.alibaba.citrus.springext.support.context.XmlApplicationContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.FileSystemResource;

@RunWith(Parameterized.class)
public class RequestContextSkipValidationTests {
    private final boolean                           skipValidation;
    private       ApplicationContext                factory;
    private       RequestContextChainingServiceImpl service;

    public RequestContextSkipValidationTests(boolean skipValidation) {
        this.skipValidation = skipValidation;
    }

    @Parameters
    public static List<Object[]> data() {
        return Arrays.asList(new Object[][] { { false }, { true } });
    }

    @Before
    public void init() {
        if (skipValidation) {
            System.setProperty("skipValidation", "true");
        }

        factory = new XmlApplicationContext(new FileSystemResource(new File(srcdir, "services-skip-validation.xml")));
    }

    @After
    public void dispose() {
        System.clearProperty("skipValidation");
    }

    @Test
    public void requestContexts() {
        service = (RequestContextChainingServiceImpl) factory.getBean("rc1");

        assertEquals(true, getFieldValue(service, "sort", null));
        assertEquals(false, getFieldValue(service, "threadContextInheritable", null));
    }

    @Test
    public void setLocale() {
        service = (RequestContextChainingServiceImpl) factory.getBean("rc2");
        SetLocaleRequestContextFactoryImpl f = getFactory(service, SetLocaleRequestContextFactoryImpl.class);

        assertEquals("_input_charset", getFieldValue(f, "inputCharsetParam", null));
        assertEquals("_output_charset", getFieldValue(f, "outputCharsetParam", null));
        assertEquals("en_US", getFieldValue(f, "defaultLocale", null).toString());
        assertEquals("UTF-8", getFieldValue(f, "defaultCharset", null));
        assertEquals("_lang", getFieldValue(f, "sessionKey", null));
        assertEquals("_lang", getFieldValue(f, "paramKey", null));
    }

    @Test
    public void parser() {
        service = (RequestContextChainingServiceImpl) factory.getBean("rc2");
        ParserRequestContextFactoryImpl f = getFactory(service, ParserRequestContextFactoryImpl.class);

        assertEquals(true, getFieldValue(f, "converterQuiet", null));
        assertEquals("lower_with_underscores", getFieldValue(f, "caseFolding", null));
        assertEquals(true, getFieldValue(f, "autoUpload", null));
        assertEquals(true, getFieldValue(f, "unescapeParameters", null));
        assertEquals(".~html", getFieldValue(f, "htmlFieldSuffix", null));
        assertEquals(false, getFieldValue(f, "useServletEngineParser", null));
        assertEquals(true, getFieldValue(f, "useBodyEncodingForURI", null));
        assertEquals("UTF-8", getFieldValue(f, "uriEncoding", null));
        assertEquals(true, getFieldValue(f, "trimming", null));
    }

    @Test
    public void session() {
        service = (RequestContextChainingServiceImpl) factory.getBean("rc2");
        SessionRequestContextFactoryImpl f = getFactory(service, SessionRequestContextFactoryImpl.class);
        SessionConfig config = f.getConfig();

        assertEquals("SESSION_MODEL", config.getModelKey());
        assertEquals(false, config.isKeepInTouch());

        assertEquals("JSESSIONID", config.getId().getCookie().getName());
        assertEquals("/", config.getId().getCookie().getPath());
        assertEquals(0, config.getId().getCookie().getMaxAge());
        assertEquals(true, config.getId().getCookie().isHttpOnly());
        assertEquals(false, config.getId().getCookie().isSecure());

        assertEquals("JSESSIONID", config.getId().getUrlEncode().getName());

        assertEquals(true, config.getId().isCookieEnabled());
        assertEquals(false, config.getId().isUrlEncodeEnabled());
    }

    @Test
    public void cookiestore() {
        service = (RequestContextChainingServiceImpl) factory.getBean("rc2");
        SessionRequestContextFactoryImpl f = getFactory(service, SessionRequestContextFactoryImpl.class);
        SessionConfig config = f.getConfig();

        StoresConfig stores = config.getStores();
        CookieStoreImpl cookieStore = (CookieStoreImpl) stores.getStore("s1");

        assertEquals("/", getFieldValue(cookieStore, "path", null));
        assertEquals(0, getFieldValue(cookieStore, "maxAge", null));
        assertEquals(true, getFieldValue(cookieStore, "httpOnly", null));
        assertEquals(false, getFieldValue(cookieStore, "secure", null));
        assertEquals(false, getFieldValue(cookieStore, "survivesInInvalidating", null));

        assertEquals(3896, getFieldValue(cookieStore, "maxLength", null));
        assertEquals(5, getFieldValue(cookieStore, "maxCount", null));
        assertEquals(false, getFieldValue(cookieStore, "checksum", null));
        assertEquals(false, getFieldValue(cookieStore, "survivesInInvalidating", null));
    }

    @Test
    public void singlevalued_cookiestore() {
        service = (RequestContextChainingServiceImpl) factory.getBean("rc2");
        SessionRequestContextFactoryImpl f = getFactory(service, SessionRequestContextFactoryImpl.class);
        SessionConfig config = f.getConfig();

        StoresConfig stores = config.getStores();
        SingleValuedCookieStoreImpl cookieStore = (SingleValuedCookieStoreImpl) stores.getStore("s2");

        assertEquals("/", getFieldValue(cookieStore, "path", null));
        assertEquals(0, getFieldValue(cookieStore, "maxAge", null));
        assertEquals(true, getFieldValue(cookieStore, "httpOnly", null));
        assertEquals(false, getFieldValue(cookieStore, "secure", null));
        assertEquals(false, getFieldValue(cookieStore, "survivesInInvalidating", null));
    }

    @Test
    public void aes_encrypter() {
        service = (RequestContextChainingServiceImpl) factory.getBean("rc2");
        SessionRequestContextFactoryImpl f = getFactory(service, SessionRequestContextFactoryImpl.class);
        SessionConfig config = f.getConfig();

        StoresConfig stores = config.getStores();
        CookieStoreImpl cookieStore = (CookieStoreImpl) stores.getStore("s1");
        SessionEncoder[] encoders = getFieldValue(cookieStore, "encoders", SessionEncoder[].class);
        SerializationEncoder sencoder = (SerializationEncoder) encoders[0];
        AesEncrypter aes = (AesEncrypter) sencoder.getEncrypter();

        assertEquals(128, aes.getKeySize());
        assertEquals(256, aes.getPoolSize());
    }

    @Test
    public void random_id() {
        service = (RequestContextChainingServiceImpl) factory.getBean("rc2");
        SessionRequestContextFactoryImpl f = getFactory(service, SessionRequestContextFactoryImpl.class);
        SessionConfig config = f.getConfig();
        RandomIDGenerator rid = (RandomIDGenerator) config.getId().getGenerator();

        assertEquals(16, rid.getLength());
    }

    @Test
    public void lifecycle_log() {
        service = (RequestContextChainingServiceImpl) factory.getBean("rc2");
        SessionRequestContextFactoryImpl f = getFactory(service, SessionRequestContextFactoryImpl.class);
        SessionConfig config = f.getConfig();
        SessionLifecycleLogger sll = (SessionLifecycleLogger) config.getSessionInterceptors()[0];

        assertEquals("debug", getFieldValue(sll, "logLevel", Level.class).toString());
        assertEquals("trace", getFieldValue(sll, "visitLogLevel", Level.class).toString());
    }

    private <F extends RequestContextFactory<?>> F getFactory(RequestContextChainingServiceImpl service, Class<F> type) {
        @SuppressWarnings("unchecked")
        List<RequestContextFactory<?>> factories = getFieldValue(service, "factories", List.class);

        for (RequestContextFactory<?> factory : factories) {
            if (type.isInstance(factory)) {
                return type.cast(factory);
            }
        }

        fail();
        return null;
    }
}
