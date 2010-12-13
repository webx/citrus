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
 * Pipeline的当前状态，是被<code>PipelineContext</code>和
 * <code>PipelineInvocationHandle</code>共享的接口。
 * 
 * @author Michael Zhou
 */
public interface PipelineStates {
    /**
     * 取得当前正在执行的pipeline的嵌套层次。注意，该号码从<code>1</code>开始计数。
     */
    int level();

    /**
     * 取得当前正在执行的valve的索引号。注意，该号码从<code>1</code>开始计数。
     */
    int index();

    /**
     * 查找label，并返回与当前pipeline相隔的层数。
     */
    int findLabel(String label);

    /**
     * 检查pipeline将是否被中断。
     */
    boolean isBroken();

    /**
     * 检查pipeline将是否已执行完成。
     */
    boolean isFinished();

    /**
     * 取得当前pipeline执行的状态。
     * <p>
     * 假如取不到，则向上查找，直到找到或者到达顶层。
     * </p>
     */
    Object getAttribute(String key);

    /**
     * 设置当前pipeline的状态。
     * <p>
     * 设置当前pipeline执行的状态，会覆盖上层同名的状态值，然而却不会影响上一层执行的状态。当执行返回到上一层时，所以有的改变都被丢弃。
     * </p>
     */
    void setAttribute(String key, Object value);
}
