package com.alibaba.citrus.util.templatelite;

/**
 * 如果visitor实现了这个接口，那么当访问visitor方法出错时，接口将被调用，以处理异常。否则，<code>Template</code>
 * 将抛出异常。
 * 
 * @author Michael Zhou
 */
public interface VisitorInvocationErrorHandler {
    void handleInvocationError(String desc, Throwable e);
}
