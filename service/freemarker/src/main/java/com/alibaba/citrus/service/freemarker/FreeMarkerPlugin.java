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
package com.alibaba.citrus.service.freemarker;

/**
 * 用来扩展freemarker的功能。
 * 
 * @author Michael Zhou
 */
public interface FreeMarkerPlugin {
    /**
     * 初始化freemarker。
     * <p>
     * 在这里，plugin可以改变和设定configuration中的设置。
     * </p>
     */
    void init(FreeMarkerConfiguration configuration);
}
