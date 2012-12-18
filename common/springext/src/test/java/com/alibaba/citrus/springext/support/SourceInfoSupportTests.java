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

package com.alibaba.citrus.springext.support;

import static com.alibaba.citrus.test.TestUtil.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import com.alibaba.citrus.springext.ResourceResolver.Resource;
import com.alibaba.citrus.springext.SourceInfo;
import org.junit.Before;
import org.junit.Test;

public class SourceInfoSupportTests {
    private SourceInfoSupport<SourceInfo<?>> parentSourceInfo;
    private SourceInfoSupport<SourceInfo<?>> sourceInfo;
    private Resource                         resource;

    @Before
    public void init() {
        parentSourceInfo = new SourceInfoSupport<SourceInfo<?>>();
        sourceInfo = new SourceInfoSupport<SourceInfo<?>>(parentSourceInfo);
        resource = createMock(Resource.class);
        expect(resource.getName()).andReturn(null).anyTimes();
        replay(resource);
    }

    @Test
    public void constructor() {
        assertEquals(null, parentSourceInfo.getParent());
        assertEquals(null, parentSourceInfo.getSource());
        assertEquals(-1, parentSourceInfo.getLineNumber());
    }

    @Test
    public void constructor2() {
        assertSame(parentSourceInfo, sourceInfo.getParent());
        assertEquals(null, sourceInfo.getSource());
        assertEquals(-1, sourceInfo.getLineNumber());

        try {
            new SourceInfoSupport<SourceInfo<?>>(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("no parent sourceInfo provided"));
        }
    }

    @Test
    public void setSource() {
        assertSame(sourceInfo, sourceInfo.setSource(null));
        assertEquals(null, sourceInfo.getSource());
        assertEquals(-1, sourceInfo.getLineNumber());

        assertSame(sourceInfo, sourceInfo.setSource(null, 10));
        assertEquals(null, sourceInfo.getSource());
        assertEquals(-1, sourceInfo.getLineNumber()); // resource不存在时line=-1

        assertSame(sourceInfo, sourceInfo.setSource(resource, 10));
        assertSame(resource, sourceInfo.getSource());
        assertEquals(10, sourceInfo.getLineNumber());

        assertSame(sourceInfo, sourceInfo.setSource(resource, 0));
        assertSame(resource, sourceInfo.getSource());
        assertEquals(-1, sourceInfo.getLineNumber()); // 最小line为1

        assertSame(sourceInfo, sourceInfo.setSource(resource, 1));
        assertSame(resource, sourceInfo.getSource());
        assertEquals(1, sourceInfo.getLineNumber());

        assertSame(sourceInfo, sourceInfo.setSource(resource));
        assertSame(resource, sourceInfo.getSource());
        assertEquals(-1, sourceInfo.getLineNumber()); // line=-1
    }

    @Test
    public void toString_() {
        assertEquals("SourceInfoSupport", parentSourceInfo.toString()); // resource==null

        sourceInfo.setSource(resource);
        assertEquals(resource.toString(), sourceInfo.toString());

        sourceInfo.setSource(resource, 10);
        assertEquals(resource.toString() + " (line 10)", sourceInfo.toString());
    }
}
