package com.alibaba.citrus.service.pull.impl;

import static com.alibaba.citrus.test.TestUtil.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static org.junit.Assert.*;

import java.util.Set;

import org.junit.Test;

import com.alibaba.citrus.service.pull.impl.PullServiceImpl.ToolName;

public class ToolNameTests {
    @Test
    public void names() {
        try {
            new ToolName(null, "  ", false);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("tool name"));
        }

        assertToolName("/test", "test", new ToolName(null, " test ", false));
        assertToolName("/test.1", "test.1", new ToolName(null, " test.1 ", false));
        assertToolName("/test/xxx", "test/xxx", new ToolName("  ", " test/xxx ", false));

        assertToolName("/prefix/test", "test", new ToolName(" prefix ", " test ", false));
        assertToolName("/prefix/test.1", "test.1", new ToolName(" prefix ", " test.1 ", false));
        assertToolName("/prefix/test/xxx", "test/xxx", new ToolName("prefix", " test/xxx ", false));

        assertToolName("/prefix/test", "test", new ToolName(" prefix ", " /test ", true));
        assertToolName("/prefix/test.1", "test.1", new ToolName(null, " /prefix/test.1 ", true));
        assertToolName("/prefix/test/xxx", "xxx", new ToolName("/prefix", " test/xxx ", true));
    }

    @Test
    public void hashCodeEquals() {
        Set<ToolName> tools = createHashSet();
        ToolName t1;
        ToolName t2;

        t1 = new ToolName(null, " test ", false);
        t2 = new ToolName(null, "test", false);
        tools.clear();
        tools.add(t1);
        assertTrue(tools.contains(t2));
        assertEquals(0, t1.compareTo(t2));

        t1 = new ToolName(null, "prefix/test/xxx", true);
        t2 = new ToolName("prefix/test", "xxx", false);
        tools.clear();
        tools.add(t1);
        assertTrue(tools.contains(t2));
        assertEquals(0, t1.compareTo(t2));

        assertFalse(t1.equals(null));
        assertFalse(t1.equals(123));
        assertTrue(t1.equals(t1));
    }

    private void assertToolName(String qname, String name, ToolName tname) {
        assertEquals(qname, tname.getQualifiedName());
        assertEquals(qname, tname.toString());
        assertEquals(name, tname.getName());
    }
}
