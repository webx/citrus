/*
 * Copyright (c) 2002-2012 Alibaba Group Holding Limited.
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
 */

package com.alibaba.citrus.test.context;

import static com.alibaba.citrus.test.TestEnvStatic.*;
import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.alibaba.citrus.service.resource.support.context.ResourceLoadingXmlApplicationContext;
import com.alibaba.citrus.util.ClassUtil;
import com.alibaba.citrus.util.io.StreamUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * ContextLoader的基类。
 *
 * @author Michael Zhou
 */
public abstract class AbstractContextLoader extends org.springframework.test.context.support.AbstractContextLoader {
    protected final        Logger             log                = LoggerFactory.getLogger(getClass());
    protected final static ApplicationContext testResourceLoader = getTestResourceLoader();

    /** 取得可装载测试环境的资源的resource loader。 */
    private static ApplicationContext getTestResourceLoader() {
        try {
            System.setProperty("test.srcdir", srcdir.getAbsolutePath());
            System.setProperty("test.destdir", destdir.getAbsolutePath());

            Resource testResourceConfig = new ClassPathResource(
                    ClassUtil.getResourceNameForPackage(SpringextContextLoader.class) + "/test-resources.xml");

            return new ResourceLoadingXmlApplicationContext(testResourceConfig);
        } finally {
            System.clearProperty("test.srcdir");
            System.clearProperty("test.destdir");
        }
    }

    @Override
    protected final String[] generateDefaultLocations(Class<?> clazz) {
        assertNotNull(clazz, "Class must not be null");

        String location = "/" + toCamelCase(clazz.getSimpleName())
                          + assertNotNull(trimToNull(getResourceSuffix()), "Resource suffix must not be empty");

        if (isGenerateContextConfigurations()) {
            File configLocation = new File(srcdir, location);

            if (!configLocation.exists()) {
                try {
                    configLocation.getParentFile().mkdirs();

                    OutputStream os = new FileOutputStream(configLocation);
                    InputStream is = getClass().getResourceAsStream("context-template.xml");

                    StreamUtil.io(is, os, true, true);

                    log.warn("Generated context configuration file: " + configLocation.getAbsolutePath());
                } catch (IOException e) {
                    log.warn("Could not generate context configuration file: " + configLocation.getAbsolutePath(), e);
                }
            }
        }

        return new String[] { location };
    }

    @Override
    protected final String[] modifyLocations(Class<?> clazz, String... locations) {
        return locations;
    }

    /** 如果默认的配置文件不存在，是否生成样本？ */
    protected boolean isGenerateContextConfigurations() {
        return true;
    }

    @Override
    protected String getResourceSuffix() {
        return "-context.xml";
    }
}
