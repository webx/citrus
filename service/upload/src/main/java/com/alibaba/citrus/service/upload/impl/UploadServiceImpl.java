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
package com.alibaba.citrus.service.upload.impl;

import java.io.File;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.FileUploadException;

import com.alibaba.citrus.service.AbstractService;
import com.alibaba.citrus.service.upload.UploadException;
import com.alibaba.citrus.service.upload.UploadParameters;
import com.alibaba.citrus.service.upload.UploadService;
import com.alibaba.citrus.service.upload.UploadSizeLimitExceededException;
import com.alibaba.citrus.service.upload.impl.cfu.DiskFileItemFactory;
import com.alibaba.citrus.service.upload.impl.cfu.ServletFileUpload;
import com.alibaba.citrus.util.HumanReadableSize;

/**
 * 这个service可以处理<code>multipart/form-data</code>格式的HTTP
 * POST请求，并将它们转换成form字段或是文件。
 * 
 * @author Michael Zhou
 */
public class UploadServiceImpl extends AbstractService<UploadService> implements UploadService {
    private final UploadParameters params = new UploadParameters();
    private ServletFileUpload fileUpload;

    public File getRepository() {
        return params.getRepository();
    }

    public HumanReadableSize getSizeMax() {
        return params.getSizeMax();
    }

    public HumanReadableSize getFileSizeMax() {
        return params.getFileSizeMax();
    }

    public HumanReadableSize getSizeThreshold() {
        return params.getSizeThreshold();
    }

    public boolean isKeepFormFieldInMemory() {
        return params.isKeepFormFieldInMemory();
    }

    public void setSizeMax(HumanReadableSize sizeMax) {
        params.setSizeMax(sizeMax);
    }

    public void setFileSizeMax(HumanReadableSize fileSizeMax) {
        params.setFileSizeMax(fileSizeMax);
    }

    public void setSizeThreshold(HumanReadableSize sizeThreshold) {
        params.setSizeThreshold(sizeThreshold);
    }

    public void setKeepFormFieldInMemory(boolean keepFormFieldInMemory) {
        params.setKeepFormFieldInMemory(keepFormFieldInMemory);
    }

    public void setRepository(File repository) {
        params.setRepository(repository);
    }

    public String[] getFileNameKey() {
        return params.getFileNameKey();
    }

    public void setFileNameKey(String[] fileNameKey) {
        params.setFileNameKey(fileNameKey);
    }

    @Override
    protected void init() {
        params.applyDefaultValues();
        getLogger().info("Upload Parameters: {}", params);

        fileUpload = getFileUpload(params, false);
    }

    /**
     * 判断是否是符合<a href="http://www.ietf.org/rfc/rfc1867.txt">RFC 1867</a>标准的
     * <code>multipart/form-data</code>类型的HTTP请求。
     * 
     * @param request HTTP请求
     * @return 如果是，则返回<code>true</code>
     */
    public boolean isMultipartContent(HttpServletRequest request) {
        return org.apache.commons.fileupload.servlet.ServletFileUpload.isMultipartContent(request);
    }

    /**
     * 解析符合<a href="http://www.ietf.org/rfc/rfc1867.txt">RFC 1867</a>标准的
     * <code>multipart/form-data</code>类型的HTTP请求。
     * 
     * @param request HTTP请求
     * @return <code>FileItem</code>的列表，按其输入的顺序罗列
     * @throws UploadException 如果解析时出错
     */
    public FileItem[] parseRequest(HttpServletRequest request) {
        return parseRequest(request, null);
    }

    /**
     * 解析符合<a href="http://www.ietf.org/rfc/rfc1867.txt">RFC 1867</a>标准的
     * <code>multipart/form-data</code>类型的HTTP请求。
     * <p>
     * 此方法覆盖了service的默认设置，适合于在action或servlet中手工执行。
     * </p>
     * 
     * @param request HTTP请求
     * @param sizeThreshold 文件放在内存中的阈值，小于此值的文件被保存在内存中。如果此值小于0，则使用预设的值
     * @param sizeMax HTTP请求的最大尺寸，超过此尺寸的请求将被抛弃。
     * @param repositoryPath 暂存上载文件的绝对路径
     * @param charset 用来解析HTTP header的编码字符集
     * @return <code>FileItem</code>的列表，按其输入的顺序罗列
     * @throws UploadException 如果解析时出错
     */
    public FileItem[] parseRequest(HttpServletRequest request, UploadParameters params) {
        assertInitialized();

        ServletFileUpload fileUpload;

        if (params == null || params.equals(this.params)) {
            fileUpload = this.fileUpload;
        } else {
            fileUpload = getFileUpload(params, true);
        }

        List<?> fileItems;

        try {
            fileItems = fileUpload.parseRequest(request);
        } catch (FileUpload.SizeLimitExceededException e) {
            throw new UploadSizeLimitExceededException(e);
        } catch (FileUpload.FileSizeLimitExceededException e) {
            throw new UploadSizeLimitExceededException(e);
        } catch (FileUploadException e) {
            throw new UploadException(e);
        }

        return fileItems.toArray(new FileItem[fileItems.size()]);
    }

    /**
     * 根据参数创建<code>FileUpload</code>对象。
     */
    private ServletFileUpload getFileUpload(UploadParameters params, boolean applyDefaultValues) {
        if (applyDefaultValues) {
            params.applyDefaultValues();
            getLogger().debug("Upload Parameters: {}", params);
        }

        // 用于生成FileItem的参数
        DiskFileItemFactory factory = new DiskFileItemFactory();

        factory.setRepository(params.getRepository());
        factory.setSizeThreshold((int) params.getSizeThreshold().getValue());
        factory.setKeepFormFieldInMemory(params.isKeepFormFieldInMemory());

        // 用于解析multipart request的参数
        ServletFileUpload fileUpload = new ServletFileUpload(factory);

        fileUpload.setSizeMax(params.getSizeMax().getValue());
        fileUpload.setFileSizeMax(params.getFileSizeMax().getValue());
        fileUpload.setFileNameKey(params.getFileNameKey());

        return fileUpload;
    }
}
