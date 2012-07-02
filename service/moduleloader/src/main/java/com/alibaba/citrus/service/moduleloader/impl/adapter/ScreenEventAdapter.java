/*
 * Copyright 2012 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */

package com.alibaba.citrus.service.moduleloader.impl.adapter;

import static com.alibaba.citrus.service.moduleloader.constant.ModuleConstant.*;
import static com.alibaba.citrus.util.Assert.*;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * 类ScreenEventAdapter.java的实现描述：TODO 类实现描述
 *
 * @author qianchao 2012-6-29 上午10:35:50
 */
public class ScreenEventAdapter extends AbstractDataBindingAdapter implements HandlerModule {

    private final Map<String, MethodInvoker> handlers;
    @Autowired
    private       HttpServletRequest         request;

    ScreenEventAdapter(Object moduleObject, Map<String, MethodInvoker> handlers) {
        super(moduleObject);
        this.handlers = handlers;
        assertTrue(!handlers.isEmpty(), "handlers");
    }

    @Override
    public void execute() throws Exception {
        String methodName = (String) request.getAttribute(EVENT_HANDLER_METHOD);
        if (methodName == null) {
            methodName = DEFAULT_EXECUTE_METHOD;
        }
        MethodInvoker mi = handlers.get(methodName);
        if (mi != null) {
            mi.invoke(moduleObject, log);
        }
    }

    @Override
    public Map<String, MethodInvoker> getHandlers() {
        return handlers;
    }

    @Override
    public MethodInvoker getExecuteHandler() {
        return handlers.get(DEFAULT_EXECUTE_METHOD);
    }
}
