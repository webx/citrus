package com.alibaba.citrus.test.context;

import org.springframework.context.ApplicationContext;
import org.springframework.util.StringUtils;

import com.alibaba.citrus.service.resource.support.context.ResourceLoadingXmlApplicationContext;
import com.alibaba.citrus.springext.support.context.AbstractXmlApplicationContext;

/**
 * 用来创建基于springext的context。
 * 
 * @author Michael Zhou
 */
public class SpringextContextLoader extends AbstractContextLoader {
    public final ApplicationContext loadContext(String... locations) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Loading ApplicationContext for locations [" + StringUtils.arrayToCommaDelimitedString(locations)
                    + "].");
        }

        ResourceLoadingXmlApplicationContext context = new ResourceLoadingXmlApplicationContext(locations,
                testResourceLoader, false);

        prepareContext(context);
        context.refresh();
        context.registerShutdownHook();

        return context;
    }

    protected void prepareContext(AbstractXmlApplicationContext context) {
    }
}
