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
 * 代表pipeline中的一个“阀门”。
 * <p>
 * 如同真实世界里的水管中的阀门，它可以控制和改变液体的流向，<code>Valve</code> 也可以控制pipeline中后续valves的执行。
 * <code>Valve</code>可以决定是否继续执行后续的valves，或是中断整个pipeline的执行。
 * </p>
 * 
 * @author Michael Zhou
 */
public interface Valve {
    void invoke(PipelineContext pipelineContext) throws Exception;
}
