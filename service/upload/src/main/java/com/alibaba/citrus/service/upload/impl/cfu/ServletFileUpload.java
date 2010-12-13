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

import static com.alibaba.citrus.util.ArrayUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileItemHeaders;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.ParameterParser;
import org.apache.commons.fileupload.RequestContext;

/**
 * 继承自commons-fileupload-1.2.1的同名类，改进了如下内容：
 * <ul>
 * <li>使用新的<code>ServletRequestContext</code>,以便在处理字符集编码时，和servlet规范相容。详见
 * {@link ServletRequestContext}。</li>
 * </ul>
 * 
 * @author Michael Zhou
 */
public class ServletFileUpload extends org.apache.commons.fileupload.servlet.ServletFileUpload {
    private String fileNameKey[];

    public ServletFileUpload() {
        super();
    }

    public ServletFileUpload(FileItemFactory fileItemFactory) {
        super(fileItemFactory);
    }

    public String[] getFileNameKey() {
        if (isEmptyArray(fileNameKey)) {
            fileNameKey = new String[] { "filename" };
        }

        return fileNameKey;
    }

    public void setFileNameKey(String[] fileNameKey) {
        this.fileNameKey = fileNameKey;
    }

    @Override
    public List<?/* FileItem */> parseRequest(HttpServletRequest request) throws FileUploadException {
        return parseRequest(new ServletRequestContext(request));
    }

    @Override
    public List<?/* FileItem */> parseRequest(RequestContext ctx) throws FileUploadException {
        @SuppressWarnings("unchecked")
        List<FileItem> items = super.parseRequest(ctx);
        String charset = ctx.getCharacterEncoding();

        for (FileItem fileItem : items) {
            if (fileItem instanceof AbstractFileItem) {
                ((AbstractFileItem) fileItem).setCharset(charset);
            }
        }

        return items;
    }

    @Override
    @SuppressWarnings({ "rawtypes", "deprecation" })
    protected String getFileName(Map /* String, String */headers) {
        return getFileName(getHeader(headers, CONTENT_DISPOSITION));
    }

    @Override
    protected String getFileName(FileItemHeaders headers) {
        return getFileName(headers.getHeader(CONTENT_DISPOSITION));
    }

    private String getFileName(String pContentDisposition) {
        String fileName = null;

        if (pContentDisposition != null) {
            String cdl = pContentDisposition.toLowerCase();

            if (cdl.startsWith(FORM_DATA) || cdl.startsWith(ATTACHMENT)) {
                ParameterParser parser = new ParameterParser();
                parser.setLowerCaseNames(true);

                // Parameter parser can handle null input
                @SuppressWarnings("unchecked")
                Map<String, String> params = parser.parse(pContentDisposition, ';');

                // 解决类似Flash上传更改了filename 为 fname 的多客户端支持
                for (String key : getFileNameKey()) {
                    fileName = trimToNull(params.get(key));

                    if (fileName != null) {
                        break;
                    }
                }
            }
        }

        return fileName;
    }

    @Override
    public FileItemIterator getItemIterator(HttpServletRequest request) throws FileUploadException, IOException {
        return super.getItemIterator(new ServletRequestContext(request));
    }
}
