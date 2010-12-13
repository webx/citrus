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
package com.alibaba.citrus.generictype.introspect;

import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.CollectionUtil.*;

import java.util.List;

import com.alibaba.citrus.util.internal.StringUtil;

/**
 * 代表一个<code>PropertyPath</code>，其格式为：
 * 
 * <pre>
 * ( &quot;.&quot;? propertyName | [index] | [key] )
 *     ( &quot;.&quot; propertyName  | [index] | [key] )*
 * </pre>
 * <p>
 * 其中，<code>index</code>为整数，<code>key</code>为单引号或双引号包围的字符串。<br>
 * 在<code>key</code>中，可使用Java字符串转义符，例如：<code>"\n\u1234"</code>。
 * </p>
 * <p>
 * <code>PropertyPath</code>支持事件模式（类似SAX）和DOM两种模式。
 * </p>
 * 
 * @author Michael Zhou
 */
public class PropertyPath {
    private final Node[] nodes;

    /**
     * 创建一个<code>PropertyPath</code>结构。
     */
    private PropertyPath(List<Node> nodes) {
        this.nodes = nodes.toArray(new Node[nodes.size()]);
    }

    /**
     * 解析字符串并生成<code>PropertyPath</code>结构。
     */
    public static PropertyPath parse(String propertyPath) {
        Parser parser = new Parser(propertyPath);

        parser.parse();

        return new PropertyPath(parser.nodes);
    }

    /**
     * 解析<code>PropertyPath</code>字符串，并访问指定visitor。
     */
    public static void parse(String propertyPath, Visitor visitor) {
        new Parser(propertyPath, visitor).parse();
    }

    /**
     * 取得所有结点。
     */
    public Node[] getNodes() {
        return nodes.clone();
    }

    /**
     * 访问visitor。
     */
    public void accept(Visitor visitor) {
        int length = nodes.length;
        Node node = null;

        for (int i = 0; i < length; i++) {
            if (node == null) {
                if (i < nodes.length - 1) {
                    node = nodes[i];
                } else {
                    nodes[i].accept(visitor);
                }
            } else {
                Node lookAhead = nodes[i];

                if (node.accept(visitor, lookAhead)) {
                    node = null;
                } else {
                    node.accept(visitor);
                    node = lookAhead;
                }
            }
        }

        if (node != null) {
            node.accept(visitor);
            node = null;
        }
    }

    /**
     * 转换成字符串。
     */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();

        buf.append("PropertyPath {\n");

        for (Node node : nodes) {
            buf.append("  ").append(node).append("\n");
        }

        buf.append("}");

        return buf.toString();
    }

    /**
     * 用来访问<code>PropertyPath</code>的visitor。
     */
    public static interface Visitor {
        /**
         * 访问property[index]。
         */
        boolean visitIndexedProperty(String propertyName, int index, String displayName, boolean last);

        /**
         * 访问property[key]。
         */
        boolean visitMappedProperty(String propertyName, String key, String displayName, boolean last);

        /**
         * 访问property。
         */
        void visitSimpleProperty(String propertyName, String displayName, boolean last);

        /**
         * 访问[index]。
         */
        void visitIndex(int index, String displayName, boolean last);

        /**
         * 访问[key]。
         */
        void visitKey(String key, String displayName, boolean last);
    }

    /**
     * 代表<code>PropertyPath</code>中的一个结点。
     */
    public static interface Node {
        boolean accept(Visitor visitor, Node lookAhead);

        void accept(Visitor visitor);
    }

    /**
     * 代表<code>PropertyPath</code>中的一个property name结点。
     */
    public static final class PropertyName implements Node {
        private final String propertyName;
        private final String displayName;
        private final boolean last;

        public PropertyName(String propertyName, String displayName, boolean last) {
            this.propertyName = propertyName;
            this.displayName = displayName;
            this.last = last;
        }

        public String getPropertyName() {
            return propertyName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public boolean accept(Visitor visitor, Node lookAhead) {
            if (lookAhead instanceof Index) {
                Index key = (Index) lookAhead;

                return visitor.visitIndexedProperty(propertyName, key.index, key.displayName, key.last);
            } else if (lookAhead instanceof Key) {
                Key key = (Key) lookAhead;

                return visitor.visitMappedProperty(propertyName, key.key, key.displayName, key.last);
            } else {
                return false;
            }
        }

        public void accept(Visitor visitor) {
            visitor.visitSimpleProperty(propertyName, displayName, last);
        }

        @Override
        public String toString() {
            return propertyName;
        }
    }

    /**
     * 代表<code>PropertyPath</code>中的一个indexed key结点。
     */
    public static final class Index implements Node {
        private final int index;
        private final String displayName;
        private final boolean last;

        public Index(int index, String displayName, boolean last) {
            this.index = index;
            this.displayName = displayName;
            this.last = last;
        }

        public int getIndex() {
            return index;
        }

        public String getDisplayName() {
            return displayName;
        }

        public boolean accept(Visitor visitor, Node lookAhead) {
            return false;
        }

        public void accept(Visitor visitor) {
            visitor.visitIndex(index, displayName, last);
        }

        @Override
        public String toString() {
            return String.format("[%d]", index);
        }
    }

    /**
     * 代表<code>PropertyPath</code>中的一个mapped key结点。
     */
    public static final class Key implements Node {
        private final String key;
        private final String displayName;
        private final boolean last;

        public Key(String key, String displayName, boolean last) {
            this.key = key;
            this.displayName = displayName;
            this.last = last;
        }

        public String getKey() {
            return key;
        }

        public String getDisplayName() {
            return displayName;
        }

        public boolean accept(Visitor visitor, Node lookAhead) {
            return false;
        }

        public void accept(Visitor visitor) {
            visitor.visitKey(key, displayName, last);
        }

        @Override
        public String toString() {
            return String.format("[\"%s\"]", StringUtil.escapeJava(key));
        }
    }

    /**
     * <code>PropertyPath</code>词法分析器。
     */
    private abstract static class Tokenizer {
        private final static char EOF = '\0';
        private final static int KEY_STATE_NORMAL = 0;
        private final static int KEY_STATE_ESCAPE = 1;
        private final static int KEY_STATE_UNICODE = 2;
        protected final String propertyPath;
        private final int lastIndex;

        // Parser的状态
        private int i = -1;
        private char ch = EOF;
        private String propertyName = null;
        private int index = -1;
        private String key = null;

        public Tokenizer(String propertyPath) {
            this.propertyPath = propertyPath;
            this.lastIndex = propertyPath.length() - 1;

            next();
            skipSpaces();
        }

        public final void parse() {
            boolean firstToken = true;

            while (ch != EOF) {
                if (Character.isLetter(ch) && firstToken) {
                    propertyName();
                    propertyName(propertyName, propertyPath.substring(0, i));
                } else if (ch == '.') {
                    next();
                    skipSpaces();

                    if (Character.isLetter(ch)) {
                        propertyName();
                    } else {
                        syntaxError();
                    }

                    propertyName(propertyName, propertyPath.substring(0, i));
                } else if (ch == '[') {
                    next();
                    skipSpaces();
                    indexOrKey();
                    skipSpaces();

                    if (ch != ']') {
                        syntaxError();
                    }

                    next();

                    if (index != -1) {
                        index(index, propertyPath.substring(0, i));
                    } else { // key != null
                        key(key, propertyPath.substring(0, i));
                    }
                } else {
                    syntaxError();
                }

                skipSpaces();
                firstToken = false;
            }

            done();
        }

        private void propertyName() {
            int beginIndex = i;

            while (Character.isLetterOrDigit(ch)) {
                next();
            }

            propertyName = propertyPath.substring(beginIndex, i);
        }

        private void indexOrKey() {
            if ('0' <= ch && ch <= '9') {
                index();
            } else if (ch == '\"' || ch == '\'') {
                char quote = ch;
                next();
                key(quote);
                next();
            }
        }

        private void index() {
            int beginIndex = i;

            while ('0' <= ch && ch <= '9') {
                next();
            }

            index = Integer.parseInt(propertyPath.substring(beginIndex, i));
            key = null;
        }

        private void key(char quote) {
            StringBuilder buf = new StringBuilder();
            StringBuilder unicode = new StringBuilder(4);
            int keyState = KEY_STATE_NORMAL;

            for (; ch != EOF && (ch != quote || keyState != KEY_STATE_NORMAL); next()) {
                switch (keyState) {
                    case KEY_STATE_UNICODE:
                        unicode.append(ch);

                        if (unicode.length() == 4) {
                            String unicodeStr = unicode.toString();

                            try {
                                int value = Integer.parseInt(unicodeStr, 16);

                                buf.append((char) value);
                            } catch (NumberFormatException e) {
                                buf.append("\\u" + unicodeStr);
                            }

                            unicode.setLength(0);
                            keyState = KEY_STATE_NORMAL;
                        }

                        break;

                    case KEY_STATE_ESCAPE:
                        keyState = KEY_STATE_NORMAL;

                        switch (ch) {
                            case '\\':
                                buf.append('\\');
                                break;

                            case '\'':
                                buf.append('\'');
                                break;

                            case '\"':
                                buf.append('"');
                                break;

                            case 'r':
                                buf.append('\r');
                                break;

                            case 'f':
                                buf.append('\f');
                                break;

                            case 't':
                                buf.append('\t');
                                break;

                            case 'n':
                                buf.append('\n');
                                break;

                            case 'b':
                                buf.append('\b');
                                break;

                            case 'u': {
                                keyState = KEY_STATE_UNICODE;
                                break;
                            }

                            default:
                                buf.append(ch);
                                break;
                        }

                        break;

                    case KEY_STATE_NORMAL:
                        if (ch == '\\') {
                            keyState = KEY_STATE_ESCAPE;
                            continue;
                        }

                        buf.append(ch);
                        break;

                    default:
                        unreachableCode();
                }
            }

            if (ch != quote) {
                syntaxError();
            }

            index = -1;
            key = buf.toString();
        }

        protected abstract void propertyName(String propertyName, String displayName);

        protected abstract void index(int index, String displayName);

        protected abstract void key(String key, String displayName);

        protected abstract void done();

        private void syntaxError() {
            throw new InvalidPropertyPathException(propertyPath, i);
        }

        private void skipSpaces() {
            while (Character.isWhitespace(ch)) {
                next();
            }
        }

        private char next() {
            if (i >= lastIndex) {
                ch = EOF;
                i = lastIndex + 1;
            } else {
                ch = propertyPath.charAt(++i);
            }

            return ch;
        }
    }

    /**
     * <code>PropertyPath</code>解析器。
     */
    private static class Parser extends Tokenizer implements Visitor {
        private final List<Node> nodes;
        private final Visitor visitor;
        private String lastPropertyName = null;
        private String lastPropertyDisplayName = null;
        private int lastIndex = -1;
        private String lastKey = null;
        private String lastKeyDisplayName = null;

        public Parser(String propertyPath) {
            this(propertyPath, null);
        }

        public Parser(String propertyPath, Visitor visitor) {
            super(propertyPath);

            if (visitor == null) {
                this.visitor = this;
                this.nodes = createLinkedList();
            } else {
                this.visitor = visitor;
                this.nodes = null;
            }
        }

        @Override
        protected void propertyName(String propertyName, String displayName) {
            if (lastPropertyName == null && lastIndex == -1 && lastKey == null) {
                lastPropertyName = propertyName;
                lastPropertyDisplayName = displayName;
            } else {
                visitLast(false);
                lastPropertyName = propertyName;
                lastPropertyDisplayName = displayName;
            }
        }

        @Override
        protected void index(int index, String displayName) {
            if (lastIndex == -1 && lastKey == null) {
                lastIndex = index;
                lastKeyDisplayName = displayName;
            } else {
                visitLast(false);
                lastIndex = index;
                lastKeyDisplayName = displayName;
            }
        }

        @Override
        protected void key(String key, String displayName) {
            if (lastIndex == -1 && lastKey == null) {
                lastKey = key;
                lastKeyDisplayName = displayName;
            } else {
                visitLast(false);
                lastKey = key;
                lastKeyDisplayName = displayName;
            }
        }

        @Override
        protected void done() {
            visitLast(true);
        }

        private void visitLast(boolean last) {
            if (lastPropertyName == null && lastIndex == -1 && lastKey == null) {
                return;
            }

            if (lastPropertyName == null) {
                if (lastIndex != -1) {
                    visitor.visitIndex(lastIndex, lastKeyDisplayName, last);
                } else { // lastKey != null
                    visitor.visitKey(lastKey, lastKeyDisplayName, last);
                }
            } else {
                if (lastIndex != -1) {
                    if (!visitor.visitIndexedProperty(lastPropertyName, lastIndex, lastKeyDisplayName, last)) {
                        visitor.visitSimpleProperty(lastPropertyName, lastPropertyDisplayName, false);
                        visitor.visitIndex(lastIndex, lastKeyDisplayName, last);
                    }
                } else if (lastKey != null) {
                    if (!visitor.visitMappedProperty(lastPropertyName, lastKey, lastKeyDisplayName, last)) {
                        visitor.visitSimpleProperty(lastPropertyName, lastPropertyDisplayName, false);
                        visitor.visitKey(lastKey, lastKeyDisplayName, last);
                    }
                } else {
                    visitor.visitSimpleProperty(lastPropertyName, lastPropertyDisplayName, last);
                }
            }

            lastPropertyName = null;
            lastPropertyDisplayName = null;
            lastIndex = -1;
            lastKey = null;
            lastKeyDisplayName = null;
        }

        /**
         * 访问property[index]。
         */
        public boolean visitIndexedProperty(String propertyName, int index, String displayName, boolean last) {
            return false;
        }

        /**
         * 访问property[key]。
         */
        public boolean visitMappedProperty(String propertyName, String key, String displayName, boolean last) {
            return false;
        }

        /**
         * 访问property。
         */
        public void visitSimpleProperty(String propertyName, String displayName, boolean last) {
            nodes.add(new PropertyName(propertyName, displayName, last));
        }

        /**
         * 访问[index]。
         */
        public void visitIndex(int index, String displayName, boolean last) {
            nodes.add(new Index(index, displayName, last));
        }

        /**
         * 访问[key]。
         */
        public void visitKey(String key, String displayName, boolean last) {
            nodes.add(new Key(key, displayName, last));
        }
    }
}
