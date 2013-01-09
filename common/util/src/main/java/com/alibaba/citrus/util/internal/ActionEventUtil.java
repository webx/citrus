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
import static com.alibaba.citrus.util.ObjectUtil.defaultIfNull;
import static com.alibaba.citrus.util.StringUtil.*;

import java.util.Enumeration;
import javax.servlet.http.HttpServletRequest;

public class ActionEventUtil {
    private static final String EVENT_PATTERN         = "eventSubmitDo";
    private static final int    EVENT_PATTERN_LENGTH  = EVENT_PATTERN.length();
    private static final String IMAGE_BUTTON_SUFFIX_1 = ".x";
    private static final String IMAGE_BUTTON_SUFFIX_2 = ".y";
    static final         String ACTION_EVENT_KEY      = "_action_event_submit_do_";

    /** 取得key=eventSubmit_doXyz, value不为空的参数。 */
    public static String getEventName(HttpServletRequest request) {
        Object savedEvent = request.getAttribute(ACTION_EVENT_KEY);

        if (savedEvent != null) {
            if (NULL_PLACEHOLDER == savedEvent) {
                return null;
            } else if (savedEvent instanceof String) {
                return (String) savedEvent;
            }
        }

        String event = doGetEventName(request);
        setEventName(request, event);

        return event;
    }

    public static void setEventName(HttpServletRequest request, String eventName) {
        Object savedEvent = trimToNull(eventName);

        if (savedEvent == null) {
            savedEvent = NULL_PLACEHOLDER;
        }

        request.setAttribute(ACTION_EVENT_KEY, savedEvent);
    }

    private static String doGetEventName(HttpServletRequest request) {
        String event = null;

        @SuppressWarnings("unchecked")
        Enumeration<String> e = request.getParameterNames();

        while (e.hasMoreElements()) {
            String originalKey = e.nextElement();
            String paramKey = toCamelCase(originalKey);

            if (paramKey.length() > EVENT_PATTERN_LENGTH && paramKey.startsWith(EVENT_PATTERN)
                && Character.isUpperCase(paramKey.charAt(EVENT_PATTERN_LENGTH))
                && !isBlank(request.getParameter(originalKey))) {
                int startIndex = EVENT_PATTERN_LENGTH;
                int endIndex = paramKey.length();

                // 支持<input type="image">
                if (paramKey.endsWith(IMAGE_BUTTON_SUFFIX_1)) {
                    endIndex -= IMAGE_BUTTON_SUFFIX_1.length();
                } else if (paramKey.endsWith(IMAGE_BUTTON_SUFFIX_2)) {
                    endIndex -= IMAGE_BUTTON_SUFFIX_2.length();
                }

                event = uncapitalize(trimToNull(paramKey.substring(startIndex, endIndex)));

                if (event != null) {
                    break;
                }
            }
        }

        return event;
    }
}
