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
package com.meterware.servletunit;

import static com.alibaba.citrus.test.TestUtil.*;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Dictionary;

import com.meterware.httpunit.FrameSelector;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;

public class PatchedInvocationContextImpl extends InvocationContextImpl {
    @SuppressWarnings("rawtypes")
    PatchedInvocationContextImpl(ServletUnitClient client, ServletRunner runner, FrameSelector frame,
                                 WebRequest request, Dictionary clientHeaders, byte[] messageBody) throws IOException,
            MalformedURLException {
        super(client, runner, frame, request, clientHeaders, messageBody);
    }

    @Override
    public WebResponse getServletResponse() throws IOException {
        try {
            Field _webResponse = getAccessibleField(getClass(), "_webResponse");
            boolean newWebResponse = _webResponse.get(this) == null;

            super.getServletResponse();

            if (newWebResponse) {
                _webResponse.set(this, new PatchedServletUnitWebResponse( //
                        getFieldValue(this, "_client", ServletUnitClient.class), //
                        getFieldValue(this, "_frame", FrameSelector.class), //
                        getFieldValue(this, "_effectiveURL", URL.class), //
                        getResponse(), getFieldValue(this, "_client", ServletUnitClient.class)
                                .getExceptionsThrownOnErrorStatus()));
            }

            return (WebResponse) _webResponse.get(this);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
