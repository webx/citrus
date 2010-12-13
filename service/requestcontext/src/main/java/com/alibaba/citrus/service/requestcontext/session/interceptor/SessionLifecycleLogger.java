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
package com.alibaba.citrus.service.requestcontext.session.interceptor;

import static com.alibaba.citrus.logconfig.support.ConfigurableLogger.Level.*;
import static com.alibaba.citrus.springext.util.SpringExtUtil.*;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.alibaba.citrus.logconfig.support.ConfigurableLogger.Level;
import com.alibaba.citrus.logconfig.support.SecurityLogger;
import com.alibaba.citrus.service.requestcontext.session.SessionConfig;
import com.alibaba.citrus.service.requestcontext.session.SessionLifecycleListener;
import com.alibaba.citrus.springext.support.parser.AbstractSingleBeanDefinitionParser;

/**
 * 用来记录session日志生命期事件的listener。
 * 
 * @author Michael Zhou
 */
public class SessionLifecycleLogger implements SessionLifecycleListener {
    private final static Level DEFAULT_LOG_LEVEL = debug;
    private final static Level DEFAULT_VISITED_LOG_LEVEL = trace;
    private final SecurityLogger log = new SecurityLogger();
    private Level logLevel;
    private Level visitLogLevel;

    public void setLogName(String name) {
        log.setLogName(name);
    }

    public void setLogLevel(Level level) {
        this.logLevel = level;
    }

    public void setVisitLogLevel(Level level) {
        this.visitLogLevel = level;
    }

    public void init(SessionConfig sessionConfig) {
        if (logLevel == null) {
            logLevel = DEFAULT_LOG_LEVEL;
        }

        if (visitLogLevel == null) {
            visitLogLevel = DEFAULT_VISITED_LOG_LEVEL;
        }
    }

    public void sessionCreated(HttpSession session) {
        if (log.isLevelEnabled(logLevel)) {
            log.log(logLevel, "session created, id=" + session.getId());
        }
    }

    public void sessionInvalidated(HttpSession session) {
        if (log.isLevelEnabled(logLevel)) {
            log.log(logLevel, "session invalidated, id=" + session.getId());
        }
    }

    public void sessionVisited(HttpSession session) {
        if (log.isLevelEnabled(visitLogLevel)) {
            log.log(visitLogLevel, "session visited, id=" + session.getId());
        }
    }

    public static class DefinitionParser extends AbstractSingleBeanDefinitionParser<SessionLifecycleLogger> {
        @Override
        protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
            attributesToProperties(element, builder);
        }
    }
}
