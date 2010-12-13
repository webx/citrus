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
/***
 * ASM tests
 * Copyright (c) 2002-2005 France Telecom
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holders nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.alibaba.citrus.asm;

import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.alibaba.citrus.asm.util.TraceClassVisitor;

/**
 * Super class for test suites based on a jar file.
 * 
 * @author Eugene Kuleshov
 * @author Eric Bruneton
 */
public abstract class AbstractTest extends TestCase {

    protected String n;

    private URL url;

    public AbstractTest() {
        super("test");
    }

    protected void init(final String n, final URL url) {
        this.n = n;
        this.url = url;
    }

    protected InputStream openStream() {
        try {
            return url.openStream();
        } catch (Exception e) {
            throw new RuntimeException("Failed to open: " + url);
        }
    }

    protected TestSuite getSuite() throws Exception {
        return getSuite(new AsmTestParams());
    }

    protected TestSuite getSuite(AsmTestParams params) throws Exception {
        String suiteName = getClass().getName();
        if (params.getParts() > 1) {
            suiteName += "-" + params.getPart();
        }
        System.out.println("Creating suite: " + suiteName);
        TestSuite suite = new TestSuite(suiteName);
        File[] files = params.getFiles();
        String clazz = params.getInclude();
        String excludeClazz = params.getExclude();
        int parts = params.getParts();
        int part = params.getPart();
        int maxCount = params.getMaxCount();
        int id = 0;
        for (File f : files) {
            if (f.isDirectory()) {
                scanDirectory("", f, suite, clazz, excludeClazz);
            } else {
                String baseurl = "jar:" + f.toURI().toURL().toExternalForm() + "!/";
                ZipFile zip = new ZipFile(f);
                Enumeration entries = zip.entries();
                while (entries.hasMoreElements() && (maxCount <= 0 || id < maxCount)) {
                    ZipEntry e = (ZipEntry) entries.nextElement();
                    String n = e.getName();
                    String p = n.replace('/', '.');
                    if (n.endsWith(".class") && (clazz == null || p.indexOf(clazz) != -1)
                            && (excludeClazz == null || p.indexOf(excludeClazz) == -1)) {
                        n = p.substring(0, p.length() - 6);
                        if (id % parts == part) {
                            AbstractTest t = getClass().newInstance();
                            URL url = new URL(baseurl + e.getName());
                            t.init(n, url);
                            suite.addTest(t);
                        }
                        ++id;
                    }
                }
                zip.close();
            }
        }
        return suite;
    }

    private void scanDirectory(final String path, final File f, final TestSuite suite, final String clazz,
                               final String excludeClazz) throws Exception {
        File[] fs = f.listFiles();
        for (int i = 0; i < fs.length; ++i) {
            String n = fs[i].getName();
            String qn = path.length() == 0 ? n : path + "." + n;
            if (fs[i].isDirectory()) {
                scanDirectory(qn, fs[i], suite, clazz, excludeClazz);
            } else if (qn.endsWith(".class") && (clazz == null || qn.indexOf(clazz) != -1)
                    && (excludeClazz == null || qn.indexOf(excludeClazz) == -1)) {
                qn = qn.substring(0, qn.length() - 6);
                AbstractTest t = getClass().newInstance();
                t.init(qn, fs[i].toURI().toURL());
                suite.addTest(t);
            }
        }
    }

    public abstract void test() throws Exception;

    public void assertEquals(final ClassReader cr1, final ClassReader cr2) throws Exception {
        assertEquals(cr1, cr2, null, null);
    }

    public void assertEquals(final ClassReader cr1, final ClassReader cr2, final ClassAdapter filter1,
                             final ClassAdapter filter2) throws Exception {
        if (!Arrays.equals(cr1.b, cr2.b)) {
            StringWriter sw1 = new StringWriter();
            StringWriter sw2 = new StringWriter();
            ClassVisitor cv1 = new TraceClassVisitor(new PrintWriter(sw1));
            ClassVisitor cv2 = new TraceClassVisitor(new PrintWriter(sw2));
            if (filter1 != null) {
                filter1.cv = cv1;
            }
            if (filter2 != null) {
                filter2.cv = cv2;
            }
            cr1.accept(filter1 == null ? cv1 : filter1, 0);
            cr2.accept(filter2 == null ? cv2 : filter2, 0);
            String s1 = sw1.toString();
            String s2 = sw2.toString();
            assertEquals("different data", s1, s2);
        }
    }

    @Override
    public String getName() {
        return super.getName() + ": " + n;
    }
}
