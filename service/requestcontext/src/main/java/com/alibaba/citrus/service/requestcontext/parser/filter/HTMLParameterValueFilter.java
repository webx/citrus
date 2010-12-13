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
package com.alibaba.citrus.service.requestcontext.parser.filter;

import static com.alibaba.citrus.util.BasicConstant.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.ObjectUtil.*;

import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.citrus.service.requestcontext.parser.ParameterValueFilter;
import com.alibaba.citrus.springext.support.BeanSupport;

/**
 * ¹ýÂË²ÎÊý¡£
 * 
 * @author Michael Zhou
 */
public class HTMLParameterValueFilter extends BeanSupport implements ParameterValueFilter {
    private HTMLInputFilter filter;
    private Map<String, Set<String>> allowed;
    private String[] deniedTags;
    private String[] selfClosingTags;
    private String[] needClosingTags;
    private String[] allowedProtocols;
    private String[] protocolAtts;
    private String[] removeBlanks;
    private String[] allowedEntities;

    @Override
    protected void init() {
        allowed = createHashMap(); // allowed tags and attrs
        deniedTags = defaultIfNull(deniedTags, EMPTY_STRING_ARRAY);
        selfClosingTags = defaultIfNull(selfClosingTags, EMPTY_STRING_ARRAY);
        needClosingTags = defaultIfNull(needClosingTags, EMPTY_STRING_ARRAY);
        allowedProtocols = defaultIfNull(allowedProtocols, EMPTY_STRING_ARRAY);
        protocolAtts = defaultIfNull(protocolAtts, EMPTY_STRING_ARRAY);
        removeBlanks = defaultIfNull(removeBlanks, EMPTY_STRING_ARRAY);
        allowedEntities = defaultIfNull(allowedEntities, EMPTY_STRING_ARRAY);

        filter = new HTMLInputFilter(allowed, deniedTags, selfClosingTags, needClosingTags, allowedProtocols,
                protocolAtts, removeBlanks, allowedEntities);
    }

    public boolean isFiltering(HttpServletRequest request) {
        return true;
    }

    public String filter(String key, String value, boolean isHtml) {
        assertInitialized();

        if (value == null) {
            return null;
        }

        return filter.filter(value, isHtml);
    }
}
