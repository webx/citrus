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
 */

package com.alibaba.citrus.logconfig.logback;

import static ch.qos.logback.core.spi.FilterReply.*;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

public class LevelRangeFilter<E> extends Filter<E> {
    private boolean acceptOnMatch = false;
    private Level levelMin;
    private Level levelMax;

    public void setLevelMax(Level levelMax) {
        this.levelMax = levelMax;
    }

    public void setLevelMin(Level levelMin) {
        this.levelMin = levelMin;
    }

    public void setAcceptOnMatch(boolean acceptOnMatch) {
        this.acceptOnMatch = acceptOnMatch;
    }

    @Override
    public FilterReply decide(E eventObject) {
        if (!isStarted()) {
            return FilterReply.NEUTRAL;
        }

        LoggingEvent event = (LoggingEvent) eventObject;

        if (this.levelMin != null && !event.getLevel().isGreaterOrEqual(levelMin)) {
            return DENY;
        }

        if (this.levelMax != null && event.getLevel().toInt() > levelMax.toInt()) {
            return DENY;
        }

        if (acceptOnMatch) {
            return ACCEPT;
        } else {
            return NEUTRAL;
        }
    }

    @Override
    public void start() {
        if (levelMin != null || levelMax != null) {
            super.start();
        }
    }
}
