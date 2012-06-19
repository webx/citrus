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

package com.alibaba.citrus.test.context;

import static com.alibaba.citrus.springext.util.ClassCompatibilityAssert.*;

import com.alibaba.citrus.service.resource.support.context.ResourceLoadingXmlApplicationContext;
import com.alibaba.citrus.springext.support.context.AbstractXmlApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.MergedContextConfiguration;
import org.springframework.util.StringUtils;

/**
 * 用来创建基于springext的context。
 *
 * @author Michael Zhou
 */
public class SpringextContextLoader extends AbstractContextLoader {
    static {
        assertSpring3_1_x();
    }

    public final ApplicationContext loadContext(String... locations) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Loading ApplicationContext for locations [" + StringUtils.arrayToCommaDelimitedString(locations)
                      + "].");
        }

        ResourceLoadingXmlApplicationContext context = new ResourceLoadingXmlApplicationContext(locations, testResourceLoader, false);

        prepareContext(context);
        context.refresh();
        context.registerShutdownHook();

        return context;
    }

    public final ConfigurableApplicationContext loadContext(MergedContextConfiguration mergedConfig) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Loading ApplicationContext for merged context configuration [" + mergedConfig + "].");
        }

        ResourceLoadingXmlApplicationContext context = new ResourceLoadingXmlApplicationContext(mergedConfig.getLocations(), testResourceLoader, false);

        context.getEnvironment().setActiveProfiles(mergedConfig.getActiveProfiles());
        prepareContext(context);
        context.refresh();
        context.registerShutdownHook();

        return context;
    }

    protected void prepareContext(AbstractXmlApplicationContext context) {
    }
}
