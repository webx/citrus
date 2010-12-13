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
package com.alibaba.citrus.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.citrus.springext.support.GenericBeanSupport;

/**
 * 作为service的基类，方便service的实现。
 * <p>
 * 非singleton的service不应该实现此接口。
 * </p>
 * 
 * @author Michael Zhou
 */
public abstract class AbstractService<S> extends GenericBeanSupport<S> {
    private Logger log;

    public final Logger getLogger() {
        if (log == null) {
            log = createLogger();
        }

        return log;
    }

    /**
     * 创建一个logger。
     */
    protected Logger createLogger() {
        return LoggerFactory.getLogger(getBeanInterface());
    }
}
