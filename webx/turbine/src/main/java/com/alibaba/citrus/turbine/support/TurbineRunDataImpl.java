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

package com.alibaba.citrus.turbine.support;

import static com.alibaba.citrus.service.requestcontext.util.RequestContextUtil.*;
import static com.alibaba.citrus.service.uribroker.uri.URIBroker.URIType.*;
import static com.alibaba.citrus.util.ArrayUtil.*;
import static com.alibaba.citrus.util.Assert.ExceptionType.*;
import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.ObjectUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
import com.alibaba.citrus.util.internal.ActionEventUtil;
import com.alibaba.citrus.webx.WebxComponent;
import com.alibaba.citrus.webx.WebxException;
import com.alibaba.citrus.webx.util.WebxUtil;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

/** 实现<code>TurbineRunData</code>接口。 */
public class TurbineRunDataImpl implements TurbineRunDataInternal {
    private final RequestContext           topRequestContext;
    private final LazyCommitRequestContext lazyCommitRequestContext;
    private final ParserRequestContext     parserRequestContext;
    private final Map<String, PullService> pullServices;
    private final Map<String, Context>     contexts;

    private final Parameters   forwardParameters = new ForwardParametersImpl();
    private final ModuleTraces moduleTraces      = new ModuleTraces();

    private TargetTuple targetTuple = new TargetTuple();
    private TargetTuple redirectTargetTuple;

    private WebxComponent currentComponent;
    private boolean       layoutEnabled;
    private String        layoutTemplateOverride;
    private URIBroker     redirectURI;

    public TurbineRunDataImpl(HttpServletRequest request) {
        this(request, null);
    }

    /** 创建一个turbine rundata，使用指定的context。 这种形式用于创建error pipeline的rundata对象。 */
    public TurbineRunDataImpl(HttpServletRequest request, Context context) {
        this.topRequestContext = assertNotNull(RequestContextUtil.getRequestContext(request),
                                               "no request context defined in request attributes");
        this.lazyCommitRequestContext = findRequestContext(topRequestContext, LazyCommitRequestContext.class);
        this.parserRequestContext = findRequestContext(topRequestContext, ParserRequestContext.class);
        this.pullServices = createHashMap();
        this.contexts = createHashMap();

        if (context != null) {
            // 将context中的内容复制到新的context中，但是不要包含pull tools。
            Context newContext = getContext();
            Set<String> keys;

            if (context instanceof PullableMappedContext) {
                keys = ((PullableMappedContext) context).keySetWithoutPulling();
            } else {
                keys = context.keySet();
            }

            for (String key : keys) {
                newContext.put(key, context.get(key));
            }
        }
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
        return targetTuple.getTarget();
    }

    public void setTarget(String target) {
        targetTuple.setTarget(target);
    }

    public String getAction() {
        return targetTuple.getAction();
    }

    public void setAction(String action) {
        targetTuple.setAction(action);
    }

    public String getActionEvent() {
        return targetTuple.getActionEvent();
    }

    public void setActionEvent(String actionEvent) {
        targetTuple.setActionEvent(actionEvent);
    }

    public String getRedirectTarget() {
        return redirectTargetTuple == null ? null : redirectTargetTuple.getTarget();
    }

    public void setRedirectTarget(String redirectTarget) {
        setRedirectTargetAndAction(redirectTarget, null, null);
    }

    private void setRedirectTargetAndAction(String redirectTarget, String redirectAction, String redirectActionEvent) {
        redirectTarget = trimToNull(redirectTarget);

        // 如果target不相同，才需要重定向。
        if (!isEquals(getTarget(), redirectTarget)) {
            redirectTargetTuple = new TargetTuple()
                    .setTarget(redirectTarget).setAction(redirectAction).setActionEvent(redirectActionEvent);
        }
    }

    public boolean doRedirectTarget() {
        if (redirectTargetTuple != null) {
            targetTuple = redirectTargetTuple;
            redirectTargetTuple = null;

            // 设置request，以便 ActionEventAdapter 不借助rundata也可取得actionEvent值。
            ActionEventUtil.setEventName(getRequest(), getActionEvent());

            return true;
        } else {
            return false;
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

    /** 设置用于重定向的uri broker。该uri会在下一次检查isRedirected()时被设置到response中。 */
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
        return redirectTargetTuple != null || getLazyCommitRequestContext().isRedirected();
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

    public Context getCurrentContext() {
        if (moduleTraces.isEmpty()) {
            return null;
        } else {
            return moduleTraces.getLast().getContext();
        }
    }

    public void pushContext(Context context) {
        pushContext(context, null);
    }

    public void pushContext(Context context, String template) {
        moduleTraces.addLast(new ModuleTrace(context, template));
    }

    public Context popContext() throws IllegalStateException {
        assertTrue(!moduleTraces.isEmpty(), ILLEGAL_STATE, "can't popContext without pushContext");
        return moduleTraces.removeLast().getContext();
    }

    public String getControlTemplate() {
        if (moduleTraces.isEmpty()) {
            return null;
        } else {
            return moduleTraces.getLast().getTemplate();
        }
    }

    public void setControlTemplate(String template) {
        assertTrue(!moduleTraces.isEmpty(), ILLEGAL_STATE, "can't setControlTemplate without pushContext");
        moduleTraces.getLast().setTemplate(template);
    }

    public boolean isLayoutEnabled() {
        return layoutEnabled;
    }

    public void setLayoutEnabled(boolean enabled) {
        this.layoutEnabled = enabled;
    }

    /**
     * 明确指定layout模板，覆盖默认的layout规则。 注意如果指定了layout，则<code>layoutEnabled</code>
     * 将被设置成<code>true</code>。
     */
    public void setLayout(String layoutTemplate) {
        layoutTemplateOverride = trimToNull(layoutTemplate);

        if (layoutTemplateOverride != null) {
            setLayoutEnabled(true);
        }
    }

    /** 取得明确指定的layout模板。 */
    public String getLayoutTemplateOverride() {
        return layoutTemplateOverride;
    }

    public Parameters forwardTo(String target) {
        setRedirectTarget(target);
        return forwardParameters;
    }

    public Parameters forwardTo(String target, String action, String actionEvent) {
        setRedirectTargetAndAction(target, action, actionEvent);
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

    /** 进行外部重定向，指定一个完整的URL location。 */
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

        public void end() {
        }

        @Override
        public String toString() {
            return "forwardTo(" + getRedirectTarget() + ") " + getParameters();
        }
    }

    private class RedirectParametersImpl implements RedirectParameters {
        private final String    uriName;
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

        public void end() {
            commitRedirectLocation();
        }

        @Override
        public String toString() {
            return "redirectTo(" + uri + ")";
        }
    }

    /** 代表module的调用栈。 */
    private class ModuleTraces extends LinkedList<ModuleTrace> {
        private static final long serialVersionUID = 8167955929944105578L;
    }

    /** 代表一个module调用的信息。 */
    private class ModuleTrace {
        private final Context context;
        private       String  template;

        public ModuleTrace(Context context, String template) {
            this.context = assertNotNull(context, "context");
            this.template = template;
        }

        public Context getContext() {
            return context;
        }

        public String getTemplate() {
            return template;
        }

        public void setTemplate(String template) {
            this.template = template;
        }
    }

    private static class TargetTuple {
        private String target;
        private String action;
        private String actionEvent;

        public String getTarget() {
            return target;
        }

        public TargetTuple setTarget(String target) {
            this.target = trimToNull(target);
            return this;
        }

        public String getAction() {
            return action;
        }

        public TargetTuple setAction(String action) {
            this.action = trimToNull(action);
            return this;
        }

        public String getActionEvent() {
            return actionEvent;
        }

        public TargetTuple setActionEvent(String actionEvent) {
            this.actionEvent = trimToNull(actionEvent);
            return this;
        }

        @Override
        public String toString() {
            return String.format("target=%s, action=%s, actionEvent=%s", target, action, actionEvent);
        }
    }
}
