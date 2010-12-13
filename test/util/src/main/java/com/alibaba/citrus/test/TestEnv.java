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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map;

import com.alibaba.citrus.logconfig.LogConfigurator;

/**
 * 用来初始化测试环境的辅助类。
 * <p>
 * 单元测试要尽可能独立于其它项目。该辅助类帮助单元测试从当前项目的相对路径中取得所有资源文件，包括：
 * </p>
 * <ul>
 * <li>测试根目录<code>basedir</code>，默认为项目根目录。</li>
 * <li>测试源文件目录<code>srcdir</code>，默认为<code>${basedir}/src/test/config/</code>。</li>
 * <li>测试目标目录<code>destdir</code>，默认为<code>${basedir}/target/test/</code>。</li>
 * <li>Logback日志文件，默认为<code>${srcdir}/logback.xml</code>，假如找不到，则使用默认的配置：
 * <ul>
 * <li><code>WARN</code>以下打印到<code>System.out</code>。</li>
 * <li><code>WARN</code>以上打印到<code>System.err</code>。</li>
 * <li>所有日志同时打印到<code>${destdir}/test.log</code>。</li>
 * </ul>
 * </li>
 * </ul>
 * 
 * @author Michael Zhou
 */
public class TestEnv {
    private Params params = createParams();
    private File basedir;
    private File srcdir;
    private File destdir;

    public TestEnv setBasedir(String basedir) {
        getParams().basedirParam = basedir;
        return this;
    }

    public TestEnv setSrcdir(String srcdir) {
        getParams().srcdirParam = srcdir;
        return this;
    }

    public TestEnv setDestdir(String destdir) {
        getParams().destdirParam = destdir;
        return this;
    }

    public TestEnv setLogbackConfig(String logbackConfig) {
        getParams().logbackConfigParam = logbackConfig;
        return this;
    }

    public TestEnv setInitFailure(Exception initFailure) {
        getParams().initFailure = initFailure;
        return this;
    }

    public TestEnv init() {
        Params params = getParams();

        if (params.inited) {
            return this;
        }

        params.inited = true;

        try {
            setupDirectories();

            URL logConfigFile = null;

            if (getParams().logbackConfigParam != null) {
                logConfigFile = findLogbackXml();
            }

            System.out.println("+-----------------------------------------------------------------------------");

            setupLogback(logConfigFile);

        } catch (Exception e) {
            params.initFailure = e;
        }

        return this;
    }

    /**
     * 初始化basedir, srcdir和destdir等。
     */
    private void setupDirectories() throws IOException {
        Params params = getParams();

        basedir = new File(params.basedirParam).getCanonicalFile();
        srcdir = new File(basedir, params.srcdirParam);
        destdir = new File(basedir, params.destdirParam);

        if (!destdir.exists()) {
            destdir.mkdirs();
        }

        if (!destdir.isDirectory() || !destdir.exists()) {
            throw new IllegalArgumentException("Destination directory does not exist: " + destdir);
        }

        System.out.println("+-----------------------------------------------------------------------------");
        System.out.println("| Set base dir to:          " + basedir);
        System.out.println("| Set source dir to:        " + srcdir);
        System.out.println("| Set destination dir to:   " + destdir);
    }

    /**
     * 初始化logback.xml，可用${basedir}, ${srcdir}, ${destdir}。
     */
    private void setupLogback(URL logConfigFile) throws IOException {
        if (logConfigFile == null) {
            return;
        }

        LogConfigurator configurator = LogConfigurator.getConfigurator("logback");
        Map<String, String> props = configurator.getDefaultProperties(false);

        props.put("basedir", basedir.getAbsolutePath());
        props.put("srcdir", srcdir.getAbsolutePath());
        props.put("destdir", destdir.getAbsolutePath());
        props.put("loggingRoot", destdir.getAbsolutePath());

        configurator.configure(logConfigFile, props);
    }

    private URL findLogbackXml() throws IOException {
        File test = new File(srcdir, getParams().logbackConfigParam);
        URL logbackXml;

        if (test.exists()) {
            logbackXml = test.toURI().toURL();
        } else {
            URL testURL = getClass().getResource("logback-test-default.xml");

            if (testURL == null) {
                throw new IllegalArgumentException("missing logback-test-default.xml");
            }

            logbackXml = testURL;
        }

        System.out.println("| Initializing log system:  " + logbackXml.toExternalForm());

        return logbackXml;
    }

    public File getBasedir() {
        assertInited();
        return basedir;
    }

    public File getSrcdir() {
        assertInited();
        return srcdir;
    }

    public File getDestdir() {
        assertInited();
        return destdir;
    }

    protected final void assertInited() {
        if (params == null) {
            return;
        }

        if (!params.inited) {
            throw new IllegalStateException("Not inited yet!");
        }

        Exception initFailure = params.initFailure;

        params = null;

        if (initFailure != null) {
            if (initFailure instanceof RuntimeException) {
                throw (RuntimeException) initFailure;
            } else {
                throw new RuntimeException(initFailure);
            }
        }
    }

    /**
     * 取得初始化参数。
     * <p>
     * 在初始化完成以后，将返回<code>null</code>。
     * </p>
     */
    protected final Params getParams() {
        if (params == null) {
            throw new IllegalStateException();
        }

        return params;
    }

    /**
     * 创建初始化参数。
     * <p>
     * 子类可扩展该参数。
     * </p>
     */
    protected Params createParams() {
        return new Params();
    }

    /**
     * 初始化参数。
     */
    protected class Params {
        public String basedirParam = ".";
        public String srcdirParam = "src/test/config/";
        public String destdirParam = "target/test/";
        public String logbackConfigParam = "logback.xml"; // 相对于srcdir
        public Exception initFailure = null;
        public boolean inited = false;
    }
}
