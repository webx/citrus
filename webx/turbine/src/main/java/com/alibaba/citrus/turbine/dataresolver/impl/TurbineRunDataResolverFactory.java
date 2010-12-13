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
package com.alibaba.citrus.turbine.dataresolver.impl;

import static com.alibaba.citrus.springext.util.SpringExtUtil.*;
import static com.alibaba.citrus.util.Assert.*;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.alibaba.citrus.service.dataresolver.DataResolver;
import com.alibaba.citrus.service.dataresolver.DataResolverContext;
import com.alibaba.citrus.service.dataresolver.DataResolverFactory;
import com.alibaba.citrus.service.moduleloader.ModuleInfo;
import com.alibaba.citrus.service.requestcontext.RequestContext;
import com.alibaba.citrus.service.requestcontext.parser.CookieParser;
import com.alibaba.citrus.service.requestcontext.parser.ParameterParser;
import com.alibaba.citrus.service.requestcontext.util.RequestContextUtil;
import com.alibaba.citrus.springext.support.parser.AbstractSingleBeanDefinitionParser;
import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.TurbineRunDataInternal;
import com.alibaba.citrus.turbine.util.TurbineUtil;

/**
 * 取得和<code>TurbineRunData</code>相关的对象。
 * <ul>
 * <li>TurbineRunData</li>
 * <li>HttpServletRequest</li>
 * <li>HttpServletResponse</li>
 * <li>HttpSession</li>
 * <li>ServletContext</li>
 * <li>ParameterParser</li>
 * <li>CookieParser</li>
 * <li>Context</li>
 * <li>RequestContext及其子类</li>
 * </ul>
 * 
 * @author Michael Zhou
 */
public class TurbineRunDataResolverFactory implements DataResolverFactory {
    private final static int index_TurbineRunData = 0;
    private final static int index_HttpServletRequest = 1;
    private final static int index_HttpServletResponse = 2;
    private final static int index_HttpSession = 3;
    private final static int index_ServletContext = 4;
    private final static int index_Parameters = 5;
    private final static int index_Cookies = 6;
    private final static int index_Context = 7;
    private final static int index_RequestContext = 8;

    private final HttpServletRequest request;

    public TurbineRunDataResolverFactory(HttpServletRequest request) {
        this.request = assertProxy(request);
    }

    private int getResolvableTypeIndex(DataResolverContext context) {
        Class<?> type = context.getTypeInfo().getRawType();

        // 排除object
        if (type.equals(Object.class)) {
            return -1;
        }

        if (type.isAssignableFrom(TurbineRunDataInternal.class)) {
            return index_TurbineRunData;
        }

        if (type.isAssignableFrom(HttpServletRequest.class)) {
            return index_HttpServletRequest;
        }

        if (type.isAssignableFrom(HttpServletResponse.class)) {
            return index_HttpServletResponse;
        }

        if (type.isAssignableFrom(HttpSession.class)) {
            return index_HttpSession;
        }

        if (type.isAssignableFrom(ServletContext.class)) {
            return index_ServletContext;
        }

        if (type.isAssignableFrom(ParameterParser.class)) {
            return index_Parameters;
        }

        if (type.isAssignableFrom(CookieParser.class)) {
            return index_Cookies;
        }

        if (type.isAssignableFrom(Context.class)) {
            return index_Context;
        }

        // type是RequestContext的子类
        if (RequestContext.class.isAssignableFrom(type)) {
            return index_RequestContext;
        }

        return -1;
    }

    public DataResolver getDataResolver(DataResolverContext context) {
        // 当所需要的对象未定义时，resolver factory仍可以创建，但在取得resolver时报错。
        // 这样使得同一套配置可用于所有环境，仅当你需要注入特定对象时，才报错。
        assertNotNull(request, "no HttpServletRequest proxy defined");

        int resolvableTypeIndex = getResolvableTypeIndex(context);

        if (resolvableTypeIndex < 0) {
            return null;
        }

        return new TurbineRunDataResolver(context, resolvableTypeIndex);
    }

    protected final TurbineRunDataInternal getTurbineRunData() {
        return (TurbineRunDataInternal) TurbineUtil.getTurbineRunData(request);
    }

    protected final String getModuleType(DataResolverContext context) {
        ModuleInfo moduleInfo = context.getExtraObject(ModuleInfo.class);

        if (moduleInfo != null) {
            return moduleInfo.getType();
        } else {
            return null;
        }
    }

    private class TurbineRunDataResolver extends AbstractDataResolver {
        private final int resolvableTypeIndex;

        private TurbineRunDataResolver(DataResolverContext context, int resolvableTypeIndex) {
            super("TurbineRunDataResolver", context);
            this.resolvableTypeIndex = resolvableTypeIndex;
        }

        public Object resolve() {
            switch (resolvableTypeIndex) {
                case index_TurbineRunData:
                    // 取得TurbineRunData/Navigator
                    return getTurbineRunData();

                case index_HttpServletRequest:
                    // 取得当前的request
                    return getTurbineRunData().getRequest();

                case index_HttpServletResponse:
                    // 取得当前的response
                    return getTurbineRunData().getResponse();

                case index_HttpSession:
                    // 取得当前的session
                    return getTurbineRunData().getRequest().getSession();

                case index_ServletContext:
                    // 取得当前的servlet容器
                    return getTurbineRunData().getRequestContext().getServletContext();

                case index_Parameters:
                    // 取得当前请求的参数集
                    return getTurbineRunData().getParameters();

                case index_Cookies:
                    // 取得当前请求的cookie集
                    return getTurbineRunData().getCookies();

                case index_Context:
                    // 取得当前的context，
                    // 对于control module，返回control context，否则返回全局context。
                    String moduleType = getModuleType(context);

                    if ("control".equalsIgnoreCase(moduleType)) {
                        return getTurbineRunData().getContextForControl();
                    } else {
                        return getTurbineRunData().getContext();
                    }

                case index_RequestContext:
                    // 取得参数类型所定义的RequestContext，假如不存在，则返回null
                    @SuppressWarnings("unchecked")
                    Class<? extends RequestContext> requestContextType = (Class<? extends RequestContext>) context
                            .getTypeInfo().getRawType();

                    return RequestContextUtil.findRequestContext(getTurbineRunData().getRequestContext(),
                            requestContextType);

                default:
                    unreachableCode();
                    return null;
            }
        }
    }

    public static class DefinitionParser extends AbstractSingleBeanDefinitionParser<TurbineRunDataResolverFactory> {
        @Override
        protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
            addConstructorArg(builder, false, HttpServletRequest.class);
        }
    }
}
