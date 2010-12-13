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

public class DiskFileItem extends AbstractFileItem {
    private static final long serialVersionUID = 4225039123863446602L;

    public DiskFileItem(String fieldName, String contentType, boolean isFormField, String fileName, int sizeThreshold,
                        boolean keepFormFieldInMemory, File repository) {
        super(fieldName, contentType, isFormField, fileName, sizeThreshold, keepFormFieldInMemory, repository);
    }

    /**
     * Removes the file contents from the temporary storage.
     */
    @Override
    protected void finalize() {
        File outputFile = dfos.getFile();

        if (outputFile != null && outputFile.exists()) {
            outputFile.delete();
        }
    }
}
