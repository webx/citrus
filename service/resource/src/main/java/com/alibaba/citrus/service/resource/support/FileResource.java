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
package com.alibaba.citrus.service.resource.support;

import static com.alibaba.citrus.util.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import com.alibaba.citrus.service.resource.Resource;

/**
 * 代表一个文件<code>File</code>的资源。
 * 
 * @author Michael Zhou
 */
public class FileResource implements Resource {
    private final File file;

    /**
     * 创建一个<code>FileResource</code>。
     */
    public FileResource(File file) {
        this.file = assertNotNull(file, "file");
    }

    /**
     * 取得资源的<code>URL</code>。
     */
    public URL getURL() {
        try {
            return file.toURI().toURL();
        } catch (MalformedURLException e) {
            return null;
        }
    }

    /**
     * 取得资源的<code>File</code>。
     */
    public File getFile() {
        return file;
    }

    /**
     * 取得资源的<code>InputStream</code>。
     */
    public InputStream getInputStream() throws IOException {
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    /**
     * 判断资源是否存在。
     */
    public boolean exists() {
        return file.exists();
    }

    /**
     * 取得资源最近修改时间。
     */
    public long lastModified() {
        return file.lastModified();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (file == null ? 0 : file.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        FileResource other = (FileResource) obj;

        if (file == null) {
            if (other.file != null) {
                return false;
            }
        } else if (!file.equals(other.file)) {
            return false;
        }

        return true;
    }

    /**
     * 将resource转换成字符串表示。
     */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();

        buf.append(getClass().getSimpleName()).append("[");

        if (file.exists()) {
            buf.append(file.isFile() ? "file: " : "directory: ");
            buf.append(file.getAbsolutePath());
        } else {
            buf.append("file or directory does not exist: ").append(file.getAbsolutePath());
        }

        buf.append("]");

        return buf.toString();
    }
}
