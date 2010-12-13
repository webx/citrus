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
package com.alibaba.citrus.service.configuration;

import static com.alibaba.citrus.test.TestEnvStatic.*;
import static com.alibaba.citrus.test.TestUtil.*;
import static org.junit.Assert.*;

import java.beans.PropertyEditor;
import java.io.File;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.PropertyEditorRegistrar;
import org.springframework.beans.PropertyEditorRegistry;
import org.springframework.beans.SimpleTypeConverter;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.FileSystemResource;

import com.alibaba.citrus.service.configuration.support.PropertyEditorRegistrarsSupport;
import com.alibaba.citrus.springext.support.context.XmlApplicationContext;

public class PropertyEditorRegistrarsTests {
    private static ApplicationContext factory;
    private PropertyEditorRegistrarsSupport registrars;

    @BeforeClass
    public static void initFactory() throws Exception {
        factory = new XmlApplicationContext(new FileSystemResource(new File(srcdir, "registrars.xml")));
    }

    @Test
    public void registrars() {
        registrars = (PropertyEditorRegistrarsSupport) factory.getBean("propertyEditorRegistrars");
        assertEquals(1, registrars.size());

        SimpleTypeConverter converter = new SimpleTypeConverter();
        registrars.registerCustomEditors(converter);

        // Ö»×¢²áÁËlong£¬Ã»×¢²áLong
        try {
            converter.convertIfNecessary("ten", Long.class);
            fail();
        } catch (TypeMismatchException e) {
        }

        assertEquals(10L, converter.convertIfNecessary("ten", long.class));
    }

    @Test
    public void registrarsRef() {
        registrars = (PropertyEditorRegistrarsSupport) factory.getBean("registrarsRef");
        assertEquals(2, registrars.size());

        SimpleTypeConverter converter = new SimpleTypeConverter();
        registrars.registerCustomEditors(converter);

        assertEquals(10L, converter.convertIfNecessary("ten", Long.class));
        assertEquals(10L, converter.convertIfNecessary("ten", long.class));
    }

    @Test
    public void toString_() {
        registrars = (PropertyEditorRegistrarsSupport) factory.getBean("registrarsRef");

        String str = registrars.toString();

        assertThat(str, containsAll("PropertyEditorRegistrarsSupport ["));
        assertThat(str, containsAll("[1/2] PropertyEditorRegistrarsSupport"));
        assertThat(str, containsAll("[1/1] " + MyRegistrar.class.getName()));
        assertThat(str, containsAll("[2/2] " + MyRegistrar.class.getName()));
    }

    public static class MyRegistrar implements PropertyEditorRegistrar {
        private Class<?> type;

        public void setType(Class<?> type) {
            this.type = type;
        }

        public void registerCustomEditors(PropertyEditorRegistry registry) {
            PropertyEditor editor = new CustomNumberEditor(Long.class, true) {
                @Override
                public void setAsText(String text) {
                    if ("ten".equals(text)) {
                        setValue(10L);
                    } else {
                        super.setAsText(text);
                    }
                }
            };

            registry.registerCustomEditor(type, editor);
        }
    }
}
