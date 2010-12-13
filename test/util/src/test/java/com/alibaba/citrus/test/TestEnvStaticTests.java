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
package com.alibaba.citrus.test;

import static com.alibaba.citrus.test.TestEnvStatic.*;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <code>TestEnvStatic</code>Ê¾Àý¡£
 * 
 * @author Michael Zhou
 */
public class TestEnvStaticTests {
    private static final Logger log = LoggerFactory.getLogger(TestEnvStaticTests.class);

    @Test
    public void printAll() {
        log.info(" basedir: " + basedir);
        log.info("  srcdir: " + srcdir);
        log.info(" destdir: " + destdir);
        log.info("javaHome: " + javaHome);
    }
}
