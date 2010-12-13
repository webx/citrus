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
package com.alibaba.citrus.service.requestcontext.basic;

import java.io.IOException;

import org.junit.Test;

public class StatusMessageInterceptorTests extends AbstractBasicResponseTests {
    @Test
    @SuppressWarnings("deprecation")
    public void checkStatusMessage_null() throws IOException {
        createResponse(new StatusMessageInterceptor() {
            public String checkStatusMessage(int sc, String msg) {
                return msg;
            }
        });

        responseMock.sendError(500);
        responseMock.setStatus(404);

        replayMocks();

        response.sendError(500, null);
        response.setStatus(404, null);

        verifyMocks();
    }

    @Test
    @SuppressWarnings("deprecation")
    public void checkStatusMessage_ignoreMessage() throws IOException {
        createResponse(new StatusMessageInterceptor() {
            public String checkStatusMessage(int sc, String msg) {
                return null; // no message
            }
        });

        responseMock.sendError(500);
        responseMock.setStatus(404);

        replayMocks();

        response.sendError(500, "hello");
        response.setStatus(404, "hello");

        verifyMocks();
    }

    @Test
    @SuppressWarnings("deprecation")
    public void checkStatusMessage_keepUnchanged() throws IOException {
        createResponse(new StatusMessageInterceptor() {
            public String checkStatusMessage(int sc, String msg) {
                return msg;
            }
        });

        responseMock.sendError(500, "hello");
        responseMock.setStatus(404, "hello");

        replayMocks();

        response.sendError(500, "hello");
        response.setStatus(404, "hello");

        verifyMocks();
    }

    @Test
    @SuppressWarnings("deprecation")
    public void checkRedirectLocation_modifiy() throws IOException {
        createResponse(new StatusMessageInterceptor() {
            public String checkStatusMessage(int sc, String msg) {
                return sc + " Error: " + msg;
            }
        });

        responseMock.sendError(500, "500 Error: hello");
        responseMock.setStatus(404, "404 Error: hello");

        replayMocks();

        response.sendError(500, "hello");
        response.setStatus(404, "hello");

        verifyMocks();
    }
}
