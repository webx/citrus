package com.alibaba.citrus.turbine.pipeline.valve;

import static com.alibaba.citrus.test.TestUtil.*;
import static org.junit.Assert.*;

import javax.servlet.http.HttpServletRequest;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.citrus.service.pipeline.PipelineContext;
import com.alibaba.citrus.service.pipeline.PipelineException;
import com.alibaba.citrus.service.pipeline.Valve;
import com.alibaba.citrus.service.pipeline.impl.PipelineImpl;
import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.TurbineRunData;
import com.alibaba.citrus.turbine.TurbineRunDataInternal;
import com.alibaba.citrus.turbine.util.TurbineUtil;
import com.alibaba.citrus.webx.util.WebxUtil;

public class PrepareForTurbineValveTests extends AbstractValveTests {
    @Test
    public void errorContext() throws Exception {
        pipeline = (PipelineImpl) factory.getBean("prepareForTurbine1");

        getInvocationContext("http://localhost/app1/aaa/bbb/myModule.vm");
        initRequestContext();

        try {
            pipeline.newInvocation().invoke();
            fail();
        } catch (PipelineException e) {
            assertThat(e, exception(IllegalArgumentException.class));
        }

        assertNull(request.getAttribute("_webx3_turbine_rundata"));

        Context savedContext = (Context) request.getAttribute("_webx3_turbine_rundata_context");
        assertNotNull(savedContext); // 保留context

        // 切换到root component，以模拟error处理的情形
        WebxUtil.setCurrentComponent(request, component.getWebxComponents().getComponent(null));

        pipeline = (PipelineImpl) factory.getBean("prepareForTurbine2");
        pipeline.newInvocation().invoke();

        assertNull(request.getAttribute("_webx3_turbine_rundata"));
        assertNull(request.getAttribute("_webx3_turbine_rundata_context")); // 清除context
    }

    private static TurbineRunData saved;

    public static class MyErrorValve implements Valve {
        @Autowired
        private HttpServletRequest request;

        public void invoke(PipelineContext pipelineContext) throws Exception {
            TurbineRunDataInternal rundata = (TurbineRunDataInternal) TurbineUtil.getTurbineRunData(request);
            saved = rundata;

            rundata.getContext().put("hello", "world");

            throw new IllegalArgumentException();
        }
    }

    public static class MyValve implements Valve {
        @Autowired
        private HttpServletRequest request;

        public void invoke(PipelineContext pipelineContext) throws Exception {
            TurbineRunDataInternal rundata = (TurbineRunDataInternal) TurbineUtil.getTurbineRunData(request);

            assertNotNull(saved);
            assertNotSame(saved, rundata);
            saved = null;

            // 第一个pipeline出错以后，第二个pipeline可以取得上个pipeline的context。
            // 这样，用于错误处理的exception pipleine就可以获得应用的context状态。
            assertEquals("world", rundata.getContext().get("hello"));

            // root context中不包含pull service，因此不存在control tool。
            assertNull(rundata.getContext().get("control"));
        }
    }
}
