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

import java.io.File;

import com.alibaba.citrus.util.HumanReadableSize;

/**
 * 定义<code>UploadService</code>的参数。
 * 
 * @author Michael Zhou
 */
public interface UploadConfiguration {
    /** 默认值：HTTP请求的最大尺寸，超过此尺寸的请求将被抛弃。 */
    long SIZE_MAX_DEFAULT = -1;

    /** 默认值：单个文件允许的最大尺寸，超过此尺寸的请求将被抛弃。 */
    long FILE_SIZE_MAX_DEFAULT = -1;

    /** 默认值：将文件放在内存中的阈值，小于此值的文件被保存在内存中。 */
    int SIZE_THRESHOLD_DEFAULT = 10240;

    /**
     * 取得暂存文件的目录。
     */
    File getRepository();

    /**
     * 取得HTTP请求的最大尺寸，超过此尺寸的请求将被抛弃。单位：字节，值<code>-1</code>表示没有限制。
     */
    HumanReadableSize getSizeMax();

    /**
     * 取得单个文件允许的最大尺寸，超过此尺寸的文件将被抛弃。单位：字节，值<code>-1</code>表示没有限制。
     */
    HumanReadableSize getFileSizeMax();

    /**
     * 取得将文件放在内存中的阈值，小于此值的文件被保存在内存中。单位：字节。
     */
    HumanReadableSize getSizeThreshold();

    /**
     * 是否将普通的form field保持在内存里？当<code>sizeThreshold</code>值为<code>0</code>
     * 的时候，该值自动为<code>true</code>。
     */
    boolean isKeepFormFieldInMemory();

    /**
     * 标准的上传文件请求中，包含这样的内容：
     * <code>Content-Disposition: attachment; filename=xxx.txt</code>
     * 。然而有些不规范的应用，会取<code>fname=xxx.txt</code>。此变量为兼容这种情况而设。
     */
    String[] getFileNameKey();
}
