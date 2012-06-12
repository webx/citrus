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

package com.alibaba.citrus.turbine;

/**
 * 用来在control module中修改control参数的接口，可注入到control module的参数中。
 *
 * @author Michael Zhou
 */
public interface ControlParameters {
    /** 取得control模板。 */
    String getControlTemplate();

    /** 设置control模板。假如之前已经指定了control模板，则覆盖之。 */
    void setControlTemplate(String template);
}
