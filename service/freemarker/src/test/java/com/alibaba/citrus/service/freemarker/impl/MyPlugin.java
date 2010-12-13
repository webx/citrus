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
package com.alibaba.citrus.service.freemarker.impl;

import com.alibaba.citrus.service.freemarker.FreeMarkerConfiguration;
import com.alibaba.citrus.service.freemarker.FreeMarkerPlugin;

import freemarker.template.Configuration;

public class MyPlugin implements FreeMarkerPlugin {
    private String charset;
    private String booleanFormat;

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public void setBooleanFormat(String format) {
        this.booleanFormat = format;
    }

    public void init(FreeMarkerConfiguration configuration) {
        Configuration config = configuration.getConfiguration();

        if (charset != null) {
            config.setDefaultEncoding(charset);
        }

        if (booleanFormat != null) {
            config.setBooleanFormat(booleanFormat);
        }
    }
}
