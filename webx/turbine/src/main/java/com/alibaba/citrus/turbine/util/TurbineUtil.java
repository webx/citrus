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
package com.alibaba.citrus.turbine.util;

import static com.alibaba.citrus.util.Assert.*;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.citrus.service.requestcontext.RequestContext;
import com.alibaba.citrus.turbine.TurbineRunData;
import com.alibaba.citrus.turbine.support.TurbineRunDataImpl;

public class TurbineUtil {
    private final static String TURBINE_RUNDATA_KEY = "_webx3_turbine_rundata_";

    /**
     * 从request中取得rundata，假如不存在，则创建之。
     */
    public static TurbineRunData getTurbineRunData(HttpServletRequest request) {
        return getTurbineRunData(request, false);
    }

    public static TurbineRunData getTurbineRunData(HttpServletRequest request, boolean create) {
        TurbineRunData rundata = (TurbineRunData) request.getAttribute(TURBINE_RUNDATA_KEY);

        if (rundata == null && create) {
            rundata = new TurbineRunDataImpl(request);
            request.setAttribute(TURBINE_RUNDATA_KEY, rundata);
        }

        return assertNotNull(rundata, "TurbineRunData not found in request attributes");
    }

    /**
     * 从requestContext中取得rundata。
     */
    public static TurbineRunData getTurbineRunData(RequestContext requestContext) {
        return getTurbineRunData(requestContext.getRequest());
    }

    /**
     * 从request中清除turbine rundata。
     */
    public static void cleanupTurbineRunData(HttpServletRequest request) {
        request.removeAttribute(TURBINE_RUNDATA_KEY);
    }
}
