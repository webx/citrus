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
package com.alibaba.citrus.service.mail;

import static com.alibaba.citrus.test.TestUtil.*;
import static org.junit.Assert.*;

import javax.mail.MessagingException;
import javax.mail.Part;

import org.junit.Before;
import org.junit.Test;

import com.alibaba.citrus.service.mail.builder.content.AbstractContent;
import com.alibaba.citrus.util.internal.ToStringBuilder.MapBuilder;

/**
 * 测试<code>AbstractContent</code>的基本功能。
 * 
 * @author Michael Zhou
 */
public class MailContentBasicTests extends AbstractMailBuilderTests {
    private MyContent content1;
    private MyContent content2;

    @Before
    public void init() {
        content1 = new MyContent();
        content1.setId("content1");
        content1.setMailBuilder(builder);
        content1.name = "content1_name";

        content2 = new MyContent();
        content2.setId("content2");
        content2.setParentContent(content1);
        content2.name = "content2_name";

        assertNotNull(content1.name);
        assertNotNull(content2.name);
    }

    @Test
    public void getId() {
        assertEquals("content1", content1.getId());
        assertEquals("content2", content2.getId());
    }

    @Test
    public void getMailBuilder() {
        assertSame(builder, content1.getMailBuilder());
        assertSame(builder, content2.getMailBuilder()); // 继承自content1

        content1.setMailBuilder(null);

        assertNoMailBuilder(content1);
        assertNoMailBuilder(content2); // 继承自content1
    }

    @Test
    public void getParentContent() {
        assertSame(null, content1.getParentContent());
        assertSame(content1, content2.getParentContent());
    }

    @Test
    public void clone_() {
        // 一层content
        MyContent contentCopy = (MyContent) content1.clone();

        assertEquals("content1", contentCopy.getId());
        assertNoMailBuilder(contentCopy);
        assertEquals(null, contentCopy.getParentContent());
        assertEquals("content1_name", contentCopy.name); // clone by copyTo()

        // 两层content
        contentCopy = (MyContent) content2.clone();

        assertEquals("content2", contentCopy.getId());
        assertNoMailBuilder(contentCopy);
        assertEquals(null, contentCopy.getParentContent());
        assertEquals("content2_name", contentCopy.name); // clone by copyTo()
    }

    @Test
    public void clone_newInstance_null() {
        class MyContent1 extends MyContent {
            @Override
            protected AbstractContent newInstance() {
                return null;
            }
        }

        try {
            new MyContent1().clone();
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("MyContent1.newInstance() returned null"));
        }
    }

    @Test
    public void clone_newInstance_wrongType() {
        class MyContent1 extends MyContent {
            @Override
            protected AbstractContent newInstance() {
                return new MyContent();
            }
        }

        try {
            new MyContent1().clone();
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("MyContent1.newInstance() returned an object of wrong class"));
        }
    }

    @Test
    public void toString_() {
        String result = "";

        result += "MyContent {\n";
        result += "  name = content1_name\n";
        result += "}";

        assertEquals(result, content1.toString());
    }

    public static class MyContent extends AbstractContent {
        public String name;

        @Override
        protected AbstractContent newInstance() {
            return new MyContent();
        }

        @Override
        protected void copyTo(AbstractContent copy) {
            ((MyContent) copy).name = name;
        }

        public void render(Part mailPart) throws MessagingException {
            mailPart.setContent("", "text/plain");
        }

        @Override
        public void toString(MapBuilder mb) {
            mb.append("name", name);
        }
    }
}
