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
package com.alibaba.citrus.turbine.support;

import static com.alibaba.citrus.service.requestcontext.util.RequestContextUtil.*;
import static com.alibaba.citrus.service.uribroker.uri.URIBroker.URIType.*;
import static com.alibaba.citrus.util.ArrayUtil.*;
import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.ObjectUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

import com.alibaba.citrus.service.pull.PullService;
import com.alibaba.citrus.service.requestcontext.RequestContext;
import com.alibaba.citrus.service.requestcontext.lazycommit.LazyCommitRequestContext;
import com.alibaba.citrus.service.requestcontext.parser.CookieParser;
import com.alibaba.citrus.service.requestcontext.parser.ParameterParser;
import com.alibaba.citrus.service.requestcontext.parser.ParserRequestContext;
import com.alibaba.citrus.service.requestcontext.util.RequestContextUtil;
import com.alibaba.citrus.service.uribroker.URIBrokerService;
import com.alibaba.citrus.service.uribroker.uri.URIBroker;
import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.TurbineRunDataInternal;
import com.alibaba.citrus.turbine.uribroker.uri.TurbineURIBroker;
import com.alibaba.citrus.webx.WebxComponent;
import com.alibaba.citrus.webx.WebxException;
import com.alibaba.citrus.webx.util.WebxUtil;

/**
 * 实现<code>TurbineRunData</code>接口。
 */
public class TurbineRunDataImpl implements TurbineRunDataInternal {
    private final RequestContext topRequestContext;
    private final LazyCommitRequestContext lazyCommitRequestContext;
    private final ParserRequestContext parserRequestContext;
    private WebxComponent currentComponent;
    private String target;
    private String redirectTarget;
    private URIBroker redirectURI;
    private final Map<String, PullService> pullServices;
    private final Map<String, Context> contexts;
    private Context contextForControl;
    private boolean layoutEnabled;
    private final Parameters forwardParameters = new ForwardParametersImpl();

    public TurbineRunDataImpl(HttpServletRequest request) {
        this.topRequestContext = assertNotNull(RequestContextUtil.getRequestContext(request),
                "no request context defined in request attributes");
        this.lazyCommitRequestContext = findRequestContext(topRequestContext, LazyCommitRequestContext.class);
        this.parserRequestContext = findRequestContext(topRequestContext, ParserRequestContext.class);
        this.pullServices = createHashMap();
        this.contexts = createHashMap();
    }

    private String normalizeComponentName(String componentName) {
        componentName = trimToNull(componentName);

        if (componentName != null) {
            WebxComponent currentComponent = getCurrentComponent();

            if (componentName.equals(currentComponent.getName())) {
                componentName = null;
            }
        }

        return componentName;
    }

    private WebxComponent getCurrentComponent() {
        if (currentComponent == null) {
            currentComponent = WebxUtil.getCurrentComponent(getRequest());
        }

        return currentComponent;
    }

    private LazyCommitRequestContext getLazyCommitRequestContext() {
        return assertNotNull(lazyCommitRequestContext, "no lazyCommitRequestContext defined in request-contexts");
    }

    private ParserRequestContext getParserRequestContext() {
        return assertNotNull(parserRequestContext, "no parserRequestContext defined in request-contexts");
    }

    private PullService getPullService(String componentName) {
        componentName = normalizeComponentName(componentName);

        if (!pullServices.containsKey(componentName)) {
            WebxComponent component;

            if (componentName == null) {
                component = getCurrentComponent();
            } else {
                component = assertNotNull(getCurrentComponent().getWebxComponents().getComponent(componentName),
                        "could not find webx component: %s", componentName);
            }

            ApplicationContext context = component.getApplicationContext();
            PullService pullService;

            try {
                pullService = (PullService) context.getBean("pullService", PullService.class);
            } catch (NoSuchBeanDefinitionException e) {
                pullService = null;
            }

            pullServices.put(componentName, pullService);
        }

        return pullServices.get(componentName); // maybe null
    }

    public RequestContext getRequestContext() {
        return topRequestContext;
    }

    public HttpServletRequest getRequest() {
        return topRequestContext.getRequest();
    }

    public HttpServletResponse getResponse() {
        return topRequestContext.getResponse();
    }

    public ParameterParser getParameters() {
        return getParserRequestContext().getParameters();
    }

    public CookieParser getCookies() {
        return getParserRequestContext().getCookies();
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = trimToNull(target);
    }

    public String getRedirectTarget() {
        return redirectTarget;
    }

    public void setRedirectTarget(String redirectTarget) {
        redirectTarget = trimToNull(redirectTarget);

        // 如果target不相同，才需要重定向。
        if (!isEquals(target, redirectTarget)) {
            this.redirectTarget = redirectTarget;
        }
    }

    public String getRedirectLocation() {
        commitRedirectLocation();
        return getLazyCommitRequestContext().getRedirectLocation();
    }

    public void setRedirectLocation(String redirectLocation) {
        try {
            getResponse().sendRedirect(redirectLocation);
        } catch (IOException e) {
            throw new WebxException("Could not redirect to URI: " + redirectLocation, e);
        }
    }

    /**
     * 设置用于重定向的uri broker。该uri会在下一次检查isRedirected()时被设置到response中。
     */
    private void setRedirectLocation(URIBroker uri) {
        this.redirectURI = uri;
    }

    private void commitRedirectLocation() {
        if (redirectURI != null) {
            String uri = redirectURI.setURIType(full).render();
            redirectURI = null; // reset

            setRedirectLocation(uri);
        }
    }

    public boolean isRedirected() {
        commitRedirectLocation(); // 确保redirect uri broker被提交
        return redirectTarget != null || getLazyCommitRequestContext().isRedirected();
    }

    public Context getContext() {
        return getContext(null);
    }

    public Context getContext(String componentName) {
        componentName = normalizeComponentName(componentName);
        Context context = contexts.get(componentName);

        if (context == null) {
            PullService pullService = getPullService(componentName);

            if (pullService != null) {
                context = new PullableMappedContext(pullService.getContext());
            } else {
                context = new MappedContext();
            }

            contexts.put(componentName, context);
        }

        return context;
    }

    public Context getContextForControl() {
        return contextForControl;
    }

    public void setContextForControl(Context parentContext) {
        this.contextForControl = parentContext;
    }

    public boolean isLayoutEnabled() {
        return layoutEnabled;
    }

    public void setLayoutEnabled(boolean enabled) {
        this.layoutEnabled = enabled;
    }

    public Parameters forwardTo(String target) {
        setRedirectTarget(target);
        return forwardParameters;
    }

    public RedirectParameters redirectTo(String uriName) {
        uriName = assertNotNull(trimToNull(uriName), "no uriName");

        URIBrokerService uris = (URIBrokerService) getCurrentComponent().getApplicationContext().getBean(
                "uriBrokerService", URIBrokerService.class);

        URIBroker uri = assertNotNull(uris.getURIBroker(uriName), "could not find uri broker named \"%s\"", uriName);

        setRedirectLocation(uri);

        return new RedirectParametersImpl(uriName, uri);
    }

    /**
     * 进行外部重定向，指定一个完整的URL location。
     */
    public void redirectToLocation(String location) {
        setRedirectLocation(location);
    }

    private class ForwardParametersImpl implements Parameters {
        public Parameters withParameter(String name, String... values) {
            if (!isEmptyArray(values)) {
                getParameters().setStrings(name, values);
            }

            return this;
        }

        @Override
        public String toString() {
            return "forwardTo(" + getRedirectTarget() + ") " + getParameters();
        }
    }

    private class RedirectParametersImpl implements RedirectParameters {
        private final String uriName;
        private final URIBroker uri;

        public RedirectParametersImpl(String uriName, URIBroker uri) {
            this.uriName = uriName;
            this.uri = uri;
        }

        public RedirectParameters withTarget(String target) {
            assertTrue(uri instanceof TurbineURIBroker, "URI is not a turbine-uri: %s", uriName);
            ((TurbineURIBroker) uri).setTarget(target);
            return this;
        }

        public Parameters withParameter(String name, String... values) {
            uri.setQueryData(name, values);
            return this;
        }

        public URIBroker uri() {
            return uri;
        }

        @Override
        public String toString() {
            return "redirectTo(" + uri + ")";
        }
    }
}
