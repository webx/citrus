/*
 * Copyright (c) 2002-2012 Alibaba Group Holding Limited.
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
 */

package com.alibaba.citrus.async.pipeline.valve;

import static com.alibaba.citrus.service.requestcontext.util.RequestContextUtil.*;
import static com.alibaba.citrus.springext.util.SpringExtUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.citrus.service.pipeline.Pipeline;
import com.alibaba.citrus.service.pipeline.PipelineContext;
import com.alibaba.citrus.service.pipeline.Valve;
import com.alibaba.citrus.service.pipeline.impl.PipelineImpl;
import com.alibaba.citrus.service.pipeline.support.AbstractValveDefinitionParser;
import com.alibaba.citrus.service.requestcontext.RequestContext;
import com.alibaba.citrus.service.requestcontext.RequestContextChainingService;
import com.alibaba.citrus.turbine.pipeline.valve.AbstractResultConsumerValve;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.core.task.AsyncTaskExecutor;
import org.w3c.dom.Element;

/**
 * 假如screen返回一个<code>Runnable</code>对象，则在另一个线程中执行之，当前valve立即返回，当前线程继续执行并很快退出。
 *
 * @author Michael Zhou
 */
public class PerformRunnableAsyncValve extends AbstractResultConsumerValve {
    final static         String ASYNC_CALLBACK_KEY = "_async_callback_";
    private final static Logger log                = LoggerFactory.getLogger(PerformRunnableAsyncValve.class);
    private long defaultTimeout;

    @Autowired
    private RequestContextChainingService rccs;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private AsyncTaskExecutor executor;

    private Pipeline asyncPipeline;

    public long getDefaultTimeout() {
        return defaultTimeout;
    }

    public void setDefaultTimeout(long defaultTimeout) {
        this.defaultTimeout = defaultTimeout;
    }

    public AsyncTaskExecutor getExecutor() {
        return executor;
    }

    public void setExecutor(AsyncTaskExecutor executor) {
        this.executor = executor;
    }

    public Pipeline getAsyncPipeline() {
        return asyncPipeline;
    }

    public void setAsyncPipeline(Pipeline asyncPipeline) {
        this.asyncPipeline = asyncPipeline;
    }

    @Override
    protected void init() throws Exception {
        if (asyncPipeline == null) {
            DoPerformRunnableValve valve = new DoPerformRunnableValve();
            valve.afterPropertiesSet();

            PipelineImpl pipeline = new PipelineImpl();
            pipeline.setValves(new Valve[] { valve });
            pipeline.afterPropertiesSet();

            asyncPipeline = pipeline;
        }
    }

    public void invoke(final PipelineContext pipelineContext) throws Exception {
        Object resultObject = getResult(pipelineContext);

        if (!(resultObject instanceof Callable<?>) && !(resultObject instanceof Runnable)) {
            return;
        }

        RequestContext rc = getRequestContext(this.request);
        final HttpServletRequest request = rc.getRequest();
        HttpServletResponse response = rc.getResponse();

        final AsyncContext asyncContext = request.startAsync(request, response);

        final AsyncCallbackAdapter callback = new AsyncCallbackAdapter(resultObject, asyncContext, defaultTimeout);
        pipelineContext.setAttribute(ASYNC_CALLBACK_KEY, callback);

        asyncContext.setTimeout(callback.getTimeout());

        // 执行子pipeline，子pipeline中必须包含DoPerformRunnableValve。
        // 执行前将当前的request/response绑定到新线程中。
        final Future<?> future = executor.submit(new Callable<Object>() {
            public Object call() {
                try {
                    try {
                        rccs.bind(request);
                        asyncPipeline.newInvocation(pipelineContext).invoke();
                    } finally {
                        rccs.unbind(request);
                    }
                } catch (Throwable e) {
                    log.error("[" + Thread.currentThread().getName() + "] Exception occurred while doing async task", e);
                } finally {
                    try {
                        asyncContext.complete();
                    } catch (IllegalStateException e) {
                        // ignore - 有可能因为超时，该异步请求已经被complete了，再次complete将会抛异常。
                    }
                }

                return null;
            }
        });

        // 当timeout时中断异步线程，并结束请求
        asyncContext.addListener(new AsyncListener() {
            public void onComplete(AsyncEvent event) throws IOException {
            }

            public void onTimeout(AsyncEvent event) throws IOException {
                future.cancel(true);

                try {
                    event.getAsyncContext().complete();
                } catch (IllegalStateException e) {
                    // ignore - 有可能因为超时，该异步请求已经被complete了，再次complete将会抛异常。
                }
            }

            public void onError(AsyncEvent event) throws IOException {
            }

            public void onStartAsync(AsyncEvent event) throws IOException {
            }
        });

        pipelineContext.invokeNext();
    }

    public static class DefinitionParser extends AbstractValveDefinitionParser<PerformRunnableAsyncValve> {
        @Override
        protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
            attributesToProperties(element, builder, "resultName", "defaultTimeout");

            // sub pipeline
            Object asyncPipeline = parsePipeline(element, null, parserContext, null, true);

            if (asyncPipeline != null) {
                builder.addPropertyValue("asyncPipeline", asyncPipeline);
            }

            // executor
            String executorRef = trimToNull(element.getAttribute("executor-ref"));

            if (executorRef != null) {
                builder.addPropertyValue("executor", new RuntimeBeanReference(executorRef));
            }
        }
    }
}