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
package com.alibaba.citrus.service.pipeline.impl;

import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.Assert.ExceptionType.*;
import static com.alibaba.citrus.util.BasicConstant.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.ObjectUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.util.Map;

import org.slf4j.Logger;

import com.alibaba.citrus.service.AbstractService;
import com.alibaba.citrus.service.pipeline.LabelNotDefinedException;
import com.alibaba.citrus.service.pipeline.Pipeline;
import com.alibaba.citrus.service.pipeline.PipelineContext;
import com.alibaba.citrus.service.pipeline.PipelineException;
import com.alibaba.citrus.service.pipeline.PipelineInvocationHandle;
import com.alibaba.citrus.service.pipeline.Valve;
import com.alibaba.citrus.util.internal.ToStringBuilder;

/**
 * 对<code>Pipeline</code>的实现。
 * 
 * @author Michael Zhou
 */
public class PipelineImpl extends AbstractService<Pipeline> implements Pipeline {
    private Valve[] valves;
    private String label;

    public Valve[] getValves() {
        return valves;
    }

    public void setValves(Valve[] valves) {
        this.valves = valves;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = trimToNull(label);
    }

    @Override
    protected void init() {
        if (valves == null) {
            valves = new Valve[0];
        }

        for (int i = 0; i < valves.length; i++) {
            assertNotNull(valves[i], "valves[%d] == null", i);
        }
    }

    public PipelineInvocationHandle newInvocation() {
        return new PipelineContextImpl(null);
    }

    public PipelineInvocationHandle newInvocation(PipelineContext parentContext) {
        return new PipelineContextImpl(assertNotNull(parentContext, "no parent PipelineContext"));
    }

    @Override
    public String toString() {
        return new ToStringBuilder().append(getBeanDescription()).append(valves).toString();
    }

    /**
     * 实现<code>PipelineContext</code>。
     */
    private final class PipelineContextImpl implements PipelineContext, PipelineInvocationHandle {
        private final Logger log = getLogger();
        private final PipelineContext parentContext;
        private final int level;
        private int executedIndex = -1;
        private int executingIndex = -1;
        private boolean broken;
        private Map<String, Object> attributes;

        public PipelineContextImpl(PipelineContext parentContext) {
            this.parentContext = parentContext;

            if (parentContext == null) {
                this.level = 1;
            } else {
                this.level = parentContext.level() + 1;
            }
        }

        public int level() {
            return level;
        }

        public int index() {
            return executingIndex + 1;
        }

        public int findLabel(String label) throws LabelNotDefinedException {
            label = assertNotNull(trimToNull(label), "no label");
            boolean isTop = TOP_LABEL.equals(label);

            if (isTop && parentContext == null) {
                return 0;
            } else if (label.equals(getLabel())) {
                return 0;
            } else if (parentContext != null) {
                return parentContext.findLabel(label) + 1;
            } else {
                throw new LabelNotDefinedException("Could not find pipeline or sub-pipeline with label \"" + label
                        + "\" in the pipeline invocation stack");
            }
        }

        public void invokeNext() {
            assertInitialized();

            if (broken) {
                return;
            }

            try {
                executingIndex++;

                if (executingIndex <= executedIndex) {
                    throw new IllegalStateException(descCurrentValve() + " has already been invoked: "
                            + valves[executingIndex]);
                }

                executedIndex++;

                if (executingIndex < valves.length) {
                    Valve valve = valves[executingIndex];

                    try {
                        if (log.isTraceEnabled()) {
                            log.trace("Entering {}: {}", descCurrentValve(), valve);
                        }

                        valve.invoke(this);
                    } catch (PipelineException e) {
                        throw e;
                    } catch (Exception e) {
                        throw new PipelineException("Failed to invoke " + descCurrentValve() + ": " + valve, e);
                    } finally {
                        if (log.isTraceEnabled()) {
                            log.trace("...Exited {}: {}", descCurrentValve(), valve);
                        }
                    }

                    if (executedIndex < valves.length && executedIndex == executingIndex) {
                        if (log.isTraceEnabled()) {
                            log.trace("{} execution was interrupted by {}: {}", new Object[] { descCurrentPipeline(),
                                    descCurrentValve(), valve });
                        }
                    }
                } else {
                    if (log.isTraceEnabled()) {
                        log.trace("{} reaches its end.", descCurrentPipeline());
                    }
                }
            } finally {
                executingIndex--;
            }
        }

        public void breakPipeline(int levels) {
            assertTrue(levels >= 0 && levels < level, "invalid break levels: %d, should be in range of [0, %d)",
                    levels, level);

            broken = true;

            if (levels > 0) {
                parentContext.breakPipeline(levels - 1);
            }
        }

        public void breakPipeline(String label) throws LabelNotDefinedException {
            breakPipeline(findLabel(label));
        }

        public boolean isBroken() {
            return broken;
        }

        public boolean isFinished() {
            return !broken && executedIndex >= valves.length;
        }

        public void invoke() throws IllegalStateException {
            assertTrue(!isBroken(), ILLEGAL_STATE, "cannot reinvoke a broken pipeline");
            executingIndex = executedIndex = -1;
            invokeNext();
        }

        public Object getAttribute(String key) {
            Object value = null;

            if (attributes != null) {
                value = attributes.get(key);
            }

            if (value == null && parentContext != null) {
                value = parentContext.getAttribute(key);
            }

            return value == NULL_PLACEHOLDER ? null : value;
        }

        public void setAttribute(String key, Object value) {
            if (attributes == null) {
                attributes = createHashMap();
            }

            attributes.put(key, defaultIfNull(value, NULL_PLACEHOLDER));
        }

        @Override
        public String toString() {
            return "Executing Pipeline " + descCurrentValve();
        }

        private String descCurrentPipeline() {
            return "Pipeline[level " + level() + "]";
        }

        private String descCurrentValve() {
            return "Valve[#" + index() + "/" + valves.length + ", level " + level() + "]";
        }
    }
}
