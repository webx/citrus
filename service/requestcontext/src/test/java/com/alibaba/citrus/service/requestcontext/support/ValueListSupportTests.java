package com.alibaba.citrus.service.requestcontext.support;

import static org.easymock.classextension.EasyMock.*;
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
