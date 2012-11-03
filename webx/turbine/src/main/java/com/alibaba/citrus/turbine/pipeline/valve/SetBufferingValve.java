/*
 * Copyright (c) 2002-2012 Alibaba Group Holding Limited.
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

package com.alibaba.citrus.turbine.pipeline.valve;

import static com.alibaba.citrus.springext.util.SpringExtUtil.*;

import com.alibaba.citrus.service.pipeline.PipelineContext;
import com.alibaba.citrus.service.pipeline.support.AbstractValve;
import com.alibaba.citrus.service.pipeline.support.AbstractValveDefinitionParser;
import com.alibaba.citrus.service.requestcontext.buffered.BufferedRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * 开关buffering。
 *
 * @author Michael Zhou
 */
public class SetBufferingValve extends AbstractValve {
    private Logger log = LoggerFactory.getLogger(getClass());
    private boolean enabled;

    @Autowired
    private BufferedRequestContext brc;

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public void invoke(PipelineContext pipelineContext) throws Exception {
        try {
            brc.setBuffering(enabled);
        } catch (IllegalStateException e) {
            log.warn("Unable to setBuffering({}): {}", enabled, e.getMessage());
        }

        pipelineContext.invokeNext();
    }

    public static class DefinitionParser extends AbstractValveDefinitionParser<SetBufferingValve> {
        @Override
        protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
            attributesToProperties(element, builder, "enabled");
        }
    }
}
