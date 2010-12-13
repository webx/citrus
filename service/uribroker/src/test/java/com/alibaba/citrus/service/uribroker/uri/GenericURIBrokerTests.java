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
package com.alibaba.citrus.service.uribroker.uri;

import static com.alibaba.citrus.test.TestUtil.*;
import static com.alibaba.citrus.util.BasicConstant.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static org.easymock.EasyMock.*;
import static org.easymock.classextension.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.junit.Test;
import org.springframework.beans.factory.ObjectFactory;

import com.alibaba.citrus.service.uribroker.interceptor.URIBrokerPathInterceptor;
import com.alibaba.citrus.service.uribroker.uri.URIBroker.URIType;
import com.alibaba.citrus.util.i18n.LocaleUtil;

public class GenericURIBrokerTests extends AbstractURIBrokerFeaturesTests<GenericURIBroker> {
    @Override
    protected void setupParentBroker(GenericURIBroker parent) {
        super.setupParentBroker(parent);

        parent.setURIType(URIType.absolute);
        parent.setBaseURI("http://www.alibaba.com/a/../hello/world");
        parent.setServerScheme("https");
        parent.setServerName("localhost");
        parent.setServerPort(443);
        parent.setLoginUser("user");
        parent.setLoginPassword("pass");
        parent.setReference("ref");

        parent.addPath("/aa/bb");
        parent.addQueryData("a", 1);
        parent.addQueryData("b", 2);
    }

    @Override
    protected void setupBroker(GenericURIBroker broker) {
        super.setupBroker(broker);

        broker.setURIType(URIType.relative);
        broker.setBaseURI("http://www.taobao.com//hello/world");
        broker.setServerScheme("http");
        broker.setServerName("taobao.com");
        broker.setServerPort(8888);
        broker.setLoginUser("user2");
        broker.setLoginPassword("pass2");
        broker.setReference("ref2");

        broker.addPath("/cc/dd");
        broker.addQueryData("a", 3);
        broker.addQueryData("a", 4);
        broker.addQueryData("c", 4);
    }

    @Override
    protected void assertParentBroker(GenericURIBroker broker) {
        super.assertParentBroker(broker);

        assertEquals(URIType.absolute, broker.getURIType());
        assertEquals("http://www.alibaba.com/hello/world", broker.getBaseURI());
        assertEquals("https", broker.getServerScheme());
        assertEquals("localhost", broker.getServerName());
        assertEquals(443, broker.getServerPort());
        assertEquals("user", broker.getLoginUser());
        assertEquals("pass", broker.getLoginPassword());
        assertEquals("ref", broker.getReference());

        assertEquals("/aa/bb", broker.getPath());
        assertEquals("/aa/bb", broker.getPath());
        assertArrayEquals(new String[] { "aa", "bb" },
                broker.getPathElements().toArray(new String[broker.getPathElements().size()]));

        assertEquals(2, broker.getQuery().size());
        Iterator<Map.Entry<String, Object>> i = broker.getQuery().entrySet().iterator();
        assertEquals("a=1", toString(i.next()));
        assertEquals("b=2", toString(i.next()));
    }

    @Override
    protected void assertBroker(GenericURIBroker broker) {
        super.assertBroker(broker);

        assertEquals(URIType.relative, broker.getURIType());
        assertEquals("http://www.taobao.com/hello/world", broker.getBaseURI());
        assertEquals("http", broker.getServerScheme());
        assertEquals("taobao.com", broker.getServerName());
        assertEquals(8888, broker.getServerPort());
        assertEquals("user2", broker.getLoginUser());
        assertEquals("pass2", broker.getLoginPassword());
        assertEquals("ref2", broker.getReference());

        assertEquals("/aa/bb/cc/dd", broker.getPath());
        assertArrayEquals(new String[] { "aa", "bb", "cc", "dd" },
                broker.getPathElements().toArray(new String[broker.getPathElements().size()]));

        assertEquals(3, broker.getQuery().size());
        Iterator<Map.Entry<String, Object>> i = broker.getQuery().entrySet().iterator();
        assertEquals("a=[1, 3, 4]", toString(i.next()));
        assertEquals("b=2", toString(i.next()));
        assertEquals("c=4", toString(i.next()));
    }

    private String toString(Map.Entry<String, Object> entry) {
        String id = entry.getKey();
        Object values = entry.getValue();

        if (values instanceof String[]) {
            return id + "=" + Arrays.toString((String[]) values);
        } else {
            return id + "=" + values;
        }
    }

    @Override
    protected void assertAfterReset_noParent(GenericURIBroker broker) {
        super.assertAfterReset_noParent(broker);

        assertEquals(null, broker.getURIType());
        assertEquals(null, broker.getBaseURI());
        assertEquals(null, broker.getServerScheme());
        assertEquals(null, broker.getServerName());
        assertEquals(-1, broker.getServerPort());
        assertEquals(null, broker.getLoginUser());
        assertEquals(null, broker.getLoginPassword());
        assertEquals(null, broker.getReference());

        assertTrue(broker.getQuery().isEmpty());
        assertEquals("", broker.getPath());
        assertTrue(broker.getPathElements().isEmpty());
    }

    @Test
    public void reset_withRequest() {
        // 空broker，not request aware，web环境
        request = getMockRequest("https", "taobao.com", 8888);
        broker.setRequest(request);
        broker.setRequestAware(false);
        assertFalse(broker.isRequestAware());
        request.getServerName(); // no exception

        broker.reset();
        assertEquals(null, broker.getServerScheme());
        assertEquals(null, broker.getServerName());
        assertEquals(-1, broker.getServerPort());
        assertEquals(null, broker.getLoginUser());
        assertEquals(null, broker.getLoginPassword());
        assertEquals(null, broker.getReference());

        // 空broker，request aware, 非web环境
        request = getMockRequest();
        broker.setRequest(request);
        broker.setRequestAware(true);
        assertTrue(broker.isRequestAware());

        try {
            ((ObjectFactory) request).getObject();
            fail();
        } catch (IllegalStateException e) {
        }

        broker.reset();
        assertEquals(null, broker.getServerScheme());
        assertEquals(null, broker.getServerName());
        assertEquals(-1, broker.getServerPort());
        assertEquals(null, broker.getLoginUser());
        assertEquals(null, broker.getLoginPassword());
        assertEquals(null, broker.getReference());

        // 空broker，request aware web环境
        request = getMockRequest("https", "taobao.com", 8888);
        broker.setRequest(request);
        broker.setRequestAware(true);
        assertTrue(broker.isRequestAware());
        request.getServerName(); // no exception

        broker.reset();
        assertEquals("https", broker.getServerScheme());
        assertEquals("taobao.com", broker.getServerName());
        assertEquals(8888, broker.getServerPort());
        assertEquals(null, broker.getLoginUser());
        assertEquals(null, broker.getLoginPassword());
        assertEquals(null, broker.getReference());

        // 非空broker, request aware, web环境
        GenericURIBroker parent = newInstance();
        parent.setServerName("localhost");

        request = getMockRequest("https", "taobao.com", 8888);
        broker.setRequest(request);
        broker.setRequestAware(true);
        broker.setParent(parent);
        assertTrue(broker.isRequestAware());
        request.getServerName(); // no exception

        broker.reset();
        assertEquals(null, broker.getServerScheme());
        assertEquals("localhost", broker.getServerName());
        assertEquals(-1, broker.getServerPort());
        assertEquals(null, broker.getLoginUser());
        assertEquals(null, broker.getLoginPassword());
        assertEquals(null, broker.getReference());
    }

    @Test
    public void setURIType() {
        // default value
        assertEquals(null, broker.getURIType());

        // set value
        broker.autoURI();
        assertEquals(URIType.auto, broker.getURIType());

        broker.fullURI();
        assertEquals(URIType.full, broker.getURIType());

        broker.absoluteURI();
        assertEquals(URIType.absolute, broker.getURIType());

        broker.relativeURI();
        assertEquals(URIType.relative, broker.getURIType());
    }

    @Test
    public void setBaseURI() {
        // init value
        assertNull(broker.getBaseURI());

        // set empty
        broker.setBaseURI(null);
        assertNull(broker.getBaseURI());

        broker.setBaseURI("  ");
        assertNull(broker.getBaseURI());

        // set unnormalized uri
        broker.setBaseURI("http://localhost/aaa/..//bbb");
        assertEquals("http://localhost/bbb", broker.getBaseURI());

        // set null
        broker.setBaseURI(null);
        assertNull(broker.getBaseURI());
    }

    @Test
    public void setServerURI() {
        // 设置broker.autoReset=true，确保getServerURI不会reset broker
        broker = (GenericURIBroker) broker.fork();
        assertTrue(broker.isAutoReset());

        // empty
        try {
            broker.setServerURI((String) null);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("serverURI"));
        }

        try {
            broker.setServerURI("  ");
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("serverURI"));
        }

        // full uri
        broker.reset();
        broker.addQueryData("x", 1);
        broker.setServerURI(" https://user:pass@taobao.com:8888/aaa/bbb/ccc?a=1#ref ");

        assertEquals("https", broker.getServerScheme());
        assertEquals("taobao.com", broker.getServerName());
        assertEquals(8888, broker.getServerPort());
        assertEquals("user", broker.getLoginUser());
        assertEquals("pass", broker.getLoginPassword());
        assertEquals("/aaa/bbb/ccc", broker.getPath());
        assertArrayEquals(new String[] { "aaa", "bbb", "ccc" }, broker.getPathElements().toArray(new String[0]));

        assertEquals(null, broker.getReference()); // serverURI不设置ref
        assertArrayEquals(new String[] { "x" }, broker.getQuery().keySet().toArray(EMPTY_STRING_ARRAY)); // serverURI不设置query

        broker.setReference("ref2");
        assertEquals("https://user:pass@taobao.com:8888/aaa/bbb/ccc", broker.getServerURI());
        assertEquals("https://user:pass@taobao.com:8888/aaa/bbb/ccc?x=1#ref2", broker.toString());

        // without password
        broker.reset();
        broker.addQueryData("x", 1);
        broker.setServerURI(" https://user@taobao.com:8888/aaa/bbb/ccc?a=1#ref ");

        assertEquals("https", broker.getServerScheme());
        assertEquals("taobao.com", broker.getServerName());
        assertEquals(8888, broker.getServerPort());
        assertEquals("user", broker.getLoginUser());
        assertEquals(null, broker.getLoginPassword());
        assertEquals("/aaa/bbb/ccc", broker.getPath());
        assertArrayEquals(new String[] { "aaa", "bbb", "ccc" }, broker.getPathElements().toArray(new String[0]));

        assertEquals(null, broker.getReference()); // serverURI不设置ref
        assertArrayEquals(new String[] { "x" }, broker.getQuery().keySet().toArray(EMPTY_STRING_ARRAY)); // serverURI不设置query

        broker.setReference("ref2");
        assertEquals("https://user@taobao.com:8888/aaa/bbb/ccc", broker.getServerURI());
        assertEquals("https://user@taobao.com:8888/aaa/bbb/ccc?x=1#ref2", broker.toString());

        // without user
        broker.reset();
        broker.addQueryData("x", 1);
        broker.setServerURI(" https://taobao.com:8888/aaa/bbb/ccc?a=1#ref ");

        assertEquals("https", broker.getServerScheme());
        assertEquals("taobao.com", broker.getServerName());
        assertEquals(8888, broker.getServerPort());
        assertEquals(null, broker.getLoginUser());
        assertEquals(null, broker.getLoginPassword());
        assertEquals("/aaa/bbb/ccc", broker.getPath());
        assertArrayEquals(new String[] { "aaa", "bbb", "ccc" }, broker.getPathElements().toArray(new String[0]));

        assertEquals(null, broker.getReference()); // serverURI不设置ref
        assertArrayEquals(new String[] { "x" }, broker.getQuery().keySet().toArray(EMPTY_STRING_ARRAY)); // serverURI不设置query

        broker.setReference("ref2");
        assertEquals("https://taobao.com:8888/aaa/bbb/ccc", broker.getServerURI());
        assertEquals("https://taobao.com:8888/aaa/bbb/ccc?x=1#ref2", broker.toString());

        // without port
        broker.reset();
        broker.addQueryData("x", 1);
        broker.setServerURI(" https://taobao.com/aaa/bbb/ccc?a=1#ref ");

        assertEquals("https", broker.getServerScheme());
        assertEquals("taobao.com", broker.getServerName());
        assertEquals(-1, broker.getServerPort());
        assertEquals(null, broker.getLoginUser());
        assertEquals(null, broker.getLoginPassword());
        assertEquals("/aaa/bbb/ccc", broker.getPath());
        assertArrayEquals(new String[] { "aaa", "bbb", "ccc" }, broker.getPathElements().toArray(new String[0]));

        assertEquals(null, broker.getReference()); // serverURI不设置ref
        assertArrayEquals(new String[] { "x" }, broker.getQuery().keySet().toArray(EMPTY_STRING_ARRAY)); // serverURI不设置query

        broker.setReference("ref2");
        assertEquals("https://taobao.com/aaa/bbb/ccc", broker.getServerURI());
        assertEquals("https://taobao.com/aaa/bbb/ccc?x=1#ref2", broker.toString());

        // set twice
        broker.reset();
        broker.addQueryData("x", 1);
        broker.setServerURI("http://taobao.com/aaa/bbb/ccc?a=1");
        broker.setServerURI("http://taobao.com/aaa/bbb/ccc");

        assertEquals("/aaa/bbb/ccc", broker.getPath());
        assertArrayEquals(new String[] { "aaa", "bbb", "ccc" }, broker.getPathElements().toArray(new String[0]));

        broker.setReference("ref2");
        assertEquals("http://taobao.com/aaa/bbb/ccc", broker.getServerURI());
        assertEquals("http://taobao.com/aaa/bbb/ccc?x=1#ref2", broker.toString());

        // with interceptor, URIType=relative
        broker.reset();
        broker.addQueryData("x", 1);
        broker.setURIType(URIType.relative);
        broker.setBaseURI("http://taobao.com/");
        broker.setServerURI("http://taobao.com/aaa/bbb/ccc");
        broker.addInterceptor(new URIBrokerPathInterceptor() {
            public void perform(URIBroker broker) {
                broker.addQueryData("y", 2);
                broker.setReference("ref2");
            }

            public String perform(URIBroker broker, String path) {
                return "/hello" + path;
            }
        });

        assertEquals("http://taobao.com/hello/aaa/bbb/ccc", broker.getServerURI());
        assertEquals("hello/aaa/bbb/ccc?x=1&y=2#ref2", broker.toString());
    }

    @Test
    public void setServerScheme() {
        // init value == null
        assertNull(broker.getServerScheme());

        // empty value
        broker.setServerScheme(null);
        assertNull(broker.getServerScheme());

        broker.setServerScheme("  ");
        assertEquals("", broker.getServerScheme());

        // simple value
        broker.setServerScheme(" http");
        assertEquals("http", broker.getServerScheme());
    }

    @Test
    public void setServerName() {
        // init value == null
        assertNull(broker.getServerName());

        // empty value
        broker.setServerName(null);
        assertNull(broker.getServerName());

        broker.setServerName("  ");
        assertEquals("", broker.getServerName());

        // simple value
        broker.setServerName(" taobao");
        assertEquals("taobao", broker.getServerName());
    }

    @Test
    public void setServerPort() {
        // init value == -1
        assertEquals(-1, broker.getServerPort());

        // empty value
        broker.setServerPort(0);
        assertEquals(-1, broker.getServerPort());

        broker.setServerPort(-10);
        assertEquals(-1, broker.getServerPort());

        // simple value
        broker.setServerPort(8080);
        assertEquals(8080, broker.getServerPort());
    }

    @Test
    public void setLoginUser() {
        // init value == null
        assertNull(broker.getLoginUser());

        // empty value
        broker.setLoginUser(null);
        assertNull(broker.getLoginUser());

        broker.setLoginUser("  ");
        assertEquals("", broker.getLoginUser());

        // simple value
        broker.setLoginUser(" user");
        assertEquals("user", broker.getLoginUser());
    }

    @Test
    public void setLoginPassword() {
        // init value == null
        assertNull(broker.getLoginPassword());

        // empty value
        broker.setLoginPassword(null);
        assertNull(broker.getLoginPassword());

        broker.setLoginPassword("  ");
        assertEquals("", broker.getLoginPassword());

        // simple value
        broker.setLoginPassword(" pass");
        assertEquals("pass", broker.getLoginPassword());
    }

    @Test
    public void setReference() {
        // init value == null
        assertNull(broker.getReference());

        // empty value
        broker.setReference(null);
        assertNull(broker.getReference());

        broker.setReference("  ");
        assertEquals("", broker.getReference());

        // simple value
        broker.setReference(" ref");
        assertEquals("ref", broker.getReference());
    }

    @Test
    public void pathSegment() {
        class MyBroker extends URIBroker {
            @Override
            protected int getPathSegmentCount() {
                return 3;
            }

            @Override
            protected URIBroker newInstance() {
                return new MyBroker();
            }
        }

        URIBroker broker = new MyBroker();

        // wrong segment
        for (int i = 0; i < 8; i++) {
            int segment = -1;

            try {
                switch (i) {
                    case 0:
                        broker.getPathSegment(segment = -1);
                        break;

                    case 1:
                        broker.getPathSegment(segment = 3);
                        break;

                    case 2:
                        broker.getPathSegmentAsString(segment = -1);
                        break;

                    case 3:
                        broker.getPathSegmentAsString(segment = 3);
                        break;

                    case 4:
                        broker.addPathSegment(segment = -1, "");
                        break;

                    case 5:
                        broker.addPathSegment(segment = 3, "");
                        break;

                    case 6:
                        broker.clearPathSegment(segment = -1);
                        break;

                    case 7:
                        broker.clearPathSegment(segment = 3);
                        break;
                }
                fail();
            } catch (IllegalArgumentException e) {
                assertThat(e, exception("segment index " + segment + " out of bound [0, 3)"));
            }
        }

        assertEquals("http:///", broker.toString());

        // add
        broker.addPathSegment(2, "c11/c22");
        broker.addPathSegment(0, "a11/a22");
        assertEquals("http:///a11/a22/c11/c22", broker.toString());

        broker.addPathSegment(1, "b11/b22");
        assertEquals("http:///a11/a22/b11/b22/c11/c22", broker.toString());

        // set / clear and add
        broker.setPathSegment(1, createArrayList("B11", "B22"));
        assertEquals("http:///a11/a22/B11/B22/c11/c22", broker.toString());

        broker.clearPathSegment(0);
        broker.addPathSegment(0, "A11/A22");
        assertEquals("http:///A11/A22/B11/B22/c11/c22", broker.toString());

        broker.clearPathSegment(2);
        broker.addPathSegment(2, "C11/C22");
        assertEquals("http:///A11/A22/B11/B22/C11/C22", broker.toString());

        // get
        assertEquals("/A11/A22", broker.getPathSegmentAsString(0));
        assertEquals("/B11/B22", broker.getPathSegmentAsString(1));
        assertEquals("/C11/C22", broker.getPathSegmentAsString(2));

        assertArrayEquals(new String[] { "A11", "A22" }, broker.getPathSegment(0).toArray(new String[2]));
        assertArrayEquals(new String[] { "B11", "B22" }, broker.getPathSegment(1).toArray(new String[2]));
        assertArrayEquals(new String[] { "C11", "C22" }, broker.getPathSegment(2).toArray(new String[2]));
    }

    @Test
    public void setPath_addPath_clearPath() {
        broker.clearPath();

        // addPath / addPath
        broker.addPath(" /aaa//\\ bbb \\ ccc// ");
        assertEquals("/aaa/bbb/ccc", broker.getPath());
        assertArrayEquals(new String[] { "aaa", "bbb", "ccc" }, broker.getPathElements().toArray(new String[0]));

        broker.addPath("ddd");
        assertEquals("/aaa/bbb/ccc/ddd", broker.getPath());
        assertArrayEquals(new String[] { "aaa", "bbb", "ccc", "ddd" }, broker.getPathElements().toArray(new String[0]));

        // clearPath
        broker.clearPath();

        // addPath / setPath
        broker.addPath(" /aaa//\\ bbb \\ ccc// ");
        assertEquals("/aaa/bbb/ccc", broker.getPath());
        assertArrayEquals(new String[] { "aaa", "bbb", "ccc" }, broker.getPathElements().toArray(new String[0]));

        broker.setPathElements(createArrayList("ddd", " /eee/fff "));
        assertEquals("/ddd/eee/fff", broker.getPath());
        assertArrayEquals(new String[] { "ddd", "eee", "fff" }, broker.getPathElements().toArray(new String[0]));
    }

    @Test
    public void setQuery_clearQuery() {
        // hasQuery        
        broker.clearQuery();

        // setQuery
        Map<String, Object> query = createLinkedHashMap();
        query.put(" a ", null); // null value
        query.put(" b ", new Object[] { 123, "456", null }); // object array
        query.put(" c ", 123); // scalar value

        broker.setQuery(query);

        assertEquals(3, broker.getQuery().size());
        assertEquals("", broker.getQuery().get("a"));
        assertArrayEquals(new String[] { "123", "456", "" }, (String[]) broker.getQuery().get("b"));
        assertEquals("123", broker.getQuery().get("c"));

        // clearQuery
        broker.clearQuery();
    }

    @Test
    public void setQueryData() {
        // empty id
        try {
            broker.setQueryData(null, "test");
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("empty query id"));
        }

        try {
            broker.setQueryData("  ", "test");
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("empty query id"));
        }

        // null value -> empty_string
        broker.setQueryData(" id ", null);
        assertEquals("", broker.getQuery().get("id"));

        // String[] values，替换其中的null -> empty_string
        broker.setQueryData("id", new String[] { null, "aa", " bb " });
        assertArrayEquals(new String[] { "", "aa", " bb " }, (String[]) broker.getQuery().get("id"));

        // int[] values
        broker.setQueryData("id", new int[] { 1, 2, 3 });
        assertArrayEquals(new String[] { "1", "2", "3" }, (String[]) broker.getQuery().get("id"));

        // Object[] values, 替换其中的null -> empty_string
        broker.setQueryData("id", new Object[] { null, "aa", 3 });
        assertArrayEquals(new String[] { "", "aa", "3" }, (String[]) broker.getQuery().get("id"));

        // scalar value
        broker.setQueryData("id", 3);
        assertEquals("3", broker.getQuery().get("id"));
    }

    @Test
    public void getQueryData() {
        // 无值
        assertNull(broker.getQueryData("myid"));

        // null值
        broker.setQueryData("myid", null);
        assertEquals("", broker.getQueryData("myid"));

        // 单值
        broker.setQueryData("myid", "test");
        assertEquals("test", broker.getQueryData("myid"));

        // 多值
        broker.addQueryData("myid", "test2");
        assertArrayEquals(new String[] { "test", "test2" }, (String[]) broker.getQuery().get("myid"));
        assertEquals("test", broker.getQueryData("myid"));
    }

    @Test
    public void addQueryData_Object() {
        // empty id
        try {
            broker.addQueryData(null, 1);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("empty query id"));
        }

        try {
            broker.setQueryData("  ", 1);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("empty query id"));
        }

        // null value
        broker.addQueryData(" id ", (Object) null);
        assertEquals("", broker.getQuery().get("id"));

        // array value
        try {
            broker.addQueryData("id", new String[1]);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("use setQueryData(array) instead"));
        }

        // Object value
        broker.addQueryData(" id ", (Object) 123);
        assertArrayEquals(new String[] { "", "123" }, (String[]) broker.getQuery().get("id"));
    }

    @Test
    public void addQueryData_String() {
        // empty id
        try {
            broker.addQueryData(null, "1");
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("empty query id"));
        }

        try {
            broker.setQueryData("  ", "1");
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("empty query id"));
        }

        // null value
        broker.addQueryData(" id ", (String) null);
        assertEquals("", broker.getQuery().get("id"));

        // remove query data
        broker.removeQueryData(" id  ");
        assertEquals(null, broker.getQuery().get("id"));

        // first value
        broker.addQueryData("id", "aa");
        assertEquals("aa", broker.getQuery().get("id"));

        // second value
        broker.addQueryData("id", "bb");
        assertArrayEquals(new String[] { "aa", "bb" }, (String[]) broker.getQuery().get("id"));

        // third value
        broker.addQueryData("id", "cc");
        assertArrayEquals(new String[] { "aa", "bb", "cc" }, (String[]) broker.getQuery().get("id"));

        // fourth value == null
        broker.addQueryData("id", null);
        assertArrayEquals(new String[] { "aa", "bb", "cc", "" }, (String[]) broker.getQuery().get("id"));
    }

    @Test
    public void removeQueryData() {
        broker.removeQueryData("id");

        broker.setQueryData("id", "aa");
        assertEquals("aa", broker.getQuery().get("id"));

        broker.removeQueryData(" id ");
        assertEquals(null, broker.getQuery().get("id"));
    }

    @Test
    public void processPathInterceptors() {
        processPathInterceptors(true);
        processPathInterceptors(false);
    }

    private void processPathInterceptors(final boolean withLeadingSlash) {
        GenericURIBroker broker = new GenericURIBroker();

        i3 = new URIBrokerPathInterceptor() {
            public void perform(URIBroker broker) {
            }

            public String perform(URIBroker broker, String path) {
                if (withLeadingSlash) {
                    return "/hello" + path;
                } else {
                    return "hello" + path;
                }
            }
        };

        setupBroker(broker);

        assertEquals("http://user2:pass2@taobao.com:8888/hello/cc/dd?a=3&a=4&c=4#ref2", broker.render());
        assertEquals("http://user2:pass2@taobao.com:8888/hello/cc/dd?a=3&a=4&c=4#ref2", broker.render());
    }

    @Test
    public void charsetEncoding() {
        GenericURIBroker broker = new GenericURIBroker();
        broker.setServerURI("http://taobao.com");
        broker.addPath("中国");
        broker.addQueryData("中国", "万岁");

        // 使用locale context
        assertNull(broker.getCharset());

        LocaleUtil.setContext(Locale.CHINA, "UTF-8");
        assertEquals("http://taobao.com/%E4%B8%AD%E5%9B%BD?%E4%B8%AD%E5%9B%BD=%E4%B8%87%E5%B2%81", broker.render());

        LocaleUtil.setContext(Locale.CHINA, "GBK");
        assertEquals("http://taobao.com/%D6%D0%B9%FA?%D6%D0%B9%FA=%CD%F2%CB%EA", broker.render());

        // 指定charset
        broker.setCharset("UTF-8");
        assertEquals("http://taobao.com/%E4%B8%AD%E5%9B%BD?%E4%B8%AD%E5%9B%BD=%E4%B8%87%E5%B2%81", broker.render());
    }

    @Test
    public void render() {
        GenericURIBroker broker = new GenericURIBroker();

        setupBroker(broker);
        assertEquals("http://user2:pass2@taobao.com:8888/cc/dd?a=3&a=4&c=4#ref2", broker.render());

        // multi-values
        broker.addQueryData("a", 1);
        broker.setQueryData("b", new String[] { "2", "3" });

        assertEquals("http://user2:pass2@taobao.com:8888/cc/dd?a=3&a=4&a=1&c=4&b=2&b=3#ref2", broker.render());

        // default port
        broker.setServerPort(80);
        assertEquals("http://user2:pass2@taobao.com/cc/dd?a=3&a=4&a=1&c=4&b=2&b=3#ref2", broker.render());

        broker.setServerScheme("https");
        assertEquals("https://user2:pass2@taobao.com:80/cc/dd?a=3&a=4&a=1&c=4&b=2&b=3#ref2", broker.render());

        broker.setServerPort(443);
        assertEquals("https://user2:pass2@taobao.com/cc/dd?a=3&a=4&a=1&c=4&b=2&b=3#ref2", broker.render());

        // no userInfo
        broker.setLoginPassword(null);
        assertEquals("https://user2@taobao.com/cc/dd?a=3&a=4&a=1&c=4&b=2&b=3#ref2", broker.render());

        broker.setLoginUser(null);
        assertEquals("https://taobao.com/cc/dd?a=3&a=4&a=1&c=4&b=2&b=3#ref2", broker.render());

        // no ref
        broker.setReference(null);
        assertEquals("https://taobao.com/cc/dd?a=3&a=4&a=1&c=4&b=2&b=3", broker.render());

        // no query
        broker.clearQuery();
        assertEquals("https://taobao.com/cc/dd", broker.render());

        // no path
        broker.clearPath();
        assertEquals("https://taobao.com/", broker.render());

        // no serverInfo
        broker.setServerName(null);
        assertEquals("https:///", broker.render());

        // no serverScheme
        broker.setServerScheme(null);
        assertEquals("http:///", broker.render());
    }

    @Test
    public void prerender() {
        // 情况一，经过init的broker是被预渲染的。
        GenericURIBroker parent = newInstance();
        setupParentBroker(parent);
        parent.init();
        assertRenderer(parent, true, true, true, "https://user:pass@localhost/aa/bb?a=1&b=2#ref");

        assertPrerender((GenericURIBroker) parent.fork());

        // 情况二，非autoReset的broker，在fork(true)时，会自动创建一个被预渲染的parent。
        parent = (GenericURIBroker) newInstance().fork(); // autoReset=true
        setupParentBroker(parent);
        assertRenderer(parent, false, false, false, "https://user:pass@localhost/aa/bb?a=1&b=2#ref");
        assertRenderer(parent, false, false, false, "http:///");
        setupParentBroker(parent);
        assertPrerender((GenericURIBroker) parent.fork());
    }

    private void assertPrerender(GenericURIBroker broker) {
        // serverScheme
        broker.setServerScheme("http");
        assertRenderer(broker, false, true, true, "http://user:pass@localhost:443/aa/bb?a=1&b=2#ref");

        // serverName
        broker.setServerName("taobao.com");
        assertRenderer(broker, false, true, true, "https://user:pass@taobao.com/aa/bb?a=1&b=2#ref");

        // serverPort
        broker.setServerPort(123);
        assertRenderer(broker, false, true, true, "https://user:pass@localhost:123/aa/bb?a=1&b=2#ref");

        // loginUser
        broker.setLoginUser("user1");
        assertRenderer(broker, false, true, true, "https://user1:pass@localhost/aa/bb?a=1&b=2#ref");

        // loginPassword
        broker.setLoginPassword("pass1");
        assertRenderer(broker, false, true, true, "https://user:pass1@localhost/aa/bb?a=1&b=2#ref");

        // setPath
        broker.setPathElements(createArrayList("cc/dd"));
        assertRenderer(broker, true, false, true, "https://user:pass@localhost/cc/dd?a=1&b=2#ref");

        // addPath
        broker.addPath(null);
        assertRenderer(broker, true, true, true, "https://user:pass@localhost/aa/bb?a=1&b=2#ref");

        broker.addPath("cc");
        assertRenderer(broker, true, true, true, "https://user:pass@localhost/aa/bb/cc?a=1&b=2#ref");

        // clearPath
        broker.clearPath();
        assertRenderer(broker, true, false, true, "https://user:pass@localhost/?a=1&b=2#ref");

        // setQuery
        Map<String, Object> map = createLinkedHashMap();
        map.put("c", new Object[] { 3, 4 });

        broker.setQuery(map);
        assertRenderer(broker, true, true, false, "https://user:pass@localhost/aa/bb?c=3&c=4#ref");

        // clearQuery
        broker.clearQuery();
        assertRenderer(broker, true, true, false, "https://user:pass@localhost/aa/bb#ref");

        // setQueryData
        broker.setQueryData("a", 3);
        assertRenderer(broker, true, true, false, "https://user:pass@localhost/aa/bb?a=3&b=2#ref");

        // addQueryData
        broker.addQueryData("a", 3);
        assertRenderer(broker, true, true, true, "https://user:pass@localhost/aa/bb?a=1&b=2&a=3#ref");

        broker.addQueryData("a", "4");
        assertRenderer(broker, true, true, true, "https://user:pass@localhost/aa/bb?a=1&b=2&a=4#ref");

        // removeQueryData
        broker.removeQueryData("a");
        assertRenderer(broker, true, true, false, "https://user:pass@localhost/aa/bb?b=2#ref");
    }

    private void assertRenderer(GenericURIBroker broker, boolean server, boolean path, boolean query,
                                String renderResult) {
        assertEquals(server, broker.renderer.isServerRendered());
        assertEquals(path, broker.renderer.isPathRendered());
        assertEquals(query, broker.renderer.isQueryRendered());
        assertEquals(renderResult, broker.render());
    }

    @Test
    public void baseURI_request() {
        broker.setURIType(URIType.absolute);

        // 以request作为baseURI, requestAware=false/true
        broker.setServerURI("http://www.taobao.com/hello/world/aaa/bbb/ccc.jsp");
        broker.setRequest(getMockRequest_withRequestURI("http", "www.taobao.com", 80));

        broker.setRequestAware(false);
        assertEquals("http://www.taobao.com/hello/world/aaa/bbb/ccc.jsp", broker.toString());

        broker.setRequestAware(true);
        assertEquals("/hello/world/aaa/bbb/ccc.jsp", broker.toString());

        // default scheme/port
        broker.setRequest(getMockRequest_withRequestURI("http", "www.taobao.com", 80));
        broker.setServerScheme(null);
        broker.setServerPort(-1);
        assertEquals("/hello/world/aaa/bbb/ccc.jsp", broker.toString());

        broker.setRequest(getMockRequest_withRequestURI(null, "www.taobao.com", -1));
        broker.setServerScheme(null);
        broker.setServerPort(-1);
        assertEquals("/hello/world/aaa/bbb/ccc.jsp", broker.toString());

        // matching scheme/server/port
        broker.setRequest(getMockRequest_withRequestURI(null, "www.taobao.com", 8080));
        broker.setServerURI("http://www.taobao.com/hello/world/aaa/bbb/ccc.jsp"); // port diffs
        assertEquals("http://www.taobao.com/hello/world/aaa/bbb/ccc.jsp", broker.toString());

        broker.setRequest(getMockRequest_withRequestURI(null, "www.taobao.com", 8080));
        broker.setServerURI("http://www.taobao.com:8080/hello/world/aaa/bbb/ccc.jsp"); // matched
        assertEquals("/hello/world/aaa/bbb/ccc.jsp", broker.toString());

        broker.setRequest(getMockRequest_withRequestURI(null, "www.taobao.com", 8080));
        broker.setServerURI("http://www.alibaba.com:8080/hello/world/aaa/bbb/ccc.jsp"); // server diffs
        assertEquals("http://www.alibaba.com:8080/hello/world/aaa/bbb/ccc.jsp", broker.toString());

        broker.setRequest(getMockRequest_withRequestURI("http", "www.alibaba.com", 8080));
        broker.setServerURI("http://www.alibaba.com:8080/hello/world/aaa/bbb/ccc.jsp"); // matched
        assertEquals("/hello/world/aaa/bbb/ccc.jsp", broker.toString());

        broker.setRequest(getMockRequest_withRequestURI("https", "www.alibaba.com", -1));
        broker.setServerURI("http://www.alibaba.com:443/hello/world/aaa/bbb/ccc.jsp"); // scheme diffs
        assertEquals("http://www.alibaba.com:443/hello/world/aaa/bbb/ccc.jsp", broker.toString());

        broker.setRequest(getMockRequest_withRequestURI("https", "www.alibaba.com", -1));
        broker.setServerURI("https://www.alibaba.com:443/hello/world/aaa/bbb/ccc.jsp"); // matched
        assertEquals("/hello/world/aaa/bbb/ccc.jsp", broker.toString());
    }

    @Test
    public void baseURI_path() {
        broker.setURIType(URIType.absolute);

        // 设置request，但如有baseURI，则以baseURI为准
        broker.setServerURI("http://www.taobao.com/hello/world/aaa/bbb/ccc.jsp");
        broker.setRequest(getMockRequest_withRequestURI("http", "www.taobao.com", 80));
        broker.setRequestAware(true);
        assertEquals("/hello/world/aaa/bbb/ccc.jsp", broker.toString());

        // default scheme/port
        broker.setBaseURI("http://www.taobao.com:80/hello/world/index.html?a=1");
        broker.setServerScheme(null);
        broker.setServerPort(-1);
        assertEquals("/hello/world/aaa/bbb/ccc.jsp", broker.toString());

        broker.setBaseURI("http://www.taobao.com/hello/world/index.html?a=1");
        broker.setServerScheme(null);
        broker.setServerPort(-1);
        assertEquals("/hello/world/aaa/bbb/ccc.jsp", broker.toString());

        // matching scheme/server/port
        broker.setBaseURI("http://www.taobao.com:8080/hello/world/index.html?a=1");
        broker.setServerURI("http://www.taobao.com/hello/world/aaa/bbb/ccc.jsp"); // port diffs
        assertEquals("http://www.taobao.com/hello/world/aaa/bbb/ccc.jsp", broker.toString());

        broker.setBaseURI("http://www.taobao.com:8080/hello/world/index.html?a=1");
        broker.setServerURI("http://www.taobao.com:8080/hello/world/aaa/bbb/ccc.jsp"); // matched
        assertEquals("/hello/world/aaa/bbb/ccc.jsp", broker.toString());

        broker.setBaseURI("http://www.taobao.com:8080/hello/world/index.html?a=1");
        broker.setServerURI("http://www.alibaba.com:8080/hello/world/aaa/bbb/ccc.jsp"); // server diffs
        assertEquals("http://www.alibaba.com:8080/hello/world/aaa/bbb/ccc.jsp", broker.toString());

        broker.setBaseURI("http://www.alibaba.com:8080/hello/world/index.html?a=1");
        broker.setServerURI("http://www.alibaba.com:8080/hello/world/aaa/bbb/ccc.jsp"); // matched
        assertEquals("/hello/world/aaa/bbb/ccc.jsp", broker.toString());

        broker.setBaseURI("https://www.alibaba.com/hello/world/index.html?a=1");
        broker.setServerURI("http://www.alibaba.com:443/hello/world/aaa/bbb/ccc.jsp"); // scheme diffs
        assertEquals("http://www.alibaba.com:443/hello/world/aaa/bbb/ccc.jsp", broker.toString());

        broker.setBaseURI("https://www.alibaba.com/hello/world/index.html?a=1");
        broker.setServerURI("https://www.alibaba.com:443/hello/world/aaa/bbb/ccc.jsp"); // matched
        assertEquals("/hello/world/aaa/bbb/ccc.jsp", broker.toString());
    }

    private HttpServletRequest getMockRequest_withRequestURI(String scheme, String serverName, int port) {
        HttpServletRequest request = getMockRequest_noReplay(scheme, serverName, port, "/hello", "/world/index.html",
                null);

        expect(request.getRequestURI()).andReturn("/hello/world/index.html?a=1&b=2").anyTimes();
        replay(request);

        return request;
    }

    @Test
    public void fullURI() {
        broker.setBaseURI("http://www.taobao.com/hello/world/index.html");
        broker.setServerURI("http://www.taobao.com/hello/world/aaa/bbb/ccc.jsp");
        broker.addQueryData("test", "value");
        assertEquals(null, broker.getURIType());
        assertEquals("http://www.taobao.com/hello/world/aaa/bbb/ccc.jsp?test=value", broker.toString());

        broker.setURIType(URIType.full);
        assertEquals("http://www.taobao.com/hello/world/aaa/bbb/ccc.jsp?test=value", broker.toString());
    }

    @Test
    public void absolutePath() {
        broker.setBaseURI("http://www.taobao.com/hello/world/index.html");
        broker.setServerURI("http://www.taobao.com/hello/world/aaa/bbb/ccc.jsp");
        broker.addQueryData("test", "value");
        broker.setURIType(URIType.absolute);

        assertEquals("/hello/world/aaa/bbb/ccc.jsp?test=value", broker.toString());

        broker.setServerURI("http://www.taobao.com/hello/aaa/bbb/ccc.jsp");
        assertEquals("/hello/aaa/bbb/ccc.jsp?test=value", broker.toString());

        broker.setServerURI("http://www.taobao.com/aaa/bbb/ccc.jsp");
        assertEquals("/aaa/bbb/ccc.jsp?test=value", broker.toString());

        // server not match
        broker.setBaseURI("http://www.taobao.com:8080/hello/world/index.html");

        broker.setServerURI("http://www.taobao.com/aaa/bbb/ccc.jsp");
        assertEquals("http://www.taobao.com/aaa/bbb/ccc.jsp?test=value", broker.toString());
    }

    @Test
    public void relativeURI() {
        // baseURI=file
        broker.setBaseURI("http://www.taobao.com/hello/world/index.html");
        broker.setServerURI("http://www.taobao.com/hello/world/aaa/bbb/ccc.jsp");
        broker.addQueryData("test", "value");
        broker.setURIType(URIType.relative);

        assertEquals("aaa/bbb/ccc.jsp?test=value", broker.toString());

        broker.setServerURI("http://www.taobao.com/hello/aaa/bbb/ccc.jsp");
        assertEquals("../aaa/bbb/ccc.jsp?test=value", broker.toString());

        broker.setServerURI("http://www.taobao.com/aaa/bbb/ccc.jsp");
        assertEquals("../../aaa/bbb/ccc.jsp?test=value", broker.toString());

        // baseURI=dir
        broker.setBaseURI("http://www.taobao.com/hello/world/");
        broker.setServerURI("http://www.taobao.com/aaa/bbb/ccc.jsp");
        assertEquals("../../aaa/bbb/ccc.jsp?test=value", broker.toString());

        // baseURI=root dir
        broker.setBaseURI("http://www.taobao.com//");
        broker.setServerURI("http://www.taobao.com/aaa/bbb/ccc.jsp");
        assertEquals("aaa/bbb/ccc.jsp?test=value", broker.toString());

        broker.setBaseURI("http://www.taobao.com");
        broker.setServerURI("http://www.taobao.com/aaa/bbb/ccc.jsp");
        assertEquals("aaa/bbb/ccc.jsp?test=value", broker.toString());

        // server not match
        broker.setBaseURI("http://www.taobao.com:8080/hello/world/index.html");

        broker.setServerURI("http://www.taobao.com/aaa/bbb/ccc.jsp");
        assertEquals("http://www.taobao.com/aaa/bbb/ccc.jsp?test=value", broker.toString());
    }

    @Test
    public void autoURI() {
        // baseURI=file
        broker.setBaseURI("http://www.taobao.com/hello/world/index.html");
        broker.setServerURI("http://www.taobao.com/hello/world/aaa/bbb/ccc.jsp");
        broker.addQueryData("test", "value");
        broker.setURIType(URIType.auto);

        assertEquals("aaa/bbb/ccc.jsp?test=value", broker.toString());

        broker.setServerURI("http://www.taobao.com/hello/aaa/bbb/ccc.jsp");
        assertEquals("/hello/aaa/bbb/ccc.jsp?test=value", broker.toString());

        broker.setServerURI("http://www.taobao.com/aaa/bbb/ccc.jsp");
        assertEquals("/aaa/bbb/ccc.jsp?test=value", broker.toString());

        // baseURI=dir
        broker.setBaseURI("http://www.taobao.com/hello/world/");
        broker.setServerURI("http://www.taobao.com/aaa/bbb/ccc.jsp");
        assertEquals("/aaa/bbb/ccc.jsp?test=value", broker.toString());

        // baseURI=root dir
        broker.setBaseURI("http://www.taobao.com//");
        broker.setServerURI("http://www.taobao.com/aaa/bbb/ccc.jsp");
        assertEquals("aaa/bbb/ccc.jsp?test=value", broker.toString());

        broker.setBaseURI("http://www.taobao.com");
        broker.setServerURI("http://www.taobao.com/aaa/bbb/ccc.jsp");
        assertEquals("aaa/bbb/ccc.jsp?test=value", broker.toString());

        broker.setBaseURI("http://www.taobao.com");
        broker.setServerURI("http://www.taobao.com/aaa/bbb/ccc.jsp");
        broker.addInterceptor(new URIBrokerPathInterceptor() {
            public void perform(URIBroker broker) {
            }

            public String perform(URIBroker broker, String path) {
                return "aa/bb/cc";
            }
        });
        assertEquals("aa/bb/cc?test=value", broker.toString());
        broker.clearInterceptors();

        // server not match
        broker.setBaseURI("http://www.taobao.com:8080/hello/world/index.html");

        broker.setServerURI("http://www.taobao.com/aaa/bbb/ccc.jsp");
        assertEquals("http://www.taobao.com/aaa/bbb/ccc.jsp?test=value", broker.toString());
    }
}
