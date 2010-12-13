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
package com.alibaba.citrus.webx.handler.impl.error;

import com.alibaba.citrus.webx.handler.RequestHandler;
import com.alibaba.citrus.webx.handler.RequestHandlerContext;
import com.alibaba.citrus.webx.util.ErrorHandlerHelper;

/**
 * 将错误通过sendError转发给servlet engine，由web.xml中定义的错误页面来处理之。
 * 
 * @author Michael Zhou
 */
public class SendErrorHandler implements RequestHandler {
    public void handleRequest(RequestHandlerContext context) throws Exception {
        ErrorHandlerHelper helper = ErrorHandlerHelper.getInstance(context.getRequest());

        helper.setServletErrorAttributes();
        context.getResponse().sendError(helper.getStatusCode());
    }
}
