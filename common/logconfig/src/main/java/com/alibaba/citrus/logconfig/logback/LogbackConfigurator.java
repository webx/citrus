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
package com.alibaba.citrus.logconfig.logback;

import java.net.URL;
import java.util.Map;

import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.LogbackException;

import com.alibaba.citrus.logconfig.LogConfigurator;

public class LogbackConfigurator extends LogConfigurator {
    @Override
    protected void doConfigure(URL configFile, Map<String, String> props) throws Exception {
        JoranConfigurator configurator = new JoranConfigurator();

        configurator.setContext(getLoggerContext(props));
        configurator.doConfigure(configFile);
    }

    private LoggerContext getLoggerContext(Map<String, String> props) {
        ILoggerFactory lcObject = LoggerFactory.getILoggerFactory();

        if (!(lcObject instanceof LoggerContext)) {
            throw new LogbackException(
                    "Expected LOGBACK binding with SLF4J, but another log system has taken the place: "
                            + lcObject.getClass().getSimpleName());
        }

        LoggerContext lc = (LoggerContext) lcObject;

        lc.reset();

        for (Map.Entry<String, String> entry : props.entrySet()) {
            lc.putProperty(entry.getKey(), entry.getValue());
        }

        return lc;
    }

    @Override
    public void shutdown() {
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        lc.stop();
    }
}
