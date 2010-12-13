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
package com.alibaba.citrus.service.upload;

import static com.alibaba.citrus.test.TestEnvStatic.*;
import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.io.FileSystemResource;

import com.alibaba.citrus.service.upload.impl.cfu.DiskFileItem;
import com.alibaba.citrus.springext.support.context.XmlBeanFactory;
import com.alibaba.citrus.util.io.StreamUtil;
import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebForm;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.meterware.servletunit.InvocationContext;
import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;
import com.meterware.servletunit.UploadServletRunner;

/**
 * 测试<code>UploadService</code>。
 * 
 * @author Michael Zhou
 */
public class UploadServiceTests {
    private static File 中文文件名;
    private static BeanFactory factory;
    private UploadService upload;
    private ServletUnitClient client;
    private HttpServletRequest request;

    @BeforeClass
    public static void initFactory() throws Exception {
        factory = new XmlBeanFactory(new FileSystemResource(new File(srcdir, "services.xml")));

        // 创建“中文文件名.txt”
        中文文件名 = new File(destdir, "中文文件名.txt");
        PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(中文文件名), "GBK"), true);

        for (int i = 0; i < 16; i++) {
            out.println("我爱北京天安门");
        }

        out.flush();
        out.close();
    }

    @Before
    public void init() throws Exception {
        upload = (UploadService) factory.getBean("uploadService");

        // Servlet container
        ServletRunner servletRunner = new FilterServletRunner();

        servletRunner.registerServlet("myservlet", MyServlet.class.getName());

        // Servlet client
        client = servletRunner.newClient();

        // 取得初始页面form.html
        WebResponse response = client.getResponse(new GetMethodWebRequest("http://localhost/myservlet"));

        WebForm form = response.getFormWithName("myform");

        // 取得提交form的request
        WebRequest request = form.getRequest();

        request.setParameter("myparam", "中华人民共和国");
        request.selectFile("myfile", new File(srcdir, "smallfile.txt"));

        File nonAsciiFile = 中文文件名;

        if (nonAsciiFile.exists()) {
            request.selectFile("myfile_中文", nonAsciiFile);
        } else {
            fail("Could not find non-ascii filename: " + nonAsciiFile.getAbsolutePath()
                    + ".  Please make sure the OS charset is correctly set.");
        }

        InvocationContext invocationContext = client.newInvocation(request);

        this.request = invocationContext.getRequest();

        // 因为页面的content type是text/html; charset=UTF-8，
        // 所以应该以UTF-8方式解析request。
        this.request.setCharacterEncoding("UTF-8");
    }

    @Test
    public void isMultipartContent() throws Exception {
        assertTrue(upload.isMultipartContent(request));

        // 初始页面的请求是一个普通的“application/x-www-form-urlencoded”请求。
        WebRequest request = new GetMethodWebRequest("http://localhost/myservlet");
        InvocationContext invocationContext = client.newInvocation(request);

        assertFalse(upload.isMultipartContent(invocationContext.getRequest()));
    }

    @Test
    public void upload() throws Exception {
        FileItem[] items = upload.parseRequest(request);

        assertEquals(4, items.length);

        // 参数的顺序是根据form.html中的field的顺序来的
        // 第一个参数：<input type="text" name="myparam"/>
        assertEquals("myparam", items[0].getFieldName());
        assertNull(items[0].getName());
        assertTrue(items[0].isFormField());
        assertTrue(items[0].isInMemory());
        assertEquals("中华人民共和国", items[0].getString()); // 自动以UTF-8解码

        // 第二个参数：<input type="file" name="myfile"/>
        assertEquals("myfile", items[1].getFieldName());
        assertEquals(new File(srcdir, "smallfile.txt"), new File(items[1].getName()));
        assertFalse(items[1].isFormField());

        // 对于file类型，不会自动用UTF-8解码，默认使用8859_1
        assertEquals(new String("中华人民共和国".getBytes("GBK"), "8859_1"), items[1].getString());
        assertEquals("中华人民共和国", items[1].getString("GBK"));

        // 这么小的文件，应该放在内存中
        assertTrue(items[1].isInMemory());

        // 第三个参数：<input type="file" name="myfile_中文"/>
        assertEquals("myfile_中文", items[2].getFieldName()); // 自动以UTF-8解码header
        assertEquals(中文文件名, new File(items[2].getName())); // 自动以UTF-8解码header
        assertFalse(items[2].isFormField());

        // 这个文件比较大，应该在文件中
        assertTrue(items[2].getSize() > 100);
        assertFalse(items[2].isInMemory());

        // 第四个参数：<input type="submit" name="submit" value="upload"/>
        assertEquals("submit", items[3].getFieldName());
        assertNull(items[3].getName());
        assertTrue(items[3].isFormField());
        assertTrue(items[3].isInMemory());
        assertEquals("upload", items[3].getString()); // 自动以UTF-8解码
    }

    @Test
    public void uploadOverrideRepository() throws Exception {
        File repositoryPath = new File(destdir, "hello");
        UploadParameters params = new UploadParameters();

        params.setRepository(repositoryPath);
        params.setSizeThreshold(0); // 强制写入文件

        FileItem[] items = upload.parseRequest(request, params);

        assertEquals(4, items.length);

        File storeLocation = ((DiskFileItem) items[1]).getStoreLocation();

        assertEquals(repositoryPath, storeLocation.getParentFile());
    }

    @Test(expected = UploadSizeLimitExceededException.class)
    public void uploadOverrideSizeMax() {
        UploadParameters params = new UploadParameters();

        params.setSizeMax(1);

        upload.parseRequest(request, params);
    }

    @Test(expected = UploadSizeLimitExceededException.class)
    public void uploadOverrideFileSizeMax() {
        UploadParameters params = new UploadParameters();

        params.setSizeMax(1000000);
        params.setFileSizeMax(1);

        upload.parseRequest(request, params);
    }

    @Test
    public void uploadOverrideThreshold() throws Exception {
        UploadParameters params = new UploadParameters();

        params.setSizeThreshold(0);

        FileItem[] items = upload.parseRequest(request, params);

        assertEquals(4, items.length);

        // 参数的顺序是根据form.html中的field的顺序来的
        // 第一个参数：<input type="text" name="myparam"/>
        assertEquals("myparam", items[0].getFieldName());
        assertNull(items[0].getName());
        assertTrue(items[0].isFormField());
        assertTrue(items[0].isInMemory()); // 当threshold为0时，form field永远在内存中
        assertEquals("中华人民共和国", items[0].getString()); // 自动以UTF-8解码

        // 第二个参数：<input type="file" name="myfile"/>
        assertEquals("myfile", items[1].getFieldName());
        assertEquals(new File(srcdir, "smallfile.txt"), new File(items[1].getName()));
        assertFalse(items[1].isFormField());

        // 对于file类型，不会自动用UTF-8解码，默认使用8859_1
        assertEquals(new String("中华人民共和国".getBytes("GBK"), "8859_1"), items[1].getString());
        assertEquals("中华人民共和国", items[1].getString("GBK"));

        // 因为threshold是0, 所以上传文件总是保存在文件系统中
        assertFalse(items[1].isInMemory());

        // 第三个参数：<input type="file" name="myfile_中文"/>
        assertEquals("myfile_中文", items[2].getFieldName()); // 自动以UTF-8解码header
        assertEquals(中文文件名, new File(items[2].getName())); // 自动以UTF-8解码header
        assertFalse(items[2].isFormField());

        // file类型一定在文件中
        assertTrue(items[2].getSize() > 100);
        assertFalse(items[2].isInMemory());

        // 第四个参数：<input type="submit" name="submit" value="upload"/>
        assertEquals("submit", items[3].getFieldName());
        assertNull(items[3].getName());
        assertTrue(items[3].isFormField());
        assertTrue(items[3].isInMemory()); // form field永远在内存中
        assertEquals("upload", items[3].getString()); // 自动以UTF-8解码
    }

    @Test
    public void fullConfig() {
        upload = (UploadService) factory.getBean("upload2");

        assertEquals(new File("/tmp/upload").toURI().toString(), upload.getRepository().toURI().toString());
        assertEquals("100", upload.getSizeMax().toString());
        assertEquals("200", upload.getFileSizeMax().toString());
        assertEquals("300", upload.getSizeThreshold().toString());
        assertEquals(true, upload.isKeepFormFieldInMemory());
        assertArrayEquals(new String[] { "filename", "fname" }, upload.getFileNameKey());
    }

    @Test
    public void toString_() {
        FileItem[] items = upload.parseRequest(request);

        assertEquals(4, items.length);

        // 参数的顺序是根据form.html中的field的顺序来的
        // 第一个参数：<input type="text" name="myparam"/>
        assertEquals("中华人民共和国", items[0].toString());

        // 第二个参数：<input type="file" name="myfile"/>
        assertEquals(new File(srcdir, "smallfile.txt").getAbsolutePath(), items[1].toString());

        // 第三个参数：<input type="file" name="myfile_中文"/>
        assertEquals(中文文件名.getAbsolutePath(), items[2].toString());

        // 第四个参数：<input type="submit" name="submit" value="upload"/>
        assertEquals("upload", items[3].toString());
    }

    /**
     * 过滤httpunit生成的request content：
     * <ul>
     * <li>过滤掉Content-Type header，因为在正式浏览器中不会出现这个。</li>
     * <li>将filename="...\\..."中的双斜杠换成单斜杠，因为正式浏览器也不会出现双斜杠。</li>
     * </ul>
     */
    private static final class FilterServletRunner extends UploadServletRunner {
        @Override
        protected byte[] filter(WebRequest request, byte[] messageBody) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            try {
                BufferedReader reader = new BufferedReader(new StringReader(new String(messageBody, "ISO-8859-1")));
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(baos, "ISO-8859-1"), true);
                String line;

                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("Content-Type: text/plain; charset=")) {
                        continue;
                    }

                    if (line.indexOf("filename=") > 0) {
                        line = line.replaceAll("\\\\+", "\\\\");
                    }

                    writer.printf("%s\r\n", line); // 注意：此处非platform-specific换行。
                }

                writer.flush();
            } catch (IOException e) {
                fail(e.getMessage());
            }

            return baos.toByteArray();
        }
    }

    public static class MyServlet extends HttpServlet {
        private static final long serialVersionUID = 3258413932522648633L;

        @Override
        protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException,
                IOException {
            response.setContentType("text/html; charset=UTF-8");

            PrintWriter out = response.getWriter();

            String html = StreamUtil.readText(new FileInputStream(new File(srcdir, "form.html")), "GBK", true);

            out.println(html);
        }

        @Override
        protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
                IOException {
            doGet(request, response);
        }
    }
}
