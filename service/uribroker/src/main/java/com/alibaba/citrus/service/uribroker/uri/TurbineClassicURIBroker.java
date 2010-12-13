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
package com.alibaba.citrus.service.uribroker.uri;

import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.util.Map;

/**
 * Turbine传统风格的URI.
 * <p>
 * 一个Turbine传统风格的URI包括如下几个部分：
 * </p>
 * 
 * <pre>
 * URI              = SERVER_INFO + PATH + "?" + QUERY_DATA + "#" + REFERENCE
 * SERVER_INFO      = scheme://loginUser:loginPassword@serverName:serverPort
 * PATH             = /contextPath/servletPath/PATH_INFO
 * PATH_INFO        = /template/templateName/screen/screenName/action/actionName/paramName/paramValue
 * QUERY_DATA       = queryKey1=value1&queryKey2=value2
 * REFERENCE        = reference
 * </pre>
 * <p>
 * 例如：
 * </p>
 * 
 * <pre>
 * http://user:pass@myserver.com:8080/mycontext/turbine/template/product,ViewItem?id=1#top
 * </pre>
 * 
 * @author Michael Zhou
 * @author dux.fangl
 */
public class TurbineClassicURIBroker extends ServletURIBroker {
    private static final int TEMPLATE_INDEX = PATH_INFO_INDEX;
    private static final int SCREEN_INDEX = PATH_INFO_INDEX + 1;
    private static final int ACTION_INDEX = PATH_INFO_INDEX + 2;
    private static final int PATH_INFO_PARAM_INDEX = PATH_INFO_INDEX + 3;
    private boolean hasTemplate;
    private boolean hasScreen;
    private boolean hasAction;

    /**
     * 取得page。
     */
    public String getPage() {
        if (hasTemplate) {
            return getPathSegment(TEMPLATE_INDEX).get(1);
        } else {
            return null;
        }
    }

    /**
     * 设置page。
     */
    public TurbineClassicURIBroker setPage(String page) {
        setPathInfo(TEMPLATE_INDEX, "template", page, false);
        hasTemplate = true;
        return this;
    }

    /**
     * 取得screen。
     */
    public String getScreen() {
        if (hasScreen) {
            return getPathSegment(SCREEN_INDEX).get(1);
        } else {
            return null;
        }
    }

    /**
     * 设置screen。
     */
    public TurbineClassicURIBroker setScreen(String screen) {
        setPathInfo(SCREEN_INDEX, "screen", screen, false);
        hasScreen = true;
        return this;
    }

    /**
     * 取得action。
     */
    public String getAction() {
        if (hasAction) {
            return getPathSegment(ACTION_INDEX).get(1);
        } else {
            return null;
        }
    }

    /**
     * 设置action。
     */
    public TurbineClassicURIBroker setAction(String action) {
        setPathInfo(ACTION_INDEX, "action", action, false);
        hasAction = true;
        return this;
    }

    /**
     * 添加pathInfo.
     */
    public TurbineClassicURIBroker addPathInfo(String id, Object value) {
        setPathInfo(PATH_INFO_PARAM_INDEX, id, value, true);
        return this;
    }

    /**
     * 添加一批pathInfo。
     */
    public void setPathInfoParams(Map<String, Object> values) {
        clearPathInfoParams();

        if (values != null) {
            for (Map.Entry<String, Object> entry : values.entrySet()) {
                addPathInfo(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * 删除所有的pathInfo。
     */
    public TurbineClassicURIBroker clearPathInfoParams() {
        clearPathSegment(PATH_INFO_PARAM_INDEX);
        return this;
    }

    private void setPathInfo(int index, String id, Object value, boolean add) {
        id = assertNotNull(trimToNull(id), "empty pathInfo id");

        // 空字符串转换成null
        String strValue = trimToNull(String.valueOf(value).replaceAll("/", ","));

        if (add) {
            addPathSegment(index, id);
        } else {
            setPathSegment(index, id);
        }

        addPathSegment(index, String.valueOf(strValue));
    }

    @Override
    protected URIBroker newInstance() {
        return new TurbineClassicURIBroker();
    }

    @Override
    protected void initDefaults(URIBroker parent) {
        super.initDefaults(parent);

        if (parent instanceof TurbineClassicURIBroker) {
            TurbineClassicURIBroker parentTurbine = (TurbineClassicURIBroker) parent;

            if (!hasTemplate) {
                hasTemplate = parentTurbine.hasTemplate;
                setPathSegment(TEMPLATE_INDEX, parentTurbine.getPathSegment(TEMPLATE_INDEX));
            }

            if (!hasScreen) {
                hasScreen = parentTurbine.hasScreen;
                setPathSegment(SCREEN_INDEX, parentTurbine.getPathSegment(SCREEN_INDEX));
            }

            if (!hasAction) {
                hasAction = parentTurbine.hasAction;
                setPathSegment(ACTION_INDEX, parentTurbine.getPathSegment(ACTION_INDEX));
            }

            setPathSegment(PATH_INFO_PARAM_INDEX, parent.getPathSegment(PATH_INFO_PARAM_INDEX),
                    getPathSegment(PATH_INFO_PARAM_INDEX));
        }
    }

    @Override
    protected void copyFrom(URIBroker parent) {
        super.copyFrom(parent);

        if (parent instanceof TurbineClassicURIBroker) {
            TurbineClassicURIBroker parentTurbine = (TurbineClassicURIBroker) parent;

            hasTemplate = parentTurbine.hasTemplate;
            setPathSegment(TEMPLATE_INDEX, parentTurbine.getPathSegment(TEMPLATE_INDEX));

            hasScreen = parentTurbine.hasScreen;
            setPathSegment(SCREEN_INDEX, parentTurbine.getPathSegment(SCREEN_INDEX));

            hasAction = parentTurbine.hasAction;
            setPathSegment(ACTION_INDEX, parentTurbine.getPathSegment(ACTION_INDEX));

            setPathSegment(PATH_INFO_PARAM_INDEX, parentTurbine.getPathSegment(PATH_INFO_PARAM_INDEX));
        }
    }

    @Override
    protected int getPathSegmentCount() {
        return 6;
    }
}
