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
package com.alibaba.citrus.service.moduleloader.impl.adapter;

import com.alibaba.citrus.util.internal.ToStringBuilder;
import com.alibaba.citrus.util.internal.ToStringBuilder.MapBuilder;

/**
 * 调用<code>execute()</code>方法，不要求实现<code>Module</code>接口。
 * 
 * @author Michael Zhou
 */
public class DataBindingAdapter extends AbstractDataBindingAdapter {
    private final MethodInvoker executeMethod;

    DataBindingAdapter(Object moduleObject, MethodInvoker executeMethod) {
        super(moduleObject);
        this.executeMethod = executeMethod;
    }

    public void execute() throws Exception {
        executeMethod.invoke(moduleObject, log);
    }

    @Override
    public String toString() {
        MapBuilder mb = new MapBuilder();

        mb.append("moduleClass", moduleObject.getClass().getName());
        mb.append("executeMethod", executeMethod);

        return new ToStringBuilder().append(getClass().getSimpleName()).append(mb).toString();
    }
}
