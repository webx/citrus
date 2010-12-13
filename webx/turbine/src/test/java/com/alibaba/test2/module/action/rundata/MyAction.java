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
package com.alibaba.test2.module.action.rundata;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.citrus.service.requestcontext.RequestContext;
import com.alibaba.citrus.service.requestcontext.parser.CookieParser;
import com.alibaba.citrus.service.requestcontext.parser.ParameterParser;
import com.alibaba.citrus.service.requestcontext.parser.ParserRequestContext;
import com.alibaba.citrus.service.requestcontext.rundata.RunData;
import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.Navigator;
import com.alibaba.citrus.turbine.TurbineRunData;

public class MyAction {
    @Autowired
    private HttpServletRequest request;

    public void doGetRundata(TurbineRunData rundata) {
        setAttribute(rundata);
    }

    public void doGetNavigator(Navigator nav) {
        setAttribute(nav);
    }

    public void doGetRequest(ServletRequest request) {
        setAttribute(request);
    }

    public void doGetResponse(ServletResponse response) {
        setAttribute(response);
    }

    public void doGetSession(HttpSession session) {
        setAttribute(session);
    }

    public void doGetServletContext(ServletContext servletContext) {
        setAttribute(servletContext);
    }

    public void doGetParameters(ParameterParser params) {
        setAttribute(params);
    }

    public void doGetCookies(CookieParser cookies) {
        setAttribute(cookies);
    }

    public void doGetContext(Context context) {
        setAttribute(context);
    }

    public void doGetRequestContext(RequestContext requestContext) {
        setAttribute(requestContext);
    }

    public void doGetRequestContext2(RunData requestContext) {
        setAttribute(requestContext);
    }

    public void doGetRequestContext3(ParserRequestContext requestContext) {
        setAttribute(requestContext);
    }

    private void setAttribute(Object data) {
        request.setAttribute("actionLog", data);
    }
}
