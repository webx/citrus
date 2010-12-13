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
package com.alibaba.citrus.service.upload;

import static com.alibaba.citrus.util.ArrayUtil.*;
import static com.alibaba.citrus.util.Assert.*;

import java.io.File;
import java.util.Arrays;

import com.alibaba.citrus.util.HumanReadableSize;
import com.alibaba.citrus.util.SystemUtil;
import com.alibaba.citrus.util.internal.ToStringBuilder.MapBuilder;

public class UploadParameters implements UploadConfiguration {
    private File repository;
    private HumanReadableSize sizeMax = new HumanReadableSize(SIZE_MAX_DEFAULT);
    private HumanReadableSize fileSizeMax = new HumanReadableSize(FILE_SIZE_MAX_DEFAULT);
    private HumanReadableSize sizeThreshold = new HumanReadableSize(SIZE_THRESHOLD_DEFAULT);
    private boolean keepFormFieldInMemory;
    private String fileNameKey[];

    public File getRepository() {
        return repository;
    }

    public void setRepository(File repository) {
        this.repository = repository;
    }

    public HumanReadableSize getSizeMax() {
        return sizeMax;
    }

    public void setSizeMax(HumanReadableSize sizeMax) {
        this.sizeMax = assertNotNull(sizeMax, "sizeMax");
    }

    public void setSizeMax(long sizeMax) {
        setSizeMax(new HumanReadableSize(sizeMax));
    }

    public HumanReadableSize getFileSizeMax() {
        return fileSizeMax;
    }

    public void setFileSizeMax(HumanReadableSize fileSizeMax) {
        this.fileSizeMax = assertNotNull(fileSizeMax, "fileSizeMax");
    }

    public void setFileSizeMax(long fileSizeMax) {
        setFileSizeMax(new HumanReadableSize(fileSizeMax));
    }

    public HumanReadableSize getSizeThreshold() {
        return sizeThreshold;
    }

    public void setSizeThreshold(HumanReadableSize sizeThreshold) {
        this.sizeThreshold = assertNotNull(sizeThreshold, "sizeThreshold");
    }

    public void setSizeThreshold(int sizeThreshold) {
        this.sizeThreshold = new HumanReadableSize(sizeThreshold);
    }

    public boolean isKeepFormFieldInMemory() {
        return keepFormFieldInMemory;
    }

    public void setKeepFormFieldInMemory(boolean keepFormFieldInMemory) {
        this.keepFormFieldInMemory = keepFormFieldInMemory;
    }

    public String[] getFileNameKey() {
        return fileNameKey;
    }

    public void setFileNameKey(String[] fileNameKey) {
        this.fileNameKey = fileNameKey;
    }

    /**
     * …Ë÷√ƒ¨»œ÷µ°£
     */
    public void applyDefaultValues() {
        if (sizeThreshold.getValue() == 0) {
            keepFormFieldInMemory = true;
        }

        if (repository == null) {
            repository = new File(SystemUtil.getUserInfo().getTempDir());
        }

        if (!repository.exists() && !repository.mkdirs()) {
            throw new IllegalArgumentException("Could not create repository directory for file uploading: "
                    + repository);
        }

        if (isEmptyArray(fileNameKey)) {
            fileNameKey = new String[] { "filename" };
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || !(obj instanceof UploadParameters)) {
            return false;
        }

        UploadParameters other = (UploadParameters) obj;

        if (repository == null) {
            if (other.repository != null) {
                return false;
            }
        } else if (!repository.equals(other.repository)) {
            return false;
        }

        if (sizeMax != other.sizeMax) {
            return false;
        }

        if (fileSizeMax != other.fileSizeMax) {
            return false;
        }

        if (sizeThreshold != other.sizeThreshold) {
            return false;
        }

        if (keepFormFieldInMemory != other.keepFormFieldInMemory) {
            return false;
        }

        if (!Arrays.equals(fileNameKey, other.fileNameKey)) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        MapBuilder mb = new MapBuilder();

        mb.append("Repository Path", getRepository());
        mb.append("Maximum Request Size", getSizeMax());
        mb.append("Maximum File Size", getFileSizeMax());
        mb.append("Threshold before Writing to File", getSizeThreshold());
        mb.append("Keep Form Field in Memory", isKeepFormFieldInMemory());
        mb.append("File Name Key", getFileNameKey());

        return mb.toString();
    }
}
