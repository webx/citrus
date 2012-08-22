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
import static com.alibaba.citrus.util.StringUtil.*;

import java.io.PrintWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.citrus.service.pipeline.PipelineContext;
import com.alibaba.citrus.service.pipeline.Valve;
import com.alibaba.citrus.service.pipeline.support.AbstractValveDefinitionParser;
import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * 将screen所返回的结果转换成json格式并输出。
 *
 * @author Michael Zhou
 */
public class RenderResultAsJsonValve implements Valve {
    private static final String DEFAULT_RESULT_NAME             = PerformScreenValve.DEFAULT_RESULT_NAME;
    private static final String DEFAULT_CONTENT_TYPE            = "application/json";
    private static final String DEFAULT_JAVASCRIPT_VARIABLE     = null;
    private static final String DEFAULT_JAVASCRIPT_CONTENT_TYPE = "application/javascript";

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private HttpServletResponse response;

    private String resultName;
    private String contentType;
    private String javascriptVariable;
    private String javascriptContentType;

    public String getResultName() {
        return resultName == null ? DEFAULT_RESULT_NAME : resultName;
    }

    public void setResultName(String resultName) {
        this.resultName = trimToNull(resultName);
    }

    public String getContentType() {
        return contentType == null ? DEFAULT_CONTENT_TYPE : contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = trimToNull(contentType);
    }

    public String getJavascriptVariable() {
        return javascriptVariable == null ? DEFAULT_JAVASCRIPT_VARIABLE : javascriptVariable;
    }

    public void setJavascriptVariable(String javascriptVariable) {
        this.javascriptVariable = trimToNull(javascriptVariable);
    }

    public String getJavascriptContentType() {
        return javascriptContentType == null ? DEFAULT_JAVASCRIPT_CONTENT_TYPE : javascriptContentType;
    }

    public void setJavascriptContentType(String javascriptContentType) {
        this.javascriptContentType = trimToNull(javascriptContentType);
    }

    public void invoke(PipelineContext pipelineContext) throws Exception {
        String javascriptVariable = getJavascriptVariable();
        boolean outputAsJson = javascriptVariable == null;

        if (outputAsJson) {
            // output as json
            response.setContentType(getContentType());
        } else {
            // output as javascript
            response.setContentType(getJavascriptContentType());
        }

        PrintWriter out = response.getWriter();
        Object resultObject = pipelineContext.getAttribute(getResultName());
        String jsonResult = JSON.toJSONString(resultObject);

        if (outputAsJson) {
            out.print(jsonResult);
        } else {
            out.print("var ");
            out.print(javascriptVariable);
            out.print(" = ");
            out.print(jsonResult);
            out.print(";");
        }
    }

    public static class DefinitionParser extends AbstractValveDefinitionParser<RenderResultAsJsonValve> {
        @Override
        protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
            attributesToProperties(element, builder, "resultName", "contentType", "javascriptVariable", "javascriptContentType");
        }
    }
}
