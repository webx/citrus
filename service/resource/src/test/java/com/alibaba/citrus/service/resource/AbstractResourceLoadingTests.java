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
package com.alibaba.citrus.service.resource;

import static com.alibaba.citrus.test.TestEnvStatic.*;
import static com.alibaba.citrus.util.ArrayUtil.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.springframework.context.ApplicationContext;
import org.springframework.core.io.FileSystemResource;

import com.alibaba.citrus.service.resource.support.ResourceLoadingSupport;
import com.alibaba.citrus.springext.support.context.XmlApplicationContext;
import com.alibaba.citrus.springext.support.context.XmlWebApplicationContext;
import com.meterware.servletunit.InvocationContext;
import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;

public abstract class AbstractResourceLoadingTests {
    protected static ServletUnitClient client;
    protected static ServletContext servletContext;
    protected final static ThreadLocal<URL> nextGetResourceURL = new ThreadLocal<URL>();

    protected static ApplicationContext parentFactory;
    protected static ApplicationContext factory;

    protected ResourceLoadingService resourceLoadingService;

    protected static void initServlet() throws Exception {
        ServletRunner servletRunner = new ServletRunner(new File(srcdir, "WEB-INF/web.xml"), "");
        client = servletRunner.newClient();

        InvocationContext ic = client.newInvocation("http://www.taobao.com/app1");
        servletContext = new ServletContextWrapper(ic.getServlet().getServletConfig().getServletContext());

        assertNotNull(servletContext);
    }

    protected static void initFactory(String configFile) throws Exception {
        initServlet();

        System.setProperty("project.home", new File("").getAbsolutePath());
        System.setProperty("srcdir", srcdir.getAbsolutePath());
        XmlWebApplicationContext webCtx = new XmlWebApplicationContext();

        webCtx.setResourceLoadingExtender(new ResourceLoadingSupport(webCtx));
        webCtx.setConfigLocation(new File(srcdir, configFile).toURI().toString());
        webCtx.setServletContext(servletContext);
        webCtx.refresh();

        parentFactory = webCtx;
        factory = webCtx;
    }

    protected static void initSubFactory(String configFile) throws Exception {
        factory = new XmlApplicationContext(new FileSystemResource(new File(srcdir, configFile)), parentFactory);
    }

    protected void assertResourceService(String resourceName, String fileName, boolean exists) throws Exception {
        Resource resource;

        try {
            resource = resourceLoadingService.getResource(resourceName);
        } catch (ResourceNotFoundException e) {
            if (!exists) {
                return;
            } else {
                throw e;
            }
        }

        assertTrue(exists);
        assertNotNull(resource);
        assertTrue(resource.exists());
        assertEquals(new File(srcdir, fileName).getAbsolutePath(), resource.getFile().getAbsolutePath());
    }

    protected void assertResourceServiceList(String resourceName, String fileName, boolean exists, boolean existsList,
                                             String... list) throws Exception {
        assertResourceService(resourceName, fileName, exists);

        // list
        String[] names;

        try {
            names = resourceLoadingService.list(resourceName);
        } catch (ResourceNotFoundException e) {
            if (!existsList) {
                return;
            } else {
                throw e;
            }
        }

        assertNames(existsList, new File(srcdir, fileName), names, list);

        // resources
        Resource[] resources;

        try {
            resources = resourceLoadingService.listResources(resourceName);
        } catch (ResourceNotFoundException e) {
            if (!existsList) {
                return;
            } else {
                throw e;
            }
        }

        assertResources(existsList, new File(srcdir, fileName), resources, list);
    }

    protected void assertNames(boolean exists, File basedir, String[] names, String[] results) throws Exception {
        if (exists) {
            names = filter(names);
            assertArrayEquals(results, names);

            assertTrue(basedir.exists()); // 目录存在

            for (String name : names) {
                String noSlashName = name;

                if (name.endsWith("/")) {
                    noSlashName = name.substring(0, name.length() - 1);
                }

                assertTrue(!noSlashName.contains("/"));
                assertTrue(!isEmpty(name));

                File file = new File(basedir, name);
                assertTrue(file.getAbsolutePath(), file.exists());
            }
        } else {
            assertNull(names);
        }
    }

    protected void assertResources(boolean exists, File basedir, Resource[] resources, String[] results)
            throws Exception {
        if (exists) {
            String[] names = filter(resources);
            assertArrayEquals(results, names);

            assertTrue(basedir.exists()); // 目录存在

            for (String name : names) {
                String noSlashName = name;

                if (name.endsWith("/")) {
                    noSlashName = name.substring(0, name.length() - 1);
                }

                assertTrue(!noSlashName.contains("/"));
                assertTrue(!isEmpty(name));

                File file = new File(basedir, name);
                assertTrue(file.getAbsolutePath(), file.exists());
            }
        } else {
            assertNull(resources);
        }
    }

    /**
     * 除去ignore文件。
     */
    private String[] filter(String[] names) {
        Arrays.sort(names);

        List<String> list = createLinkedList(names);

        for (Iterator<String> i = list.iterator(); i.hasNext();) {
            String name = i.next();

            if (name.startsWith(".") || "CVS".equals(name)) {
                i.remove();
            }
        }

        return list.toArray(new String[list.size()]);
    }

    /**
     * 除去ignore文件。
     */
    private String[] filter(Resource[] resources) {
        Arrays.sort(resources, new Comparator<Resource>() {
            public int compare(Resource o1, Resource o2) {
                return getResourceFileName(o1).compareTo(getResourceFileName(o2));
            }
        });

        List<String> list = createLinkedList();

        for (Resource resource : resources) {
            String name = getResourceFileName(resource);

            if (name.startsWith(".") || "CVS".equals(name)) {
                continue;
            } else {
                list.add(name);
            }
        }

        return list.toArray(new String[list.size()]);
    }

    private String getResourceFileName(Resource resource) {
        String name = resource.getURL().toExternalForm();
        boolean isDirectory = name.endsWith("/");

        if (isDirectory) {
            name = name.substring(0, name.length() - 1);
        }

        return name.substring(name.lastIndexOf("/") + 1) + (isDirectory ? "/" : "");
    }

    public static class MyServlet extends HttpServlet {
        private static final long serialVersionUID = 5198015331020551722L;
    }

    /**
     * httpunit未实现getResourcePaths方法。
     */
    public static class ServletContextWrapper implements ServletContext {
        private final ServletContext servletContext;

        public ServletContextWrapper(ServletContext servletContext) {
            this.servletContext = servletContext;
        }

        public URL getResource(String path) throws MalformedURLException {
            URL presetResult = nextGetResourceURL.get();

            if (presetResult != null) {
                return presetResult;
            } else {
                return servletContext.getResource(path);
            }
        }

        public Set<?> getResourcePaths(String path) {
            try {
                URL baseURL = getResource(path);
                File basedir = null;

                if (baseURL != null) {
                    basedir = new File(baseURL.toURI());
                } else {
                    return null;
                }

                String[] names = basedir.list();

                if (isEmptyArray(names)) {
                    return null;
                }

                Set<String> nameSet = createHashSet();

                if (!path.endsWith("/")) {
                    path += "/";
                }

                for (String name : names) {
                    File file = new File(basedir, name);
                    nameSet.add(path + name + (file.isDirectory() ? "/" : ""));
                }

                return nameSet;
            } catch (Exception e) {
                return null;
            }
        }

        public Object getAttribute(String name) {
            return servletContext.getAttribute(name);
        }

        public Enumeration<?> getAttributeNames() {
            return servletContext.getAttributeNames();
        }

        public ServletContext getContext(String uripath) {
            return servletContext.getContext(uripath);
        }

        public String getContextPath() {
            return servletContext.getContextPath();
        }

        public String getInitParameter(String name) {
            return servletContext.getInitParameter(name);
        }

        public Enumeration<?> getInitParameterNames() {
            return servletContext.getInitParameterNames();
        }

        public int getMajorVersion() {
            return servletContext.getMajorVersion();
        }

        public String getMimeType(String file) {
            return servletContext.getMimeType(file);
        }

        public int getMinorVersion() {
            return servletContext.getMinorVersion();
        }

        public RequestDispatcher getNamedDispatcher(String name) {
            return servletContext.getNamedDispatcher(name);
        }

        public String getRealPath(String path) {
            return servletContext.getRealPath(path);
        }

        public RequestDispatcher getRequestDispatcher(String path) {
            return servletContext.getRequestDispatcher(path);
        }

        public InputStream getResourceAsStream(String path) {
            return servletContext.getResourceAsStream(path);
        }

        public String getServerInfo() {
            return servletContext.getServerInfo();
        }

        @Deprecated
        public Servlet getServlet(String name) throws ServletException {
            return servletContext.getServlet(name);
        }

        public String getServletContextName() {
            return servletContext.getServletContextName();
        }

        @Deprecated
        public Enumeration<?> getServletNames() {
            return servletContext.getServletNames();
        }

        @Deprecated
        public Enumeration<?> getServlets() {
            return servletContext.getServlets();
        }

        @Deprecated
        public void log(Exception exception, String msg) {
            servletContext.log(exception, msg);
        }

        public void log(String message, Throwable throwable) {
            servletContext.log(message, throwable);
        }

        public void log(String msg) {
            servletContext.log(msg);
        }

        public void removeAttribute(String name) {
            servletContext.removeAttribute(name);
        }

        public void setAttribute(String name, Object object) {
            servletContext.setAttribute(name, object);
        }
    }
}
