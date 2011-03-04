package com.alibaba.citrus.turbine;

/**
 * 用来在control module中修改control参数的接口，可注入到control module的参数中。
 * 
 * @author Michael Zhou
 */
public interface ControlParameters {
    /**
     * 取得control模板。
     */
    String getControlTemplate();

    /**
     * 设置control模板。假如之前已经指定了control模板，则覆盖之。
     */
    void setControlTemplate(String template);
}
