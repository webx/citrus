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
package com.alibaba.citrus.service.template.impl;

import static com.alibaba.citrus.test.TestEnvStatic.*;
import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.StringUtil.*;
import static com.alibaba.citrus.util.io.StreamUtil.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Writer;

import com.alibaba.citrus.service.template.TemplateContext;
import com.alibaba.citrus.service.template.TemplateEngine;
import com.alibaba.citrus.service.template.TemplateException;
import com.alibaba.citrus.util.FileUtil;
import com.alibaba.citrus.util.io.ByteArrayInputStream;

public class SimpleEngine implements TemplateEngine {
    private String basedir;
    private String[] exts;

    public void setBasedir(String basedir) {
        this.basedir = basedir;
    }

    public void setExtensions(String exts) {
        this.exts = exts.split(",");
    }

    public String[] getDefaultExtensions() {
        return exts;
    }

    public boolean exists(String templateName) {
        return getTemplateFile(templateName).exists();
    }

    public String getText(String templateName, TemplateContext context) throws TemplateException, IOException {
        return readText(getInputStream(templateName), null, true);
    }

    public void writeTo(String templateName, TemplateContext context, OutputStream ostream) throws TemplateException,
            IOException {
        io(getInputStream(templateName), ostream, true, true);
    }

    public void writeTo(String templateName, TemplateContext context, Writer writer) throws TemplateException,
            IOException {
        io(new InputStreamReader(getInputStream(templateName)), writer, true, true);
    }

    private File getTemplateFile(String templateName) {
        return new File(srcdir, "templates/" + trimToEmpty(basedir) + "/" + templateName);
    }

    /**
     * 返回相对路径的字节流。
     */
    private InputStream getInputStream(String templateName) throws IOException {
        File templateFile = getTemplateFile(templateName).getCanonicalFile();

        assertTrue(templateFile.exists(), templateFile.getAbsolutePath() + " not exist");

        String relativePath = FileUtil.normalizeAbsolutePath(templateFile.getAbsolutePath().substring(
                new File(srcdir, "templates").getCanonicalPath().length()));

        return new ByteArrayInputStream(relativePath.getBytes());
    }

    @Override
    public String toString() {
        return "SimpleEngine[" + basedir + "]";
    }
}
