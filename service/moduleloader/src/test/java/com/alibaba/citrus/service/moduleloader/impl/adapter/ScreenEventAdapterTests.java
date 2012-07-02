/**
 *
 */

package com.alibaba.citrus.service.moduleloader.impl.adapter;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.citrus.service.AbstractWebTests;
import com.alibaba.citrus.service.moduleloader.Module;
import com.alibaba.citrus.service.moduleloader.ModuleLoaderService;
import org.junit.Test;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;

/** @author qianchao */
public class ScreenEventAdapterTests extends AbstractWebTests {

    @Test
    public void screenEventAdapterTest() {
        // 创建不包含request-contexts对象的spring container
        ApplicationContext factory = createContext("adapter/services-annotation.xml", null);
        HttpServletRequest request = createMock(HttpServletRequest.class);

        // 注册mock request
        ((ConfigurableListableBeanFactory) factory.getAutowireCapableBeanFactory()).registerResolvableDependency(
                HttpServletRequest.class, request);

        ModuleLoaderService moduleLoader = (ModuleLoaderService) factory.getBean("moduleLoaderService");

        Module module = moduleLoader.getModule("screen", "testScreen");
        assertNotNull(module);
        assertTrue("module is no handle module", module instanceof HandlerModule);
        HandlerModule handler = (HandlerModule) module;
        assertNotNull(handler.getExecuteHandler());
        assertTrue(handler.getHandlers().size() == 2);
    }
}
