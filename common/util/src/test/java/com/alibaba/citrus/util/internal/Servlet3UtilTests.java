package com.alibaba.citrus.util.internal;

import static org.junit.Assert.*;

import org.junit.Test;

public class Servlet3UtilTests {
    @Test
    public void isServlet3() {
        try {
            Servlet3UtilTests.class.getClassLoader().loadClass("javax.servlet.AsyncListener");
            assertTrue(Servlet3Util.isServlet3());
        } catch (ClassNotFoundException e) {
            assertFalse(Servlet3Util.isServlet3());
        }
    }
}
