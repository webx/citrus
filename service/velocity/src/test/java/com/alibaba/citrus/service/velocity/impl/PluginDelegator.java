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
package com.alibaba.citrus.service.velocity.impl;

import java.io.IOException;

import org.springframework.core.io.Resource;

import com.alibaba.citrus.service.velocity.VelocityConfiguration;
import com.alibaba.citrus.service.velocity.VelocityPlugin;

public class PluginDelegator implements VelocityPlugin {
    public final static ThreadLocal<VelocityPlugin> pluginHolder = new ThreadLocal<VelocityPlugin>();

    public void init(VelocityConfiguration configuration) throws Exception {
        VelocityPlugin plugin = pluginHolder.get();

        if (plugin != null) {
            plugin.init(configuration);
        }
    }

    public Resource[] getMacros() throws IOException {
        VelocityPlugin plugin = pluginHolder.get();

        if (plugin != null) {
            return plugin.getMacros();
        }

        return null;
    }
}
