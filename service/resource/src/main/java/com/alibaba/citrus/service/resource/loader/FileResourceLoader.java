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
package com.alibaba.citrus.service.resource.loader;

import static com.alibaba.citrus.service.resource.ResourceLoadingOption.*;
import static com.alibaba.citrus.util.ArrayUtil.*;
import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.FileUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.io.File;
import java.net.URL;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.citrus.service.resource.Resource;
import com.alibaba.citrus.service.resource.ResourceLister;
import com.alibaba.citrus.service.resource.ResourceListerContext;
import com.alibaba.citrus.service.resource.ResourceLoaderContext;
import com.alibaba.citrus.service.resource.ResourceLoadingOption;
import com.alibaba.citrus.service.resource.ResourceLoadingService;
import com.alibaba.citrus.service.resource.ResourceMatchResult;
import com.alibaba.citrus.service.resource.support.FileResource;
import com.alibaba.citrus.util.internal.ToStringBuilder;

/**
 * 用来装载文件系统中的资源。
 * 
 * @author Michael Zhou
 */
public class FileResourceLoader implements ResourceLister {
    private final static Logger log = LoggerFactory.getLogger(FileResourceLoader.class);
    private String basedir;
    private String configFileBasedir;
    private SearchPath[] paths;

    /**
     * 取得basedir。
     * <p>
     * 假如没有指定，则返回当前配置文件所在的目录（即file-loader配置所在的目录）。
     * </p>
     */
    public String getBasedir() {
        return basedir;
    }

    /**
     * 设置basedir。
     */
    public void setBasedir(String basedir) {
        this.basedir = trimToNull(basedir);
    }

    /**
     * 取得用来配置当前file-loader的配置文件所在的目录。
     * <p>
     * 这个目录将被用作默认的相对路径根目录。
     * </p>
     * <p>
     * 假如配置文件并非直接从文件系统中取得，则返回<code>null</code>。
     * </p>
     */
    public String getConfigFileBasedir() {
        return configFileBasedir;
    }

    /**
     * 设置file-loader所在的配置文件的URL。
     */
    public void setConfigFileURL(URL configFileURL) {
        if (configFileURL != null) {
            File configFile = null;

            try {
                configFile = new File(configFileURL.toURI());
            } catch (Exception e) {
                // not a file: URL
            }

            if (configFile != null) {
                this.configFileBasedir = configFile.getParentFile().getAbsolutePath();
            }
        }
    }

    public SearchPath[] getPaths() {
        return paths;
    }

    public void setPaths(SearchPath[] paths) {
        this.paths = paths;
    }

    /**
     * 初始化loader，并设定loader所在的<code>ResourceLoadingService</code>的实例。
     */
    public void init(ResourceLoadingService resourceLoadingService) {
        // 设置basedir：
        // 1. 如果没有指定basedir，则将当前配置文件所在目录看做basedir
        // 2. 如果指定了相对路径的basedir，则相对于当前配置文件所在目录
        // 3. 如果指定了绝对路径的basedir，则以此作为basedir
        // 最后，规格化basedir。
        if (basedir == null) {
            basedir = configFileBasedir;
        } else {
            if (configFileBasedir != null) {
                basedir = getSystemDependentAbsolutePathBasedOn(configFileBasedir, basedir);
            }
        }

        basedir = trimToNull(normalizePath(basedir));

        // 如果未指定path，则加入默认的path：/
        if (isEmptyArray(paths)) {
            paths = new SearchPath[] { new SearchPath("/", true) };
        }

        // 设置relative path的basedir
        for (SearchPath searchPath : paths) {
            searchPath.init(basedir);
        }
    }

    /**
     * 查找文件资源。
     */
    public Resource getResource(ResourceLoaderContext context, Set<ResourceLoadingOption> options) {
        File file = find(context, options);

        if (file != null) {
            return new FileResource(file);
        } else {
            return null;
        }
    }

    /**
     * 查找目录列表。
     */
    public String[] list(ResourceListerContext context, Set<ResourceLoadingOption> options) {
        File file = find(context, options);
        File[] files = file == null ? null : file.listFiles();

        if (files != null) {
            String[] names = new String[files.length];

            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    names[i] = files[i].getName() + "/";
                } else {
                    names[i] = files[i].getName();
                }
            }

            return names;
        } else {
            return null;
        }
    }

    /**
     * 查找文件。
     */
    private File find(ResourceMatchResult context, Set<ResourceLoadingOption> options) {
        File file = null;

        log.trace("Searching for file {} in {} search-paths", context.getResourceName(), paths.length);

        for (SearchPath searchPath : paths) {
            File resourceFile = searchPath.getPath(context);

            if (log.isTraceEnabled()) {
                StringBuilder buf = new StringBuilder();

                buf.append("Search in ").append(searchPath).append("\n");
                buf.append("  Testing file: ").append(resourceFile.getAbsolutePath());

                if (resourceFile.exists()) {
                    buf.append(", file exists");
                } else {
                    buf.append(", file does not exist");
                }

                log.trace(buf.toString());
            }

            if (resourceFile.exists()) {
                file = resourceFile;
                break;
            } else {
                // 如果文件不存在，但指定了for_create参数，则返回第一个不存在的文件对象。
                if (options != null && options.contains(FOR_CREATE)) {
                    if (file == null) {
                        file = resourceFile;
                    }
                }
            }
        }

        return file;
    }

    @Override
    public String toString() {
        return new ToStringBuilder().append(getClass().getSimpleName()).append(paths).toString();
    }

    /**
     * 代表一个搜索路径。
     * <p>
     * 假如指定了basedir，则表示path为相对于basedir的路径；否则表示path为绝对路径。
     * </p>
     */
    public static class SearchPath {
        private final String path;
        private final boolean relative;
        private String basedir;

        public SearchPath(String path, boolean relative) {
            this.path = assertNotNull(trimToNull(normalizePath(path)), "path");
            this.relative = relative;
        }

        public void init(String basedir) {
            if (relative) {
                this.basedir = assertNotNull(basedir, "Could not get basedir for search path: %s.  "
                        + "Please set basedir explictly at file-loader or use absolute path instead", this);
            }
        }

        /**
         * 取得匹配的路径。
         */
        public File getPath(ResourceMatchResult context) {
            String realPath = context.substitute(path);

            if (basedir != null) {
                realPath = new File(basedir, realPath).getAbsolutePath();
            }

            return new File(normalizePath(realPath));
        }

        @Override
        public String toString() {
            StringBuilder buf = new StringBuilder();

            if (relative) {
                buf.append("relpath=").append(path);
                buf.append(", basedir=").append(basedir);
            } else {
                buf.append("abspath=").append(path);
            }

            return buf.toString();
        }
    }
}
