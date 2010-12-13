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
/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package com.alibaba.citrus.service.velocity.impl;

import org.apache.velocity.runtime.parser.node.AbstractExecutor;
import org.apache.velocity.runtime.parser.node.BooleanPropertyExecutor;
import org.apache.velocity.runtime.parser.node.GetExecutor;
import org.apache.velocity.runtime.parser.node.MapGetExecutor;
import org.apache.velocity.runtime.parser.node.PropertyExecutor;
import org.apache.velocity.util.introspection.Info;
import org.apache.velocity.util.introspection.VelPropertyGet;

/**
 * 修改velocity默认的uberspect，改变默认的get property方法的顺序：
 * <ul>
 * <li><code>getFoo()</code>或<code>getfoo()</code>。</li>
 * <li><code>isFoo()</code>或<code>isfoo()</code>。</li>
 * <li><code>Map.get(String)</code>。</li>
 * <li><code>AnyType.get(String)</code>。</li>
 * </ul>
 * 
 * @author Michael Zhou
 */
public class CustomizedUberspectImpl extends org.apache.velocity.util.introspection.UberspectImpl {
    @Override
    public VelPropertyGet getPropertyGet(Object obj, String identifier, Info i) throws Exception {
        if (obj == null) {
            return null;
        }

        Class<?> claz = obj.getClass();

        /*
         * first try for a getFoo() type of property (also getfoo() )
         */
        AbstractExecutor executor = new PropertyExecutor(log, introspector, claz, identifier);

        /*
         * if that didn't work, look for boolean isFoo()
         */
        if (!executor.isAlive()) {
            executor = new BooleanPropertyExecutor(log, introspector, claz, identifier);
        }

        /*
         * Let's see if we are a map...
         */
        if (!executor.isAlive()) {
            executor = new MapGetExecutor(log, claz, identifier);
        }

        /*
         * finally, look for get("foo")
         */
        if (!executor.isAlive()) {
            executor = new GetExecutor(log, introspector, claz, identifier);
        }

        return executor.isAlive() ? new VelGetterImpl(executor) : null;
    }
}
