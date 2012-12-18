/*
 * Copyright (c) 2002-2012 Alibaba Group Holding Limited.
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
 */

package com.alibaba.citrus.springext;

import com.alibaba.citrus.springext.ResourceResolver.Resource;

/**
 * 如果一个类实现了这个接口，那么可以从这个类中取得用来装入该类数据的原始数据源对象。
 * 这个类将有助于实现IDE plugins。
 *
 * @author Michael Zhou
 */
public interface SourceInfo<P extends SourceInfo<?>> {
    P getParent();

    Resource getSource();

    int getLineNumber();
}

