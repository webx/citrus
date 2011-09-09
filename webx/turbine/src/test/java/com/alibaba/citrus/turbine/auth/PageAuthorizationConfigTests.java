package com.alibaba.citrus.turbine.auth;

import static org.junit.Assert.*;

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
    private PageAuthorizationService pageAuthorizationService;

    @Override
    public void init() {
        assertNotNull(pageAuthorizationService);
        auth = (PageAuthorizationServiceImpl) pageAuthorizationService;
    }
}
