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

import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;
import static org.springframework.web.context.support.WebApplicationContextUtils.*;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.context.WebApplicationContext;

import com.alibaba.citrus.util.internal.regex.PathNameWildcardCompiler;
import com.alibaba.citrus.webx.WebxComponents;
import com.alibaba.citrus.webx.context.WebxComponentsContext;

/**
 * 初始化spring容器的filter。
 * 
 * @author Michael Zhou
 */
public class WebxFrameworkFilter extends FilterBean {
    private String parentContextAttribute;
    private WebxComponents components;
    private Pattern[] excludes;

    /**
     * 用于在servletContext中保存parent context的attribute key。
     */
    public final String getParentContextAttribute() {
        return parentContextAttribute;
    }

    /**
     * 设置用于在servletContext中保存parent context的attribute key。
     */
    public final void setParentContextAttribute(String parentContextAttribute) {
        this.parentContextAttribute = trimToNull(parentContextAttribute);
    }

    /**
     * 设置要排除掉的URL。
     */
    public void setExcludes(String excludes) {
        List<Pattern> excludePatterns = createLinkedList();

        for (String exclude : split(excludes, ", \r\n")) {
            exclude = trimToNull(exclude);

            if (exclude != null) {
                excludePatterns.add(PathNameWildcardCompiler.compilePathName(exclude));
            }
        }

        if (!excludePatterns.isEmpty()) {
            this.excludes = excludePatterns.toArray(new Pattern[excludePatterns.size()]);
        } else {
            this.excludes = null;
        }
    }

    /**
     * 取得所有components的信息。
     */
    public WebxComponents getWebxComponents() {
        return components;
    }

    /**
     * 初始化filter。
     */
    @Override
    protected final void init() throws Exception {
        WebApplicationContext parentContext = findParentContext();

        if (parentContext instanceof WebxComponentsContext) {
            components = ((WebxComponentsContext) parentContext).getWebxComponents();
        }
    }

    /**
     * 在<code>ServletContext</code>中查找parent context。
     * <ul>
     * <li>假如未指定<code>parentContextAttribute</code>，则查找默认的attribute key。</li>
     * <li>假如指定了init-param <code>parentContextAttribute</code>，则查找指定的attribute
     * key。假如没找到，则报错。</li>
     * </ul>
     */
    private WebApplicationContext findParentContext() {
        WebApplicationContext parentContext = null;
        String parentContextAttribute = getParentContextAttribute();

        if (parentContextAttribute == null) {
            parentContext = getWebApplicationContext(getServletContext());
        } else {
            parentContext = getWebApplicationContext(getServletContext(), parentContextAttribute);
            assertNotNull(parentContext, "No WebApplicationContext \"%s\" found: not registered?",
                    parentContextAttribute);
        }

        return parentContext;
    }

    protected void initFrameworkFilter() {
    }

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        // 如果指定了excludes，并且当前requestURI匹配任何一个exclude pattern，
        // 则立即放弃控制，将控制还给servlet engine。
        if (isExcluded(request)) {
            chain.doFilter(request, response);
            return;
        }

        try {
            getWebxComponents().getWebxRootController().service(request, response, chain);
        } catch (IOException e) {
            throw e;
        } catch (ServletException e) {
            throw e;
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    /**
     * 判断当前requestURI是否匹配任何一个exclude pattern。
     */
    private boolean isExcluded(HttpServletRequest request) {
        if (excludes != null) {
            String requestURI = request.getRequestURI();

            for (Pattern exclude : excludes) {
                if (exclude.matcher(requestURI).find()) {
                    return true;
                }
            }
        }

        return false;
    }
}
