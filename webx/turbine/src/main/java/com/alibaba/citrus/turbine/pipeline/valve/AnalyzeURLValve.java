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
package com.alibaba.citrus.turbine.pipeline.valve;

import static com.alibaba.citrus.springext.util.SpringExtUtil.*;
import static com.alibaba.citrus.turbine.TurbineConstant.*;
import static com.alibaba.citrus.turbine.util.TurbineUtil.*;
import static com.alibaba.citrus.util.FileUtil.*;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.alibaba.citrus.service.mappingrule.MappingRuleService;
import com.alibaba.citrus.service.pipeline.PipelineContext;
import com.alibaba.citrus.service.pipeline.support.AbstractValve;
import com.alibaba.citrus.service.pipeline.support.AbstractValveDefinitionParser;
import com.alibaba.citrus.turbine.TurbineRunDataInternal;
import com.alibaba.citrus.util.ServletUtil;
import com.alibaba.citrus.util.StringUtil;
import com.alibaba.citrus.webx.WebxComponent;

/**
 * 根据URL的内容来设置rundata。根据以下规则：
 * <ol>
 * <li>取得servletPath + pathInfo - componentPath作为target。</li>
 * <li>使用MappingRuleService，将target的后缀转换成统一的内部后缀。例如：将jhtml转换成jsp。</li>
 * </ol>
 * 
 * @author Michael Zhou
 */
public class AnalyzeURLValve extends AbstractValve {
    @Autowired
    private HttpServletRequest request;

    @Autowired
    private MappingRuleService mappingRuleService;

    @Autowired
    private WebxComponent component;

    private String homepage;

    public String getHomepage() {
        return homepage;
    }

    public void setHomepage(String homepage) {
        this.homepage = normalizeAbsolutePath(homepage);
    }

    @Override
    protected void init() throws Exception {
        if (homepage == null) {
            setHomepage("/index");
        }
    }

    public void invoke(PipelineContext pipelineContext) throws Exception {
        TurbineRunDataInternal rundata = (TurbineRunDataInternal) getTurbineRunData(request);
        String target = null;

        // 取得target，并转换成统一的内部后缀名。
        String pathInfo = ServletUtil.getResourcePath(rundata.getRequest()).substring(
                component.getComponentPath().length());

        if ("/".equals(pathInfo)) {
            pathInfo = getHomepage();
        }

        // 注意，必须将pathInfo转换成camelCase。
        int lastSlashIndex = pathInfo.lastIndexOf("/");

        if (lastSlashIndex >= 0) {
            pathInfo = pathInfo.substring(0, lastSlashIndex) + "/"
                    + StringUtil.toCamelCase(pathInfo.substring(lastSlashIndex + 1));
        } else {
            pathInfo = StringUtil.toCamelCase(pathInfo);
        }

        target = mappingRuleService.getMappedName(EXTENSION_INPUT, pathInfo);

        rundata.setTarget(target);

        pipelineContext.invokeNext();
    }

    public static class DefinitionParser extends AbstractValveDefinitionParser<AnalyzeURLValve> {
        @Override
        protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
            attributesToProperties(element, builder, "homepage");
        }
    }
}
