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
package com.alibaba.citrus.service.jsp;

import com.alibaba.citrus.service.template.TemplateEngine;
import com.alibaba.citrus.service.template.TemplateNotFoundException;

/**
 * Jsp模板引擎。
 * <p>
 * 注意，Jsp模板引擎没有完整地实现<code>TemplateEngine</code>
 * 接口的约定。因为Jsp无法输出到指定的输出流，也没有办法取得结果字符串。Jsp只能输出到response中。
 * 因此，Jsp模板引擎只能用于生成WEB页面，而无法用于通用的文本生成。
 * </p>
 * 
 * @author Michael Zhou
 */
public interface JspEngine extends TemplateEngine {
    /**
     * 取得相对于servletContext的模板路径。这个路径可被
     * <code>javax.servlet.RequestDispatcher</code> 使用，以便找到jsp的实例。
     */
    String getPathWithinServletContext(String templateName) throws TemplateNotFoundException;
}
