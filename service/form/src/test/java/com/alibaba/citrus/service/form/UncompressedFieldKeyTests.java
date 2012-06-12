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

package com.alibaba.citrus.service.form;

import static org.junit.Assert.*;

import com.alibaba.citrus.service.form.support.FormTool;
import com.alibaba.citrus.service.form.support.FormTool.FieldHelper;
import com.alibaba.citrus.service.form.support.FormTool.GroupInstanceHelper;
import com.alibaba.citrus.service.pull.PullService;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class UncompressedFieldKeyTests extends AbstractFormServiceTests {
    private Form     form;
    private Group    group;
    private FormTool tool;

    @BeforeClass
    public static void initFactory() {
        factory = createContext("services-form.xml", true);
    }

    @Before
    public void init() {
        getFormService("form1");

        PullService pullService = (PullService) factory.getBean("pullService");
        tool = (FormTool) pullService.getTools().get("form");
        assertNotNull(tool);
    }

    /** FieldKeyFormat=compressed，同时支持压缩、不压缩混合格式的key，大小写不敏感。 */
    @Test
    public void compress_uncompress_hybrid() throws Exception {
        Object[][] args = new Object[][] { { "sumbit", "提交" }, //
                                           { "_fm.grOup1._0.f", "aaa" }, // group1.field1
                                           { "_fm.group1._0.field2", "bbb" }, // group1.field2
        };

        invokePost(args);
        form = formService.getForm();

        assertEquals(true, form.isValid());
        assertEquals(1, form.getGroups().size());

        group = form.getGroup("group1");

        assertEquals(true, group.isValid());
        assertEquals("aaa", group.getField("field1").getStringValue());
        assertEquals("bbb", group.getField("field2").getStringValue());
    }

    /** FieldKeyFormat=uncompressed，不支持压缩，只支持不压缩格式的key，大小写不敏感。 */
    @Test
    public void uncompress_only_1() throws Exception {
        getFormService("form5");

        Object[][] args = new Object[][] { { "sumbit", "提交" }, //
                                           { "_fm.grOup1._0.fieLd1", "aaa" }, // group1.field1
                                           { "_fm.group1._0.field2.absent", "ccc" }, // group1.field2
                                           { "_fm.group1._0.Field2.attach", "eNpb85aBtYSBOTEpGQAUSAM8" }, // group1.field2
        };

        invokePost(args);
        form = formService.getForm();

        assertEquals(true, form.isValid());
        assertEquals(1, form.getGroups().size());

        group = form.getGroup("group1");

        assertEquals(true, group.isValid());
        assertEquals("aaa", group.getField("field1").getStringValue());
        assertEquals("ccc", group.getField("field2").getStringValue());
        assertEquals("abc", group.getField("field2").getAttachment());
    }

    @Test
    public void uncompress_only_2() throws Exception {
        getFormService("form5");

        Object[][] args = new Object[][] { { "sumbit", "提交" }, //
                                           { "_fm.grOup1._0.f", "aaa" }, // group1.field1
                                           { "_fm.group1._0.field2", "bbb" }, // group1.field2
        };

        invokePost(args);
        form = formService.getForm();

        assertEquals(false, form.isValid());
    }

    @Test
    public void uncompressed_key() throws Exception {
        getFormService("form5");
        tool.setFormService(formService);

        invokePost(null);

        GroupInstanceHelper group = tool.get("group1").getDefaultInstance();
        FieldHelper field = group.get("field1");

        assertEquals("_fm.group1._0.field1", field.getKey());
        assertEquals("_fm.group1._0.field1.absent", field.getAbsentKey());
        assertEquals("_fm.group1._0.field1.attach", field.getAttachmentKey());
    }
}
