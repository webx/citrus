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
package com.alibaba.citrus.service.requestcontext.rewrite.impl;

import java.util.regex.MatchResult;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.citrus.service.requestcontext.parser.ParserRequestContext;
import com.alibaba.citrus.service.requestcontext.util.RequestContextUtil;
import com.alibaba.citrus.util.internal.regex.MatchResultSubstitution;

/**
 * 和rewrite相关的工具类。
 * 
 * @author Michael Zhou
 */
public class RewriteUtil {
    public static boolean isFullURL(String path) {
        return path.matches("^\\w+:.*");
    }

    public static MatchResultSubstitution getMatchResultSubstitution(MatchResult ruleMatchResult,
                                                                     MatchResult conditionMatchResult) {
        return new MatchResultSubstitution("$%", ruleMatchResult, conditionMatchResult);
    }

    public static String getSubstitutedTestString(String testString, MatchResult ruleMatchResult,
                                                  MatchResult conditionMatchResult, HttpServletRequest request) {
        testString = eval(testString, request);

        return getMatchResultSubstitution(ruleMatchResult, conditionMatchResult).substitute(testString);
    }

    protected static String eval(String expr, HttpServletRequest request) {
        int length = expr.length();
        int startIndex = expr.indexOf("%{");

        // 如果表达式不包含%{}，则直接返回之。
        if (startIndex < 0) {
            return expr;
        }

        int endIndex = expr.indexOf("}", startIndex + 2);

        if (endIndex < 0) {
            return expr;
        }

        // 创建复合的表达式。
        StringBuffer resultBuffer = new StringBuffer();
        StringBuffer charBuffer = new StringBuffer();
        StringBuffer varNameBuffer = new StringBuffer();

        MAIN_LOOP: for (int i = 0; i < length;) {
            char ch = expr.charAt(i);

            switch (ch) {
                case '%':

                    if (i + 1 < length) {
                        ch = expr.charAt(++i);

                        switch (ch) {
                            case '%':
                                charBuffer.append(ch);
                                break;

                            case '{':

                                if (charBuffer.length() > 0) {
                                    resultBuffer.append(charBuffer);
                                    charBuffer.setLength(0);
                                }

                                if (i + 1 < length) {
                                    ++i;

                                    while (i < length) {
                                        ch = expr.charAt(i);

                                        switch (ch) {
                                            case '}':

                                                String varName = varNameBuffer.toString();
                                                String varNameExpanded = expand(varName, request);

                                                if (varNameExpanded == null) {
                                                    resultBuffer.append("%{").append(varName).append("}");
                                                } else {
                                                    resultBuffer.append(varNameExpanded);
                                                }

                                                varNameBuffer.setLength(0);
                                                ++i;
                                                continue MAIN_LOOP;

                                            default:
                                                varNameBuffer.append(ch);
                                                ++i;
                                        }
                                    }

                                    if (varNameBuffer.length() > 0) {
                                        resultBuffer.append("%{").append(varNameBuffer);
                                    }
                                }

                                break;

                            default:
                                charBuffer.append(ch);
                        }
                    } else {
                        charBuffer.append(ch);
                    }

                    break;

                default:
                    charBuffer.append(ch);
            }

            i++;
        }

        if (charBuffer.length() > 0) {
            resultBuffer.append(charBuffer);
        }

        return resultBuffer.toString();
    }

    /**
     * 展开变量。
     * 
     * @return 注意，如果返回null，表示按原样显示，例如：%{XYZ}
     */
    private static String expand(String varName, HttpServletRequest request) {
        boolean valid = true;
        String result;

        // =====================================================
        //  Client side of the IP connection
        // =====================================================

        if ("REMOTE_HOST".equals(varName)) {
            result = request.getRemoteHost();
        }
        //
        else if ("REMOTE_ADDR".equals(varName)) {
            result = request.getRemoteAddr();
        }
        //
        else if ("REMOTE_USER".equals(varName)) {
            result = request.getRemoteUser();
        }
        //
        else if ("REQUEST_METHOD".equals(varName)) {
            result = request.getMethod();
        }
        //
        else if ("QUERY_STRING".equals(varName)) {
            if ("post".equalsIgnoreCase(request.getMethod())) {
                ParserRequestContext parserRequestContext = RequestContextUtil.findRequestContext(request,
                        ParserRequestContext.class);

                result = parserRequestContext.getParameters().toQueryString();
            } else {
                result = request.getQueryString();
            }
        }
        //
        else if (varName.startsWith("QUERY:")) {
            ParserRequestContext parserRequestContext = RequestContextUtil.findRequestContext(request,
                    ParserRequestContext.class);

            result = parserRequestContext.getParameters().getString(varName.substring("QUERY:".length()).trim());
        }
        //
        else if ("AUTH_TYPE".equals(varName)) {
            result = request.getAuthType();
        }

        // =====================================================
        //  HTTP layer details extracted from HTTP headers
        // =====================================================

        else if ("SERVER_NAME".equals(varName)) {
            result = request.getServerName();
        }
        //
        else if ("SERVER_PORT".equals(varName)) {
            result = String.valueOf(request.getServerPort());
        }
        //
        else if ("SERVER_PROTOCOL".equals(varName)) {
            result = request.getProtocol();
        }

        // =====================================================
        //  HTTP headers
        // =====================================================

        else if ("HTTP_USER_AGENT".equals(varName)) {
            result = request.getHeader("User-Agent");
        }
        //
        else if ("HTTP_REFERER".equals(varName)) {
            result = request.getHeader("Referer");
        }
        //
        else if ("HTTP_HOST".equals(varName)) {
            result = request.getHeader("Host");
        }
        //
        else if ("HTTP_ACCEPT".equals(varName)) {
            result = request.getHeader("Accept");
        }
        //
        else if ("HTTP_COOKIE".equals(varName)) {
            result = request.getHeader("Cookie");
        }

        // =====================================================
        //  Others
        // =====================================================

        else if ("REQUEST_URI".equals(varName)) {
            result = request.getRequestURI();
        } else {
            result = null;
            valid = false;
        }

        // 如果变量合法，但值为null，则返回""
        if (valid && result == null) {
            result = "";
        }

        return result;
    }
}
