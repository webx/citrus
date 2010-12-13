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
package com.alibaba.citrus.service.upload.support;

import java.beans.PropertyEditorSupport;

import org.apache.commons.fileupload.FileItem;

/**
 * ½«<code>FileItem</code>×ª»»³É×Ö·û´®¡£
 * 
 * @author Michael Zhou
 */
public class StringFileItemEditor extends PropertyEditorSupport {
    @Override
    public void setAsText(String text) {
        setValue(text);
    }

    @Override
    public void setValue(Object value) {
        if (value instanceof FileItem) {
            super.setValue(((FileItem) value).getName());
        } else {
            super.setValue(value);
        }
    }
}
