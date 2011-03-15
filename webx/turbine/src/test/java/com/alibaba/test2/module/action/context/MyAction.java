package com.alibaba.test2.module.action.context;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.citrus.turbine.dataresolver.ContextValue;

public class MyAction {
    @Autowired
    private HttpServletRequest request;

    public void doGetInt(@ContextValue("aaa") int i) {
        setAttribute(i);
    }

    public void doGetString(@ContextValue("aaa") String s) {
        setAttribute(s);
    }

    private void setAttribute(Object data) {
        request.setAttribute("actionLog", data);
    }
}
