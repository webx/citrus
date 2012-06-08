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

package com.alibaba.citrus.service.requestcontext.support;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import org.apache.commons.fileupload.FileItem;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.SimpleTypeConverter;

public class ValueListSupportTests {
    private ValueListSupport values;

    @Before
    public void init() {
        values = new ValueListSupport(new SimpleTypeConverter(), true);
    }

    @Test
    public void getFileItem() {
        FileItem item = createMock(FileItem.class);

        values.addValue(item);

        assertSame(item, values.getFileItem());
    }

    @Test
    public void getFileItem_nonFileItem() {
        values.addValue("str");

        assertNull(values.getFileItem());
    }

    @Test
    public void getFileItems() {
        FileItem item = createMock(FileItem.class);

        values.addValue(item);

        assertArrayEquals(new FileItem[] { item }, values.getFileItems());
    }

    @Test
    public void getFileItems_noItems() {
        assertArrayEquals(new FileItem[0], values.getFileItems());
    }

    @Test
    public void getFileItems_withNonFileItems() {
        FileItem item = createMock(FileItem.class);

        values.addValue(item);
        values.addValue("");
        values.addValue(null);

        assertArrayEquals(new FileItem[] { item }, values.getFileItems());
    }
}
