package com.alibaba.citrus.turbine.auth;

import static com.alibaba.citrus.turbine.auth.impl.PageAuthorizationServiceImpl.PageAuthorizationResult.*;
import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.alibaba.citrus.test.context.SpringextContextLoader;
import com.alibaba.citrus.turbine.auth.impl.PageAuthorizationServiceImpl;

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
