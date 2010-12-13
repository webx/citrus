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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

/**
 * 将charset倒到一个文件中，以便用第三方工具转换编码。
 * 
 * @author Michael Zhou
 */
public class CharsetDumper extends CharsetTool {
    public static void main(String[] args) throws Exception {
        CharsetDumper dumper = new CharsetDumper();

        dumper.dump("gbk.txt");
    }

    private Charset charset;
    private CharsetEncoder encoder;
    private int startChar;
    private int endChar;

    public CharsetDumper() {
        this("GBK", 0x4E00, 0xFA30);
    }

    public CharsetDumper(String charsetName, int startChar, int endChar) {
        this.charset = Charset.forName(charsetName);
        this.encoder = charset.newEncoder();
        this.startChar = Math.max(startChar, MIN_CHAR);
        this.endChar = Math.min(endChar, MAX_CHAR);
    }

    public void dump(String dumpfile) throws IOException {
        File destfile = getFile(dumpfile);

        System.out.println("Dump to " + destfile.getAbsolutePath());

        Writer out = null;

        int blocks = 0;
        int chars = 0;
        int maxBlockSize = 0;

        try {
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(destfile), OUTPUT_CHARSET));

            CharsetBlock block = new CharsetBlock();
            int i = startChar;

            while ((block = nextBlock(i)) != null) {
                chars += block.getLength();
                maxBlockSize = Math.max(maxBlockSize, block.getLength());
                blocks++;

                out.write(block + NEW_LINE);

                i = block.getEnd();

                int count = 0;

                for (int j = block.getStart(); j < block.getEnd(); j++, count++) {
                    out.write((char) j);
                    out.write(SEP_CHAR);

                    if (count % LINE_LENGTH == LINE_LENGTH - 1 || j == block.getEnd() - 1) {
                        out.write(NEW_LINE);
                    }
                }

                out.write(NEW_LINE);
            }

            out.write("- Charset:        " + charset.name() + NEW_LINE);
            out.write("- Blocks:         " + blocks + NEW_LINE);
            out.write("- Max block size: " + maxBlockSize + NEW_LINE);
            out.write("- Total chars:    " + chars + NEW_LINE);
            out.flush();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                }
            }
        }
    }

    private CharsetBlock nextBlock(int i) {
        while (i < endChar) {
            if (encoder.canEncode((char) i)) {
                break;
            }

            i++;
        }

        if (i >= endChar) {
            return null;
        }

        CharsetBlock block = new CharsetBlock();

        block.setStart(i);

        do {
            i++;
        } while (i < endChar && encoder.canEncode((char) i));

        block.setEnd(i);

        return block;
    }
}
