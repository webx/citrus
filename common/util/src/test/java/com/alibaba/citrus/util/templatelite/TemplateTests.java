/*
 * Copyright (c) 2002-2012 Alibaba Group Holding Limited.
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

package com.alibaba.citrus.util.templatelite;

import static com.alibaba.citrus.test.TestUtil.*;
import static org.junit.Assert.*;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

public class TemplateTests extends AbstractTemplateTests {
    @Before
    public void init() {
        template = new Template(new File(srcdir, "test06_real_case.txt"));
    }

    @Test
    public void getSubTemplate() throws Exception {
        assertEquals("itemlist", template.getSubTemplate("itemlist").getName());
        assertEquals("dateItem", template.getSubTemplate("itemlist").getSubTemplate("dateItem").getName());
        assertEquals("datetimeItem", template.getSubTemplate("itemlist").getSubTemplate("datetimeItem").getName());
    }

    @Test
    public void getParameter() throws Exception {
        assertEquals("UTF-8", template.getParameter("charset"));
        assertEquals("on", template.getParameter("trimming"));
        assertEquals("collapse", template.getParameter("whitespace"));

        assertEquals(null, template.getParameter("notexist"));
    }

    @Test
    public void toString_() throws Exception {
        String s = template.toString();

        assertThat(s, containsAllRegex("#\\(template\\) with 5 nodes at .+test06_real_case.txt \\{\n" //
                , "params\\s+= \\[" //
                , "nodes\\s+= \\[" //
                , "sub-templates\\s+= \\[" //
                , "\\[1/3\\] charset=UTF-8" //
                , "\\[1/5\\] Text with 21 characters:" //
                , "\\[2/5\\] \\$\\{title:我的标题\\} at .+test06_real_case.txt: Line 7 Column 12" //
                , "\\[1/1\\] #itemlist with 3 nodes at .+test06_real_case.txt: Line 14 Column 1" //
        ));
    }
}
