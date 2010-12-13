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
package com.alibaba.citrus.service.pull;

import java.util.Map;
import java.util.Set;

/**
 * 代表一个context，通过context可以取得所有的tools。
 * <p>
 * 该实现包含延迟加载的逻辑，只当有需要时，才会加载指定的tool。
 * </p>
 * 
 * @author Michael Zhou
 */
public interface PullContext {
    Object pull(String name);

    Set<String> getToolNames();

    Map<String, Object> getTools();
}
