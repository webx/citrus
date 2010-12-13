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
package com.alibaba.citrus.service.requestcontext.session.impl;

import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.ObjectUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;
import static java.util.Collections.*;

import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.citrus.service.requestcontext.session.SessionAttributeInterceptor;
import com.alibaba.citrus.service.requestcontext.session.SessionConfig;
import com.alibaba.citrus.service.requestcontext.session.SessionInterceptor;
import com.alibaba.citrus.service.requestcontext.session.SessionLifecycleListener;
import com.alibaba.citrus.service.requestcontext.session.SessionModel;
import com.alibaba.citrus.service.requestcontext.session.SessionModelEncoder;
import com.alibaba.citrus.service.requestcontext.session.SessionRequestContext;
import com.alibaba.citrus.service.requestcontext.session.SessionStore;
import com.alibaba.citrus.service.requestcontext.session.SessionStore.StoreContext;
import com.alibaba.citrus.util.internal.ToStringBuilder;
import com.alibaba.citrus.util.internal.ToStringBuilder.MapBuilder;

/**
 * 实现了<code>HttpSession</code>接口。
 * <p>
 * 注意，每个request均会创建独立的session对象，此对象本身不是线程安全的，不能被多线程同时访问。但其后备的session
 * store是线程安全的。
 * </p>
 */
public class SessionImpl implements HttpSession {
    private final static Logger log = LoggerFactory.getLogger(SessionImpl.class);
    private final HttpSessionInternal sessionInternal = new HttpSessionInternal();
    private String sessionID;
    private SessionRequestContext requestContext;
    private String modelKey;
    private SessionModelImpl model;
    private boolean isNew;
    private Map<String, SessionAttribute> attrs = createHashMap();
    private Map<String, Object> storeStates = createHashMap();
    private boolean invalidated = false;
    private boolean cleared = false;

    /**
     * 创建一个session对象。
     */
    public SessionImpl(String sessionID, SessionRequestContext requestContext, boolean isNew, boolean create) {
        this.sessionID = assertNotNull(sessionID, "no sessionID");
        this.requestContext = requestContext;
        this.modelKey = requestContext.getSessionConfig().getModelKey();

        EventType event;

        // 进到这里的session可能有四种情况：
        // 1. Requested sessionID为空
        // 2. Requested sessionID所对应的session不存在
        // 3. Requested sessionID所对应的session已经过期
        // 3.5 Requested sessionID和model中的session ID不匹配，视作session过期
        // 4. Requested sessionID所对应的session存在且合法
        if (isNew) {
            event = EventType.CREATED;

            // 情况1：创建新的model，并保存之。
            log.debug("No session ID was found in cookie or URL.  A new session will be created.");
            sessionInternal.invalidate();
        } else {
            model = (SessionModelImpl) sessionInternal.getAttribute(modelKey);

            if (model == null) {
                event = EventType.CREATED;

                // 情况2：创建新的model，并保存之。
                log.debug("Session state was not found for sessionID \"{}\".  A new session will be created.",
                        sessionID);
                isNew = true;
                sessionInternal.invalidate();
            } else {
                boolean expired = false;

                String modelSessionID = trimToNull(model.getSessionID());

                // 检查SessionID的值是否相等，并兼容SessionModel中没有设置SessionID的场景
                // 特殊情况：model中未包含session ID，这时，不作检查，以request中的sessionID为准
                if (modelSessionID != null && !modelSessionID.equals(sessionID)) {
                    // 情况3.5 视作过期
                    expired = true;

                    log.warn("Requested session ID \"{}\" does not match the ID in session model \"{}\".  "
                            + "Force expired the session.", sessionID, modelSessionID);
                }

                // Session model被返回前，会先被decode。因此，修改所返回的session model对象，并不会影响store中的对象值。
                model.setSession(this); // update the session config & session id in model

                expired |= model.isExpired();

                if (expired) {
                    event = EventType.RECREATED;

                    // 情况3：更新model如同新建的一样，同时清除老数据。
                    if (log.isDebugEnabled()) {
                        log.debug(
                                "Session has expired: sessionID={}, created at {}, last accessed at {}, "
                                        + "maxInactiveInterval={}, forceExpirationPeriod={}",
                                new Object[] { modelSessionID, new Date(model.getCreationTime()),
                                        new Date(model.getLastAccessedTime()), model.getMaxInactiveInterval(),
                                        getSessionRequestContext().getSessionConfig().getForceExpirationPeriod() });
                    }

                    isNew = true;
                    sessionInternal.invalidate();
                } else {
                    event = EventType.VISITED;

                    // 情况4：更新model的最近访问时间。
                    if (log.isTraceEnabled()) {
                        log.trace(
                                "Activate session: sessionID={}, last accessed at {}, maxInactiveInterval={}",
                                new Object[] { modelSessionID, new Date(model.getLastAccessedTime()),
                                        model.getMaxInactiveInterval() });
                    }

                    model.touch();
                }
            }
        }

        this.isNew = isNew;

        // 确保model attribute的modified=true
        sessionInternal.setAttribute(modelKey, model);

        // 调用session lifecycle listener
        fireEvent(event);
    }

    /**
     * 取得创建该session的request context。
     * 
     * @return request context
     */
    public SessionRequestContext getSessionRequestContext() {
        return requestContext;
    }

    /**
     * 取得当前的model。
     * 
     * @return model对象
     */
    public SessionModel getSessionModel() {
        return model;
    }

    /**
     * 取得session ID。
     * 
     * @return session ID
     */
    public String getId() {
        return sessionID;
    }

    /**
     * 取得session的创建时间。
     * 
     * @return 创建时间戮
     * @throws IllegalStateException 如果session已经invalidated
     */
    public long getCreationTime() {
        assertValid("getCreationTime");
        return sessionInternal.getCreationTime();
    }

    /**
     * 取得最近访问时间。
     * 
     * @return 最近访问时间戮
     * @throws IllegalStateException 如果session已经invalidated
     */
    public long getLastAccessedTime() {
        assertValid("getLastAccessedTime");
        return model.getLastAccessedTime();
    }

    /**
     * 取得session的最大不活动期限，超过此时间，session就会失效。
     * 
     * @return 不活动期限的秒数
     */
    public int getMaxInactiveInterval() {
        assertModel("getMaxInactiveInterval");
        return model.getMaxInactiveInterval();
    }

    /**
     * 设置session的最大不活动期限，超过此时间，session就会失效。
     * 
     * @param maxInactiveInterval 不活动期限的秒数
     */
    public void setMaxInactiveInterval(int maxInactiveInterval) {
        assertModel("setMaxInactiveInterval");
        model.setMaxInactiveInterval(maxInactiveInterval);
    }

    /**
     * 取得当前session所属的servlet context。
     * 
     * @return <code>ServletContext</code>对象
     */
    public ServletContext getServletContext() {
        return requestContext.getServletContext();
    }

    /**
     * 取得指定名称的attribute值。
     * 
     * @param name attribute名称
     * @return attribute的值
     * @throws IllegalStateException 如果session已经invalidated
     */
    public Object getAttribute(String name) {
        assertValid("getAttribute");
        return sessionInternal.getAttribute(name);
    }

    /**
     * 取得所有attributes的名称。
     * 
     * @return attribute名称列表
     * @throws IllegalStateException 如果session已经invalidated
     */
    public Enumeration<String> getAttributeNames() {
        assertValid("getAttributeNames");

        Set<String> attrNames = getAttributeNameSet();

        final Iterator<String> i = attrNames.iterator();

        return new Enumeration<String>() {
            public boolean hasMoreElements() {
                return i.hasNext();
            }

            public String nextElement() {
                return i.next();
            }
        };
    }

    private Set<String> getAttributeNameSet() {
        SessionConfig sessionConfig = requestContext.getSessionConfig();
        String[] storeNames = sessionConfig.getStores().getStoreNames();
        Set<String> attrNames = createLinkedHashSet();

        for (String storeName : storeNames) {
            SessionStore store = sessionConfig.getStores().getStore(storeName);

            for (String attrName : store.getAttributeNames(getId(), new StoreContextImpl(storeName))) {
                if (!isEquals(attrName, modelKey)) {
                    attrNames.add(attrName);
                }
            }
        }

        for (SessionAttribute attr : attrs.values()) {
            if (attr.getValue() == null) {
                attrNames.remove(attr.getName());
            } else {
                attrNames.add(attr.getName());
            }
        }

        attrNames.remove(modelKey);

        return attrNames;
    }

    /**
     * 设置指定名称的attribute值。
     * 
     * @param name attribute名称
     * @param value attribute的值
     * @throws IllegalStateException 如果session已经invalidated
     * @throws IllegalArgumentException 如果指定的attribute名称不被支持
     */
    public void setAttribute(String name, Object value) {
        assertValid("setAttribute");
        assertAttributeNameForModification("setAttribute", name);
        sessionInternal.setAttribute(name, value);
    }

    /**
     * 删除一个attribute。
     * 
     * @param name 要删除的attribute名称
     * @throws IllegalStateException 如果session已经invalidated
     */
    public void removeAttribute(String name) {
        assertValid("removeAttribute");
        assertAttributeNameForModification("removeAttribute", name);
        setAttribute(name, null);
    }

    /**
     * 使一个session作废。
     * 
     * @throws IllegalStateException 如果session已经invalidated
     */
    public void invalidate() {
        assertValid("invalidate");
        sessionInternal.invalidate();
        invalidated = true;

        fireEvent(EventType.INVALIDATED);
    }

    /**
     * 清除一个session。
     * 
     * @throws IllegalStateException 如果session已经invalidated
     */
    public void clear() {
        assertValid("clear");
        sessionInternal.invalidate();
    }

    /**
     * 判断当前session是否非法。
     */
    public boolean isInvalidated() {
        return invalidated;
    }

    /**
     * 当前session是否为新的？
     * 
     * @return 如果是新的，则返回<code>true</code>
     * @throws IllegalStateException 如果session已经invalidated
     */
    public boolean isNew() {
        assertValid("isNew");
        return isNew;
    }

    /**
     * 确保model已经被取得，即session已被初始化。
     * 
     * @param methodName 当前正要执行的方法
     */
    protected void assertModel(String methodName) {
        if (model == null) {
            throw new IllegalStateException("Cannot call method " + methodName
                    + ": the session has not been initialized");
        }
    }

    /**
     * 确保session处于valid状态。
     * 
     * @param methodName 当前正要执行的方法
     */
    protected void assertValid(String methodName) {
        assertModel(methodName);

        if (invalidated) {
            throw new IllegalStateException("Cannot call method " + methodName
                    + ": the session has already invalidated");
        }
    }

    /**
     * 检查将要更改的attr name是否合法。
     */
    protected void assertAttributeNameForModification(String methodName, String attrName) {
        if (modelKey.equals(attrName)) {
            throw new IllegalArgumentException("Cannot call method " + methodName + " with attribute " + attrName);
        }
    }

    /**
     * 提交session的内容，删除的、新增的、修改的内容被保存。
     */
    public void commit() {
        String[] storeNames = requestContext.getSessionConfig().getStores().getStoreNames();
        Map<String, Object[]> stores = createHashMap();

        // 按store对attrs进行分堆。
        boolean modified = false;

        for (Map.Entry<String, SessionAttribute> entry : attrs.entrySet()) {
            String attrName = entry.getKey();
            SessionAttribute attr = entry.getValue();

            if (attr.isModified()) {
                String storeName = attr.getStoreName();
                SessionStore store = attr.getStore();
                Object[] storeInfo = stores.get(storeName);

                if (storeInfo == null) {
                    storeInfo = new Object[] { store, createHashMap() };
                    stores.put(storeName, storeInfo);
                }

                @SuppressWarnings("unchecked")
                Map<String, Object> storeAttrs = (Map<String, Object>) storeInfo[1];
                Object attrValue = attr.getValue();

                // 特殊处理model，将其转换成store中的值。
                if (attrValue instanceof SessionModel) {
                    attrValue = requestContext.getSessionConfig().getSessionModelEncoders()[0]
                            .encode((SessionModel) attrValue);
                } else {
                    // 只检查非session model对象的modified状态
                    modified = true;
                }

                storeAttrs.put(attrName, attrValue);
            }
        }

        // 如果既没有参数改变（即没有调用setAttribute和removeAttribute），
        // 也没有被清除（即没有调用invalidate和clear），并且isKeepInTouch=false，
        // 则不提交了，直接退出。
        if (!modified && !cleared && !requestContext.getSessionConfig().isKeepInTouch()) {
            return;
        }

        // 对每一个store分别操作。
        for (Map.Entry<String, Object[]> entry : stores.entrySet()) {
            String storeName = entry.getKey();
            SessionStore store = (SessionStore) entry.getValue()[0];

            @SuppressWarnings("unchecked")
            Map<String, Object> storeAttrs = (Map<String, Object>) entry.getValue()[1];

            store.commit(storeAttrs, getId(), new StoreContextImpl(storeName));
        }

        // 检查遗漏的store，并提交之
        if (storeNames.length > stores.size()) {
            for (String storeName : storeNames) {
                if (!stores.containsKey(storeName)) {
                    SessionStore store = requestContext.getSessionConfig().getStores().getStore(storeName);
                    Map<String, Object> storeAttrs = emptyMap();

                    store.commit(storeAttrs, sessionID, new StoreContextImpl(storeName));
                }
            }
        }
    }

    /**
     * @deprecated no replacement
     */
    @Deprecated
    public javax.servlet.http.HttpSessionContext getSessionContext() {
        throw new UnsupportedOperationException("No longer supported method: getSessionContext");
    }

    /**
     * @deprecated use getAttribute instead
     */
    @Deprecated
    public Object getValue(String name) {
        return getAttribute(name);
    }

    /**
     * @deprecated use getAttributeNames instead
     */
    @Deprecated
    public String[] getValueNames() {
        assertValid("getValueNames");

        Set<String> names = getAttributeNameSet();

        return names.toArray(new String[names.size()]);
    }

    /**
     * @deprecated use setAttribute instead
     */
    @Deprecated
    public void putValue(String name, Object value) {
        setAttribute(name, value);
    }

    /**
     * @deprecated use removeAttribute instead
     */
    @Deprecated
    public void removeValue(String name) {
        removeAttribute(name);
    }

    @Override
    public String toString() {
        MapBuilder mb = new MapBuilder();
        MapBuilder attrsBuilder = new MapBuilder().setPrintCount(true).setSortKeys(true);

        mb.append("sessionID", sessionID);
        mb.append("model", model);
        mb.append("isNew", isNew);
        mb.append("invalidated", invalidated);

        attrsBuilder.appendAll(attrs);
        attrsBuilder.remove(modelKey);

        mb.append("attrs", attrsBuilder);

        return new ToStringBuilder().append("HttpSession").append(mb).toString();
    }

    private void fireEvent(EventType event) {
        for (SessionInterceptor l : getSessionRequestContext().getSessionConfig().getSessionInterceptors()) {
            if (l instanceof SessionLifecycleListener) {
                SessionLifecycleListener listener = (SessionLifecycleListener) l;

                try {
                    switch (event) {
                        case RECREATED:
                            listener.sessionInvalidated(this);

                        case CREATED:
                            listener.sessionCreated(this);

                        case VISITED:
                            listener.sessionVisited(this);
                            break;

                        case INVALIDATED:
                            listener.sessionInvalidated(this);
                            break;

                        default:
                            unreachableCode();
                    }
                } catch (Exception e) {
                    // 避免因listener出错导致应用的退出。
                    log.error("Listener \"" + listener.getClass().getSimpleName() + "\" failed", e);
                }
            }
        }
    }

    /**
     * Session事件的类型。
     */
    private enum EventType {
        CREATED,
        RECREATED, // 先invalidate然后再create
        INVALIDATED,
        VISITED
    }

    /**
     * 存放session store的状态。
     */
    private class StoreContextImpl implements StoreContext {
        private String storeName;

        public StoreContextImpl(String storeName) {
            this.storeName = storeName;
        }

        public Object getState() {
            return storeStates.get(storeName);
        }

        public void setState(Object stateObject) {
            if (stateObject == null) {
                storeStates.remove(storeName);
            } else {
                storeStates.put(storeName, stateObject);
            }
        }

        public StoreContext getStoreContext(String storeName) {
            return new StoreContextImpl(storeName);
        }

        public SessionRequestContext getSessionRequestContext() {
            return SessionImpl.this.getSessionRequestContext();
        }

        public HttpSession getHttpSession() {
            return sessionInternal;
        }
    }

    /**
     * 内部使用的session对象，不会抛出<code>IllegalStateException</code>异常。
     */
    private class HttpSessionInternal implements HttpSession {
        public String getId() {
            return SessionImpl.this.getId();
        }

        public long getCreationTime() {
            return model == null ? 0 : model.getCreationTime();
        }

        public long getLastAccessedTime() {
            return SessionImpl.this.getLastAccessedTime();
        }

        public int getMaxInactiveInterval() {
            return SessionImpl.this.getMaxInactiveInterval();
        }

        public void setMaxInactiveInterval(int maxInactiveInterval) {
            SessionImpl.this.setMaxInactiveInterval(maxInactiveInterval);
        }

        public ServletContext getServletContext() {
            return SessionImpl.this.getServletContext();
        }

        public Object getAttribute(String name) {
            SessionAttribute attr = attrs.get(name);
            SessionConfig sessionConfig = requestContext.getSessionConfig();
            Object value;

            if (attr == null) {
                String storeName = sessionConfig.getStoreMappings().getStoreNameForAttribute(name);

                if (storeName == null) {
                    value = null;
                } else {
                    attr = new SessionAttribute(name, SessionImpl.this, storeName, new StoreContextImpl(storeName));
                    value = attr.getValue();

                    // 对于session model，需要对其解码
                    if (value != null && modelKey.equals(name)) {
                        value = decodeSessionModel(value); // 如果解码失败，则返回null
                        attr.updateValue(value);
                    }

                    // 只有当value非空（store中包含了此对象），才把它放到attrs表中，否则可能会产生很多垃圾attr对象
                    if (value != null) {
                        attrs.put(name, attr);
                    }
                }
            } else {
                value = attr.getValue();
            }

            return interceptGet(name, value);
        }

        private Object interceptGet(String name, Object value) {
            for (SessionInterceptor l : getSessionRequestContext().getSessionConfig().getSessionInterceptors()) {
                if (l instanceof SessionAttributeInterceptor) {
                    SessionAttributeInterceptor interceptor = (SessionAttributeInterceptor) l;
                    value = interceptor.onRead(name, value);
                }
            }

            return value;
        }

        private Object decodeSessionModel(Object value) {
            SessionModel.Factory factory = new SessionModel.Factory() {
                public SessionModel newInstance(String sessionID, long creationTime, long lastAccessedTime,
                                                int maxInactiveInterval) {
                    return new SessionModelImpl(sessionID, creationTime, lastAccessedTime, maxInactiveInterval);
                }
            };

            SessionModel model = null;
            SessionModelEncoder[] encoders = requestContext.getSessionConfig().getSessionModelEncoders();

            for (SessionModelEncoder encoder : encoders) {
                model = encoder.decode(value, factory);

                if (model != null) {
                    break;
                }
            }

            if (model == null) {
                log.warn("Could not decode session model {} by {} encoders", value, encoders.length);
            }

            return model;
        }

        public Enumeration<String> getAttributeNames() {
            return SessionImpl.this.getAttributeNames();
        }

        public void setAttribute(String name, Object value) {
            value = interceptSet(name, value);

            SessionAttribute attr = attrs.get(name);
            SessionConfig sessionConfig = requestContext.getSessionConfig();

            if (attr == null) {
                String storeName = sessionConfig.getStoreMappings().getStoreNameForAttribute(name);

                if (storeName == null) {
                    throw new IllegalArgumentException("No storage configured for session attribute: " + name);
                } else {
                    attr = new SessionAttribute(name, SessionImpl.this, storeName, new StoreContextImpl(storeName));
                    attrs.put(name, attr);
                }
            }

            attr.setValue(value);
        }

        private Object interceptSet(String name, Object value) {
            for (SessionInterceptor l : getSessionRequestContext().getSessionConfig().getSessionInterceptors()) {
                if (l instanceof SessionAttributeInterceptor) {
                    SessionAttributeInterceptor interceptor = (SessionAttributeInterceptor) l;
                    value = interceptor.onWrite(name, value);
                }
            }

            return value;
        }

        public void removeAttribute(String name) {
            SessionImpl.this.removeAttribute(name);
        }

        public void invalidate() {
            // 清除session数据
            attrs.clear();
            cleared = true;

            // 通知所有的store过期其数据
            SessionConfig sessionConfig = requestContext.getSessionConfig();
            String[] storeNames = sessionConfig.getStores().getStoreNames();

            for (String storeName : storeNames) {
                SessionStore store = sessionConfig.getStores().getStore(storeName);

                store.invaldiate(sessionID, new StoreContextImpl(storeName));
            }

            // 清除model
            if (model == null) {
                model = new SessionModelImpl(SessionImpl.this);
            } else {
                model.reset();
            }
        }

        public boolean isNew() {
            return SessionImpl.this.isNew();
        }

        /**
         * @deprecated
         */
        @Deprecated
        public javax.servlet.http.HttpSessionContext getSessionContext() {
            return SessionImpl.this.getSessionContext();
        }

        /**
         * @deprecated
         */
        @Deprecated
        public Object getValue(String name) {
            return SessionImpl.this.getValue(name);
        }

        /**
         * @deprecated
         */
        @Deprecated
        public String[] getValueNames() {
            return SessionImpl.this.getValueNames();
        }

        /**
         * @deprecated
         */
        @Deprecated
        public void putValue(String name, Object value) {
            SessionImpl.this.putValue(name, value);
        }

        /**
         * @deprecated
         */
        @Deprecated
        public void removeValue(String name) {
            SessionImpl.this.removeValue(name);
        }
    }
}
