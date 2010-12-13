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
package com.alibaba.citrus.util.internal.regex;

import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.BasicConstant.*;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * 这个类将一个包含通配符的类名, 编译成正则表达式. 格式描述如下:
 * <ul>
 * <li>合法的<em>类名字符</em>包括: 字母/数字/下划线/'$';</li>
 * <li>合法的<em>类名分隔符</em>为小数点".";</li>
 * <li>"＊"代表0个或多个<em>类名字符</em>;</li>
 * <li>"？"代表1个<em>类名字符</em>;</li>
 * <li>"＊＊"代表0个或多个<em>类名字符</em>或<em>类名分隔符</em>;</li>
 * <li>不能连续出现3个"＊";</li>
 * <li>不能连续出现2个<em>类名分隔符</em>;</li>
 * <li>"＊＊"的前后只能是<em>类名分隔符</em>.</li>
 * </ul>
 * <p>
 * 转换后的正则表达式, 对每一个通配符建立<em>引用变量</em>, 依次为<code>$1</code>, <code>$2</code>, ...
 * </p>
 * 
 * @author Michael Zhou
 */
public class ClassNameWildcardCompiler {
    /** 强制从头匹配 */
    public static final int MATCH_PREFIX = 0x1000;

    // 私有常量
    private static final char ESCAPE_CHAR = '\\';
    private static final char DOT = '.';
    private static final char UNDERSCORE = '_';
    private static final char DOLLAR = '$';
    private static final char STAR = '*';
    private static final char QUESTION = '?';
    private static final String REGEX_MATCH_PREFIX = "^";
    private static final String REGEX_WORD_BOUNDARY = "\\b";
    private static final String REGEX_DOT = "\\.";
    private static final String REGEX_DOT_NO_DUP = "\\.(?!\\.)";
    private static final String REGEX_CLASS_NAME_CHAR = "[\\w\\$]";
    private static final String REGEX_CLASS_NAME_SINGLE_CHAR = "(" + REGEX_CLASS_NAME_CHAR + ")";
    private static final String REGEX_CLASS_NAME = "(" + REGEX_CLASS_NAME_CHAR + "*)";
    private static final String REGEX_CLASS_NAME_FULL = "(" + REGEX_CLASS_NAME_CHAR + "+(?:" + REGEX_DOT_NO_DUP
            + REGEX_CLASS_NAME_CHAR + "*)*(?=" + REGEX_DOT + "|$)|)" + REGEX_DOT + "?";

    // 上一个token的状态
    private static final int LAST_TOKEN_START = 0;
    private static final int LAST_TOKEN_DOT = 1;
    private static final int LAST_TOKEN_CLASS_NAME = 2;
    private static final int LAST_TOKEN_STAR = 3;
    private static final int LAST_TOKEN_DOUBLE_STAR = 4;
    private static final int LAST_TOKEN_QUESTION = 5;

    private ClassNameWildcardCompiler() {
    }

    /**
     * 将包含通配符的类名, 编译成正则表达式.
     */
    public static Pattern compileClassName(String pattern) throws PatternSyntaxException {
        return compileClassName(pattern, 0);
    }

    /**
     * 将包含通配符的类名, 编译成正则表达式.
     */
    public static Pattern compileClassName(String pattern, int options) throws PatternSyntaxException {
        return Pattern.compile(classNameToRegex(pattern, options), options);
    }

    /**
     * 取得相关度数值。
     * <p>
     * 所谓相关度数值，即除去分隔符和通配符以后，剩下的字符长度。
     * 相关度数值可用来对匹配结果排序。例如：a.b.c既匹配a又匹配*，但显然前者为更“相关”的匹配。
     * </p>
     */
    public static int getClassNameRelevancy(String pattern) {
        pattern = normalizeClassName(pattern);

        if (pattern == null) {
            return 0;
        }

        int relevant = 0;

        for (int i = 0; i < pattern.length(); i++) {
            switch (pattern.charAt(i)) {
                case DOT:
                case STAR:
                case QUESTION:
                    continue;

                default:
                    relevant++;
            }
        }

        return relevant;
    }

    /**
     * 将包含通配符的类名, 编译成正则表达式.
     */
    public static String classNameToRegex(String pattern, int options) throws PatternSyntaxException {
        pattern = assertNotNull(normalizeClassName(pattern), "pattern");

        int lastToken = LAST_TOKEN_START;
        StringBuilder buffer = new StringBuilder(pattern.length() * 2);

        boolean matchPrefix = (options & MATCH_PREFIX) != 0;

        if (matchPrefix) {
            buffer.append(REGEX_MATCH_PREFIX);
        }

        for (int i = 0; i < pattern.length(); i++) {
            char ch = pattern.charAt(i);

            switch (ch) {
                case DOT:
                    // dot后面不能是dot, dot不能作为字符串的开始
                    if (lastToken == LAST_TOKEN_DOT || lastToken == LAST_TOKEN_START) {
                        throw new PatternSyntaxException("Syntax Error", pattern, i);
                    }

                    // 因为**已经包括了dot, 所以不需要额外地匹配dot
                    if (lastToken != LAST_TOKEN_DOUBLE_STAR) {
                        buffer.append(REGEX_DOT_NO_DUP);
                    }

                    lastToken = LAST_TOKEN_DOT;
                    break;

                case STAR:
                    int j = i + 1;

                    if (j < pattern.length() && pattern.charAt(j) == STAR) {
                        i = j;

                        // **前面只能是dot
                        if (lastToken != LAST_TOKEN_START && lastToken != LAST_TOKEN_DOT) {
                            throw new PatternSyntaxException("Syntax Error", pattern, i);
                        }

                        lastToken = LAST_TOKEN_DOUBLE_STAR;
                        buffer.append(REGEX_CLASS_NAME_FULL);
                    } else {
                        // *前面不能是*或**
                        if (lastToken == LAST_TOKEN_STAR || lastToken == LAST_TOKEN_DOUBLE_STAR) {
                            throw new PatternSyntaxException("Syntax Error", pattern, i);
                        }

                        lastToken = LAST_TOKEN_STAR;
                        buffer.append(REGEX_CLASS_NAME);
                    }

                    break;

                case QUESTION:
                    lastToken = LAST_TOKEN_QUESTION;
                    buffer.append(REGEX_CLASS_NAME_SINGLE_CHAR);
                    break;

                default:
                    // **后只能是dot
                    if (lastToken == LAST_TOKEN_DOUBLE_STAR) {
                        throw new PatternSyntaxException("Syntax Error", pattern, i);
                    }

                    if (Character.isLetterOrDigit(ch) || ch == UNDERSCORE) {
                        // 加上word边界, 进行整字匹配
                        if (lastToken == LAST_TOKEN_START) {
                            buffer.append(REGEX_WORD_BOUNDARY).append(ch); // 前边界
                        } else if (i + 1 == pattern.length()) {
                            buffer.append(ch).append(REGEX_WORD_BOUNDARY); // 后边界
                        } else {
                            buffer.append(ch);
                        }
                    } else if (ch == DOLLAR) {
                        buffer.append(ESCAPE_CHAR).append(DOLLAR);
                    } else {
                        throw new PatternSyntaxException("Syntax Error", pattern, i);
                    }

                    lastToken = LAST_TOKEN_CLASS_NAME;
            }
        }

        return buffer.toString();
    }

    /**
     * 规格化类名。
     * <ul>
     * <li>除去两端空白</li>
     * <li>将"/"和"\\"转换成"."</li>
     * <li>将重复的"."转换成单个的"."</li>
     * <li>除去首尾的"."</li>
     * </ul>
     */
    public static String normalizeClassName(String name) {
        if (name == null) {
            return null;
        }

        name = name.trim();
        name = name.replaceAll("[/\\\\\\.]+", ".");
        name = name.replaceAll("^\\.|\\.$", EMPTY_STRING);

        return name;
    }

    /**
     * 将类名转化成路径名。
     * <ul>
     * <li>规格化类名</li>
     * <li>将"."转换成"/"</li>
     * </ul>
     */
    public static String classNameToPathName(String name) {
        name = normalizeClassName(name);

        if (name == null) {
            return null;
        }

        name = name.replace('.', '/');

        return name;
    }
}
