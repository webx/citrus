package com.alibaba.citrus.turbine.pipeline.valve;

import static org.junit.Assert.*;

import org.junit.Test;

import com.alibaba.citrus.service.pipeline.impl.PipelineImpl;
import com.alibaba.citrus.turbine.TurbineRunData;
import com.alibaba.citrus.turbine.pipeline.valve.PageAuthorizationValve.Callback;

public class PageAuthorizationValveTests extends AbstractValveTests {
    @Override
    public void init() {
        pipeline = (PipelineImpl) factory.getBean("pageAuthValve");
        assertNotNull(pipeline);
    }

    @Test
    public void user() throws Exception {
        getInvocationContext("http://localhost/app1/aaa/myModule.jsp?action=aaa/MyAction");
        initRequestContext();

        Status status = MyCallback.newStatus();
        pipeline.newInvocation().invoke();
        assertFalse(status.allowed);
        assertTrue(status.denied);

        status = MyCallback.newStatus();
        status.user = "baobao";
        pipeline.newInvocation().invoke();
        assertTrue(status.allowed);
        assertFalse(status.denied);
    }

    @Test
    public void role() throws Exception {
        getInvocationContext("http://localhost/app1/aaa/myModule.jsp?action=aaa/MyAction");
        initRequestContext();

        Status status = MyCallback.newStatus();
        status.user = "other";
        pipeline.newInvocation().invoke();
        assertFalse(status.allowed);
        assertTrue(status.denied);

        status = MyCallback.newStatus();
        status.user = "other";
        status.roles = new String[] { "admin" };
        pipeline.newInvocation().invoke();
        assertTrue(status.allowed);
        assertFalse(status.denied);
    }

    @Test
    public void action() throws Exception {
        getInvocationContext("http://localhost/app1/bbb/myModule.jsp?action=MyAction");
        initRequestContext();

        Status status = MyCallback.newStatus();
        pipeline.newInvocation().invoke();
        assertFalse(status.allowed);
        assertTrue(status.denied);

        getInvocationContext("http://localhost/app1/bbb/myModule.jsp?action=bbb/MyAction");
        initRequestContext();

        status = MyCallback.newStatus();
        pipeline.newInvocation().invoke();
        assertTrue(status.allowed);
        assertFalse(status.denied);

        status = MyCallback.newStatus();
        status.actions = new String[] { "myaction" };
        pipeline.newInvocation().invoke();
        assertFalse(status.allowed);
        assertTrue(status.denied);
    }

    @Test
    public void action_screen() throws Exception {
        getInvocationContext("http://localhost/app1/ccc/myModule.jsp?action=ccc/MyAction");
        initRequestContext();

        Status status = MyCallback.newStatus();
        pipeline.newInvocation().invoke();
        assertFalse(status.allowed);
        assertTrue(status.denied);
    }

    @Test
    public void actionEvent() throws Exception {
        getInvocationContext("http://localhost/app1/ddd/myModule.jsp?action=ddd.MyAction");
        initRequestContext();

        Status status = MyCallback.newStatus();
        pipeline.newInvocation().invoke();
        assertFalse(status.allowed);
        assertTrue(status.denied);

        getInvocationContext("http://localhost/app1/ddd/myModule.jsp?action=ddd/MyAction&event_submit_do_my_event=y");
        initRequestContext();

        status = MyCallback.newStatus();
        pipeline.newInvocation().invoke();
        assertTrue(status.allowed);
        assertFalse(status.denied);
    }

    public static class MyCallback implements Callback<Status> {
        private static ThreadLocal<Status> statusHolder = new ThreadLocal<Status>();

        public static Status getStatus() {
            Status status = statusHolder.get();
            assertNotNull(status);
            return status;
        }

        public static Status newStatus() {
            Status status = new Status();
            statusHolder.set(status);
            return status;
        }

        public String getUserName(Status status) {
            return getStatus().user;
        }

        public String[] getRoleNames(Status status) {
            return getStatus().roles;
        }

        public String[] getActions(Status status) {
            return getStatus().actions;
        }

        public Status onStart(TurbineRunData rundata) throws Exception {
            return getStatus();
        }

        public void onAllow(Status status) throws Exception {
            getStatus().allowed = true;
        }

        public void onDeny(Status status) throws Exception {
            getStatus().denied = true;
        }
    }

    private static class Status {
        String user;
        String[] roles;
        String[] actions;
        boolean allowed;
        boolean denied;
    }
}
