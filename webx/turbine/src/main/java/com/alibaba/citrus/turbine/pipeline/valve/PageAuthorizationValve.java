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

package com.alibaba.citrus.turbine.pipeline.valve;

import static com.alibaba.citrus.springext.util.SpringExtUtil.*;
import static com.alibaba.citrus.turbine.util.TurbineUtil.*;
import static com.alibaba.citrus.util.ArrayUtil.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.util.List;
import javax.servlet.http.HttpServletRequest;

import com.alibaba.citrus.service.pipeline.PipelineContext;
import com.alibaba.citrus.service.pipeline.support.AbstractValve;
import com.alibaba.citrus.service.pipeline.support.AbstractValveDefinitionParser;
import com.alibaba.citrus.turbine.TurbineRunData;
import com.alibaba.citrus.turbine.auth.PageAuthorizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

public class PageAuthorizationValve extends AbstractValve {
    @Autowired
    private HttpServletRequest request;

    @Autowired
    private PageAuthorizationService pageAuthorizationService;

    private Callback<?> callback;

    public void setCallback(Callback<?> callback) {
        this.callback = callback;
    }

    @Override
    protected void init() throws Exception {
        if (callback == null) {
            callback = new DefaultCallback();
        }
    }

    public void invoke(PipelineContext pipelineContext) throws Exception {
        TurbineRunData rundata = getTurbineRunData(request);

        @SuppressWarnings("unchecked")
        Callback<Object> cb = (Callback<Object>) callback;

        Object status = cb.onStart(rundata);

        String userName = cb.getUserName(status);
        String[] roleNames = cb.getRoleNames(status);

        String target = rundata.getTarget();
        String action = rundata.getAction();
        String event = capitalize(rundata.getActionEvent());

        // 取得当前请求的actions，包括三部分：
        // 1. screen
        // 2. action.*.event - 假如请求包含action参数的话
        // 3. callback返回的额外actions
        // 只有当所有action全部被授权时，请求才会继续处理下去。
        List<String> actions = createLinkedList();

        actions.add("screen");

        if (action != null) {
            if (event != null) {
                actions.add("action." + action + ".do" + event);
            } else {
                actions.add("action." + action);
            }
        }

        String[] extraActions = cb.getActions(status);

        if (!isEmptyArray(extraActions)) {
            for (String extraAction : extraActions) {
                actions.add(extraAction);
            }
        }

        // 检查权限，根据：
        // 1. 当前的target
        // 2. 当前的user和roles
        // 3. 将要执行的actions，例如：screen、action.xxx.UserAction
        if (pageAuthorizationService.isAllow(target, userName, roleNames, actions.toArray(new String[actions.size()]))) {
            cb.onAllow(status);
            pipelineContext.invokeNext();
        } else {
            cb.onDeny(status);
            // end of pipeline
        }
    }

    public interface Callback<T> {
        String getUserName(T status);

        String[] getRoleNames(T status);

        String[] getActions(T status);

        T onStart(TurbineRunData rundata) throws Exception;

        void onAllow(T status) throws Exception;

        void onDeny(T status) throws Exception;
    }

    private class DefaultCallback implements Callback<TurbineRunData> {
        public String getUserName(TurbineRunData status) {
            return null;
        }

        public String[] getRoleNames(TurbineRunData status) {
            return null;
        }

        public String[] getActions(TurbineRunData status) {
            return null;
        }

        public TurbineRunData onStart(TurbineRunData rundata) {
            return rundata;
        }

        public void onAllow(TurbineRunData status) throws Exception {
        }

        public void onDeny(TurbineRunData status) {
        }
    }

    public static class DefinitionParser extends AbstractValveDefinitionParser<PageAuthorizationValve> {
        @Override
        protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
            element.setAttribute("class", element.getAttribute("callbackClass"));
            builder.addPropertyValue("callback", parseBean(element, parserContext, builder));
        }
    }
}
