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

import static com.alibaba.citrus.generictype.TypeInfoUtil.*;
import static com.alibaba.citrus.test.TestUtil.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static org.easymock.EasyMock.*;
import static org.easymock.classextension.EasyMock.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.ObjectFactory;

import com.alibaba.citrus.service.uribroker.interceptor.URIBrokerInterceptor;
import com.alibaba.citrus.util.i18n.LocaleUtil;

public abstract class AbstractURIBrokerFeaturesTests<B extends URIBroker> {
    protected final Class<?> brokerClass = resolveParameter(getClass(), AbstractURIBrokerFeaturesTests.class, 0)
            .getRawType();
    protected HttpServletRequest request;
    protected B broker;
    protected URIBrokerInterceptor i1;
    protected URIBrokerInterceptor i2;
    protected URIBrokerInterceptor i3;

    @Before
    public void init() {
        this.request = getMockRequest();
        this.broker = newInstance();
        this.i1 = createMock(URIBrokerInterceptor.class);
        this.i2 = createMock(URIBrokerInterceptor.class);
        this.i3 = createMock(URIBrokerInterceptor.class);

        // init locale
        LocaleUtil.setContext(Locale.CHINA, "UTF-8");
    }

    @After
    public void resetLocale() {
        LocaleUtil.resetContext();
    }

    protected void setupParentBroker(B parent) {
        parent.setCharset("GBK");
        parent.addInterceptor(i1);
        parent.addInterceptor(i2);
    }

    protected void assertParentBroker(B broker) {
        assertEquals("GBK", broker.getCharset());
        assertEquals(2, broker.getInterceptors().size());
        assertArrayEquals(new Object[] { i1, i2 }, broker.getInterceptors().toArray(new Object[2]));
    }

    protected void setupBroker(B broker) {
        broker.setCharset("UTF-8");
        broker.addInterceptor(i3);
    }

    protected void assertBroker(B broker) {
        assertEquals("UTF-8", broker.getCharset());
        assertEquals(3, broker.getInterceptors().size());
        assertArrayEquals(new Object[] { i1, i2, i3 }, broker.getInterceptors().toArray(new Object[3]));
    }

    @Test
    public void getRequest() {
        assertNull(getFieldValue(broker, "request", HttpServletRequest.class));

        broker.setRequest(request);
        assertSame(request, getFieldValue(broker, "request", HttpServletRequest.class));
    }

    @Test
    public void getRequestAware() {
        assertEquals(true, broker.isRequestAware()); // request aware by default

        broker.setRequestAware(false);
        assertEquals(false, broker.isRequestAware());
    }

    @Test
    public void getParent() throws Exception {
        URIBroker b0 = new GenericURIBroker();
        URIBroker1 b1 = new URIBroker1();
        URIBroker2 b2 = new URIBroker2();

        // default is no parent
        assertNull(b0.getParent());

        // not a superclass - 也许可，但只有共同的父类部分数据被继承
        b2.setParent(b1);
        assertSame(b1, b2.getParent());

        // same class
        b2.setParent(new URIBroker2());
        assertThat(b2.getParent(), instanceOf(URIBroker2.class));

        // superclass
        b2.setParent(b0);
        assertSame(b0, b2.getParent());

        // re-set parent
        b2.setParent(b0);
        assertSame(b0, b2.getParent());

        // after init
        assertNull(b1.getParent());
        b1.init();

        try {
            b1.setParent(b0);
            fail();
        } catch (IllegalStateException e) {
            assertThat(e, exception("already initialized"));
        }

        assertNotNull(b2.getParent());
        b2.init();

        try {
            b2.setParent(b0);
            fail();
        } catch (IllegalStateException e) {
            assertThat(e, exception("already initialized"));
        }
    }

    @Test
    public void getCharset() {
        // default is null
        assertEquals(null, broker.getCharset());

        // set empty
        broker.setCharset(null);
        assertEquals(null, broker.getCharset());

        broker.setCharset("  ");
        assertEquals(null, broker.getCharset());

        // set value
        broker.setCharset(" GBK ");
        assertEquals("GBK", broker.getCharset());
    }

    @Test
    public void isAutoReset() {
        // default is false
        assertEquals(false, broker.isAutoReset());

        // fork(true)
        URIBroker b = broker.fork(true);
        assertEquals(true, b.isAutoReset());

        // fork(false)
        b = broker.fork(false);
        assertEquals(false, b.isAutoReset());
    }

    @Test
    public void getInterceptors() {
        // no interceptors by default
        assertFalse(broker.hasInterceptors());
        assertNull(getFieldValue(broker, "interceptors", null));
        assertTrue(broker.getInterceptors().isEmpty()); // auto create
        assertFalse(broker.hasInterceptors());

        // add null
        try {
            broker.addInterceptor(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("interceptor"));
        }

        // add
        URIBrokerInterceptor i1 = createMock(URIBrokerInterceptor.class);
        URIBrokerInterceptor i2 = createMock(URIBrokerInterceptor.class);

        broker.addInterceptor(i1);
        broker.addInterceptor(i2);

        assertTrue(broker.hasInterceptors());
        assertEquals(2, broker.getInterceptors().size());
        assertArrayEquals(new Object[] { i1, i2 }, broker.getInterceptors().toArray(new Object[2]));

        // set
        broker.setInterceptors(createArrayList(i2, i1));
        assertTrue(broker.hasInterceptors());
        assertEquals(2, broker.getInterceptors().size());
        assertArrayEquals(new Object[] { i2, i1 }, broker.getInterceptors().toArray(new Object[2]));

        // clear
        broker.clearInterceptors();
        assertFalse(broker.hasInterceptors());
        assertTrue(broker.getInterceptors().isEmpty());
    }

    /**
     * init broker，broker的内容为空。
     */
    @Test
    public final void init_broker_nonOverride() {
        B parent = newInstance();

        setupParentBroker(parent);

        broker.setParent(parent);
        broker.init();
        assertEquals(true, getFieldValue(broker, "initialized", null));

        assertParentBroker(broker);
    }

    /**
     * init broker，broker有内容。
     */
    @Test
    public final void init_broker_override() {
        B parent = newInstance();

        setupParentBroker(parent);
        setupBroker(broker);

        broker.setParent(parent);
        broker.init();
        assertEquals(true, getFieldValue(broker, "initialized", null));

        assertBroker(broker);
    }

    @Test
    public final void reset_noParent() {
        setupBroker(broker);
        broker.init();

        broker.reset();
        assertAfterReset_noParent(broker);
    }

    @Test
    public final void reset_withParent() {
        B parent = newInstance();
        setupParentBroker(parent);
        setupBroker(broker);
        broker.setParent(parent);
        broker.init();
        assertBroker(broker);

        broker.reset();
        assertParentBroker(broker); // 等同于parent
    }

    protected void assertAfterReset_noParent(B broker) {
        assertNull(broker.getCharset());
        assertFalse(broker.hasInterceptors());
    }

    @Test
    public void newInstanceInternal() {
        URIBroker3 broker = new URIBroker3();
        String className = URIBroker3.class.getName();

        // null
        try {
            broker.newInstanceInternal();
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception(className + ".newInstance() returns null"));
        }

        // same object
        broker.instance = broker;

        try {
            broker.newInstanceInternal();
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception(className + ".newInstance() returns itself"));
        }

        // wrong type
        broker.instance = new GenericURIBroker();

        try {
            broker.newInstanceInternal();
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e,
                    exception(className + ".newInstance() returns wrong type: " + GenericURIBroker.class.getName()));
        }

        // right type
        broker.instance = new URIBroker3();
        assertSame(broker.instance, broker.newInstanceInternal());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void fork() {
        // parent, autoReset == false
        B parent = newInstance();
        parent.setRequestAware(false);
        parent.setRequest(request);
        setupParentBroker(parent);
        assertFalse(parent.isAutoReset());

        assertParentBroker(parent);

        // broker, autoReset == true
        broker = (B) parent.fork();
        assertFalse(broker.isRequestAware()); // request和requestAware被复制
        assertSame(request, getFieldValue(broker, "request", HttpServletRequest.class));
        assertTrue(broker.isAutoReset());
        assertSame(parent, broker.getParent()); // 因为parent.autoReset为false，所以可作为fork结果的parent

        assertParentBroker(broker); // 此时broker和parent是内容相同的

        setupBroker(broker);
        assertBroker(broker); // 此时broker内容被添加

        // newBroker = broker.fork(), autoReset == true
        // 将在内部fork一个新的autoReset为false的parent
        B newBroker = (B) broker.fork();
        assertFalse(newBroker.isRequestAware()); // request和requestAware被复制
        assertSame(request, getFieldValue(newBroker, "request", HttpServletRequest.class));
        assertTrue(newBroker.isAutoReset());
        assertNotSame(broker, newBroker.getParent()); // 因为broker.autoReset为true，所以必须fork以后才能作为新broker的parent

        assertParentBroker(broker); // fork以后，broker被reset，如同render一样
        assertBroker((B) newBroker.getParent()); // newBroker的直接parent的值，等同于reset之前的broker
        assertFalse(newBroker.getParent().isAutoReset()); // newBroker的直接parent的autoReset==false
    }

    @Test
    public void processInterceptors() {
        i1.perform(broker);
        i2.perform(broker);
        i3.perform(broker);
        replay(i1, i2, i3);

        B parent = newInstance();
        setupParentBroker(parent);

        broker.setParent(parent);
        setupBroker(broker);
        broker.init();

        assertFalse(broker.isAutoReset());
        assertEquals(broker.render(), broker.render()); // 第二次render将不会重新调用interceptors

        verify(i1, i2, i3);
        reset(i1, i2, i3);

        // broker.reset以后，所有的interceptors将被重新调用
        i1.perform(broker);
        i2.perform(broker);
        i3.perform(broker);
        replay(i1, i2, i3);

        broker.reset();
        setupBroker(broker);
        assertEquals(broker.render(), broker.render());

        verify(i1, i2, i3);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void toString_and_render() {
        B parent = newInstance();
        setupParentBroker(parent);
        assertFalse(parent.isAutoReset());

        broker = (B) parent.fork();
        setupBroker(broker);
        assertTrue(broker.isAutoReset());

        // 对于autoReset=true的broker
        assertBroker(broker);
        broker.toString(); // toString不会reset
        assertBroker(broker);
        broker.render(); // render会reset
        assertParentBroker(broker);

        // 对于autoReset=false的broker
        assertParentBroker(parent);
        parent.toString(); // toString不会reset
        assertParentBroker(parent);
        parent.render(); // render也不会reset
        assertParentBroker(parent);
    }

    protected final HttpServletRequest getMockRequest() {
        RequestProxy request = createMock(RequestProxy.class);

        expect(request.getObject()).andThrow(new IllegalStateException()).anyTimes();
        expect(request.getScheme()).andThrow(new IllegalStateException()).anyTimes();
        expect(request.getServerName()).andThrow(new IllegalStateException()).anyTimes();
        expect(request.getServerPort()).andThrow(new IllegalStateException()).anyTimes();
        expect(request.getContextPath()).andThrow(new IllegalStateException()).anyTimes();
        expect(request.getServletPath()).andThrow(new IllegalStateException()).anyTimes();
        replay(request);

        return request;
    }

    protected final HttpServletRequest getMockRequest(String scheme, String serverName, int serverPort) {
        return getMockRequest(scheme, serverName, serverPort, null, null, null);
    }

    protected final HttpServletRequest getMockRequest(String scheme, String serverName, int serverPort,
                                                      String contextPath, String servletPath, String pathInfo) {
        HttpServletRequest request = getMockRequest_noReplay(scheme, serverName, serverPort, contextPath, servletPath,
                pathInfo);
        replay(request);
        return request;
    }

    protected final HttpServletRequest getMockRequest_noReplay(String scheme, String serverName, int serverPort,
                                                               String contextPath, String servletPath, String pathInfo) {
        HttpServletRequest request = createMock(HttpServletRequest.class);

        expect(request.getScheme()).andReturn(scheme).anyTimes();
        expect(request.getServerName()).andReturn(serverName).anyTimes();
        expect(request.getServerPort()).andReturn(serverPort).anyTimes();
        expect(request.getContextPath()).andReturn(contextPath).anyTimes();
        expect(request.getServletPath()).andReturn(servletPath).anyTimes();
        expect(request.getPathInfo()).andReturn(pathInfo).anyTimes();

        return request;
    }

    @SuppressWarnings("unchecked")
    protected final B newInstance() {
        try {
            return (B) brokerClass.newInstance();
        } catch (Exception e) {
            fail(e.toString());
            return null;
        }
    }

    public static interface RequestProxy extends HttpServletRequest, ObjectFactory {
    }

    public static class URIBroker1 extends GenericURIBroker {
    }

    public static class URIBroker2 extends GenericURIBroker {
    }

    public static class URIBroker3 extends GenericURIBroker {
        private URIBroker instance;

        @Override
        protected URIBroker newInstance() {
            return instance;
        }
    }
}
