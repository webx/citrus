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

import com.alibaba.citrus.util.i18n.provider.ChineseCharConverterProvider;

/**
 * 根据word转换的dump文件创建转换码表。
 * 
 * @author Michael Zhou
 */
public class CodeTableMaker extends CharsetTool {
    public static void main(String[] args) throws Exception {
        CharsetLoader loader = new CharsetLoader();

        char[] simp_to_trad = loader.load("gbk_trad_word.txt");
        char[] trad_to_simp = loader.load("gbk_simp_word.txt");

        new CharsetComparator().compare(simp_to_trad, "Trad", trad_to_simp, "Simp", "simp_trad_compare.txt");

        new CodeTableMaker().saveToTable("simp_to_trad_table.txt", simp_to_trad);
        new CodeTableMaker().saveToTable("trad_to_simp_table.txt", trad_to_simp);
    }

    public void saveToTable(String tablefile, char[] table) throws IOException {
        File destfile = getFile(tablefile);

        System.out.println("Create code table file: " + destfile.getAbsolutePath());

        Writer out = null;

        try {
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(destfile),
                    ChineseCharConverterProvider.CODE_TABLE_CHARSET));

            for (int i = 0; i < table.length; i++) {
                char ch = table[i];

                if (ch == 0) {
                    ch = (char) i;
                }

                out.write(ch);
            }
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                }
            }
        }
    }
}
