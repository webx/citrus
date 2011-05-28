package com.alibaba.citrus.dev.handler.impl;

import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.ClassUtil.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.FileUtil.*;
import static com.alibaba.citrus.util.StringEscapeUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;
import static java.util.Collections.*;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alibaba.citrus.service.resource.Resource;
import com.alibaba.citrus.service.resource.ResourceLoadingService;
import com.alibaba.citrus.service.resource.ResourceNotFoundException;
import com.alibaba.citrus.service.resource.ResourceTrace;
import com.alibaba.citrus.service.resource.ResourceTraceElement;
import com.alibaba.citrus.service.uribroker.URIBrokerService;
import com.alibaba.citrus.service.uribroker.uri.URIBroker;
import com.alibaba.citrus.util.IllegalPathException;
import com.alibaba.citrus.util.templatelite.Template;
import com.alibaba.citrus.webx.handler.RequestHandlerContext;

public class ServiceExplorerHandler extends AbstractExplorerHandler {
    private static final String FN_RESOURCES = "ResourceLoading";
    private static final String FN_URIS = "URIs";
    private static final Map<String, String> AVAILABLE_FUNCTIONS = createLinkedHashMap();

    static {
        AVAILABLE_FUNCTIONS.put(FN_RESOURCES, "Resources");
        AVAILABLE_FUNCTIONS.put(FN_URIS, "URIs");
    }

    @Override
    protected String getDefaultFunction() {
        return FN_RESOURCES;
    }

    @Override
    protected Map<String, String> getAvailableFunctions() {
        return AVAILABLE_FUNCTIONS;
    }

    @Override
    protected String[] getStyleSheets() {
        return new String[] { "serviceExplorer.css" };
    }

    @Override
    protected String[] getJavaScripts() {
        return new String[] { "serviceExplorer.js" };
    }

    @Override
    protected ServiceExplorerVisitor getBodyVisitor(RequestHandlerContext context) {
        return new ServiceExplorerVisitor(context);
    }

    @SuppressWarnings("unused")
    private class ServiceExplorerVisitor extends AbstractExplorerVisitor {
        public ServiceExplorerVisitor(RequestHandlerContext context) {
            super(context);
        }

        public Object visitResources(Template resourcesTemplate) {
            return new ResourcesExplorerVisitor(context, this, getService("resourceLoadingService",
                    ResourceLoadingService.class));
        }

        public Object visitUris(Template urisTemplate) {
            return new UrisExplorerVisitor(context, this, getService("uriBrokerService", URIBrokerService.class));
        }
    }

    @SuppressWarnings("unused")
    private class ResourcesExplorerVisitor extends AbstractFallbackVisitor {
        private final ResourceLoadingService resourceLoadingService;
        private final String resourceName;
        private String title;
        private String content;

        public ResourcesExplorerVisitor(RequestHandlerContext context, ServiceExplorerVisitor v,
                                        ResourceLoadingService resourceLoadingService) {
            super(context, v);
            this.resourceLoadingService = assertNotNull(resourceLoadingService, "resourceLoadingService");

            String resourceName;

            try {
                resourceName = defaultIfEmpty(normalizeAbsolutePath(context.getRequest().getParameter("resource")), "/");
            } catch (IllegalPathException e) {
                resourceName = "/";
            }

            this.resourceName = resourceName;
        }

        public void visitService(Template serviceNotAvailableTemplate, Template serviceTemplate) {
            if (resourceLoadingService == null) {
                serviceNotAvailableTemplate.accept(this);
            } else {
                serviceTemplate.accept(this);
            }
        }

        public void visitResourceName() {
            out().print(escapeHtml(resourceName));
        }

        private ResourceTraceElement traceElement;
        private Resource result;

        public void visitTraceResource(Template traceResourceTemplate) {
            traceResourceTemplate.accept(this);
        }

        public void visitTrace(Template traceElementTemplate, Template resultTemplate, Template resultNotExistTemplate,
                               Template resultNotFoundTemplate) {
            ResourceTrace trace = resourceLoadingService.trace(resourceName, ResourceLoadingService.FOR_CREATE);

            for (ResourceTraceElement traceElement : trace) {
                this.traceElement = traceElement;
                traceElementTemplate.accept(this);
            }

            result = trace.getResult();

            if (result == null) {
                resultNotFoundTemplate.accept(this);
            } else if (!result.exists()) {
                resultNotExistTemplate.accept(this);
            } else {
                resultTemplate.accept(this);
            }
        }

        public void visitTrace(String param) {
            if ("resourceName".equals(param)) {
                out().print(escapeHtml(traceElement.getResourceName()));
            } else if ("patternType".equals(param)) {
                out().print(escapeHtml(traceElement.getPatternType()));
            } else if ("patternName".equals(param)) {
                out().print(escapeHtml(traceElement.getPatternName()));
            } else if ("serviceName".equals(param)) {
                out().print(escapeHtml(traceElement.getBeanName()));
            } else if ("serviceLocation".equals(param)) {
                out().print(escapeHtml(traceElement.getConfigLocation()));
            } else if ("serviceLocationShort".equals(param)) {
                out().print(escapeHtml(traceElement.getShortLocation()));
            } else {
                unreachableCode();
            }
        }

        public void visitInternal(Template internalTemplate) {
            if (traceElement.isInternalPattern()) {
                internalTemplate.accept(this);
            }
        }

        public void visitResult() {
            out().print(escapeHtml(result.toString()));
        }

        private List<String> subResourceNames;
        private String fullSubResourceName;
        private String relativeSubResourceName;

        public void visitSubResources(Template subResourcesTemplate) {
            try {
                subResourceNames = createArrayList(resourceLoadingService.list(resourceName,
                        ResourceLoadingService.FOR_CREATE));
            } catch (ResourceNotFoundException e) {
                subResourceNames = createArrayList();
            }

            Collections.sort(subResourceNames, new Comparator<String>() {
                public int compare(String o1, String o2) {
                    int d1 = o1.endsWith("/") ? 0 : 1;
                    int d2 = o2.endsWith("/") ? 0 : 1;

                    if (d1 == d2) {
                        return o1.compareTo(o2);
                    } else {
                        return d1 - d2;
                    }
                }
            });

            try {
                normalizeAbsolutePath(resourceName + "/..");
                subResourceNames.add(0, "..");
            } catch (IllegalPathException e) {
            }

            subResourcesTemplate.accept(this);
        }

        public void visitSubResource(Template subResourceTemplate) {
            for (String subResourceName : subResourceNames) {
                fullSubResourceName = normalizeAbsolutePath(resourceName + "/" + subResourceName);
                relativeSubResourceName = subResourceName;

                subResourceTemplate.accept(this);
            }
        }

        public void visitSubResourceName(String type) {
            if ("relative".equals(type)) {
                out().print(escapeHtml(relativeSubResourceName));
            } else {
                out().print(escapeHtml(fullSubResourceName));
            }
        }

        private String pattern;

        public void visitPatterns(Template patternsTemplate) {
            patternsTemplate.accept(this);
        }

        public void visitPattern(Template patternTemplate) {
            for (String pattern : resourceLoadingService.getPatterns(true)) {
                this.pattern = pattern;
                patternTemplate.accept(this);
            }
        }

        public void visitPattern() {
            out().print(escapeHtml(pattern));
        }

        public void visitTitle() {
            out().print(escapeHtml(title));
        }

        public void visitBoxContent() {
            out().print(escapeHtml(content));
        }
    }

    @SuppressWarnings("unused")
    private class UrisExplorerVisitor extends AbstractFallbackVisitor {
        private final URIBrokerService uris;
        private URIBroker uri;
        private boolean exposed;
        private String name;

        public UrisExplorerVisitor(RequestHandlerContext context, ServiceExplorerVisitor v, URIBrokerService uris) {
            super(context, v);
            this.uris = uris;
        }

        public void visitService(Template serviceNotAvailableTemplate, Template serviceTemplate) {
            if (uris == null) {
                serviceNotAvailableTemplate.accept(this);
            } else {
                serviceTemplate.accept(this);
            }
        }

        public void visitUri(Template uriTemplate) {
            final Set<String> exposedNames = createHashSet(uris.getExposedNames());
            List<String> names = createArrayList(uris.getNames());

            sort(names, new Comparator<String>() {
                public int compare(String o1, String o2) {
                    int t1 = exposedNames.contains(o1) ? 0 : 1;
                    int t2 = exposedNames.contains(o2) ? 0 : 1;

                    if (t1 != t2) {
                        return t1 - t2;
                    } else {
                        return o1.compareTo(o2);
                    }
                }
            });

            for (String name : names) {
                this.uri = uris.getURIBroker(name);
                this.exposed = exposedNames.contains(name);
                this.name = name;

                uriTemplate.accept(this);
            }
        }

        public void visitUriName(Template exposedTemplate, Template hiddenTemplate) {
            if (exposed) {
                exposedTemplate.accept(this);
            } else {
                hiddenTemplate.accept(this);
            }
        }

        public void visitName() {
            out().print(name);
        }

        public void visitValueTypePackage() {
            out().print(uri.getClass().getPackage().getName() + ".");
        }

        public void visitValueTypeName() {
            out().print(getSimpleClassName(uri.getClass(), false));
        }

        public void visitValue() {
            out().print(uri.toString());
        }

        public void visitHidden(Template hiddenTemplate) {
            if (!exposed) {
                hiddenTemplate.accept(this);
            }
        }
    }
}
