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

import static com.alibaba.citrus.util.ArrayUtil.*;
import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.BasicConstant.*;
import static com.alibaba.citrus.util.StringUtil.*;

import com.alibaba.citrus.util.internal.ToStringBuilder.CollectionBuilder;

public class Flags {
    private final String[] flags;

    public Flags() {
        this((String[]) null);
    }

    public Flags(String... flags) {
        this.flags = flags == null ? EMPTY_STRING_ARRAY : flags;
    }

    public boolean isEmpty() {
        return isEmptyArray(flags);
    }

    protected String[] getFlags() {
        return flags;
    }

    /**
     * 检查flags，如果存在，则返回<code>true</code>。
     */
    protected boolean hasFlags(String... names) {
        assertTrue(!isEmptyArray(names), "names");

        for (String flag : flags) {
            for (String name : names) {
                if (flag.startsWith(name)) {
                    // flag或F=*
                    if (flag.length() == name.length() || flag.charAt(name.length()) == '=') {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * 如果flag不存在，则返回<code>null</code>。如果flag无值，则返回空字符串。否则返回值。
     */
    protected String getFlagValue(String... names) {
        assertTrue(!isEmptyArray(names), "names");

        for (String flag : flags) {
            for (String name : names) {
                if (flag.startsWith(name)) {
                    if (flag.length() == name.length()) {
                        return "";
                    } else if (flag.charAt(name.length()) == '=') {
                        return trimToEmpty(flag.substring(name.length() + 1));
                    }
                }
            }
        }

        return null;
    }

    @Override
    public String toString() {
        return new CollectionBuilder().setOneLine(true).appendAll(flags).toString();
    }
}
