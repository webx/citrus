package com.alibaba.citrus.webx.util;

import static com.alibaba.citrus.test.TestUtil.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.junit.Test;

public class RequestURIFilterTests {
    private RequestURIFilter filter;

    @Test
    public void patterns() throws Exception {
        Pattern[] patterns;

        // no patterns - default value
        patterns = getPatterns(null);
        assertNull(patterns);

        // set empty patterns
        patterns = getPatterns(" ");
        assertNull(patterns);

        patterns = getPatterns(" \r\n, ");
        assertNull(patterns);

        // with patterns
        patterns = getPatterns("/aa , *.jpg");
        assertEquals(2, patterns.length);

        patterns = getPatterns("/aa  *.jpg");
        assertEquals(2, patterns.length);

        patterns = getPatterns("/aa\r\n*.jpg");
        assertEquals(2, patterns.length);
    }

    private Pattern[] getPatterns(String s) {
        filter = new RequestURIFilter(s);
        return getFieldValue(filter, "patterns", Pattern[].class);
    }

    @Test
    public void matches() throws Exception {
        filter = new RequestURIFilter("/aa , /bb/test*, *.jpg");

        assertMatches(true, "/aa/bb");
        assertMatches(false, "/aabb");
        assertMatches(true, "/bb/testcc/dd");
        assertMatches(true, "/bb/test/cc/dd");
        assertMatches(false, "/cc/aa/bb");

        assertMatches(true, "/cc/test.jpg");
        assertMatches(true, "/cc/aa/bb/test.jpg");

        assertMatches(false, "/cc/aa/bb/test.htm");
    }

    private void assertMatches(boolean matches, String requestURI) throws Exception {
        HttpServletRequest request = createMock(HttpServletRequest.class);

        expect(request.getRequestURI()).andReturn(requestURI).anyTimes();
        replay(request);

        assertEquals(matches, filter.matches(request));

        verify(request);
    }

    @Test
    public void toString_() {
        filter = new RequestURIFilter(null);
        assertEquals("FilterOf[]", filter.toString());

        filter = new RequestURIFilter("/aa , *.jpg");
        assertEquals("FilterOf [\n" + //
                "  [1/2] /aa\n" + //
                "  [2/2] *.jpg\n" + //
                "]", filter.toString());
    }
}
