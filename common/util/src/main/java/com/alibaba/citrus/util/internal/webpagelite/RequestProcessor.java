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
package com.alibaba.citrus.util.internal.webpagelite;

import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.ObjectUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;
import static com.alibaba.citrus.util.internal.webpagelite.ServletRequestContext.*;
import static com.alibaba.citrus.util.io.StreamUtil.*;
import static javax.servlet.http.HttpServletResponse.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alibaba.citrus.util.internal.templatelite.Template;
import com.alibaba.citrus.util.internal.templatelite.TextWriter;

/**
 * 一个简单的、利用templatelite.<code>Template</code>模板生成WEB页面及相关资源的工具类。
 * 
 * @author Michael Zhou
 */
public abstract class RequestProcessor<RC extends RequestContext> implements PageComponentRegistry {
    private final static String DEFAULT_CONTENT_TYPE = "application/octet-stream";
    private final static Map<String, String> contentTypes;
    private final Map<String, PageComponent> components = createTreeMap(new Comparator<String>() {
        public int compare(String s1, String s2) {
            int lenDiff = s2.length() - s1.length();

            if (lenDiff != 0) {
                return lenDiff; // 先按名称长度倒排序
            } else {
                return s1.compareTo(s2); // 再按字母顺序排序
            }
        }
    });

    static {
        contentTypes = createHashMap();

        contentTypes.put("css", "text/css");
        contentTypes.put("js", "application/javascript");
        contentTypes.put("html", "text/html");
        contentTypes.put("htm", "text/html");
        contentTypes.put("xml", "text/xml");
        contentTypes.put("txt", "text/plain");
        contentTypes.put("gif", "image/gif");
        contentTypes.put("jpg", "image/jpeg");
        contentTypes.put("png", "image/png");
        contentTypes.put("ico", "image/x-icon");
    }

    /**
     * 注册一个组件。
     */
    public void register(String componentPath, PageComponent component) {
        assertNotNull(componentPath, "componentPath is null");
        assertTrue(componentPath.length() == 0 || !componentPath.startsWith("/") && componentPath.endsWith("/"),
                "invalid componentPath: %s", componentPath);
        assertTrue(!components.containsKey(componentPath), "duplicated component: %s", componentPath);

        components.put(componentPath, component);
    }

    /**
     * 取得所有的componentPaths。
     */
    public String[] getComponentPaths() {
        return components.keySet().toArray(new String[components.size()]);
    }

    /**
     * 取得指定名称的组件。
     */
    public <PC extends PageComponent> PC getComponent(String componentPath, Class<PC> componentClass) {
        componentPath = PageComponent.normalizeComponentPath(componentPath);

        PageComponent component = assertNotNull(components.get(componentPath), "Component not found: %s", componentPath);

        if (componentClass != null) {
            return componentClass.cast(component);
        } else {
            @SuppressWarnings("unchecked")
            PC pc = (PC) component;
            return pc;
        }
    }

    /**
     * 处理请求。
     */
    public final void processRequest(final RC request) throws IOException {
        final String resourceName = request.getResourceName();

        // 先看看是不是css、js之类的资源文件，或者资源文件模板
        URL tmpres = getRawResource(resourceName);
        final boolean template;

        if (tmpres == null) {
            tmpres = getRawResource(resourceName + ".tpl");
            template = true;
        } else {
            template = false;
        }

        final URL resource = tmpres;

        if (resource != null && !resource.getPath().endsWith("/")) {
            checkLastModified(request, getLastModifiedOfResource(request, resource, template), new Runnable() {
                public void run() throws IOException {
                    if (beforeRenderingResource(request, resource, template)) {
                        renderResource(request, resource, template);
                    }
                }
            });

            return;
        }

        // 查找resource
        boolean found = resourceExists(resourceName);

        if (!found && !resourceName.endsWith("/")) {
            String indexPage = resourceName + "/";

            if (resourceExists(indexPage)) {
                request.redirectTo(request.getResourceURL(indexPage)); // 重定向到目录的正规形式
                return;
            }
        }

        // 报错：资源找不到
        if (!found) {
            request.resourceNotFound(resourceName);
            return;
        }

        // 渲染页面
        checkLastModified(request, getLastModifiedOfPage(request, resourceName), new Runnable() {
            public void run() throws IOException {
                if (beforeRenderingPage(request, resourceName)) {
                    renderPage(request, resourceName);
                }
            }
        });
    }

    private void checkLastModified(RC request, long lastModified, Runnable runner) throws IOException {
        ServletRequestContext servletRequest = getServletRequestContext(request);

        // 假如：
        // 1. request为servlet request；
        //    并且，lastModified >= 0；
        //    并且，GET方法；
        // 2. 或者，lastModified > ifModifiedSince
        // 执行runner
        if (lastModified < 0 || servletRequest == null
                || !"get".equalsIgnoreCase(servletRequest.getRequest().getMethod())) {
            runner.run();
        } else {
            long ifModifiedSince = servletRequest.getRequest().getDateHeader("If-Modified-Since");

            if (ifModifiedSince < lastModified / 1000 * 1000) {
                if (lastModified >= 0) {
                    servletRequest.getResponse().setDateHeader("Last-Modified", lastModified);
                }

                runner.run();
            } else {
                servletRequest.getResponse().setStatus(SC_NOT_MODIFIED);
            }
        }
    }

    /**
     * 判断资源是否存在。
     */
    protected abstract boolean resourceExists(String resourceName);

    /**
     * 查找指定名称的资源URL，如果没找到，就返回<code>null</code>。
     * <p>
     * 先查找所有注册的component，再查找当前类同级目录的classpath。
     * </p>
     */
    private URL getRawResource(String resourceName) {
        for (String componentPath : components.keySet()) {
            if (resourceName.startsWith(componentPath)) {
                String componentResourceName = resourceName.substring(componentPath.length());
                URL resource = components.get(componentPath).getClass().getResource(componentResourceName);

                if (resource == null) {
                    resource = getClass().getResource(componentResourceName);
                }

                return resource;
            }
        }

        URL resource = null;
        Set<String> visitedPackages = createHashSet();

        for (Class<?> processorClass = getClass(); processorClass != null
                && RequestProcessor.class.isAssignableFrom(processorClass); processorClass = processorClass
                .getSuperclass()) {
            String processorPackage = processorClass.getPackage().getName();

            if (visitedPackages.contains(processorPackage)) {
                continue;
            }

            visitedPackages.add(processorPackage);
            resource = processorClass.getResource(resourceName);

            if (resource != null) {
                break;
            }
        }

        return resource;
    }

    protected long getLastModifiedOfPage(RC request, String resourceName) throws IOException {
        return -1;
    }

    protected long getLastModifiedOfResource(RC request, URL resource, boolean template) throws IOException {
        if (!template) {
            return resource.openConnection().getLastModified();
        }

        return -1;
    }

    protected boolean beforeRenderingPage(RC request, String resourceName) throws IOException {
        return true;
    }

    protected boolean beforeRenderingResource(RC request, URL resource, boolean template) throws IOException {
        return true;
    }

    /**
     * 渲染页面。
     */
    protected abstract void renderPage(RC request, String resourceName) throws IOException;

    /**
     * 渲染css、js之类的资源文件，或者资源文件模板。
     */
    private void renderResource(final RC request, URL resource, boolean template) throws IOException {
        String resourceName = request.getResourceName();
        int extIndex = resourceName.indexOf(".", resourceName.lastIndexOf("/") + 1);
        String ext = extIndex > 0 ? resourceName.substring(extIndex + 1) : null;
        String contentType = defaultIfNull(contentTypes.get(ext), DEFAULT_CONTENT_TYPE);

        if (!template) {
            io(resource.openStream(), request.getOutputStream(contentType), true, true);
        } else {
            Template tpl = new Template(resource, "ISO-8859-1");
            PrintWriter out = request.getWriter(contentType);

            // 对于模板文件中，${url:relativeUrl}将被转换成完整的URL
            tpl.accept(new TextWriter<PrintWriter>(out) {
                @SuppressWarnings("unused")
                public void visitUrl(String relativeUrl) {
                    out().print(request.getResourceURL(relativeUrl));
                }
            });

            out.flush();
        }
    }

    /**
     * 取得所有component的资源文件。
     */
    protected List<String> getComponentResources(String ext) {
        List<String> names = createLinkedList();

        ext = trimToEmpty(ext);

        if (!ext.startsWith(".")) {
            ext = "." + ext;
        }

        for (PageComponent component : components.values()) {
            String name = component.getComponentName() + ext;

            if (component.getClass().getResource(name) != null) {
                names.add(component.getComponentPath() + name);
            }
        }

        return names;
    }

    private interface Runnable {
        void run() throws IOException;
    }
}
