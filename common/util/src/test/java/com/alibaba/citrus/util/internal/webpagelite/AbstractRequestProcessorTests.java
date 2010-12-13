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

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.Before;

public abstract class AbstractRequestProcessorTests {
    protected String contentType;
    protected ByteArrayOutputStream baos;
    protected StringWriter sw;
    protected String redirectUrl;
    protected String resourceNotFound;

    @Before
    public void initStream() {
        baos = new ByteArrayOutputStream();
        sw = new StringWriter();
    }

    protected class MyRequest extends RequestContext {
        public MyRequest(String baseURL, String resourceName) {
            super(baseURL, resourceName);
        }

        @Override
        protected OutputStream doGetOutputStream(String contentType) throws IOException {
            AbstractRequestProcessorTests.this.contentType = contentType;
            return new BufferedOutputStream(baos);
        }

        @Override
        protected PrintWriter doGetWriter(String contentType) throws IOException {
            AbstractRequestProcessorTests.this.contentType = contentType;
            return new PrintWriter(sw, true);
        }

        @Override
        public void redirectTo(String url) throws IOException {
            redirectUrl = url;
        }

        @Override
        public void resourceNotFound(String resourceName) throws IOException {
            resourceNotFound = resourceName;
        }
    }
}
