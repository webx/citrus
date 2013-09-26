package com.alibaba.citrus.util.internal;

import static org.junit.Assert.*;

import javax.servlet.http.HttpServletRequest;

import org.junit.Test;

public class Servlet3UtilTests {
    @Test
    public void isServlet3() {
        try {
            HttpServletRequest.class.getMethod("getAsyncContext");
            assertTrue(Servlet3Util.isServlet3());
        } catch (NoSuchMethodException e) {
            assertFalse(Servlet3Util.isServlet3());
        }
    }
}
