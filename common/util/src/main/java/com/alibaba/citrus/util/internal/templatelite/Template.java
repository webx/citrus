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
package com.alibaba.citrus.util.internal.templatelite;

import static com.alibaba.citrus.util.ArrayUtil.*;
import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.BasicConstant.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.ObjectUtil.*;
import static com.alibaba.citrus.util.StringEscapeUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.cglib.reflect.FastClass;

import com.alibaba.citrus.util.internal.ToStringBuilder;
import com.alibaba.citrus.util.internal.ToStringBuilder.MapBuilder;

/**
 * 代表一个简易的模板。
 * 
 * @author Michael Zhou
 */
public final class Template {
    private final String name;
    final InputSource source;
    final Location location;
    Object[] nodes;

    /**
     * 从File中创建template。
     */
    public Template(File source) {
        this(new InputSource(source, null));
    }

    /**
     * 从File中创建template。
     */
    public Template(File source, String charset) {
        this(new InputSource(source, charset));
    }

    /**
     * 从URL中创建template。
     */
    public Template(URL source) {
        this(new InputSource(source, null));
    }

    /**
     * 从URL中创建template。
     */
    public Template(URL source, String charset) {
        this(new InputSource(source, charset));
    }

    /**
     * 从输入流中创建template。
     */
    public Template(InputStream stream, String charset, String systemId) {
        this(new InputSource(stream, charset, systemId));
    }

    /**
     * 从输入流中创建template。
     */
    public Template(Reader reader, String systemId) {
        this(new InputSource(reader, systemId));
    }

    /**
     * 内部构造函数：创建主模板。
     */
    private Template(InputSource source) {
        this.name = null;
        this.source = assertNotNull(source, "source");
        this.location = new Location(source.systemId, 0, 0);

        source.reloadIfNecessary(this);
    }

    /**
     * 内部构造函数：创建子模板。
     */
    private Template(String name, Object[] nodes, Location location) {
        this.name = trimToNull(name);
        this.source = null;
        this.location = assertNotNull(location, "location");
        this.nodes = defaultIfEmptyArray(nodes, EMPTY_OBJECT_ARRAY);
    }

    public String getName() {
        return name;
    }

    /**
     * 将模板渲染成文本。
     */
    public String toString(TextWriter<? super StringBuilder> writer) {
        writer.setOut(new StringBuilder());
        accept(writer);
        return writer.out().toString();
    }

    /**
     * 渲染模板。
     */
    public void accept(Object visitor) throws TemplateRuntimeException {
        if (source != null) {
            source.reloadIfNecessary(this);
        }

        for (Object node : nodes) {
            invokeVisitor(visitor, node);
        }
    }

    /**
     * 根据node类型，访问visitor相应的方法。
     */
    private void invokeVisitor(Object visitor, Object node) throws TemplateRuntimeException {
        assertNotNull(visitor, "visitor is null");

        Class<?> visitorClass = visitor.getClass();
        Text text = null;
        Placeholder placeholder = null;
        Template template = null;
        TemplateGroup group = null;
        NodeType nodeType = null;
        String nodeName = null;

        if (node instanceof Text) {
            text = (Text) node;
            nodeName = "Text";
            nodeType = NodeType.TEXT;
        } else if (node instanceof Placeholder) {
            placeholder = (Placeholder) node;
            nodeName = placeholder.name;
            nodeType = NodeType.PLACEHOLDER;
        } else if (node instanceof Template) {
            template = (Template) node;
            nodeName = template.getName();
            nodeType = NodeType.TEMPLATE;
        } else if (node instanceof TemplateGroup) {
            group = (TemplateGroup) node;
            nodeName = group.name;
            nodeType = NodeType.TEMPLATE_GROUP;
        } else {
            unreachableCode("Unexpected node type: " + node.getClass().getName());
        }

        String methodName = "visit" + trimToEmpty(capitalize(nodeName));

        try {
            Method method = null;
            Object[] params = null;

            switch (nodeType) {
                case TEXT:
                    // visitText(String)
                    // 和TemplateVisitor接口中的方法相同，故不再特别判断接口
                    method = findMethod(visitorClass, methodName, NodeType.TEXT);
                    params = new Object[] { text.text };
                    break;

                case PLACEHOLDER:
                    try {
                        // 1. visitXyz()
                        // 2. visitXyz(String[])
                        // 3. visitXyz(String)
                        // 4. visitXyz(String, String, ...)
                        method = findMethod(visitorClass, methodName, NodeType.PLACEHOLDER);

                        if (method.getParameterTypes().length == 0) {
                            params = EMPTY_OBJECT_ARRAY;
                        } else if (method.getParameterTypes()[0].equals(String[].class)) {
                            params = new Object[] { placeholder.params };
                        } else {
                            params = new Object[method.getParameterTypes().length];

                            for (int i = 0; i < params.length && i < placeholder.params.length; i++) {
                                params[i] = placeholder.params[i];
                            }
                        }
                    } catch (NoSuchMethodException e) {
                        if (visitor instanceof FallbackVisitor) {
                            ((FallbackVisitor) visitor).visitPlaceholder(placeholder.name, placeholder.params.clone());
                        } else {
                            throw e;
                        }
                    }

                    break;

                case TEMPLATE:
                    try {
                        // visitXyz(Template template)
                        method = findMethod(visitorClass, methodName, NodeType.TEMPLATE);
                        params = new Object[] { template };
                    } catch (NoSuchMethodException e) {
                        if (visitor instanceof FallbackVisitor) {
                            ((FallbackVisitor) visitor).visitTemplate(template.name, template);
                        } else {
                            throw e;
                        }
                    }

                    break;

                case TEMPLATE_GROUP:
                    try {
                        // 1. visitXyz(Template[] template)
                        // 2. visitXyz(Template template1, Template template2, ...)
                        method = findMethod(visitorClass, methodName, NodeType.TEMPLATE_GROUP);

                        if (method.getParameterTypes()[0].equals(Template[].class)) {
                            params = new Object[] { group.templates };
                        } else {
                            params = new Object[method.getParameterTypes().length];

                            for (int i = 0; i < params.length && i < group.templates.length; i++) {
                                params[i] = group.templates[i];
                            }
                        }
                    } catch (NoSuchMethodException e) {
                        if (visitor instanceof FallbackVisitor) {
                            ((FallbackVisitor) visitor).visitTemplateGroup(group.name, group.templates.clone());
                        } else {
                            throw e;
                        }
                    }

                    break;

                default:
                    unreachableCode();
            }

            if (method != null) {
                FastClass.create(visitorClass).getMethod(method).invoke(visitor, params);
            }
        } catch (InvocationTargetException e) {
            throw new TemplateRuntimeException("Error rendering " + shortDescription(node), e.getCause());
        } catch (Exception e) {
            throw new TemplateRuntimeException("Error rendering " + shortDescription(node), e);
        }
    }

    /**
     * 查找visit方法。
     */
    private Method findMethod(Class<?> visitorClass, String methodName, NodeType nodeType) throws NoSuchMethodException {
        Method[] methods = visitorClass.getMethods();
        Method method = null;

        for (Method candidateMethod : methods) {
            if (methodName.equals(candidateMethod.getName())) {
                Class<?>[] paramTypes = candidateMethod.getParameterTypes();
                int paramsCount = paramTypes.length;
                boolean paramTypeMatches = false;

                switch (nodeType) {
                    case TEXT:
                        // visitText(String)
                        if (paramsCount == 1 && paramTypes[0].equals(String.class)) {
                            paramTypeMatches = true;
                        }

                        break;

                    case PLACEHOLDER:
                        // 1. visitXyz()
                        // 2. visitXyz(String[])
                        // 3. visitXyz(String)
                        // 4. visitXyz(String, String, ...)
                        if (paramsCount == 0) {
                            paramTypeMatches = true; // 情况1
                        } else if (paramsCount == 1 && paramTypes[0].equals(String[].class)) {
                            paramTypeMatches = true; // 情况2
                        } else {
                            paramTypeMatches = true; // 情况3,4

                            for (int i = 0; i < paramsCount; i++) {
                                if (!paramTypes[i].equals(String.class)) {
                                    paramTypeMatches = false;
                                    break;
                                }
                            }
                        }

                        break;

                    case TEMPLATE:
                        // visitXyz(Template template)
                        if (paramsCount == 1 && Template.class.equals(paramTypes[0])) {
                            paramTypeMatches = true;
                        }

                        break;

                    case TEMPLATE_GROUP:
                        // 1. visitXyz(Template[] template)
                        // 2. visitXyz(Template template1, Template template2, ...)
                        if (paramsCount == 1 && Template[].class.equals(paramTypes[0])) {
                            paramTypeMatches = true;
                        } else if (paramsCount >= 1) {
                            paramTypeMatches = true;

                            for (int i = 0; i < paramsCount; i++) {
                                if (!paramTypes[i].equals(Template.class)) {
                                    paramTypeMatches = false;
                                    break;
                                }
                            }
                        }

                        break;

                    default:
                        unreachableCode();
                }

                if (paramTypeMatches) {
                    method = candidateMethod;
                    break;
                }
            }
        }

        if (method == null) {
            StringBuilder buf = new StringBuilder();

            buf.append(visitorClass.getSimpleName()).append(".").append(methodName);
            buf.append("(");

            switch (nodeType) {
                case TEXT:
                    buf.append("String");
                    break;

                case PLACEHOLDER:
                    buf.append("String...");
                    break;

                case TEMPLATE:
                case TEMPLATE_GROUP:
                    buf.append("Template...");
                    break;

                default:
                    unreachableCode();
            }

            buf.append(")");

            throw new NoSuchMethodException(buf.toString());
        }

        return method;
    }

    private String shortDescription(Object node) {
        if (node instanceof Template) {
            return ((Template) node).shortDescription();
        } else if (node instanceof TemplateGroup) {
            return ((TemplateGroup) node).shortDescription();
        } else {
            return node.toString();
        }
    }

    private String shortDescription() {
        return new ToStringBuilder().format("#%s with %d nodes at %s", name == null ? "(template)" : name,
                nodes.length, location).toString();
    }

    @Override
    public String toString() {
        return new ToStringBuilder()
                .format("#%s with %d nodes at %s", name == null ? "(template)" : name, nodes.length, location)
                .append(nodes).toString();
    }

    /**
     * 代表node类型。
     */
    private enum NodeType {
        TEXT,
        PLACEHOLDER,
        TEMPLATE,
        TEMPLATE_GROUP;
    }

    /**
     * 代表一个文本结点。
     */
    static class Text {
        final String text;

        public Text(String text) {
            this.text = assertNotNull(text, "text is null");
        }

        @Override
        public String toString() {
            String brief;

            if (text.length() < 10) {
                brief = text;
            } else {
                brief = text.substring(0, 10) + "...";
            }

            return String.format("Text with %d characters: %s", text.length(), escapeJava(brief));
        }
    }

    /**
     * 代表一个<code>${var}</code>结点。
     */
    static class Placeholder {
        final String name;
        final String paramsString;
        final String[] params;
        final Location location;

        public Placeholder(String name, String paramsString, Location location) {
            this.name = assertNotNull(trimToNull(name), "${missing name}");
            this.paramsString = trimToNull(paramsString);
            this.params = splitParams();
            this.location = assertNotNull(location, "location");
        }

        private String[] splitParams() {
            if (paramsString == null) {
                return EMPTY_STRING_ARRAY;
            } else {
                String[] params = paramsString.split(",");

                for (int i = 0; i < params.length; i++) {
                    params[i] = trimToNull(params[i]);
                }

                return params;
            }
        }

        @Override
        public String toString() {
            if (isEmptyArray(params)) {
                return new ToStringBuilder().format("${%s} at %s", name, location).toString();
            } else {
                return new ToStringBuilder().format("${%s:%s} at %s", name, paramsString, location).toString();
            }
        }
    }

    /**
     * 代表一组相关的templates。
     */
    static class TemplateGroup {
        final String name;
        final Template[] templates;
        final Location location;

        public TemplateGroup(Template[] templates) {
            assertTrue(!isEmptyArray(templates), "templates is empty");
            this.templates = templates;
            this.name = templates[0].name;
            this.location = templates[0].location;
        }

        private String shortDescription() {
            return new ToStringBuilder().format("#%s[] with %d templates at %s",
                    name == null ? "(template group)" : name, templates.length, location).toString();
        }

        @Override
        public String toString() {
            return new ToStringBuilder()
                    .format("#%s[] with %d templates at %s", name == null ? "(template group)" : name,
                            templates.length, location).append(templates).toString();
        }
    }

    static class Location {
        final String systemId;
        final int lineNumber;
        final int columnNumber;

        public Location(String systemId, int lineNumber, int columnNumber) {
            this.systemId = trimToNull(systemId);
            this.lineNumber = lineNumber;
            this.columnNumber = columnNumber;
        }

        @Override
        public String toString() {
            return toString(systemId, lineNumber, columnNumber);
        }

        private static String toString(String systemId, int lineNumber, int columnNumber) {
            StringBuilder buf = new StringBuilder();

            if (systemId == null) {
                buf.append("[unknown source]");
            } else {
                buf.append(systemId);
            }

            if (lineNumber > 0) {
                buf.append(": Line ").append(lineNumber);

                if (columnNumber > 0) {
                    buf.append(" Column ").append(columnNumber);
                }
            }

            return buf.toString();
        }
    }

    /**
     * 解析器
     */
    private static class Parser {
        private final static Pattern DIRECTIVE_PATTERN = Pattern.compile("(\\\\\\S)" // group 1
                + "|(\\s*##)" // group 2
                + "|\\$\\{\\s*([A-Za-z]\\w*)(\\s*:([^\\}]*))?\\s*\\}" // group 3, 4, 5
                + "|(\\s*)#([A-Za-z]\\w*)(\\s*(\\[\\s*\\]))?(\\s*(##.*)?)" // group 6, 7, 8, 9, 10, 11
        );

        private final static Set<String> KEYWORDS = createTreeSet("text", "placeholder", "template", "templategroup",
                "end");

        private final static int INDEX_OF_ESCAPE = 1;
        private final static int INDEX_OF_COMMENT = 2;
        private final static int INDEX_OF_PLACEHOLDER = 3;
        private final static int INDEX_OF_PLACEHOLDER_PARAMS = 5;
        private final static int INDEX_OF_SUBTEMPLATE_PREFIX = 6;
        private final static int INDEX_OF_SUBTEMPLATE = 7;
        private final static int INDEX_OF_SUBTEMPLATE_GROUP = 9;
        private final static int INDEX_OF_SUBTEMPLATE_SUFFIX = 10;

        private final BufferedReader reader;
        private final String systemId;
        private String currentLine;
        private int lineNumber = 1;
        private ParsingStack stack = new ParsingStack();
        private StringBuilder buf = new StringBuilder();

        public Parser(Reader reader, String systemId) {
            this.systemId = trimToNull(systemId);

            if (reader instanceof BufferedReader) {
                this.reader = (BufferedReader) reader;
            } else {
                this.reader = new BufferedReader(reader);
            }
        }

        public Object[] parse() {
            stack.push(new ParsingTemplate(null, systemId, 0, 0), false);

            for (; nextLine(); lineNumber++) {
                Matcher matcher = DIRECTIVE_PATTERN.matcher(currentLine);
                int index = 0;
                boolean appendNewLine = true;

                while (matcher.find()) {
                    buf.append(currentLine, index, matcher.start());
                    index = matcher.end();

                    // Escaped Char: \x
                    if (matcher.group(INDEX_OF_ESCAPE) != null) {
                        buf.append(matcher.group(INDEX_OF_ESCAPE).charAt(1));
                    }

                    // Comment: ## xxx
                    else if (matcher.group(INDEX_OF_COMMENT) != null) {
                        index = currentLine.length(); // 忽略当前行后面所有内容

                        if (matcher.start(INDEX_OF_COMMENT) == 0) {
                            appendNewLine = false; // 如果注释是从行首开始，则忽略掉整行
                        }

                        break; // ignore the rest of line
                    }

                    // Placeholder: ${var}
                    else if (matcher.group(INDEX_OF_PLACEHOLDER) != null) {
                        pushConstantNode();

                        String name = matcher.group(INDEX_OF_PLACEHOLDER);
                        String paramsString = matcher.group(INDEX_OF_PLACEHOLDER_PARAMS);
                        Location location = new Location(systemId, lineNumber, matcher.start() + 1);

                        checkName(name, location);

                        stack.topNodes().add(new Placeholder(name, paramsString, location));
                    }

                    // Sub-template: #template
                    else if (matcher.group(INDEX_OF_SUBTEMPLATE) != null) {
                        // 如果#前面只有空白，那么就忽略#前面的空白
                        if (matcher.start() > 0) {
                            buf.append(currentLine, matcher.start(INDEX_OF_SUBTEMPLATE_PREFIX),
                                    matcher.end(INDEX_OF_SUBTEMPLATE_PREFIX));
                        }

                        pushConstantNode();

                        String name = matcher.group(INDEX_OF_SUBTEMPLATE);
                        boolean group = matcher.group(INDEX_OF_SUBTEMPLATE_GROUP) != null;
                        boolean sameLine = false;

                        // #end of sub-template
                        if ("end".equals(name)) {
                            // #end后面不应有[]
                            if (group) {
                                throw new TemplateParseException("Unexpected [] after #end tag at "
                                        + Location.toString(systemId, lineNumber,
                                                matcher.start(INDEX_OF_SUBTEMPLATE_GROUP) + 1));
                            }

                            // #end没有对应的#template
                            else if (stack.size() <= 1) {
                                throw new TemplateParseException("Unmatched #end tag at "
                                        + Location.toString(systemId, lineNumber,
                                                matcher.end(INDEX_OF_SUBTEMPLATE_PREFIX) + 1));
                            }

                            // #end
                            else {
                                // #end与最后一个#template是否处于同一行？
                                if (lineNumber == stack.peek().lineNumber) {
                                    sameLine = true;
                                }

                                stack.popNode();
                            }
                        }

                        // start sub-template or template group
                        else {
                            int columnNumber = matcher.end(INDEX_OF_SUBTEMPLATE_PREFIX) + 1;

                            checkName(name, new Location(systemId, lineNumber, columnNumber));

                            stack.push(new ParsingTemplate(name, systemId, lineNumber, columnNumber), group);
                        }

                        // 如果#xxx到行末均为空白，则忽略所有空白和换行，除非#end和#xxx在同一行上。
                        if (matcher.end(INDEX_OF_SUBTEMPLATE_SUFFIX) < currentLine.length() || sameLine) {
                            buf.append(currentLine, matcher.start(INDEX_OF_SUBTEMPLATE_SUFFIX),
                                    matcher.end(INDEX_OF_SUBTEMPLATE_SUFFIX)); // 后面空白
                            appendNewLine = true;
                        } else {
                            appendNewLine = false;
                        }
                    } else {
                        unreachableCode();
                    }
                }

                buf.append(currentLine, index, currentLine.length());

                if (appendNewLine) {
                    buf.append("\n");
                }
            }

            pushConstantNode();

            if (stack.size() > 1) {
                StringBuilder buf = new StringBuilder("Unclosed tags: ");

                while (stack.size() > 1) {
                    buf.append("#").append(stack.popNode());

                    if (stack.size() > 1) {
                        buf.append(", ");
                    }
                }

                buf.append(" at ").append(Location.toString(systemId, lineNumber, 0));

                throw new TemplateParseException(buf.toString());
            }

            assertTrue(stack.size() == 1);

            return stack.topNodes().toArray(new Object[stack.topNodes().size()]);
        }

        private void pushConstantNode() {
            if (buf.length() > 0) {
                stack.topNodes().add(new Text(buf.toString()));
                buf.setLength(0);
            }
        }

        private boolean nextLine() {
            try {
                currentLine = reader.readLine();
            } catch (IOException e) {
                throw new TemplateParseException("Reading error at " + Location.toString(systemId, lineNumber, 0), e);
            }

            return currentLine != null;
        }

        private void checkName(String name, Object location) {
            if (KEYWORDS.contains(name.toLowerCase())) {
                throw new TemplateParseException("Reserved name: " + name + " at " + location);
            }
        }
    }

    private static class ParsingStack {
        private final LinkedList<Object> stack = createLinkedList();

        public List<Object> topNodes() {
            return peek().nodes;
        }

        public void push(ParsingTemplate pt, boolean group) {
            if (group) {
                // 如果当前template属于正在parse的group，则合并之。
                ParsingTemplateGroup parsingGroup = getLastParsingGroup();

                if (parsingGroup != null && isEquals(pt.name, parsingGroup.name)) {
                    // 结束group中的上一个template
                    Template lastTemplate = ((ParsingTemplate) stack.removeLast()).toTemplate();
                    parsingGroup.groupedTemplates.add(lastTemplate);
                } else {
                    parsingGroup = new ParsingTemplateGroup(pt.name);
                    stack.addLast(parsingGroup);
                }
            }

            stack.addLast(pt);
        }

        private ParsingTemplateGroup getLastParsingGroup() {
            if (stack.size() > 2) {
                Object node = stack.get(stack.size() - 2);

                if (node instanceof ParsingTemplateGroup) {
                    return (ParsingTemplateGroup) node;
                }
            }

            return null;
        }

        public ParsingTemplate peek() {
            return (ParsingTemplate) stack.getLast();
        }

        public int size() {
            return stack.size();
        }

        public String popNode() {
            Template tpl = ((ParsingTemplate) stack.removeLast()).toTemplate();
            Object node;

            if (!stack.isEmpty() && stack.getLast() instanceof ParsingTemplateGroup) {
                ParsingTemplateGroup group = (ParsingTemplateGroup) stack.removeLast();
                group.groupedTemplates.add(tpl);
                node = group.toTemplateGroup();
            } else {
                node = tpl;
            }

            topNodes().add(node);

            return tpl.getName();
        }

        @Override
        public String toString() {
            return new ToStringBuilder().append(stack).toString();
        }
    }

    private static class ParsingTemplateGroup {
        private final String name;
        private final List<Template> groupedTemplates = createLinkedList();

        public ParsingTemplateGroup(String name) {
            this.name = name;
        }

        public Object toTemplateGroup() {
            return new TemplateGroup(groupedTemplates.toArray(new Template[groupedTemplates.size()]));
        }

        @Override
        public String toString() {
            MapBuilder mb = new MapBuilder();

            mb.append("name", name);
            mb.append("templates", groupedTemplates);

            return new ToStringBuilder().append("TemplateGroup").append(mb).toString();
        }
    }

    private static class ParsingTemplate {
        private final String name;
        private final List<Object> nodes = createLinkedList();
        private final String systemId;
        private final int lineNumber;
        private final int columnNumber;

        public ParsingTemplate(String name, String systemId, int lineNumber, int columnNumber) {
            this.name = name;
            this.systemId = systemId;
            this.lineNumber = lineNumber;
            this.columnNumber = columnNumber;
        }

        public Template toTemplate() {
            return new Template(name, nodes.toArray(new Object[nodes.size()]), new Location(systemId, lineNumber,
                    columnNumber));
        }

        @Override
        public String toString() {
            MapBuilder mb = new MapBuilder();

            mb.append("name", name);
            mb.append("systemId", systemId);
            mb.append("lineNumber", lineNumber);
            mb.append("columnNumber", columnNumber);
            mb.append("nodes", nodes);

            return new ToStringBuilder().append("Template").append(mb).toString();
        }
    }

    /**
     * 保存文件来源，必要时重装模板。
     */
    static class InputSource {
        Object source;
        private long lastModified = 0;
        private final String charset;
        private final String systemId;

        public InputSource(File source, String charset) {
            this(source, charset, null);
        }

        public InputSource(URL source, String charset) {
            this(source, charset, null);
        }

        public InputSource(InputStream source, String charset, String systemId) {
            this((Object) source, charset, systemId);
        }

        public InputSource(Reader source, String systemId) {
            this(source, null, systemId);
        }

        private InputSource(Object source, String charset, String systemId) {
            assertNotNull(source, "source");

            if (source instanceof URL) {
                try {
                    this.source = new File(((URL) source).toURI()); // convert URL to File
                } catch (Exception e) {
                    this.source = source;
                }
            } else {
                this.source = source;
            }

            this.charset = defaultIfNull(trimToNull(charset), "UTF-8");

            if (source instanceof URL) {
                this.systemId = ((URL) source).toExternalForm();
            } else if (source instanceof File) {
                this.systemId = ((File) source).toURI().toString();
            } else {
                this.systemId = trimToNull(systemId);
            }
        }

        private void reloadIfNecessary(Template template) {
            assertNotNull(template, "template");
            assertTrue(template.source == this);

            boolean doLoad = false;

            if (template.nodes == null) {
                doLoad = true;
            } else if (source instanceof File && ((File) source).lastModified() != this.lastModified) {
                doLoad = true;
            }

            if (doLoad) {
                Reader reader;

                try {
                    if (source instanceof File) {
                        reader = new InputStreamReader(new FileInputStream((File) source), charset);
                    } else if (source instanceof URL) {
                        reader = new InputStreamReader(((URL) source).openStream(), charset);
                        source = null; // clear source
                    } else if (source instanceof InputStream) {
                        reader = new InputStreamReader((InputStream) source, charset);
                        source = null; // clear source
                    } else if (source instanceof Reader) {
                        reader = (Reader) source;
                        source = null; // clear source
                    } else {
                        throw new IllegalStateException("Source has already been read");
                    }
                } catch (IOException e) {
                    throw new TemplateParseException(e);
                }

                template.nodes = new Parser(reader, systemId).parse();

                if (source instanceof File) {
                    this.lastModified = ((File) source).lastModified();
                }
            }
        }
    }
}
