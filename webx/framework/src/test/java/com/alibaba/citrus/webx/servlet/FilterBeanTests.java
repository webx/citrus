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
package com.alibaba.citrus.webx.servlet;

import static com.alibaba.citrus.test.TestUtil.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static org.easymock.EasyMock.*;
import static org.easymock.classextension.EasyMock.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeansException;

import com.alibaba.citrus.test.TestEnvStatic;

public class FilterBeanTests {
    private FilterBeanImpl filter;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private ServletContext context;
    private FilterConfig config;
    private Map<String, String> initParams;

    private boolean inited;
    private boolean beanWrapperInited;

    static {
        TestEnvStatic.init();
    }

    @Before
    public void init() throws Exception {
        initFilter();
    }

    private void initFilter(String... required) throws Exception {
        filter = new FilterBeanImpl();

        request = createMock(HttpServletRequest.class);
        response = createMock(HttpServletResponse.class);
        context = createMock(ServletContext.class);
        config = createMock(FilterConfig.class);

        Hashtable<String, String> params = new Hashtable<String, String>();

        if (initParams != null) {
            params.putAll(initParams);
        }

        if (required != null) {
            for (String requiredName : required) {
                filter.addRequiredProperty(requiredName);
            }
        }

        expect(config.getFilterName()).andReturn("myFilter").anyTimes();
        expect(config.getServletContext()).andReturn(context).anyTimes();
        expect(config.getInitParameterNames()).andReturn(params.keys()).anyTimes();

        for (String key : params.keySet()) {
            expect(config.getInitParameter(key)).andReturn(params.get(key)).anyTimes();
        }

        context.log("Initializing filter: myFilter");
        context.log("FilterBeanImpl - myFilter: initialization completed");

        replay(request, response, context, config);

        filter.init(config);
    }

    @Test
    public void getFilterConfig() {
        assertSame(config, filter.getFilterConfig());
    }

    @Test
    public void getServletContext() {
        assertSame(context, filter.getServletContext());
    }

    @Test
    public void getFilterName() {
        assertEquals("myFilter", filter.getFilterName());
    }

    @Test
    public void initHooks() {
        assertTrue(inited);
        assertTrue(beanWrapperInited);
    }

    @Test
    public void missingRequired() throws Exception {
        initParams = createHashMap();
        initParams.put("aaa", "111");

        try {
            initFilter("aaa", "bbb", "ccc");
            fail();
        } catch (ServletException e) {
            assertThat(
                    e,
                    exception(IllegalArgumentException.class, "Failed to set bean properties on filter: myFilter",
                            "Initialization for filter myFilter failed.  ",
                            "The following required properties were missing: [bbb, ccc]"));
        }
    }

    @Test
    public void injecting() throws Exception {
        initParams = createHashMap();
        initParams.put("aaa", "111");
        initParams.put("bbb", "222");
        initParams.put("ccc", "v3");
        initParams.put("ddd", "444");

        initFilter("aaa", "bbb", "ccc");

        assertEquals("111", filter.aaa);
        assertEquals(222, filter.bbb);
        assertEquals(MyEnum.v3, filter.ccc);
        assertEquals((Long) 444L, filter.ddd);
    }

    @Test
    public void setPropertyFailed() throws Exception {
        initParams = createHashMap();
        initParams.put("eee", "555");

        try {
            initFilter();
            fail();
        } catch (ServletException e) {
            assertThat(e, exception(BeansException.class, "Failed to set bean properties on filter: myFilter"));
        }
    }

    public class FilterBeanImpl extends FilterBean {
        private String aaa;
        private int bbb;
        private MyEnum ccc;
        private Long ddd;

        public void setAaa(String aaa) {
            this.aaa = aaa;
        }

        public void setBbb(int bbb) {
            this.bbb = bbb;
        }

        public void setCcc(MyEnum ccc) {
            this.ccc = ccc;
        }

        public void setDdd(Long ddd) {
            this.ddd = ddd;
        }

        public void setEee(String eee) {
            throw new UnsupportedOperationException();
        }

        @Override
        protected void init() throws ServletException {
            inited = true;
        }

        @Override
        protected void initBeanWrapper(BeanWrapper bw) throws BeansException {
            beanWrapperInited = true;
        }

        @Override
        protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
                throws IOException, ServletException {
        }
    }

    public enum MyEnum {
        v1,
        v2,
        v3
    }
}
