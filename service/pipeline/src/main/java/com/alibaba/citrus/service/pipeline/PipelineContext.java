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
 * <code>PipelineContext</code>
 * 是由pipeline提供给valve的一个上下文对象，它代表了当前pipeline的执行状态，并控制pipeline的执行步骤。
 * 
 * @author Michael Zhou
 */
public interface PipelineContext extends PipelineStates {
    /**
     * 执行pipeline中下一个valve。
     * 
     * @throws IllegalStateException 假如该方法被多次调用。
     */
    void invokeNext() throws IllegalStateException, PipelineException;

    /**
     * 中断并跳出pipeline的执行。
     * 
     * @param levels 中断并跳出指定层数的pipeline，<code>0</code>代表仅中断当前pipeline的执行。
     */
    void breakPipeline(int levels);

    /**
     * 中断并跳出pipeline的执行。
     * 
     * @param label 中断并跳出指定label的pipeline
     */
    void breakPipeline(String label);
}
