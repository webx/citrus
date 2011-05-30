package com.alibaba.citrus.dev.handler.impl;

import static com.alibaba.citrus.util.CollectionUtil.*;

import java.io.IOException;
import java.util.Map;

import com.alibaba.citrus.dev.handler.impl.visitor.BeansVisitor;
import com.alibaba.citrus.dev.handler.impl.visitor.ConfigurationsVisitor;
import com.alibaba.citrus.dev.handler.impl.visitor.ResolvableDepsVisitor;
import com.alibaba.citrus.dev.handler.impl.visitor.ResourcesExplorerVisitor;
import com.alibaba.citrus.dev.handler.impl.visitor.UrisExplorerVisitor;
import com.alibaba.citrus.dev.handler.util.ConfigurationFileReader;
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
            return new ResourcesExplorerVisitor(context, this, getService("resourceLoadingService",
                    ResourceLoadingService.class));
        }

        public Object visitUris(Template urisTemplate) {
            return new UrisExplorerVisitor(context, this, getService("uriBrokerService", URIBrokerService.class));
        }
    }
}
