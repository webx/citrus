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

package com.alibaba.citrus.util.templatelite;

import static com.alibaba.citrus.util.ArrayUtil.*;
import static com.alibaba.citrus.util.Assert.ExceptionType.*;
import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.BasicConstant.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.StringEscapeUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;
import static java.lang.Math.*;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.Formatter;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.alibaba.citrus.util.FileUtil;
import com.alibaba.citrus.util.ToStringBuilder;
import com.alibaba.citrus.util.ToStringBuilder.MapBuilder;
import net.sf.cglib.reflect.FastClass;

/**
 * 一个简易的模板。
 * <p>
 * 格式如下：
 * </p>
 * <dl>
 * <dt>模板解析参数</dt>
 * <dd>
 * <p>
 * 参数必须定义在模板或子模板的开头，在所有的内容开始之前。
 * </p>
 * <p/>
 * <pre>
 * #@charset UTF-8
 * </pre>
 * <p/>
 * </dd>
 * <dt>注释</dt>
 * <dd>
 * <p>
 * 注释可以写在任何地方。如果一个注释从行首开始，或者注释之前没有任何可见字符，则整行将被忽略。
 * </p>
 * <p/>
 * <pre>
 * ## comment
 * </pre>
 * <p/>
 * </dd>
 * <dt>替换变量（placeholder）</dt>
 * <dd>
 * <p>
 * 最简单的写法：
 * </p>
 * <p/>
 * <pre>
 * ${placeholder}
 * </pre>
 * <p/>
 * <p>
 * 包含一个或多个参数：
 * </p>
 * <p/>
 * <pre>
 * ${placeholder: param, param}
 * </pre>
 * <p/>
 * <p>
 * 可引用一个或多个子模板作为参数。所引用的子模板将从当前子模板或者上级子模板中查找。
 * </p>
 * <p/>
 * <pre>
 * ${placeholder: #subtpl, #subtpl}
 * </pre>
 * <p/>
 * <p>
 * 也可引用多级子模板作为参数。
 * </p>
 * <p/>
 * <pre>
 * ${placeholder: #tpl1.subtpl2.suptpl3}
 * </pre>
 * <p/>
 * <p>
 * 使用.*可达到引用一组模板的作用。
 * </p>
 * <p/>
 * <pre>
 * ${placeholder: #tpl1.*}
 * </pre>
 * <p/>
 * </dd>
 * <dt>包含子模板</dt>
 * <dd>
 * <p>
 * 直接包含一个子模板，不调用visitor。
 * </p>
 * <p/>
 * <pre>
 * $#{subtpl}
 * </pre>
 * <p/>
 * <p>
 * 也可包含多级子模板。
 * </p>
 * <p/>
 * <pre>
 * $#{tpl.subtpl1.subtpl2}
 * </pre>
 * <p/>
 * </dd>
 * <dt>定义子模板</dt>
 * <dd>
 * <p>
 * 子模板必须位于模板或其它子模板的末尾。从最后一行内容到子模板之间的空行将被忽略。子模板可以包含其它子模板。
 * </p>
 * <p/>
 * <pre>
 * #subtpl
 * #@trimming on
 * #@whitespace collapse
 * content
 * #end
 * </pre>
 * <p/>
 * </dd>
 * <dt>导入子模板</dt>
 * <dd>
 * <p>
 * 导入外部文件，作为子模板。这种方法所产生的子模板，和直接定义子模板的效果完全相同。但将子模板定义在外部文件中，有利于整理并缩短模板的长度。
 * </p>
 * <p/>
 * <pre>
 * #subtpl(relative_file_name)
 * </pre>
 * <p/>
 * <p>
 * 或者：
 * </p>
 * <p/>
 * <pre>
 * #subtpl("relative_file_name")
 * </pre>
 * <p/>
 * </dd>
 * </dl>
 *
 * @author Michael Zhou
 */
public final class Template {
    private final static int                   MAX_REDIRECT_DEPTH  = 10;
    private final static Node[]                EMPTY_NODES         = new Node[0];
    private final static Map<String, Template> predefinedTemplates = createHashMap();
    private final String      name;
    final         InputSource source;
    final         Location    location;
    final         Template    ref;
    Node[]                nodes;
    Map<String, Template> subtemplates;
    Map<String, String>   params;

    static {
        // 预定义模板，可供任何模板中直接使用，例如：$#{SPACE}可强制插入空格。
        predefineTemplate("SPACE", " ");
        predefineTemplate("BR", "\n");
    }

    /** 从File中创建template。 */
    public Template(File source) {
        this(new InputSource(source, null), null);
    }

    /** 从URL中创建template。 */
    public Template(URL source) {
        this(new InputSource(source, null), null);
    }

    /** 从输入流中创建template。 */
    public Template(InputStream stream, String systemId) {
        this(new InputSource(stream, systemId), null);
    }

    /** 从输入流中创建template。 */
    public Template(Reader reader, String systemId) {
        this(new InputSource(reader, systemId), null);
    }

    /** 内部构造函数：创建主模板。 */
    private Template(InputSource source, String name) {
        this.name = trimToNull(name);
        this.source = assertNotNull(source, "source");
        this.location = new Location(source.systemId, 0, 0);
        this.ref = null;

        source.reloadIfNecessary(this);
    }

    /** 内部构造函数：创建子模板。 */
    private Template(String name, Node[] nodes, Map<String, String> params, Map<String, Template> subtemplates,
                     Location location) {
        this.name = trimToNull(name);
        this.source = null;
        this.location = assertNotNull(location, "location");
        this.ref = null;

        update(nodes, params, subtemplates);
    }

    /** 内部构造函数：创建template ref。 */
    private Template(Template ref) {
        this.ref = assertNotNull(ref, "template ref");
        this.name = null;
        this.source = null;
        this.location = null;
        this.nodes = null;
        this.subtemplates = null;
        this.params = null;
    }

    private void assertNotRef() {
        assertNull(ref, UNSUPPORTED_OPERATION, "template ref");
    }

    private void update(Node[] nodes, Map<String, String> params, Map<String, Template> subtemplates) {
        assertNotRef();

        this.nodes = defaultIfEmptyArray(nodes, EMPTY_NODES);
        this.params = createArrayHashMap(assertNotNull(params, "params").size());
        this.subtemplates = createArrayHashMap(assertNotNull(subtemplates, "subtemplates").size());

        this.params.putAll(params);
        this.subtemplates.putAll(subtemplates);
    }

    public String getName() {
        if (ref == null) {
            return name;
        } else {
            return ref.name;
        }
    }

    public String getParameter(String name) {
        if (ref == null) {
            return params.get(name);
        } else {
            return ref.params.get(name);
        }
    }

    public Template getSubTemplate(String name) {
        if (ref == null) {
            return subtemplates.get(name);
        } else {
            return ref.subtemplates.get(name);
        }
    }

    /** 将模板渲染成文本。 */
    public String renderToString(TextWriter<? super StringBuilder> writer) {
        writer.setOut(new StringBuilder());
        accept(writer);
        return writer.out().toString();
    }

    /** 渲染模板。 */
    public void accept(Object visitor) throws TemplateRuntimeException {
        if (ref == null) {
            if (source != null) {
                source.reloadIfNecessary(this);
            }

            for (Node node : nodes) {
                invokeVisitor(visitor, node);
            }
        } else {
            // 调用ref，也就是${placeholder: #template}或者$#{template}中，被注入的template对象。
            // 首先调用：visitTemplateName(Template)方法，如果方法不存在，则直接调用被引用的template。
            invokeVisitor(visitor, ref);
        }
    }

    /** 调用visitTemplateName(Template)方法，如果方法不存在，则直接调用被引用的template。 */
    private void invokeVisitor(Object visitor, Template templateRef) throws TemplateRuntimeException {
        invokeVisitor(visitor, templateRef, 0);
    }

    /** 根据node类型，访问visitor相应的方法。 */
    private void invokeVisitor(Object visitor, Node node) throws TemplateRuntimeException {
        assertNotRef();
        invokeVisitor(visitor, node, 0);
    }

    /** 根据node类型，访问visitor相应的方法。 */
    private void invokeVisitor(Object visitor, Object node, int redirectDepth) throws TemplateRuntimeException {
        // 对$#{includeTemplate}直接调用template
        if (node instanceof IncludeTemplate) {
            assertNotNull(((IncludeTemplate) node).includedTemplate).accept(visitor);
            return;
        }

        Class<?> visitorClass = assertNotNull(visitor, "visitor is null").getClass();

        try {
            Method method = null;
            Object[] params = null;

            // 对普通文本调用visitText(String)
            if (node instanceof Text) {
                Text text = (Text) node;
                method = findVisitTextMethod(visitorClass, "visitText");
                params = new Object[] { text.text };
            }

            // 对ref template调用visitTemplateName(Template)，如果不存在该方法，则直接调用template
            else if (node instanceof Template) {
                Template ref = (Template) node;
                method = findVisitTemplateMethod(visitorClass, "visit" + trimToEmpty(capitalize(ref.getName())));

                if (method == null) {
                    ref.accept(visitor); // 方法不存在，直接调用ref template
                    return;
                }

                params = new Object[] { ref };
            }

            // 对${placeholder}调用：
            //
            // 1. visitXyz()                                        // 无参数
            // 2. visitXyz(String[])                                // 数组参数
            //    visitXyz(Template[])
            //    visitXyz(Object[])
            // 3. visitXyz(Template, String, String, Template, ...) // 独立参数
            else if (node instanceof Placeholder) {
                Placeholder placeholder = (Placeholder) node;
                int placeholderParamCount = placeholder.params.length;
                String methodName = "visit" + trimToEmpty(capitalize(placeholder.name));

                try {
                    method = findVisitPlaceholderMethod(visitorClass, methodName, placeholder.params);
                    int methodParamCount = method.getParameterTypes().length;

                    if (method.getParameterTypes().length == 0) {
                        params = EMPTY_OBJECT_ARRAY;
                    } else if (method.getParameterTypes()[0].isArray()) {
                        Object[] array;

                        if (method.getParameterTypes()[0].equals(String[].class)) {
                            array = new String[placeholderParamCount];
                        } else if (method.getParameterTypes()[0].equals(Template[].class)) {
                            array = new Template[placeholderParamCount];
                        } else {
                            array = new Object[placeholderParamCount];
                        }

                        params = new Object[] { toPlaceholderParameterValues(placeholder, array) };
                    } else {
                        params = toPlaceholderParameterValues(placeholder, new Object[methodParamCount]);
                    }
                } catch (NoSuchMethodException e) {
                    boolean processed = false;

                    if (visitor instanceof FallbackVisitor) {
                        processed = ((FallbackVisitor) visitor).visitPlaceholder(placeholder.name,
                                                                                 toPlaceholderParameterValues(placeholder, new Object[placeholderParamCount]));
                    }

                    if (!processed) {
                        throw e;
                    }
                }
            } else {
                unreachableCode("Unexpected node type: " + node.getClass().getName());
            }

            if (method != null) {
                Object newVisitor = null;

                try {
                    newVisitor = FastClass.create(visitorClass).getMethod(method).invoke(visitor, params);
                } catch (InvocationTargetException e) {
                    if (visitor instanceof VisitorInvocationErrorHandler) {
                        ((VisitorInvocationErrorHandler) visitor).handleInvocationError(node.toString(), e.getCause());
                    } else {
                        throw new TemplateRuntimeException("Error rendering " + node, e.getCause());
                    }
                }

                // 如果当前visitor返回了一个新的visitor对象，则重定向到新的visitor。
                if (newVisitor != null && visitor != newVisitor) {
                    if (redirectDepth >= MAX_REDIRECT_DEPTH) {
                        throw new TemplateRuntimeException("Redirection out of control (depth>" + MAX_REDIRECT_DEPTH
                                                           + ") in " + method);
                    }

                    invokeVisitor(newVisitor, node, redirectDepth + 1);
                }
            }
        } catch (TemplateRuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new TemplateRuntimeException("Error rendering " + node, e);
        }
    }

    /** 查找visitText方法。 */
    private Method findVisitTextMethod(Class<?> visitorClass, String methodName) throws NoSuchMethodException {
        Method method = null;

        for (Method candidateMethod : visitorClass.getMethods()) {
            if (methodName.equals(candidateMethod.getName())) {
                Class<?>[] paramTypes = candidateMethod.getParameterTypes();
                int paramsCount = paramTypes.length;

                // visitText(String)
                if (paramsCount == 1 && paramTypes[0].equals(String.class)) {
                    method = candidateMethod;
                    break;
                }
            }
        }

        if (method == null) {
            throw new NoSuchMethodException(visitorClass.getSimpleName() + "." + methodName + "(String)");
        }

        return method;
    }

    /** 查找visitTemplate方法。 */
    private Method findVisitTemplateMethod(Class<?> visitorClass, String methodName) throws NoSuchMethodException {
        Method method = null;

        for (Method candidateMethod : visitorClass.getMethods()) {
            if (methodName.equals(candidateMethod.getName())) {
                Class<?>[] paramTypes = candidateMethod.getParameterTypes();
                int paramsCount = paramTypes.length;

                // visitTemplateName(Template)
                if (paramsCount == 1 && paramTypes[0].equals(Template.class)) {
                    method = candidateMethod;
                    break;
                }
            }
        }

        return method;
    }

    /** 查找visitPlaceholder方法。 */
    private Method findVisitPlaceholderMethod(Class<?> visitorClass, String methodName,
                                              PlaceholderParameter[] placeholderParams) throws NoSuchMethodException {
        Method[] methods = visitorClass.getMethods();

        // 统计placeholder参数的类型及数量。
        // Placeholder支持两种类型的参数：String和Template。
        Class<?>[] placeholderParamTypes = new Class[placeholderParams.length];
        int placeholderParamCount = placeholderParamTypes.length;
        int placeholderParamStringCount = 0;
        int placeholderParamTemplateCount = 0;

        for (int i = 0; i < placeholderParamCount; i++) {
            PlaceholderParameter param = placeholderParams[i];

            if (param.isTemplateReference()) {
                placeholderParamTemplateCount++;
                placeholderParamTypes[i] = Template.class;
            } else {
                placeholderParamStringCount++;
                placeholderParamTypes[i] = String.class;
            }
        }

        Method method = null;
        int minIndexWeight = Integer.MAX_VALUE;

        for (Method candidateMethod : methods) {
            if (methodName.equals(candidateMethod.getName())) {
                Class<?>[] methodParamTypes = candidateMethod.getParameterTypes();
                int methodParamCount = methodParamTypes.length;
                boolean paramTypeMatches = false;
                int indexWeight = Integer.MAX_VALUE;

                switch (methodParamCount) {
                    case 0:
                        // visitXyz()
                        paramTypeMatches = true;
                        indexWeight = 100000 * abs(methodParamCount - placeholderParamCount);
                        break;

                    case 1:
                        // 如果placeholder参数全为String，可调用visitXyz(String[])
                        if (placeholderParamStringCount == placeholderParamCount
                            && methodParamTypes[0].equals(String[].class)) {
                            paramTypeMatches = true;
                            indexWeight = 50;
                        }

                        // 如果placeholder参数全为Template，可调用visitXyz(Template[])
                        if (placeholderParamTemplateCount == placeholderParamCount
                            && methodParamTypes[0].equals(Template[].class)) {
                            paramTypeMatches = true;
                            indexWeight = 50;
                        }

                        // 任何placeholder参数，都可调用visitXyz(Object[])
                        if (methodParamTypes[0].equals(Object[].class)) {
                            paramTypeMatches = true;
                            indexWeight = 90;
                        }

                        // no break;

                    default:
                        // 如果placeholder的参数比方法中的参数多，多余的placeholder参数将被忽略。
                        // 如果placeholder的参数比方法中的参数少，不足的参数值将为null。
                        // 访问visitXyz(Template, String, String, Template, ...)
                        if (!paramTypeMatches) {
                            paramTypeMatches = true;

                            for (int i = 0; i < methodParamCount; i++) {
                                if (i < placeholderParamCount) {
                                    if (!methodParamTypes[i].equals(placeholderParamTypes[i])) {
                                        paramTypeMatches = false;
                                        break;
                                    }
                                } else {
                                    if (!methodParamTypes[i].equals(String.class)
                                        && !methodParamTypes[i].equals(Template.class)) {
                                        paramTypeMatches = false;
                                        break;
                                    }
                                }
                            }

                            if (paramTypeMatches) {
                                indexWeight = 100 * abs(methodParamCount - placeholderParamCount);
                            }
                        }

                        break;
                }

                if (paramTypeMatches && indexWeight < minIndexWeight) {
                    minIndexWeight = indexWeight;
                    method = candidateMethod;

                    if (indexWeight == 0) {
                        break;
                    }
                }
            }
        }

        if (method == null) {
            StringBuilder buf = new StringBuilder();
            Formatter format = new Formatter(buf);
            int count = 1;

            buf.append("One of the following method:\n");

            // 方法1. 详细匹配
            format.format("  %d. %s.%s(", count++, visitorClass.getSimpleName(), methodName);

            for (int i = 0; i < placeholderParamCount; i++) {
                if (i > 0) {
                    buf.append(", ");
                }

                buf.append(placeholderParamTypes[i].getSimpleName());
            }

            buf.append(")\n");

            // 方法2. 数组匹配
            format.format("  %d. %s.%s(", count++, visitorClass.getSimpleName(), methodName);

            if (placeholderParamStringCount == placeholderParamCount) {
                buf.append("String");
            } else if (placeholderParamTemplateCount == placeholderParamCount) {
                buf.append("Template");
            } else {
                buf.append("Object");
            }

            buf.append("[])\n");

            // 方法3. 无参数
            if (placeholderParamCount > 0) {
                format.format("  %d. %s.%s()", count++, visitorClass.getSimpleName(), methodName);
            }

            if (buf.charAt(buf.length() - 1) == '\n') {
                buf.setLength(buf.length() - 1);
            }

            throw new NoSuchMethodException(buf.toString());
        }

        return method;
    }

    private Object[] toPlaceholderParameterValues(Placeholder placeholder, Object[] params) {
        for (int i = 0; i < params.length && i < placeholder.params.length; i++) {
            PlaceholderParameter param = placeholder.params[i];

            if (param.isTemplateReference()) {
                params[i] = assertNotNull(param.getTemplateReference());
            } else {
                params[i] = param.getValue();
            }
        }

        return params;
    }

    @Override
    public String toString() {
        if (ref == null) {
            MapBuilder mb = new MapBuilder();

            mb.append("params", params.entrySet());
            mb.append("nodes", nodes);
            mb.append("sub-templates", subtemplates.values());

            return new ToStringBuilder()
                    .format("#%s with %d nodes at %s", name == null ? "(template)" : name, nodes.length, location)
                    .append(mb).toString();
        } else {
            return "ref to " + ref;
        }
    }

    private static void predefineTemplate(String name, String text) {
        Template template = new Template(name, new Node[] { new Text(text, new Location(null, 1, 1)) },
                                         Collections.<String, String>emptyMap(), Collections.<String, Template>emptyMap(), new Location(null,
                                                                                                                                        1, 1));

        predefinedTemplates.put(name, template);
    }

    /** 代表一个结点。 */
    static abstract class Node {
        final Location location;

        public Node(Location location) {
            this.location = assertNotNull(location, "location");
        }

        public abstract String desc();
    }

    /** 代表一个文本结点。 */
    static class Text extends Node {
        final String text;

        public Text(String text, Location location) {
            super(location);
            this.text = assertNotNull(text, "text is null");
        }

        @Override
        public String desc() {
            return "text";
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

    /** 代表一个<code>${var}</code>结点。 */
    static class Placeholder extends Node {
        private final static PlaceholderParameter[] EMPTY_PARAMS = new PlaceholderParameter[0];
        final String name;
        final String paramsString;
        PlaceholderParameter[] params;

        public Placeholder(String name, String paramsString, Location location) {
            super(location);
            this.name = assertNotNull(trimToNull(name), "${missing name}");
            this.paramsString = trimToNull(paramsString);
            this.params = splitParams();
        }

        private PlaceholderParameter[] splitParams() {
            if (paramsString == null) {
                return EMPTY_PARAMS;
            } else {
                String[] paramValues = paramsString.split(",");
                PlaceholderParameter[] params = new PlaceholderParameter[paramValues.length];

                for (int i = 0; i < params.length; i++) {
                    params[i] = new PlaceholderParameter(trimToNull(paramValues[i]));
                }

                return params;
            }
        }

        @Override
        public String desc() {
            if (isEmptyArray(params)) {
                return "${" + name + "}";
            } else {
                return "${" + name + ":" + paramsString + "}";
            }
        }

        @Override
        public String toString() {
            return new ToStringBuilder().format("%s at %s", desc(), location).toString();
        }
    }

    static class PlaceholderParameter {
        private final String   value;
        private       Template templateReference;

        public PlaceholderParameter(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public boolean isTemplateReference() {
            return value != null && value.length() > 1 && value.startsWith("#");
        }

        public String getTemplateName() {
            return isTemplateReference() ? value.substring(1) : null;
        }

        public Template getTemplateReference() {
            return templateReference;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    /** 代表一个<code>$#{template}</code>结点。 */
    static class IncludeTemplate extends Node {
        final String templateName;
        Template includedTemplate;

        public IncludeTemplate(String templateName, Location location) {
            super(location);
            this.templateName = assertNotNull(trimToNull(templateName), "$#{missing template name}");
        }

        @Override
        public String desc() {
            return "$#{" + templateName + "}";
        }

        @Override
        public String toString() {
            return new ToStringBuilder().format("%s at %s", desc(), location).toString();
        }
    }

    static class Location {
        final String systemId;
        final int    lineNumber;
        final int    columnNumber;

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

    /** 解析器 */
    private static class Parser {
        private final static Pattern DIRECTIVE_PATTERN = Pattern.compile("\\\\(\\$|\\$#|#|#@|\\\\)" // group 1
                                                                         + "|(\\s*##)" // group 2
                                                                         + "|\\$\\{\\s*([A-Za-z]\\w*)(\\s*:([^\\}]*))?\\s*\\}" // group 3, 4, 5
                                                                         + "|\\$#\\{\\s*([A-Za-z][\\.\\w]*)\\s*\\}" // group 6
                                                                         + "|(\\s*)#([A-Za-z]\\w*)(\\s*\\(\\s*(.*)\\s*\\))?(\\s*(##.*)?)" // group 7, 8, 9, 10, 11, 12
                                                                         + "|(\\s*)#@\\s*([A-Za-z]\\w*)(\\s+(.*?))?(##.*)?$" // group 11, 12, 13, 14, 15
        );

        private final static Set<String> KEYWORDS = createTreeSet("text", "placeholder", "template", "end");

        private final static int INDEX_OF_ESCAPE             = 1;
        private final static int INDEX_OF_COMMENT            = 2;
        private final static int INDEX_OF_PLACEHOLDER        = 3;
        private final static int INDEX_OF_PLACEHOLDER_PARAMS = 5;
        private final static int INDEX_OF_INCLUDE_TEMPLATE   = 6;
        private final static int INDEX_OF_SUBTEMPLATE_PREFIX = 7;
        private final static int INDEX_OF_SUBTEMPLATE        = 8;
        private final static int INDEX_OF_IMPORT_FILE        = 9;
        private final static int INDEX_OF_IMPORT_FILE_NAME   = 10;
        private final static int INDEX_OF_SUBTEMPLATE_SUFFIX = 11;
        private final static int INDEX_OF_PARAM_PREFIX       = 13;
        private final static int INDEX_OF_PARAM              = 14;
        private final static int INDEX_OF_PARAM_VALUE        = 16;

        private final InputSource    source;
        private final BufferedReader reader;
        private final String         systemId;
        private final ParsingTemplateStack stack = new ParsingTemplateStack();
        private final TextBuffer buf;
        private       String     currentLine;
        private int lineNumber = 1;

        public Parser(Reader reader, String systemId, InputSource source) {
            this.source = assertNotNull(source, "input source");
            this.systemId = trimToNull(systemId);

            if (reader instanceof BufferedReader) {
                this.reader = (BufferedReader) reader;
            } else {
                this.reader = new BufferedReader(reader);
            }

            this.buf = new TextBuffer(systemId);
        }

        public ParsingTemplate parse() {
            stack.push(new ParsingTemplate(null, systemId, 0, 0, null));

            for (; nextLine(); lineNumber++) {
                Matcher matcher = DIRECTIVE_PATTERN.matcher(currentLine);
                int index = 0;
                boolean appendNewLine = true;

                while (matcher.find()) {
                    buf.append(currentLine, index, matcher.start(), lineNumber, index + 1);
                    index = matcher.end();

                    // Escaped Char: \x
                    if (matcher.group(INDEX_OF_ESCAPE) != null) {
                        buf.append(matcher.group(INDEX_OF_ESCAPE), lineNumber, matcher.start(INDEX_OF_ESCAPE));
                    }

                    // Comment: ## xxx
                    else if (matcher.group(INDEX_OF_COMMENT) != null) {
                        index = currentLine.length(); // 忽略当前行后面所有内容

                        if (matcher.start(INDEX_OF_COMMENT) == 0) {
                            appendNewLine = false; // 如果注释是从行首开始，则忽略掉整行
                        }

                        break; // ignore the rest of line
                    }

                    // Param: #@param value
                    else if (matcher.group(INDEX_OF_PARAM) != null) {
                        pushTextNode();

                        String name = matcher.group(INDEX_OF_PARAM);

                        // #@前面只允许有空白（只允许从行首开始定义），否则报错。
                        if (matcher.start() > 0) {
                            throw new TemplateParseException("#@" + name
                                                             + " should start at new line, which is now at "
                                                             + Location.toString(systemId, lineNumber, matcher.end(INDEX_OF_PARAM_PREFIX) + 1));
                        }

                        String value = trimToEmpty(matcher.group(INDEX_OF_PARAM_VALUE));

                        stack.peek().addParam(name, value, lineNumber, matcher.end(INDEX_OF_PARAM_PREFIX) + 1);

                        // 忽略本行
                        appendNewLine = false;
                    }

                    // Placeholder: ${var}
                    else if (matcher.group(INDEX_OF_PLACEHOLDER) != null) {
                        pushTextNode();

                        String name = matcher.group(INDEX_OF_PLACEHOLDER);
                        String paramsString = matcher.group(INDEX_OF_PLACEHOLDER_PARAMS);
                        Location location = new Location(systemId, lineNumber, matcher.start() + 1);

                        checkName(name, location);

                        stack.peek().addNode(new Placeholder(name, paramsString, location));
                    }

                    // Include template: $#{template}
                    else if (matcher.group(INDEX_OF_INCLUDE_TEMPLATE) != null) {
                        pushTextNode();

                        String templateName = matcher.group(INDEX_OF_INCLUDE_TEMPLATE);
                        Location location = new Location(systemId, lineNumber, matcher.start() + 1);

                        stack.peek().addNode(new IncludeTemplate(templateName, location));
                    }

                    // Sub-template: #template
                    else if (matcher.group(INDEX_OF_SUBTEMPLATE) != null) {
                        String name = matcher.group(INDEX_OF_SUBTEMPLATE);

                        // #前面只允许有空白（只允许从行首开始定义），否则报错。
                        if (matcher.start() > 0) {
                            throw new TemplateParseException("#"
                                                             + name
                                                             + " should start at new line, which is now at "
                                                             + Location.toString(systemId, lineNumber,
                                                                                 matcher.end(INDEX_OF_SUBTEMPLATE_PREFIX) + 1));
                        }

                        // #xxx必须独占一行
                        if (matcher.end(INDEX_OF_SUBTEMPLATE_SUFFIX) < currentLine.length()) {
                            throw new TemplateParseException("Invalid content followed after #"
                                                             + name
                                                             + " at "
                                                             + Location.toString(systemId, lineNumber,
                                                                                 matcher.end(INDEX_OF_SUBTEMPLATE_SUFFIX) + 1));
                        }

                        pushTextNode();

                        // #end of sub-template
                        if ("end".equals(name)) {
                            // #end后跟()
                            if (matcher.group(INDEX_OF_IMPORT_FILE) != null) {
                                throw new TemplateParseException("Invalid character '(' after #end tag at "
                                                                 + Location.toString(systemId, lineNumber, matcher.start(INDEX_OF_IMPORT_FILE)
                                                                                                           + matcher.group(INDEX_OF_IMPORT_FILE).indexOf("(") + 1));
                            }

                            // #end没有对应的#template
                            else if (stack.size() <= 1) {
                                throw new TemplateParseException("Unmatched #end tag at "
                                                                 + Location.toString(systemId, lineNumber,
                                                                                     matcher.end(INDEX_OF_SUBTEMPLATE_PREFIX) + 1));
                            }

                            // #end
                            else {
                                Template subTemplate = stack.pop();
                                stack.peek().addSubTemplate(subTemplate);
                            }
                        }

                        // start sub-template
                        else {
                            int columnNumber = matcher.end(INDEX_OF_SUBTEMPLATE_PREFIX) + 1;

                            checkName(name, new Location(systemId, lineNumber, columnNumber));

                            // 从另一个文件中读取子模板
                            if (matcher.group(INDEX_OF_IMPORT_FILE) != null) {
                                String importedFileName = trimToNull(trim(
                                        trimToEmpty(matcher.group(INDEX_OF_IMPORT_FILE_NAME)), "\""));

                                int importedFileColumnNumber = matcher.start(INDEX_OF_IMPORT_FILE_NAME) + 1;

                                if (importedFileName == null) {
                                    throw new TemplateParseException("Import file name is not specified at "
                                                                     + Location.toString(systemId, lineNumber, importedFileColumnNumber));
                                }

                                InputSource importedSource = null;
                                Exception e = null;

                                try {
                                    importedSource = source.getRelative(importedFileName);
                                } catch (Exception ee) {
                                    e = ee;
                                }

                                if (importedSource == null || e != null) {
                                    throw new TemplateParseException("Could not import template file \""
                                                                     + importedFileName + "\" at "
                                                                     + Location.toString(systemId, lineNumber, importedFileColumnNumber), e);
                                }

                                Template importedTemplate;

                                try {
                                    importedTemplate = new Template(importedSource, name);
                                } catch (Exception ee) {
                                    throw new TemplateParseException("Could not import template file \""
                                                                     + importedFileName + "\" at "
                                                                     + Location.toString(systemId, lineNumber, importedFileColumnNumber), ee);
                                }

                                stack.peek().addSubTemplate(importedTemplate);
                            }

                            // 开始一个新模板
                            else {
                                stack.push(new ParsingTemplate(name, systemId, lineNumber, columnNumber,
                                                               stack.peek().params));
                            }
                        }

                        // 忽略本行
                        appendNewLine = false;
                    } else {
                        unreachableCode();
                    }
                }

                buf.append(currentLine, index, currentLine.length(), lineNumber, index + 1);

                if (appendNewLine) {
                    buf.newLine();
                }
            }

            pushTextNode();

            if (stack.size() > 1) {
                StringBuilder buf = new StringBuilder("Unclosed tags: ");

                while (stack.size() > 1) {
                    buf.append("#").append(stack.pop().getName());

                    if (stack.size() > 1) {
                        buf.append(", ");
                    }
                }

                buf.append(" at ").append(Location.toString(systemId, lineNumber, 0));

                throw new TemplateParseException(buf.toString());
            }

            assertTrue(stack.size() == 1);

            ParsingTemplate parsingTemplate = stack.peek();

            postProcessParsingTemplate(parsingTemplate);

            return parsingTemplate;
        }

        private void pushTextNode() {
            Text node = buf.toText();
            buf.clear();

            if (node != null) {
                stack.peek().addNode(node);
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

        /**
         * 后期处理。
         * <ol>
         * <li>检查include template。</li>
         * <li>检查placeholder中的template reference。</li>
         * <li>根据trimming和whitespace参数的值，处理模板中的空白和换行。</li>
         * </ol>
         */
        private void postProcessParsingTemplate(ParsingTemplate parsingTemplate) {
            LinkedList<Map<String, Template>> templateStack = createLinkedList();

            templateStack.addFirst(parsingTemplate.subtemplates);

            for (Node node : parsingTemplate.nodes) {
                postProcessNode(node, templateStack);
            }

            chomp(parsingTemplate.nodes);
            trimIfNeccessary(parsingTemplate.nodes, parsingTemplate.params);
            collapseWhitespacesIfNeccessary(parsingTemplate.nodes, parsingTemplate.params);

            for (Template subTemplate : parsingTemplate.subtemplates.values()) {
                postProcessTemplate(subTemplate, templateStack);
            }

            templateStack.removeFirst();
        }

        private void postProcessTemplate(Template template, LinkedList<Map<String, Template>> templateStack) {
            templateStack.addFirst(template.subtemplates);

            // 处理nodes
            for (Node node : template.nodes) {
                postProcessNode(node, templateStack);
            }

            LinkedList<Node> nodes = createLinkedList(template.nodes);

            chomp(nodes);
            trimIfNeccessary(nodes, template.params);
            collapseWhitespacesIfNeccessary(nodes, template.params);

            template.nodes = nodes.toArray(new Node[nodes.size()]);

            // 递归处理子模板
            for (Template subTemplate : template.subtemplates.values()) {
                postProcessTemplate(subTemplate, templateStack);
            }

            templateStack.removeFirst();
        }

        private void postProcessNode(Node node, LinkedList<Map<String, Template>> templateStack) {
            // $#{includeTemplate}
            if (node instanceof IncludeTemplate) {
                ((IncludeTemplate) node).includedTemplate = new Template(findTemplate(
                        ((IncludeTemplate) node).templateName, templateStack, node.location, "Included")); // create template ref
            }

            // ${placeholder: #templateRef}
            if (node instanceof Placeholder && !isEmptyArray(((Placeholder) node).params)) {
                List<PlaceholderParameter> expandedParameters = createLinkedList();

                for (PlaceholderParameter param : ((Placeholder) node).params) {
                    if (param.isTemplateReference()) {
                        String templateName = param.getTemplateName();
                        String parentName;

                        if (templateName.equals("*") || templateName.endsWith(".*")) {
                            Map<String, Template> subtemplates;

                            if (templateName.equals("*")) {
                                subtemplates = templateStack.getFirst();
                                parentName = EMPTY_STRING;
                            } else {
                                String parentTemplateName = templateName.substring(0,
                                                                                   templateName.length() - ".*".length());

                                Template parentTemplate = findTemplate(parentTemplateName, templateStack,
                                                                       ((Placeholder) node).location, "Referenced");

                                subtemplates = parentTemplate.subtemplates;
                                parentName = parentTemplateName + ".";
                            }

                            for (Template template : subtemplates.values()) {
                                PlaceholderParameter newParam = new PlaceholderParameter("#" + parentName
                                                                                         + template.getName());
                                newParam.templateReference = new Template(template); // create template ref
                                expandedParameters.add(newParam);
                            }
                        } else {
                            param.templateReference = new Template(findTemplate(templateName, templateStack,
                                                                                ((Placeholder) node).location, "Referenced")); // create template ref

                            expandedParameters.add(param);
                        }
                    } else {
                        expandedParameters.add(param);
                    }
                }

                ((Placeholder) node).params = expandedParameters.toArray(new PlaceholderParameter[expandedParameters
                        .size()]);
            }
        }

        private Template findTemplate(String templateName, LinkedList<Map<String, Template>> templateStack,
                                      Location location, String messagePrefix) {
            String[] parts = split(templateName, ".");
            Template template = null;

            if (parts.length >= 1) {
                for (Map<String, Template> templates : templateStack) {
                    template = templates.get(parts[0]);

                    if (template != null) {
                        break;
                    }
                }

                // 在predefined templates中找
                if (template == null) {
                    template = predefinedTemplates.get(parts[0]);
                }

                // 取子模板
                for (int i = 1; i < parts.length && template != null; i++) {
                    template = template.getSubTemplate(parts[i]);
                }
            }

            if (template == null) {
                throw new TemplateParseException(messagePrefix + " template " + templateName
                                                 + " is not found in the context around " + location);
            }

            return template;
        }

        /** 除去template结尾的换行。 */
        private void chomp(LinkedList<Node> nodes) {
            if (!nodes.isEmpty() && nodes.getLast() instanceof Text) {
                Text node = (Text) nodes.getLast();
                String text = node.text;

                if (text.endsWith("\n")) {
                    text = text.substring(0, text.length() - 1);
                }

                if (isEmpty(text)) {
                    nodes.removeLast();
                } else {
                    nodes.set(nodes.size() - 1, new Text(text, node.location));
                }
            }
        }

        /** 去除每行的首尾空白，去除模板首尾的空白和换行符。 */
        private boolean trimIfNeccessary(LinkedList<Node> nodes, Map<String, String> params) {
            boolean trimming = getBoolean(params.get("trimming"), false, "on", "yes", "true");

            if (!trimming) {
                return false;
            }

            // 去除模板开头的空白。
            if (!nodes.isEmpty() && nodes.getFirst() instanceof Text) {
                Text firstNode = (Text) nodes.getFirst();
                String text = trimStart(firstNode.text);

                if (isEmpty(text)) {
                    nodes.removeFirst();
                } else {
                    nodes.set(0, new Text(text, firstNode.location));
                }
            }

            // 去除模板末尾的空白。
            if (!nodes.isEmpty() && nodes.getLast() instanceof Text) {
                Text lastNode = (Text) nodes.getLast();
                String text = trimEnd(lastNode.text);

                if (isEmpty(text)) {
                    nodes.removeLast();
                } else {
                    nodes.set(nodes.size() - 1, new Text(text, lastNode.location));
                }
            }

            // 去除每行首尾的空白
            boolean startOfLine = true;
            boolean endOfLine;

            for (ListIterator<Node> i = nodes.listIterator(); i.hasNext(); ) {
                Node node = i.next();
                endOfLine = !i.hasNext();

                if (!(node instanceof Text)) {
                    startOfLine = false;
                    continue;
                }

                String text = ((Text) node).text;
                StringBuilder buf = new StringBuilder(text.length());

                int start = 0;
                for (int pos = text.indexOf("\n"); pos >= 0 && pos < text.length(); start = pos + 1, pos = text
                        .indexOf("\n", start)) {
                    String line = text.substring(start, pos);

                    if (startOfLine) {
                        line = trim(line);
                    } else {
                        line = trimEnd(line);
                    }

                    buf.append(line).append("\n");
                    startOfLine = true;
                }

                String line = text.substring(start);

                if (startOfLine && endOfLine) {
                    line = trim(line);
                } else if (startOfLine) {
                    line = trimStart(line);
                } else if (endOfLine) {
                    line = trimEnd(line);
                }

                buf.append(line);

                i.set(new Text(buf.toString(), node.location));
            }

            return true;
        }

        /** 将多个空白变成一个空白；如果多个空白中包含换行符，则转换成一个换行符。 */
        private boolean collapseWhitespacesIfNeccessary(LinkedList<Node> nodes, Map<String, String> params) {
            boolean collapseWhitespaces = getBoolean(params.get("whitespace"), false, "collapse");

            if (!collapseWhitespaces) {
                return false;
            }

            for (ListIterator<Node> i = nodes.listIterator(); i.hasNext(); ) {
                Node node = i.next();

                if (node instanceof Text) {
                    char[] cs = ((Text) node).text.toCharArray();
                    StringBuilder buf = new StringBuilder(cs.length);
                    boolean ws = false;
                    boolean newline = false;

                    for (char c : cs) {
                        if (c == '\n') {
                            newline = true;
                        } else if (Character.isWhitespace(c)) {
                            ws = true;
                        } else {
                            if (newline) {
                                buf.append('\n');
                            } else if (ws) {
                                buf.append(' ');
                            }

                            ws = false;
                            newline = false;
                            buf.append(c);
                        }
                    }

                    if (newline) {
                        buf.append('\n');
                    } else if (ws) {
                        buf.append(' ');
                    }

                    i.set(new Text(buf.toString(), node.location));
                }
            }

            return true;
        }

        private boolean getBoolean(String value, boolean defaultValue, String... specificValues) {
            if (!isEmpty(value) && !isEmptyArray(specificValues)) {
                for (String specificValue : specificValues) {
                    if (value.equalsIgnoreCase(specificValue)) {
                        return !defaultValue;
                    }
                }
            }

            return defaultValue;
        }
    }

    /** 保存纯文本内容，并记录第一个非空字符的行列号。 */
    private static class TextBuffer {
        private final StringBuilder buf = new StringBuilder();
        private final String systemId;
        private int lineNumber   = -1;
        private int columnNumber = -1;

        public TextBuffer(String systemId) {
            this.systemId = systemId;
        }

        public void append(String s, int lineNumber, int columnNumber) {
            append(s, 0, s.length(), lineNumber, columnNumber);
        }

        public void append(CharSequence s, int start, int end, int lineNumber, int columnNumber) {
            buf.append(s, start, end);

            if (this.lineNumber == -1 || this.columnNumber == -1) {
                for (int i = start; i < end; i++) {
                    char c = s.charAt(i);

                    if (Character.isWhitespace(c)) {
                        columnNumber++;
                    } else {
                        this.lineNumber = lineNumber;
                        this.columnNumber = columnNumber;
                        break;
                    }
                }
            }
        }

        public void newLine() {
            buf.append("\n");
        }

        public void clear() {
            buf.setLength(0);
            lineNumber = -1;
            columnNumber = -1;
        }

        public Text toText() {
            if (buf.length() > 0) {
                return new Text(buf.toString(), new Location(systemId, lineNumber, columnNumber));
            }

            return null;
        }
    }

    private static class ParsingTemplateStack {
        private final LinkedList<ParsingTemplate> stack = createLinkedList();

        public void push(ParsingTemplate pt) {
            ParsingTemplate currentParsingTemplate = peek();

            if (currentParsingTemplate != null) {
                Template duplicatedTemplate = currentParsingTemplate.getSubTemplate(pt.name);

                if (duplicatedTemplate != null) {
                    throw new TemplateParseException("Duplicated template name #" + pt.name + " at "
                                                     + Location.toString(pt.systemId, pt.lineNumber, pt.columnNumber)
                                                     + ".  Another template with the same name is located in " + duplicatedTemplate.location);
                }
            }

            stack.addLast(pt);
        }

        public ParsingTemplate peek() {
            if (stack.isEmpty()) {
                return null;
            } else {
                return stack.getLast();
            }
        }

        public int size() {
            return stack.size();
        }

        public Template pop() {
            return stack.removeLast().toTemplate();
        }

        @Override
        public String toString() {
            return new ToStringBuilder().append(stack).toString();
        }
    }

    private static class ParsingTemplate {
        private final String systemId;
        private final int    lineNumber;
        private final int    columnNumber;
        private final String name;
        private final LinkedList<Node>      nodes        = createLinkedList();
        private final Map<String, String>   params       = createTreeMap();
        private final Map<String, Template> subtemplates = createArrayHashMap();

        public ParsingTemplate(String name, String systemId, int lineNumber, int columnNumber,
                               Map<String, String> parentParams) {
            this.name = name;
            this.systemId = systemId;
            this.lineNumber = lineNumber;
            this.columnNumber = columnNumber;

            if (parentParams != null) {
                this.params.putAll(parentParams);
            }
        }

        public void addNode(Node node) {
            if (node != null) {
                if (!subtemplates.isEmpty()) {
                    // 在sub templates被定义以后，不能再增加任何nodes，否则报错。
                    // 特殊情况：对于text node，如果未包含可见字符，则不报错，只是安静地丢弃。
                    if (node.location.lineNumber > 0) {
                        throw new TemplateParseException("Invalid " + node.desc() + " here at " + node.location);
                    }
                } else {
                    if (node instanceof Text && !nodes.isEmpty() && nodes.getLast() instanceof Text) {
                        // 合并text node
                        Text lastNode = (Text) nodes.removeLast();
                        Text thisNode = (Text) node;
                        Location location;

                        if (lastNode.location.lineNumber > 0) {
                            location = lastNode.location;
                        } else {
                            location = node.location;
                        }

                        nodes.add(new Text(lastNode.text + thisNode.text, location));
                    } else {
                        nodes.add(node);
                    }
                }
            }
        }

        public void addParam(String name, String value, int lineNumber, int columnNumber) {
            if (!subtemplates.isEmpty() || hasNonEmptyNode()) {
                throw new TemplateParseException("Invalid #@" + name + " here at "
                                                 + Location.toString(systemId, lineNumber, columnNumber));
            }

            params.put(name, trimToEmpty(value));
        }

        public void addSubTemplate(Template template) {
            subtemplates.put(template.getName(), template);
        }

        public Template getSubTemplate(String name) {
            return subtemplates.get(name);
        }

        private boolean hasNonEmptyNode() {
            for (Node node : nodes) {
                if (node.location.lineNumber > 0) {
                    return true;
                }
            }

            return false;
        }

        public Template toTemplate() {
            return new Template(name, nodes.toArray(new Node[nodes.size()]), params, subtemplates, new Location(
                    systemId, lineNumber, columnNumber));
        }

        public void updateTemplate(Template template) {
            template.update(nodes.toArray(new Node[nodes.size()]), params, subtemplates);
        }

        @Override
        public String toString() {
            MapBuilder mb = new MapBuilder();

            mb.append("name", name);
            mb.append("systemId", systemId);
            mb.append("lineNumber", lineNumber);
            mb.append("columnNumber", columnNumber);
            mb.append("nodes", nodes);
            mb.append("params", params);
            mb.append("sub-templates", subtemplates);

            return new ToStringBuilder().append("Template").append(mb).toString();
        }
    }

    /** 保存文件来源，必要时重装模板。 */
    static class InputSource {
        private final static Pattern CHARSET_DETECTIVE_PATTERN = Pattern.compile("^(\\s*##)" // comment
                                                                                 + "|^(\\s*)#@\\s*(\\w+)\\s+(.*?)\\s*(##.*)?$" // charset或其它参数
                                                                                 + "|^\\s*$" // empty line
        );

        private long lastModified = 0;
        final String systemId;
        Object source;

        public InputSource(File source) {
            this(source, null);
        }

        public InputSource(URL source) {
            this(source, null);
        }

        public InputSource(InputStream source, String systemId) {
            this((Object) source, systemId);
        }

        public InputSource(Reader source, String systemId) {
            this((Object) source, systemId);
        }

        private InputSource(Object source, String systemId) {
            assertNotNull(source, "source");

            if (source instanceof URL) {
                try {
                    this.source = new File(((URL) source).toURI().normalize()); // convert URL to File
                } catch (Exception e) {
                    this.source = source;
                }
            } else if (source instanceof File) {
                this.source = new File(((File) source).toURI().normalize()); // remove ../
            } else {
                this.source = source;
            }

            if (this.source instanceof URL) {
                this.systemId = ((URL) this.source).toExternalForm();
            } else if (this.source instanceof File) {
                this.systemId = ((File) this.source).toURI().toString();
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
                    reader = getReader();
                } catch (IOException e) {
                    throw new TemplateParseException(e);
                }

                new Parser(reader, systemId, this).parse().updateTemplate(template);

                if (source instanceof File) {
                    this.lastModified = ((File) source).lastModified();
                }
            }
        }

        /** 根据指定的相对于当前source的路径，取得input source。 */
        InputSource getRelative(String relativePath) throws Exception {
            relativePath = trimToNull(relativePath);

            if (relativePath != null) {
                String sourceURI = null;

                if (source instanceof File) {
                    sourceURI = ((File) source).toURI().toString();
                } else if (source instanceof URL) {
                    sourceURI = ((URL) source).toExternalForm();
                }

                if (sourceURI != null) {
                    return new InputSource(new URL(FileUtil.resolve(sourceURI, relativePath)));
                }
            }

            return null;
        }

        Reader getReader() throws IOException {
            Reader reader;

            if (source instanceof Reader) {
                reader = (Reader) source;
                source = null; // clear source
            } else {
                BufferedInputStream istream = null;

                if (source instanceof File) {
                    istream = new BufferedInputStream(new FileInputStream((File) source));
                } else if (source instanceof URL) {
                    try {
                        source = new File(((URL) source).toURI());
                        istream = new BufferedInputStream(new FileInputStream((File) source));
                    } catch (IllegalArgumentException e) {
                    } catch (URISyntaxException e) {
                    }

                    if (istream == null) {
                        istream = new BufferedInputStream(((URL) source).openStream());
                    }
                } else if (source instanceof InputStream) {
                    istream = new BufferedInputStream((InputStream) source);
                    source = null; // clear source
                } else {
                    throw new IllegalStateException("Unknown source: " + source);
                }

                String charset = detectCharset(istream, "UTF-8");
                reader = new InputStreamReader(istream, charset);
            }

            return reader;
        }

        /** 读取输入流的前几行，查找<code>#@charset</code>参数。如果未找到，则返回指定默认值。 */
        static String detectCharset(BufferedInputStream istream, String defaultCharset) throws IOException {
            int readlimit = 1024 * 4;
            istream.mark(readlimit);

            StringBuilder buf = new StringBuilder(readlimit);

            try {
                int c;
                for (int i = 0; i < readlimit && (c = istream.read()) != -1; i++) {
                    if (c == '\r' || c == '\n') {
                        String line = buf.toString();
                        buf.setLength(0);

                        Matcher matcher = CHARSET_DETECTIVE_PATTERN.matcher(line);

                        if (matcher.find()) {
                            String charset = null;

                            if ("charset".equals(matcher.group(3))) {
                                charset = matcher.group(4);
                            }

                            // 如果找到#@charset则返回。
                            // 忽略charset前的空行、注释和其它参数。
                            if (charset != null) {
                                return charset;
                            }
                        } else {
                            // 碰到第一行非注释、非空行、非#@param，则立即返回。
                            break;
                        }
                    } else {
                        buf.append((char) c);
                    }
                }
            } finally {
                istream.reset();
            }

            return defaultCharset;
        }
    }
}
