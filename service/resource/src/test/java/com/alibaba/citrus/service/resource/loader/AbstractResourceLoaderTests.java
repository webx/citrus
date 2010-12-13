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
package com.alibaba.citrus.service.resource.loader;

import static com.alibaba.citrus.test.TestEnvStatic.*;
import static org.junit.Assert.*;

import java.io.File;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.alibaba.citrus.service.resource.AbstractResourceLoadingTests;
import com.alibaba.citrus.service.resource.Resource;
import com.alibaba.citrus.service.resource.ResourceLister;
import com.alibaba.citrus.service.resource.ResourceListerContext;
import com.alibaba.citrus.service.resource.ResourceLoader;
import com.alibaba.citrus.service.resource.ResourceLoaderContext;
import com.alibaba.citrus.service.resource.ResourceLoadingOption;
import com.alibaba.citrus.util.internal.regex.MatchResultSubstitution;

public abstract class AbstractResourceLoaderTests<L extends ResourceLoader> extends AbstractResourceLoadingTests {
    protected L loader;

    protected void assertResourceLoader(String resourceName, String fileName, boolean exists) throws Exception {
        assertResourceLoader(resourceName, fileName, exists, null);
    }

    protected void assertResourceLoader(String resourceName, String fileName, boolean exists,
                                        Set<ResourceLoadingOption> options) throws Exception {
        Resource resource = loader.getResource(new MyContext(resourceName), options);

        if (exists) {
            assertNotNull(resource);
            assertTrue(resource.exists());
            assertEquals(new File(srcdir, fileName).getAbsolutePath(), resource.getFile().getAbsolutePath());
        } else {
            assertNull(resource);
        }
    }

    protected void assertResourceLister(String resourceName, String basedir, boolean exists, String... results)
            throws Exception {
        assertTrue(loader instanceof ResourceLister);

        String[] names = ((ResourceLister) loader).list(new MyContext(resourceName), null);

        assertNames(exists, (basedir == null ? null : new File(srcdir, basedir)), names, results);
    }

    protected abstract String getPrefix();

    public class MyContext implements ResourceLoaderContext, ResourceListerContext {
        private String name;
        private Pattern pattern;
        private MatchResultSubstitution subs;

        public MyContext(String name) {
            this.name = name;
            this.pattern = Pattern.compile("^/" + getPrefix());

            Matcher matcher = this.pattern.matcher(name);

            assertTrue(matcher.find());

            this.subs = new MatchResultSubstitution(matcher);
        }

        public String getResourceName() {
            return name;
        }

        public String substitute(String substitution) {
            return name.substring(0, subs.getMatch().start()) + subs.substitute(substitution)
                    + name.substring(subs.getMatch().end());
        }

        public Resource getResource(String newResourceName, Set<ResourceLoadingOption> options) {
            fail();
            return null;
        }

        public String[] list(String newResourceName, Set<ResourceLoadingOption> options) {
            fail();
            return null;
        }
    }
}
