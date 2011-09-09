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

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.citrus.service.moduleloader.ModuleLoaderException;
import com.alibaba.citrus.service.moduleloader.ModuleLoaderService;
import com.alibaba.citrus.service.pipeline.PipelineContext;
import com.alibaba.citrus.service.pipeline.PipelineException;
import com.alibaba.citrus.service.pipeline.support.AbstractValve;
import com.alibaba.citrus.service.pipeline.support.AbstractValveDefinitionParser;
import com.alibaba.citrus.turbine.TurbineRunData;
import com.alibaba.citrus.util.StringUtil;

/**
 * 执行action module，通常用来处理用户提交的表单。
 * 
 * @author Michael Zhou
 */
public class PerformActionValve extends AbstractValve {
    @Autowired
    private HttpServletRequest request;

    @Autowired
    private ModuleLoaderService moduleLoaderService;

    public void invoke(PipelineContext pipelineContext) throws Exception {
        TurbineRunData rundata = getTurbineRunData(request);

        // 检查重定向标志，如果是重定向，则不需要将页面输出。
        if (!rundata.isRedirected()) {
            String action = rundata.getAction();

            // 如果找到action，则执行之。
            if (!StringUtil.isEmpty(action)) {
                String actionKey = "_action_" + action;

                // 防止重复执行同一个action。
                if (rundata.getRequest().getAttribute(actionKey) == null) {
                    rundata.getRequest().setAttribute(actionKey, "executed");

                    try {
                        moduleLoaderService.getModule(ACTION_MODULE, action).execute();
                    } catch (ModuleLoaderException e) {
                        throw new PipelineException("Could not load action module: " + action, e);
                    } catch (Exception e) {
                        throw new PipelineException("Failed to execute action module", e);
                    }
                }
            }
        }

        pipelineContext.invokeNext();
    }

    public static class DefinitionParser extends AbstractValveDefinitionParser<PerformActionValve> {
    }
}
