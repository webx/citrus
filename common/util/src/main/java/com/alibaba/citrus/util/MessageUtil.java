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
package com.alibaba.citrus.util;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * 和<code>ResourceBundle</code>及消息字符串有关的工具类。
 * 
 * @author Michael Zhou
 */
public class MessageUtil {
    /**
     * 从<code>ResourceBundle</code>中取得字符串，并使用<code>MessageFormat</code>格式化字符串.
     * 
     * @param bundle resource bundle
     * @param key 要查找的键
     * @param params 参数表
     * @return key对应的字符串，如果key为<code>null</code>或resource bundle为
     *         <code>null</code>，或resource key未找到，则返回<code>key</code>
     */
    public static String getMessage(ResourceBundle bundle, String key, Object... params) {
        if (bundle == null || key == null) {
            return key;
        }

        try {
            return formatMessage(bundle.getString(key), params);
        } catch (MissingResourceException e) {
            return key;
        }
    }

    /**
     * 使用<code>MessageFormat</code>格式化字符串.
     * 
     * @param message 要格式化的字符串
     * @param params 参数表
     * @return 格式化的字符串，如果message为<code>null</code>，则返回<code>null</code>
     */
    public static String formatMessage(String message, Object... params) {
        if (message == null || params == null || params.length == 0) {
            return message;
        }

        return MessageFormat.format(message, params);
    }
}
