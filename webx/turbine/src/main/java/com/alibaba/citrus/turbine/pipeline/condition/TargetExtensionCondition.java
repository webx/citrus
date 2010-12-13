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
package com.alibaba.citrus.turbine.pipeline.condition;

import static com.alibaba.citrus.springext.util.SpringExtUtil.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.FileUtil.*;

import java.util.Set;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.alibaba.citrus.service.pipeline.PipelineStates;
import com.alibaba.citrus.service.pipeline.support.AbstractConditionDefinitionParser;
import com.alibaba.citrus.util.StringUtil;

/**
 * 根据target后缀来判断。
 */
public class TargetExtensionCondition extends AbstractTurbineCondition {
    private final Set<String> extensions = createHashSet();

    public void setExtension(String exts) {
        extensions.clear();

        // 支持无后缀
        if (exts == null) {
            extensions.add(null);
        } else {
            String[] extArray = StringUtil.split(exts, ", ");

            for (String ext : extArray) {
                extensions.add(normalizeExtension(ext));
            }
        }
    }

    public boolean isSatisfied(PipelineStates states) {
        String ext = getExtension(getRunData().getTarget(), "null", true);

        if (extensions.contains(ext)) {
            log.debug("Target extension matched: {}", ext);
            return true;
        }

        return false;
    }

    public static class DefinitionParser extends AbstractConditionDefinitionParser<TargetExtensionCondition> {
        @Override
        protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
            attributesToProperties(element, builder, "extension");
        }
    }

}
