package com.alibaba.test.app1.module.control;

import com.alibaba.citrus.turbine.ControlParameters;
import com.alibaba.citrus.turbine.dataresolver.ContextValue;

public class MyControlContextValue {
    public void execute(@ContextValue("template") String template, ControlParameters params) throws Exception {
        params.setControlTemplate(template);
    }
}
