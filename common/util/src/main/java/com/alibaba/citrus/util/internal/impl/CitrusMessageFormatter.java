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
package com.alibaba.citrus.util.internal.impl;

import static com.alibaba.citrus.util.Assert.*;

import com.alibaba.citrus.util.internal.MessageFormatter;

/**
 * Citrus内部专用的<code>MessageFormatter</code>。
 * <p>
 * Message类必须以“
 * <code>com.alibaba.citrus.<strong>xxx.yyy.Zzz</strong>Messages</code>”方式命名。<br>
 * 而相应的resource bundle资源名为：“
 * <code>com/alibaba/citrus/messages/<strong>xxx_yyy_Zzz</strong>.properties</code>
 * ”。
 * </p>
 * 
 * @author Michael Zhou
 */
public class CitrusMessageFormatter<T> extends MessageFormatter<T> {
    final static String PREFIX = "com.alibaba.citrus.";
    final static String SUFFIX = "Messages";
    final static String RB_PACKAGE = "com.alibaba.citrus.messages.";
    private final String bundleName;

    /**
     * 创建一个<code>CitrusMessageFormatter</code>实例。
     */
    public CitrusMessageFormatter() {
        String name = getClass().getName();

        assertTrue(name.startsWith(PREFIX) && name.endsWith(SUFFIX), "Unsupported Message class: ", name);

        this.bundleName = RB_PACKAGE
                + name.substring(PREFIX.length(), name.length() - SUFFIX.length()).replace('.', '_');
    }

    /**
     * 取得<code>ResourceBundle</code>的名称，默认和类名相同。
     */
    @Override
    protected String getBundleName() {
        return bundleName;
    }
}
