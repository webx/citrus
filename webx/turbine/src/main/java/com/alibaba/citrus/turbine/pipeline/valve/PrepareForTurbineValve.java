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

import static com.alibaba.citrus.turbine.util.TurbineUtil.*;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.citrus.service.pipeline.PipelineContext;
import com.alibaba.citrus.service.pipeline.support.AbstractValve;
import com.alibaba.citrus.service.pipeline.support.AbstractValveDefinitionParser;
import com.alibaba.citrus.turbine.TurbineRunData;
import com.alibaba.citrus.util.Utils;

/**
 * 预备turbine运行所需要的一些内容。
 * 
 * @author Michael Zhou
 */
public class PrepareForTurbineValve extends AbstractValve {
    @Autowired
    private HttpServletRequest request;

    public void invoke(PipelineContext pipelineContext) throws Exception {
        TurbineRunData rundata = getTurbineRunData(request, true);

        try {
            pipelineContext.setAttribute("rundata", rundata);

            for (Map.Entry<String, Object> entry : Utils.getUtils().entrySet()) {
                pipelineContext.setAttribute(entry.getKey(), entry.getValue());
            }

            pipelineContext.invokeNext();
        } finally {
            cleanupTurbineRunData(request);
        }
    }

    public static class DefinitionParser extends AbstractValveDefinitionParser<PrepareForTurbineValve> {
    }
}
