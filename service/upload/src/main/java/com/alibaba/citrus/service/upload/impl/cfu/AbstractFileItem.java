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
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.citrus.service.upload.impl.cfu;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemHeaders;
import org.apache.commons.fileupload.FileItemHeadersSupport;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.DeferredFileOutputStream;

import com.alibaba.citrus.util.StringUtil;

/**
 * 改进自<code>commons-fileupload-1.2.1</code>的同名类。
 * <p>
 * 解决了如下问题：
 * </p>
 * <ol>
 * <li>原<code>DiskFileItem</code>类（以下简称原类）在解析form field的值时，会利用
 * <code>content-type</code>头部指定的<code>charset</code>值来决定其字符集编码。例如，下面的
 * <code>multipart/form-data</code>请求片段指定了myparam field值的字符集编码为
 * <code>UTF-8</code>：
 * 
 * <pre>
 * ----HttpUnit-part0-aSgQ2M
 * Content-Disposition: form-data; name=&quot;myparam&quot;
 * Content-Type: text/plain; charset=UTF-8
 * </pre>
 * 
 * 然而，除了单元测试所用的<code>httpunit/servletunit</code>以外，几乎没有浏览器会在这里指定
 * <code>content-type</code>以及 <code>charset</code>。因此原类的
 * <code>getString()</code>总是得不到解码正确的字符串。</li>
 * <li>原类将内容的长度超过<code>sizeThreshold</code>的字段 —— 无论普通的form fields或是文件字段 ——
 * 均存入文件 。这是一种优化。然而在某些情况下，我们希望关闭这种优化 —— 将 <code>sizeThreshold</code>设置成
 * <code>0</code> ，以便让所有上传文件无论大小都被存入磁盘。然而仍然希望普通的form fields被保存在内存里。</li>
 * <li>创建文件时，希望能自动创建目录。</li>
 * </ol>
 * <p>
 * 具体改进了如下内容：
 * </p>
 * <ul>
 * <li>利用传入的<code>charset</code>参数，而不是<code>content-type</code>来解码form
 * field。但该参数对于文件型字段无效。</li>
 * <li>删除<code>getCharSet()</code>方法，添加<code>getCharset()</code>和
 * <code>setCharset()</code>方法。</li>
 * <li>修改<code>getString()</code>方法，对form field使用指定的<code>charset</code>来解码。</li>
 * <li>添加<code>keepFormFieldInMemory</code>属性。</li>
 * <li>改进<code>getOutputStream()</code>方法，，当
 * <code>keepFormFieldInMemory == true</code>时，不将form fields写入文件，即将
 * <code>threshold</code>设置成<code>Integer.MAX_VALUE</code>。</li>
 * <li>利用<code>File.createTempFile()</code>来生成临时文件，删除原<code>getTempFile()</code>
 * 方法，及相关的<code>getUniqueId()</code>方法、<code>counter</code> field、
 * <code>tempFile</code> field。</li>
 * <li>改进write()方法，当文件目录不存在时，创建之。</li>
 * <li>改进toString()方法，使之返回文件名，这种形式是为了方便页面引用<code>FileItem</code>对象。</li>
 * </ul>
 * 
 * @author Michael Zhou
 */
public abstract class AbstractFileItem implements FileItem, FileItemHeadersSupport {
    private static final long serialVersionUID = 486705336474235297L;

    /**
     * Default content charset to be used when no explicit charset parameter is
     * provided by the sender. Media subtypes of the "text" type are defined to
     * have a default charset value of "ISO-8859-1" when received via HTTP.
     */
    public static final String DEFAULT_CHARSET = "ISO-8859-1";

    // ----------------------------------------------------------- Data members

    /**
     * UID used in unique file name generation.
     */
    private static final String UID = new java.rmi.server.UID().toString().replace(':', '_').replace('-', '_');

    /**
     * The name of the form field as provided by the browser.
     */
    private String fieldName;

    /**
     * The content type passed by the browser, or <code>null</code> if not
     * defined.
     */
    private String contentType;

    /**
     * Whether or not this item is a simple form field.
     */
    private boolean isFormField;

    /**
     * The original filename in the user's filesystem.
     */
    private String fileName;

    /**
     * The size of the item, in bytes. This is used to cache the size when a
     * file item is moved from its original location.
     */
    private long size = -1;

    /**
     * The threshold above which uploads will be stored on disk.
     */
    private int sizeThreshold;

    private boolean keepFormFieldInMemory;

    /**
     * The directory in which uploaded files will be stored, if stored on disk.
     */
    private File repository;

    /**
     * Cached contents of the file.
     */
    private byte[] cachedContent;

    /**
     * Output stream for this item.
     */
    protected transient DeferredFileOutputStream dfos;

    /**
     * File to allow for serialization of the content of this item.
     */
    private File dfosFile;

    /**
     * The file items headers.
     */
    private FileItemHeaders headers;

    /**
     * 用于解码字段值的字符集编码。
     */
    private String charset;

    // ----------------------------------------------------------- Constructors

    /**
     * Constructs a new <code>DiskFileItem</code> instance.
     * 
     * @param fieldName The name of the form field.
     * @param contentType The content type passed by the browser or
     *            <code>null</code> if not specified.
     * @param isFormField Whether or not this item is a plain form field, as
     *            opposed to a file upload.
     * @param fileName The original filename in the user's filesystem, or
     *            <code>null</code> if not specified.
     * @param sizeThreshold The threshold, in bytes, below which items will be
     *            retained in memory and above which they will be stored as a
     *            file.
     * @para keepFormFieldInMemory
     * @param repository The data repository, which is the directory in which
     *            files will be created, should the item size exceed the
     *            threshold.
     */
    public AbstractFileItem(String fieldName, String contentType, boolean isFormField, String fileName,
                            int sizeThreshold, boolean keepFormFieldInMemory, File repository) {
        // 设置默认值
        if (sizeThreshold < 0) {
            sizeThreshold = 0;
        }

        if (sizeThreshold == 0) {
            keepFormFieldInMemory = true;
        }

        if (repository == null) {
            repository = new File(System.getProperty("java.io.tmpdir"));
        }

        if (!repository.exists()) {
            repository.mkdirs();
        }

        this.fieldName = fieldName;
        this.contentType = contentType;
        this.isFormField = isFormField;
        this.fileName = fileName;
        this.sizeThreshold = sizeThreshold;
        this.keepFormFieldInMemory = keepFormFieldInMemory;
        this.repository = repository;
    }

    // ------------------------------- Methods from javax.activation.DataSource

    /**
     * Returns an {@link java.io.InputStream InputStream} that can be used to
     * retrieve the contents of the file.
     * 
     * @return An {@link java.io.InputStream InputStream} that can be used to
     *         retrieve the contents of the file.
     * @throws IOException if an error occurs.
     */
    public InputStream getInputStream() throws IOException {
        if (!isInMemory()) {
            return new FileInputStream(dfos.getFile());
        }

        if (cachedContent == null) {
            cachedContent = dfos.getData();
        }
        return new ByteArrayInputStream(cachedContent);
    }

    /**
     * Returns the content type passed by the agent or <code>null</code> if not
     * defined.
     * 
     * @return The content type passed by the agent or <code>null</code> if not
     *         defined.
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * 取得当前field的字符集编码。
     */
    public String getCharset() {
        return charset;
    }

    /**
     * 设置当前field的字符集编码。
     */
    public void setCharset(String charset) {
        this.charset = charset;
    }

    /**
     * Returns the original filename in the client's filesystem.
     * 
     * @return The original filename in the client's filesystem.
     */
    public String getName() {
        return fileName;
    }

    // ------------------------------------------------------- FileItem methods

    /**
     * Provides a hint as to whether or not the file contents will be read from
     * memory.
     * 
     * @return <code>true</code> if the file contents will be read from memory;
     *         <code>false</code> otherwise.
     */
    public boolean isInMemory() {
        if (cachedContent != null) {
            return true;
        }
        return dfos.isInMemory();
    }

    /**
     * Returns the size of the file.
     * 
     * @return The size of the file, in bytes.
     */
    public long getSize() {
        if (size >= 0) {
            return size;
        } else if (cachedContent != null) {
            return cachedContent.length;
        } else if (dfos.isInMemory()) {
            return dfos.getData().length;
        } else {
            return dfos.getFile().length();
        }
    }

    /**
     * Returns the contents of the file as an array of bytes. If the contents of
     * the file were not yet cached in memory, they will be loaded from the disk
     * storage and cached.
     * 
     * @return The contents of the file as an array of bytes.
     */
    public byte[] get() {
        if (isInMemory()) {
            if (cachedContent == null) {
                cachedContent = dfos.getData();
            }
            return cachedContent;
        }

        byte[] fileData = new byte[(int) getSize()];
        FileInputStream fis = null;

        try {
            fis = new FileInputStream(dfos.getFile());
            fis.read(fileData);
        } catch (IOException e) {
            fileData = null;
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }

        return fileData;
    }

    /**
     * Returns the contents of the file as a String, using the specified
     * encoding. This method uses {@link #get()} to retrieve the contents of the
     * file.
     * 
     * @param charset The charset to use.
     * @return The contents of the file, as a string.
     * @throws UnsupportedEncodingException if the requested character encoding
     *             is not available.
     */
    public String getString(final String charset) throws UnsupportedEncodingException {
        return new String(get(), charset);
    }

    /**
     * 取得字段或文件的内容。
     * <p>
     * 对于form field，将使用传入的<code>charset</code>所指定的字符集编码。
     * </p>
     * <p>
     * 对于文件，使用固定的<code>ISO-8859-1</code>字符集编码。如果想以其它编码来读取文件文本，可使用{@link
     * getString(charset)} 方法。
     * </p>
     * 
     * @return 字段值或文件文本
     */
    public String getString() {
        byte[] rawdata = get();
        String charset = null;

        if (isFormField()) {
            charset = getCharset();
        }

        if (charset == null) {
            charset = DEFAULT_CHARSET;
        }

        try {
            return new String(rawdata, charset);
        } catch (UnsupportedEncodingException e) {
            try {
                return new String(rawdata, DEFAULT_CHARSET);
            } catch (UnsupportedEncodingException ee) {
                return new String(rawdata);
            }
        }
    }

    /**
     * A convenience method to write an uploaded item to disk. The client code
     * is not concerned with whether or not the item is stored in memory, or on
     * disk in a temporary location. They just want to write the uploaded item
     * to a file.
     * <p>
     * This implementation first attempts to rename the uploaded item to the
     * specified destination file, if the item was originally written to disk.
     * Otherwise, the data will be copied to the specified file.
     * <p>
     * This method is only guaranteed to work <em>once</em>, the first time it
     * is invoked for a particular item. This is because, in the event that the
     * method renames a temporary file, that file will no longer be available to
     * copy or rename again at a later time.
     * 
     * @param file The <code>File</code> into which the uploaded item should be
     *            stored.
     * @throws Exception if an error occurs.
     */
    public void write(File file) throws Exception {
        // 自动创建目录
        if (file != null) {
            file.getParentFile().mkdirs();
        }

        if (isInMemory()) {
            FileOutputStream fout = null;
            try {
                fout = new FileOutputStream(file);
                fout.write(get());
            } finally {
                if (fout != null) {
                    fout.close();
                }
            }
        } else {
            File outputFile = getStoreLocation();
            if (outputFile != null) {
                // Save the length of the file
                size = outputFile.length();
                /*
                 * The uploaded file is being stored on disk in a temporary
                 * location so move it to the desired file.
                 */
                if (!outputFile.renameTo(file)) {
                    BufferedInputStream in = null;
                    BufferedOutputStream out = null;
                    try {
                        in = new BufferedInputStream(new FileInputStream(outputFile));
                        out = new BufferedOutputStream(new FileOutputStream(file));
                        IOUtils.copy(in, out);
                    } finally {
                        if (in != null) {
                            try {
                                in.close();
                            } catch (IOException e) {
                                // ignore
                            }
                        }
                        if (out != null) {
                            try {
                                out.close();
                            } catch (IOException e) {
                                // ignore
                            }
                        }
                    }
                }
            } else {
                /*
                 * For whatever reason we cannot write the file to disk.
                 */
                throw new FileUploadException("Cannot write uploaded file to disk!");
            }
        }
    }

    /**
     * Deletes the underlying storage for a file item, including deleting any
     * associated temporary disk file. Although this storage will be deleted
     * automatically when the <code>FileItem</code> instance is garbage
     * collected, this method can be used to ensure that this is done at an
     * earlier time, thus preserving system resources.
     */
    public void delete() {
        cachedContent = null;
        File outputFile = getStoreLocation();
        if (outputFile != null && outputFile.exists()) {
            outputFile.delete();
        }
    }

    /**
     * Returns the name of the field in the multipart form corresponding to this
     * file item.
     * 
     * @return The name of the form field.
     * @see #setFieldName(java.lang.String)
     */
    public String getFieldName() {
        return fieldName;
    }

    /**
     * Sets the field name used to reference this file item.
     * 
     * @param fieldName The name of the form field.
     * @see #getFieldName()
     */
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    /**
     * Determines whether or not a <code>FileItem</code> instance represents a
     * simple form field.
     * 
     * @return <code>true</code> if the instance represents a simple form field;
     *         <code>false</code> if it represents an uploaded file.
     * @see #setFormField(boolean)
     */
    public boolean isFormField() {
        return isFormField;
    }

    /**
     * Specifies whether or not a <code>FileItem</code> instance represents a
     * simple form field.
     * 
     * @param state <code>true</code> if the instance represents a simple form
     *            field; <code>false</code> if it represents an uploaded file.
     * @see #isFormField()
     */
    public void setFormField(boolean state) {
        isFormField = state;
    }

    /**
     * Returns an {@link java.io.OutputStream OutputStream} that can be used for
     * storing the contents of the file.
     * 
     * @return An {@link java.io.OutputStream OutputStream} that can be used for
     *         storing the contensts of the file.
     * @throws IOException if an error occurs.
     */
    public OutputStream getOutputStream() throws IOException {
        if (dfos == null) {
            int sizeThreshold;

            if (keepFormFieldInMemory && isFormField()) {
                sizeThreshold = Integer.MAX_VALUE;
            } else {
                sizeThreshold = this.sizeThreshold;
            }

            dfos = new DeferredFileOutputStream(sizeThreshold, "upload_" + UID, ".tmp", repository);
        }
        return dfos;
    }

    // --------------------------------------------------------- Public methods

    /**
     * Returns the {@link java.io.File} object for the <code>FileItem</code>'s
     * data's temporary location on the disk. Note that for
     * <code>FileItem</code>s that have their data stored in memory, this method
     * will return <code>null</code>. When handling large files, you can use
     * {@link java.io.File#renameTo(java.io.File)} to move the file to new
     * location without copying the data, if the source and destination
     * locations reside within the same logical volume.
     * 
     * @return The data file, or <code>null</code> if the data is stored in
     *         memory.
     */
    public File getStoreLocation() {
        return dfos == null ? null : dfos.getFile();
    }

    // ------------------------------------------------------ Protected methods

    // -------------------------------------------------------- Private methods

    /**
     * Returns a string representation of this object.
     * 
     * @return a string representation of this object.
     */
    @Override
    public String toString() {
        return StringUtil.defaultIfEmpty(getName(), getString());
    }

    // -------------------------------------------------- Serialization methods

    /**
     * Writes the state of this object during serialization.
     * 
     * @param out The stream to which the state should be written.
     * @throws IOException if an error occurs.
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        // Read the data
        if (dfos.isInMemory()) {
            cachedContent = get();
        } else {
            cachedContent = null;
            dfosFile = dfos.getFile();
        }

        // write out values
        out.defaultWriteObject();
    }

    /**
     * Reads the state of this object during deserialization.
     * 
     * @param in The stream from which the state should be read.
     * @throws IOException if an error occurs.
     * @throws ClassNotFoundException if class cannot be found.
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        // read values
        in.defaultReadObject();

        OutputStream output = getOutputStream();
        if (cachedContent != null) {
            output.write(cachedContent);
        } else {
            FileInputStream input = new FileInputStream(dfosFile);
            IOUtils.copy(input, output);
            dfosFile.delete();
            dfosFile = null;
        }
        output.close();

        cachedContent = null;
    }

    /**
     * Returns the file item headers.
     * 
     * @return The file items headers.
     */
    public FileItemHeaders getHeaders() {
        return headers;
    }

    /**
     * Sets the file item headers.
     * 
     * @param pHeaders The file items headers.
     */
    public void setHeaders(FileItemHeaders pHeaders) {
        headers = pHeaders;
    }
}
