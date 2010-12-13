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

import static com.alibaba.citrus.util.ArrayUtil.*;
import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.BasicConstant.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.ObjectUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.citrus.service.uribroker.interceptor.URIBrokerInterceptor;
import com.alibaba.citrus.service.uribroker.interceptor.URIBrokerPathInterceptor;
import com.alibaba.citrus.util.StringUtil;

/**
 * 代表一个基本的URI风格。
 * <p>
 * 一个URI包括如下几个部分：
 * </p>
 * 
 * <pre>
 * URI         = SERVER_INFO + PATH + &quot;?&quot; + QUERY_DATA + &quot;#&quot; + REFERENCE
 * SERVER_INFO = scheme://loginUser:loginPassword@serverName:serverPort
 * PATH        = /path/path
 * QUERY_DATA  = queryKey1=value1&amp;queryKey2=value2
 * REFERENCE   = reference
 * </pre>
 * <p>
 * 例如：
 * </p>
 * 
 * <pre>
 * http://user:pass@myserver.com:8080/view?id=1#top
 * </pre>
 * <p>
 * 注意，<code>URIBroker</code>没有提供修改path的方法。如果要添加、删除、修改path，请直接使用子类
 * <code>GenericURIBroker</code>。
 * </p>
 * 
 * @author Michael Zhou
 * @author dux.fangl
 */
public abstract class URIBroker extends URIBrokerFeatures {
    /**
     * 代表URI的类型。
     */
    public static enum URIType {
        /** 自动选择URI的类型。 */
        auto,

        /** 完整URI，包括serverInfo, path。 */
        full,

        /** 绝对URI，不包括serverInfo，但包括完整的path。 */
        absolute,

        /** 相对URI，不包括serverInfo，其path为相对于当前请求的URI的相对路径。 */
        relative
    }

    public static final String SERVER_SCHEME_HTTP = "http";
    public static final String SERVER_SCHEME_HTTPS = "https";
    public static final Integer SERVER_PORT_HTTP = 80;
    public static final Integer SERVER_PORT_HTTPS = 443;

    protected static final int PATH_INDEX = 0;

    private static final Pattern pathPattern = Pattern.compile("(^/*|/+)(^[^/]+|[^/]*)");
    private final int[] pathSegmentIndexes = new int[getPathSegmentCount()];
    private URIType type;
    private URI baseURI;
    private String serverScheme;
    private String serverName;
    private int serverPort = -1;
    private String loginUser;
    private String loginPassword;
    private String reference;
    private final List<String> path = createLinkedList();
    private final Map<String, Object> query = createLinkedHashMap();

    /**
     * 取得URI类型。
     */
    public URIType getURIType() {
        return type;
    }

    /**
     * 设置URI类型。
     */
    public URIBroker setURIType(URIType type) {
        this.type = type;
        return this;
    }

    /**
     * 设置成自动URI类型。
     */
    public URIBroker autoURI() {
        return setURIType(URIType.auto);
    }

    /**
     * 设置成完整URI类型。
     */
    public URIBroker fullURI() {
        return setURIType(URIType.full);
    }

    /**
     * 设置成绝对URI类型。
     */
    public URIBroker absoluteURI() {
        return setURIType(URIType.absolute);
    }

    /**
     * 设置成相对URI类型。
     */
    public URIBroker relativeURI() {
        return setURIType(URIType.relative);
    }

    /**
     * 取得baseURI，当<code>URIType==absolute/relative</code>时，用来生成以此为基准的URI。
     */
    public String getBaseURI() {
        return baseURI == null ? null : baseURI.toString();
    }

    /**
     * 设置baseURI，当<code>URIType==absolute/relative</code>时，用来生成以此为基准的URI。
     */
    public URIBroker setBaseURI(String baseURI) {
        baseURI = trimToNull(baseURI);
        this.baseURI = baseURI == null ? null : URI.create(baseURI).normalize();
        return this;
    }

    /**
     * 取得不包含query和reference的serverURI。
     */
    public String getServerURI() {
        processInterceptors();

        StringBuilder buf = new StringBuilder();
        render(buf, true);

        return buf.toString();
    }

    /**
     * 设置现成的uri，不包含query和reference。
     */
    public final URIBroker setServerURI(String uriString) {
        URL uri;

        try {
            uri = new URL(assertNotNull(trimToNull(uriString), "serverURI"));
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e.getMessage());
        }

        String serverScheme = uri.getProtocol();
        String[] userInfo = StringUtil.split(uri.getUserInfo(), ":");
        String serverName = uri.getHost();
        int serverPort = uri.getPort();

        if (serverScheme != null) {
            setServerScheme(serverScheme);
        }

        if (!isEmptyArray(userInfo)) {
            if (userInfo.length > 0) {
                setLoginUser(userInfo[0]);
            }

            if (userInfo.length > 1) {
                setLoginPassword(userInfo[1]);
            }
        }

        if (serverName != null) {
            setServerName(serverName);
        }

        if (serverPort > 0) {
            setServerPort(serverPort);
        }

        setServerURI(uri);

        return this;
    }

    protected void setServerURI(URL uri) {
    }

    /**
     * 取得URI scheme。
     */
    public String getServerScheme() {
        return serverScheme;
    }

    /**
     * 设置URI scheme，只能是http和https。
     */
    public URIBroker setServerScheme(String serverScheme) {
        this.serverScheme = trim(serverScheme);
        renderer.clearServerBuffer();
        return this;
    }

    /**
     * 取得服务器名。
     */
    public String getServerName() {
        return serverName;
    }

    /**
     * 设置服务器名。
     */
    public URIBroker setServerName(String serverName) {
        this.serverName = trim(serverName);
        renderer.clearServerBuffer();
        return this;
    }

    /**
     * 取得服务器端口。
     */
    public int getServerPort() {
        return serverPort;
    }

    /**
     * 设置服务器端口。注意默认的80端口和443端口分别在http和https时不做输出。
     */
    public URIBroker setServerPort(int serverPort) {
        this.serverPort = serverPort <= 0 ? -1 : serverPort;
        renderer.clearServerBuffer();
        return this;
    }

    /**
     * 取得登录用户。
     */
    public String getLoginUser() {
        return loginUser;
    }

    /**
     * 设置登录用户。
     */
    public URIBroker setLoginUser(String loginUser) {
        this.loginUser = trim(loginUser);
        renderer.clearServerBuffer();
        return this;
    }

    /**
     * 取得登录密码。
     */
    public String getLoginPassword() {
        return loginPassword;
    }

    /**
     * 设置登录密码。
     */
    public URIBroker setLoginPassword(String loginPassword) {
        this.loginPassword = trim(loginPassword);
        renderer.clearServerBuffer();
        return this;
    }

    /**
     * 取得reference引用。
     */
    public String getReference() {
        return reference;
    }

    /**
     * 设置reference引用。
     */
    public URIBroker setReference(String reference) {
        this.reference = trim(reference);
        return this;
    }

    /**
     * 取得URL的path部分。
     */
    public String getPath() {
        return getAllPathSegmentsAsString(PATH_INDEX);
    }

    /**
     * 取得URI path列表。
     */
    public List<String> getPathElements() {
        return getAllPathSegments(PATH_INDEX);
    }

    /**
     * 取得当前URI path分成几段。
     */
    protected abstract int getPathSegmentCount();

    protected final List<String> getAllPathSegments(int segment) {
        return subPath(segment, true);
    }

    protected final List<String> getPathSegment(int segment) {
        return subPath(segment, false);
    }

    private List<String> subPath(int segment, boolean multi) {
        assertSegment(segment);

        int size = path.size();
        int endIndex = multi ? size : pathSegmentIndexes[segment];
        int beginIndex = segment > 0 ? pathSegmentIndexes[segment - 1] : 0;

        if (beginIndex == 0 && endIndex == size) {
            return path;
        } else {
            return path.subList(beginIndex, endIndex);
        }
    }

    protected final String getAllPathSegmentsAsString(int segment) {
        return getSubPathAsString(segment, true);
    }

    protected final String getPathSegmentAsString(int segment) {
        return getSubPathAsString(segment, false);
    }

    private String getSubPathAsString(int segment, boolean multi) {
        assertSegment(segment);

        StringBuilder buf = new StringBuilder();
        int endIndex = multi ? path.size() : pathSegmentIndexes[segment];
        int beginIndex = segment > 0 ? pathSegmentIndexes[segment - 1] : 0;

        for (int i = beginIndex; i < endIndex; i++) {
            buf.append("/").append(path.get(i));
        }

        return buf.toString();
    }

    /**
     * 合并多个list，并加入到某个segment中。
     * <ul>
     * <li>以复制的方式合并，支持如下操作：path = parent.path + path。</li>
     * <li>合并时不再解析每一项path，假设每一项path在合并前已经是合法的格式，即不包含"/"。</li>
     * </ul>
     */
    @SuppressWarnings("unchecked")
    protected final void setPathSegment(int segment, List<?>... lists) {
        List<String> mergedList = createLinkedList();

        if (lists != null) {
            for (List<?> list : lists) {
                mergedList.addAll((List<String>) list);
            }
        }

        clearPathSegment(segment);

        if (!mergedList.isEmpty()) {
            for (String element : mergedList) {
                addPathSegment(segment, element, false);
            }
        }
    }

    protected final void setPathSegment(int segment, String path) {
        clearPathSegment(segment);
        addPathSegment(segment, path);
    }

    protected final void addPathSegment(int segment, String path) {
        addPathSegment(segment, path, true);
    }

    private void addPathSegment(int segment, String path, boolean split) {
        assertSegment(segment);

        if (path != null) {
            int index = pathSegmentIndexes[segment];
            boolean isAppend = index == pathSegmentIndexes[pathSegmentIndexes.length - 1];

            if (split) {
                for (String element : StringUtil.split(path, " /\\")) {
                    index = addPathElement(element, index, isAppend);
                }
            } else {
                index = addPathElement(path, index, isAppend);
            }

            int newAdded = index - pathSegmentIndexes[segment];

            // 如果什么也没有被加入，则加入空字符串
            if (newAdded > 0) {
                for (int i = segment; i < pathSegmentIndexes.length; i++) {
                    pathSegmentIndexes[i] += newAdded;
                }
            }
        }
    }

    private int addPathElement(String element, int elementIndex, boolean isAppend) {
        if (!isEmpty(element)) {
            path.add(elementIndex, element);

            if (isAppend) {
                renderer.updatePathBuffer(element);
            } else {
                renderer.clearPathBuffer();
            }

            return elementIndex + 1;
        }

        return elementIndex;
    }

    protected final void clearPathSegment(int segment) {
        assertSegment(segment);

        int index = pathSegmentIndexes[segment];
        int previousIndex = segment > 0 ? pathSegmentIndexes[segment - 1] : 0;
        int removedCount = index - previousIndex;
        boolean isTruncate = index == pathSegmentIndexes[pathSegmentIndexes.length - 1];

        if (removedCount > 0) {
            ListIterator<String> i = path.listIterator(index);

            for (int j = 0; j < removedCount; j++) {
                i.previous();
                i.remove();
            }

            for (int j = segment; j < pathSegmentIndexes.length; j++) {
                pathSegmentIndexes[j] -= removedCount;
            }

            if (isTruncate) {
                renderer.truncatePathBuffer(removedCount);
            } else {
                renderer.clearPathBuffer();
            }
        }
    }

    private void assertSegment(int segment) {
        if (segment < 0 || segment >= pathSegmentIndexes.length) {
            throw new IllegalArgumentException("segment index " + segment + " out of bound [0, "
                    + pathSegmentIndexes.length + ")");
        }
    }

    /**
     * 取得URI query表。
     */
    public Map<String, Object/* String or String[] */> getQuery() {
        return query;
    }

    /**
     * 设置一组query。
     */
    public void setQuery(Map<String, Object> query) {
        getQuery().clear();

        if (query != null && !query.isEmpty()) {
            for (Map.Entry<String, Object> entry : query.entrySet()) {
                setQueryData(entry.getKey(), entry.getValue());
            }
        }

        renderer.clearQueryBuffer();
    }

    /**
     * 删除所有的query。
     */
    public URIBroker clearQuery() {
        if (query != null) {
            query.clear();
        }

        renderer.clearQueryBuffer();

        return this;
    }

    /**
     * 取得指定id对应的query值。
     */
    public String getQueryData(String id) {
        Object value = getQuery().get(id);

        if (value instanceof String) {
            return (String) value;
        } else if (value instanceof String[]) {
            String[] values = (String[]) value;

            if (values.length > 0) {
                return values[0];
            }
        }

        return null;
    }

    /**
     * 设置query。
     */
    public URIBroker setQueryData(String id, Object values) {
        id = assertNotNull(trimToNull(id), "empty query id");

        Map<String, Object> query = getQuery();

        if (values == null) {
            query.put(id, EMPTY_STRING);
        } else if (values instanceof String[]) {
            String[] strArray = (String[]) values;

            for (int i = 0; i < strArray.length; i++) {
                if (strArray[i] == null) {
                    strArray[i] = EMPTY_STRING;
                }
            }

            query.put(id, strArray);
        } else if (values.getClass().isArray()) {
            String[] strArray = new String[Array.getLength(values)];

            for (int i = 0; i < strArray.length; i++) {
                Object value = Array.get(values, i);
                strArray[i] = value == null ? EMPTY_STRING : String.valueOf(value);
            }

            query.put(id, strArray);
        } else {
            query.put(id, String.valueOf(values));
        }

        renderer.clearQueryBuffer();

        return this;
    }

    /**
     * 添加query。
     */
    public URIBroker addQueryData(String id, Object value) {
        id = assertNotNull(trimToNull(id), "empty query id");

        String strValue;

        if (value == null) {
            strValue = EMPTY_STRING;
        } else {
            assertTrue(!value.getClass().isArray(), "use setQueryData(array) instead");
            strValue = String.valueOf(value);
        }

        Object values = getQuery().get(id);

        if (values == null) {
            values = strValue;
        } else if (values instanceof String) {
            values = new String[] { (String) values, strValue };
        } else if (values instanceof String[]) {
            int length = ((String[]) values).length;
            String[] tmp = new String[length + 1];
            System.arraycopy(values, 0, tmp, 0, length);
            tmp[length] = strValue;
            values = tmp;
        } else {
            unreachableCode();
        }

        getQuery().put(id, values);

        renderer.updateQueryBuffer(id, strValue);

        return this;
    }

    /**
     * 删除指定的query。
     */
    public URIBroker removeQueryData(String id) {
        if (query != null) {
            query.remove(trimToNull(id));
        }

        renderer.clearQueryBuffer();

        return this;
    }

    /**
     * 复制parent broker中的值作为默认值，但不覆盖当前broker中已有的值。
     * <p>
     * 子类应该覆盖此方法，以实现特定的init功能。
     * </p>
     */
    @Override
    protected void initDefaults(URIBroker parent) {
        if (type == null) {
            type = parent.getURIType();
        }

        if (baseURI == null) {
            baseURI = parent.baseURI;
        }

        if (serverScheme == null) {
            serverScheme = parent.getServerScheme();
        }

        if (serverName == null) {
            serverName = parent.getServerName();
        }

        if (serverPort <= 0) {
            serverPort = parent.getServerPort();
        }

        if (loginUser == null) {
            loginUser = parent.getLoginUser();
        }

        if (loginPassword == null) {
            loginPassword = parent.getLoginPassword();
        }

        if (reference == null) {
            reference = parent.getReference();
        }

        if (!parent.getQuery().isEmpty()) {
            // 合并query，包括合并query的值
            Map<String, Object> queryCopy = createLinkedHashMap();
            queryCopy.putAll(getQuery());

            clearQuery();
            getQuery().putAll(parent.getQuery());

            for (Map.Entry<String, Object> entry : queryCopy.entrySet()) {
                String id = entry.getKey();
                Object values = entry.getValue();

                if (values instanceof String) {
                    addQueryData(id, values);
                } else if (values instanceof String[]) {
                    for (String value : (String[]) values) {
                        addQueryData(id, value);
                    }
                }
            }
        }
    }

    /**
     * 复制parent的状态。
     * <p>
     * 子类应该覆盖此方法，以实现特定的reset功能。
     * </p>
     */
    @Override
    protected void copyFrom(URIBroker parent) {
        baseURI = parent.baseURI;
        setURIType(parent.getURIType());
        setServerScheme(parent.getServerScheme());
        setServerName(parent.getServerName());
        setServerPort(parent.getServerPort());
        setLoginUser(parent.getLoginUser());
        setLoginPassword(parent.getLoginPassword());
        setReference(parent.getReference());

        clearQuery();
        setQuery(parent.getQuery());
    }

    /**
     * 将request中的运行时信息填充到uri broker中。
     */
    @Override
    protected void populateWithRequest(HttpServletRequest request) {
        // scheme、serverName和serverPort必须一起设。
        if (serverScheme == null && serverName == null && serverPort <= 0) {
            setServerScheme(request.getScheme());
            setServerName(request.getServerName());
            setServerPort(request.getServerPort());
        }
    }

    /**
     * 按顺序执行所有path interceptors。
     * <p>
     * 和<code>URIBrokerInterceptor</code>不同，
     * <code>URIBrokerPathInterceptor</code>在每次render时均会被调用。
     * </p>
     */
    protected String processPathInterceptors(String path) {
        if (hasInterceptors()) {
            for (Map.Entry<URIBrokerInterceptor, Integer> entry : getInterceptorStates().entrySet()) {
                if (entry.getKey() instanceof URIBrokerPathInterceptor) {
                    URIBrokerPathInterceptor interceptor = (URIBrokerPathInterceptor) entry.getKey();

                    if (entry.getValue() == 1) {
                        path = interceptor.perform(this, path);
                    }
                }
            }
        }

        return path;
    }

    @Override
    protected final void render(StringBuilder buf) {
        render(buf, false);
    }

    private void render(StringBuilder buf, boolean renderServerURIOnly) {
        URIType type = renderServerURIOnly || getURIType() == null ? URIType.full : getURIType();
        BaseURI baseURI = null;
        boolean renderServer = true;

        switch (type) {
            case auto:
            case absolute:
            case relative:
                baseURI = createBaseURI();

                if (baseURI != null) {
                    // 当且仅当baseURI中的server信息和broker中的一致，才不渲染server
                    String scheme = getEffectiveServerScheme(getServerScheme());
                    int port = getEffectiveServerPort(scheme, getServerPort());

                    if (isEquals(scheme, baseURI.serverScheme) && isEquals(getServerName(), baseURI.serverName)
                            && port == baseURI.serverPort) {
                        renderServer = false;
                    }
                }

                break;

            case full:
        }

        // server info
        if (renderServer) {
            if (renderer.isServerRendered()) {
                buf.append(renderer.serverBuffer);
            } else {
                renderServer(buf);
            }
        }

        // path info
        String path = EMPTY_STRING;

        if (!URIBroker.this.path.isEmpty()) {
            StringBuilder pathBuf = new StringBuilder();

            if (renderer.isPathRendered()) {
                pathBuf.append(renderer.pathBuffer);
            } else {
                renderPath(pathBuf);
            }

            path = pathBuf.toString();
        }

        path = trimToEmpty(processPathInterceptors(path));

        // 将path转换成相对路径（前提：server未渲染）
        if (!renderServer) {
            switch (type) {
                case relative:
                    path = getRelativePath(baseURI.path, path);
                    break;

                case auto:
                    String relativePath = getRelativePath(baseURI.path, path);

                    if (!relativePath.startsWith("../")) {
                        path = relativePath;
                    }

                    break;

                case full:
                case absolute:
            }
        }

        if (renderServer && !path.startsWith("/")) {
            buf.append("/");
        }

        buf.append(path);

        if (!renderServerURIOnly) {
            // query info
            if (renderer.isQueryRendered()) {
                buf.append(renderer.queryBuffer);
            } else {
                renderQuery(buf);
            }

            // #reference
            String reference = getReference();

            if (!isEmpty(reference)) {
                buf.append("#");
                buf.append(reference);
            }
        }
    }

    /**
     * 从request或指定URI中创建并规格化baseURI。
     */
    private BaseURI createBaseURI() {
        BaseURI result = null;

        if (baseURI != null) {
            result = new BaseURI();
            result.serverScheme = getEffectiveServerScheme(baseURI.getScheme());
            result.serverName = trimToNull(baseURI.getHost());
            result.serverPort = getEffectiveServerPort(result.serverScheme, baseURI.getPort());
            result.path = trimToEmpty(baseURI.getPath());
        } else {
            HttpServletRequest request = getRealRequest();

            if (request != null) {
                result = new BaseURI();
                result.serverScheme = getEffectiveServerScheme(request.getScheme());
                result.serverName = trimToNull(request.getServerName());
                result.serverPort = getEffectiveServerPort(result.serverScheme, request.getServerPort());
                result.path = trimToEmpty(request.getRequestURI());
            }
        }

        return result;
    }

    /**
     * 将path转换成相对于base的相对路径。
     */
    private String getRelativePath(String base, String path) {
        // 去除base中最后一段路径，例如：
        //  /sub/dir/index.html -> /sub/dir
        //  /sub/dir/ -> /sub/dir
        int index = base.lastIndexOf("/");

        if (index >= 0) {
            base = base.substring(0, index);
        }

        // 特殊情况：base为空，直接返回path，但除去开头的/
        if (isEmpty(base)) {
            if (path.startsWith("/")) {
                return path.replaceFirst("^/+", EMPTY_STRING);
            } else {
                return path;
            }
        }

        Matcher baseMatcher = pathPattern.matcher(base);
        Matcher pathMatcher = pathPattern.matcher(path);

        boolean baseFound;
        boolean pathFound;

        // 去除base和path中相同的部分，例如：
        //  /this/is/base/dir -> /base/dir
        //  /this/is/my/path  -> /my/path
        do {
            baseFound = baseMatcher.find();
            pathFound = pathMatcher.find();
        } while (baseFound && pathFound && isEquals(baseMatcher.group(2), pathMatcher.group(2)));

        StringBuilder relativePath = new StringBuilder();

        // 将base中剩余的部分，分别转换成../
        for (; baseFound; baseFound = baseMatcher.find()) {
            relativePath.append("../");
        }

        // 将path中剩余的部分，追加到末尾
        while (pathFound) {
            relativePath.append(pathMatcher.group(2));
            pathFound = pathMatcher.find();

            if (pathFound) {
                relativePath.append("/");
            }
        }

        return relativePath.toString();
    }

    /**
     * 渲染URI服务器信息。
     */
    @Override
    protected final void renderServer(StringBuilder buf) {
        // scheme://
        String serverScheme = getEffectiveServerScheme(getServerScheme());

        buf.append(serverScheme);
        buf.append("://");

        // user:password@
        String loginUser = getLoginUser();
        String loginPassword = getLoginPassword();

        if (!isEmpty(loginUser)) {
            buf.append(loginUser);

            if (!isEmpty(loginPassword)) {
                buf.append(":").append(loginPassword);
            }

            buf.append("@");
        }

        // hostname:port
        String serverName = getServerName();

        if (!isEmpty(serverName)) {
            buf.append(serverName);

            if (!isDefaultPort(serverScheme)) {
                buf.append(":");
                buf.append(getServerPort());
            }
        }
    }

    private String getEffectiveServerScheme(String serverScheme) {
        return defaultIfEmpty(serverScheme, SERVER_SCHEME_HTTP);
    }

    private int getEffectiveServerPort(String serverScheme, int serverPort) {
        if (serverPort <= 0) {
            if (SERVER_SCHEME_HTTP.equals(serverScheme)) {
                return SERVER_PORT_HTTP;
            } else if (SERVER_SCHEME_HTTPS.equals(serverScheme)) {
                return SERVER_PORT_HTTPS;
            }
        }

        return serverPort;
    }

    private boolean isDefaultPort(String serverScheme) {
        int serverPort = getServerPort();
        boolean isDefaultPort = serverPort <= 0;

        if (!isDefaultPort) {
            // http和80
            isDefaultPort |= SERVER_SCHEME_HTTP.equals(serverScheme) && serverPort == SERVER_PORT_HTTP;

            // https和443
            isDefaultPort |= SERVER_SCHEME_HTTPS.equals(serverScheme) && serverPort == SERVER_PORT_HTTPS;
        }
        return isDefaultPort;
    }

    /**
     * 渲染URI path。
     */
    @Override
    protected final void renderPath(StringBuilder buf) {
        for (String path : URIBroker.this.path) {
            buf.append("/").append(escapeURL(path));
        }
    }

    /**
     * 渲染URI query string。
     */
    @Override
    protected final void renderQuery(StringBuilder buf) {
        if (!getQuery().isEmpty()) {
            buf.append("?");

            for (Iterator<Map.Entry<String, Object>> i = getQuery().entrySet().iterator(); i.hasNext();) {
                Map.Entry<String, Object> entry = i.next();
                String id = escapeURL(entry.getKey());
                Object value = entry.getValue();

                if (value instanceof String[]) {
                    int length = ((String[]) value).length;

                    for (int j = 0; j < length; j++) {
                        buf.append(id).append("=").append(escapeURL(((String[]) value)[j]));

                        if (j + 1 < length) {
                            buf.append("&");
                        }
                    }
                } else {
                    buf.append(id).append("=").append(escapeURL(String.valueOf(value)));
                }

                if (i.hasNext()) {
                    buf.append("&");
                }
            }
        }
    }

    private class BaseURI {
        String serverScheme;
        String serverName;
        int serverPort;
        String path;
    }
}
