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
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

import com.alibaba.citrus.service.resource.Resource;

/**
 * 代表一个<code>URL</code>的资源。
 * 
 * @author Michael Zhou
 */
public class URLResource implements Resource {
    private final URL url;
    private final File file;

    /**
     * 创建一个<code>URLResource</code>。
     */
    public URLResource(URL url) {
        this.url = assertNotNull(url, "url");
        this.file = toFile(url);
    }

    private static File toFile(URL url) {
        if ("file".equalsIgnoreCase(url.getProtocol())) {
            try {
                return new File(url.toURI());
            } catch (URISyntaxException e) {
                return null;
            }
        }

        return null;
    }

    /**
     * 取得资源的<code>URL</code>。
     */
    public URL getURL() {
        return url;
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
        // 对文件优化
        if (file != null) {
            try {
                return new FileInputStream(file);
            } catch (FileNotFoundException e) {
                return null;
            }
        } else {
            return url.openStream();
        }
    }

    /**
     * 判断资源是否存在。
     */
    public boolean exists() {
        // 对文件优化
        if (file != null) {
            return file.exists();
        } else {
            InputStream istream = null;
            boolean exists = false;

            try {
                istream = url.openStream();
                exists = true;
            } catch (IOException e) {
            } finally {
                if (istream != null) {
                    try {
                        istream.close();
                    } catch (IOException e) {
                    }
                }
            }

            return exists;
        }
    }

    /**
     * 取得资源最近修改时间。
     */
    public long lastModified() {
        if (file != null) {
            return file.lastModified();
        } else {
            URLConnection conn = null;
            long lastModified = 0;

            try {
                conn = url.openConnection();
                lastModified = conn.getLastModified();
            } catch (IOException e) {
            }

            return lastModified;
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (url == null ? 0 : url.hashCode());
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

        URLResource other = (URLResource) obj;

        if (url == null) {
            if (other.url != null) {
                return false;
            }
        } else if (!url.equals(other.url)) {
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

        if (file != null) {
            // 按文件形式显示
            if (file.exists()) {
                buf.append(file.isFile() ? "file: " : "directory: ");
                buf.append(file.getAbsolutePath());
            } else {
                buf.append("file or directory does not exist: ").append(file.getAbsolutePath());
            }
        } else {
            // 按URL形式显示
            buf.append("URL: ").append(url.toExternalForm());
        }

        buf.append("]");

        return buf.toString();
    }
}
