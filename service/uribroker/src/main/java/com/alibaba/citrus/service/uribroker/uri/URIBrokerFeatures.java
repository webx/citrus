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
package com.alibaba.citrus.service.uribroker.uri;

import static com.alibaba.citrus.springext.util.SpringExtUtil.*;
import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.Assert.ExceptionType.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.citrus.service.template.Renderable;
import com.alibaba.citrus.service.uribroker.interceptor.URIBrokerInterceptor;
import com.alibaba.citrus.util.StringEscapeUtil;

/**
 * 这个类将URIBroker中，和URL的结构没有直接关系的一些基础特性分离出来，使代码更清晰。
 * 
 * @author Michael Zhou
 */
public abstract class URIBrokerFeatures implements Renderable {
    protected final Renderer renderer = new Renderer();
    private boolean requestAware = true;
    private HttpServletRequest request;
    private URIBroker parent;
    private boolean initialized;
    private String charset;
    private boolean autoReset;
    private Map<URIBrokerInterceptor, Integer> interceptors;

    /**
     * 设置运行时信息。由Spring自动装入。
     */
    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }

    /**
     * 是否自动从request中填充值。
     */
    public boolean isRequestAware() {
        return requestAware;
    }

    /**
     * 设置是否自动从request中填充值。
     */
    public void setRequestAware(boolean requestAware) {
        this.requestAware = requestAware;
    }

    /**
     * 取得parent URI broker。
     */
    public URIBroker getParent() {
        return parent;
    }

    /**
     * 设置parent URI broker。
     * <p>
     * 每次reset，当前URIBroker的状态将会恢复成和它的parent相同。
     * </p>
     */
    public void setParent(URIBroker parent) {
        assertTrue(!initialized, ILLEGAL_STATE, "already initialized");

        if (parent != null) {
            this.parent = parent;
        }
    }

    /**
     * 取得URL encoding的编码字符集。
     * <p>
     * 假如值为<code>null</code>，将从<code>LocaleUtil</code>中取得。
     * </p>
     */
    public String getCharset() {
        return charset;
    }

    /**
     * 设置URL encoding的编码字符集。
     */
    public void setCharset(String charset) {
        this.charset = trimToNull(charset);
    }

    /**
     * 是否自动reset。
     * <p>
     * URI broker是有状态的。假如自动reset设为<code>true</code>，则其状态在执行<code>render()</code>
     * 方法之后将恢复原状。 从配置文件中生成的uri broker，其autoReset为<code>false</code>，说明其状态不能被改变。
     * </p>
     */
    public boolean isAutoReset() {
        return autoReset;
    }

    /**
     * 是否有interceptor？
     */
    public boolean hasInterceptors() {
        return interceptors != null && !interceptors.isEmpty();
    }

    /**
     * 取得所有的interceptors及其状态。
     */
    protected Map<URIBrokerInterceptor, Integer> getInterceptorStates() {
        if (interceptors == null) {
            interceptors = createLinkedHashMap();
        }

        return interceptors;
    }

    /**
     * 取得所有的interceptors。
     */
    public Collection<URIBrokerInterceptor> getInterceptors() {
        return getInterceptorStates().keySet();
    }

    /**
     * 取得所有的interceptors。
     */
    public void setInterceptors(Collection<URIBrokerInterceptor> interceptors) {
        clearInterceptors();

        for (URIBrokerInterceptor interceptor : interceptors) {
            addInterceptor(interceptor);
        }
    }

    /**
     * 添加一个interceptor。
     */
    public void addInterceptor(URIBrokerInterceptor interceptor) {
        getInterceptorStates().put(assertNotNull(interceptor, "interceptor"), null);
    }

    /**
     * 清除interceptors。
     */
    public void clearInterceptors() {
        if (interceptors != null) {
            interceptors.clear();
        }
    }

    /**
     * 初始化URI broker，将当前broker与parent合并。
     */
    public final void init() {
        if (initialized) {
            return;
        }

        initialized = true;

        if (parent != null) {
            // 确保parent已经初始化
            parent.init();

            // charset
            if (charset == null) {
                charset = parent.getCharset();
            }

            // init interceptors，将parent interceptors加在前面
            if (parent.hasInterceptors()) {
                mergeLinkedHashMap(parent.getInterceptorStates(), getInterceptorStates());
            }

            // init others
            initDefaults(parent);
        }

        renderer.prerender();
    }

    /**
     * 复制parent broker中的值作为默认值，但不覆盖当前broker中已有的值。
     */
    protected abstract void initDefaults(URIBroker parent);

    /**
     * 复位到parent的状态。
     */
    public final void reset() {
        URIBroker parent = this.parent;

        if (parent == null) {
            parent = newInstanceInternal();
        }

        // reset charset
        charset = parent.getCharset();

        // reset interceptors
        clearInterceptors();

        if (parent.hasInterceptors()) {
            for (URIBrokerInterceptor interceptor : parent.getInterceptors()) {
                addInterceptor(interceptor);
            }
        }

        // reset others
        copyFrom(parent);

        // 复制renderer。
        // 确保同一类型的broker，才复制预渲染buffer，因为不同的broker有可能渲染结果不同。
        if (parent.getClass().equals(getClass())) {
            renderer.copyFrom(parent.renderer);
        }

        // read request
        HttpServletRequest realRequest = getRealRequest();

        if (realRequest != null) {
            populateWithRequest(realRequest);
        }
    }

    /**
     * 复制parent的状态。
     * <p>
     * 子类应该覆盖此方法，以实现特定的reset功能。
     * </p>
     */
    protected abstract void copyFrom(URIBroker parent);

    /**
     * 将request中的运行时信息填充到uri broker中。
     */
    protected abstract void populateWithRequest(HttpServletRequest request);

    /**
     * 当以下条件成立时，返回真实的request对象。
     * <ul>
     * <li>isRequestAware == true</li>
     * <li>request != null</li>
     * <li>处于web环境，request proxy可取得真实的request对象。</li>
     * </ul>
     */
    protected final HttpServletRequest getRealRequest() {
        if (isRequestAware()) {
            return getProxyTarget(request);
        }

        return null;
    }

    /**
     * 以当前URI broker为模板, 创建一个新的URI broker。新的broker在执行<code>render()</code>
     * 方法后会自动复位。
     * <p>
     * 此方法和<code>render()</code>具有相同的副作用，假如<code>autoReset == true</code>
     * ，那么所有状态自动复位。可应用于以下场景（velocity）：
     * </p>
     * 
     * <pre>
     * #set ($sub_uri = $uri.addPath("xxx/yyy").fork())
     * 
     * #foreach (...)
     *    &lt;a href="$sub_uri.addQueryData(...)"&gt;...&lt;/a&gt;
     * #end
     * </pre>
     */
    public final URIBroker fork() {
        return fork(true);
    }

    /**
     * 以当前URI broker为模板, 创建一个新的URI broker。
     */
    public final URIBroker fork(boolean autoReset) {
        URIBroker parentBroker;
        URIBroker broker = null;

        // 确保当前broker不是autoReset的, 否则当前broker的reset会影响新生成的broker
        if (autoReset && isAutoReset()) {
            parentBroker = fork(false);
            parentBroker.renderer.prerender();

            // 复位当前的broker, 就象执行过render一样
            reset();
        } else {
            parentBroker = (URIBroker) this;
        }

        // 生成新的基于parentBroker的broker
        // 确保新建的broker是非空，且为同一类型。
        broker = newInstanceInternal();

        ((URIBrokerFeatures) broker).autoReset = autoReset;
        broker.setRequestAware(parentBroker.isRequestAware());
        broker.setRequest(((URIBrokerFeatures) parentBroker).request);
        broker.setParent(parentBroker);
        broker.reset();

        return broker;
    }

    /**
     * 创建一个新的空白broker。
     */
    protected final URIBroker newInstanceInternal() {
        URIBroker instance = assertNotNull(newInstance(), "%s.newInstance() returns null", getClass().getName());

        assertTrue(instance != this, "%s.newInstance() returns itself", getClass().getName());

        assertTrue(instance.getClass().equals(getClass()), "%s.newInstance() returns wrong type: %s", getClass()
                .getName(), instance.getClass().getName());

        return instance;
    }

    /**
     * 创建新的实例。
     */
    protected abstract URIBroker newInstance();

    /**
     * 按顺序执行所有interceptors。
     */
    protected void processInterceptors() {
        if (hasInterceptors()) {
            for (Map.Entry<URIBrokerInterceptor, Integer> entry : getInterceptorStates().entrySet()) {
                if (entry.getValue() == null) {
                    entry.setValue(1);
                    entry.getKey().perform((URIBroker) this);
                }
            }
        }
    }

    /**
     * 渲染uri。
     */
    public final String render() {
        return render(autoReset);
    }

    private String render(boolean reset) {
        processInterceptors();

        StringBuilder buf = new StringBuilder();
        render(buf);

        if (reset) {
            reset();
        }

        return buf.toString();
    }

    protected abstract void render(StringBuilder buf);

    protected abstract void renderServer(StringBuilder buf);

    protected abstract void renderPath(StringBuilder buf);

    protected abstract void renderQuery(StringBuilder buf);

    /**
     * 工具方法：进行URL编码，使用uribroker中指定的charset。
     */
    protected final String escapeURL(String str) {
        String charset = trimToNull(getCharset());

        try {
            return StringEscapeUtil.escapeURL(str, charset);
        } catch (UnsupportedEncodingException e) {
            return StringEscapeUtil.escapeURL(str);
        }
    }

    /**
     * 将parent map中的值插入到当前map的前面。
     */
    protected final <K, V> void mergeLinkedHashMap(Map<K, V> parentMap, Map<K, V> thisMap) {
        assertNotNull(thisMap, "thisMap");

        Map<K, V> thisMapCopy = createLinkedHashMap();
        thisMapCopy.putAll(thisMap);
        thisMap.clear();

        if (parentMap != null) {
            thisMap.putAll(parentMap);
        }

        thisMap.putAll(thisMapCopy);
    }

    /**
     * 取得URI字符串，但不reset。
     */
    @Override
    public String toString() {
        return render(false);
    }

    /**
     * 用来渲染URL、管理buffer的辅助类。
     * <p>
     * 为了加快渲染URL的速度，在init的时候会进行预渲染，以填充<code>serverBuffer</code>、
     * <code>pathBuffer</code>、<code>queryBuffer</code>
     * 等三个buffer。当fork的时候，直接将buffer复制给新的对象
     * ，假如新的broker没有经过很大的修改就render的话，渲染速度会大大加快。
     * </p>
     */
    public final class Renderer {
        protected final StringBuilder serverBuffer = new StringBuilder();
        protected final StringBuilder pathBuffer = new StringBuilder();
        protected final StringBuilder queryBuffer = new StringBuilder();

        public boolean isServerRendered() {
            return serverBuffer.length() > 0;
        }

        public boolean isPathRendered() {
            return pathBuffer.length() > 0;
        }

        public boolean isQueryRendered() {
            return queryBuffer.length() > 0;
        }

        public void clearServerBuffer() {
            serverBuffer.setLength(0);
        }

        public void clearPathBuffer() {
            pathBuffer.setLength(0);
        }

        public void updatePathBuffer(String path) {
            if (isPathRendered()) {
                pathBuffer.append("/").append(escapeURL(path));
            }
        }

        public void truncatePathBuffer(int removedCount) {
            if (isPathRendered()) {
                int index = pathBuffer.length();

                for (int i = 0; i < removedCount && index >= 0; i++) {
                    index = pathBuffer.lastIndexOf("/", index - 1);
                }

                if (index >= 0) {
                    pathBuffer.setLength(index);
                } else {
                    pathBuffer.setLength(0);
                }
            }
        }

        public void clearQueryBuffer() {
            queryBuffer.setLength(0);
        }

        public void updateQueryBuffer(String id, String value) {
            if (isQueryRendered()) {
                queryBuffer.append("&").append(escapeURL(id)).append("=").append(escapeURL(value));
            }
        }

        private void prerender() {
            if (!isServerRendered()) {
                renderServer(serverBuffer);
            }

            if (!isPathRendered()) {
                renderPath(pathBuffer);
            }

            if (!isQueryRendered()) {
                renderQuery(queryBuffer);
            }
        }

        private void copyFrom(Renderer parent) {
            // server info
            clearServerBuffer();

            if (parent.isServerRendered()) {
                serverBuffer.append(parent.serverBuffer);
            }

            // path info
            clearPathBuffer();

            if (parent.isPathRendered()) {
                pathBuffer.append(parent.pathBuffer);
            }

            // query info
            clearQueryBuffer();

            if (parent.isQueryRendered()) {
                queryBuffer.append(parent.queryBuffer);
            }
        }
    }
}
