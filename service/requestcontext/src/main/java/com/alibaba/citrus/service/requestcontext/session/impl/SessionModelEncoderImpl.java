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
package com.alibaba.citrus.service.requestcontext.session.impl;

import static com.alibaba.citrus.util.BasicConstant.*;
import static com.alibaba.citrus.util.ObjectUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.citrus.service.requestcontext.session.SessionModel;
import com.alibaba.citrus.service.requestcontext.session.SessionModel.Factory;
import com.alibaba.citrus.service.requestcontext.session.SessionModelEncoder;
import com.alibaba.citrus.springext.support.parser.AbstractSingleBeanDefinitionParser;

/**
 * <code>SessionModelEncoder</code>的默认实现：将model内容保存成字符串。
 * 
 * @author Michael Zhou
 */
public class SessionModelEncoderImpl implements SessionModelEncoder {
    private static final Logger log = LoggerFactory.getLogger(SessionModelEncoderImpl.class);
    private static final String pattern = "'{'id:\"{0}\",ct:{1,number,#},ac:{2,number,#},mx:{3,number,#}'}'";

    public Object encode(SessionModel model) {
        Object[] args = { defaultIfNull(model.getSessionID(), EMPTY_STRING), //
                model.getCreationTime(), //
                model.getLastAccessedTime(), //
                model.getMaxInactiveInterval() //
        };

        String data = new MessageFormat(pattern).format(args);

        if (log.isDebugEnabled()) {
            log.debug("Stored session model data: {}", data);
        }

        return data;
    }

    public SessionModel decode(Object data, Factory factory) {
        SessionModel model = null;

        if (data instanceof String) {
            log.trace("Trying to parse session model data: {}", data);

            try {
                Object[] values = new MessageFormat(pattern).parse((String) data);

                String sessionID = trimToNull((String) values[0]);
                long creationTime = (Long) values[1];
                long lastAccessedTime = (Long) values[2];
                int maxInactiveInterval = ((Long) values[3]).intValue();

                model = factory.newInstance(sessionID, creationTime, lastAccessedTime, maxInactiveInterval);
            } catch (Exception e) {
                if (log.isDebugEnabled()) {
                    log.debug("Could not parse session model data: " + data, e);
                }
            }
        }

        return model;
    }

    public static class DefinitionParser extends AbstractSingleBeanDefinitionParser<SessionModelEncoderImpl> {
    }
}
