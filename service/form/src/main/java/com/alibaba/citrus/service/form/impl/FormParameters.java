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

package com.alibaba.citrus.service.form.impl;

import static com.alibaba.citrus.service.requestcontext.util.RequestContextUtil.*;
import static com.alibaba.citrus.util.CollectionUtil.*;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;

import com.alibaba.citrus.service.requestcontext.parser.ParserRequestContext;
import com.alibaba.citrus.util.ToStringBuilder;

final class FormParameters {
    private final Map<String, FormParameter> params = createLinkedHashMap();
    private final HttpServletRequest   request;
    private final ParserRequestContext prc;

    public FormParameters(HttpServletRequest request) {
        this.request = request;
        this.prc = findRequestContext(request, ParserRequestContext.class);
    }

    public void addFormParameter(FormParameter param) {
        params.put(param.normalizedKey, param);
    }

    public FormParameter getFormParameter(String normalizedKey) {
        return params.get(normalizedKey);
    }

    public Object[] getValues(String normalizedKey) {
        FormParameter param = params.get(normalizedKey);

        if (param == null) {
            return null;
        } else if (prc != null) {
            // 假如配置了ParserRequestContext，则取得objects，以便支持FileItem，否则只支持字符串值。
            return prc.getParameters().getObjects(param.originalKey);
        } else {
            return request.getParameterValues(param.originalKey);
        }
    }

    public String getStringValue(String normalizedKey) {
        FormParameter param = params.get(normalizedKey);

        if (param == null) {
            return null;
        } else if (prc != null) {
            // 假如配置了ParserRequestContext，则取得objects，以便支持FileItem，否则只支持字符串值。
            return prc.getParameters().getString(param.originalKey);
        } else {
            return request.getParameter(param.originalKey);
        }
    }

    @Override
    public String toString() {
        return new ToStringBuilder().append("FormParameters").append(params.values()).toString();
    }

    /** 代表一个表单参数的信息。 */
    final static class FormParameter {
        public final String groupKey;
        public final String fieldKey;
        public final String instanceKey;
        public final String additionalInfo;
        public final String originalKey;
        public final String normalizedKey;

        FormParameter(String groupKey, String fieldKey, String instanceKey, String additionalInfo, String originalKey,
                      String normalizedKey) {
            this.groupKey = groupKey;
            this.fieldKey = fieldKey;
            this.instanceKey = instanceKey;
            this.additionalInfo = additionalInfo;
            this.originalKey = originalKey;
            this.normalizedKey = normalizedKey;
        }

        @Override
        public String toString() {
            return normalizedKey;
        }
    }
}
