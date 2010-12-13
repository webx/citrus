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
import static com.alibaba.citrus.util.StringUtil.*;

/**
 * 代表一个替换。替换字符串中的变量。通常变量以<code>'$'</code>开始，例如： <code>$1</code>，<code>$2</code>
 * 等，但<code>Substitution</code>类可支持对多种不同前缀的变量进行替换。
 * 
 * @author Michael Zhou
 */
public abstract class Substitution {
    protected final String replacementPrefixes;

    /**
     * 创建一个替换，以<code>'$'</code>为变量前缀。
     */
    public Substitution() {
        this("$");
    }

    /**
     * 创建一个替换，以指定字符为变量前缀。
     */
    public Substitution(String replacementPrefixes) {
        this.replacementPrefixes = assertNotNull(trimToNull(replacementPrefixes), "replacementPrefixes");
    }

    /**
     * 替换字符串中的变量。
     */
    public final String substitute(String input) {
        if (input == null) {
            return null;
        }

        StringBuilder buf = new StringBuilder();

        substitute(buf, input);

        return buf.toString();
    }

    /**
     * 替换字符串中的变量。
     */
    public final void substitute(StringBuilder buf, String input) {
        int length = input.length();
        int index;

        for (int i = 0; i < length;) {
            char ch = input.charAt(i);

            if (ch == '\\') {
                i++;

                if (i < length) {
                    buf.append(input.charAt(i++));
                } else {
                    buf.append(ch);
                }
            } else if ((index = replacementPrefixes.indexOf(ch)) >= 0) {
                i++;

                int num = -1;
                int numStartIndex = i; // 保存index

                while (i < length) {
                    int digit = input.charAt(i) - '0';

                    if (digit < 0 || digit > 9) {
                        break;
                    }

                    i++;

                    if (num == -1) {
                        num = digit;
                    } else {
                        num = num * 10 + digit;
                    }
                }

                String groupValue;

                if (num == -1) { // not a number
                    buf.append(ch);
                } else if ((groupValue = group(index, num)) != null) {
                    buf.append(groupValue);
                } else { // out of range
                    buf.append(ch);
                    buf.append(input, numStartIndex, i);
                }
            } else {
                buf.append(ch);
                i++;
            }
        }
    }

    /**
     * 子类覆盖此方法，以提供指定类型、指定group序号的的replacement结果。
     */
    protected abstract String group(int index, int groupNumber);
}
