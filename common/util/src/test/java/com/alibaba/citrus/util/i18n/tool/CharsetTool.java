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
package com.alibaba.citrus.util.i18n.tool;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.text.ParseException;

import com.alibaba.citrus.util.MessageUtil;

/**
 * 有关Charset工具的基类。
 * 
 * @author Michael Zhou
 */
public abstract class CharsetTool {
    protected static final int MIN_CHAR = 128;
    protected static final int MAX_CHAR = 65536;
    protected static final String BLOCK_HEAD = "+ {0} - {1} ({2} chars)";
    protected static final String SEP_CHAR = ";";
    protected static final Charset OUTPUT_CHARSET = Charset.forName("GB18030");
    protected static final String NEW_LINE = "\r\n";
    protected static final int LINE_LENGTH = 20;
    protected static final File srcdir;
    protected static final File destdir;

    static {
        try {
            // 设置目录
            File basedir = new File("").getCanonicalFile();

            System.setProperty("project.home", basedir.getAbsolutePath());

            srcdir = new File(basedir, "src/conf.test");
            destdir = new File(basedir, "target/test");

            if (!srcdir.isDirectory() || !srcdir.exists()) {
                throw new RuntimeException("Source directory does not exist: " + srcdir);
            }

            destdir.mkdirs();

            if (!destdir.isDirectory() || !destdir.exists()) {
                throw new RuntimeException("Destination directory does not exist: " + destdir);
            }

            System.out.println("Set base dir to:        " + basedir);
            System.out.println("Set source dir to:      " + srcdir);
            System.out.println("Set destination dir to: " + destdir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected static File getFile(String filename) {
        return new File(srcdir, filename);
    }

    protected static String hex(int i) {
        String value = Integer.toHexString(i).toUpperCase();
        int length = value.length();

        switch (length) {
            case 1:
                return "000" + value;

            case 2:
                return "00" + value;

            case 3:
                return "0" + value;

            case 4:
                return value;

            default:
                throw new IllegalArgumentException(String.valueOf(value));
        }
    }

    protected static void assertTrue(boolean condition) {
        if (!condition) {
            throw new AssertionError();
        }
    }

    protected static class CharsetBlock {
        private int start;
        private int end;

        public static CharsetBlock parseHeader(String line) {
            try {
                Object[] params = new MessageFormat(BLOCK_HEAD).parse(line);
                CharsetBlock block = new CharsetBlock();

                block.setStart(Integer.parseInt((String) params[0], 16));
                block.setEnd(Integer.parseInt((String) params[1], 16) + 1);

                return block;
            } catch (NumberFormatException e) {
                return null;
            } catch (ParseException e) {
                return null;
            }
        }

        public int getStart() {
            return start;
        }

        public void setStart(int start) {
            this.start = start;
        }

        public int getEnd() {
            return end;
        }

        public void setEnd(int end) {
            this.end = end;
        }

        public int getLength() {
            return end - start;
        }

        @Override
        public String toString() {
            return MessageUtil.formatMessage(BLOCK_HEAD, hex(getStart()), hex(getEnd() - 1), new Integer(getLength()));
        }
    }
}
