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

import static com.alibaba.citrus.test.TestUtil.*;
import static org.junit.Assert.*;

import java.util.Arrays;

import javax.servlet.http.HttpSession;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.alibaba.citrus.service.requestcontext.AbstractRequestContextsTests;
import com.alibaba.citrus.service.requestcontext.session.SessionRequestContext;
import com.alibaba.citrus.test.runner.Prototyped;
import com.alibaba.citrus.test.runner.Prototyped.TestName;

@RunWith(Prototyped.class)
public abstract class AbstractSingleValueEncoderTests extends AbstractRequestContextsTests<SessionRequestContext>
        implements Cloneable {
    protected HttpSession session;
    protected String beanName;
    protected String attrName;
    protected String cookieName;
    protected Object value1;
    protected String value1Encoded;
    protected Object value2;
    protected String value2Encoded;
    protected Object value3;
    protected String value3Encoded;

    @BeforeClass
    public static void initFactory() {
        createBeanFactory("services-session-single-valued-cookie-stores.xml");
    }

    @TestName
    public String testName() {
        return beanName;
    }

    @Override
    protected void afterInitRequestContext() {
        session = requestContext.getRequest().getSession();
    }

    @Test
    public void session() throws Exception {
        // request 1 - new request
        invokeNoopServlet("/servlet");
        initRequestContext(beanName);

        assertEquals(true, session.isNew());

        session.setAttribute(attrName, value1);

        requestContexts.commitRequestContext(requestContext);
        commitToClient();

        // check new added cookie
        String[] newCookies = clientResponse.getHeaderFields("set-cookie");

        Arrays.sort(newCookies);

        assertEquals(2, newCookies.length);

        assertThat(newCookies[0], containsAll("JSESSIONID="));
        assertThat(newCookies[1], containsAll(cookieName + "=" + value1Encoded + ";", " Path=/; HttpOnly"));

        // request 2 - modify values
        invokeNoopServlet("/servlet");
        initRequestContext(beanName);

        assertEquals(false, session.isNew());

        assertEquals(value1, session.getAttribute(attrName));

        session.setAttribute(attrName, value2);

        requestContexts.commitRequestContext(requestContext);
        commitToClient();

        // check new added cookie
        newCookies = clientResponse.getHeaderFields("set-cookie");

        Arrays.sort(newCookies);

        assertEquals(1, newCookies.length);
        assertThat(newCookies[0], containsAll(cookieName + "=" + value2Encoded + ";", " Path=/; HttpOnly"));

        // request 3 - remove values
        invokeNoopServlet("/servlet");
        initRequestContext(beanName);

        assertEquals(false, session.isNew());

        assertEquals(value2, session.getAttribute(attrName));

        session.removeAttribute(attrName); // remove

        requestContexts.commitRequestContext(requestContext);
        commitToClient();

        // check new added cookie
        newCookies = clientResponse.getHeaderFields("set-cookie");

        Arrays.sort(newCookies);

        assertEquals(1, newCookies.length);
        assertThat(newCookies[0],
                containsAll(cookieName + "=; Expires=Thu, 01-Jan-1970 00:00:10 GMT; Path=/; HttpOnly"));

        // request 4 - re-add attrs
        invokeNoopServlet("/servlet");
        initRequestContext(beanName);

        assertEquals(false, session.isNew());

        // assertFalse(session.getAttributeNames().hasMoreElements()); // no attributes, 由于httpunit parse cookie的bug，string=; 真实环境不会这样。

        session.setAttribute(attrName, value3);

        requestContexts.commitRequestContext(requestContext);
        commitToClient();

        // check new added cookie
        newCookies = clientResponse.getHeaderFields("set-cookie");

        Arrays.sort(newCookies);

        assertEquals(1, newCookies.length);

        assertThat(newCookies[0], containsAll(cookieName + "=" + value3Encoded + ";", " Path=/; HttpOnly"));

        // request 5 - read only, no modification
        invokeNoopServlet("/servlet");
        initRequestContext(beanName);

        assertEquals(false, session.isNew());
        session.setAttribute("count", 1); // 改变session，但不改变singleValued store中的值，这将引起空map提交

        requestContexts.commitRequestContext(requestContext);
        commitToClient();

        // check new added cookie
        newCookies = clientResponse.getHeaderFields("set-cookie");

        Arrays.sort(newCookies);

        assertEquals(1, newCookies.length);

        assertThat(newCookies[0], containsAll(cookieName + "=" + value3Encoded + ";", " Path=/; HttpOnly")); // XXX! 未修改也会设cookie，需要改进

        // request 6 - invalidate
        invokeNoopServlet("/servlet");
        initRequestContext(beanName);

        assertEquals(value3, session.getAttribute(attrName));

        session.invalidate();

        requestContexts.commitRequestContext(requestContext);
        commitToClient();

        // check new added cookie
        newCookies = clientResponse.getHeaderFields("set-cookie");

        Arrays.sort(newCookies);

        assertEquals(2, newCookies.length);

        assertThat(newCookies[0], containsAll("JSESSIONID=; Expires=Thu, 01-Jan-1970 00:00:10 GMT; Path=/; HttpOnly"));
        assertThat(newCookies[1],
                containsAll(cookieName + "=; Expires=Thu, 01-Jan-1970 00:00:10 GMT; Path=/; HttpOnly"));
    }
}
