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
package com.alibaba.citrus.util;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

/**
 * 测试<code>Paginator</code>。
 * 
 * @author Michael Zhou
 */
public class PaginatorTests {
    private Paginator pg;

    @Before
    public void init() {
        pg = new Paginator(10);
    }

    /**
     * 测试无限多项。
     */
    @Test
    public void unknownItems() {
        assertEquals(Paginator.UNKNOWN_ITEMS, pg.getItems());
        assertEquals(10, pg.getItemsPerPage());

        // 初始状态，第1页
        assertEquals(1, pg.getPage());
        assertEquals(1, pg.getFirstPage());
        assertEquals(1, pg.getPreviousPage());
        assertEquals(1, pg.getPreviousPage(10));
        assertEquals(2, pg.getNextPage());
        assertEquals(11, pg.getNextPage(10));

        assertEquals(1, pg.getBeginIndex());
        assertEquals(10, pg.getEndIndex());

        assertEquals(0, pg.getOffset());
        assertEquals(10, pg.getLength());

        assertTrue(pg.isDisabledPage(0));
        assertTrue(pg.isDisabledPage(1));
        assertFalse(pg.isDisabledPage(2));

        // 下一页，第2页
        pg.setPage(2);

        assertEquals(2, pg.getPage());
        assertEquals(1, pg.getFirstPage());
        assertEquals(1, pg.getPreviousPage());
        assertEquals(1, pg.getPreviousPage(10));
        assertEquals(3, pg.getNextPage());
        assertEquals(12, pg.getNextPage(10));

        assertEquals(11, pg.getBeginIndex());
        assertEquals(20, pg.getEndIndex());

        assertEquals(10, pg.getOffset());
        assertEquals(10, pg.getLength());

        assertTrue(pg.isDisabledPage(0));
        assertFalse(pg.isDisabledPage(1));
        assertTrue(pg.isDisabledPage(2));

        // 第11页
        pg.setPage(11);

        assertEquals(11, pg.getPage());
        assertEquals(1, pg.getFirstPage());
        assertEquals(10, pg.getPreviousPage());
        assertEquals(1, pg.getPreviousPage(10));
        assertEquals(12, pg.getNextPage());
        assertEquals(21, pg.getNextPage(10));

        assertEquals(101, pg.getBeginIndex());
        assertEquals(110, pg.getEndIndex());

        assertEquals(100, pg.getOffset());
        assertEquals(10, pg.getLength());

        assertTrue(pg.isDisabledPage(0));
        assertFalse(pg.isDisabledPage(1));
        assertFalse(pg.isDisabledPage(2));
        assertTrue(pg.isDisabledPage(11));
        assertFalse(pg.isDisabledPage(12));
    }

    /**
     * 测试有限多项。
     */
    @Test
    public void limitedItems() {
        pg.setItems(123);

        assertEquals(13, pg.getPages());
        assertEquals(123, pg.getItems());
        assertEquals(10, pg.getItemsPerPage());

        // 初始状态，第1页
        assertEquals(1, pg.getPage());
        assertEquals(1, pg.getFirstPage());
        assertEquals(1, pg.getPreviousPage());
        assertEquals(1, pg.getPreviousPage(10));
        assertEquals(2, pg.getNextPage());
        assertEquals(11, pg.getNextPage(10));
        assertEquals(13, pg.getLastPage());

        assertEquals(1, pg.getBeginIndex());
        assertEquals(10, pg.getEndIndex());

        assertEquals(0, pg.getOffset());
        assertEquals(10, pg.getLength());

        assertTrue(pg.isDisabledPage(0));
        assertTrue(pg.isDisabledPage(1));
        assertFalse(pg.isDisabledPage(2));
        assertFalse(pg.isDisabledPage(13));
        assertTrue(pg.isDisabledPage(14));

        // 下一页，第2页
        pg.setPage(2);

        assertEquals(2, pg.getPage());
        assertEquals(1, pg.getFirstPage());
        assertEquals(1, pg.getPreviousPage());
        assertEquals(1, pg.getPreviousPage(10));
        assertEquals(3, pg.getNextPage());
        assertEquals(12, pg.getNextPage(10));

        assertEquals(11, pg.getBeginIndex());
        assertEquals(20, pg.getEndIndex());

        assertEquals(10, pg.getOffset());
        assertEquals(10, pg.getLength());

        assertTrue(pg.isDisabledPage(0));
        assertFalse(pg.isDisabledPage(1));
        assertTrue(pg.isDisabledPage(2));
        assertFalse(pg.isDisabledPage(13));
        assertTrue(pg.isDisabledPage(14));

        // 第11页
        pg.setPage(11);

        assertEquals(11, pg.getPage());
        assertEquals(1, pg.getFirstPage());
        assertEquals(10, pg.getPreviousPage());
        assertEquals(1, pg.getPreviousPage(10));
        assertEquals(12, pg.getNextPage());
        assertEquals(13, pg.getNextPage(10));

        assertEquals(101, pg.getBeginIndex());
        assertEquals(110, pg.getEndIndex());

        assertEquals(100, pg.getOffset());
        assertEquals(10, pg.getLength());

        assertTrue(pg.isDisabledPage(0));
        assertFalse(pg.isDisabledPage(1));
        assertFalse(pg.isDisabledPage(2));
        assertTrue(pg.isDisabledPage(11));
        assertFalse(pg.isDisabledPage(13));
        assertTrue(pg.isDisabledPage(14));

        // 第13页（最后一页）
        pg.setPage(13);

        assertEquals(13, pg.getPage());
        assertEquals(1, pg.getFirstPage());
        assertEquals(12, pg.getPreviousPage());
        assertEquals(3, pg.getPreviousPage(10));
        assertEquals(13, pg.getNextPage());
        assertEquals(13, pg.getNextPage(10));

        assertEquals(121, pg.getBeginIndex());
        assertEquals(123, pg.getEndIndex());

        assertEquals(120, pg.getOffset());
        assertEquals(3, pg.getLength());

        assertTrue(pg.isDisabledPage(0));
        assertFalse(pg.isDisabledPage(1));
        assertFalse(pg.isDisabledPage(2));
        assertFalse(pg.isDisabledPage(11));
        assertTrue(pg.isDisabledPage(13));
        assertTrue(pg.isDisabledPage(14));
    }

    @Test
    public void setItems() {
        pg.setItems(123);
        pg.setPage(11);
        pg.setItems(133); // 改变items

        assertEquals(14, pg.getPages());
        assertEquals(133, pg.getItems());
        assertEquals(10, pg.getItemsPerPage());

        assertEquals(11, pg.getPage());
        assertEquals(1, pg.getFirstPage());
        assertEquals(10, pg.getPreviousPage());
        assertEquals(1, pg.getPreviousPage(10));
        assertEquals(12, pg.getNextPage());
        assertEquals(14, pg.getNextPage(10));

        assertEquals(101, pg.getBeginIndex());
        assertEquals(110, pg.getEndIndex());

        assertEquals(100, pg.getOffset());
        assertEquals(10, pg.getLength());

        assertTrue(pg.isDisabledPage(0));
        assertFalse(pg.isDisabledPage(1));
        assertFalse(pg.isDisabledPage(2));
        assertTrue(pg.isDisabledPage(11));
        assertFalse(pg.isDisabledPage(13));
        assertFalse(pg.isDisabledPage(14));
        assertTrue(pg.isDisabledPage(15));
    }

    @Test
    public void setItemsPerPage() {
        pg.setItems(123);
        pg.setPage(11);

        // 变大itemsPerPage
        pg.setItemsPerPage(20);

        assertEquals(7, pg.getPages());
        assertEquals(123, pg.getItems());
        assertEquals(20, pg.getItemsPerPage());

        assertEquals(6, pg.getPage());
        assertEquals(1, pg.getFirstPage());
        assertEquals(5, pg.getPreviousPage());
        assertEquals(1, pg.getPreviousPage(10));
        assertEquals(7, pg.getNextPage());
        assertEquals(7, pg.getNextPage(10));

        assertEquals(101, pg.getBeginIndex());
        assertEquals(120, pg.getEndIndex());

        assertEquals(100, pg.getOffset());
        assertEquals(20, pg.getLength());

        assertTrue(pg.isDisabledPage(0));
        assertFalse(pg.isDisabledPage(1));
        assertFalse(pg.isDisabledPage(2));
        assertTrue(pg.isDisabledPage(6));
        assertFalse(pg.isDisabledPage(7));
        assertTrue(pg.isDisabledPage(8));

        // 变小itemsPerPage
        pg.setItemsPerPage(5);

        assertEquals(25, pg.getPages());
        assertEquals(123, pg.getItems());
        assertEquals(5, pg.getItemsPerPage());

        assertEquals(21, pg.getPage());
        assertEquals(1, pg.getFirstPage());
        assertEquals(20, pg.getPreviousPage());
        assertEquals(11, pg.getPreviousPage(10));
        assertEquals(22, pg.getNextPage());
        assertEquals(25, pg.getNextPage(10));

        assertEquals(101, pg.getBeginIndex());
        assertEquals(105, pg.getEndIndex());

        assertEquals(100, pg.getOffset());
        assertEquals(5, pg.getLength());

        assertTrue(pg.isDisabledPage(0));
        assertFalse(pg.isDisabledPage(1));
        assertFalse(pg.isDisabledPage(2));
        assertTrue(pg.isDisabledPage(21));
        assertFalse(pg.isDisabledPage(22));
        assertTrue(pg.isDisabledPage(26));
    }

    @Test
    public void noItems() {
        pg.setItems(0);
        pg.setPage(1);

        assertEquals(0, pg.getPages());
        assertEquals(10, pg.getItemsPerPage());

        assertEquals(0, pg.getPage());
        assertEquals(0, pg.getFirstPage());
        assertEquals(0, pg.getPreviousPage());
        assertEquals(0, pg.getPreviousPage(10));
        assertEquals(0, pg.getNextPage());
        assertEquals(0, pg.getNextPage(10));

        assertEquals(0, pg.getBeginIndex());
        assertEquals(0, pg.getEndIndex());

        assertEquals(0, pg.getOffset());
        assertEquals(0, pg.getLength());

        assertTrue(pg.isDisabledPage(0));
        assertTrue(pg.isDisabledPage(1));
        assertTrue(pg.isDisabledPage(2));
        assertTrue(pg.isDisabledPage(21));
        assertTrue(pg.isDisabledPage(22));
        assertTrue(pg.isDisabledPage(26));
    }

    @Test
    public void setItem() {
        pg.setItems(123);

        // 13页
        pg.setItem(122);

        assertEquals(13, pg.getPage());
        assertEquals(1, pg.getFirstPage());
        assertEquals(12, pg.getPreviousPage());
        assertEquals(3, pg.getPreviousPage(10));
        assertEquals(13, pg.getNextPage());
        assertEquals(13, pg.getNextPage(10));

        assertEquals(121, pg.getBeginIndex());
        assertEquals(123, pg.getEndIndex());

        assertEquals(120, pg.getOffset());
        assertEquals(3, pg.getLength());

        assertTrue(pg.isDisabledPage(0));
        assertFalse(pg.isDisabledPage(1));
        assertFalse(pg.isDisabledPage(2));
        assertFalse(pg.isDisabledPage(11));
        assertTrue(pg.isDisabledPage(13));
        assertTrue(pg.isDisabledPage(14));

        // 1页
        pg.setItem(0);

        assertEquals(1, pg.getPage());
        assertEquals(1, pg.getFirstPage());
        assertEquals(1, pg.getPreviousPage());
        assertEquals(1, pg.getPreviousPage(10));
        assertEquals(2, pg.getNextPage());
        assertEquals(11, pg.getNextPage(10));
        assertEquals(13, pg.getLastPage());

        assertEquals(1, pg.getBeginIndex());
        assertEquals(10, pg.getEndIndex());

        assertEquals(0, pg.getOffset());
        assertEquals(10, pg.getLength());

        assertTrue(pg.isDisabledPage(0));
        assertTrue(pg.isDisabledPage(1));
        assertFalse(pg.isDisabledPage(2));
        assertFalse(pg.isDisabledPage(13));
        assertTrue(pg.isDisabledPage(14));
    }

    @Test
    public void slider() {
        pg.setItems(0);
        assertEquals(0, pg.getSlider().length);

        pg.setItems(123);
        assertEquals(0, pg.getSlider(0).length);

        assertSlide(1, 2, 3, 4, 5, 6, 7);

        pg.setPage(2);
        assertSlide(1, 2, 3, 4, 5, 6, 7);

        pg.setPage(3);
        assertSlide(1, 2, 3, 4, 5, 6, 7);

        pg.setPage(4);
        assertSlide(1, 2, 3, 4, 5, 6, 7);

        pg.setPage(5);
        assertSlide(2, 3, 4, 5, 6, 7, 8);

        pg.setPage(6);
        assertSlide(3, 4, 5, 6, 7, 8, 9);

        pg.setPage(7);
        assertSlide(4, 5, 6, 7, 8, 9, 10);

        pg.setPage(8);
        assertSlide(5, 6, 7, 8, 9, 10, 11);

        pg.setPage(9);
        assertSlide(6, 7, 8, 9, 10, 11, 12);

        pg.setPage(10);
        assertSlide(7, 8, 9, 10, 11, 12, 13);

        pg.setPage(11);
        assertSlide(7, 8, 9, 10, 11, 12, 13);

        pg.setPage(12);
        assertSlide(7, 8, 9, 10, 11, 12, 13);

        pg.setPage(13);
        assertSlide(7, 8, 9, 10, 11, 12, 13);
    }

    private void assertSlide(int a, int b, int c, int d, int e, int f, int g) {
        int i = 0;
        int[] slider = pg.getSlider(7);

        assertEquals(a, slider[i++]);
        assertEquals(b, slider[i++]);
        assertEquals(c, slider[i++]);
        assertEquals(d, slider[i++]);
        assertEquals(e, slider[i++]);
        assertEquals(f, slider[i++]);
        assertEquals(g, slider[i++]);
    }
}
