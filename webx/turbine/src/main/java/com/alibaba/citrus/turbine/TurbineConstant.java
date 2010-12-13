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
package com.alibaba.citrus.turbine;

/**
 * 经典的webx方案的常量。 添加了一些citrus用到的常量
 * 
 * @author Michael Zhou
 * @author dux.fangl
 */
public interface TurbineConstant {
    /* Turbine Scheme 模块和模板类型。 */

    /** 模块类型：action，处理用户提交内容的模块。 */
    String ACTION_MODULE = "action";

    /** 模块类型：screen，代表页面的主体。 */
    String SCREEN_MODULE = "screen";

    /** 模块类型：screen，代表页面的主体。 */
    String SCREEN_MODULE_NO_TEMPLATE = "screen.notemplate";

    /** 模块类型：control，代表页面的可重用片段。 */
    String CONTROL_MODULE = "control";

    /** 模块类型：control，代表页面的可重用片段。 */
    String CONTROL_MODULE_NO_TEMPLATE = "control.notemplate";

    /** 模板类型：screen，代表页面的主体。 */
    String SCREEN_TEMPLATE = "screen.template";

    /** 模板类型：control，代表页面的可重用片段。 */
    String CONTROL_TEMPLATE = "control.template";

    /** 模板类型：layout，代表页面的布局。 */
    String LAYOUT_TEMPLATE = "layout.template";

    /** URL后缀转换：输入 */
    String EXTENSION_INPUT = "extension.input";

    /** URL后缀转换：输出 */
    String EXTENSION_OUTPUT = "extension.output";

    /* Template context相关常量。 */

    /** 在rundata attribute和template context中代表screen的内容的key。 */
    String SCREEN_PLACEHOLDER_KEY = "screen_placeholder";

    /* HTML Template相关的常量(HtmlPageAttributeTool)。 */

    /** Default doctype root element. */
    String DEFAULT_HTML_DOCTYPE_ROOT_ELEMENT_KEY = "default.html.doctype.root.element";

    /** Default value for the doctype root element */
    String DEFAULT_HTML_DOCTYPE_ROOT_ELEMENT_DEFAULT = "HTML";

    /** Default doctype dtd. */
    String DEFAULT_HTML_DOCTYPE_IDENTIFIER_KEY = "default.html.doctype.identifier";

    /** Default Doctype dtd value */
    String DEFAULT_HTML_DOCTYPE_IDENTIFIER_DEFAULT = "-//W3C//DTD HTML 4.01 Transitional//EN";

    /** Default doctype url. */
    String DEFAULT_HTML_DOCTYPE_URI_KEY = "default.html.doctype.url";

    /** Default doctype url value. */
    String DEFAULT_HTML_DOCTYPE_URI_DEFAULT = "http://www.w3.org/TR/1999/REC-html401-19991224/loose.dtd";
}
