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

import com.alibaba.citrus.service.dataresolver.DataResolverService;

/**
 * 以事件的方式调用action的方法。
 * <p>
 * 如果submit按钮以eventSubmit_doSomething命名，并且action实现中包含此方法，则调用之。
 * </p>
 * <p>
 * 支持绑定参数，参见{@link DataResolverService}。
 * </p>
 *
 * @author Michael Zhou
 */
public class ActionEventAdapterFactory extends AbstractModuleEventAdapterFactory<ActionEventAdapter> {
    @Override
    protected boolean isAdaptableType(String type) {
        return "action".equalsIgnoreCase(type);
    }

    /** 当符合特定条件时，例如，当表单验证未通过时，action可被跳过不执行。 */
    @Override
    protected boolean isEventHandlerSkippable() {
        return true;
    }

    protected ActionEventAdapter createAdapter(Object moduleObject, Map<String, MethodInvoker> handlers, MethodInvoker preHandler, MethodInvoker postHanlder) {
        return new ActionEventAdapter(moduleObject, handlers, preHandler, postHanlder);
    }
}
