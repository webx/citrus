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
package com.alibaba.citrus.webx.handler;

import static com.alibaba.citrus.test.TestUtil.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.alibaba.citrus.webx.AbstractWebxTests;
import com.alibaba.citrus.webx.util.WebxUtil;

public class MainHandlerTests extends AbstractWebxTests {
    @Before
    public void init() throws Exception {
        prepareWebClient(null);
    }

    @After
    public void destroy() throws Exception {
        System.clearProperty("productionModeFromSystemProperties");
    }

    @Test
    public void internalRequest_productionMode() throws Exception {
        System.setProperty("productionModeFromSystemProperties", "true");
        prepareWebClient(null);

        // homepage - 不会导向main internal page
        assertHomepage("");
        assertHomepage("/");
        assertHomepage("?home");
        assertHomepage("/?a=1&home=&b=2");

        // 不存在的internal页面
        assertNotAvailable("/internal/notexist");

        // main - not available in production mode
        assertNotAvailable("/internal");
        assertNotAvailable("/internal/");

        // schema - not available in production mode
        assertNotAvailable("/internal/schema");
        assertNotAvailable("/internal/schema/");
    }

    private void assertNotAvailable(String url) throws Exception {
        invokeServlet(url);

        assertEquals(404, clientResponseCode);

        // http unit sendError的实现，真实服务器将返回web.xml中的error-page
        assertThat(clientResponseContent, containsAll("<html><head><title></title></head><body></body></html>"));
    }

    /**
     * 在开发模式下，访问/或/internal，都将显示main internal page。
     */
    @Test
    public void internalRequest_main() throws Exception {
        assertMainInternalPage("");
        assertMainInternalPage("/");
        assertMainInternalPage("/internal");
        assertMainInternalPage("/internal/");
    }

    /**
     * 在开发模式下，访问/?home，则显示原来的homepage。
     */
    @Test
    public void internalRequest_homepage() throws Exception {
        // 以下为home参数的几种形态，是用正则表达式匹配的
        assertHomepage("?home");
        assertHomepage("/?home=");
        assertHomepage("/?a=1&home=2&b=3");
        assertHomepage("/?a=1&home&b=3");

        // 只有/?home才会显示homepage
        assertMainInternalPage("/internal?home");
        assertMainInternalPage("/internal/?home");
    }

    private void assertHomepage(String url) throws Exception {
        invokeServlet(url);

        assertEquals(200, clientResponseCode);
        assertEquals("Homepage", clientResponseContent);
    }

    private void assertMainInternalPage(String url) throws Exception {
        invokeServlet(url);

        assertEquals(200, clientResponseCode);

        // 其值不同于ResourceServlet所返回的homepage页
        assertThat(clientResponseContent, not(equalTo("Homepage")));

        // 包含webx版本号
        assertThat(clientResponseContent, containsString(WebxUtil.getWebxVersion()));

        // 包含home
        assertThat(clientResponseContent, containsString("images/home1.gif\" alt=\"Home\" /> Home</a>"));

        // 包含application home
        assertThat(clientResponseContent,
                containsString("images/home2.gif\" alt=\"Application Home\" /> Application Home</a>"));
    }
}
