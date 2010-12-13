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

import static com.alibaba.citrus.turbine.TurbineConstant.*;
import static com.alibaba.citrus.turbine.util.TurbineUtil.*;
import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.BasicConstant.*;
import static com.alibaba.citrus.util.ObjectUtil.*;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.citrus.service.mappingrule.MappingRuleService;
import com.alibaba.citrus.service.pipeline.PipelineContext;
import com.alibaba.citrus.service.pipeline.support.AbstractValve;
import com.alibaba.citrus.service.pipeline.support.AbstractValveDefinitionParser;
import com.alibaba.citrus.service.requestcontext.buffered.BufferedRequestContext;
import com.alibaba.citrus.service.template.TemplateException;
import com.alibaba.citrus.service.template.TemplateService;
import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.TurbineRunData;
import com.alibaba.citrus.turbine.TurbineRunDataInternal;
import com.alibaba.citrus.turbine.support.ContextAdapter;

/**
 * 渲染模板。
 * 
 * @author dux.fangl
 * @author Michael Zhou
 */
public class RenderTemplateValve extends AbstractValve {
    @Autowired
    private BufferedRequestContext bufferedRequestContext;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private MappingRuleService mappingRuleService;

    public void invoke(PipelineContext pipelineContext) throws Exception {
        TurbineRunDataInternal rundata = (TurbineRunDataInternal) getTurbineRunData(request);
        String target = assertNotNull(rundata.getTarget(), "Target was not specified");

        // 检查重定向标志，如果是重定向，则不需要将页面输出。
        if (!rundata.isRedirected()) {
            Context context = rundata.getContext();

            renderTemplate(getScreenTemplate(target), context, rundata);

            // layout可被禁用。
            if (rundata.isLayoutEnabled()) {
                String layoutTemplate = getLayoutTemplate(target);

                if (templateService.exists(layoutTemplate)) {
                    String screenContent = defaultIfNull(bufferedRequestContext.popCharBuffer(), EMPTY_STRING);
                    context.put(SCREEN_PLACEHOLDER_KEY, screenContent);

                    renderTemplate(layoutTemplate, context, rundata);
                }
            }
        }

        pipelineContext.invokeNext();
    }

    protected String getScreenTemplate(String target) {
        return mappingRuleService.getMappedName(SCREEN_TEMPLATE, target);
    }

    protected String getLayoutTemplate(String target) {
        return mappingRuleService.getMappedName(LAYOUT_TEMPLATE, target);
    }

    protected void renderTemplate(String templateName, Context context, TurbineRunData rundata)
            throws TemplateException, IOException {
        templateService.writeTo(templateName, new ContextAdapter(context), rundata.getResponse().getWriter());
    }

    public static class DefinitionParser extends AbstractValveDefinitionParser<RenderTemplateValve> {
    }
}
