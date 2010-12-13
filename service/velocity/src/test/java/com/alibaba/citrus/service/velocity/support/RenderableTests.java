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
package com.alibaba.citrus.service.velocity.support;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Iterator;

import org.apache.velocity.app.event.ReferenceInsertionEventHandler;
import org.junit.BeforeClass;
import org.junit.Test;

import com.alibaba.citrus.service.template.Renderable;
import com.alibaba.citrus.service.template.TemplateContext;
import com.alibaba.citrus.service.template.support.MappedTemplateContext;
import com.alibaba.citrus.service.velocity.AbstractVelocityEngineTests;
import com.alibaba.citrus.service.velocity.VelocityEngineTests.MyRenderable;

public class RenderableTests extends AbstractVelocityEngineTests {
    @BeforeClass
    public static void initFactory() {
        factory = createFactory("services_renderable.xml");
    }

    @Test
    public void renderable() throws Exception {
        getEngine("default", factory);

        // 有且仅有一个handler
        Iterator<?> i = velocityEngine.getConfiguration().getEventCartridge().getReferenceInsertionEventHandlers();
        assertThat(i.next(), instanceOf(RenderableHandler.class));
        assertFalse(i.hasNext());

        // 渲染
        TemplateContext ctx = new MappedTemplateContext();
        ctx.put("object", new MyRenderable());

        String content = templateService.getText("test_renderable.vm", ctx);
        assertThat(content, containsString("from render()"));
        assertThat(content, not(containsString("from toString()")));
    }

    @Test
    public void renderable_config() throws Exception {
        getEngine("with_renderable_support", factory);

        // 有两个handler
        Iterator<?> i = velocityEngine.getConfiguration().getEventCartridge().getReferenceInsertionEventHandlers();
        assertThat(i.next(), instanceOf(MakeEverythingRenderable.class));
        assertThat(i.next(), instanceOf(RenderableHandler.class));
        assertFalse(i.hasNext());

        // 渲染
        TemplateContext ctx = new MappedTemplateContext();
        ctx.put("object", "world");

        String content = templateService.getText("test_renderable.vm", ctx);
        assertThat(content, containsString("Hello, world"));
    }

    public static class MakeEverythingRenderable implements ReferenceInsertionEventHandler {
        public Object referenceInsert(String reference, final Object value) {
            return new Renderable() {
                public String render() {
                    return "Hello, " + value;
                }
            };
        }
    }
}
