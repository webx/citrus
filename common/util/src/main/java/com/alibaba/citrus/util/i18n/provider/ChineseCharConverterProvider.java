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
package com.alibaba.citrus.util.i18n.provider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import com.alibaba.citrus.util.i18n.CharConverter;
import com.alibaba.citrus.util.i18n.CharConverterProvider;

/**
 * 用来转换简繁中文的provider。
 * 
 * @author Michael Zhou
 */
public abstract class ChineseCharConverterProvider implements CharConverterProvider {
    public static final String CODE_TABLE_CHARSET = "UTF-16BE";
    private char[] codeTable;

    public CharConverter createCharConverter() {
        loadCodeTable();

        return new CharConverter() {
            @Override
            public char convert(char ch) {
                return codeTable[ch];
            }
        };
    }

    /**
     * 装载编码表。
     */
    protected final char[] loadCodeTable() {
        if (codeTable == null) {
            InputStream istream = getClass().getResourceAsStream(getCodeTableName() + ".ctable");

            if (istream == null) {
                throw new RuntimeException("Could not find code table: " + getCodeTableName());
            }

            Reader reader = null;

            try {
                reader = new BufferedReader(new InputStreamReader(istream, CODE_TABLE_CHARSET));
                codeTable = new char[65536];

                for (int i = 0; i < 65536; i++) {
                    codeTable[i] = (char) reader.read();
                }
            } catch (IOException e) {
                throw new RuntimeException("Could not read code table: " + getCodeTableName(), e);
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                    }
                }
            }
        }

        return codeTable;
    }

    protected abstract String getCodeTableName();
}
