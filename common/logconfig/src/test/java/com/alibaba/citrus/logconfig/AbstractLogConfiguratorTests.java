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
package com.alibaba.citrus.logconfig;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractLogConfiguratorTests {
    private PrintStream savedOut;
    private PrintStream savedErr;
    private ByteArrayOutputStream outBytes;
    private ByteArrayOutputStream errBytes;
    protected String out;
    protected String err;

    protected final String getStdout() {
        System.out.flush();

        try {
            return new String(outBytes.toByteArray(), "GBK");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } finally {
            outBytes.reset();
        }
    }

    protected final String getStderr() {
        System.err.flush();

        try {
            return new String(errBytes.toByteArray(), "GBK");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } finally {
            errBytes.reset();
        }
    }

    protected void log() {
        Logger log = LoggerFactory.getLogger("TestLog");

        log.trace("test trace");
        log.debug("test debug");
        log.info("test info");
        log.warn("test warn");
        log.error("test error");
    }

    protected final void invokeInLoader(String envLogSystem, String testMethodName, Object... args) throws Exception {
        ClassLoader cl = getClassLoader(envLogSystem);
        int argCount;

        if (args == null || args.length == 0) {
            argCount = 0;
        } else {
            argCount = args.length;
        }

        Class<?> testClass = cl.loadClass(getClass().getName());
        Class<?> thisClass = cl.loadClass(AbstractLogConfiguratorTests.class.getName());
        Method testMethod = null;

        for (Method m : testClass.getDeclaredMethods()) {
            if (m.getName().equals(testMethodName) && m.getParameterTypes().length == argCount) {
                testMethod = m;
                break;
            }
        }

        assertNotNull(testMethod);
        testMethod.setAccessible(true);

        assertFalse(testMethod.toString(), Modifier.isPublic(testMethod.getModifiers())); // 防止错误地调用public方法

        Method initStdoutStderr = thisClass.getDeclaredMethod("initStdoutStderr");
        Method restoreStdoutStderr = thisClass.getDeclaredMethod("restoreStdoutStderr");

        initStdoutStderr.setAccessible(true);
        restoreStdoutStderr.setAccessible(true);

        ClassLoader saved = Thread.currentThread().getContextClassLoader();
        Object testObject = null;
        String[] stdOutErr = { null, null };

        try {
            Thread.currentThread().setContextClassLoader(cl);
            testObject = testClass.newInstance();
            initStdoutStderr.invoke(testObject);
            testMethod.invoke(testObject, args);
        } catch (InvocationTargetException e) {
            Throwable t = e.getTargetException();

            if (t instanceof Exception) {
                throw (Exception) t;
            } else {
                throw (Error) t;
            }
        } finally {
            if (testObject != null) {
                stdOutErr = (String[]) restoreStdoutStderr.invoke(testObject);
            }

            Thread.currentThread().setContextClassLoader(saved);

            out = stdOutErr[0];
            err = stdOutErr[1];
        }
    }

    private ClassLoader getClassLoader(String envLogSystems) throws Exception {
        LinkedHashSet<URL> classpath = new LinkedHashSet<URL>();
        Map<String, URL> urls = new HashMap<String, URL>();

        for (String path : System.getProperty("java.class.path").split(File.pathSeparator)) {
            if (path.contains("logback-classic")) {
                urls.put("logback", new File(path).toURI().toURL());
            } else if (path.contains("slf4j-log4j12")) {
                urls.put("log4j", new File(path).toURI().toURL());
            } else {
                classpath.add(new File(path).toURI().toURL());
            }
        }

        if (envLogSystems != null) {
            // 依次加入provider jars。
            for (String envLogSystem : envLogSystems.split("(,|\\s)+")) {
                envLogSystem = LogConfigurator.trimToNull(envLogSystem);

                if (envLogSystem != null && urls.containsKey(envLogSystem)) {
                    classpath.add(urls.get(envLogSystem));
                }
            }
        }

        return new URLClassLoader(classpath.toArray(new URL[classpath.size()]), null);
    }

    @SuppressWarnings("unused")
    private void initStdoutStderr() throws Exception {
        savedOut = System.out;
        savedErr = System.err;

        outBytes = new ByteArrayOutputStream();
        errBytes = new ByteArrayOutputStream();

        System.setOut(new PrintStream(outBytes, true, "GBK"));
        System.setErr(new PrintStream(errBytes, true, "GBK"));
    }

    @SuppressWarnings("unused")
    private String[] restoreStdoutStderr() {
        String out = getStdout();
        String err = getStderr();

        System.setOut(savedOut);
        System.setErr(savedErr);

        return new String[] { out, err };
    }
}
