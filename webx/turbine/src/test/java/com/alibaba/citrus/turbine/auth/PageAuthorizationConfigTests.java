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

package com.alibaba.citrus.turbine.auth;

import static com.alibaba.citrus.turbine.auth.impl.PageAuthorizationServiceImpl.PageAuthorizationResult.*;
import static org.junit.Assert.*;

import com.alibaba.citrus.test.context.SpringextContextLoader;
import com.alibaba.citrus.turbine.auth.impl.PageAuthorizationServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "page-auth.xml" }, loader = SpringextContextLoader.class)
public class PageAuthorizationConfigTests extends PageAuthorizationServiceTests {
    @Autowired
    @Qualifier("pageAuthorizationService")
    private PageAuthorizationServiceImpl pageAuthorizationService;

    @Autowired
    @Qualifier("defaultAllow")
    private PageAuthorizationServiceImpl authDefaultAllow;

    @Override
    public void init() {
        assertNotNull(pageAuthorizationService);
        auth = pageAuthorizationService;
    }

    @Test
    public void defaultDeny() {
        assertFalse(auth.isAllowByDefault());

        assertSame(TARGET_NOT_MATCH, auth.authorize("/notmatch", null, null, (String[]) null));
        assertFalse(auth.isAllow("/notmatch", null, null, (String[]) null));
    }

    @Test
    public void defaultAllow() {
        assertTrue(authDefaultAllow.isAllowByDefault());

        assertSame(TARGET_NOT_MATCH, authDefaultAllow.authorize("/notmatch", null, null, (String[]) null));
        assertTrue(authDefaultAllow.isAllow("/notmatch", null, null, (String[]) null));
    }
}
