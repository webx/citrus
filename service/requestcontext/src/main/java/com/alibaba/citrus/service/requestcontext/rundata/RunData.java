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
package com.alibaba.citrus.service.requestcontext.rundata;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.alibaba.citrus.service.requestcontext.RequestContext;
import com.alibaba.citrus.service.requestcontext.parser.CookieParser;
import com.alibaba.citrus.service.requestcontext.parser.ParameterParser;
import com.alibaba.citrus.util.io.ByteArray;

/**
 * <code>RunData</code>提供了对常用request context一站式的访问方法。
 * <p>
 * 这不是一个必备的request context，你也可以直接取得下层的request
 * context来达到同样的功能。但rundata为应用提供了一个快捷方式。
 * </p>
 * 
 * @author Michael Zhou
 */
public interface RunData extends RequestContext {
    // ===================================================
    // HTTP request信息。
    // ===================================================

    /**
     * 取得所有query参数。
     * 
     * @return <code>ParameterParser</code>实例
     */
    ParameterParser getParameters();

    /**
     * 取得所有cookie。
     * 
     * @return <code>CookieParser</code>实例
     */
    CookieParser getCookies();

    /**
     * 取得当前正在处理的HTTP请求。
     * 
     * @return HTTP请求对象
     */
    HttpServletRequest getRequest();

    /**
     * 取得当前正在处理的HTTP响应。
     * 
     * @return HTTP响应对象
     */
    HttpServletResponse getResponse();

    /**
     * 取得当前请求的HTTP session。
     * 
     * @return HTTP session对象
     */
    HttpSession getSession();

    /**
     * 取得创建当前servlet的container的上下文信息。
     * 
     * @return 创建当前servlet的container的上下文信息
     */
    ServletContext getServletContext();

    /**
     * 取得web应用的上下文路径，相当于<code>HttpServletRequest.getContextPath</code>所返回的值。
     * 
     * @return web应用的上下文路径
     */
    String getContextPath();

    /**
     * 取得servlet路径，相当于<code>HttpServletRequest.getServletPath</code>所返回的值。
     * 
     * @return servlet路径
     */
    String getServletPath();

    /**
     * 取得path info路径，相当于<code>HttpServletRequest.getPathInfo</code>所返回的值。
     * 
     * @return path info路径
     */
    String getPathInfo();

    /**
     * 取得当前的request URL，包括query string。
     * 
     * @return 当前请求的request URL
     */
    String getRequestURL();

    /**
     * 取得当前的request URL，包括query string。
     * 
     * @param withQueryString 是否包含query string
     * @return 当前请求的request URL
     */
    String getRequestURL(boolean withQueryString);

    /**
     * 检查请求的类型是否为post。
     */
    boolean isPostRequest();

    // ===================================================
    // 附加信息。
    // ===================================================

    /**
     * 取得正在访问当前应用的用户。
     * 
     * @return 用户对象
     */
    User getUser();

    /**
     * 设置正在访问当前应用的用户。
     * 
     * @param user 用户对象
     */
    void setUser(User user);

    /**
     * 取得和当前请求绑定的对象。当请求结束时，所有的attributes将被抛弃。
     * 
     * @param key 对象的key
     * @return 和key相对应的对象
     */
    Object getAttribute(String key);

    /**
     * 将指定对象绑定到当前请求中。当请求结束时，所有的attributes将被抛弃。
     * 
     * @param key 对象的key
     * @param object 和key相对应的对象
     */
    void setAttribute(String key, Object object);

    // ===================================================
    // HTTP response信息。
    // ===================================================

    /**
     * 取得content type。
     * 
     * @return content type，包括charset的定义
     */
    String getContentType();

    /**
     * 设置content type。 如果content type不包含charset，并且
     * <code>getCharacterEncoding</code>被设置，则加上charset标记。
     * 
     * @param contentType content type
     */
    void setContentType(String contentType);

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
    void setContentType(String contentType, boolean appendCharset);

    /**
     * 取得response的输出字符集。
     */
    String getCharacterEncoding();

    /**
     * 设置response输出字符集。注意，此方法必须在第一次<code>getWriter</code>之前执行。
     * 
     * @param charset 输出字符集，如果charset为<code>null</code>
     *            ，则从contentType中删除charset标记
     */
    void setCharacterEncoding(String charset);

    /**
     * 取得重定向的URI。
     * 
     * @return 重定向的URI，如果没有重定向，则返回<code>null</code>
     */
    String getRedirectLocation();

    /**
     * 设置重定向URI。
     * 
     * @param location 重定向的URI
     * @throws IOException 输入输出失败
     * @throws IllegalStateException 如果response已经committed
     */
    void setRedirectLocation(String location) throws IOException;

    /**
     * 判决系统是否已经重定向。
     * 
     * @return 如果<code>setRedirectLocation</code>被调用，则返回<code>true</code>
     */
    boolean isRedirected();

    /**
     * 取得最近设置的HTTP status。
     * 
     * @return HTTP status值
     */
    int getStatusCode();

    /**
     * 设置HTTP status。
     * 
     * @param status HTTP status值
     */
    void setStatusCode(int status);

    // ===================================================
    // Response buffer控制。
    // ===================================================

    /**
     * 设置是否将所有信息保存在内存中。
     * 
     * @return 如果是，则返回<code>true</code>
     */
    boolean isBuffering();

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
    void setBuffering(boolean buffering);

    /**
     * 创建新的buffer，保存老的buffer。
     * 
     * @throws IllegalStateException 如果不在buffer模式，或<code>getWriter</code>及
     *             <code>getOutputStream</code>方法从未被调用
     */
    void pushBuffer();

    /**
     * 弹出最近的buffer，如果堆栈中只有一个buffer，则弹出后再创建一个新的。
     * 
     * @return 最近的buffer内容
     * @throws IllegalStateException 如果不在buffer模式，或<code>getWriter</code>
     *             方法曾被调用，或<code>getOutputStream</code>方法从未被调用
     */
    ByteArray popByteBuffer();

    /**
     * 弹出最近的buffer，如果堆栈中只有一个buffer，则弹出后再创建一个新的。
     * 
     * @return 最近的buffer内容
     * @throws IllegalStateException 如果不在buffer模式，或<code>getOutputStream</code>
     *             方法曾被调用，或<code>getWriter</code>方法从未被调用
     */
    String popCharBuffer();

    /**
     * 清除所有buffers，常用于显示出错信息。
     * 
     * @throws IllegalStateException 如果response已经commit
     */
    void resetBuffer();

    /**
     * 将指定的字符串根据<code>getCaseFolding()</code>的设置，转换成指定大小写形式。
     * 
     * @param str 要转换的字符串
     * @return 转换后的字符串
     */
    String convertCase(String str);
}
