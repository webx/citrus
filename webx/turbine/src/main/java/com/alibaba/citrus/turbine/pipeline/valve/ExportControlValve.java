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
import static com.alibaba.citrus.turbine.util.TurbineUtil.*;
import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.ObjectUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.alibaba.citrus.service.pipeline.PipelineContext;
import com.alibaba.citrus.service.pipeline.support.AbstractValve;
import com.alibaba.citrus.service.requestcontext.parser.ParameterParser;
import com.alibaba.citrus.springext.support.parser.AbstractSingleBeanDefinitionParser;
import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.TurbineRunData;
import com.alibaba.citrus.turbine.TurbineRunDataInternal;
import com.alibaba.citrus.turbine.util.ControlTool;
import com.alibaba.citrus.util.internal.regex.Substitution;

/**
 * 直接渲染control。
 * 
 * @author Michael Zhou
 */
public class ExportControlValve extends AbstractValve {
    private static final String DEFAULT_CONTROL_TOOL_NAME = "control";
    private static final String DEFAULT_SUBSTITUTION_NAME = "subst";

    @Autowired
    private HttpServletRequest request;

    private String controlToolName;
    private String controlExporterTarget;

    private String substName;
    private String templateName;
    private String moduleName;

    /**
     * 在pull serivce中的<code>ControlTool</code>的名称。
     */
    public void setControlTool(String controlToolName) {
        this.controlToolName = trimToNull(controlToolName);
    }

    /**
     * 用来处理control内容的target。
     */
    public void setControlExporterTarget(String controlExporterTarget) {
        this.controlExporterTarget = trimToNull(controlExporterTarget);
    }

    /**
     * 在<code>PipelineContext</code>中的<code>Substitution</code>的名称，默认为
     * <code>subst</code>。
     * <p>
     * 这个substitution对象通常是由某个condition匹配后放到context中的。
     * </p>
     */
    public void setSubst(String substName) {
        this.substName = trimToNull(substName);
    }

    /**
     * 设置control template名称，可以包含<code>$1</code>、<code>$2</code>这样的替换符。
     */
    public void setTemplate(String templateName) {
        this.templateName = trimToNull(templateName);
    }

    /**
     * 设置control module名称，可以包含<code>$1</code>、<code>$2</code>这样的替换符。
     */
    public void setModule(String moduleName) {
        this.moduleName = trimToNull(moduleName);
    }

    @Override
    protected void init() throws Exception {
        assertNotNull(controlExporterTarget, "no controlExporterTemplate specified");
        assertTrue(templateName != null || moduleName != null, "neither template nor module name was specified");
    }

    public void invoke(PipelineContext pipelineContext) throws Exception {
        TurbineRunDataInternal rundata = (TurbineRunDataInternal) getTurbineRunData(request);
        Substitution subst = getSubstitution(pipelineContext, DEFAULT_SUBSTITUTION_NAME);

        String template = trimToNull(subst.substitute(templateName));
        String module = trimToNull(subst.substitute(moduleName));

        Context context = rundata.getContext();
        ControlTool controlTool = getControlTool(context, template, module, rundata);

        // render control
        rundata.getResponse().getWriter(); // 必须先预备好buffer
        String content = controlTool.render();

        // 设置context，以便后续模块能够读取
        context.put("controlContent", content); // controlContent为control渲染的结果
        context.put("controlTarget", template != null ? template : module);

        // 重定向至controlExporter target
        rundata.setRedirectTarget(controlExporterTarget);

        pipelineContext.invokeNext();
    }

    private Substitution getSubstitution(PipelineContext pipelineContext, String defaultName) {
        String substName = defaultIfNull(this.substName, defaultName);

        return assertNotNull((Substitution) pipelineContext.getAttribute(substName),
                "no Substitution exists in pipelineContext: name=%s", substName);
    }

    private ControlTool getControlTool(Context context, String template, String module, TurbineRunData rundata) {
        String toolName = defaultIfNull(controlToolName, DEFAULT_CONTROL_TOOL_NAME);
        Object tool = context.get(toolName);

        assertTrue(tool instanceof ControlTool, "no control tool: %s", toolName);

        ControlTool controlTool = (ControlTool) tool;

        // setTemplate or setModule
        if (template != null) {
            controlTool.setTemplate(template);
        } else if (module != null) {
            controlTool.setModule(module);
        }

        // setParameter
        ParameterParser params = rundata.getParameters();

        for (String key : params.keySet()) {
            Object[] values = params.getObjects(key);

            switch (values.length) {
                case 0:
                    break;

                case 1:
                    controlTool.setParameter(key, values[0]);
                    break;

                default:
                    controlTool.setParameter(key, values);
                    break;
            }
        }

        return controlTool;
    }

    public static class DefinitionParser extends AbstractSingleBeanDefinitionParser<ExportControlValve> {
        @Override
        protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
            attributesToProperties(element, builder, "controlTool", "controlExporterTarget", "subst", "template",
                    "module");
        }
    }
}
