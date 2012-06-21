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

package com.alibaba.citrus.webx.handler.impl.error;

import com.alibaba.citrus.webx.handler.RequestHandler;
import com.alibaba.citrus.webx.handler.RequestHandlerContext;
import com.alibaba.citrus.webx.util.ErrorHandlerHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 将错误通过sendError转发给servlet engine，由web.xml中定义的错误页面来处理之。
 *
 * @author Michael Zhou
 */
public class SendErrorHandler implements RequestHandler {
    private final static Logger log = LoggerFactory.getLogger(SendErrorHandler.class);

    public void handleRequest(RequestHandlerContext context) throws Exception {
        ErrorHandlerHelper helper = ErrorHandlerHelper.getInstance(context.getRequest());

        helper.setServletErrorAttributes();
        helper.logError(log);

        context.getResponse().sendError(helper.getStatusCode());
    }
}
