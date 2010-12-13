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

import java.io.File;

/**
 * 用来存储form field的<code>FileItem</code>实现。
 * <p>
 * 避免了<code>DiskFileItem.finalize()</code>方法的开销。
 * </p>
 * 
 * @author Michael Zhou
 */
public class InMemoryFormFieldItem extends AbstractFileItem {
    private static final long serialVersionUID = -103002370072467461L;

    public InMemoryFormFieldItem(String fieldName, String contentType, boolean isFormField, String fileName,
                                 int sizeThreshold, boolean keepFormFieldInMemory, File repository) {
        super(fieldName, contentType, isFormField, fileName, sizeThreshold, keepFormFieldInMemory, repository);
    }
}
