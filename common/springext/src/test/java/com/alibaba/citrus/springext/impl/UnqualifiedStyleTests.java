/*
 * Copyright (c) 2002-2013 Alibaba Group Holding Limited.
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
 */

package com.alibaba.citrus.springext.impl;

import static com.alibaba.citrus.springext.ContributionType.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;

import com.alibaba.citrus.springext.ConfigurationPoint;
import com.alibaba.citrus.springext.Contribution;
import com.alibaba.citrus.springext.Schema;
import com.alibaba.citrus.springext.SourceInfo;
import com.alibaba.citrus.springext.support.ClasspathResourceResolver;
import com.alibaba.citrus.springext.util.ConvertToUnqualifiedStyle;
import com.alibaba.citrus.test.TestEnvStatic;
import com.alibaba.citrus.util.io.StreamUtil;
import org.junit.Test;

public class UnqualifiedStyleTests {
    private ConfigurationPointsImpl cps;
    private SpringPluggableSchemas  sps;

    static {
        TestEnvStatic.init();
    }

    @Test
    public void unqualifiedStyleContribution() throws Exception {
        cps = new ConfigurationPointsImpl((ClassLoader) null, null);

        ConfigurationPoint cp1 = cps.getConfigurationPointByName("my/cp1");
        Contribution contrib1 = cp1.getContribution("test1", BEAN_DEFINITION_PARSER);
        Schema schema = contrib1.getSchemas().getMainSchema();

        // my/cp1/test1.xsd包含elementFormDefault，转换后被强制去除。
        assertQualified(schema, false);
    }

    @Test
    public void qualifiedStyleContribution() throws Exception {
        cps = new ConfigurationPointsImpl(new ClasspathResourceResolver(getClass().getClassLoader()) {
            @Override
            public Resource getResource(String location) {
                String name = ConvertToUnqualifiedStyle.class.getName().replace('.', '/') + ".class";

                if (name.equals(location)) {
                    return null;
                } else {
                    return super.getResource(location);
                }
            }
        });

        ConfigurationPoint cp1 = cps.getConfigurationPointByName("my/cp1");
        Contribution contrib1 = cp1.getContribution("test1", BEAN_DEFINITION_PARSER);
        Schema schema = contrib1.getSchemas().getMainSchema();

        // my/cp1/test1.xsd包含elementFormDefault。此处摸拟老版本，故qualified被保留。
        assertQualified(schema, true);
    }

    @Test
    public void unqualifiedStyleSpringPluggableSchemas() throws Exception {
        sps = new SpringPluggableSchemas();

        Schema schema = sps.getNamedMappings().get("www.alibaba.com/schema/springext-base-types.xsd");

        // springext-base-types.xsd包含elementFormDefault，转换后被保留。
        assertQualified(schema, true);
    }

    private void assertQualified(Schema schema, boolean qualified) throws IOException {
        String textTransformed = schema.getText();
        String textOriginal = StreamUtil.readText(((SourceInfo<?>) schema).getSource().getInputStream(), "UTF-8", true);

        String elementQualified = "elementFormDefault=\"qualified\"";

        assertThat(textOriginal, containsString(elementQualified));

        if (qualified) {
            assertThat(textTransformed, containsString(elementQualified));
        } else {
            assertThat(textTransformed, not(containsString(elementQualified)));
        }
    }
}
