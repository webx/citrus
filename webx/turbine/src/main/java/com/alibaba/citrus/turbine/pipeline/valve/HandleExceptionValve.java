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
package com.alibaba.citrus.turbine.pipeline.valve;

import static com.alibaba.citrus.springext.util.DomUtil.*;
import static com.alibaba.citrus.springext.util.SpringExtUtil.*;
import static com.alibaba.citrus.turbine.util.TurbineUtil.*;
import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.ExceptionUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.alibaba.citrus.service.pipeline.PipelineContext;
import com.alibaba.citrus.service.pipeline.support.AbstractValve;
import com.alibaba.citrus.springext.support.parser.AbstractSingleBeanDefinitionParser;
import com.alibaba.citrus.turbine.TurbineRunDataInternal;
import com.alibaba.citrus.webx.util.ErrorHandlerHelper;

/**
 * 在<code>&lt;pipeline id=&quot;exceptionPipeline&quot;&gt;</code>中，处理异常的valve。
 * 
 * @author Michael Zhou
 */
public class HandleExceptionValve extends AbstractValve {
    private static final String HELPER_NAME_DEFAULT = "error";
    private final HttpServletRequest request;
    private String defaultPage;
    private ExceptionHandler[] exceptionHandlers;
    private String helperName;

    public HandleExceptionValve(HttpServletRequest request) {
        this.request = assertNotNull(assertProxy(request), "no request");
    }

    public void setDefaultPage(String defaultPage) {
        this.defaultPage = trimToNull(defaultPage);
    }

    public void setExceptionHandlers(ExceptionHandler[] exceptionHandlers) {
        this.exceptionHandlers = exceptionHandlers;
    }

    public void setHelperName(String helperName) {
        this.helperName = trimToNull(helperName);
    }

    @Override
    protected void init() throws Exception {
        assertNotNull(defaultPage, "no defaultPage");

        if (exceptionHandlers == null) {
            exceptionHandlers = new ExceptionHandler[0];
        }

        // 按exception排序，将子类排在前
        exceptionHandlers = sortExceptions(exceptionHandlers);

        if (helperName == null) {
            helperName = HELPER_NAME_DEFAULT;
        }
    }

    private ExceptionHandler[] sortExceptions(ExceptionHandler[] handlers) {
        Set<ExceptionHandler> visited = createHashSet();
        List<ExceptionHandler> sorted = createLinkedList();

        for (ExceptionHandler handler : handlers) {
            visitException(handler, handlers, visited, sorted);
        }

        return sorted.toArray(new ExceptionHandler[sorted.size()]);
    }

    private void visitException(ExceptionHandler handler, ExceptionHandler[] handlers, Set<ExceptionHandler> visited,
                                List<ExceptionHandler> sorted) {
        if (visited.contains(handler)) {
            return;
        }

        visited.add(handler);

        for (ExceptionHandler test : handlers) {
            if (handler.getExceptionType().isAssignableFrom(test.getExceptionType())) {
                visitException(test, handlers, visited, sorted);
            }
        }

        sorted.add(handler);
    }

    public void invoke(PipelineContext pipelineContext) throws Exception {
        TurbineRunDataInternal rundata = (TurbineRunDataInternal) getTurbineRunData(request);
        ErrorHandlerHelper helper = ErrorHandlerHelper.getInstance(request);
        Throwable exception = helper.getException();

        // 模板中可用的helper
        rundata.getContext().put(helperName, helper);

        if (exception != null) {
            int statusCode = -1;
            String target = null;

            // 从最根本的exception cause开始反向追溯，例如：t1 caused by t2 caused by t3，
            // 那么，检查顺序为t3, t2, t1。
            CAUSES: for (Throwable cause : getCauses(exception, true)) {
                // 对于每个异常，查找匹配的exception handlers。
                // 所有handlers已经排序，较特殊的异常在前，假设T1 extends T2，那么T1在T2之前。
                for (ExceptionHandler exceptionHandler : exceptionHandlers) {
                    if (exceptionHandler.getExceptionType().isInstance(cause)) {
                        statusCode = exceptionHandler.getStatusCode();
                        target = exceptionHandler.getPage();
                        break CAUSES;
                    }
                }
            }

            if (statusCode > 0) {
                rundata.getResponse().setStatus(statusCode);
                helper.setStatusCode(statusCode); // 更新request attributes
            }

            if (target == null) {
                target = defaultPage;
            }

            rundata.setTarget(target);
        }

        // 执行下一个Valve
        pipelineContext.invokeNext();
    }

    public static class ExceptionHandler {
        private final Class<? extends Throwable> exceptionType;
        private final int statusCode;
        private final String page;

        public ExceptionHandler(Class<? extends Throwable> exceptionType, int statusCode, String page) {
            this.exceptionType = assertNotNull(exceptionType, "no exception type");
            this.statusCode = statusCode <= 0 ? -1 : statusCode;
            this.page = trimToNull(page);
        }

        public Class<? extends Throwable> getExceptionType() {
            return exceptionType;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public String getPage() {
            return page;
        }

        @Override
        public String toString() {
            StringBuilder buf = new StringBuilder();

            buf.append("on ").append(exceptionType.getName());

            if (page != null) {
                buf.append(" go ").append(page);
            }

            if (statusCode > 0) {
                buf.append(" status code is ").append(statusCode);
            }

            return buf.toString();
        }
    }

    public static class DefinitionParser extends AbstractSingleBeanDefinitionParser<HandleExceptionValve> {
        @Override
        protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
            attributesToProperties(element, builder, "defaultPage", "helperName");

            addConstructorArg(builder, true, HttpServletRequest.class);

            List<Object> exceptionHandlers = createManagedList(element, parserContext);

            for (Element onExceptionElement : subElements(element, and(sameNs(element), name("on-exception")))) {
                exceptionHandlers.add(doParseOnException(onExceptionElement, parserContext, builder));
            }

            builder.addPropertyValue("exceptionHandlers", exceptionHandlers);
        }

        private Object doParseOnException(Element onExceptionElement, ParserContext parserContext,
                                          BeanDefinitionBuilder builder) {
            BeanDefinitionBuilder onExceptionBuilder = BeanDefinitionBuilder
                    .genericBeanDefinition(ExceptionHandler.class);

            onExceptionBuilder.addConstructorArgValue(onExceptionElement.getAttribute("type"));

            if (onExceptionElement.hasAttribute("statusCode")) {
                onExceptionBuilder.addConstructorArgValue(onExceptionElement.getAttribute("statusCode"));
            } else {
                onExceptionBuilder.addConstructorArgValue("-1");
            }

            onExceptionBuilder.addConstructorArgValue(onExceptionElement.getAttribute("page"));

            return onExceptionBuilder.getBeanDefinition();
        }
    }
}
