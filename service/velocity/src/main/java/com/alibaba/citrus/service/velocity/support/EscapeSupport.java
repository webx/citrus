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
package com.alibaba.citrus.service.velocity.support;

import static com.alibaba.citrus.util.ArrayUtil.*;
import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.BasicConstant.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.velocity.app.event.ReferenceInsertionEventHandler;
import org.apache.velocity.context.Context;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.TemplateInitException;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.parser.node.Node;
import org.apache.velocity.util.ContextAware;
import org.apache.velocity.util.introspection.Info;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import com.alibaba.citrus.service.configuration.ProductionModeAware;
import com.alibaba.citrus.service.velocity.FastCloneable;
import com.alibaba.citrus.service.velocity.VelocityConfiguration;
import com.alibaba.citrus.service.velocity.VelocityPlugin;
import com.alibaba.citrus.util.StringEscapeUtil;
import com.alibaba.citrus.util.internal.ToStringBuilder;
import com.alibaba.citrus.util.internal.ToStringBuilder.MapBuilder;

public class EscapeSupport implements VelocityPlugin, ReferenceInsertionEventHandler, ContextAware, FastCloneable,
        ProductionModeAware {
    private final static Logger log = LoggerFactory.getLogger(EscapeSupport.class);
    private final static String ESCAPE_TYPE_KEY = "_ESCAPE_SUPPORT_TYPE_";
    private ResourceLoader loader;
    private EscapeType defaultEscape;
    private EscapeRule[] escapeRules;
    private boolean cacheReferences;
    private Map<String, EscapeType> referenceCache = createConcurrentHashMap();
    private transient Context context;

    public Object createCopy() {
        EscapeSupport copy = new EscapeSupport();

        copy.loader = loader;
        copy.defaultEscape = defaultEscape;
        copy.escapeRules = escapeRules;
        copy.cacheReferences = cacheReferences;
        copy.referenceCache = referenceCache;

        return copy;
    }

    public void setProductionMode(boolean productionMode) {
        this.cacheReferences = productionMode;
    }

    public void setDefaultEscape(String defaultEscapeType) {
        this.defaultEscape = EscapeType.getEscapeType(defaultEscapeType);
    }

    public void setEscapeRules(EscapeRule[] escapeRules) {
        this.escapeRules = escapeRules;
    }

    public void init(VelocityConfiguration configuration) throws Exception {
        this.loader = configuration.getResourceLoader();

        configuration.getProperties().addProperty("userdirective", Escape.class.getName());
        configuration.getProperties().addProperty("userdirective", Noescape.class.getName());
    }

    public Resource[] getMacros() throws IOException {
        String resourceName = "classpath:" + getClass().getPackage().getName().replace('.', '/') + "/escape_macros.vm";
        Resource resource = assertNotNull(loader, "loader").getResource(resourceName);

        return new Resource[] { resource };
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public Object referenceInsert(String reference, Object value) {
        assertNotNull(context, "context");

        if (value == null) {
            return null;
        }

        // 如果当前引用是在#set($v = "$ref")中的，则不进行escape，推迟到最终输出时再escape。
        if (InterpolationUtil.isInInterpolation(context)) {
            return value;
        }

        EscapeType escapeType = getEscapeType(reference);

        if (escapeType == null) {
            return value;
        }

        return escapeType.escape(value.toString());
    }

    private EscapeType getEscapeType(String reference) {
        // 1. 假如明确指定了#escape或#noescape，则使用之。
        EscapeType escapeType = (EscapeType) context.get(ESCAPE_TYPE_KEY);

        if (escapeType != null) {
            log.debug("{} specified for reference {}", escapeType, reference);
            return escapeType;
        }

        // 2. 假如未明确指定，则查找规则
        // 3. 假如没有规则，或规则未匹配，则使用默认值
        if (cacheReferences) {
            escapeType = referenceCache.get(reference);
        }

        if (escapeType == null) {
            escapeType = findEscapeType(reference);

            if (cacheReferences) {
                referenceCache.put(reference, escapeType);
            }
        }

        return escapeType;
    }

    private EscapeType findEscapeType(String reference) {
        EscapeType escapeType = null;
        String normalizedRef = normalizeReference(reference);

        if (!isEmptyArray(escapeRules)) {
            for (EscapeRule rule : escapeRules) {
                Pattern matchedPattern = rule.matches(normalizedRef);

                if (matchedPattern != null) {
                    escapeType = rule.getEscapeType();

                    if (log.isDebugEnabled()) {
                        log.debug("{} matched {} for reference {}", new Object[] { escapeType, matchedPattern,
                                reference });
                    }

                    break;
                }
            }
        }

        if (escapeType == null) {
            escapeType = defaultEscape;
            log.debug("{} used by default for reference {}", escapeType, reference);
        }

        return escapeType;
    }

    private static boolean renderWithEscape(EscapeType escapeType, InternalContextAdapter context, Writer writer,
                                            Node node) throws IOException, ResourceNotFoundException,
            ParseErrorException, MethodInvocationException {
        EscapeType savedEscapeType = setEscapeType(escapeType, context);

        try {
            return node.render(context, writer);
        } finally {
            setEscapeType(savedEscapeType, context);
        }
    }

    public static EscapeType setEscapeType(EscapeType escapeType, InternalContextAdapter context) {
        if (escapeType == null) {
            return (EscapeType) context.remove(ESCAPE_TYPE_KEY);
        } else {
            return (EscapeType) context.localPut(ESCAPE_TYPE_KEY, escapeType);
        }
    }

    private static final Pattern referencePattern = Pattern
            .compile("\\s*\\$\\s*\\!?\\s*(\\{\\s*(.*?)\\s*\\}|(.*?))\\s*");

    private static String normalizeReference(String reference) {
        if (reference == null) {
            return EMPTY_STRING;
        }

        Matcher matcher = referencePattern.matcher(reference);

        if (matcher.matches()) {
            String form1 = matcher.group(2);
            String form2 = matcher.group(3);

            if (form1 == null) {
                return form2;
            } else {
                return form1;
            }
        } else {
            return reference;
        }
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        // TODO Auto-generated method stub
        return super.clone();
    }

    public static enum EscapeType {
        NO_ESCAPE("noescape") {
            @Override
            public String escape(String str) {
                return str;
            }

            @Override
            public String toString() {
                return "#noescape()";
            }
        },

        JAVA("java") {
            @Override
            public String escape(String str) {
                return StringEscapeUtil.escapeJava(str);
            }
        },

        JAVA_SCRIPT("javascript") {
            @Override
            public String escape(String str) {
                return StringEscapeUtil.escapeJavaScript(str);
            }
        },

        HTML("html") {
            @Override
            public String escape(String str) {
                return StringEscapeUtil.escapeHtml(str);
            }
        },

        XML("xml") {
            @Override
            public String escape(String str) {
                return StringEscapeUtil.escapeXml(str);
            }
        },

        URL("url") {
            @Override
            public String escape(String str) {
                return StringEscapeUtil.escapeURL(str);
            }
        },

        SQL("sql") {
            @Override
            public String escape(String str) {
                return StringEscapeUtil.escapeSql(str);
            }
        };

        private static final Map<String, EscapeType> namedTypes = createHashMap();
        private static final ArrayList<String> names = createArrayList();
        private final String name;

        static {
            for (EscapeType escapeType : EscapeType.values()) {
                namedTypes.put(escapeType.getName(), escapeType);
                names.add(escapeType.getName());
            }

            names.trimToSize();
        }

        private EscapeType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public abstract String escape(String str);

        public static EscapeType getEscapeType(String name) {
            name = trimToNull(name);

            if (name != null) {
                return namedTypes.get(name.toLowerCase());
            }

            return null;
        }

        public static List<String> getNames() {
            return names;
        }

        @Override
        public String toString() {
            return "#escape(\"" + getName() + "\")";
        }
    }

    /**
     * Escape directive。
     */
    public static class Escape extends Directive {
        @Override
        public String getName() {
            return "escape";
        }

        @Override
        public int getType() {
            return BLOCK;
        }

        @Override
        public void init(RuntimeServices rs, InternalContextAdapter context, Node node) throws TemplateInitException {
            super.init(rs, context, node);

            if (node.jjtGetNumChildren() != 2) {
                throw new TemplateInitException("Invalid args for #" + getName()
                        + ".  Expected 1 and only 1 string arg.", context.getCurrentTemplateName(), node.getColumn(),
                        node.getLine());
            }
        }

        @Override
        public boolean render(InternalContextAdapter context, Writer writer, Node node) throws IOException,
                ResourceNotFoundException, ParseErrorException, MethodInvocationException {
            Node escapeTypeNode = node.jjtGetChild(0);
            Object escapeTypeObject = escapeTypeNode.value(context);
            String escapeTypeString = escapeTypeObject == null ? null : escapeTypeObject.toString();
            EscapeType escapeType = EscapeType.getEscapeType(escapeTypeString);

            if (escapeType == null) {
                throw new ParseErrorException("Invalid escape type: "
                        + escapeTypeObject
                        + " at "
                        + new Info(escapeTypeNode.getTemplateName(), escapeTypeNode.getColumn(),
                                escapeTypeNode.getLine()) + ".  Available escape types: " + EscapeType.getNames());
            }

            return renderWithEscape(escapeType, context, writer, node.jjtGetChild(1));
        }
    }

    /**
     * Noescape directive。
     */
    public static class Noescape extends Directive {
        @Override
        public String getName() {
            return "noescape";
        }

        @Override
        public int getType() {
            return BLOCK;
        }

        @Override
        public void init(RuntimeServices rs, InternalContextAdapter context, Node node) throws TemplateInitException {
            super.init(rs, context, node);

            if (node.jjtGetNumChildren() != 1) {
                throw new TemplateInitException("Invalid args for #" + getName() + ".  Expected 0 args.",
                        context.getCurrentTemplateName(), node.getColumn(), node.getLine());
            }
        }

        @Override
        public boolean render(InternalContextAdapter context, Writer writer, Node node) throws IOException,
                ResourceNotFoundException, ParseErrorException, MethodInvocationException {
            return renderWithEscape(EscapeType.NO_ESCAPE, context, writer, node.jjtGetChild(0));
        }
    }

    public static class EscapeRule {
        private final Pattern pattern;
        private final EscapeType escape;

        public EscapeRule(String escapeType, String[] patterns) {
            this.escape = assertNotNull(EscapeType.getEscapeType(escapeType), "no escapeType specified");

            assertTrue(!isEmptyArray(patterns), "no pattern specified");

            StringBuilder buf = new StringBuilder();

            for (String pattern : patterns) {
                pattern = assertNotNull(trimToNull(pattern), "no pattern");

                if (buf.length() > 0) {
                    buf.append("|");
                }

                buf.append("(").append(pattern).append(")");
            }

            this.pattern = Pattern.compile(buf.toString(), Pattern.CASE_INSENSITIVE);
        }

        public Pattern matches(String reference) {
            if (pattern.matcher(reference).find()) {
                return pattern;
            }

            return null;
        }

        public EscapeType getEscapeType() {
            return escape;
        }

        @Override
        public String toString() {
            MapBuilder mb = new MapBuilder();

            mb.append("escape", escape);
            mb.append("pattern", pattern);

            return new ToStringBuilder().append("EscapeRule").append(mb).toString();
        }
    }
}
