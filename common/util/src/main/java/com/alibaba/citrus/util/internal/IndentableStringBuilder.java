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
package com.alibaba.citrus.util.internal;

import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.BasicConstant.*;
import static com.alibaba.citrus.util.ObjectUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.util.ArrayList;

/**
 * 支持分级缩进的string builder。
 * 
 * @author Michael Zhou
 */
public class IndentableStringBuilder extends NormalizableStringBuilder<IndentableStringBuilder> {
    private final IndentStack indents = new IndentStack();
    private final int defaultIndent;
    private int indentLevel;
    private int quoteLevel;
    private boolean lazyAppendNewLine; // 推迟输出换行，推迟到下一个字符被输出前
    private boolean lazyStartHangingIndent; // 推迟启动缩进，推迟到下一个换行后或下一个start()。其效果为悬挂缩进
    private int hangingIndent;

    public IndentableStringBuilder() {
        this(-1);
    }

    public IndentableStringBuilder(int indent) {
        this.defaultIndent = indent <= 0 ? 2 : indent;
    }

    @Override
    public void clear() {
        super.clear();

        indents.clear();
        indentLevel = 0;
        quoteLevel = 0;
        lazyAppendNewLine = false;
        lazyStartHangingIndent = false;
        hangingIndent = 0;
    }

    /**
     * 此处收到的字符中，所有 CR/LF/CRLF 均已被规格化成统一的LF了。
     */
    @Override
    protected void visit(char c) {
        boolean newLine = endsWithNewLine();

        if (c == LF && lazyStartHangingIndent) {
            appendInternalNewLine();
            doStartHanglingIndentIfRequired();
            return;
        }

        // 在end quote后追加换行
        if (!newLine && lazyAppendNewLine) {
            appendInternalNewLine();
            newLine = true;
        }

        // 输出begin quotes
        for (; quoteLevel < indentLevel; quoteLevel++) {
            String beginQuote = indents.getBeginQuote(quoteLevel);

            if (isEmpty(beginQuote)) {
                if (!newLine && indents.independent(quoteLevel)) {
                    appendInternalNewLine();
                    newLine = true;
                }
            } else {
                if (newLine) {
                    appendIndent(quoteLevel);
                } else {
                    if (!endsWith(" ")) {
                        appendInternal(" "); // begin quote前空一格
                    }
                }

                appendInternal(beginQuote);
                appendInternalNewLine();

                newLine = true;
            }
        }

        lazyAppendNewLine = false;

        // 输出字符
        if (c == LF) {
            appendInternalNewLine();
        } else {
            if (newLine) {
                appendIndent(indentLevel);
            }

            appendInternal(c);
        }
    }

    /**
     * 创建一级缩进。
     */
    public IndentableStringBuilder start() {
        return start(null, null, -1);
    }

    /**
     * 创建一级缩进。
     */
    public IndentableStringBuilder start(int indent) {
        return start(null, null, indent);
    }

    /**
     * 创建一级缩进，使用指定的前后括弧。
     */
    public IndentableStringBuilder start(String beginQuote, String endQuote) {
        return start(beginQuote, endQuote, -1);
    }

    /**
     * 创建一级缩进，使用指定的前后括弧。
     */
    public IndentableStringBuilder start(String beginQuote, String endQuote, int indent) {
        doStartHanglingIndentIfRequired();
        indents.pushIndent(beginQuote, endQuote, indent);
        indentLevel++;
        return this;
    }

    /**
     * 从下一个换行或start()开始悬挂缩进。
     */
    public IndentableStringBuilder startHangingIndent() {
        return startHangingIndent(0);
    }

    /**
     * 从下一个换行或start()开始悬挂缩进。
     */
    public IndentableStringBuilder startHangingIndent(int indentOffset) {
        doStartHanglingIndentIfRequired();

        lazyStartHangingIndent = true;

        if (!lazyAppendNewLine && lineLength() - currentIndent() > 0 && quoteLevel >= indentLevel) {
            hangingIndent = defaultIndent(lineLength() - currentIndent() + indentOffset);
        } else {
            hangingIndent = defaultIndent(indentOffset);
        }

        return this;
    }

    /**
     * 确保悬挂缩进（如果有的话）已经启动。
     */
    private void doStartHanglingIndentIfRequired() {
        if (lazyStartHangingIndent) {
            lazyStartHangingIndent = false;
            start(EMPTY_STRING, EMPTY_STRING, hangingIndent);
        }
    }

    /**
     * 结束一级缩进。注意，输出结果之前，须至少调用一次end()，以确保最后的换行可以被输出。
     */
    public IndentableStringBuilder end() {
        flush();

        // 结束未发生的悬挂缩进
        if (lazyStartHangingIndent) {
            if (!endsWithNewLine()) {
                lazyAppendNewLine = true;
            }

            lazyStartHangingIndent = false;
            return this;
        }

        // 对于刚开始就结束的，不输出end quote
        if (indentLevel > quoteLevel) {
            indentLevel--;
        } else {
            assertTrue(indentLevel == quoteLevel, "indentLevel != quoteLevel");

            if (indentLevel > 0) {
                indentLevel--;
                quoteLevel--;

                String endQuote = indents.getEndQuote(indentLevel);

                if (!isEmpty(endQuote)) {
                    // 确保end quote之前换行
                    if (!endsWithNewLine()) {
                        appendInternalNewLine();
                    }

                    // 输出end quote
                    appendIndent(indentLevel);
                    appendInternal(endQuote);
                }

                lazyAppendNewLine = true;
            }
        }

        indents.popIndent();

        return this;
    }

    /**
     * 取得当前缩进的数量。
     */
    public int currentIndent() {
        return indents.getCurrentIndent();
    }

    /**
     * 如果indent未指定，则取得默认indent。
     */
    private int defaultIndent(int indent) {
        return indent <= 0 ? defaultIndent : indent;
    }

    private void appendIndent(int indentLevel) {
        int indent = indents.getIndent(indentLevel - 1);

        for (int j = 0; j < indent; j++) {
            appendInternal(' ');
        }
    }

    /**
     * 存放缩进信息的栈。
     */
    private class IndentStack extends ArrayList<Object> {
        private static final long serialVersionUID = -876139304840511103L;
        private static final int entrySize = 4;

        public String getBeginQuote(int indentLevel) {
            if (indentLevel < 0 || indentLevel >= depth()) {
                return EMPTY_STRING;
            }

            return (String) super.get(indentLevel * entrySize);
        }

        public String getEndQuote(int indentLevel) {
            if (indentLevel < 0 || indentLevel >= depth()) {
                return EMPTY_STRING;
            }

            return (String) super.get(indentLevel * entrySize + 1);
        }

        public int getIndent(int indentLevel) {
            if (indentLevel < 0 || indentLevel >= depth()) {
                return 0;
            }

            return (Integer) super.get(indentLevel * entrySize + 2);
        }

        /**
         * 如果当前level依附于后一个level，则返回false。
         */
        public boolean independent(int indentLevel) {
            if (indentLevel < 0 || indentLevel >= depth() - 1) {
                return true;
            }

            int i1 = (Integer) super.get(indentLevel * entrySize + 3);
            int i2 = (Integer) super.get((indentLevel + 1) * entrySize + 3);

            return i1 != i2;
        }

        public int getCurrentIndent() {
            int depth = depth();

            if (depth > 0) {
                return getIndent(depth - 1);
            } else {
                return 0;
            }
        }

        public int depth() {
            return super.size() / entrySize;
        }

        public void pushIndent(String beginQuote, String endQuote, int indent) {
            super.add(defaultIfNull(beginQuote, "{"));
            super.add(defaultIfNull(endQuote, "}"));
            super.add(defaultIndent(indent) + getCurrentIndent());
            super.add(length());
        }

        public void popIndent() {
            int length = super.size();

            if (length > 0) {
                for (int i = 0; i < entrySize; i++) {
                    super.remove(--length);
                }
            }
        }
    }
}

/**
 * 将CR/LF/CRLF统一成LF的string builder。
 * 
 * @author Michael Zhou
 */
abstract class NormalizableStringBuilder<B extends NormalizableStringBuilder<B>> implements Appendable {
    protected final static char CR = '\r';
    protected final static char LF = '\n';
    private final static char NONE = '\0';
    private final StringBuilder out = new StringBuilder();
    private final String newLine;
    private int newLineStartIndex = 0;
    private char readAheadBuffer = '\0';

    public NormalizableStringBuilder() {
        this(null);
    }

    public NormalizableStringBuilder(String newLine) {
        this.newLine = defaultIfNull(newLine, String.valueOf(LF));
    }

    /**
     * 清除所有内容。
     */
    public void clear() {
        out.setLength(0);
        newLineStartIndex = 0;
        readAheadBuffer = '\0';
    }

    /**
     * 取得buffer中内容的长度。
     */
    public final int length() {
        return out.length();
    }

    /**
     * 取得当前行的长度。
     */
    public final int lineLength() {
        return out.length() - newLineStartIndex;
    }

    /**
     * <code>Appendable</code>接口方法。
     */
    public final B append(CharSequence csq) {
        return append(csq, 0, csq.length());
    }

    /**
     * <code>Appendable</code>接口方法。
     */
    public final B append(CharSequence csq, int start, int end) {
        for (int i = start; i < end; i++) {
            append(csq.charAt(i));
        }

        return thisObject();
    }

    /**
     * <code>Appendable</code>接口方法。
     */
    public final B append(char c) {
        // 将 CR|LF|CRLF 转化成统一的 LF
        switch (readAheadBuffer) {
            case NONE:
                switch (c) {
                    case CR: // \r
                        readAheadBuffer = CR;
                        break;

                    case LF: // \n
                        readAheadBuffer = NONE;
                        visit(LF);
                        break;

                    default:
                        readAheadBuffer = NONE;
                        visit(c);
                        break;
                }

                break;

            case CR:
                switch (c) {
                    case CR: // \r\r
                        readAheadBuffer = CR;
                        visit(LF);
                        break;

                    case LF: // \r\n
                        readAheadBuffer = NONE;
                        visit(LF);
                        break;

                    default:
                        readAheadBuffer = NONE;
                        visit(LF);
                        visit(c);
                        break;
                }

                break;

            default:
                unreachableCode();
                break;
        }

        return thisObject();
    }

    /**
     * 子类覆盖此方法，以便接收所有字符。其中，所有 CR/LF/CRLF 均已被规格化成统一的LF了。
     */
    protected abstract void visit(char c);

    /**
     * 子类通过此方法向内部buffer中添加内容。
     */
    protected final void appendInternal(String s) {
        out.append(s);
    }

    /**
     * 子类通过此方法向内部buffer中添加内容。
     */
    protected final void appendInternal(char c) {
        out.append(c);
    }

    /**
     * 子类通过此方法向内部buffer中添加换行。
     * <p>
     * 子类必须通过此方法来换行，否则<code>newLineStartIndex</code>会不正确。
     * </p>
     */
    protected final void appendInternalNewLine() {
        out.append(newLine);
        newLineStartIndex = out.length();
    }

    /**
     * 判断buf是否以指定字符串结尾。
     */
    public final boolean endsWith(String testStr) {
        if (testStr == null) {
            return false;
        }

        int testStrLength = testStr.length();
        int bufferLength = out.length();

        if (bufferLength < testStrLength) {
            return false;
        }

        int baseIndex = bufferLength - testStrLength;

        for (int i = 0; i < testStrLength; i++) {
            if (out.charAt(baseIndex + i) != testStr.charAt(i)) {
                return false;
            }
        }

        return true;
    }

    /**
     * 判断out是否以换行结尾，或者是空buffer。
     */
    public final boolean endsWithNewLine() {
        return out.length() == 0 || endsWith(newLine);
    }

    private B thisObject() {
        @SuppressWarnings("unchecked")
        B buf = (B) this;
        return buf;
    }

    /**
     * 确保最后一个换行被输出。
     */
    public final void flush() {
        if (readAheadBuffer == CR) {
            readAheadBuffer = NONE;
            visit(LF);
        }
    }

    @Override
    public final String toString() {
        return out.toString();
    }
}
