/*
 * Copyright (c) 2002-2012 Alibaba Group Holding Limited.
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
 */

package com.alibaba.citrus.util.internal;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;

public class ScreenEventUtilTests {
    private HttpServletRequest request;

    @Before
    public void init() {
        request = createMock(HttpServletRequest.class);
    }

    @Test
    public void getEventName() {
        assertGetEventName(null, new Object());
        assertGetEventName(null, null);
        assertGetEventName("testevent", "testevent");
    }

    private void assertGetEventName(String expectedValue, Object attr) {
        reset(request);
        expect(request.getAttribute(ScreenEventUtil.SCREEN_EVENT_KEY)).andReturn(attr).anyTimes();
        replay(request);

        assertEquals(expectedValue, ScreenEventUtil.getEventName(request));
    }

    @Test
    public void setEventName() {
        assertSetEventName(null, null);
        assertSetEventName(null, " ");
        assertSetEventName(null, "");

        assertSetEventName("aaa", " aaa ");
    }

    private void assertSetEventName(String expectedValue, String event) {
        reset(request);

        if (expectedValue == null) {
            request.removeAttribute(ScreenEventUtil.SCREEN_EVENT_KEY);
        } else {
            request.setAttribute(ScreenEventUtil.SCREEN_EVENT_KEY, expectedValue);
        }

        expectLastCall().once();
        replay(request);

        ScreenEventUtil.setEventName(request, event);
        verify(request);
    }
}
