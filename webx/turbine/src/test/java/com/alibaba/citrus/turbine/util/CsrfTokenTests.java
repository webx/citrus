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
package com.alibaba.citrus.turbine.util;

import static com.alibaba.citrus.test.TestUtil.*;
import static org.easymock.EasyMock.*;
import static org.easymock.classextension.EasyMock.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.junit.Before;
import org.junit.Test;

import com.alibaba.citrus.service.requestcontext.util.RequestContextUtil;
import com.alibaba.citrus.util.StringUtil;
import com.meterware.httpunit.WebRequest;

public class CsrfTokenTests extends AbstractPullToolTests<CsrfToken> {
    private HttpServletRequest request;
    private HttpSession session;

    @Before
    public void initMock() {
        request = createMock(HttpServletRequest.class);
        session = createMock(HttpSession.class);
        expect(request.getSession()).andReturn(session).anyTimes();
        expect(session.getId()).andReturn("aaa").anyTimes();
        expect(session.getCreationTime()).andReturn(1234L).anyTimes();
    }

    @Override
    protected String toolName() {
        return "csrfToken";
    }

    @Test
    public void checkScope() throws Exception {
        assertSame(tool, getTool()); // global scope
    }

    @Test
    public void getConfiguration() throws Exception {
        tool = new CsrfToken(newRequest);

        // default values
        assertEquals(CsrfToken.DEFAULT_TOKEN_KEY, CsrfToken.getKey());
        assertEquals(CsrfToken.DEFAULT_MAX_TOKENS, CsrfToken.getMaxTokens());

        // thread context key
        CsrfToken.setContextTokenConfiguration(" testKey ", -1);
        assertEquals("testKey", CsrfToken.getKey());
        assertEquals(CsrfToken.DEFAULT_MAX_TOKENS, CsrfToken.getMaxTokens());

        CsrfToken.setContextTokenConfiguration(" testKey ", 2);
        assertEquals("testKey", CsrfToken.getKey());
        assertEquals(2, CsrfToken.getMaxTokens());

        // reset
        CsrfToken.resetContextTokenConfiguration();
        assertEquals(CsrfToken.DEFAULT_TOKEN_KEY, CsrfToken.getKey());
        assertEquals(CsrfToken.DEFAULT_MAX_TOKENS, CsrfToken.getMaxTokens());
    }

    @Test
    public void getLongLiveTokenInSession() {
        replay(session);

        try {
            CsrfToken.getLongLiveTokenInSession(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("session"));
        }

        String token = CsrfToken.getLongLiveTokenInSession(session);

        assertNotNull(token);
        assertTrue(StringUtil.containsOnly(token,
                "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray()));
    }

    @Test
    @Deprecated
    public void getLongLiveToken() throws Exception {
        // -----------------------
        // 请求1，取得token
        String token = tool.getLongLiveToken();

        assertNotNull(token);
        assertThat(tool.getHiddenField(true).toString(),
                containsString("<input name='_csrf_token' type='hidden' value='"));
        assertThat(tool.getLongLiveHiddenField().toString(), containsString(token));
        assertEquals("_csrf_token", CsrfToken.getKey());

        // 同一个请求，再次取得token
        assertEquals(token, tool.getLongLiveToken());
        assertEquals(null, newRequest.getSession().getAttribute("_csrf_token"));

        commitRequestContext();

        // -----------------------
        // 请求2，再次取得token
        getInvocationContext("http://localhost/app1/1.html");
        initRequestContext();

        assertEquals(token, tool.getLongLiveToken());

        assertThat(tool.getHiddenField(true).toString(),
                containsString("<input name='_csrf_token' type='hidden' value='"));
        assertThat(tool.getLongLiveHiddenField().toString(), containsString(token));
        assertEquals("_csrf_token", CsrfToken.getKey());

        // 和unique token混用
        String token2 = tool.getUniqueToken();

        assertNotNull(token2);
        assertThat(tool.getHiddenField(false).toString(),
                containsString("<input name='_csrf_token' type='hidden' value='"));
        assertThat(tool.getUniqueHiddenField().toString(), containsString(token2));
        assertEquals("_csrf_token", CsrfToken.getKey());
        assertEquals(token2, newRequest.getSession().getAttribute("_csrf_token"));

        commitRequestContext();

        // -----------------------
        // 请求3，取得token
        getInvocationContext("http://localhost/app1/1.html");
        initRequestContext();

        assertEquals(token, tool.getLongLiveToken());

        assertThat(tool.getHiddenField(true).toString(),
                containsString("<input name='_csrf_token' type='hidden' value='"));
        assertThat(tool.getLongLiveHiddenField().toString(), containsString(token));
        assertEquals("_csrf_token", CsrfToken.getKey());

        assertEquals(token2, newRequest.getSession().getAttribute("_csrf_token"));

        // 让session过期，但保持sessionId
        final String sessionId = newRequest.getSession().getId();
        newRequest.getSession().invalidate();
        commitRequestContext();

        // -----------------------
        // 请求4，再次取得token
        assertEquals("", client.getCookieValue("JSESSIONID"));
        assertTrue(client.getCookieDetails("JSESSIONID").isExpired());
        Thread.sleep(10); // 确保创建时间改变

        getInvocationContext("http://localhost/app1/1.html", new WebRequestCallback() {
            public void process(WebRequest wr) {
                wr.setHeaderField("Cookie", "JSESSIONID=" + sessionId);
            }
        });

        initRequestContext();

        assertTrue(newRequest.getSession().isNew()); // 新session
        assertEquals(sessionId, newRequest.getSession().getId()); // id不变

        String token3 = tool.getLongLiveToken();

        assertFalse(token.equals(token3)); // token由id和时间共同生成，因此即使id不变，token也改变
        assertNotNull(token3);
        assertThat(tool.getHiddenField(true).toString(),
                containsString("<input name='_csrf_token' type='hidden' value='"));
        assertThat(tool.getLongLiveHiddenField().toString(), containsString(token3));
        assertEquals("_csrf_token", CsrfToken.getKey());

        commitRequestContext();
    }

    @Test
    public void getUniqueToken() throws Exception {
        // -----------------------
        // 请求1，取得token
        String token = tool.getUniqueToken();

        assertNotNull(token);
        assertThat(tool.getUniqueHiddenField().toString(),
                containsString("<input name='_csrf_token' type='hidden' value='"));
        assertThat(tool.getUniqueHiddenField().toString(), containsString(token));
        assertEquals("_csrf_token", CsrfToken.getKey());

        // 同一个请求，再次取得token
        assertEquals(token, tool.getUniqueToken());
        assertEquals(token, newRequest.getSession().getAttribute("_csrf_token"));

        commitRequestContext();

        // -----------------------
        // 请求2，再次取得token
        getInvocationContext("http://localhost/app1/1.html");
        initRequestContext();

        String token2 = tool.getUniqueToken();

        assertNotNull(token2);
        assertThat(tool.getUniqueHiddenField().toString(),
                containsString("<input name='_csrf_token' type='hidden' value='"));
        assertThat(tool.getUniqueHiddenField().toString(), containsString(token2));
        assertEquals("_csrf_token", CsrfToken.getKey());

        // 同一个请求，再次取得token
        assertEquals(token2, tool.getUniqueToken());
        assertEquals(token + "/" + token2, newRequest.getSession().getAttribute("_csrf_token"));

        commitRequestContext();

        // -----------------------
        // 请求3-8，取得token
        String tokens = token + "/" + token2;

        for (int i = 2; i < 8; i++) {
            getInvocationContext("http://localhost/app1/1.html");
            initRequestContext();

            String token_i = tool.getUniqueToken();

            assertNotNull(token_i);
            assertThat(tool.getUniqueHiddenField().toString(),
                    containsString("<input name='_csrf_token' type='hidden' value='"));
            assertThat(tool.getUniqueHiddenField().toString(), containsString(token_i));
            assertEquals("_csrf_token", CsrfToken.getKey());

            // 同一个请求，再次取得token
            assertEquals(token_i, tool.getUniqueToken());
            assertEquals(tokens += "/" + token_i, newRequest.getSession().getAttribute("_csrf_token"));

            commitRequestContext();
        }

        // -----------------------
        // 请求9，取得token，抛弃第一个token
        getInvocationContext("http://localhost/app1/1.html");
        initRequestContext();

        String token_9 = tool.getUniqueToken();
        assertEquals(token_9, tool.getUniqueToken());

        tokens += "/" + token_9;
        tokens = tokens.substring(tokens.indexOf("/") + 1);
        assertEquals(tokens, newRequest.getSession().getAttribute("_csrf_token"));

        commitRequestContext();

        // -----------------------
        // 请求10，取得token，设置maxTokens=3
        getInvocationContext("http://localhost/app1/1.html");
        initRequestContext();

        CsrfToken.setContextTokenConfiguration(null, 3);

        String token_10 = tool.getUniqueToken();
        assertEquals(token_10, tool.getUniqueToken());

        tokens += "/" + token_10;
        tokens = tokens.substring(indexOf(tokens, "/", 6) + 1);
        assertEquals(tokens, newRequest.getSession().getAttribute("_csrf_token"));

        CsrfToken.resetContextTokenConfiguration();
        commitRequestContext();
    }

    private int indexOf(String str, String strToFind, int count) {
        int index = -1;

        for (int i = 0; i < count; i++) {
            index = str.indexOf(strToFind, index + 1);
        }

        return index;
    }

    @Test
    public void check_defaultKey_succ() {
        expect(request.getParameter("_csrf_token")).andReturn("any");
        replay(request);

        assertTrue(CsrfToken.check(request));
        verify(request);
    }

    @Test
    public void check_defaultKey_failed() {
        expect(request.getParameter("_csrf_token")).andReturn(null);
        replay(request);

        assertFalse(CsrfToken.check(request));
        verify(request);
    }

    @Test
    public void check_contextKey_succ() {
        CsrfToken.setContextTokenConfiguration("contextKey", -1);

        expect(request.getParameter("contextKey")).andReturn("any");
        replay(request);

        assertTrue(CsrfToken.check(request));

        CsrfToken.resetContextTokenConfiguration();
        verify(request);
    }

    @Test
    public void check_contextKey_failed() {
        CsrfToken.setContextTokenConfiguration("contextKey", -1);

        expect(request.getParameter("contextKey")).andReturn(null);
        replay(request);

        assertFalse(CsrfToken.check(request));

        CsrfToken.resetContextTokenConfiguration();
        verify(request);
    }

    @Test
    public void toString_() {
        // in request
        String token = tool.getUniqueToken();

        assertNotNull(token);
        assertThat(tool.toString(), not(equalTo("<No thread-bound request>")));

        // not in request
        requestContexts.commitRequestContext(RequestContextUtil.getRequestContext(newRequest));
        assertEquals("<No thread-bound request>", tool.toString());
    }
}
