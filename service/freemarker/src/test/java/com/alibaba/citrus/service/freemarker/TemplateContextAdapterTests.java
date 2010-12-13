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
package com.alibaba.citrus.service.freemarker;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.alibaba.citrus.service.freemarker.impl.TemplateContextAdapter;
import com.alibaba.citrus.service.template.TemplateContext;
import com.alibaba.citrus.service.template.support.MappedTemplateContext;

import freemarker.template.SimpleNumber;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

public class TemplateContextAdapterTests {
    private TemplateContext ctx;

    @Before
    public void init() {
        ctx = new MappedTemplateContext();

        ctx.put("aaa", 111);
        ctx.put("bbb", 222);
        ctx.put("ccc", 333);
    }

    @Test
    public void test() throws TemplateModelException {
        TemplateContextAdapter adapter = new TemplateContextAdapter(ctx, null);

        assertSimpleNumber(adapter.get("aaa"), 111);
        assertSimpleNumber(adapter.get("bbb"), 222);
        assertSimpleNumber(adapter.get("ccc"), 333);
        assertNull(adapter.get("ddd"));
    }

    private void assertSimpleNumber(TemplateModel model, Number num) {
        assertThat(model, instanceOf(SimpleNumber.class));
        assertEquals(num, ((SimpleNumber) model).getAsNumber());
    }
}
