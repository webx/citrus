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

import static com.alibaba.citrus.springext.util.SpringExtUtil.*;
import static com.alibaba.citrus.util.BasicConstant.*;
import static com.alibaba.citrus.util.FileUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.alibaba.citrus.logconfig.support.SecurityLogger;
import com.alibaba.citrus.service.requestcontext.parser.UploadedFileFilter;
import com.alibaba.citrus.springext.support.BeanSupport;
import com.alibaba.citrus.springext.support.parser.AbstractSingleBeanDefinitionParser;
import com.alibaba.citrus.util.FileUtil;

/**
 * 根据文件名后缀过滤上传文件。
 * 
 * @author Michael Zhou
 */
public class UploadedFileExtensionWhitelist extends BeanSupport implements UploadedFileFilter {
    private final SecurityLogger log = new SecurityLogger();
    private String[] extensions;

    public void setAllowedExtensions(String[] extensions) {
        this.extensions = extensions;
    }

    public void setLogName(String logName) {
        log.setLogName(logName);
    }

    public boolean isFiltering(HttpServletRequest request) {
        return true;
    }

    @Override
    protected void init() throws Exception {
        if (extensions == null) {
            extensions = EMPTY_STRING_ARRAY;
        }

        for (int i = 0; i < extensions.length; i++) {
            extensions[i] = FileUtil.normalizeExtension(extensions[i]);
        }
    }

    public FileItem filter(String key, FileItem file) {
        if (file == null) {
            return null;
        }

        boolean allowed = false;

        // 未指定文件名 - 返回null
        // 文件名没有后缀 - 返回字符串“null”
        // 后缀被规格化为小写字母
        String ext = getExtension(file.getName(), "null", true);

        if (ext != null) {
            for (String allowedExtension : extensions) {
                if (allowedExtension.equals(ext)) {
                    allowed = true;
                    break;
                }
            }
        }

        if (!allowed) {
            log.getLogger().warn("Uploaded file type \"{}\" is denied: {}", ext, file.getName());
            return null;
        } else {
            return file;
        }
    }

    public static class DefinitionParser extends AbstractSingleBeanDefinitionParser<UploadedFileExtensionWhitelist> {
        @Override
        protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
            attributesToProperties(element, builder, "logName");

            String extensions = trimToNull(element.getAttribute("extensions"));

            if (extensions != null) {
                builder.addPropertyValue("allowedExtensions", extensions);
            }
        }
    }
}
