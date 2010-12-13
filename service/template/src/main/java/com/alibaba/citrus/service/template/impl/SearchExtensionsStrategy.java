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
package com.alibaba.citrus.service.template.impl;

import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.CollectionUtil.*;

import java.util.List;

/**
 * 假如当前模板名后缀无法找到模板，试着以其它后缀寻找。
 * <p>
 * 例如：模板<code>test.vm</code>不存在，那么就尝试一下<code>test.jsp</code>、
 * <code>test.ftl</code>等。
 * </p>
 * 
 * @author Michael Zhou
 */
public class SearchExtensionsStrategy implements TemplateSearchingStrategy {
    private final String[] availableExtensions;

    public SearchExtensionsStrategy(String[] extensions) {
        this.availableExtensions = assertNotNull(extensions, "extensions");
    }

    public Object getKey(String templateName) {
        return null;
    }

    public boolean findTemplate(TemplateMatcher matcher) {
        List<String> testedExtensions = createArrayList(availableExtensions.length);
        boolean found = false;
        String ext = matcher.getExtension();

        if (ext != null) {
            testedExtensions.add(ext);
            found = matcher.findTemplate();
        }

        for (int i = 0; !found && i < availableExtensions.length; i++) {
            ext = availableExtensions[i];

            if (!testedExtensions.contains(ext)) {
                testedExtensions.add(ext);
                matcher.setExtension(ext);
                found = matcher.findTemplate();
            }
        }

        return found;
    }
}
