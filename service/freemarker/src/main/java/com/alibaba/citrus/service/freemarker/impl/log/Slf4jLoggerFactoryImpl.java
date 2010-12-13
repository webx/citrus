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
package com.alibaba.citrus.service.freemarker.impl.log;

import static com.alibaba.citrus.util.Assert.*;
import freemarker.log.Logger;
import freemarker.log.PublicLoggerFactory;

public class Slf4jLoggerFactoryImpl implements PublicLoggerFactory {
    public Logger getLogger(String category) {
        return new Slf4jLogger(org.slf4j.LoggerFactory.getLogger(assertNotNull(category, "category")));
    }

    private static class Slf4jLogger extends Logger {
        private final org.slf4j.Logger log;

        public Slf4jLogger(org.slf4j.Logger log) {
            this.log = assertNotNull(log, "log");
        }

        @Override
        public void debug(String message) {
            log.debug(message);
        }

        @Override
        public void debug(String message, Throwable t) {
            log.debug(message, t);
        }

        @Override
        public void error(String message) {
            log.error(message);
        }

        @Override
        public void error(String message, Throwable t) {
            log.error(message, t);
        }

        @Override
        public void info(String message) {
            log.info(message);
        }

        @Override
        public void info(String message, Throwable t) {
            log.info(message, t);
        }

        @Override
        public void warn(String message) {
            log.warn(message);
        }

        @Override
        public void warn(String message, Throwable t) {
            log.warn(message, t);
        }

        @Override
        public boolean isDebugEnabled() {
            return log.isDebugEnabled();
        }

        @Override
        public boolean isInfoEnabled() {
            return log.isInfoEnabled();
        }

        @Override
        public boolean isWarnEnabled() {
            return log.isWarnEnabled();
        }

        @Override
        public boolean isErrorEnabled() {
            return log.isErrorEnabled();
        }

        @Override
        public boolean isFatalEnabled() {
            return log.isErrorEnabled();
        }

        @Override
        public String toString() {
            return log.toString();
        }
    }
}
