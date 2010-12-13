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
package com.alibaba.citrus.service.resource;

/**
 * 定义了查找resource时的参数。
 */
public enum ResourceLoadingOption {
    /**
     * 假如指定了这个选项，则表明所查找的resource是用来“被创建”的，因此不必存在。 例如，为了创建新文件、新目录而查找资源。
     */
    FOR_CREATE;
}
