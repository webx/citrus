/*
 * Copyright 2010 Alibaba Group Holding Limited.
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
 *
 */
package com.alibaba.citrus.turbine.pipeline.valve;

import static com.alibaba.citrus.turbine.util.TurbineUtil.*;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.citrus.service.configuration.ProductionModeAware;
import com.alibaba.citrus.service.moduleloader.ActionEventException;
import com.alibaba.citrus.service.pipeline.Pipeline;
import com.alibaba.citrus.service.pipeline.PipelineContext;
import com.alibaba.citrus.service.pipeline.PipelineException;
import com.alibaba.citrus.service.pipeline.Valve;
import com.alibaba.citrus.springext.support.BeanSupport;
import com.alibaba.citrus.turbine.TurbineRunDataInternal;
import com.alibaba.citrus.webx.WebxException;

/**
 * 用来处理声明式异常处理的valve，应用可以根据不同的异常配置，显示不同的异常页面。
 * 
 * @author youqun.zhangyq
 * @deprecated use HandleExceptionValve instead
 */
@Deprecated
public class ExceptionRecoveryValve extends BeanSupport implements Valve, ProductionModeAware {
    private static final Logger log = LoggerFactory.getLogger(ExceptionRecoveryValve.class);

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private HttpServletResponse response;

    // 异常在上下文中的属性名称
    private String name;

    // 缺少恢复异常的处理页面
    private String defaultErrorPage;

    // 存储异常信息的标识符
    private String errorKey;

    // 是否使用布局
    private boolean useLayout = true;

    private boolean productionMode;

    private Map<String, String> exceptionMapping = new HashMap<String, String>();

    private Map<String, Integer> exceptionErrorCodeMapping = new HashMap<String, Integer>();

    public ExceptionRecoveryValve() {
        this.name = "exception";
        this.defaultErrorPage = "global/error";
        this.errorKey = "error";
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDefaultErrorPage(String errorPage) {
        this.defaultErrorPage = errorPage;
    }

    public void setErrorKey(String errorKey) {
        this.errorKey = errorKey;
    }

    public void setUseLayout(boolean useLayout) {
        this.useLayout = useLayout;
    }

    public void setExceptionMapping(Map<String, String> exceptionMapping) {
        this.exceptionMapping = exceptionMapping;
    }

    public void setExceptionErrorCodeMapping(Map<String, Integer> exceptionErrorCodeMapping) {
        this.exceptionErrorCodeMapping = exceptionErrorCodeMapping;
    }

    @Override
    protected void init() throws Exception {
        log.warn("ExceptionRecoveryValve is deprecated.  Please use HandleExceptionValve instead.");
    }

    public void invoke(PipelineContext pipelineContext) throws Exception {

        TurbineRunDataInternal rundata = (TurbineRunDataInternal) getTurbineRunData(request);

        Throwable originalException = (Throwable) pipelineContext.getAttribute(name);
        Throwable exception = originalException;

        if (exception != null) {
            // 提取应用异常
            exception = this.extractAppException(exception);
        }

        //如果是开发模式，中止当前Pipeline，交给ExceptionHandler去处理
        if (!this.productionMode) {
            pipelineContext.breakPipeline(Pipeline.TOP_LABEL);
            pipelineContext.invokeNext();

            throw (Exception) exception;
        }

        String exceptionClass = exception.getClass().getName();

        // 设置errorCode 
        this.setResponseStatus(exceptionClass);

        // 将异常对象写入上下文
        rundata.getContext().put(this.errorKey, exception);

        //获取errorPage
        String errorPage = this.findErrorPage(exceptionClass);

        // 设置出错页面
        rundata.setTarget(errorPage);

        // 是否使用Layout
        rundata.setLayoutEnabled(this.useLayout);

        // 执行下一个Valve
        pipelineContext.invokeNext();
    }

    private void setResponseStatus(String exceptionClass) {
        Integer errorCode = exceptionErrorCodeMapping.get(exceptionClass);
        if (errorCode == null) {
            errorCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
        }
        response.setStatus(errorCode);
    }

    private Throwable extractAppException(Throwable exception) {
        // 提取应用异常
        if (exception instanceof PipelineException) {
            Throwable cause = exception.getCause();
            if (cause != null) {
                exception = cause;
                if (exception instanceof WebxException || exception instanceof ActionEventException) {
                    cause = exception.getCause();
                    if (cause != null) {
                        exception = cause;
                    }
                }
            }
        }
        return exception;
    }

    private String findErrorPage(String exceptionClass) {
        // 根据异常与错误处理页面的映射，获取errorPage（缺省使用defaultErrorPage）
        String errorPage = this.exceptionMapping.get(exceptionClass);
        if (errorPage == null) {
            errorPage = this.defaultErrorPage;
        }
        return errorPage;
    }

    @Override
    public String toString() {
        return "ExceptionRecoveryValve";
    }

    public void setProductionMode(boolean productionMode) {
        this.productionMode = productionMode;
    }
}
