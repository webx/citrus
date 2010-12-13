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
package com.alibaba.citrus.webx.util;

import static com.alibaba.citrus.util.Assert.*;

import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.citrus.util.ClassLoaderUtil;
import com.alibaba.citrus.util.StringUtil;
import com.alibaba.citrus.webx.WebxComponent;

public class WebxUtil {
    private final static String CURRENT_WEBX_COMPONENT_KEY = "webx.component";
    private final static String REVISION = StringUtil.trim("$Revision: 16258 $", " $");

    public static WebxComponent getCurrentComponent(HttpServletRequest request) {
        return assertNotNull((WebxComponent) request.getAttribute(CURRENT_WEBX_COMPONENT_KEY),
                "No WebxComponent bound in request.  "
                        + "Make sure WebxFrameworkFilter run or set WebxComponent explicitly by calling WebxUtil");
    }

    public static void setCurrentComponent(HttpServletRequest request, WebxComponent component) {
        if (component == null) {
            request.removeAttribute(CURRENT_WEBX_COMPONENT_KEY);
        } else {
            request.setAttribute(CURRENT_WEBX_COMPONENT_KEY, component);
        }
    }

    private final static Pattern versionPattern = Pattern.compile("webx[^/\\\\]*-(\\d+[^!/]+)\\.\\w+",
            Pattern.CASE_INSENSITIVE);

    /**
     * È¡µÃwebx version¡£
     */
    public static String getWebxVersion() {
        URL url = ClassLoaderUtil.whichClass(WebxUtil.class.getName(), WebxUtil.class);
        return getVersion(url);
    }

    public static String getRevision() {
        return REVISION;
    }

    private static String getVersion(URL url) {
        String version = REVISION;

        if (url != null) {
            String name = url.getFile();
            Matcher m = versionPattern.matcher(name);

            if (m.find()) {
                version = m.group(1);
            }
        }

        return version;
    }
}
