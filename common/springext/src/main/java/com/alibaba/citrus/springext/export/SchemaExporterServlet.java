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
package com.alibaba.citrus.springext.export;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.citrus.util.internal.webpagelite.ServletRequestContext;

/**
 * 调用<code>SchemaExportWEB</code>，显示页面。
 * 
 * @author Michael Zhou
 */
public class SchemaExporterServlet extends HttpServlet {
    private static final long serialVersionUID = -598137064919280947L;
    private SchemaExporterWEB exporter;

    @Override
    public void init() throws ServletException {
        exporter = new SchemaExporterWEB();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        exporter.processRequest(new ServletRequestContext(request, response, getServletContext()));
    }
}
