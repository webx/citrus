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
package com.alibaba.citrus.turbine.util;

import static com.alibaba.citrus.util.CollectionUtil.*;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.citrus.service.pull.ToolFactory;
import com.alibaba.citrus.springext.support.parser.AbstractSingleBeanDefinitionParser;
import com.alibaba.citrus.util.StringUtil;

/**
 * Template context tool that can be used to set various attributes of a HTML
 * page. This tool does not automatically make the changes in the HTML page for
 * you. You must use this tool in your layout template to retrieve the
 * attributes.
 * <p>
 * The set/add methods are can be used from a screen template, action, screen
 * class, layour template, or anywhere else. The get methods should be used in
 * your layout template(s) to construct the appropriate HTML tags.
 * </p>
 * <p>
 * Example usage of this tool to build the HEAD and BODY tags in your layout
 * templates:
 * </p>
 * <p>
 * <code>## Set defaults for all pages using this layout.  Anything set here can<br>
 * ## be overridden in the screen template.<br>
 * $page.setTitle("My default page title");<br>
 * $page.setHttpEquiv("Content-Style-Type","text/css")<br>
 * $page.addStyleSheet($content.getURI("myStyleSheet.css"))<br>
 * $page.addScript($content.getURI("globalJavascriptCode.js"))<br>
 * <br>
 * ## build the HTML, HEAD, and BODY tags dynamically<br>
 * &lt;html&gt;<br>
 * &lt;head&gt;<br>
 * #if( $page.Title != "" )<br>
 * &lt;title&gt;$page.Title&lt;/title&gt;<br>
 * #end<br>
 * #foreach($metaTag in $page.MetaTags.keySet())<br>
 * &lt;meta name="$metaTag" content="$page.MetaTags.get($metaTag)"&gt;<br>
 * #end<br>
 * #foreach($httpEquiv in $page.HttpEquivs.keySet())<br>
 * &lt;meta http-equiv="$httpEquiv" content="$page.HttpEquivs.get($httpEquiv)"&gt;<br>
 * #end<br>
 * #foreach( $styleSheet in $page.StyleSheets )<br>
 * &lt;link rel="stylesheet" href="$styleSheet.Url"<br>
 * #if($styleSheet.Type != "" ) type="$styleSheet.Type" #end<br>
 * #if($styleSheet.Media != "") media="$styleSheet.Media" #end<br>
 * #if($styleSheet.Title != "") title="$styleSheet.Title" #end<br>
 * &gt;<br>
 * #end<br>
 * #foreach( $script in $page.Scripts )<br>
 * &lt;script type="text/javascript" src="$script" language="JavaScript"&gt;&lt;/script&gt;<br>
 * #end<br>
 * &lt;/head&gt;<br>
 * <br>
 * ## Construct the body tag.  Iterate through the body attributes to build the opening tag<br>
 * &lt;body<br>
 * #foreach( $attributeName in $page.BodyAttributes.keySet() )<br>
 * $attributeName = "$page.BodyAttributes.get($attributeName)"<br>
 * #end<br>
 * &gt; </code>
 * </p>
 * <p>
 * Example usages of this tool in your screen templates:<br>
 * <code>$page.addScript($content.getURI("myJavascript.js")<br>
 * $page.setTitle("My page title")<br>
 * $page.setHttpEquiv("refresh","5; URL=http://localhost/nextpage.html")</code>
 * </p>
 * 
 * @author <a href="mailto:quintonm@bellsouth.net">Quinton McCombs</a>
 * @author <a href="mailto:seade@backstagetech.com.au">Scott Eade</a>
 * @version $Id: HtmlPageAttributeTool.java 705 2004-03-17 14:29:38Z baobao $
 */
public class HtmlPageAttributeTool {
    private static final Logger log = LoggerFactory.getLogger(HtmlPageAttributeTool.class);

    /** The title */
    private String title;

    /** Body Attributes */
    private Map<String, String> bodyAttributes = createHashMap();

    /** Script references */
    private List<String> scripts = createLinkedList();

    /** Stylesheet references */
    private List<StyleSheet> styleSheets = createLinkedList();

    /** Inline styles */
    private List<String> styles = createLinkedList();

    /** Meta tags for the HEAD */
    private Map<String, String> metaTags = createHashMap();

    /** http-equiv tags */
    private Map<String, String> httpEquivs = createHashMap();

    /**
     * Set the title in the page. This returns an empty String so that the
     * template doesn't complain about getting a null return value. Subsequent
     * calls to this method will replace the current title.
     * 
     * @param title A String with the title.
     * @return a <code>HtmlPageAttributes</code> (self).
     */
    public HtmlPageAttributeTool setTitle(String title) {
        this.title = title;
        return this;
    }

    /**
     * Get the title in the page. This returns an empty String if empty so that
     * the template doesn't complain about getting a null return value.
     * 
     * @return A String with the title.
     */
    public String getTitle() {
        if (StringUtil.isEmpty(this.title)) {
            return "";
        }

        return title;
    }

    /**
     * Adds an attribute to the BODY tag.
     * 
     * @param name A String.
     * @param value A String.
     * @return a <code>HtmlPageAttributes</code> (self).
     * @deprecated Use addBodyAttribute instead.
     */
    @Deprecated
    public HtmlPageAttributeTool addAttribute(String name, String value) {
        log.info("Use of the addAttribute(name,value) method is deprecated.  Please use "
                + "addBodyAttribute(name,value) instead.");
        return addBodyAttribute(name, value);
    }

    /**
     * Adds an attribute to the BODY tag.
     * 
     * @param name A String.
     * @param value A String.
     * @return a <code>HtmlPageAttributes</code> (self).
     */
    public HtmlPageAttributeTool addBodyAttribute(String name, String value) {
        this.bodyAttributes.put(name, value);
        return this;
    }

    /**
     * Returns the map of body attributes
     * 
     * @return the map
     */
    public Map<String, String> getBodyAttributes() {
        return this.bodyAttributes;
    }

    /**
     * Adds a script reference
     * 
     * @param scriptURL
     * @return a <code>HtmlPageAttributes</code> (self).
     */
    public HtmlPageAttributeTool addScript(String scriptURL) {
        this.scripts.add(scriptURL);
        return this;
    }

    /**
     * Adds a script reference
     * 
     * @param scriptURL
     * @return a <code>HtmlPageAttributes</code> (self).
     * @deprecated Use addScript instead
     */
    @Deprecated
    public HtmlPageAttributeTool setScript(String scriptURL) {
        log.info("Use of the setScript(scriptURL) method is deprecated.  Please use " + "addScript(scriptURL) instead.");
        return addScript(scriptURL);
    }

    /**
     * Returns a collection of script URLs
     * 
     * @return list of String objects constainings URLs of javascript files to
     *         include
     */
    public List<String> getScripts() {
        return this.scripts;
    }

    /**
     * Adds a style sheet reference
     * 
     * @param styleSheetURL URL of the style sheet
     * @return a <code>HtmlPageAttributes</code> (self).
     */
    public HtmlPageAttributeTool addStyleSheet(String styleSheetURL) {
        addStyleSheet(styleSheetURL, "screen", null, "text/css");
        return this;
    }

    /**
     * Adds a style sheet reference
     * 
     * @param styleSheetURL URL of the style sheet
     * @param media name of the media
     * @param title title of the stylesheet
     * @param type content type
     * @return a <code>HtmlPageAttributes</code> (self).
     */
    public HtmlPageAttributeTool addStyleSheet(String styleSheetURL, String media, String title, String type) {
        StyleSheet ss = new StyleSheet(styleSheetURL);

        ss.setMedia(media);
        ss.setTitle(title);
        ss.setType(type);
        this.styleSheets.add(ss);
        return this;
    }

    /**
     * Adds a style sheet reference
     * 
     * @param styleSheetURL
     * @return a <code>HtmlPageAttributes</code> (self).
     * @deprecated use addStyleSheet instead
     */
    @Deprecated
    public HtmlPageAttributeTool setStyleSheet(String styleSheetURL) {
        log.info("Use of the setStyleSheet(styleSheetURL) method is deprecated.  Please use "
                + "addStyleSheet(styleSheetURL) instead.");
        return addStyleSheet(styleSheetURL);
    }

    /**
     * Adds a style sheet reference
     * 
     * @param styleSheetURL
     * @param media name of the media
     * @return a <code>HtmlPageAttributes</code> (self).
     * @deprecated use addStyleSheet instead
     */
    @Deprecated
    public HtmlPageAttributeTool setStyleSheet(String styleSheetURL, String media) {
        log.info("Use of the setStyleSheet(styleSheetURL,media) method is deprecated.  "
                + "Please use addStyleSheet(styleSheetURL,media) instead.");
        return addStyleSheet(styleSheetURL, media, null, "text/css");
    }

    /**
     * Returns a collection of script URLs
     * 
     * @return list StyleSheet objects (inner class)
     */
    public List<StyleSheet> getStyleSheets() {
        return this.styleSheets;
    }

    /**
     * Adds a STYLE element to the HEAD of the page with the provided content.
     * 
     * @param styleText The contents of the <code>style</code> tag.
     * @return a <code>HtmlPageAttributes</code> (self).
     * @deprecated use addStyle instead
     */
    @Deprecated
    public HtmlPageAttributeTool setStyle(String styleText) {
        log.info("Use of the setStyle(styleText) method is deprecated.  Please use " + "addStyle(styleText) instead.");
        return addStyle(styleText);
    }

    /**
     * Adds a STYLE element to the HEAD of the page with the provided content.
     * 
     * @param styleText The contents of the <code>style</code> tag.
     * @return a <code>HtmlPageAttributes</code> (self).
     */
    public HtmlPageAttributeTool addStyle(String styleText) {
        this.styles.add(styleText);
        return this;
    }

    /**
     * Returns a collection of styles
     * 
     * @return list of String objects containing the contents of style tags
     */
    public List<String> getStyles() {
        return this.styles;
    }

    /**
     * Set a keywords META tag in the HEAD of the page.
     * 
     * @param keywords A String.
     * @return a <code>HtmlPageAttributes</code> (self).
     */
    public HtmlPageAttributeTool setKeywords(String keywords) {
        this.metaTags.put("keywords", keywords);
        return this;
    }

    /**
     * Sets a HttpEquiv META tag in the HEAD of the page, usage: <br>
     * <code>setHttpEquiv("refresh", "5; URL=http://localhost/nextpage.html")</code>
     * <br>
     * <code>setHttpEquiv("Expires", "Tue, 20 Aug 1996 14:25:27 GMT")</code>
     * 
     * @param httpEquiv The value to use for the http-equiv attribute.
     * @param content The text for the content attribute of the meta tag.
     * @return a <code>HtmlPageAttributes</code> (self).
     */
    public HtmlPageAttributeTool setHttpEquiv(String httpEquiv, String content) {
        this.httpEquivs.put(httpEquiv, content);
        return this;
    }

    /**
     * Add a description META tag to the HEAD of the page.
     * 
     * @param description A String.
     * @return a <code>HtmlPageAttributes</code> (self).
     */
    public HtmlPageAttributeTool setDescription(String description) {
        this.metaTags.put("description", description);
        return this;
    }

    /**
     * Set the background image for the BODY tag.
     * 
     * @param url A String.
     * @return a <code>HtmlPageAttributes</code> (self).
     */
    public HtmlPageAttributeTool setBackground(String url) {
        this.bodyAttributes.put("backgroup", url);
        return this;
    }

    /**
     * Set the background color for the BODY tag. You can use either color names
     * or color values (e.g. "white" or "#ffffff" or "ffffff").
     * 
     * @param color A String.
     * @return a <code>HtmlPageAttributes</code> (self).
     */
    public HtmlPageAttributeTool setBgColor(String color) {
        this.bodyAttributes.put("BGCOLOR", color);
        return this;
    }

    /**
     * Set the text color for the BODY tag. You can use either color names or
     * color values (e.g. "white" or "#ffffff" or "ffffff").
     * 
     * @param color A String.
     * @return a <code>HtmlPageAttributes</code> (self).
     */
    public HtmlPageAttributeTool setTextColor(String color) {
        this.bodyAttributes.put("TEXT", color);
        return this;
    }

    /**
     * Set the link color for the BODY tag. You can use either color names or
     * color values (e.g. "white" or "#ffffff" or "ffffff").
     * 
     * @param color A String.
     * @return a <code>HtmlPageAttributes</code> (self).
     */
    public HtmlPageAttributeTool setLinkColor(String color) {
        this.bodyAttributes.put("LINK", color);
        return this;
    }

    /**
     * Set the visited link color for the BODY tag.
     * 
     * @param color A String.
     * @return a <code>HtmlPageAttributes</code> (self).
     */
    public HtmlPageAttributeTool setVlinkColor(String color) {
        this.bodyAttributes.put("VLINK", color);
        return this;
    }

    /**
     * Set the active link color for the BODY tag.
     * 
     * @param color A String.
     * @return a <code>HtmlPageAttributes</code> (self).
     */
    public HtmlPageAttributeTool setAlinkColor(String color) {
        this.bodyAttributes.put("ALINK", color);
        return this;
    }

    /**
     * Gets the map of http equiv tags
     * 
     * @return Map of http equiv names to the contents
     */
    public Map<String, String> getHttpEquivs() {
        return this.httpEquivs;
    }

    /**
     * Gets the map of meta tags
     * 
     * @return Map of http equiv names to the contents
     */
    public Map<String, String> getMetaTags() {
        return this.metaTags;
    }

    /**
     * A dummy toString method that returns an empty string.
     * 
     * @return An empty String ("").
     */
    @Override
    public String toString() {
        return "";
    }

    /**
     * Helper class to hold data about a stylesheet
     */
    public class StyleSheet {
        private String url;
        private String title;
        private String media;
        private String type;

        /**
         * Constructor requiring the URL to be set
         * 
         * @param url URL of the external style sheet
         */
        public StyleSheet(String url) {
            setUrl(url);
        }

        /**
         * Gets the content type of the style sheet
         * 
         * @return content type
         */
        public String getType() {
            return StringUtil.isEmpty(type) ? "" : type;
        }

        /**
         * Sets the content type of the style sheet
         * 
         * @param type content type
         */
        public void setType(String type) {
            this.type = type;
        }

        /**
         * @return String representation of the URL
         */
        public String getUrl() {
            return url;
        }

        /**
         * Sets the URL of the external style sheet
         * 
         * @param url The URL of the stylesheet
         */
        private void setUrl(String url) {
            this.url = url;
        }

        /**
         * Gets the title of the style sheet
         * 
         * @return title
         */
        public String getTitle() {
            return StringUtil.isEmpty(title) ? "" : title;
        }

        /**
         * Sets the title of the stylesheet
         * 
         * @param title
         */
        public void setTitle(String title) {
            this.title = title;
        }

        /**
         * Gets the media for which the stylesheet should be applied.
         * 
         * @return name of the media
         */
        public String getMedia() {
            return StringUtil.isEmpty(media) ? "" : media;
        }

        /**
         * Sets the media for which the stylesheet should be applied.
         * 
         * @param media name of the media
         */
        public void setMedia(String media) {
            this.media = media;
        }
    }

    public static class DefinitionParser extends AbstractSingleBeanDefinitionParser<Factory> {
    }

    public static class Factory implements ToolFactory {
        public boolean isSingleton() {
            return false;
        }

        public Object createTool() throws Exception {
            return new HtmlPageAttributeTool();
        }
    }
}
