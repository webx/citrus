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
package com.alibaba.citrus.service.requestcontext.parser;

import static com.alibaba.citrus.test.TestEnvStatic.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static org.easymock.EasyMock.*;
import static org.easymock.classextension.EasyMock.*;
import static org.junit.Assert.*;

import java.io.File;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.easymock.classextension.EasyMock;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.alibaba.citrus.service.requestcontext.AbstractRequestContextsTests;
import com.alibaba.citrus.util.i18n.LocaleUtil;

/**
 * 测试<code>ParserRequestContext</code>。
 */
public class ParserRequestContextTests extends AbstractRequestContextsTests<ParserRequestContext> {
    @BeforeClass
    public static void initFactory() {
        createBeanFactory("services-parser.xml");
    }

    @Before
    public void init() throws Exception {
        invokeReadFileServlet("form.html");
        initRequestContext();

        // 设置thread context编码，以便url encoding正确执行
        LocaleUtil.setContext(null, "GBK");
    }

    @Test
    public void urlEncodeForm() throws Exception {
        invokeReadFileServlet("form2.html");
        initRequestContext();

        assertEquals("hello", requestContext.getParameters().getString("myparam"));
        assertEquals("hello", requestContext.getParameters().getStrings("myparam")[0]);
        assertEquals("中华人民共和国", requestContext.getParameters().getStrings("myparam")[1]);

        // 用request接口直接取值
        assertEquals("hello", newRequest.getParameter("myparam"));
        assertEquals("hello", newRequest.getParameterValues("myparam")[0]);
        assertEquals("中华人民共和国", newRequest.getParameterValues("myparam")[1]);
    }

    @Test
    public void multipartForm() throws Exception {
        assertEquals("hello", requestContext.getParameters().getString("myparam"));

        // 取得单个file item
        FileItem fileItem = requestContext.getParameters().getFileItem("myfile");

        assertEquals("myfile", fileItem.getFieldName());
        assertEquals(new File(srcdir, "smallfile.txt"), new File(fileItem.getName()));
        assertFalse(fileItem.isFormField());
        assertEquals(new String("中华人民共和国".getBytes("GBK"), "8859_1"), fileItem.getString());
        assertEquals("中华人民共和国", fileItem.getString("GBK"));
        assertTrue(fileItem.isInMemory());

        // 取得多个file items
        FileItem[] fileItems = requestContext.getParameters().getFileItems("myfile");
        String[] fileNames = requestContext.getParameters().getStrings("myfile");

        assertEquals(fileItems.length, fileNames.length);
        assertEquals(4, fileNames.length);

        assertEquals(new File(srcdir, "smallfile.txt"), new File(fileItems[0].getName()));
        assertEquals(new File(srcdir, "smallfile_.JPG"), new File(fileItems[1].getName())); // case insensitive
        assertEquals(new File(srcdir, "smallfile.gif"), new File(fileItems[2].getName()));
        assertEquals(new File(srcdir, "smallfile"), new File(fileItems[3].getName()));

        assertEquals(new File(srcdir, "smallfile.txt"), new File(fileNames[0]));
        assertEquals(new File(srcdir, "smallfile_.JPG"), new File(fileNames[1])); // case insensitive
        assertEquals(new File(srcdir, "smallfile.gif"), new File(fileNames[2]));
        assertEquals(new File(srcdir, "smallfile"), new File(fileNames[3]));

        // 用request接口直接取值
        assertEquals("hello", newRequest.getParameter("myparam"));
        assertEquals(new File(srcdir, "smallfile.txt"), new File(newRequest.getParameter("myfile")));
    }

    @Test
    public void uploaded_file_whitelist() throws Exception {
        invokeReadFileServlet("form.html");
        initRequestContext("parser_whitelist");

        // 取得多个file items
        FileItem[] fileItems = requestContext.getParameters().getFileItems("myfile");
        String[] fileNames = requestContext.getParameters().getStrings("myfile");

        assertEquals(fileItems.length, fileNames.length);
        assertEquals(4, fileNames.length);

        assertNull(fileItems[0]);
        assertEquals(new File(srcdir, "smallfile_.JPG"), new File(fileItems[1].getName())); // case insensitive
        assertEquals(new File(srcdir, "smallfile.gif"), new File(fileItems[2].getName()));
        assertNull(fileItems[3]);

        assertEquals("", fileNames[0]);
        assertEquals(new File(srcdir, "smallfile_.JPG"), new File(fileNames[1])); // case insensitive
        assertEquals(new File(srcdir, "smallfile.gif"), new File(fileNames[2]));
        assertEquals("", fileNames[3]);
    }

    @Test
    public void uploaded_file_whitelist2() throws Exception {
        invokeReadFileServlet("form.html");
        initRequestContext("parser_whitelist2");

        // 取得多个file items
        FileItem[] fileItems = requestContext.getParameters().getFileItems("myfile");
        String[] fileNames = requestContext.getParameters().getStrings("myfile");

        assertEquals(fileItems.length, fileNames.length);
        assertEquals(4, fileNames.length);

        assertEquals(new File(srcdir, "smallfile.txt"), new File(fileItems[0].getName()));
        assertNull(fileItems[1]);
        assertNull(fileItems[2]);
        assertEquals(new File(srcdir, "smallfile"), new File(fileItems[3].getName()));

        assertEquals(new File(srcdir, "smallfile.txt"), new File(fileNames[0]));
        assertEquals("", fileNames[1]);
        assertEquals("", fileNames[2]);
        assertEquals(new File(srcdir, "smallfile"), new File(fileNames[3]));
    }

    @Test
    public void cookies() throws Exception {
        assertEquals("mycookievalue", requestContext.getCookies().getString("mycookie"));

        requestContext.getCookies().setCookie("hello", "baobao");

        commitToClient();

        assertEquals("baobao", clientResponse.getNewCookieValue("hello"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void getParameterNames() {
        List<String> keys = createArrayList();

        for (Enumeration<String> e = newRequest.getParameterNames(); e.hasMoreElements();) {
            keys.add(e.nextElement());
        }

        assertArrayEquals(requestContext.getParameters().keySet().toArray(new String[0]), keys.toArray(new String[0]));
    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void getRequestMap() throws Exception {
        Map paramMap = newRequest.getParameterMap();

        // unmodifiable
        try {
            paramMap.put("test", "fail");
            fail("UnsupportedOperationException expected");
        } catch (UnsupportedOperationException e) {
        }

        try {
            paramMap.remove("myparam");
            fail("UnsupportedOperationException expected");
        } catch (UnsupportedOperationException e) {
        }

        try {
            ((Map.Entry) paramMap.entrySet().iterator().next()).setValue(null);
            fail("UnsupportedOperationException expected");
        } catch (UnsupportedOperationException e) {
        }

        // containsKey
        assertTrue(paramMap.containsKey("myparam"));
        assertFalse(paramMap.containsKey(new Object())); // not a String key

        // get
        assertNull(paramMap.get(new Object())); // not a String key

        String[] myparam = (String[]) paramMap.get("myparam");
        String[] myfile = (String[]) paramMap.get("myfile");
        String[] submit = (String[]) paramMap.get("submit");

        assertEquals("hello", myparam[0]);
        assertEquals("中华人民共和国", myparam[1]);

        assertEquals(new File(srcdir, "smallfile.txt"), new File(myfile[0]));

        assertEquals("upload", submit[0]);

        // keySet
        assertArrayEquals(requestContext.getParameters().keySet().toArray(new String[0]),
                paramMap.keySet().toArray(new String[0]));

        // values
        String[][] values = new String[requestContext.getParameters().size()][];

        int i = 0;
        for (String key : requestContext.getParameters().keySet()) {
            values[i++] = requestContext.getParameters().getStrings(key);
        }

        assertArrayEquals(values, paramMap.values().toArray(new String[0][]));
    }

    @Test
    public void toQueryString() throws Exception {
        ParameterParser params = requestContext.getParameters();

        assertEquals("myparam=hello&myparam=%D6%D0%BB%AA%C8%CB%C3%F1%B9%B2%BA%CD%B9%FA&submit=upload",
                params.toQueryString());

        params.setStrings("array", new String[] { "11", "22", "33" });

        assertEquals(
                "myparam=hello&myparam=%D6%D0%BB%AA%C8%CB%C3%F1%B9%B2%BA%CD%B9%FA&submit=upload&array=11&array=22&array=33",
                params.toQueryString());
    }

    @Test
    public void parse_post() throws Exception {
        // 在post/put方法中，将由引擎来解码form data
        Map<String, String[]> engineDecodedParams = createLinkedHashMap();
        engineDecodedParams.put("a", new String[] { "2" });

        initMockRequest("POST", "a=1", null, engineDecodedParams);
        initRequestContext();
        assertTrue(requestContext.isUseBodyEncodingForURI());
        assertArrayEquals(new String[] { "2" }, newRequest.getParameterValues("a"));

        initMockRequest("PUT", "a=1", null, engineDecodedParams);
        initRequestContext();
        assertTrue(requestContext.isUseBodyEncodingForURI());
        assertArrayEquals(new String[] { "2" }, newRequest.getParameterValues("a"));
    }

    @Test
    public void parse_get() throws Exception {
        String utf8_中国 = URLEncoder.encode("中国", "UTF-8");
        String gbk_中国 = URLEncoder.encode("中国", "GBK");

        String utf8_中国_raw = new String("中国".getBytes("UTF-8"), "8859_1"); // 模仿ie行为，不进行urlencoding
        String gbk_中国_raw = new String("中国".getBytes("GBK"), "8859_1"); // 模仿ie行为，不进行urlencoding

        Map<String, String[]> engineDecodedParams = createLinkedHashMap();
        engineDecodedParams.put("a", new String[] { "China" });

        // 在get/head方法中，将自行解析query，不调用引擎
        // 且useBodyEncodingForURI=true，使用指定的charset来解码
        initMockRequest("GET", "a=" + gbk_中国, "GBK", null);
        initRequestContext();
        assertFalse(requestContext.isUseServletEngineParser());
        assertTrue(requestContext.isUseBodyEncodingForURI());
        assertArrayEquals(new String[] { "中国" }, newRequest.getParameterValues("a"));

        initMockRequest("HEAD", gbk_中国 + "=1", "GBK", null);
        initRequestContext();
        assertFalse(requestContext.isUseServletEngineParser());
        assertTrue(requestContext.isUseBodyEncodingForURI());
        assertArrayEquals(new String[] { "1" }, newRequest.getParameterValues("中国"));

        initMockRequest("GET", "a=" + gbk_中国_raw, "GBK", null);
        initRequestContext();
        assertFalse(requestContext.isUseServletEngineParser());
        assertTrue(requestContext.isUseBodyEncodingForURI());
        assertArrayEquals(new String[] { "中国" }, newRequest.getParameterValues("a"));

        initMockRequest("HEAD", gbk_中国_raw + "=1", "GBK", null);
        initRequestContext();
        assertFalse(requestContext.isUseServletEngineParser());
        assertTrue(requestContext.isUseBodyEncodingForURI());
        assertArrayEquals(new String[] { "1" }, newRequest.getParameterValues("中国"));

        // 在get/head方法中，明确设置useServletEngineParser，此时useBodyEncodingForURI无效
        initMockRequest("GET", null, null, engineDecodedParams);
        initRequestContext("parser_useServletEngineParser");
        assertTrue(requestContext.isUseServletEngineParser());
        assertTrue(requestContext.isUseBodyEncodingForURI());
        assertArrayEquals(new String[] { "China" }, newRequest.getParameterValues("a"));

        // 在get/head方法中，useBodyEncodingForURI=false，总是使用utf-8解码
        initMockRequest("GET", "a=" + utf8_中国, "GBK", null);
        initRequestContext("parser_dont_useBodyEncodingForURI");
        assertFalse(requestContext.isUseServletEngineParser());
        assertFalse(requestContext.isUseBodyEncodingForURI());
        assertEquals("UTF-8", requestContext.getURIEncoding());
        assertArrayEquals(new String[] { "中国" }, newRequest.getParameterValues("a"));

        initMockRequest("GET", "a=" + utf8_中国_raw, "GBK", null);
        initRequestContext("parser_dont_useBodyEncodingForURI");
        assertFalse(requestContext.isUseServletEngineParser());
        assertFalse(requestContext.isUseBodyEncodingForURI());
        assertEquals("UTF-8", requestContext.getURIEncoding());
        assertArrayEquals(new String[] { "中国" }, newRequest.getParameterValues("a"));

        // 在get/head方法中，useBodyEncodingForURI=true，但未指定charset，则使用ISO-8859-1
        initMockRequest("GET", "a=" + gbk_中国, null, null);
        initRequestContext();
        assertFalse(requestContext.isUseServletEngineParser());
        assertTrue(requestContext.isUseBodyEncodingForURI());
        assertArrayEquals(new String[] { new String("中国".getBytes("GBK"), "8859_1") },
                newRequest.getParameterValues("a"));
    }

    @Test
    public void parse_query() throws Exception {
        // 多值
        initMockRequest("GET", "  &a=1&a=2&b=3& ", "GBK", null);
        initRequestContext();
        assertArrayEquals(new String[] { "1", "2" }, newRequest.getParameterValues("a"));
        assertArrayEquals(new String[] { "3" }, newRequest.getParameterValues("b"));
        assertArrayEquals(new Object[] { "a", "b" }, newRequest.getParameterMap().keySet().toArray());

        // 空值
        initMockRequest("GET", "a", "GBK", null);
        initRequestContext();
        assertArrayEquals(new String[] {}, newRequest.getParameterValues("a"));
        assertArrayEquals(new Object[] { "a" }, newRequest.getParameterMap().keySet().toArray());

        initMockRequest("GET", "a&b=&=1", "GBK", null);
        initRequestContext();
        assertArrayEquals(new String[] {}, newRequest.getParameterValues("a"));
        assertArrayEquals(new String[] {}, newRequest.getParameterValues("b"));
        assertArrayEquals(new Object[] { "a", "b" }, newRequest.getParameterMap().keySet().toArray());
    }

    @Test
    public void parse_trimming() throws Exception {
        initMockRequest("GET", "  &a=++&a=+2+&b=+3+& ", "GBK", null);
        initRequestContext();
        assertArrayEquals(new String[] { "", "2" }, newRequest.getParameterValues("a"));
        assertArrayEquals(new String[] { "3" }, newRequest.getParameterValues("b"));
        assertArrayEquals(new Object[] { "a", "b" }, newRequest.getParameterMap().keySet().toArray());
    }

    private void initMockRequest(String method, String queryString, String charset,
                                 Map<String, String[]> engineDecodedParams) {
        request = createMock(HttpServletRequest.class);
        response = createMock(HttpServletResponse.class);

        request.setAttribute(EasyMock.<String> anyObject(), anyObject());
        expectLastCall().anyTimes();

        expect(request.getLocale()).andReturn(Locale.CHINA).anyTimes();
        expect(request.getContentType()).andReturn(null).anyTimes();
        expect(request.getMethod()).andReturn(method).anyTimes();
        expect(request.getQueryString()).andReturn(queryString).anyTimes();
        expect(request.getCharacterEncoding()).andReturn(charset).anyTimes();

        if (engineDecodedParams != null) {
            expect(request.getParameterMap()).andReturn(engineDecodedParams).anyTimes();
        }

        replay(request, response);
    }
}
