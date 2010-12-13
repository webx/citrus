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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.alibaba.citrus.service.mail.builder.MailContent;
import com.alibaba.citrus.service.mail.builder.content.AlternativeMultipartContent;
import com.alibaba.citrus.service.mail.builder.content.MixedMultipartContent;
import com.alibaba.citrus.service.mail.builder.content.MultipartContent;
import com.alibaba.citrus.service.mail.builder.content.TextContent;
import com.alibaba.citrus.test.runner.Prototyped;
import com.alibaba.citrus.test.runner.Prototyped.Prototypes;
import com.alibaba.citrus.test.runner.Prototyped.TestData;
import com.alibaba.citrus.test.runner.Prototyped.TestName;

/**
 * 测试两个multipart content的功能。
 * 
 * @author Michael Zhou
 */
@RunWith(Prototyped.class)
public class MultipartContentTests extends AbstractMailBuilderTests implements Cloneable {
    private TextContent content1;
    private TextContent content2;
    private Class<?> multipartClass;
    private String multipartClassName;
    private MultipartContent multipart;
    private String multipartContentType;

    @Before
    public void init() throws Exception {
        content1 = new TextContent("content1");
        content2 = new TextContent("content2", "text/html");

        multipart = (MultipartContent) multipartClass.newInstance();
        multipart.addContent(content1);
        multipart.addContent(content2);

        builder.setContent(multipart);
    }

    @TestName
    public String testName() {
        return multipartClass.getSimpleName();
    }

    @Prototypes
    public static TestData<MultipartContentTests> data() {
        TestData<MultipartContentTests> data = TestData.getInstance(MultipartContentTests.class);
        MultipartContentTests prototype;

        prototype = data.newPrototype();
        prototype.multipartClass = MixedMultipartContent.class;
        prototype.multipartClassName = "MixedMultipartContent";
        prototype.multipartContentType = "multipart/mixed";

        prototype = data.newPrototype();
        prototype.multipartClass = AlternativeMultipartContent.class;
        prototype.multipartClassName = "AlternativeMultipartContent";
        prototype.multipartContentType = "multipart/alternative";

        return data;
    }

    @Test
    public void addContent() {
        assertSame(multipart, content1.getParentContent());
        assertSame(multipart, content2.getParentContent());
    }

    @Test
    public void getContents() {
        assertArrayEquals(new MailContent[] { content1, content2 }, multipart.getContents());
    }

    @Test
    public void clone_() {
        MultipartContent copy = (MultipartContent) multipart.clone();
        MailContent[] contentCopys = copy.getContents();

        assertEquals(2, contentCopys.length);
        assertNotSame(content1, contentCopys[0]);
        assertNotSame(content2, contentCopys[1]);

        assertEquals(content1.getClass(), contentCopys[0].getClass());
        assertEquals(content2.getClass(), contentCopys[1].getClass());
    }

    @Test
    public void render() throws Exception {
        assertThat(getMessageAsText(), containsAllRegex( //
                "Content-Type: " + multipartContentType + ";\\s+boundary=\"--.+\"" + REGEX_EOL, //
                "Content-Type: text/plain; charset=UTF-8" + REGEX_EOL, //
                "Content-Transfer-Encoding: 8bit" + REGEX_EOL, //
                "Content-Type: text/html; charset=UTF-8" + REGEX_EOL, //
                "content1" + REGEX_EOL, //
                "content2" + REGEX_EOL));
    }

    @Test
    public void toString_() {
        String result = "";

        result += multipartClassName + " [\n";
        result += "  [1/2] TextContent {\n";
        result += "          contentType = text/plain\n";
        result += "          text        = content1\n";
        result += "        }\n";
        result += "  [2/2] TextContent {\n";
        result += "          contentType = text/html\n";
        result += "          text        = content2\n";
        result += "        }\n";
        result += "]";

        assertEquals(result, multipart.toString());
    }

}
