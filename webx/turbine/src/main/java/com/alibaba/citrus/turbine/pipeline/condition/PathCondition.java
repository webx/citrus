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
import static com.alibaba.citrus.util.ObjectUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.alibaba.citrus.service.pipeline.PipelineStates;
import com.alibaba.citrus.service.pipeline.support.AbstractConditionDefinitionParser;
import com.alibaba.citrus.util.FileUtil;
import com.alibaba.citrus.util.ServletUtil;
import com.alibaba.citrus.util.StringUtil;
import com.alibaba.citrus.util.internal.regex.MatchResultSubstitution;
import com.alibaba.citrus.util.internal.regex.Substitution;

/**
 * ¸ù¾ÝservletPath + componentPath + pathInfoÀ´ÅÐ¶Ï¡£
 * 
 * @author Michael Zhou
 */
public class PathCondition extends AbstractTurbineCondition {
    public final static String DEFAULT_VAR = "subst";
    private Pattern[] patterns;
    private String[] patternStrings;
    private String var;

    public void setName(String patterns) {
        if (patterns != null) {
            this.patternStrings = StringUtil.split(patterns, ", ");
            this.patterns = new Pattern[patternStrings.length];

            for (int i = 0; i < patternStrings.length; i++) {
                this.patterns[i] = Pattern.compile(patternStrings[i]);
            }
        }
    }

    public void setVar(String var) {
        this.var = trimToNull(var);
    }

    public final boolean isSatisfied(PipelineStates states) {
        String path = FileUtil.normalizeAbsolutePath(getPath());

        for (int i = 0; i < patterns.length; i++) {
            Matcher matcher = patterns[i].matcher(path);

            if (matcher.find()) {
                log(patternStrings[i]);

                String var = defaultIfNull(this.var, DEFAULT_VAR);
                Substitution subst = new MatchResultSubstitution(matcher);

                states.setAttribute(var, subst);

                return true;
            }
        }

        return false;
    }

    protected String getPath() {
        return ServletUtil.getResourcePath(getRunData().getRequest());
    }

    protected void log(String patternString) {
        log.debug("URL path(servletPath/pathInfo) matched pattern: {}", patternString);
    }

    public static class DefinitionParser extends AbstractPathConditionDefinitionParser<PathCondition> {
    }

    protected static abstract class AbstractPathConditionDefinitionParser<T extends PathCondition> extends
            AbstractConditionDefinitionParser<T> {
        @Override
        protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
            attributesToProperties(element, builder, "name", "var");
        }
    }
}
