package com.alibaba.citrus.turbine.dataresolver;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用来标识一个context中的值。
 * <p>
 * 用法如下：
 * </p>
 * <ol>
 * <li>指定值的名称：<code>@ContextValue("name")</code>。</li> </li>
 * </ol>
 * 
 * @author Michael Zhou
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.PARAMETER })
public @interface ContextValue {
    /**
     * 用于标识context值的名称。
     * <p>
     * 此参数用于简化的形式：<code>@ContextValue("name")</code>。
     * </p>
     */
    String value();
}
