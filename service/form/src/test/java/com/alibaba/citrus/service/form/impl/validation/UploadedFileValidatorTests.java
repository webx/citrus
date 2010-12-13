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
package com.alibaba.citrus.service.form.impl.validation;

import static com.alibaba.citrus.test.TestEnvStatic.*;
import static com.alibaba.citrus.test.TestUtil.*;
import static org.junit.Assert.*;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import com.alibaba.citrus.util.HumanReadableSize;

public class UploadedFileValidatorTests extends AbstractValidatorTests<UploadedFileValidator> {
    private File txt_size3;
    private File txt_size10;
    private File txt_size4;
    private File gif_size4;
    private File jpg_size0;
    private File jpg_size5;
    private File notype_size4;

    @Before
    public void prepareFile() {
        txt_size3 = new File(srcdir, "data/file1.txt");
        txt_size10 = new File(srcdir, "data/file2.txt");
        txt_size4 = new File(srcdir, "data/file3.txt");
        gif_size4 = new File(srcdir, "data/file4.gif");
        jpg_size0 = new File(srcdir, "data/file5.jpg");
        jpg_size5 = new File(srcdir, "data/file6.jpg");
        notype_size4 = new File(srcdir, "data/file7");

        assertEquals(3, txt_size3.length());
        assertEquals(10, txt_size10.length());
        assertEquals(4, txt_size4.length());

        assertEquals(4, gif_size4.length());
        assertEquals(0, jpg_size0.length());
        assertEquals(5, jpg_size5.length());

        assertEquals(4, notype_size4.length());
    }

    @Override
    protected String getGroupName() {
        return "g";
    }

    @Test
    public void init_contentTypes() throws Exception {
        UploadedFileValidator v = newValidator();

        // empty
        v.setContentType(null);
        assertArrayEquals(null, v.getContentType());

        v.setContentType(new String[] {});
        assertArrayEquals(null, v.getContentType());

        v.setContentType(new String[] { " ", null });
        assertArrayEquals(null, v.getContentType());

        // with values
        v.setContentType(new String[] { " AAA", "", " bbb/CCC " });
        assertArrayEquals(new String[] { "aaa", "bbb/ccc" }, v.getContentType());
    }

    @Test
    public void init_sizeLimit() throws Exception {
        UploadedFileValidator v = newValidator();

        // default value
        assertEquals(-1, v.getMinSize().getValue());
        assertEquals(-1, v.getMaxSize().getValue());

        // set null
        try {
            v.setMinSize(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("minSize"));
        }

        try {
            v.setMaxSize(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("maxSize"));
        }

        // set values
        v.setMinSize(new HumanReadableSize(123));
        v.setMaxSize(new HumanReadableSize(456));

        assertEquals(123, v.getMinSize().getValue());
        assertEquals(456, v.getMaxSize().getValue());
    }

    /**
     * 无fileItems，也通过。如果不允许这种情况，则用required-validator来确保fileItems存在。
     */
    @Test
    public void validate_noFileItems() throws Exception {
        requestWithUpload("");
        assertEquals(true, field10.isValid());
        assertEquals(null, field10.getMessage());
    }

    /**
     * 无参数。
     */
    @Test
    public void validate_defaultConfig() throws Exception {
        requestWithUpload(txt_size3);
        assertEquals(true, field1.isValid());
        assertEquals(null, field1.getMessage());
    }

    /**
     * minSize=4
     */
    @Test
    public void validate_minSize() throws Exception {
        requestWithUpload("", new File[] { txt_size3, txt_size10 }); // size=3,10
        assertEquals(false, field2.isValid());
        assertEquals("field2 must be larger than 4", field2.getMessage());

        requestWithUpload("", txt_size10); // size=10
        assertEquals(true, field2.isValid());
        assertEquals(null, field2.getMessage());

        requestWithUpload("", txt_size4); // size=4
        assertEquals(true, field2.isValid());
        assertEquals(null, field2.getMessage());
    }

    /**
     * maxSize=4
     */
    @Test
    public void validate_maxSize() throws Exception {
        requestWithUpload("", "", new File[] { txt_size4, txt_size10 }); // size=4,10
        assertEquals(false, field3.isValid());
        assertEquals("field3 must be smaller than 4", field3.getMessage());

        requestWithUpload("", "", txt_size3); // size=3
        assertEquals(true, field3.isValid());
        assertEquals(null, field3.getMessage());

        requestWithUpload("", "", txt_size4); // size=4
        assertEquals(true, field3.isValid());
        assertEquals(null, field3.getMessage());
    }

    /**
     * minSize=4, maxSize=9
     */
    @Test
    public void validate_minSize_maxSize() throws Exception {
        requestWithUpload("", "", "", new File[] { txt_size4, txt_size10 }); // size=4,10
        assertEquals(false, field4.isValid());
        assertEquals("field4 should be in size between 4 and 9", field4.getMessage());

        requestWithUpload("", "", "", txt_size3); // size=3
        assertEquals(false, field4.isValid());
        assertEquals("field4 should be in size between 4 and 9", field4.getMessage());

        requestWithUpload("", "", "", txt_size4); // size=4
        assertEquals(true, field4.isValid());
        assertEquals(null, field4.getMessage());
    }

    /**
     * contentType=image/gif
     */
    @Test
    public void validate_contentType() throws Exception {
        requestWithUpload("", "", "", "", new File[] { txt_size3, gif_size4 });
        assertEquals(false, field5.isValid());
        assertEquals("field5 should be of content type [image/gif]", field5.getMessage());

        requestWithUpload("", "", "", "", txt_size3);
        assertEquals(false, field5.isValid());
        assertEquals("field5 should be of content type [image/gif]", field5.getMessage());

        requestWithUpload("", "", "", "", gif_size4);
        assertEquals(true, field5.isValid());
        assertEquals(null, field5.getMessage());
    }

    /**
     * contentType=image/gif, text/plain
     */
    @Test
    public void validate_contentTypes() throws Exception {
        requestWithUpload("", "", "", "", "", new File[] { txt_size3, gif_size4 });
        assertEquals(true, field6.isValid());
        assertEquals(null, field6.getMessage());

        requestWithUpload("", "", "", "", "", jpg_size0);
        assertEquals(false, field6.isValid());
        assertEquals("field6 should be of content type [image/gif, text/plain]", field6.getMessage());
    }

    /**
     * contentType=image/*, minSize=4, maxSize=9
     */
    @Test
    public void validate_minSize_maxSize_contentTypes() throws Exception {
        requestWithUpload("", "", "", "", "", "", new File[] { gif_size4, jpg_size0 });
        assertEquals(false, field7.isValid());
        assertEquals("field7 should be of content type [image/] and be in size between 4 and 9", field7.getMessage());

        requestWithUpload("", "", "", "", "", "", new File[] { gif_size4, jpg_size5 });
        assertEquals(true, field7.isValid());
        assertEquals(null, field7.getMessage());

        requestWithUpload("", "", "", "", "", "", jpg_size5);
        assertEquals(true, field7.isValid());
        assertEquals(null, field7.getMessage());
    }

    /**
     * minSize=0, maxSize=0
     */
    @Test
    public void validate_forceEmptyFile() throws Exception {
        requestWithUpload("", "", "", "", "", "", "", jpg_size0);
        assertEquals(true, field8.isValid());
        assertEquals(null, field8.getMessage());

        requestWithUpload("", "", "", "", "", "", "", new File[] { gif_size4, jpg_size5 });
        assertEquals(false, field8.isValid());
        assertEquals("field8 should be in size between 0 and 0", field8.getMessage());
    }

    @Test
    public void validate_noContentType() throws Exception {
        // minSize=4
        requestWithUpload("", notype_size4); // no ContentType, size=4
        assertEquals(true, field2.isValid());
        assertEquals(null, field2.getMessage());

        // contentType=image/gif
        requestWithUpload("", "", "", "", notype_size4); // no ContentType, size=4
        assertEquals(false, field5.isValid());
        assertEquals("field5 should be of content type [image/gif]", field5.getMessage());
    }

    @Test
    public void validate_extension() throws Exception {
        // ext=jpg
        requestWithUpload("", "", "", "", "", "", "", "", jpg_size5);
        assertEquals(true, field9.isValid());
        assertEquals(null, field9.getMessage());

        // ext=gif
        requestWithUpload("", "", "", "", "", "", "", "", gif_size4);
        assertEquals(false, field9.isValid());
        assertEquals("field9 should be of [jpg, null]", field9.getMessage());

        // ext=null
        requestWithUpload("", "", "", "", "", "", "", "", notype_size4);
        assertEquals(true, field9.isValid());
        assertEquals(null, field9.getMessage());
    }
}
