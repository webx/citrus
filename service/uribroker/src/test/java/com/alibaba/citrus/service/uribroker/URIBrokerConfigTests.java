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

package com.alibaba.citrus.service.uribroker;

import static com.alibaba.citrus.test.TestUtil.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static java.util.Collections.*;
import static org.junit.Assert.*;

import java.util.List;
import javax.servlet.http.HttpServletRequest;

import com.alibaba.citrus.service.pull.PullService;
import com.alibaba.citrus.service.uribroker.impl.URIBrokerServiceImpl;
import com.alibaba.citrus.service.uribroker.interceptor.URIBrokerInterceptor;
import com.alibaba.citrus.service.uribroker.interceptor.URIBrokerPathInterceptor;
import com.alibaba.citrus.service.uribroker.uri.GenericURIBroker;
import com.alibaba.citrus.service.uribroker.uri.URIBroker;
import com.alibaba.citrus.service.uribroker.uri.URIBroker.URIType;
import com.alibaba.citrus.util.i18n.LocaleUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

/**
 * 测试uribroker的配置。
 *
 * @author Michael Zhou
 */
public class URIBrokerConfigTests extends AbstractURIBrokerServiceTests {
    private GenericURIBroker uri;

    @Before
    public void init() throws Exception {
        LocaleUtil.setContext(null, "UTF-8");
    }

    @After
    public void dispose() {
        LocaleUtil.resetContext();
    }

    @Test
    public void withoutRequest() {
        ApplicationContext factory = createContext("services.xml", null);
        service = (URIBrokerServiceImpl) factory.getBean("uriBrokerService");

        assertNotNull(service);
        assertNull(getFieldValue(service, "request", HttpServletRequest.class));
        assertSame(service, factory.getBean("uris"));

        uri = (GenericURIBroker) service.getURIBroker("link");
        assertEquals("http:///", uri.toString());
    }

    @Test
    public void link_URIBroker() {
        uri = (GenericURIBroker) service.getURIBroker("link");

        assertEquals(null, uri.getURIType());

        assertEquals("http://localhost/", uri.render());
        assertEquals("http://user:pass@www.alibaba.com/aa/bb",
                     uri.setServerURI("http://user:pass@www.alibaba.com/aa/bb").render());
        assertEquals("http://www.alibaba.com/", uri.setServerName("www.alibaba.com").render());
        assertEquals("http://localhost:8080/", uri.setServerPort(8080).render());
        assertEquals("http://localhost/", uri.setServerPort(80).render());
        assertEquals("https://localhost:80/", uri.setServerScheme("https").render());
        assertEquals("http://localhost/aa/bb", uri.addPath("aa").addPath("bb").render());

        // fork
        GenericURIBroker newUri = (GenericURIBroker) uri.setServerURI("http://www.taobao.com/aa/bb").fork();

        assertEquals("http://www.taobao.com/aa/bb/cc", newUri.addPath("cc").render());
        assertEquals("http://www.taobao.com/aa/bb/dd", newUri.addPath("dd").render());
    }

    @Test
    public void link2_URIBroker() {
        uri = (GenericURIBroker) service.getURIBroker("link2");

        assertEquals(URIType.relative, uri.getURIType());

        assertEquals("http://myuser2:mypass2@myservername2:1234/aaa/a1/bbb/ccc/ddd?"
                     + "aaa=1111&bbb=2222&ccc=3333#myreference2", uri.render());

        assertEquals("http://myuser2:mypass2@myservername2:1234/aaa/a1/bbb/ccc/ddd?"
                     + "aaa=1111&bbb=2222&ccc=3333#myreference2", uri.toString());
    }

    @Test
    public void link3_URIBroker_extendsLink2_overrideWithEmpty() {
        uri = (GenericURIBroker) service.getURIBroker("link3");
        assertEquals(URIType.relative, uri.getURIType());
        assertEquals("https:///aaa/a1/bbb/ccc/ddd?aaa=1111&bbb=2222&ccc=3333", uri.toString());
    }

    @Test
    public void link4_serverURI() {
        uri = (GenericURIBroker) service.getURIBroker("link4");
        assertEquals("http://user:pass@server:1234/a/b/c?a=1&b=+&c=#ref", uri.toString());
        assertEquals("http://user:pass@server:1234/a/b/c", uri.getServerURI());
    }

    @Test
    public void mylink_URIBoker_variableReplacement() {
        uri = (GenericURIBroker) service.getURIBroker("mylink");
        assertEquals("http://www.taobao.com/", uri.render());
    }

    @Test
    public void linkCharset_URLEncoding() {
        // 未指明charset的，使用context中指定的charset进行URL encoding。
        uri = (GenericURIBroker) service.getURIBroker("link");
        assertEquals("http://localhost/?str=%E4%B8%AD%E5%9B%BD", uri.addQueryData("str", "中国").render());

        // 指明charset的，使用指定的charset进行URL encoding。
        uri = (GenericURIBroker) service.getURIBroker("linkCharset");
        assertEquals("http://localhost/?str=%D6%D0%B9%FA", uri.addQueryData("str", "中国").render());
    }

    @Test
    public void linkWithInterceptor_URIBroker() {
        PullService pull = (PullService) factory.getBean("pullService");

        // 确保uri interceptor在render之前没有被执行过
        // 曾经的bug：pull service在logging为debug时，会提前执行uri.interceptors。
        uri = (GenericURIBroker) pull.getContext().pull("linkWithInterceptor");
        assertTrue(uri.addQueryData("path", "hello").render().startsWith("http://www.mydomain.com/hello?path=hello&r="));

        assertTrue(uri.toString().startsWith("http://localhost/"));
        assertTrue(uri.render().startsWith("http://www.mydomain.com/abc?r="));

        // 添加另一个interceptor
        uri.addInterceptor(new URIBrokerInterceptor() {
            public void perform(URIBroker broker) {
                broker.addQueryData("id", 1);
            }
        });

        assertTrue(uri.render().startsWith("http://www.mydomain.com/abc?id=1&r="));
    }

    @Test
    public void linkWithPathInterceptor_URIBroker() {
        uri = (GenericURIBroker) service.getURIBroker("linkWithPathInterceptor");

        assertEquals("http://www.mydomain.com/abc/def", uri.render());

        // 添加另一个interceptor
        uri.addInterceptor(new URIBrokerPathInterceptor() {
            public void perform(URIBroker broker) {
                broker.setServerName("www.mydomain1.com");
            }

            public String perform(URIBroker broker, String path) {
                broker.addQueryData("id", 1);
                return path;
            }
        });

        assertEquals("http://www.mydomain1.com/abc/def?id=1", uri.render());
    }

    @Test
    public void not_requestAware() {
        service = (URIBrokerServiceImpl) factory.getBean("not-requestAware");

        for (String name : service.getNames()) {
            URIBroker uri = service.getURIBroker(name);

            if ("link-request".equals(name)) {
                assertEquals("http://localhost/", uri.render());
            } else {
                assertEquals("http:///", uri.render());
                assertEquals("http://www.alibaba.com:9999/", uri.setServerName("www.alibaba.com").setServerPort(9999)
                                                                .render());
            }
        }
    }

    @Test
    public void standalone() {
        URIBroker broker = (URIBroker) factory.getBean("standalone_uri");
        assertEquals("http://www.taobao.com/hello", broker.toString());
    }

    @Test
    public void noCharset() {
        service = (URIBrokerServiceImpl) factory.getBean("noDefaultCharset");

        URIBroker link1 = service.getURIBroker("a");
        assertEquals(null, link1.getCharset());
        assertEquals("http://localhost/?name=%E4%B8%AD%E5%9B%BD", link1.addQueryData("name", "中国").render()); // from context

        URIBroker link2 = service.getURIBroker("b");
        assertEquals("GBK", link2.getCharset());
        assertEquals("http://localhost/?name=%D6%D0%B9%FA", link2.addQueryData("name", "中国").render());
    }

    @Test
    public void defaultCharset() {
        service = (URIBrokerServiceImpl) factory.getBean("withDefaultCharset");

        URIBroker link1 = service.getURIBroker("x");
        assertEquals("UTF-8", link1.getCharset());
        assertEquals("http://localhost/?name=%E4%B8%AD%E5%9B%BD", link1.addQueryData("name", "中国").render());

        URIBroker link2 = service.getURIBroker("y");
        assertEquals("GBK", link2.getCharset());
        assertEquals("http://localhost/?name=%D6%D0%B9%FA", link2.addQueryData("name", "中国").render());
    }

    @Test
    public void uriBean() {
        service = (URIBrokerServiceImpl) factory.getBean("uri_bean");

        URIBroker bean = service.getURIBroker("mybean");
        assertEquals("https://myserver/aa/bb/cc", bean.render());
    }

    @Test
    public void importAndOverride() {
        service = (URIBrokerServiceImpl) factory.getBean("import");
        URIBrokerServiceImpl imported = (URIBrokerServiceImpl) factory.getBean("uris");

        // check exposed names
        List<String> importedNames = createArrayList(imported.getExposedNames());
        List<String> names = createArrayList(service.getExposedNames());

        names.add("link2");

        sort(importedNames);
        sort(names);

        assertEquals(importedNames, names);

        // check all names
        importedNames = createArrayList(imported.getNames());
        names = createArrayList(service.getNames());

        importedNames.add("newlink");

        sort(importedNames);
        sort(names);

        assertEquals(importedNames, names);

        // check uris
        for (String name : names) {
            URIBroker uri = service.getURIBroker(name);
            URIBroker importedUri = imported.getURIBroker(name);

            if (uri != null) {
                uri = uri.getParent();
                assertFalse(uri.isAutoReset());
            }

            if (importedUri != null) {
                importedUri = importedUri.getParent();
                assertFalse(importedUri.isAutoReset());
            }

            if ("link2".equals(name)) {
                assertEquals("http:///", uri.toString());
                assertEquals(
                        "http://myuser2:mypass2@myservername2:1234/aaa/a1/bbb/ccc/ddd?aaa=1111&bbb=2222&ccc=3333#myreference2",
                        importedUri.toString());
            } else if ("newlink".equals(name)) {
                assertEquals("http://www.taobao.com/newlink", uri.toString());
                assertNull(importedUri);
            } else {
                assertNotNull(uri);
                assertSame(importedUri, uri);
            }
        }
    }
}
