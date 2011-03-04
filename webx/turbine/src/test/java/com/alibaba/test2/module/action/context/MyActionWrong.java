package com.alibaba.test2.module.action.context;

import com.alibaba.citrus.turbine.dataresolver.ContextValue;

public class MyActionWrong {
    public void doWrong(@ContextValue("") String s) {
    }
}
