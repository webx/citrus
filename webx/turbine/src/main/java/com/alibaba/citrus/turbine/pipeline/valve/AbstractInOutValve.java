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

package com.alibaba.citrus.turbine.pipeline.valve;

import static com.alibaba.citrus.util.StringUtil.*;

import com.alibaba.citrus.service.pipeline.PipelineContext;
import com.alibaba.citrus.service.pipeline.support.AbstractValve;

/**
 * 从该抽象基类派生的valve，支持从pipeline context中读取、或者向pipeline context输出一个对象。
 * 例如，<code>PerformScreenValve</code>可以输出一个对象，而<code>RenderResultAsJsonValve</code>则取得该对象并生成JSON。
 *
 * @author Michael Zhou
 */
public abstract class AbstractInOutValve extends AbstractValve {
    protected static final String DEFAULT_ATTRIBUTE_KEY = "result";
    private String inKey;
    private String outKey;

    public final String getIn() {
        String key = inKey;

        if (key == null) {
            key = getInDefault();
        }

        if (key == null) {
            key = DEFAULT_ATTRIBUTE_KEY;
        }

        return key;
    }

    /**
     * 子类可覆盖此方法，以设置in的默认值。
     *
     * @return 返回<code>null</code>使用系统默认值。
     */
    protected String getInDefault() {
        return null;
    }

    public final void setIn(String inKey) {
        this.inKey = trimToNull(inKey);
    }

    public final String getOut() {
        String key = outKey;

        if (key == null) {
            key = getOutDefault();
        }

        if (key == null) {
            key = DEFAULT_ATTRIBUTE_KEY;
        }

        return key;
    }

    /**
     * 子类可覆盖此方法，以设置out的默认值。
     *
     * @return 返回<code>null</code>使用系统默认值。
     */
    protected String getOutDefault() {
        return null;
    }

    public final void setOut(String outKey) {
        this.outKey = trimToNull(outKey);
    }

    /** 从pipeline context中取得输入对象。 */
    protected final Object getInputValue(PipelineContext pipelineContext) {
        return pipelineContext.getAttribute(getIn());
    }

    /**
     * 从pipeline context中取出输入对象。
     * 假如对象是可被接受的，那么该对象将从context中被删除，也就是说，没有第二个人可以再次取得同一个对象。
     *
     * @return 如果对象存在且被<code>filterInputValue(Object)</code>所接受，则返回之；否则返回<code>null</code>。
     */
    protected final Object consumeInputValue(PipelineContext pipelineContext) {
        Object value = pipelineContext.getAttribute(getIn());

        if (filterInputValue(value)) {
            pipelineContext.setAttribute(getIn(), null);
        } else {
            value = null;
        }

        return value;
    }

    /**
     * 子类可以覆盖此方法，以接受特定的对象。
     *
     * @return 如果对象可被接受，则返回<code>true</code>。默认的实现总是返回<code>true</code>。
     */
    protected boolean filterInputValue(Object inputValue) {
        return true;
    }

    /** 将指定的对象放到pipeline context中，作为输出对象。 */
    protected final void setOutputValue(PipelineContext pipelineContext, Object outputValue) {
        pipelineContext.setAttribute(getOut(), outputValue);
    }
}
