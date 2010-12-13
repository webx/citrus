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
package com.alibaba.citrus.service.velocity;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import com.alibaba.citrus.service.resource.ResourceFilter;
import com.alibaba.citrus.service.resource.ResourceFilterChain;
import com.alibaba.citrus.service.resource.ResourceLoadingOption;
import com.alibaba.citrus.service.resource.ResourceLoadingService;
import com.alibaba.citrus.service.resource.ResourceMatchResult;
import com.alibaba.citrus.service.resource.ResourceNotFoundException;
import com.alibaba.citrus.service.resource.support.InputStreamResource;
import com.alibaba.citrus.util.io.StreamUtil;

public abstract class AbstractResourceLoaderTests {
    protected final String readText(InputStream stream) throws IOException {
        return StreamUtil.readText(stream, null, true);
    }

    /**
     * ³ýÈ¥resource URLµÄfilter¡£
     */
    public static class NoURLFilter implements ResourceFilter {
        public void init(ResourceLoadingService resourceLoadingService) {
        }

        public com.alibaba.citrus.service.resource.Resource doFilter(ResourceMatchResult filterMatchResult,
                                                                     Set<ResourceLoadingOption> options,
                                                                     ResourceFilterChain chain)
                throws ResourceNotFoundException {
            com.alibaba.citrus.service.resource.Resource resource = chain.doFilter(filterMatchResult, options);

            try {
                return new InputStreamResource(resource.getInputStream());
            } catch (IOException e) {
                fail();
                return null;
            }
        }
    }
}
