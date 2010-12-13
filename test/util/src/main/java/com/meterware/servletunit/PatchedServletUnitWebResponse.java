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
import java.net.URL;

import javax.servlet.http.HttpServletResponse;

import com.meterware.httpunit.FrameSelector;
import com.meterware.httpunit.cookies.PatchedCookieJar;

public class PatchedServletUnitWebResponse extends ServletUnitWebResponse {
    public PatchedServletUnitWebResponse(ServletUnitClient client, FrameSelector frame, URL url,
                                         HttpServletResponse response, boolean throwExceptionOnError)
            throws IOException {
        super(client, frame, url, response, throwExceptionOnError);
        setCookieJar();
    }

    public PatchedServletUnitWebResponse(ServletUnitClient client, FrameSelector frame, URL url,
                                         HttpServletResponse response) throws IOException {
        super(client, frame, url, response);
        setCookieJar();
    }

    void setCookieJar() {
        try {
            Field _cookies = getAccessibleField(getClass(), "_cookies");

            if (_cookies.get(this) == null) {
                _cookies.set(this, new PatchedCookieJar(this));
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
