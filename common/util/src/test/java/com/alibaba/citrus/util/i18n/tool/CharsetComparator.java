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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import com.alibaba.citrus.util.MessageUtil;

/**
 * 比较两个dump文件的差异。
 * 
 * @author Michael Zhou
 */
public class CharsetComparator extends CharsetTool {
    public static void main(String[] args) throws Exception {
        char[] table1 = new CharsetLoader().load("gbk_trad_word.txt");
        char[] table2 = new CharsetLoader().load("gbk_trad_fjds.txt");

        new CharsetComparator().compare(table1, "Word", table2, "Fjds", "word_fj_compare.txt");
    }

    public void compare(char[] table1, String name1, char[] table2, String name2, String resultfile) throws IOException {
        File destfile = getFile(resultfile);

        System.out.println(MessageUtil.formatMessage("Compare {0} and {1}", name1, name2));
        System.out.println(MessageUtil.formatMessage("Results save to {0}", destfile));

        PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(destfile), OUTPUT_CHARSET));

        int aMiss = 0;
        int bMiss = 0;
        int diff = 0;

        for (int i = 0; i < MAX_CHAR; i++) {
            char a = table1[i];
            char b = table2[i];

            if (a != b) {
                String aDesc = a + "";
                String bDesc = b + "";

                if (a == 0) {
                    aDesc = "miss " + (char) i;
                    aMiss++;
                } else if (b == 0) {
                    bDesc = "miss" + (char) i;
                    bMiss++;
                } else {
                    diff++;
                }

                out.println(MessageUtil.formatMessage("{0}({1}) - {4}({2}) - {5}({3})", new Object[] { hex(i),
                        new Character((char) i), aDesc, bDesc, name1, name2 }));
            }
        }

        out.println(name1 + " miss: " + aMiss);
        out.println(name2 + " miss: " + bMiss);
        out.println("Diff:      " + diff);

        out.close();
    }
}
