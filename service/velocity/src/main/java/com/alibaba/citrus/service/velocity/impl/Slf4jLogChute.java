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
package com.alibaba.citrus.service.velocity.impl;

import static com.alibaba.citrus.util.Assert.*;

import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.log.LogChute;
import org.slf4j.Logger;

/**
 * 将日志输出到slf4j。
 * 
 * @author Michael Zhou
 */
public class Slf4jLogChute implements LogChute {
    private final Logger log;

    public Slf4jLogChute(Logger log) {
        this.log = assertNotNull(log, "log");
    }

    public void init(RuntimeServices rs) throws Exception {
    }

    public void log(int level, String message) {
        log(level, message, null);
    }

    public void log(int level, String message, Throwable t) {
        // 对WARN以下level不记录异常详情 ── 避免Velocity在找不到资源时会打印异常。
        switch (level) {
            case TRACE_ID:
            case DEBUG_ID:
            case INFO_ID:
                if (t != null) {
                    if (message == null) {
                        message = t.getMessage();
                        t = null;
                    } else {
                        message += ": " + t.getMessage();
                        t = null;
                    }
                }

                break;

            default:
        }

        switch (level) {
            case TRACE_ID:
                log.trace(message, t);
                break;

            case DEBUG_ID:
                log.debug(message, t);
                break;

            case INFO_ID:
                log.info(message, t);
                break;

            case WARN_ID:
                log.warn(message, t);
                break;

            case ERROR_ID:
                log.error(message, t);
                break;

            default:
        }
    }

    public boolean isLevelEnabled(int level) {
        switch (level) {
            case TRACE_ID:
                return log.isTraceEnabled();

            case DEBUG_ID:
                return log.isDebugEnabled();

            case INFO_ID:
                return log.isInfoEnabled();

            case WARN_ID:
                return log.isWarnEnabled();

            case ERROR_ID:
                return log.isErrorEnabled();

            default:
                return false;
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + log.getName() + "]";
    }
}
