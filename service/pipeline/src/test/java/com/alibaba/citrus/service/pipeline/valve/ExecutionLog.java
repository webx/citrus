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

import static com.alibaba.citrus.util.BasicConstant.*;

import java.util.List;

import com.alibaba.citrus.service.pipeline.PipelineContext;
import com.alibaba.citrus.util.CollectionUtil;

public class ExecutionLog {
    private final static ThreadLocal<List<String>> log = new ThreadLocal<List<String>>();
    public static String counterName = "loopCount";

    static {
        reset();
    }

    public static void reset() {
        log.set(CollectionUtil.<String> createLinkedList());
    }

    public static void add(PipelineContext pipelineContext) {
        if (pipelineContext.getAttribute(counterName) != null) {
            log.get().add(
                    String.format("%d-%d-loop-%d", pipelineContext.level(), pipelineContext.index(),
                            pipelineContext.getAttribute(counterName)));
        } else {
            log.get().add(String.format("%d-%d", pipelineContext.level(), pipelineContext.index()));
        }
    }

    public static String[] toArray() {
        return log.get().toArray(EMPTY_STRING_ARRAY);
    }
}
