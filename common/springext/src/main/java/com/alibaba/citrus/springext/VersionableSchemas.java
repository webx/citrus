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
package com.alibaba.citrus.springext;

/**
 * 代表一个主schema，以及一组标记版本的同名schema。
 * <p>
 * 例如：
 * </p>
 * <ul>
 * <li>主schema：<code>beans.xsd</code>；</li>
 * <li>2.5版本schema：<code>beans-2.5.xsd</code>；</li>
 * <li>2.0版本schema：<code>beans-2.0.xsd</code>。</li>
 * </ul>
 * 
 * @author Michael Zhou
 */
public interface VersionableSchemas extends Schemas {
    Schema getMainSchema();

    Schema getVersionedSchema(String version);

    String[] getVersions();
}
