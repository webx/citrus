package com.alibaba.citrus.springext.util;

/**
 * 用来创建proxy目标对象的工厂。
 * 
 * @author Michael Zhou
 */
public interface ProxyTargetFactory {
    Object getObject();
}
