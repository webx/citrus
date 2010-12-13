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

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.citrus.service.mappingrule.MappingRuleService;
import com.alibaba.citrus.service.moduleloader.Module;
import com.alibaba.citrus.service.moduleloader.ModuleLoaderException;
import com.alibaba.citrus.service.moduleloader.ModuleLoaderService;
import com.alibaba.citrus.service.moduleloader.ModuleNotFoundException;
import com.alibaba.citrus.service.pipeline.PipelineContext;
import com.alibaba.citrus.service.pipeline.support.AbstractValve;
import com.alibaba.citrus.service.pipeline.support.AbstractValveDefinitionParser;
import com.alibaba.citrus.turbine.TurbineRunData;
import com.alibaba.citrus.util.StringUtil;
import com.alibaba.citrus.webx.WebxException;

/**
 * 执行screen。
 * 
 * @author Michael Zhou
 */
public class PerformScreenValve extends AbstractValve {
    @Autowired
    private ModuleLoaderService moduleLoaderService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private MappingRuleService mappingRuleService;

    public MappingRuleService getMappingRuleService() {
        return mappingRuleService;
    }

    public void invoke(PipelineContext pipelineContext) throws Exception {
        TurbineRunData rundata = getTurbineRunData(request);

        // 检查重定向标志，如果是重定向，则不需要将页面输出。
        if (!rundata.isRedirected()) {
            setContentType(rundata);
            performScreenModule(rundata);
        }

        pipelineContext.invokeNext();
    }

    /**
     * 设置content type。
     */
    protected void setContentType(TurbineRunData rundata) {
        // 设置content type，不需要设置charset，因为SetLocaleRequestContext已经设置了charset。
        // 避免覆盖别人设置的contentType。
        if (StringUtil.isEmpty(rundata.getResponse().getContentType())) {
            rundata.getResponse().setContentType("text/html");
        }
    }

    /**
     * 执行screen模块。
     */
    protected void performScreenModule(TurbineRunData rundata) {
        String target = assertNotNull(rundata.getTarget(), "Target was not specified");

        // 从target中取得screen module名称
        String moduleName = getModuleName(target);

        // 如果设置了template，则默认打开layout
        rundata.setLayoutEnabled(true);

        try {
            Module module = moduleLoaderService.getModuleQuiet(SCREEN_MODULE, moduleName);

            // 当指定了templateName时，可以没有的screen module，而单单渲染模板。
            // 这样就实现了page-driven，即先写模板，必要时再写一个module class与之对应。
            if (module != null) {
                module.execute();
            } else {
                if (isScreenModuleRequired()) {
                    throw new ModuleNotFoundException("Could not find screen module: " + moduleName);
                }
            }
        } catch (ModuleLoaderException e) {
            throw new WebxException("Failed to load screen module: " + moduleName, e);
        } catch (Exception e) {
            throw new WebxException("Failed to execute screen: " + moduleName, e);
        }
    }

    /**
     * 如果返回<code>true</code>，那么当模块找不到时，会抛异常。子类可以覆盖此方法，以改变行为。
     */
    protected boolean isScreenModuleRequired() {
        return true;
    }

    /**
     * 根据target取得screen模块名。子类可以修改映射规则。
     */
    protected String getModuleName(String target) {
        return mappingRuleService.getMappedName(SCREEN_MODULE_NO_TEMPLATE, target);
    }

    public static class DefinitionParser extends AbstractValveDefinitionParser<PerformScreenValve> {
    }
}
