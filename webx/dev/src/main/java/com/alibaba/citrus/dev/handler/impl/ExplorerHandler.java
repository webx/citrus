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

package com.alibaba.citrus.dev.handler.impl;

import static com.alibaba.citrus.util.CollectionUtil.*;

import java.io.IOException;
import java.util.Map;

import com.alibaba.citrus.dev.handler.impl.visitor.BeansVisitor;
import com.alibaba.citrus.dev.handler.impl.visitor.ConfigurationsVisitor;
import com.alibaba.citrus.dev.handler.impl.visitor.PullToolsVisitor;
import com.alibaba.citrus.dev.handler.impl.visitor.ResolvableDepsVisitor;
import com.alibaba.citrus.dev.handler.impl.visitor.ResourcesVisitor;
import com.alibaba.citrus.dev.handler.impl.visitor.UrisVisitor;
import com.alibaba.citrus.dev.handler.util.ConfigurationFileReader;
import com.alibaba.citrus.service.pull.PullService;
import com.alibaba.citrus.service.resource.ResourceLoadingService;
import com.alibaba.citrus.service.uribroker.URIBrokerService;
import com.alibaba.citrus.util.templatelite.Template;
import com.alibaba.citrus.webx.handler.RequestHandlerContext;

public class ExplorerHandler extends AbstractExplorerHandler {
    private static final Map<String, String> AVAILABLE_FUNCTIONS = createLinkedHashMap();

    static {
        AVAILABLE_FUNCTIONS.put("Beans", "Beans");
        AVAILABLE_FUNCTIONS.put("Configurations", "Configurations");
        AVAILABLE_FUNCTIONS.put("ResolvableDependencies", "Resolvable Dependencies");
        AVAILABLE_FUNCTIONS.put("Resources", "Resources");
        AVAILABLE_FUNCTIONS.put("URIs", "URIs");
        AVAILABLE_FUNCTIONS.put("PullTools", "Pull Tools");
    }

    @Override
    protected Map<String, String> getAvailableFunctions() {
        return AVAILABLE_FUNCTIONS;
    }

    @Override
    protected String getDefaultFunction() {
        return "Beans";
    }

    @Override
    protected ExplorerVisitor getBodyVisitor(RequestHandlerContext context) {
        return new ExplorerVisitor(context);
    }

    @Override
    protected String[] getStyleSheets() {
        return new String[] { "explorer.css" };
    }

    @Override
    protected String[] getJavaScripts() {
        return new String[] { "explorer.js" };
    }

    public class ExplorerVisitor extends AbstractExplorerVisitor {
        public ExplorerVisitor(RequestHandlerContext context) {
            super(context);
        }

        public Object visitBeans(Template beansTemplate) {
            return new BeansVisitor(context, this);
        }

        public Object visitConfigurations(Template configurationsTemplate) throws IOException {
            return new ConfigurationsVisitor(context, this,
                                             new ConfigurationFileReader(appcontext, configLocations).toConfigurationFiles());
        }

        public Object visitResolvableDependencies(Template resolvableDepsTemplate) {
            return new ResolvableDepsVisitor(context, this);
        }

        public Object visitResources(Template resourcesTemplate) {
            return new ResourcesVisitor(context, this, getService("resourceLoadingService",
                                                                  ResourceLoadingService.class));
        }

        public Object visitUris(Template urisTemplate) {
            return new UrisVisitor(context, this, getService("uriBrokerService", URIBrokerService.class));
        }

        public Object visitPullTools(Template pullToolsTemplate) {
            return new PullToolsVisitor(context, this, getService("pullService", PullService.class));
        }
    }
}
