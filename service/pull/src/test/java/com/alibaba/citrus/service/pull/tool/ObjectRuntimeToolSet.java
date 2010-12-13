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
package com.alibaba.citrus.service.pull.tool;

import static java.util.Collections.*;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.citrus.service.pull.RuntimeToolSetFactory;
import com.alibaba.citrus.service.pull.ToolFactory;

/**
 * 根据request中的id参数设置tool name。
 * 
 * @author Michael Zhou
 */
public class ObjectRuntimeToolSet extends BaseFactory implements ToolFactory, RuntimeToolSetFactory {
    @Autowired
    private HttpServletRequest request;
    private String idParam = "id";

    public void setIdParam(String idParam) {
        this.idParam = idParam;
    }

    @Override
    public Object createTool() {
        return new Object();
    }

    public Object createToolSet() {
        return new Object();
    }

    public Iterable<String> getToolNames(Object tool) {
        return singletonList(request.getParameter(idParam));
    }

    public Object createTool(Object tool, String name) {
        return new Object();
    }
}
