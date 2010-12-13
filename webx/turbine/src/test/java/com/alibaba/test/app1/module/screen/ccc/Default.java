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
package com.alibaba.test.app1.module.screen.ccc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.test.module.screen.AbstractModule;

public class Default extends AbstractModule {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private int count;

    public void execute() throws Exception {
        count++;
        log.trace("module.screen.ccc.Default execute success " + count + " times!");
        getRunData().getRequest().setAttribute("module.screen.ccc.Default", "execute success " + count + " times!");
    }
}
