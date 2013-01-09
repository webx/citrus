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

import static com.alibaba.citrus.util.BasicConstant.*;
import static java.util.Collections.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Vector;
import javax.servlet.http.HttpServletRequest;

import org.easymock.Capture;
import org.easymock.IAnswer;
import org.junit.Test;

public class ActionEventUtilTests {
    private HttpServletRequest request;
    private Capture<String> cap = new Capture<String>();

    @Test
    public void getEventName_null() {
        assertEventName(null);
    }

    @Test
    public void getEventName_submit() {
        assertEventName("update", "eventSubmitDoUpdate", "yes");

        // no value
        assertEventName(null, "eventSubmitDoUpdate", "");
        assertEventName(null, "eventSubmitDoUpdate", "  ");
    }

    @Test
    public void getEventName_imageButton() {
        assertEventName("update", "eventSubmitDoUpdate.x", "yes");
        assertEventName("update", "eventSubmitDoUpdate.y", "yes");
        assertEventName("update", "eventSubmitDoUpdate.X", "yes");
        assertEventName("update", "eventSubmitDoUpdate.Y", "yes");
    }

    @Test
    public void getEventName_case() {
        assertEventName("deleteAll", "event_Submit_do_Delete_all", "yes");
        assertEventName("deleteAll", "eventSubmit_do_DeleteAll", "yes");
        assertEventName("deleteAll", "eventSubmit_doDeleteAll", "yes");
        assertEventName("deleteAll", "EVENTSubmit_DODeleteAll", "yes");

        // 不能识别的key
        assertEventName(null, "eventSubmit_dodeleteAll", "yes");
    }

    @Test
    public void setEventName() {
        request = createMock(HttpServletRequest.class);

        // set value
        request.setAttribute(ActionEventUtil.ACTION_EVENT_KEY, "newEvent");
        expectLastCall().once();
        replay(request);

        ActionEventUtil.setEventName(request, "newEvent");
        verify(request);

        // set null
        reset(request);
        request.setAttribute(ActionEventUtil.ACTION_EVENT_KEY, NULL_PLACEHOLDER);
        expectLastCall().once();
        replay(request);

        ActionEventUtil.setEventName(request, null);
        verify(request);
    }

    private void assertEventName(String event, String... values) {
        initRequest(true, values);
        assertEquals(event, ActionEventUtil.getEventName(request));

        if (event == null) {
            assertSame(NULL_PLACEHOLDER, cap.getValue());
        } else {
            assertEquals(event, cap.getValue());
        }

        verify(request);

        initRequest(false);
        assertEquals(event, ActionEventUtil.getEventName(request));
        verify(request);
    }

    private void initRequest(boolean set, String... values) {
        request = createMock(HttpServletRequest.class);

        if (set) {
            cap.setValue(null);

            final Vector<String> keys = new Vector<String>();

            if (values != null) {
                for (int i = 0; i < values.length; i += 2) {
                    String key = values[i];
                    String value = values[i + 1];

                    keys.add(key);
                    expect(request.getParameter(key)).andReturn(value).anyTimes();
                }
            }

            request.getParameterNames();
            expectLastCall().andAnswer(new IAnswer<Enumeration<?>>() {
                public Enumeration<?> answer() throws Throwable {
                    return keys.elements();
                }
            }).once();

            request.setAttribute(eq(ActionEventUtil.ACTION_EVENT_KEY), capture(cap));
            expectLastCall().once();
        }

        expect(request.getAttribute(ActionEventUtil.ACTION_EVENT_KEY)).andReturn(cap.getValue()).once();

        replay(request);
    }
}
