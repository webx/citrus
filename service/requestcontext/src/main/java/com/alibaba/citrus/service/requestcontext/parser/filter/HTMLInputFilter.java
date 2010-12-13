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
package com.alibaba.citrus.service.requestcontext.parser.filter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.citrus.util.StringUtil;

/**
 * HTML filtering utility for protecting against XSS (Cross Site Scripting).
 * <p>
 * This code is licensed under a Creative Commons Attribution-ShareAlike 2.5
 * License http://creativecommons.org/licenses/by-sa/2.5/
 * </p>
 * <p>
 * This code is a Java port of the original work in PHP by Cal Hendersen.
 * http://code.iamcal.com/php/lib_filter/
 * </p>
 * <p>
 * The trickiest part of the translation was handling the differences in regex
 * handling between PHP and Java. These resources were helpful in the process:
 * </p>
 * <ul>
 * <li>http://java.sun.com/j2se/1.4.2/docs/api/java/util/regex/Pattern.html
 * <li>http://us2.php.net/manual/en/reference.pcre.pattern.modifiers.php
 * <li>http://www.regular-expressions.info/modifiers.html
 * </ul>
 * <p>
 * A note on naming conventions: instance variables are prefixed with a "v";
 * global constants are in all caps.
 * </p>
 * 
 * <pre>
 * Sample use:
 * String input = ...
 * String clean = new HTMLInputFilter().filter( input );
 * </pre>
 * <p>
 * If you find bugs or have suggestions on improvement (especially regarding
 * perfomance), please contact me at the email below. The latest version of this
 * source can be found at
 * </p>
 * <ul>
 * <li>http://josephoconnell.com/java/xss-html-filter/
 * </ul>
 * <p>
 * 做了如下修改：
 * </p>
 * <ul>
 * <li>效率改进：顺序搜索变为搜索Set。
 * <li>效率改进：预编译正则表达式patterns。
 * <li>效率改进：避免了同步操作。
 * <li>修改了构造函数，配合service进行配置。
 * <li>修正了输入中包含$时出错的bug。
 * </ul>
 * 
 * @author Joseph O'Connell <joe.oconnell at gmail dot com>
 * @author Michael Zhou
 * @version 1.0
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class HTMLInputFilter {
    /**
     * flag determining whether to try to make tags when presented with
     * "unbalanced" angle brackets (e.g. "<b text </b>" becomes "<b> text
     * </b>"). If set to false, unbalanced angle brackets will be html escaped.
     */
    protected static final boolean ALWAYS_MAKE_TAGS = true;

    /**
     * flag determing whether comments are allowed in input String.
     */
    protected static final boolean STRIP_COMMENTS = false;

    /** regex flag union representing /si modifiers in php * */
    protected static final int REGEX_FLAGS_SI = Pattern.CASE_INSENSITIVE | Pattern.DOTALL;

    /**
     * set of allowed html elements, along with allowed attributes for each
     * element *
     */
    protected final Map vAllowed; // <String,
    // Set<String>>

    /** set of denied html elements * */
    protected final Set vDeniedTags; // <String>

    /** html elements which must always be self-closing (e.g. "<img />") * */
    protected final Set vSelfClosingTags;

    /**
     * html elements which must always have separate opening and closing tags
     * (e.g. "<b></b>") *
     */
    protected final Set vNeedClosingTags;

    /** attributes which should be checked for valid protocols * */
    protected final Set vProtocolAtts;

    /** allowed protocols * */
    protected final Set vAllowedProtocols;

    /**
     * tags which should be removed if they contain no content (e.g. "<b></b>"
     * or "<b />") *
     */
    protected final Set vRemoveBlanks;

    /** entities allowed within html markup * */
    protected final Set vAllowedEntities;

    protected final static Logger log = LoggerFactory.getLogger(HTMLInputFilter.class);

    public HTMLInputFilter() {
        this(null, null, null, null, null, null, null, null);
    }

    public HTMLInputFilter(Map allowed, String[] deniedTags, String[] selfClosingTags, String[] needClosingTags,
                           String[] allowedProtocols, String[] protocolAtts, String[] removeBlanks,
                           String[] allowedEntities) {
        vAllowed = allowed == null ? new HashMap() : allowed;

        Set a_atts = getOrCreateSet(vAllowed, "a"); // <String>
        a_atts.add("href");
        a_atts.add("target");

        Set img_atts = getOrCreateSet(vAllowed, "img"); // <String>
        img_atts.add("src");
        img_atts.add("width");
        img_atts.add("height");
        img_atts.add("alt");

        getOrCreateSet(vAllowed, "b"); // no_attrs
        getOrCreateSet(vAllowed, "strong"); // no_attrs
        getOrCreateSet(vAllowed, "i"); // no_attrs
        getOrCreateSet(vAllowed, "em"); // no_attrs

        vDeniedTags = mergeList(deniedTags, new String[] { "script" });
        vSelfClosingTags = mergeList(selfClosingTags, new String[] { "img" });
        vNeedClosingTags = mergeList(needClosingTags, new String[] { "a", "b", "strong", "i", "em" });
        vAllowedProtocols = mergeList(allowedProtocols, new String[] { "http", "mailto" });
        vProtocolAtts = mergeList(protocolAtts, new String[] { "src", "href" });
        vRemoveBlanks = mergeList(removeBlanks, new String[] { "a", "b", "strong", "i", "em" });
        vAllowedEntities = mergeList(allowedEntities, new String[] { "amp", "gt", "lt", "quot" });
    }

    private Set getOrCreateSet(Map map, String key) {
        Set set = (Set) map.get(key);

        if (set == null) {
            set = new HashSet();
            map.put(key, set);
        }

        return set;
    }

    private Set mergeList(String[] list, String[] defaultList) {
        Set result = new HashSet();

        if (list != null) {
            for (String element : list) {
                result.add(element);
            }
        }

        if (defaultList != null) {
            for (String element : defaultList) {
                result.add(element);
            }
        }

        return result;
    }

    // ---------------------------------------------------------------
    // my versions of some PHP library functions

    public static String chr(int decimal) {
        return String.valueOf((char) decimal);
    }

    public static String htmlSpecialChars(String s) {
        s = s.replaceAll("&", "&amp;");
        s = s.replaceAll("\"", "&quot;");
        s = s.replaceAll("<", "&lt;");
        s = s.replaceAll(">", "&gt;");
        return s;
    }

    // ---------------------------------------------------------------

    /**
     * given a user submitted input String, filter out any invalid or restricted
     * html.
     * 
     * @param input text (i.e. submitted by a user) than may contain html
     * @return "clean" version of input, with only valid, whitelisted html
     *         elements allowed
     */
    public String filter(String input) {
        return filter(input, true);
    }

    public String filter(String input, boolean isHtml) {
        return new FilterRunner(this).filter(input, isHtml);
    }
}

@SuppressWarnings({ "unchecked", "rawtypes" })
final class FilterRunner {
    private final HTMLInputFilter filter;

    /** counts of open tags for each (allowable) html element * */
    private final Map vTagCounts = new HashMap(); // <String, Integer>

    public FilterRunner(HTMLInputFilter filter) {
        this.filter = filter;
    }

    public String filter(String input, boolean isHtml) {
        boolean debugEnabled = HTMLInputFilter.log.isDebugEnabled();
        String s = input;

        if (debugEnabled) {
            HTMLInputFilter.log.debug("************************************************");
            if (isHtml) {
                HTMLInputFilter.log.debug("         HTML INPUT: " + input);
            } else {
                HTMLInputFilter.log.debug("     ORDINARY INPUT: " + input);
            }
        }

        if (isHtml) {
            s = escapeComments(s);
            if (debugEnabled) {
                HTMLInputFilter.log.debug("     escapeComments: " + s);
            }
        }

        if (isHtml) {
            s = balanceHTML(s);
            if (debugEnabled) {
                HTMLInputFilter.log.debug("        balanceHTML: " + s);
            }
        }

        s = checkTags(s, isHtml);
        if (debugEnabled) {
            HTMLInputFilter.log.debug("          checkTags: " + s);
        }

        if (isHtml) {
            s = processRemoveBlanks(s);
            if (debugEnabled) {
                HTMLInputFilter.log.debug("processRemoveBlanks: " + s);
            }
        }

        if (isHtml) {
            s = validateEntities(s);
            if (debugEnabled) {
                HTMLInputFilter.log.debug("    validateEntites: " + s);
            }
        }

        if (debugEnabled) {
            HTMLInputFilter.log.debug("************************************************\n\n");
        }

        return s;
    }

    private final static Pattern escapeCommentsPattern = Pattern.compile("<!--(.*?)-->", Pattern.DOTALL);

    protected String escapeComments(String s) {
        Matcher m = escapeCommentsPattern.matcher(s);
        StringBuffer buf = new StringBuffer();
        if (m.find()) {
            String match = m.group(1); // (.*?)
            appendReplacement(m, buf, "<!--" + HTMLInputFilter.htmlSpecialChars(match) + "-->");
        }
        m.appendTail(buf);

        return buf.toString();
    }

    protected String balanceHTML(String s) {
        if (HTMLInputFilter.ALWAYS_MAKE_TAGS) {
            //
            // try and form html
            //
            s = regexReplace("^>", "", s);
            s = regexReplace("<([^>]*?)(?=<|$)", "<$1>", s);
            s = regexReplace("(^|>)([^<]*?)(?=>)", "$1<$2", s);

        } else {
            //
            // escape stray brackets
            //
            s = regexReplace("<([^>]*?)(?=<|$)", "&lt;$1", s);
            s = regexReplace("(^|>)([^<]*?)(?=>)", "$1$2&gt;<", s);

            //
            // the last regexp causes '<>' entities to appear
            // (we need to do a lookahead assertion so that the last bracket can
            // be used in the next pass of the regexp)
            //
            s = s.replaceAll("<>", "");
        }

        return s;
    }

    private final static Pattern checkTagsPattern = Pattern.compile("<(.*?)>", Pattern.DOTALL);

    protected String checkTags(String s, boolean isHtml) {
        Matcher m = checkTagsPattern.matcher(s);

        StringBuffer buf = new StringBuffer();
        while (m.find()) {
            String replaceStr = m.group(1);
            replaceStr = processTag(replaceStr, isHtml);
            appendReplacement(m, buf, replaceStr);
        }
        m.appendTail(buf);

        s = buf.toString();

        if (isHtml) {
            // these get tallied in processTag
            // (remember to reset before subsequent calls to filter method)
            for (Iterator i = vTagCounts.keySet().iterator(); i.hasNext();) {
                String key = (String) i.next();
                for (int ii = 0; ii < ((Integer) vTagCounts.get(key)).intValue(); ii++) {
                    s += "</" + key + ">";
                }
            }
        }

        return s;
    }

    private final static Pattern processTagPatternEnding = Pattern.compile("^/([a-z0-9]+)",
            HTMLInputFilter.REGEX_FLAGS_SI);
    private final static Pattern processTagPatternStarting = Pattern.compile("^([a-z0-9]+)(.*?)(/?)$",
            HTMLInputFilter.REGEX_FLAGS_SI);
    private final static Pattern processTagPatternComment = Pattern.compile("^!--(.*)--$",
            HTMLInputFilter.REGEX_FLAGS_SI);

    protected String processTag(String s, boolean isHtml) {
        // ending tags
        Matcher m = processTagPatternEnding.matcher(s);
        if (m.find()) {
            String name = m.group(1).toLowerCase();
            if (!isHtml || filter.vAllowed.containsKey(name)) {
                if (!filter.vSelfClosingTags.contains(name)) {
                    if (vTagCounts.containsKey(name)) {
                        vTagCounts.put(name, new Integer(((Integer) vTagCounts.get(name)).intValue() - 1));
                        return "</" + name + ">";
                    }
                }
            }

            return "";
        }

        // starting tags
        m = processTagPatternStarting.matcher(s);
        if (m.find()) {
            String name = m.group(1).toLowerCase();
            String body = m.group(2);
            String ending = m.group(3);

            // 删除被拒绝的tag
            if (filter.vDeniedTags.contains(name)) {
                return "";
            }

            // log.debug( "in a starting tag, name='" + name + "'; body='" + body + "'; ending='" + ending + "'" );
            if (filter.vAllowed.containsKey(name)) {
                String params = "";

                Pattern p2 = Pattern.compile("([a-z0-9]+)=([\"'])(.*?)\\2", HTMLInputFilter.REGEX_FLAGS_SI);
                Pattern p3 = Pattern.compile("([a-z0-9]+)(=)([^\"\\s']+)", HTMLInputFilter.REGEX_FLAGS_SI);
                Matcher m2 = p2.matcher(body);
                Matcher m3 = p3.matcher(body);
                List paramNames = new ArrayList(); // <String>
                List paramValues = new ArrayList(); // <String>
                while (m2.find()) {
                    paramNames.add(m2.group(1)); // ([a-z0-9]+)
                    paramValues.add(m2.group(3)); // (.*?)
                }
                while (m3.find()) {
                    paramNames.add(m3.group(1)); // ([a-z0-9]+)
                    paramValues.add(m3.group(3)); // ([^\"\\s']+)
                }

                String paramName, paramValue;
                for (int ii = 0; ii < paramNames.size(); ii++) {
                    paramName = ((String) paramNames.get(ii)).toLowerCase();
                    paramValue = (String) paramValues.get(ii);

                    // log.debug( "paramName='" + paramName + "'" );
                    // log.debug( "paramValue='" + paramValue + "'" );
                    // log.debug( "allowed? " + vAllowed.get( name ).contains( paramName ) );

                    if (((Set) filter.vAllowed.get(name)).contains(paramName)) {
                        if (filter.vProtocolAtts.contains(paramName)) {
                            paramValue = processParamProtocol(paramValue);
                        }
                        params += " " + paramName + "=\"" + paramValue + "\"";
                    }
                }

                if (filter.vSelfClosingTags.contains(name)) {
                    ending = " /";
                }

                if (filter.vNeedClosingTags.contains(name)) {
                    ending = "";
                }

                if (ending == null || ending.length() < 1) {
                    if (vTagCounts.containsKey(name)) {
                        vTagCounts.put(name, new Integer(((Integer) vTagCounts.get(name)).intValue() + 1));
                    } else {
                        vTagCounts.put(name, new Integer(1));
                    }
                } else {
                    ending = " /";
                }
                return "<" + name + params + ending + ">";
            } else if (isHtml) {
                return "";
            } else {
                if (vTagCounts.containsKey(name)) {
                    vTagCounts.put(name, new Integer(((Integer) vTagCounts.get(name)).intValue() + 1));
                } else {
                    vTagCounts.put(name, new Integer(1));
                }

                if (isHtml && StringUtil.isEmpty(s)) {
                    return "";
                } else {
                    return "<" + s + ">";
                }
            }
        }

        // comments
        m = processTagPatternComment.matcher(s);
        if (m.find()) {
            String comment = m.group();
            if (HTMLInputFilter.STRIP_COMMENTS) {
                return "";
            } else {
                return "<" + comment + ">";
            }
        }

        if (isHtml && StringUtil.isEmpty(s)) {
            return "";
        } else {
            return "<" + s + ">";
        }
    }

    protected String processParamProtocol(String s) {
        s = decodeEntities(s);
        Pattern p = Pattern.compile("^([^:]+):", HTMLInputFilter.REGEX_FLAGS_SI);
        Matcher m = p.matcher(s);
        if (m.find()) {
            String protocol = m.group(1);
            if (!filter.vAllowedProtocols.contains(protocol)) {
                // bad protocol, turn into local anchor link instead
                s = "#" + s.substring(protocol.length() + 1, s.length());
                if (s.startsWith("#//")) {
                    s = "#" + s.substring(3, s.length());
                }
            }
        }

        return s;
    }

    protected String decodeEntities(String s) {
        StringBuffer buf = new StringBuffer();

        Pattern p = Pattern.compile("&#(\\d+);?");
        Matcher m = p.matcher(s);
        while (m.find()) {
            String match = m.group(1);
            int decimal = Integer.decode(match).intValue();
            appendReplacement(m, buf, HTMLInputFilter.chr(decimal));
        }
        m.appendTail(buf);
        s = buf.toString();

        buf = new StringBuffer();
        p = Pattern.compile("&#x([0-9a-f]+);?");
        m = p.matcher(s);
        while (m.find()) {
            String match = m.group(1);
            int decimal = Integer.decode(match).intValue();
            appendReplacement(m, buf, HTMLInputFilter.chr(decimal));
        }
        m.appendTail(buf);
        s = buf.toString();

        buf = new StringBuffer();
        p = Pattern.compile("%([0-9a-f]{2});?");
        m = p.matcher(s);
        while (m.find()) {
            String match = m.group(1);
            int decimal = Integer.decode(match).intValue();
            appendReplacement(m, buf, HTMLInputFilter.chr(decimal));
        }
        m.appendTail(buf);
        s = buf.toString();

        s = validateEntities(s);
        return s;
    }

    protected String processRemoveBlanks(String s) {
        for (Iterator i = filter.vRemoveBlanks.iterator(); i.hasNext();) {
            String tag = (String) i.next();

            s = regexReplace("<" + tag + "(\\s[^>]*)?></" + tag + ">", "", s);
            s = regexReplace("<" + tag + "(\\s[^>]*)?/>", "", s);
        }

        return s;
    }

    protected String validateEntities(String s) {
        // validate entities throughout the string
        Pattern p = Pattern.compile("&([^&;]*)(?=(;|&|$))");
        Matcher m = p.matcher(s);
        StringBuffer buf = new StringBuffer();
        if (m.find()) {
            String one = m.group(1); // ([^&;]*)
            String two = m.group(2); // (?=(;|&|$))
            appendReplacement(m, buf, checkEntity(one, two));
        }
        m.appendTail(buf);
        s = buf.toString();

        // validate quotes outside of tags
        p = Pattern.compile("(>|^)([^<]+?)(<|$)", Pattern.DOTALL);
        m = p.matcher(s);
        buf = new StringBuffer();
        if (m.find()) {
            String one = m.group(1); // (>|^)
            String two = m.group(2); // ([^<]+?)
            String three = m.group(3); // (<|$)
            appendReplacement(m, buf, one + two.replaceAll("\"", "&quot;") + three);
        }
        m.appendTail(buf);

        return s;
    }

    protected String checkEntity(String preamble, String term) {
        if (!term.equals(";")) {
            return "&amp;" + preamble;
        }

        if (isValidEntity(preamble)) {
            return "&" + preamble;
        }

        return "&amp;" + preamble;
    }

    protected boolean isValidEntity(String entity) {
        return filter.vAllowedEntities.contains(entity);
    }

    protected String regexReplace(String regex_pattern, String replacement, String s) {
        Pattern p = Pattern.compile(regex_pattern);
        Matcher m = p.matcher(s);
        return m.replaceAll(replacement);
    }

    private Matcher appendReplacement(Matcher matcher, StringBuffer buf, String replacement) {
        return matcher.appendReplacement(buf, quoteReplacement(replacement));
    }

    /**
     * 从JDK5中<code>Matcher.quoteReplacement()</code>复制过来，确保JDK1.4的兼容性。
     */
    private String quoteReplacement(String s) {
        if (s.indexOf('\\') == -1 && s.indexOf('$') == -1) {
            return s;
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\') {
                sb.append('\\');
                sb.append('\\');
            } else if (c == '$') {
                sb.append('\\');
                sb.append('$');
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
