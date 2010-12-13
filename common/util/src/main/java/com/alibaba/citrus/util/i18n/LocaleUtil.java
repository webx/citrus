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
package com.alibaba.citrus.util.i18n;

import static com.alibaba.citrus.util.BasicConstant.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.EventListener;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.citrus.util.ClassLoaderUtil;
import com.alibaba.citrus.util.StringUtil;
import com.alibaba.citrus.util.io.StreamUtil;

/**
 * 用来处理地域和字符编码的工具类。
 * <p>
 * 由于系统locale和charset是不可靠的，不同的环境可能会有不同的系统设置，因此应用程序最好不要依赖这个系统值。
 * <code>LocaleUtil</code>提供了一个方案，可以“修改”默认locale和charset。
 * </p>
 * <p>
 * <code>LocaleUtil</code>提供了以下几个作用域的locale/charset设定：
 * </p>
 * <ul>
 * <li>系统作用域：由JVM所运行的操作系统环境决定，在JVM生命期内不改变。可通过<code>LocaleUtil.getSystem()</code>
 * 取得。</li>
 * <li>默认作用域：在整个JVM中全局有效，可被改变。可通过<code>LocaleUtil.getDefault()</code>
 * 取得。如未明确指定，则取“系统作用域”的值。</li>
 * <li>线程作用域：在整个线程中全局有效，可被改变。可通过<code>LocaleUtil.getContext()</code>
 * 取得。如未明确指定，则取“默认作用域”的值。每个线程都可以有自己的locale和charset设置，不会干扰其它线程。</li>
 * </ul>
 * <p>
 * Util工具箱里的其它工具类，当需要时，将从<code>LocaleUtil.getContext()</code>
 * 中取得当前的locale和charset设置。例如：<code>StringEscapeUtil.escapeURL(value)</code>
 * ，如不指定charset
 * ，将从context中取得charset。这样，框架往往可以修改context值，而所有线程中的方法调用将服从于框架的locale和charset设定。
 * </p>
 * 
 * @author Michael Zhou
 */
public class LocaleUtil {
    private static final LocaleInfo systemLocaleInfo = new LocaleInfo();
    private static LocaleInfo defaultLocalInfo = systemLocaleInfo;
    private static final ThreadLocal<LocaleInfo> contextLocaleInfoHolder = new ThreadLocal<LocaleInfo>();

    /**
     * 判断locale是否被支持。
     * 
     * @param locale 要检查的locale
     */
    public static boolean isLocaleSupported(Locale locale) {
        return locale != null && AvailableLocalesLoader.locales.AVAILABLE_LANGUAGES.contains(locale.getLanguage())
                && AvailableLocalesLoader.locales.AVAILABLE_COUNTRIES.contains(locale.getCountry());
    }

    /**
     * 判断指定的charset是否被支持。
     * 
     * @param charset 要检查的charset
     */
    public static boolean isCharsetSupported(String charset) {
        return Charset.isSupported(charset);
    }

    /**
     * 解析locale字符串。
     * <p>
     * Locale字符串是符合下列格式：<code>language_country_variant</code>。
     * </p>
     * 
     * @param localeString 要解析的字符串
     * @return <code>Locale</code>对象，如果locale字符串为空，则返回<code>null</code>
     */
    public static Locale parseLocale(String localeString) {
        localeString = trimToNull(localeString);

        if (localeString == null) {
            return null;
        }

        String language = EMPTY_STRING;
        String country = EMPTY_STRING;
        String variant = EMPTY_STRING;

        // language
        int start = 0;
        int index = localeString.indexOf("_");

        if (index >= 0) {
            language = localeString.substring(start, index).trim();

            // country
            start = index + 1;
            index = localeString.indexOf("_", start);

            if (index >= 0) {
                country = localeString.substring(start, index).trim();

                // variant
                variant = localeString.substring(index + 1).trim();
            } else {
                country = localeString.substring(start).trim();
            }
        } else {
            language = localeString.substring(start).trim();
        }

        return new Locale(language, country, variant);
    }

    /**
     * 取得正规的字符集名称, 如果指定字符集不存在, 则抛出<code>UnsupportedEncodingException</code>.
     * 
     * @param charset 字符集名称
     * @return 正规的字符集名称
     * @throws IllegalCharsetNameException 如果指定字符集名称非法
     * @throws UnsupportedCharsetException 如果指定字符集不存在
     */
    public static String getCanonicalCharset(String charset) {
        return Charset.forName(charset).name();
    }

    /**
     * 取得备选的resource bundle风格的名称列表。
     * <p>
     * 例如：
     * <code>calculateBundleNames("hello.jsp", new Locale("zh", "CN", "variant"))</code>
     * 将返回下面列表：
     * <ol>
     * <li>hello_zh_CN_variant.jsp</li>
     * <li>hello_zh_CN.jsp</li>
     * <li>hello_zh.jsp</li>
     * <li>hello.jsp</li>
     * </ol>
     * </p>
     * 
     * @param baseName bundle的基本名
     * @param locale 区域设置
     * @return 所有备选的bundle名
     */
    public static List<String> calculateBundleNames(String baseName, Locale locale) {
        return calculateBundleNames(baseName, locale, false);
    }

    /**
     * 取得备选的resource bundle风格的名称列表。
     * <p>
     * 例如：
     * <code>calculateBundleNames("hello.jsp", new Locale("zh", "CN", "variant"),
     * false)</code>将返回下面列表：
     * <ol>
     * <li>hello_zh_CN_variant.jsp</li>
     * <li>hello_zh_CN.jsp</li>
     * <li>hello_zh.jsp</li>
     * <li>hello.jsp</li>
     * </ol>
     * </p>
     * <p>
     * 当<code>noext</code>为<code>true</code>时，不计算后缀名，例如
     * <code>calculateBundleNames("hello.world",
     * new Locale("zh", "CN", "variant"), true)</code>将返回下面列表：
     * <ol>
     * <li>hello.world_zh_CN_variant</li>
     * <li>hello.world_zh_CN</li>
     * <li>hello.world_zh</li>
     * <li>hello.world</li>
     * </ol>
     * </p>
     * 
     * @param baseName bundle的基本名
     * @param locale 区域设置
     * @return 所有备选的bundle名
     */
    public static List<String> calculateBundleNames(String baseName, Locale locale, boolean noext) {
        baseName = StringUtil.trimToEmpty(baseName);

        if (locale == null) {
            locale = new Locale(EMPTY_STRING);
        }

        // 取后缀。
        String ext = EMPTY_STRING;
        int extLength = 0;

        if (!noext) {
            int extIndex = baseName.lastIndexOf(".");

            if (extIndex != -1) {
                ext = baseName.substring(extIndex, baseName.length());
                extLength = ext.length();
                baseName = baseName.substring(0, extIndex);

                if (extLength == 1) {
                    ext = EMPTY_STRING;
                    extLength = 0;
                }
            }
        }

        // 计算locale后缀。
        LinkedList<String> result = createLinkedList();
        String language = locale.getLanguage();
        int languageLength = language.length();
        String country = locale.getCountry();
        int countryLength = country.length();
        String variant = locale.getVariant();
        int variantLength = variant.length();

        StringBuilder buffer = new StringBuilder(baseName);

        buffer.append(ext);
        result.addFirst(buffer.toString());
        buffer.setLength(buffer.length() - extLength);

        // 如果locale是("", "", "").
        if (languageLength + countryLength + variantLength == 0) {
            return result;
        }

        // 加入baseName_language，如果baseName为空，则不加下划线。
        if (buffer.length() > 0) {
            buffer.append('_');
        }

        buffer.append(language);

        if (languageLength > 0) {
            buffer.append(ext);
            result.addFirst(buffer.toString());
            buffer.setLength(buffer.length() - extLength);
        }

        if (countryLength + variantLength == 0) {
            return result;
        }

        // 加入baseName_language_country
        buffer.append('_').append(country);

        if (countryLength > 0) {
            buffer.append(ext);
            result.addFirst(buffer.toString());
            buffer.setLength(buffer.length() - extLength);
        }

        if (variantLength == 0) {
            return result;
        }

        // 加入baseName_language_country_variant
        buffer.append('_').append(variant);

        buffer.append(ext);
        result.addFirst(buffer.toString());
        buffer.setLength(buffer.length() - extLength);

        return result;
    }

    /**
     * 取得操作系统默认的区域。
     * 
     * @return 操作系统默认的区域
     */
    public static LocaleInfo getSystem() {
        return systemLocaleInfo;
    }

    /**
     * 取得默认的区域。
     * 
     * @return 默认的区域
     */
    public static LocaleInfo getDefault() {
        return defaultLocalInfo == null ? systemLocaleInfo : defaultLocalInfo;
    }

    /**
     * 设置默认的区域。
     * 
     * @param locale 区域
     * @return 原来的默认区域
     */
    public static LocaleInfo setDefault(Locale locale) {
        LocaleInfo old = getDefault();
        setDefaultAndNotify(new LocaleInfo(locale, null, systemLocaleInfo));
        return old;
    }

    /**
     * 设置默认的区域。
     * 
     * @param locale 区域
     * @param charset 编码字符集
     * @return 原来的默认区域
     */
    public static LocaleInfo setDefault(Locale locale, String charset) {
        LocaleInfo old = getDefault();
        setDefaultAndNotify(new LocaleInfo(locale, charset, systemLocaleInfo));
        return old;
    }

    /**
     * 设置默认的区域。
     * 
     * @param localeInfo 区域和编码字符集信息
     * @return 原来的默认区域
     */
    public static LocaleInfo setDefault(LocaleInfo localeInfo) {
        if (localeInfo == null) {
            return setDefault(null, null);
        } else {
            LocaleInfo old = getDefault();
            setDefaultAndNotify(localeInfo);
            return old;
        }
    }

    private static void setDefaultAndNotify(LocaleInfo localeInfo) {
        defaultLocalInfo = localeInfo;

        for (Notifier notifier : notifiers) {
            notifier.defaultChanged(localeInfo);
        }
    }

    /**
     * 复位默认的区域设置。
     */
    public static void resetDefault() {
        defaultLocalInfo = systemLocaleInfo;

        for (Notifier notifier : notifiers) {
            notifier.defaultReset();
        }
    }

    /**
     * 取得当前thread默认的区域。
     * 
     * @return 当前thread默认的区域
     */
    public static LocaleInfo getContext() {
        LocaleInfo contextLocaleInfo = contextLocaleInfoHolder.get();
        return contextLocaleInfo == null ? getDefault() : contextLocaleInfo;
    }

    /**
     * 设置当前thread默认的区域。
     * 
     * @param locale 区域
     * @return 原来的thread默认的区域
     */
    public static LocaleInfo setContext(Locale locale) {
        LocaleInfo old = getContext();
        setContextAndNotify(new LocaleInfo(locale, null, defaultLocalInfo));
        return old;
    }

    /**
     * 设置当前thread默认的区域。
     * 
     * @param locale 区域
     * @param charset 编码字符集
     * @return 原来的thread默认的区域
     */
    public static LocaleInfo setContext(Locale locale, String charset) {
        LocaleInfo old = getContext();
        setContextAndNotify(new LocaleInfo(locale, charset, defaultLocalInfo));
        return old;
    }

    /**
     * 设置当前thread默认的区域。
     * 
     * @param localeInfo 区域和编码字符集信息
     * @return 原来的thread默认的区域
     */
    public static LocaleInfo setContext(LocaleInfo localeInfo) {
        if (localeInfo == null) {
            return setContext(null, null);
        } else {
            LocaleInfo old = getContext();
            setContextAndNotify(localeInfo);
            return old;
        }
    }

    private static void setContextAndNotify(LocaleInfo localeInfo) {
        contextLocaleInfoHolder.set(localeInfo);

        for (Notifier notifier : notifiers) {
            notifier.contextChanged(localeInfo);
        }
    }

    /**
     * 复位当前thread的区域设置。
     */
    public static void resetContext() {
        contextLocaleInfoHolder.remove();

        for (Notifier notifier : notifiers) {
            notifier.contextReset();
        }
    }

    private static Logger log = LoggerFactory.getLogger(LocaleUtil.class);
    private static Notifier[] notifiers = getNotifiers();

    private static Notifier[] getNotifiers() {
        try {
            URL[] files = ClassLoaderUtil.getResources("META-INF/services/localeNotifiers", ClassLoaderUtil.class);
            List<Notifier> list = createLinkedList();

            for (URL file : files) {
                for (String className : StringUtil
                        .split(StreamUtil.readText(file.openStream(), "UTF-8", true), "\r\n ")) {
                    list.add(Notifier.class.cast(ClassLoaderUtil.newInstance(className, ClassLoaderUtil.class)));
                }
            }

            return list.toArray(new Notifier[list.size()]);
        } catch (Exception e) {
            log.warn("Failure in LocaleUtil.getNotifiers()", e);
            return new Notifier[0];
        }
    }

    /**
     * 当default或context locale被改变时，通知监听器。
     */
    public interface Notifier extends EventListener {
        void defaultChanged(LocaleInfo newValue);

        void defaultReset();

        void contextChanged(LocaleInfo newValue);

        void contextReset();
    }

    /**
     * 延迟加载所有可用的国家和语言。
     */
    private static class AvailableLocalesLoader {
        private static final AvailableLocales locales = new AvailableLocales();
    }

    private static class AvailableLocales {
        private final Set<String> AVAILABLE_LANGUAGES = createHashSet();
        private final Set<String> AVAILABLE_COUNTRIES = createHashSet();

        private AvailableLocales() {
            Locale[] availableLocales = Locale.getAvailableLocales();

            for (Locale locale : availableLocales) {
                AVAILABLE_LANGUAGES.add(locale.getLanguage());
                AVAILABLE_COUNTRIES.add(locale.getCountry());
            }
        }
    }
}
