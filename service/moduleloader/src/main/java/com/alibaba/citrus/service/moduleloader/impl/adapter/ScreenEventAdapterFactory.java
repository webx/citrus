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

package com.alibaba.citrus.service.moduleloader.impl.adapter;

import java.util.Map;

/**
 * 以事件的方式调用screen的方法。
 * <p>
 * 如果target为/screenName/eventName，并且screen实现中包含doEventName方法，则调用之。
 * </p>
 * <p>
 * 支持绑定参数，参见{@link com.alibaba.citrus.service.dataresolver.DataResolverService}。
 * </p>
 *
 * @author Michael Zhou
 */
public class ScreenEventAdapterFactory extends AbstractModuleEventAdapterFactory<ScreenEventAdapter> {
    @Override
    protected boolean isAdaptableType(String type) {
        return "screen".equalsIgnoreCase(type);
    }

    /** Screen一定会被执行，不能被跳过。 */
    @Override
    protected boolean isEventHandlerSkippable() {
        return false;
    }

    protected ScreenEventAdapter createAdapter(Object moduleObject, Map<String, MethodInvoker> handlers, MethodInvoker preHandler, MethodInvoker postHanlder) {
        return new ScreenEventAdapter(moduleObject, handlers, preHandler, postHanlder);
    }
}

