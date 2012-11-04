/*
 * Copyright (c) 2002-2012 Alibaba Group Holding Limited.
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
 */

package com.alibaba.citrus.turbine.pipeline.valve;

import static com.alibaba.citrus.springext.util.SpringExtUtil.*;
import static com.alibaba.citrus.turbine.TurbineConstant.*;
import static com.alibaba.citrus.turbine.util.TurbineUtil.*;
import static com.alibaba.citrus.util.Assert.*;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.citrus.service.mappingrule.MappingRuleService;
import com.alibaba.citrus.service.moduleloader.Module;
import com.alibaba.citrus.service.moduleloader.ModuleEvent;
import com.alibaba.citrus.service.moduleloader.ModuleLoaderException;
import com.alibaba.citrus.service.moduleloader.ModuleLoaderService;
import com.alibaba.citrus.service.moduleloader.ModuleNotFoundException;
import com.alibaba.citrus.service.moduleloader.ModuleReturningValue;
import com.alibaba.citrus.service.pipeline.PipelineContext;
import com.alibaba.citrus.service.pipeline.support.AbstractValveDefinitionParser;
import com.alibaba.citrus.turbine.TurbineRunData;
import com.alibaba.citrus.util.StringUtil;
import com.alibaba.citrus.util.internal.ScreenEventUtil;
import com.alibaba.citrus.webx.WebxException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * 执行screen，如果有返回值的话，将返回的对象放到pipeline context中。
 *
 * @author Michael Zhou
 */
public class PerformScreenValve extends AbstractInOutValve {
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

            Object result = null;

            try {
                result = performScreenModule(rundata);
            } finally {
                setOutputValue(pipelineContext, result);
            }
        }

        pipelineContext.invokeNext();
    }

    /** 设置content type。 */
    protected void setContentType(TurbineRunData rundata) {
        // 设置content type，不需要设置charset，因为SetLocaleRequestContext已经设置了charset。
        // 避免覆盖别人设置的contentType。
        if (StringUtil.isEmpty(rundata.getResponse().getContentType())) {
            rundata.getResponse().setContentType("text/html");
        }
    }

    /** 执行screen模块。 */
    protected Object performScreenModule(TurbineRunData rundata) {
        ModuleFinder finder = new ModuleFinder(rundata.getTarget());

        // 如果设置了template，则默认打开layout
        rundata.setLayoutEnabled(true);

        try {
            Module module = finder.getScreenModule();

            // 当指定了templateName时，可以没有的screen module，而单单渲染模板。
            // 这样就实现了page-driven，即先写模板，必要时再写一个module class与之对应。
            if (module != null) {
                // 将event传入screen。
                ScreenEventUtil.setEventName(rundata.getRequest(), finder.event);

                try {

                    if (module instanceof ModuleReturningValue) {
                        return ((ModuleReturningValue) module).executeAndReturn();
                    } else {
                        module.execute();
                    }
                } finally {
                    ScreenEventUtil.setEventName(rundata.getRequest(), null);
                }
            } else {
                if (isScreenModuleRequired()) {
                    throw new ModuleNotFoundException("Could not find screen module: " + finder.moduleName);
                }
            }
        } catch (ModuleLoaderException e) {
            throw new WebxException("Failed to load screen module: " + finder.moduleName, e);
        } catch (Exception e) {
            throw new WebxException("Failed to execute screen: " + finder.moduleName, e);
        }

        return null;
    }

    private class ModuleFinder {
        private final String target;
        private       String moduleName;
        private       String eventModuleName;
        private       String event;

        public ModuleFinder(String target) {
            this.target = assertNotNull(target, "Target was not specified");
        }

        public Module getScreenModule() throws ModuleLoaderException {
            // 从target中取得screen module名称
            moduleName = getModuleName(target);

            Module module = moduleLoaderService.getModuleQuiet(SCREEN_MODULE, moduleName);

            if (module != null) {
                return module;
            }

            if (parseEvent()) {
                module = moduleLoaderService.getModuleQuiet(SCREEN_MODULE, eventModuleName);

                if (module instanceof ModuleEvent) {
                    return module;
                }
            }

            return null;
        }

        /** 尝试将target解释成moduleName/eventName。如果target=/xxx/yyy.ext，则moduleName=/Xxx，eventName=yyy。 */
        private boolean parseEvent() {
            int slashIndex = target.lastIndexOf("/");
            int dotIndex = target.lastIndexOf(".");

            if (slashIndex > 0) {
                event = target.substring(slashIndex + 1, dotIndex > slashIndex ? dotIndex : target.length());
                eventModuleName = getModuleName(target.substring(0, slashIndex));

                return true;
            }

            return false;
        }
    }

    /** 如果返回<code>true</code>，那么当模块找不到时，会抛异常。子类可以覆盖此方法，以改变行为。 */
    protected boolean isScreenModuleRequired() {
        return true;
    }

    /** 根据target取得screen模块名。子类可以修改映射规则。 */
    protected String getModuleName(String target) {
        return mappingRuleService.getMappedName(SCREEN_MODULE_NO_TEMPLATE, target);
    }

    public static class DefinitionParser extends AbstractValveDefinitionParser<PerformScreenValve> {
        @Override
        protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
            attributesToProperties(element, builder, "out");
        }
    }
}
