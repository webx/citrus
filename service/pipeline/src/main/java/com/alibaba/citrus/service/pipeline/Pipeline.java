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
package com.alibaba.citrus.service.pipeline;

/**
 * 代表一组顺序执行的操作，好象液体流过一根管道一样。
 * 
 * @author Michael Zhou
 */
public interface Pipeline {
    /**
     * 特殊的label，用来中断整个pipeline的执行。
     */
    String TOP_LABEL = "#TOP";

    /**
     * 取得pipeline的标签。
     * <p>
     * 这是一个可选的参数，用来方便break中断指定label的pipeline。
     * </p>
     */
    String getLabel();

    /**
     * 创建一次新的执行。
     */
    PipelineInvocationHandle newInvocation();

    /**
     * 创建一次新的执行，并将此次执行看作另一个执行的子过程。
     */
    PipelineInvocationHandle newInvocation(PipelineContext parentContext);
}
