package com.alibaba.test.app1.module.control;

import static org.junit.Assert.*;

import com.alibaba.citrus.turbine.ControlParameters;

public class MyControlChangingTemplate {
    public static String expectedTemplateName;
    public static String changedTemplateName;

    public void execute(ControlParameters params) throws Exception {
        assertEquals(expectedTemplateName, params.getControlTemplate());
        params.setControlTemplate(changedTemplateName);
    }
}
