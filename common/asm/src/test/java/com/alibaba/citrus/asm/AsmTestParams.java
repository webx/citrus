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
package com.alibaba.citrus.asm;

import static com.alibaba.citrus.test.TestEnvStatic.*;
import static com.alibaba.citrus.test.TestUtil.*;

import java.io.File;
import java.io.IOException;

import com.alibaba.citrus.asm.test.cases.Generator;
import com.alibaba.citrus.test.TestUtil;

/**
 * 帮助asm测试。
 * 
 * @author Michael Zhou
 */
public class AsmTestParams {
    private final static File cases = TestUtil.getClassesDir(AsmTestParams.class);
    private final static File output = new File(destdir, "output");
    private final static File[] defaultFiles;
    private final static String defaultInclude;
    private final static String defaultExclude;
    private File[] files = defaultFiles;
    private String include = defaultInclude;
    private String exclude = defaultExclude;
    private int parts = 1;
    private int part = 0;
    private int maxCount = 5000;

    static {
        File testJar = new File(getJavaHome(), "jre/lib/rt.jar");

        if (!testJar.exists()) {
            testJar = new File(getJavaHome(), "../Classes/classes.jar"); // mac style

            if (!testJar.exists()) {
                throw new IllegalArgumentException("could not find jar file: jre/lib/rt.jar or classes.jar");
            }
        }

        defaultFiles = new File[] { testJar, cases };
        defaultInclude = "";
        defaultExclude = AsmTestParams.class.getPackage().getName();

        // 生成测试用的class文件
        try {
            Generator.main(new String[] { cases.getAbsolutePath() });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public AsmTestParams() {
        String maxCount = System.getProperty("maxCount");

        if (maxCount != null) {
            try {
                this.maxCount = Integer.parseInt(maxCount);
            } catch (NumberFormatException e) {
            }
        }
    }

    public AsmTestParams(String clazz) {
        this();
        this.include = clazz;
    }

    public AsmTestParams(int parts, int part) {
        this();
        this.parts = parts;
        this.part = part;
    }

    public File getOutput() {
        return output;
    }

    public String getClassPath(String relativePath) {
        return new File(cases, relativePath).getAbsolutePath();
    }

    public File[] getFiles() {
        return files;
    }

    public String getInclude() {
        return include;
    }

    public String getExclude() {
        return exclude;
    }

    public int getParts() {
        return parts;
    }

    public int getPart() {
        return part;
    }

    public int getMaxCount() {
        return maxCount;
    }
}
