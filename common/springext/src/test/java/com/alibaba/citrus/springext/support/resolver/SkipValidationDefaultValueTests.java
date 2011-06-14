package com.alibaba.citrus.springext.support.resolver;

import static com.alibaba.citrus.test.TestEnvStatic.*;
import static org.junit.Assert.*;

import java.io.File;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.AutowireCandidateQualifier;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.FileSystemResource;

import com.alibaba.citrus.springext.support.context.XmlApplicationContext;

public class SkipValidationDefaultValueTests {
    private static ApplicationContext appContext;
    private static DefaultListableBeanFactory factory;

    @BeforeClass
    public static void init() {
        System.setProperty("skipValidation", "true");

        try {
            appContext = new XmlApplicationContext(new FileSystemResource(new File(srcdir, "beans-default-values.xml")));
        } finally {
            System.clearProperty("skipValidation");
        }

        factory = (DefaultListableBeanFactory) appContext.getAutowireCapableBeanFactory();
    }

    @Test
    public void useDefaults() {
        AbstractBeanDefinition bd = (AbstractBeanDefinition) factory.getBeanDefinition("myString1");

        assertEquals(true, bd.isLazyInit());
        assertEquals(true, bd.isAutowireCandidate());
        assertEquals(AbstractBeanDefinition.AUTOWIRE_BY_NAME, bd.getAutowireMode());
        assertEquals(AbstractBeanDefinition.DEPENDENCY_CHECK_SIMPLE, bd.getDependencyCheck());

        assertEquals(1, bd.getQualifiers().size());
        AutowireCandidateQualifier q = (AutowireCandidateQualifier) bd.getQualifiers().iterator().next();
        assertEquals(Qualifier.class.getName(), q.getTypeName());
    }

    @Test
    public void overrideDefaults() {
        AbstractBeanDefinition bd = (AbstractBeanDefinition) factory.getBeanDefinition("myString2");

        assertEquals(false, bd.isLazyInit());
        assertEquals(true, bd.isAutowireCandidate());
        assertEquals(AbstractBeanDefinition.AUTOWIRE_NO, bd.getAutowireMode());
        assertEquals(AbstractBeanDefinition.DEPENDENCY_CHECK_NONE, bd.getDependencyCheck());

        assertEquals(1, bd.getQualifiers().size());
        AutowireCandidateQualifier q = (AutowireCandidateQualifier) bd.getQualifiers().iterator().next();
        assertEquals(Test.class.getName(), q.getTypeName());
    }

    @Test
    public void useDefaults_notAutowireCandidate() {
        AbstractBeanDefinition bd = (AbstractBeanDefinition) factory.getBeanDefinition("myString3");

        assertEquals(true, bd.isLazyInit());
        assertEquals(false, bd.isAutowireCandidate());
        assertEquals(AbstractBeanDefinition.AUTOWIRE_BY_NAME, bd.getAutowireMode());
        assertEquals(AbstractBeanDefinition.DEPENDENCY_CHECK_SIMPLE, bd.getDependencyCheck());

        assertEquals(0, bd.getQualifiers().size());
    }
}
