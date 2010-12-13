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
package com.alibaba.citrus.service.pipeline.valve;

import com.alibaba.citrus.service.pipeline.PipelineContext;
import com.alibaba.citrus.service.pipeline.Valve;

public class LogAndBreakValve implements Valve {
    private final String label;
    private final int breakLevels;

    public LogAndBreakValve() {
        this(0);
    }

    public LogAndBreakValve(int breakLevels) {
        this.label = "<null>";
        this.breakLevels = breakLevels;
    }

    public LogAndBreakValve(String label) {
        this.label = label;
        this.breakLevels = -1;
    }

    public void invoke(PipelineContext pipelineContext) {
        ExecutionLog.add(pipelineContext);

        if ("<null>".equals(label)) {
            pipelineContext.breakPipeline(breakLevels);
        } else {
            pipelineContext.breakPipeline(label);
        }

        pipelineContext.invokeNext();
    }

    @Override
    public String toString() {
        return "LogAndBreakValve[" + label + ", " + breakLevels + "]";
    }
}
