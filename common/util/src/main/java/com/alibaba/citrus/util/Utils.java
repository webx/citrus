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

import static com.alibaba.citrus.util.ArrayUtil.*;

import java.util.Collections;
import java.util.Map;

import com.alibaba.citrus.util.i18n.LocaleUtil;
import com.alibaba.citrus.util.internal.StaticFunctionDelegatorBuilder;
import com.alibaba.citrus.util.io.StreamUtil;

/**
 * 集成常用的工具类。
 * 
 * @author Michael Zhou
 */
public class Utils {
    private static final ArrayUtil ARRAY_UTIL = new ArrayUtil();
    private static final ClassLoaderUtil CLASS_LOADER_UTIL = new ClassLoaderUtil();
    private static final ClassUtil CLASS_UTIL = new ClassUtil();
    private static final ExceptionUtil EXCEPTION_UTIL = new ExceptionUtil();
    private static final FileUtil FILE_UTIL = new FileUtil();
    private static final LocaleUtil LOCALE_UTIL = new LocaleUtil();
    private static final MessageUtil MESSAGE_UTIL = new MessageUtil();
    private static final ObjectUtil OBJECT_UTIL = new ObjectUtil();
    private static final StreamUtil STREAM_UTIL = new StreamUtil();
    private static final StringEscapeUtil STRING_ESCAPE_UTIL = new StringEscapeUtil();
    private static final StringUtil STRING_UTIL = new StringUtil();
    private static final SystemUtil SYSTEM_UTIL = new SystemUtil();
    private static final Object MATH_UTIL = createMixin(Math.class);
    private static final Object MIXIN_UTILS = createMixin( //
            ArrayUtil.class, //
            ClassLoaderUtil.class, //
            ClassUtil.class, //
            ExceptionUtil.class, //
            FileUtil.class, //
            LocaleUtil.class, //
            MessageUtil.class, //
            ObjectUtil.class, //
            StreamUtil.class, //
            StringEscapeUtil.class, //
            StringUtil.class, //
            SystemUtil.class, //
            Math.class);

    private static final Map<String, Object> ALL_UTILS = Collections.unmodifiableMap(arrayToMap(new Object[][] { //
            { "arrayUtil", ARRAY_UTIL }, //
                    { "classLoaderUtil", CLASS_LOADER_UTIL }, //
                    { "classUtil", CLASS_UTIL }, //
                    { "exceptionUtil", EXCEPTION_UTIL }, //
                    { "fileUtil", FILE_UTIL }, //
                    { "localeUtil", LOCALE_UTIL }, //
                    { "messageUtil", MESSAGE_UTIL }, //
                    { "objectUtil", OBJECT_UTIL }, //
                    { "streamUtil", STREAM_UTIL }, //
                    { "stringEscapeUtil", STRING_ESCAPE_UTIL }, //
                    { "stringUtil", STRING_UTIL }, //
                    { "systemUtil", SYSTEM_UTIL }, //
                    { "mathUtil", MATH_UTIL }, //
                    { "utils", MIXIN_UTILS } //
            }, String.class, Object.class));

    private static Object createMixin(Class<?>... classes) {
        StaticFunctionDelegatorBuilder builder = new StaticFunctionDelegatorBuilder();

        builder.setClassLoader(Utils.class.getClassLoader());

        for (Class<?> clazz : classes) {
            builder.addClass(clazz);
        }

        return builder.toObject();
    }

    /**
     * 取得包含所有utils的map
     * 
     * @return utils map
     */
    public static Map<String, Object> getUtils() {
        return ALL_UTILS;
    }
}
