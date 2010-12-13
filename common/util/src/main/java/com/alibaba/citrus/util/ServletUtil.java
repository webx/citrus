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
package com.alibaba.citrus.util;

import static com.alibaba.citrus.util.FileUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;

/**
 * 有关servlet的小工具。
 * 
 * @author Michael Zhou
 */
public class ServletUtil {
    /**
     * 判断servlet是否为前缀映射。
     * <p>
     * Servlet mapping有两种匹配方式：前缀匹配和后缀匹配。
     * </p>
     * <ul>
     * <li>对于前缀匹配，例如：/turbine/aaa/bbb，servlet path为/turbine，path info为/aaa/bbb</li>
     * <li>对于后缀匹配，例如：/aaa/bbb.html，servlet path为/aaa/bbb.html，path info为null</li>
     * </ul>
     */
    public static boolean isPrefixServletMapping(HttpServletRequest request) {
        String pathInfo = trimToNull(request.getPathInfo());

        if (pathInfo != null) {
            return true;
        } else {
            // 特殊情况：前缀映射/turbine，requestURI=/turbine
            // 此时，pathInfo也是null，但其实是前缀匹配。
            // 这种情况可以通过查看servletPath是否有后缀来部分地识别。
            String servletPath = trimToEmpty(request.getServletPath());
            int index = servletPath.lastIndexOf("/");

            if (servletPath.indexOf(".", index + 1) >= 0) {
                return false;
            } else {
                return true;
            }
        }
    }

    /**
     * 取得request所请求的资源路径。
     * <p>
     * 资源路径为<code>getServletPath() + getPathInfo()</code>。
     * </p>
     * <p>
     * 注意，<code>ResourcePath</code>以<code>"/"</code>开始，如果无内容，则返回空字符串
     * <code>""</code>。
     * </p>
     */
    public static String getResourcePath(HttpServletRequest request) {
        String pathInfo = normalizeAbsolutePath(request.getPathInfo(), false);
        String servletPath = normalizeAbsolutePath(request.getServletPath(), pathInfo.length() != 0);

        return servletPath + pathInfo;
    }

    /**
     * 取得request请求的基准URL。
     * <p>
     * 基准URL等同于<code>SERVER/contextPath</code>。
     * </p>
     * <p>
     * 基准URL总是<strong>不</strong>以<code>"/"</code>结尾。
     * </p>
     * <p>
     * 以下等式总是成立：<code>fullURL = baseURL + resourcePath</code>。
     * </p>
     */
    public static String getBaseURL(HttpServletRequest request) {
        String fullURL = request.getRequestURL().toString();
        String fullPath;

        try {
            fullPath = new URL(fullURL).getPath();
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid URL: " + fullURL, e);
        }

        // 基本URL
        StringBuilder buf = new StringBuilder(fullURL);
        buf.setLength(fullURL.length() - fullPath.length());

        // 加上contextPath
        buf.append(normalizeAbsolutePath(request.getContextPath(), true));

        return buf.toString();
    }

    /**
     * 取得request所请求的资源路径。
     * <ul>
     * <li>对于前缀匹配的servlet，等同于<code>getPathInfo()</code>。例如映射
     * <code>/turbine/*</code>：<code>/turbine/xx/yy</code>的resource path为
     * <code>/xx/yy</code>。</li>
     * <li>对于后缀匹配的servlet，等同于 <code>getServletPath()</code>。例如映射
     * <code>*.do</code>：<code>/xx/yy.do</code>的resource path为
     * <code>/xx/yy.do</code>。</li>
     * </ul>
     * <p>
     * 注意，<code>ResourcePath</code>以<code>"/"</code>开始，如果无内容，则返回空字符串
     * <code>""</code>。
     * </p>
     * <p>
     * 本方法适用于servlet-mapping对应的URL。
     * </p>
     */
    public static String getServletResourcePath(HttpServletRequest request) {
        String resourcePath;

        if (isPrefixServletMapping(request)) {
            resourcePath = request.getPathInfo();
        } else {
            resourcePath = request.getServletPath();
        }

        resourcePath = normalizeAbsolutePath(resourcePath, false);

        return resourcePath;
    }

    /**
     * 取得request请求的基准URL。
     * <ul>
     * <li>对于前缀匹配的servlet，等同于<code>SERVER/contextPath/servletPath</code>。例如映射
     * <code>/turbine/*</code>：<code>http://localhost/myapp/turbine/xx/yy</code>
     * 的baseURL为 <code>http://localhost/myapp/turbine</code>。</li>
     * <li>对于后缀匹配的servlet，等同于<code>SERVER/contextPath</code>。例如映射
     * <code>*.do</code>：<code>http://localhost/myapp/xx/yy.do</code>的baseURL为
     * <code>http://localhost/myapp</code>。</li>
     * </ul>
     * <p>
     * 基准URL总是<strong>不</strong>以<code>"/"</code>结尾。
     * </p>
     * <p>
     * 以下等式总是成立：<code>fullURL = servletBaseURL + servletResourcePath</code>。
     * </p>
     * <p>
     * 本方法适用于servlet-mapping对应的URL。
     * </p>
     */
    public static String getServletBaseURL(HttpServletRequest request) {
        String fullURL = request.getRequestURL().toString();
        String fullPath;

        try {
            fullPath = new URL(fullURL).getPath();
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid URL: " + fullURL, e);
        }

        // 基本URL
        StringBuilder buf = new StringBuilder(fullURL);
        buf.setLength(fullURL.length() - fullPath.length());

        // 加上contextPath
        buf.append(normalizeAbsolutePath(request.getContextPath(), true));

        // 对于前缀匹配，加上servletPath
        if (isPrefixServletMapping(request)) {
            buf.append(normalizeAbsolutePath(request.getServletPath(), true));
        }

        return buf.toString();
    }

    /**
     * 规格化URI。
     */
    public static String normalizeURI(String uri) {
        return URI.create(trimToEmpty(uri)).normalize().toString();
    }
}
