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

import static com.alibaba.citrus.service.requestcontext.rewrite.impl.RewriteUtil.*;
import static com.alibaba.citrus.util.ArrayUtil.*;
import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.BasicConstant.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.util.regex.MatchResult;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import com.alibaba.citrus.service.requestcontext.parser.ParameterParser;
import com.alibaba.citrus.util.ArrayUtil;
import com.alibaba.citrus.util.ObjectUtil;
import com.alibaba.citrus.util.StringEscapeUtil;
import com.alibaba.citrus.util.StringUtil;
import com.alibaba.citrus.util.internal.ToStringBuilder;
import com.alibaba.citrus.util.internal.ToStringBuilder.MapBuilder;
import com.alibaba.citrus.util.internal.regex.MatchResultSubstitution;

public class RewriteSubstitution implements InitializingBean {
    private static final Logger log = LoggerFactory.getLogger(RewriteSubstitution.class);
    private String uri;
    private SubstitutionFlags flags;
    private Parameter[] parameters;

    public void setUri(String uri) {
        this.uri = StringUtil.trimToNull(uri);
    }

    public SubstitutionFlags getFlags() {
        return flags;
    }

    public void setFlags(String[] flags) {
        this.flags = new SubstitutionFlags(flags);
    }

    public void setParameters(Parameter[] params) {
        this.parameters = params;
    }

    public void afterPropertiesSet() throws Exception {
        // flags
        if (flags == null) {
            flags = new SubstitutionFlags();
        }

        // params
        if (parameters == null) {
            parameters = new Parameter[0];
        }
    }

    public String substitute(String path, MatchResultSubstitution resultSubs) {
        if (uri == null) {
            if (log.isTraceEnabled()) {
                log.trace("No substitution applied to path: \"{}\"", StringEscapeUtil.escapeJava(path));
            }

            return path;
        }

        String subsPath = resultSubs.substitute(uri);

        if (!isFullURL(subsPath)) {
            MatchResult ruleMatchResult = resultSubs.getMatch();

            subsPath = path.substring(0, ruleMatchResult.start()) // before match
                    + subsPath // match
                    + path.substring(ruleMatchResult.end()); // after match
        }

        if (log.isDebugEnabled()) {
            log.debug("Rewriting \"{}\" to \"{}\"", StringEscapeUtil.escapeJava(path),
                    StringEscapeUtil.escapeJava(subsPath));
        }

        return subsPath;
    }

    public boolean substituteParameters(ParameterParser params, MatchResultSubstitution resultSubs) {
        boolean parameterSubstituted = false;

        if (!params.isEmpty()) {
            if (!flags.hasQSA()) {
                params.clear();
                parameterSubstituted = true;

                log.debug("All parameters have been cleared.  To prevent from clearing the parameters, "
                        + "just specify \"QSA\" or \"qsappend\"(query string append) flag to the substitution");
            }
        }

        for (Parameter param : parameters) {
            String key = param.getKey(resultSubs);
            String[] values = param.getValues(resultSubs);

            if (!StringUtil.isEmpty(key)) {
                if (ArrayUtil.isEmptyArray(values) || values.length == 1 && StringUtil.isEmpty(values[0])) {
                    params.remove(key);

                    if (log.isDebugEnabled()) {
                        log.debug("Removed parameter: \"{}\"", StringEscapeUtil.escapeJava(key));
                    }
                } else {
                    params.setStrings(key, values);

                    if (log.isDebugEnabled()) {
                        log.debug("Set parameter: \"{}\"=\"{}\"", StringEscapeUtil.escapeJava(key),
                                ObjectUtil.toString(values));
                    }
                }

                parameterSubstituted = true;
            }
        }

        return parameterSubstituted;
    }

    @Override
    public String toString() {
        MapBuilder mb = new MapBuilder();

        if (uri != null) {
            mb.append("uri", uri);
        }

        if (!isEmptyArray(parameters)) {
            mb.append("params", parameters);
        }

        if (!flags.isEmpty()) {
            mb.append("flags", flags);
        }

        return new ToStringBuilder().append("Substitution").append(mb).toString();
    }

    /**
     * 代表substitution的标志位。
     */
    public static class SubstitutionFlags extends Flags {
        public SubstitutionFlags() {
            super();
        }

        public SubstitutionFlags(String... flags) {
            super(flags);
        }

        /**
         * 标志位：保留所有GET、POST、UPLOAD所得的参数。
         */
        public boolean hasQSA() {
            return hasFlags("QSA", "qsappend");
        }

        /**
         * 标志位：如果一个规则被匹配，则继续处理其后继规则；如果该规则不被匹配，则其后继规则将被跳过。
         */
        public boolean hasC() {
            return hasFlags("C", "chain");
        }

        /**
         * 标志位：如果一个规则被匹配，并指明该参数，则立即结束。
         */
        public boolean hasL() {
            return hasFlags("L", "last");
        }

        /**
         * 标志位：重定向，并取得redirect code。如果未指定重定向，则返回<code>0</code>。
         * <p>
         * 默认为<code>302 moved temporarily</code>。如指定<code>R=301</code>则表示
         * <code>301 moved permanently</code>。
         * </p>
         */
        public int getRedirectCode() {
            String value = getFlagValue("R", "redirect");
            int defaultStatusCode = HttpServletResponse.SC_MOVED_TEMPORARILY;

            if (value == null) {
                return 0;
            } else if (value.length() == 0) {
                return defaultStatusCode;
            } else {
                try {
                    return Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    return defaultStatusCode;
                }
            }
        }
    }

    public static class Parameter implements InitializingBean {
        private String key;
        private String[] values;

        public void setKey(String key) {
            this.key = trimToNull(key);
        }

        public void setValue(String value) {
            this.values = new String[] { value };
        }

        public void setValues(String[] values) {
            this.values = values;
        }

        public void afterPropertiesSet() throws Exception {
            assertNotNull(key, "missing key attribute for parameter");

            if (values == null) {
                values = EMPTY_STRING_ARRAY;
            }
        }

        public String getKey(MatchResultSubstitution resultSubs) {
            return resultSubs.substitute(key);
        }

        public String[] getValues(MatchResultSubstitution resultSubs) {
            String[] subsValues = new String[values.length];

            for (int i = 0; i < values.length; i++) {
                subsValues[i] = resultSubs.substitute(values[i]);
            }

            return subsValues;
        }

        @Override
        public String toString() {
            ToStringBuilder buf = new ToStringBuilder().append(key).append(" = ");

            if (values.length == 1) {
                buf.append(values[0]);
            } else {
                buf.appendArray(values);
            }

            return buf.toString();
        }
    }
}
