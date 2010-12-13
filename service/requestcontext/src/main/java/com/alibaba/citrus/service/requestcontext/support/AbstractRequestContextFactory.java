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
package com.alibaba.citrus.service.requestcontext.support;

import static com.alibaba.citrus.generictype.TypeInfoUtil.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.citrus.service.requestcontext.RequestContext;
import com.alibaba.citrus.service.requestcontext.RequestContextFactory;
import com.alibaba.citrus.springext.support.BeanSupport;
import com.alibaba.citrus.util.internal.ToStringBuilder;
import com.alibaba.citrus.util.internal.ToStringBuilder.MapBuilder;

/**
 * <code>RequestContextFactory</code>接口的基本实现。
 * 
 * @author Michael Zhou
 */
public abstract class AbstractRequestContextFactory<R extends RequestContext> extends BeanSupport implements
        RequestContextFactory<R> {
    protected final Logger log = LoggerFactory.getLogger(getClass());
    private final Class<R> requestContextInterface;

    @SuppressWarnings("unchecked")
    public AbstractRequestContextFactory() {
        this.requestContextInterface = (Class<R>) resolveParameter(getClass(), RequestContextFactory.class, 0)
                .getRawType();
    }

    /**
     * 取得当前factory将生成的request context接口。
     */
    public final Class<R> getRequestContextInterface() {
        return requestContextInterface;
    }

    public Class<? extends R> getRequestContextProxyInterface() {
        return requestContextInterface;
    }

    /**
     * 初始化完成后打印日志。
     */
    @Override
    protected void postInit() {
        log.debug("Initialized {}", this);
    }

    /**
     * 取得字符串表示。
     * 
     * @return 字符串表示
     */
    @Override
    public String toString() {
        MapBuilder mb = new MapBuilder();
        Object conf = dumpConfiguration();

        mb.append("factoryName", getBeanName());

        if (conf != null) {
            mb.append("configuration", conf);
        }

        return new ToStringBuilder().append(getBeanDescription()).append(mb).toString();
    }

    protected Object dumpConfiguration() {
        return null;
    }
}
