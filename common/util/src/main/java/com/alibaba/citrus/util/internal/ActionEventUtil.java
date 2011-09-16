package com.alibaba.citrus.util.internal;

import static com.alibaba.citrus.util.BasicConstant.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

public class ActionEventUtil {
    private static final String EVENT_PATTERN = "eventSubmitDo";
    private static final int EVENT_PATTERN_LENGTH = EVENT_PATTERN.length();
    private static final String IMAGE_BUTTON_SUFFIX_1 = ".x";
    private static final String IMAGE_BUTTON_SUFFIX_2 = ".y";
    static final String ACTION_EVENT_KEY = "_action_event_submit_do_";

    /**
     * 取得key=eventSubmit_doXyz, value不为空的参数。
     */
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

        if (event == null) {
            savedEvent = NULL_PLACEHOLDER;
        } else {
            savedEvent = event;
        }

        request.setAttribute(ACTION_EVENT_KEY, savedEvent);

        return event;
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
