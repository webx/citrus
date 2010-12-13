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

import static com.alibaba.citrus.util.CollectionUtil.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

/**
 * ×°ÔØcharsetÎÄ¼þ¡£
 * 
 * @author Michael Zhou
 */
public class CharsetLoader extends CharsetTool {
    public char[] load(String dumpfile) throws IOException {
        return load(dumpfile, OUTPUT_CHARSET.name());
    }

    public char[] load(String dumpfile, String charset) throws IOException {
        File srcfile = getFile(dumpfile);

        System.out.println("Load " + srcfile.getAbsolutePath());

        BufferedReader in = null;
        char[] codeTable = new char[65536];

        int converted = 0;
        int blocks = 0;
        int chars = 0;
        int maxBlockSize = 0;

        try {
            in = new BufferedReader(new InputStreamReader(new FileInputStream(srcfile), charset));

            String line;

            while ((line = in.readLine()) != null) {
                CharsetBlock block = null;

                if (line.startsWith("+")) {
                    block = CharsetBlock.parseHeader(line);
                    assertTrue(block != null);

                    chars += block.getLength();
                    maxBlockSize = Math.max(maxBlockSize, block.getLength());
                    blocks++;

                    System.out.println("Read block: " + block);

                    String[] strs = readBlock(in, block);
                    int j = 0;
                    boolean blockErrorNotified = false;

                    for (int i = block.getStart(); i < block.getEnd(); i++, j++) {
                        String str = strs[j];

                        if (str.length() != 1 || str.charAt(0) == '?' || str.charAt(0) == 0xFFFD
                                || str.charAt(0) == 0x25A1) {
                            if (!blockErrorNotified) {
                                blockErrorNotified = true;
                                System.out.flush();
                                System.err.flush();
                                System.err.println("Error at " + block + ": char=\"" + str + "\"");
                                System.err.flush();
                            }

                            continue;
                        }

                        char ch = str.charAt(0);

                        if (i != ch) {
                            codeTable[i] = ch;
                            converted++;
                        }
                    }
                }

                if (line.startsWith("-")) {
                    System.out.println("Read: " + line);
                }
            }
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
        }

        System.out.println("- Total blocks:    " + blocks);
        System.out.println("- Max block size: " + maxBlockSize);
        System.out.println("- Total chars:    " + chars);
        System.out.println("- Total " + converted + " converted.");

        return codeTable;
    }

    private String[] readBlock(BufferedReader in, CharsetBlock block) throws IOException {
        String line;
        List<String> strs = createArrayList(block.getLength());

        while ((line = in.readLine()) != null) {
            if (line.length() == 0) {
                break;
            }

            strs.addAll(Arrays.asList(line.split(SEP_CHAR)));
        }

        assertTrue(strs.size() == block.getLength());

        return strs.toArray(new String[strs.size()]);
    }
}
