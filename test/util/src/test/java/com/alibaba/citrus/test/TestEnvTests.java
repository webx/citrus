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

import static org.junit.Assert.*;

import java.io.File;

import org.hamcrest.Matchers;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ≤‚ ‘<code>TestEnv</code>°£
 * 
 * @author Michael Zhou
 */
public class TestEnvTests {
    private static TestEnv env;

    @BeforeClass
    public static void init() {
        env = new TestEnv().init();
    }

    @Test(expected = IllegalStateException.class)
    public void notInited() {
        new TestEnv().getSrcdir();
    }

    @Test
    public void basedir() throws Exception {
        assertEquals(new File("").getCanonicalFile(), env.getBasedir());
    }

    @Test
    public void srcdir() throws Exception {
        assertThat(env.getSrcdir().toURI().toURL().toExternalForm(), Matchers.endsWith("src/test/config/"));
        assertTrue("file exists", new File(env.getSrcdir(), "dummy.txt").exists());
    }

    @Test
    public void destdir() throws Exception {
        assertThat(env.getDestdir().toURI().toURL().toExternalForm(), Matchers.endsWith("target/test/"));
    }

    @Test
    public void log() {
        Logger log = LoggerFactory.getLogger(TestEnvTests.class);

        log.trace("trace message");
        log.debug("debug message");
        log.info("info message");
        log.warn("warn message");
        log.error("error message");
    }
}
