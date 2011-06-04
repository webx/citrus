package com.alibaba.citrus.service.upload;

import static com.alibaba.citrus.test.TestEnvStatic.*;
import static org.junit.Assert.*;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.FileSystemResource;

import com.alibaba.citrus.service.upload.impl.UploadServiceImpl;
import com.alibaba.citrus.springext.support.context.XmlApplicationContext;

@RunWith(Parameterized.class)
public class UploadServiceSkipValidationTests {
    private final boolean skipValidation;
    private ApplicationContext factory;
    private UploadServiceImpl upload;

    public UploadServiceSkipValidationTests(boolean skipValidation) {
        this.skipValidation = skipValidation;
    }

    @Parameters
    public static List<Object[]> data() {
        return Arrays.asList(new Object[][] { { false }, { true } });
    }

    @Before
    public void init() {
        if (skipValidation) {
            System.setProperty("skipValidation", "true");
        }

        factory = new XmlApplicationContext(new FileSystemResource(new File(srcdir, "services-skip-validation.xml")));
        upload = (UploadServiceImpl) factory.getBean("uploadService");
    }

    @After
    public void dispose() {
        System.clearProperty("skipValidation");
    }

    @Test
    public void test() {
        assertEquals(-1, upload.getSizeMax().getValue());
        assertEquals(-1, upload.getFileSizeMax().getValue());
        assertEquals("10K", upload.getSizeThreshold().getHumanReadable());
        assertArrayEquals(new String[] { "filename" }, upload.getFileNameKey());
    }
}
