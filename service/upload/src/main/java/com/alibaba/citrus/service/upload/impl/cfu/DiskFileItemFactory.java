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
package com.alibaba.citrus.service.upload.impl.cfu;

import org.apache.commons.fileupload.FileItem;

/**
 * 继承自commons-fileupload-1.2.1的同名类，改进了如下内容：
 * <ul>
 * <li>添加新的<code>keepFormFieldInMemory</code>参数。</li>
 * <li>创建新的DiskFileItem对象。</li>
 * </ul>
 * 
 * @author Michael Zhou
 */
public class DiskFileItemFactory extends org.apache.commons.fileupload.disk.DiskFileItemFactory {
    private boolean keepFormFieldInMemory;

    public boolean isKeepFormFieldInMemory() {
        return keepFormFieldInMemory;
    }

    public void setKeepFormFieldInMemory(boolean keepFormFieldInMemory) {
        this.keepFormFieldInMemory = keepFormFieldInMemory;
    }

    @Override
    public FileItem createItem(String fieldName, String contentType, boolean isFormField, String fileName) {
        int sizeThreshold = getSizeThreshold();

        if (isFormField && (sizeThreshold == 0 || keepFormFieldInMemory)) {
            return new InMemoryFormFieldItem(fieldName, contentType, isFormField, fileName, sizeThreshold,
                    keepFormFieldInMemory, getRepository());
        } else {
            return new DiskFileItem(fieldName, contentType, isFormField, fileName, sizeThreshold,
                    keepFormFieldInMemory, getRepository());
        }
    }
}
