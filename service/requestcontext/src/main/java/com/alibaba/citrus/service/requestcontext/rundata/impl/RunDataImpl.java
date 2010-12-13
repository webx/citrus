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
package com.alibaba.citrus.service.requestcontext.rundata.impl;

import static com.alibaba.citrus.service.requestcontext.util.RequestContextUtil.*;
import static com.alibaba.citrus.util.Assert.*;

import java.io.IOException;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.citrus.service.requestcontext.RequestContext;
import com.alibaba.citrus.service.requestcontext.buffered.BufferedRequestContext;
import com.alibaba.citrus.service.requestcontext.lazycommit.LazyCommitRequestContext;
import com.alibaba.citrus.service.requestcontext.locale.SetLocaleRequestContext;
import com.alibaba.citrus.service.requestcontext.parser.CookieParser;
import com.alibaba.citrus.service.requestcontext.parser.ParameterParser;
import com.alibaba.citrus.service.requestcontext.parser.ParserRequestContext;
import com.alibaba.citrus.service.requestcontext.rundata.RunData;
import com.alibaba.citrus.service.requestcontext.rundata.User;
import com.alibaba.citrus.service.requestcontext.support.AbstractRequestContextWrapper;
import com.alibaba.citrus.util.StringUtil;
import com.alibaba.citrus.util.io.ByteArray;

/**
 * <code>RunData</code>的实现类。
 * 
 * @author Michael Zhou
 */
public class RunDataImpl extends AbstractRequestContextWrapper implements RunData {
    private final static Logger log = LoggerFactory.getLogger(RunData.class);
    private final BufferedRequestContext bufferedRequestContext;
    private final LazyCommitRequestContext lazyCommitRequestContext;
    private final SetLocaleRequestContext setLocaleRequestContext;
    private final ParserRequestContext parserRequestContext;
    private User user;

    public RunDataImpl(RequestContext wrappedContext) {
        super(wrappedContext);
        this.bufferedRequestContext = findRequestContext(wrappedContext, BufferedRequestContext.class);
        this.lazyCommitRequestContext = findRequestContext(wrappedContext, LazyCommitRequestContext.class);
        this.setLocaleRequestContext = findRequestContext(wrappedContext, SetLocaleRequestContext.class);
        this.parserRequestContext = findRequestContext(wrappedContext, ParserRequestContext.class);

        if (bufferedRequestContext == null) {
            log.debug("RunData feature BufferedRequestContext disabled");
        }

        if (lazyCommitRequestContext == null) {
            log.debug("RunData feature LazyCommitRequestContext disabled");
        }

        if (setLocaleRequestContext == null) {
            log.debug("RunData feature SetLocaleRequestContext disabled");
        }

        if (parserRequestContext == null) {
            log.debug("RunData feature ParserRequestContext disabled");
        }
    }

    protected BufferedRequestContext getBufferedRequestContext() {
        return assertNotNull(bufferedRequestContext, "Could not find BufferedRequestContext in request context chain");
    }

    protected LazyCommitRequestContext getLazyCommitRequestContext() {
        return assertNotNull(lazyCommitRequestContext,
                "Could not find LazyCommitRequestContext in request context chain");
    }

    protected SetLocaleRequestContext getSetLocaleRequestContext() {
        return assertNotNull(setLocaleRequestContext, "Could not find SetLocaleRequestContext in request context chain");
    }

    protected ParserRequestContext getParserRequestContext() {
        return assertNotNull(parserRequestContext, "Could not find ParserRequestContext in request context chain");
    }

    // ===================================================
    // HTTP request信息。
    // ===================================================

    /**
     * 取得所有query参数。第一次执行此方法时，将会解析request，从中取得所有的参数。
     * 
     * @return <code>ParameterParser</code>实例
     */
    public ParameterParser getParameters() {
        return getParserRequestContext().getParameters();
    }

    /**
     * 取得所有cookie。第一次执行此方法时，将会解析request，从中取得所有cookies。
     * 
     * @return <code>CookieParser</code>实例
     */
    public CookieParser getCookies() {
        return getParserRequestContext().getCookies();
    }

    /**
     * 取得当前请求的HTTP session。
     * 
     * @return HTTP session对象
     */
    public HttpSession getSession() {
        return getRequest().getSession();
    }

    /**
     * 取得web应用的上下文路径，相当于<code>HttpServletRequest.getContextPath</code>所返回的值。
     * 
     * @return web应用的上下文路径
     */
    public String getContextPath() {
        return getRequest().getContextPath();
    }

    /**
     * 取得servlet路径，相当于<code>HttpServletRequest.getServletPath</code>所返回的值。
     * 
     * @return servlet路径
     */
    public String getServletPath() {
        return getRequest().getServletPath();
    }

    /**
     * 取得path info路径，相当于<code>HttpServletRequest.getPathInfo</code>所返回的值。
     * 
     * @return path info路径
     */
    public String getPathInfo() {
        return getRequest().getPathInfo();
    }

    /**
     * 取得当前的request URL，包括query string。
     * 
     * @return 当前请求的request URL
     */
    public String getRequestURL() {
        return getRequestURL(true);
    }

    /**
     * 取得当前的request URL，包括query string。
     * 
     * @param withQueryString 是否包含query string
     * @return 当前请求的request URL
     */
    public String getRequestURL(boolean withQueryString) {
        StringBuffer buffer = getRequest().getRequestURL();

        if (withQueryString) {
            String queryString = StringUtil.trimToNull(getRequest().getQueryString());

            if (queryString != null) {
                buffer.append('?').append(queryString);
            }
        }

        return buffer.toString();
    }

    /**
     * 检查请求的类型是否为post。
     */
    public boolean isPostRequest() {
        return "post".equalsIgnoreCase(getRequest().getMethod());
    }

    // ===================================================
    // 附加信息。
    // ===================================================

    /**
     * 取得正在访问当前应用的用户。
     * 
     * @return 用户对象
     */
    public User getUser() {
        return this.user;
    }

    /**
     * 设置正在访问当前应用的用户。
     * 
     * @param user 用户对象
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * 取得和当前请求绑定的对象。当请求结束时，所有的attributes将被抛弃。
     * 
     * @param key 对象的key
     * @return 和key相对应的对象
     */
    public Object getAttribute(String key) {
        return getRequest().getAttribute(key);
    }

    /**
     * 将指定对象绑定到当前请求中。当请求结束时，所有的attributes将被抛弃。
     * 
     * @param key 对象的key
     * @param object 和key相对应的对象
     */
    public void setAttribute(String key, Object object) {
        if (object == null) {
            getRequest().removeAttribute(key);
        } else {
            getRequest().setAttribute(key, object);
        }
    }

    // ===================================================
    // HTTP response信息。
    // ===================================================

    /**
     * 取得content type。
     * 
     * @return content type，包括charset的定义
     */
    public String getContentType() {
        return getSetLocaleRequestContext().getResponseContentType();
    }

    /**
     * 设置content type。 如果content type不包含charset，并且
     * <code>getCharacterEncoding</code>被设置，则加上charset标记。
     * 
     * @param contentType content type
     */
    public void setContentType(String contentType) {
        getResponse().setContentType(contentType);
    }

    /**
     * 设置content type。 如果content type不包含charset，并且
     * <code>getCharacterEncoding</code>被设置，则加上charset标记。
     * <p>
     * 如果<code>appendCharset</code>为<code>false</code>，则content
     * type中将不包含charset标记。
     * </p>
     * 
     * @param contentType content type
     * @param appendCharset 输出字符集
     */
    public void setContentType(String contentType, boolean appendCharset) {
        getSetLocaleRequestContext().setResponseContentType(contentType, appendCharset);
    }

    /**
     * 取得response的输出字符集。
     */
    public String getCharacterEncoding() {
        return getResponse().getCharacterEncoding();
    }

    /**
     * 设置response输出字符集。注意，此方法必须在第一次<code>getWriter</code>之前执行。
     * 
     * @param charset 输出字符集，如果charset为<code>null</code>
     *            ，则从contentType中删除charset标记
     */
    public void setCharacterEncoding(String charset) {
        getSetLocaleRequestContext().setResponseCharacterEncoding(charset);
    }

    /**
     * 取得重定向的URI。
     * 
     * @return 重定向的URI，如果没有重定向，则返回<code>null</code>
     */
    public String getRedirectLocation() {
        return getLazyCommitRequestContext().getRedirectLocation();
    }

    /**
     * 设置重定向URI。
     * 
     * @param location 重定向的URI
     * @throws IOException 输入输出失败
     * @throws IllegalStateException 如果response已经committed
     */
    public void setRedirectLocation(String location) throws IOException {
        getResponse().sendRedirect(location);
    }

    /**
     * 判决系统是否已经重定向。
     * 
     * @return 如果<code>setRedirectLocation</code>被调用，则返回<code>true</code>
     */
    public boolean isRedirected() {
        return getLazyCommitRequestContext().isRedirected();
    }

    /**
     * 取得最近设置的HTTP status。
     * 
     * @return HTTP status值
     */
    public int getStatusCode() {
        return getLazyCommitRequestContext().getStatus();
    }

    /**
     * 设置HTTP status。
     * 
     * @param status HTTP status值
     */
    public void setStatusCode(int status) {
        getResponse().setStatus(status);
    }

    // ===================================================
    // Response buffer控制。
    // ===================================================

    /**
     * 设置是否将所有信息保存在内存中。
     * 
     * @return 如果是，则返回<code>true</code>
     */
    public boolean isBuffering() {
        return getBufferedRequestContext().isBuffering();
    }

    /**
     * 设置buffer模式，如果设置成<code>true</code>，表示将所有信息保存在内存中，否则直接输出到原始response中。
     * <p>
     * 此方法必须在<code>getOutputStream</code>和<code>getWriter</code>方法之前执行，否则将抛出
     * <code>IllegalStateException</code>。
     * </p>
     * 
     * @param buffering 是否buffer内容
     * @throws IllegalStateException <code>getOutputStream</code>或
     *             <code>getWriter</code>方法已经被执行
     */
    public void setBuffering(boolean buffering) {
        getBufferedRequestContext().setBuffering(buffering);
    }

    /**
     * 创建新的buffer，保存老的buffer。
     * 
     * @throws IllegalStateException 如果不在buffer模式，或<code>getWriter</code>及
     *             <code>getOutputStream</code>方法从未被调用
     */
    public void pushBuffer() {
        getBufferedRequestContext().pushBuffer();
    }

    /**
     * 弹出最近的buffer，如果堆栈中只有一个buffer，则弹出后再创建一个新的。
     * 
     * @return 最近的buffer内容
     * @throws IllegalStateException 如果不在buffer模式，或<code>getWriter</code>
     *             方法曾被调用，或<code>getOutputStream</code>方法从未被调用
     */
    public ByteArray popByteBuffer() {
        return getBufferedRequestContext().popByteBuffer();
    }

    /**
     * 弹出最近的buffer，如果堆栈中只有一个buffer，则弹出后再创建一个新的。
     * 
     * @return 最近的buffer内容
     * @throws IllegalStateException 如果不在buffer模式，或<code>getOutputStream</code>
     *             方法曾被调用，或<code>getWriter</code>方法从未被调用
     */
    public String popCharBuffer() {
        return getBufferedRequestContext().popCharBuffer();
    }

    /**
     * 清除所有buffers，常用于显示出错信息。
     * 
     * @throws IllegalStateException 如果response已经commit
     */
    public void resetBuffer() {
        getResponse().resetBuffer();
    }

    /**
     * 将指定的字符串根据<code>getCaseFolding()</code>的设置，转换成指定大小写形式。
     * 
     * @param str 要转换的字符串
     * @return 转换后的字符串
     */
    public String convertCase(String str) {
        return getParserRequestContext().convertCase(str);
    }
}
