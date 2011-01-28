package com.alibaba.citrus.turbine.util;

import static com.alibaba.citrus.test.TestUtil.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.alibaba.citrus.turbine.Context;

public class ControlToolExportAllTests extends AbstractPullToolTests<ControlTool> {
    @Override
    protected String toolName() {
        return "controlExportAll";
    }

    @Before
    public void init() throws Exception {
        rundata.getResponse().getWriter();
    }

    @Test
    public void render_exportVars() throws Exception {
        Context context1 = rundata.getContext();
        Context context2 = rundata.getContext("app2");
        assertTrue(tool.exportAll);

        context1.put("var1", "init");
        context1.put("var2", "init");

        // no current context
        tool.setTemplate("control_set").render();
        assertEquals("init", context1.get("var1"));
        assertEquals("init", context1.get("var2"));

        // app1:context -> app1:control, without exports
        rundata.pushContext(context1);

        tool.setTemplate("control_set").render();
        assertEquals("app1", context1.get("var1"));
        assertEquals(null, context1.get("var2"));

        // app1:context -> app1:control.export(var1, var2)
        context1.put("var1", "init");
        context1.put("var2", "init");

        tool.setTemplate("control_set").export("var1").render();
        assertEquals("app1", context1.get("var1"));
        assertEquals(null, context1.get("var2"));

        context1.put("var1", "init");
        context1.put("var2", "init");

        tool.setTemplate("control_set").export("var2").render();
        assertEquals("app1", context1.get("var1"));
        assertEquals(null, context1.get("var2"));

        context1.put("var1", "init");
        context1.put("var2", "init");

        tool.setTemplate("control_set").export("var1", "var2").render();
        assertEquals("app1", context1.get("var1"));
        assertEquals(null, context1.get("var2"));

        // app1:context -> app1:control -> app2:control.export(var1, var2)
        context1.put("var1", "init");
        context1.put("var2", "init");
        context2.put("var1", "init");
        context2.put("var2", "init");

        String content = tool.setTemplate("control_nest").render();

        assertEquals("app2", context1.get("var1"));
        assertEquals(null, context1.get("var2"));
        assertEquals("init", context2.get("var1"));
        assertEquals("init", context2.get("var2"));

        assertThat(content, containsAll("1. app2", "2. $var2"));

        // app1:context -> app1:control.export(var1, var2) -> app2:control.export(var1, var2)
        context1.put("var1", "init");
        context1.put("var2", "init");
        context2.put("var1", "init");
        context2.put("var2", "init");

        content = tool.setTemplate("control_nest").export("var1", "var2").render();

        assertEquals("app2", context1.get("var1"));
        assertEquals(null, context1.get("var2"));
        assertEquals("init", context2.get("var1"));
        assertEquals("init", context2.get("var2"));

        assertThat(content, containsAll("1. app2", "2. $var2"));
    }
}
