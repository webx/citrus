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
 * 所有使用了result返回值对象的valve的基类。
 *
 * @author Michael Zhou
 */
public abstract class AbstractResultConsumerValve extends AbstractValve {
    protected static final String DEFAULT_RESULT_NAME = PerformScreenValve.DEFAULT_RESULT_NAME;
    private String resultName;

    public String getResultName() {
        return resultName == null ? DEFAULT_RESULT_NAME : resultName;
    }

    public void setResultName(String resultName) {
        this.resultName = trimToNull(resultName);
    }

    protected final Object getResult(PipelineContext pipelineContext) {
        return pipelineContext.getAttribute(getResultName());
    }
}
