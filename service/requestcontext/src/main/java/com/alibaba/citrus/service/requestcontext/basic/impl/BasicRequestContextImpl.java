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
package com.alibaba.citrus.service.requestcontext.basic.impl;

import static com.alibaba.citrus.util.ArrayUtil.*;

import com.alibaba.citrus.service.requestcontext.RequestContext;
import com.alibaba.citrus.service.requestcontext.RequestContextException;
import com.alibaba.citrus.service.requestcontext.basic.BasicRequestContext;
import com.alibaba.citrus.service.requestcontext.support.AbstractRequestContextWrapper;

public class BasicRequestContextImpl extends AbstractRequestContextWrapper implements BasicRequestContext {
    private Object[] interceptors;

    public BasicRequestContextImpl(RequestContext wrappedContext, Object[] interceptors) {
        super(wrappedContext);

        this.interceptors = addDefaultInterceptors(interceptors);
        setResponse(new BasicResponseImpl(this, wrappedContext.getResponse(), this.interceptors));
    }

    public Object[] getResponseHeaderInterceptors() {
        return interceptors.clone();
    }

    @Override
    public void prepare() {
        ((BasicResponseImpl) getResponse()).prepareResponse();
    }

    @Override
    public void commit() throws RequestContextException {
        ((BasicResponseImpl) getResponse()).commitResponse();
    }

    private Object[] addDefaultInterceptors(Object[] interceptors) {
        if (isEmptyArray(interceptors)) {
            return new Object[] { new ResponseHeaderSecurityFilter() };
        }

        for (Object interceptor : interceptors) {
            if (interceptor instanceof ResponseHeaderSecurityFilter) {
                return interceptors;
            }
        }

        // appending response-header-security-filter
        Object[] newInterceptors = new Object[interceptors.length + 1];
        System.arraycopy(interceptors, 0, newInterceptors, 0, interceptors.length);
        newInterceptors[interceptors.length] = new ResponseHeaderSecurityFilter();

        return newInterceptors;
    }
}
