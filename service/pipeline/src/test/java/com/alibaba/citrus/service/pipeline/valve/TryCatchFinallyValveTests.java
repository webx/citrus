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
package com.alibaba.citrus.service.pipeline.valve;

import static com.alibaba.citrus.test.TestUtil.*;
import static org.junit.Assert.*;

import org.junit.Test;

import com.alibaba.citrus.service.pipeline.PipelineException;
import com.alibaba.citrus.service.pipeline.impl.valve.TryCatchFinallyValve;

public class TryCatchFinallyValveTests extends AbstractValveTests<TryCatchFinallyValve> {
    @Test
    public void setTry() {
        assertNull(valve.getTry());

        pipeline = createPipeline();
        valve.setTry(pipeline);
        assertSame(pipeline, valve.getTry());
    }

    @Test
    public void setCatch() {
        assertNull(valve.getCatch());

        pipeline = createPipeline();
        valve.setCatch(pipeline);
        assertSame(pipeline, valve.getCatch());
    }

    @Test
    public void setFinally() {
        assertNull(valve.getFinally());

        pipeline = createPipeline();
        valve.setFinally(pipeline);
        assertSame(pipeline, valve.getFinally());
    }

    @Test
    public void setExceptionName() {
        // default value
        assertEquals("exception", valve.getExceptionName());

        // set value
        valve.setExceptionName("testException");
        assertEquals("testException", valve.getExceptionName());
    }

    @Test
    public void toString_() {
        String str;

        // empty
        assertEquals("TryCatchFinally{}", valve.toString());

        // with try
        valve.setTry(createPipeline(new LogValve()));

        str = "";
        str += "TryCatchFinally {\n";
        str += "  try = Pipeline [\n";
        str += "          [1/1] LogValve\n";
        str += "        ]\n";
        str += "}";

        assertEquals(str, valve.toString());

        // with catch
        valve.setCatch(createPipeline(new LogValve(), new LogValve()));

        str = "";
        str += "TryCatchFinally {\n";
        str += "  try   = Pipeline [\n";
        str += "            [1/1] LogValve\n";
        str += "          ]\n";
        str += "  catch = Pipeline [\n";
        str += "            [1/2] LogValve\n";
        str += "            [2/2] LogValve\n";
        str += "          ]\n";
        str += "}";

        assertEquals(str, valve.toString());

        // with finally
        valve.setFinally(createPipeline(new LogValve(), new LogValve(), new LogValve()));

        str = "";
        str += "TryCatchFinally {\n";
        str += "  try     = Pipeline [\n";
        str += "              [1/1] LogValve\n";
        str += "            ]\n";
        str += "  catch   = Pipeline [\n";
        str += "              [1/2] LogValve\n";
        str += "              [2/2] LogValve\n";
        str += "            ]\n";
        str += "  finally = Pipeline [\n";
        str += "              [1/3] LogValve\n";
        str += "              [2/3] LogValve\n";
        str += "              [3/3] LogValve\n";
        str += "            ]\n";
        str += "}";

        assertEquals(str, valve.toString());

        // with exceptionName
        valve.setExceptionName("myexception");

        str = "";
        str += "TryCatchFinally {\n";
        str += "  try           = Pipeline [\n";
        str += "                    [1/1] LogValve\n";
        str += "                  ]\n";
        str += "  catch         = Pipeline [\n";
        str += "                    [1/2] LogValve\n";
        str += "                    [2/2] LogValve\n";
        str += "                  ]\n";
        str += "  finally       = Pipeline [\n";
        str += "                    [1/3] LogValve\n";
        str += "                    [2/3] LogValve\n";
        str += "                    [3/3] LogValve\n";
        str += "                  ]\n";
        str += "  exceptionName = myexception\n";
        str += "}";

        assertEquals(str, valve.toString());
    }

    @Test
    public void invoke_empty() {
        pipeline = createPipeline(new LogValve(), valve, new LogValve());

        // empty valve
        assertInvoke(pipeline, false);
        assertLog("1-1", "1-3");
    }

    @Test
    public void invoke_try() {
        pipeline = createPipeline(new LogValve(), valve, new LogValve());

        // with try, no catch，异常被抛出
        valve.setTry(createPipeline(new LogValve(), new WrongValve(), new LogValve()));

        try {
            pipeline.newInvocation().invoke();
            fail();
        } catch (PipelineException e) {
            assertThat(e, exception(IllegalArgumentException.class, "something wrong"));
        }

        assertLog("1-1" /* 1-2 tryCatchFinally */, //
                "2-1", "2-2" /* exception */);
    }

    @Test
    public void invoke_try_finally() {
        pipeline = createPipeline(new LogValve(), valve, new LogValve());

        // no catch, with finally，异常被抛出，但finally被执行
        valve.setTry(createPipeline(new LogValve(), new WrongValve(), new LogValve()));
        valve.setFinally(createPipeline(new LogValve(), new LogValve(), new LogValve()));

        try {
            pipeline.newInvocation().invoke();
            fail();
        } catch (PipelineException e) {
            assertThat(e, exception(IllegalArgumentException.class, "something wrong"));
        }

        assertLog("1-1" /* 1-2 tryCatchFinally */, //
                /* try */"2-1", "2-2" /* exception */, //
                /* finally */"2-1", "2-2", "2-3");
    }

    @Test
    public void invoke_try_catch() {
        pipeline = createPipeline(new LogValve(), valve, new LogValve());

        // with catch
        valve.setTry(createPipeline(new LogValve(), new WrongValve(), new LogValve()));
        valve.setCatch(createPipeline(new LogValve(), new RecoveryValve(), new LogValve()));

        assertInvoke(pipeline, false);
        assertLog("1-1" /* 1-2 tryCatchFinally */, //
                /* try */"2-1", "2-2" /* exception */, //
                /* catch */"2-1", "2-2", "2-3", //
                "1-3");
    }

    @Test
    public void invoke_try_catch_failed() {
        pipeline = createPipeline(new LogValve(), valve, new LogValve());

        // with catch
        valve.setTry(createPipeline(new LogValve(), new WrongValve(), new LogValve()));
        valve.setCatch(createPipeline(new LogValve(), new RecoveryValve("myexception"), new LogValve()));

        try {
            pipeline.newInvocation().invoke();
            fail();
        } catch (AssertionError e) {
            assertThat(e, exception("something wrong"));
        }

        assertLog("1-1" /* 1-2 tryCatchFinally */, //
                /* try */"2-1", "2-2" /* exception */, //
                /* catch */"2-1", "2-2" //
        );
    }

    @Test
    public void invoke_try_catch_customized_exceptionName() {
        pipeline = createPipeline(new LogValve(), valve, new LogValve());

        // with catch
        valve.setTry(createPipeline(new LogValve(), new WrongValve(), new LogValve()));
        valve.setCatch(createPipeline(new LogValve(), new RecoveryValve("myexception"), new LogValve()));
        valve.setExceptionName("myexception");

        assertInvoke(pipeline, false);
        assertLog("1-1" /* 1-2 tryCatchFinally */, //
                /* try */"2-1", "2-2" /* exception */, //
                /* catch */"2-1", "2-2", "2-3", //
                "1-3");
    }

    @Test
    public void invoke_try_catch_finally() {
        pipeline = createPipeline(new LogValve(), valve, new LogValve());

        // with catch and finally
        valve.setTry(createPipeline(new LogValve(), new WrongValve(), new LogValve()));
        valve.setCatch(createPipeline(new LogValve(), new RecoveryValve(), new LogValve()));
        valve.setFinally(createPipeline(new LogValve(), new LogValve(), new LogValve()));

        assertInvoke(pipeline, false);
        assertLog("1-1" /* 1-2 tryCatchFinally */, //
                /* try */"2-1", "2-2" /* exception */, //
                /* catch */"2-1", "2-2", "2-3", //
                /* finally */"2-1", "2-2", "2-3", //
                "1-3");
    }

    @Test
    public void config() {
        // empty valve
        pipeline = getPipelineImplFromFactory("tcf-empty");
        assertInvoke(pipeline, false);
        assertLog("1-1", "1-3");

        // try
        pipeline = getPipelineImplFromFactory("tcf-try");

        try {
            pipeline.newInvocation().invoke();
            fail();
        } catch (PipelineException e) {
            assertThat(e, exception(IllegalArgumentException.class, "something wrong"));
        }

        assertLog("1-1" /* 1-2 tryCatchFinally */, //
                "2-1", "2-2" /* exception */);

        // try-catch
        pipeline = getPipelineImplFromFactory("tcf-try-catch");

        assertInvoke(pipeline, false);
        assertLog("1-1" /* 1-2 tryCatchFinally */, //
                /* try */"2-1", "2-2" /* exception */, //
                /* catch */"2-1", "2-2", "2-3", //
                "1-3");

        // try-catch-finally, using customized exceptionName, using label
        pipeline = getPipelineImplFromFactory("tcf-try-catch-finally");

        assertInvoke(pipeline, false);
        assertLog("1-1" /* 1-2 tryCatchFinally */, //
                /* try */"2-1", "2-2" /* exception */, //
                /* catch */"2-1", "2-2", "2-3", //
                /* finally */"2-1" /* 2-2 break */, //
                "1-3");

        // broken try
        pipeline = getPipelineImplFromFactory("tcf-broken-try");

        assertInvoke(pipeline, true);
        assertLog("1-1" /* 1-2 tryCatchFinally */, //
                /* try */"2-1" /* 2-2 break to top */, //
                /* finally */"2-1", "2-2" //
        );

        // pipeline reference
        pipeline = getPipelineImplFromFactory("tcf-ref");

        assertInvoke(pipeline, false);
        assertLog("1-1" /* 1-2 tryCatchFinally */, //
                /* try */"2-1", "2-2"/* exception */, //
                /* catch */"2-1", //
                /* finally */"2-1", "2-2", "2-3", //
                "1-3");
    }
}
