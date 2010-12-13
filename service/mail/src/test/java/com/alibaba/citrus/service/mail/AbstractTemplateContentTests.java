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

import com.alibaba.citrus.service.mail.builder.MailBuilderException;
import com.alibaba.citrus.service.mail.builder.content.TemplateContent;
import com.alibaba.citrus.service.pull.PullService;
import com.alibaba.citrus.service.template.TemplateNotFoundException;
import com.alibaba.citrus.service.template.TemplateService;

/**
 * Ä£°åcontent²âÊÔ»ùÀà¡£
 * 
 * @author Michael Zhou
 */
public abstract class AbstractTemplateContentTests<TC extends TemplateContent> extends AbstractMailBuilderTests {
    protected TC content;
    protected TemplateService templateService;
    protected PullService pullService;

    @Before
    public final void initSuper() {
        content = createContent();
        templateService = (TemplateService) factory.getBean("templateService");
        pullService = (PullService) factory.getBean("pullService");

        assertNotNull(templateService);
        assertNotNull(pullService);
    }

    protected abstract TC createContent();

    @Test
    public void getTemplateService() {
        try {
            content.getTemplateService();
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("templateService"));
        }

        content.setTemplateService(templateService);
        assertSame(templateService, content.getTemplateService());
    }

    @Test
    public void getPullService() {
        assertSame(null, content.getPullService());

        content.setPullService(pullService);
        assertSame(pullService, content.getPullService());
    }

    @Test
    public void getTemplateName() {
        assertEquals(null, content.getTemplateName());

        content.setTemplate("  test.vm ");
        assertEquals("test.vm", content.getTemplateName());
    }

    @Test
    public void noTemplateService() throws Exception {
        initContent((String) null, false, false);

        try {
            getMessageAsText();
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("templateService"));
        }
    }

    @Test
    public void noTemplateName() throws Exception {
        initContent((String) null, true, false);

        try {
            getMessageAsText();
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("templateName"));
        }
    }

    @Test
    public void templateNotFound() throws Exception {
        initContent("notExist.vm", true, false);

        try {
            getMessageAsText();
            fail();
        } catch (MailBuilderException e) {
            assertThat(
                    e,
                    exception(TemplateNotFoundException.class, "Failed to render template: notExist.vm",
                            "Could not find template \"/notExist.vm\""));
        }
    }

    protected final void initContent(String templateName, boolean withTemplateService, boolean withPullService) {
        initContent(createContent(), withTemplateService, withPullService);

        if (templateName != null) {
            content.setTemplate(templateName);
        }
    }

    protected final void initContent(TC content, boolean withTemplateService, boolean withPullService) {
        this.content = content;
        builder.setContent(content);

        if (withTemplateService) {
            content.setTemplateService(templateService);
        }

        if (withPullService) {
            content.setPullService(pullService);
        }
    }
}
