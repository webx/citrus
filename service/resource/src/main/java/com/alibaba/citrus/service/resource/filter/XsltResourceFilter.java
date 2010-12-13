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
package com.alibaba.citrus.service.resource.filter;

import static com.alibaba.citrus.service.resource.ResourceLoadingService.*;
import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Set;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.citrus.service.resource.Resource;
import com.alibaba.citrus.service.resource.ResourceFilter;
import com.alibaba.citrus.service.resource.ResourceFilterChain;
import com.alibaba.citrus.service.resource.ResourceLoadingOption;
import com.alibaba.citrus.service.resource.ResourceLoadingService;
import com.alibaba.citrus.service.resource.ResourceMatchResult;
import com.alibaba.citrus.service.resource.ResourceNotFoundException;
import com.alibaba.citrus.service.resource.support.ByteArrayResource;
import com.alibaba.citrus.service.resource.support.FileResource;
import com.alibaba.citrus.util.StringUtil;
import com.alibaba.citrus.util.io.StreamUtil;

/**
 * 将xml resource变换格式的filter。
 * 
 * @author Michael Zhou
 */
public class XsltResourceFilter implements ResourceFilter {
    private static final Logger log = LoggerFactory.getLogger(XsltResourceFilter.class);
    private ResourceLoadingService resourceLoadingService;
    private String xslt;
    private String saveTo;
    private File saveToDir;
    private boolean failIfNotFound = true;

    public void setXslt(String xslt) {
        this.xslt = trimToNull(xslt);
    }

    public void setFailIfNotFound(boolean failIfNotFound) {
        this.failIfNotFound = failIfNotFound;
    }

    public void setSaveTo(String saveTo) {
        this.saveTo = StringUtil.trimToNull(saveTo);
    }

    public void init(ResourceLoadingService resourceLoadingService) {
        this.resourceLoadingService = assertNotNull(resourceLoadingService, "resourceLoadingService");

        assertNotNull(xslt, "missing xslt");
    }

    private File getSaveToDir() {
        if (saveToDir != null) {
            return saveToDir;
        }

        if (saveTo == null) {
            return null;

        }

        File dir;

        try {
            dir = resourceLoadingService.getResourceAsFile(saveTo, FOR_CREATE);

            if (!dir.exists()) {
                dir.mkdirs();
            }
        } catch (ResourceNotFoundException e) {
            throw new IllegalArgumentException("Specified saveTo dir is not exist: " + saveTo, e);
        }

        if (!dir.exists()) {
            throw new IllegalArgumentException("Specified saveTo dir is not exist: " + saveTo);
        }

        saveToDir = dir;

        log.debug("Transformed resource will be saved into directory: {}", dir.getAbsolutePath());

        return saveToDir;
    }

    public Resource doFilter(ResourceMatchResult filterMatchResult, Set<ResourceLoadingOption> options,
                             ResourceFilterChain chain) throws ResourceNotFoundException {
        String resourceName = filterMatchResult.getResourceName();
        String xsltName = filterMatchResult.substitute(xslt);
        Resource xsltRes;

        try {
            xsltRes = resourceLoadingService.getResource(xsltName);
        } catch (ResourceNotFoundException e) {
            xsltRes = null;
        }

        if (xsltRes == null && failIfNotFound) {
            throw new ResourceNotFoundException("Could not find XSLT file for " + this + ", resourceName="
                    + filterMatchResult.getResourceName());
        }

        Resource resource = chain.doFilter(filterMatchResult, options);

        resource = transformDocument(resource, resourceName, xsltRes, xsltName);

        if (getSaveToDir() != null) {
            resource = saveToDir(resource, resourceName);
        }

        return resource;
    }

    /**
     * 用XSLT转换XML资源。
     */
    private Resource transformDocument(Resource xmlRes, String xmlResName, Resource xsltRes, String xsltResName)
            throws ResourceNotFoundException {
        log.debug("Applying XSLT \"{}\" to resource \"{}\"", xsltResName, xmlResName);

        try {
            TransformerFactory factory = TransformerFactory.newInstance();
            Source xsltSource = new StreamSource(xsltRes.getInputStream(), xsltResName);
            Transformer transformer = factory.newTransformer(xsltSource);

            Source source = new StreamSource(xmlRes.getInputStream(), xmlResName);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Result result = new StreamResult(baos);

            transformer.transform(source, result);

            return new ByteArrayResource(baos.toByteArray());
        } catch (Exception e) {
            throw new ResourceNotFoundException("Could not apply XSLT \"" + xsltResName + "\" to resource \""
                    + xmlResName + "\"", e);
        }
    }

    /**
     * 将资源保存到目录中。
     */
    private Resource saveToDir(Resource resource, String resourceName) {
        File fileToSave = new File(saveToDir, resourceName);
        File parentDir = fileToSave.getParentFile();

        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }

        if (!parentDir.exists()) {
            log.warn("Directory to save is not exist: " + parentDir.getAbsolutePath());
            return resource;
        }

        FileOutputStream fos = null;

        try {
            fos = new FileOutputStream(fileToSave);
            StreamUtil.io(resource.getInputStream(), fos, true, true);
        } catch (IOException e) {
            log.warn("Could not save to file: " + fileToSave.getAbsolutePath(), e);
            return resource;
        }

        Resource transformed = new FileResource(fileToSave);

        if (log.isDebugEnabled()) {
            log.debug("Transformed resource is saved to " + fileToSave.getAbsolutePath());
        }

        return transformed;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + xslt + "]";
    }
}
