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

import static com.alibaba.citrus.util.StringUtil.*;

import javax.servlet.http.HttpServletRequest;

public class ScreenEventUtil {
    static final String SCREEN_EVENT_KEY = "_screen_event_";

    /** 取得event名称。 */
    public static String getEventName(HttpServletRequest request) {
        Object savedEvent = request.getAttribute(SCREEN_EVENT_KEY);

        if (savedEvent instanceof String) {
            return (String) savedEvent;
        }

        return null;
    }

    /** 将event名称保存到request中，以便下次取用。 */
    public static void setEventName(HttpServletRequest request, String event) {
        event = trimToNull(event);

        if (event == null) {
            request.removeAttribute(SCREEN_EVENT_KEY);
        } else {
            request.setAttribute(SCREEN_EVENT_KEY, event);
        }
    }
}
