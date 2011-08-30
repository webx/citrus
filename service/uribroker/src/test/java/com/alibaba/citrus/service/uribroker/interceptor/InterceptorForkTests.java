package com.alibaba.citrus.service.uribroker.interceptor;

import static org.junit.Assert.*;

import java.util.Collections;

import org.junit.Test;

import com.alibaba.citrus.service.uribroker.uri.GenericURIBroker;
import com.alibaba.citrus.service.uribroker.uri.URIBroker;

public class InterceptorForkTests {
    @Test
    public void test() {
        URIBroker u1 = new GenericURIBroker();
        u1.setServerURI("http://localhost:8080/");
        u1.setInterceptors(Collections.<URIBrokerInterceptor> singletonList(new MyInterceptor()));

        u1 = u1.fork();

        assertEquals(true, u1.isAutoReset());
        assertEquals("http://localhost:8080/", u1.toString());
        assertEquals("http://localhost:8080/?test=webx3", u1.render());

        // u1.toString以后，interceptor不被执行。
        u1.toString(); // http://localhost:8080/
        URIBroker u2 = u1.fork();

        assertEquals("http://localhost:8080/", u2.toString());
        assertEquals("http://localhost:8080/?test=webx3", u2.render());
        assertEquals("http://localhost:8080/", u2.toString());
    }

    public static class MyInterceptor implements URIBrokerInterceptor {
        public void perform(URIBroker broker) {
            broker.addQueryData("test", "webx3");
        }
    }
}
