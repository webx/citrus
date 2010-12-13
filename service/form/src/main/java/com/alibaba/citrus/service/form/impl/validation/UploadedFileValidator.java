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
package com.alibaba.citrus.service.form.impl.validation;

import static com.alibaba.citrus.util.ArrayUtil.*;
import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.util.List;

import org.apache.commons.fileupload.FileItem;

import com.alibaba.citrus.service.form.support.AbstractValidator;
import com.alibaba.citrus.service.form.support.AbstractValidatorDefinitionParser;
import com.alibaba.citrus.util.FileUtil;
import com.alibaba.citrus.util.HumanReadableSize;

/**
 * 验证上传文件的validator。
 * 
 * @author Michael Zhou
 */
public class UploadedFileValidator extends AbstractValidator {
    private String[] contentTypes;
    private String[] extensions;
    private HumanReadableSize minSize = new HumanReadableSize(-1);
    private HumanReadableSize maxSize = new HumanReadableSize(-1);

    /**
     * 取得上传文件的contentTypes。
     */
    public String[] getContentType() {
        return contentTypes;
    }

    /**
     * 设置上传文件的contentType。
     */
    public void setContentType(String[] contentTypes) {
        this.contentTypes = normalizeStrings(contentTypes, 0);
    }

    /**
     * 取得上传文件的文件名后缀。
     */
    public String[] getExtension() {
        return extensions;
    }

    /**
     * 设置上传文件的文件名后缀。
     */
    public void setExtension(String[] extensions) {
        this.extensions = normalizeStrings(extensions, 1);
    }

    /**
     * 取得最小尺寸。
     * <p>
     * 支持K/M/G/T等。
     * </p>
     */
    public HumanReadableSize getMinSize() {
        return minSize;
    }

    /**
     * 设置最小尺寸。
     * <p>
     * 支持K/M/G/T等。
     * </p>
     */
    public void setMinSize(HumanReadableSize minSize) {
        this.minSize = assertNotNull(minSize, "minSize");
    }

    /**
     * 取得最大尺寸。
     * <p>
     * 支持K/M/G/T等。
     * </p>
     */
    public HumanReadableSize getMaxSize() {
        return maxSize;
    }

    /**
     * 设置最大尺寸。
     * <p>
     * 支持K/M/G/T等。
     * </p>
     */
    public void setMaxSize(HumanReadableSize maxSize) {
        this.maxSize = assertNotNull(maxSize, "maxSize");
    }

    /**
     * 验证一个字段。
     */
    public boolean validate(Context context) {
        long minSize = this.minSize.getValue();
        long maxSize = this.maxSize.getValue();

        if (isEmptyArray(contentTypes) && isEmptyArray(extensions) && maxSize <= 0 && minSize < 0) {
            return true;
        }

        FileItem[] fileItems = context.getField().getFileItems();

        // 假如不存在fileItems，也通过验证。用required-validator来确保fileItem有值。
        if (isEmptyArray(fileItems)) {
            return true;
        }

        for (FileItem fileItem : fileItems) {
            if (fileItem == null) {
                continue; // 忽略空item
            }

            // 检查size limit。
            if (minSize >= 0 && fileItem.getSize() < minSize) {
                return false;
            }

            if (maxSize >= 0 && fileItem.getSize() > maxSize) {
                return false;
            }

            // 检查content type。
            if (!isEmptyArray(contentTypes)) {
                String fileContentType = normalizeContentType(fileItem.getContentType());

                if (fileContentType == null) {
                    return false;
                }

                boolean matched = false;

                for (String expectedContentType : contentTypes) {
                    if (fileContentType.startsWith(expectedContentType)) {
                        matched = true;
                        break;
                    }
                }

                if (!matched) {
                    return false;
                }
            }

            // 检查extension。
            if (!isEmptyArray(extensions)) {
                // 未指定文件名 - 返回null
                // 文件名没有后缀 - 返回字符串“null”
                // 后缀被规格化为小写字母
                String ext = FileUtil.getExtension(fileItem.getName(), "null");

                if (ext == null) {
                    return false;
                }

                boolean matched = false;

                for (String expectedExtension : extensions) {
                    if (expectedExtension.equals(ext)) {
                        matched = true;
                        break;
                    }
                }

                if (!matched) {
                    return false;
                }
            }
        }

        return true;
    }

    private String[] normalizeStrings(String[] strs, int type) {
        if (isEmptyArray(strs)) {
            return null;
        }

        List<String> strList = createLinkedList();

        for (String str : strs) {
            switch (type) {
                case 0:
                    str = normalizeContentType(str);
                    break;

                case 1:
                    str = FileUtil.normalizeExtension(str);
                    break;

                default:
                    unreachableCode();
            }

            if (str != null) {
                strList.add(str);
            }
        }

        if (strList.isEmpty()) {
            return null;
        } else {
            return strList.toArray(new String[strList.size()]);
        }
    }

    private String normalizeContentType(String contentType) {
        contentType = trimToNull(contentType);

        if (contentType != null) {
            contentType = contentType.toLowerCase();
        }

        return contentType;
    }

    public static class DefinitionParser extends AbstractValidatorDefinitionParser<UploadedFileValidator> {
    }
}
