package com.alibaba.citrus.webx.util;

import static com.alibaba.citrus.test.TestUtil.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.junit.Test;

public class ExcludeFilterTests {
    private ExcludeFilter filter;

    @Test
    public void excludes() throws Exception {
        Pattern[] excludes;

        // no excludes - default value
        excludes = getExcludes(null);
        assertNull(excludes);

        // set empty excludes
        excludes = getExcludes(" ");
        assertNull(excludes);

        excludes = getExcludes(" \r\n, ");
        assertNull(excludes);

        // with excludes
        excludes = getExcludes("/aa , *.jpg");
        assertEquals(2, excludes.length);

        excludes = getExcludes("/aa  *.jpg");
        assertEquals(2, excludes.length);

        excludes = getExcludes("/aa\r\n*.jpg");
        assertEquals(2, excludes.length);
    }

    private Pattern[] getExcludes(String s) {
        filter = new ExcludeFilter(s);
        return getFieldValue(filter, "excludes", Pattern[].class);
    }

    @Test
    public void isExcluded() throws Exception {
        filter = new ExcludeFilter("/aa , *.jpg");

        assertExcluded(true, "/aa/bb");
        assertExcluded(false, "/cc/aa/bb");

        assertExcluded(true, "/cc/test.jpg");
        assertExcluded(true, "/cc/aa/bb/test.jpg");

        assertExcluded(false, "/cc/aa/bb/test.htm");
    }

    private void assertExcluded(boolean excluded, String requestURI) throws Exception {
        HttpServletRequest request = createMock(HttpServletRequest.class);

        expect(request.getRequestURI()).andReturn(requestURI).anyTimes();
        replay(request);

        assertEquals(excluded, filter.isExcluded(request));

        verify(request);
    }

    @Test
    public void toString_() {
        filter = new ExcludeFilter(null);
        assertEquals("Excludes[]", filter.toString());

        filter = new ExcludeFilter("/aa , *.jpg");
        assertEquals("Excludes [\n" + //
                "  [1/2] /aa\n" + //
                "  [2/2] *.jpg\n" + //
                "]", filter.toString());
    }
}
