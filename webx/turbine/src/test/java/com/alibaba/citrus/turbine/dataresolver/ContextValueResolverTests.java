/*
 * Copyright (c) 2002-2012 Alibaba Group Holding Limited.
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
 */

package com.alibaba.citrus.turbine.dataresolver;

import static com.alibaba.citrus.test.TestUtil.*;
import static org.junit.Assert.*;

import com.alibaba.citrus.service.moduleloader.Module;
import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.TurbineRunDataInternal;
import com.alibaba.citrus.turbine.support.MappedContext;
import com.alibaba.citrus.turbine.util.TurbineUtil;
import org.junit.Test;

public class ContextValueResolverTests extends AbstractDataResolverTests {
    private TurbineRunDataInternal rundata;
    private Module                 module;

    @Test
    public void getInt() throws Exception {
        // default value
        request("action", "context.myAction", "doGetInt");
        module.execute();
        assertLog("actionLog", Integer.class, 0);

        // wrong type
        request("action", "context.myAction", "doGetInt");
        rundata.getContext().put("aaa", "string");
        module.execute();
        assertLog("actionLog", Integer.class, 0);

        // right type
        request("action", "context.myAction", "doGetInt");
        rundata.getContext().put("aaa", 123);
        module.execute();
        assertLog("actionLog", Integer.class, 123);
    }

    @Test
    public void getString() throws Exception {
        // default value
        request("action", "context.myAction", "doGetString");
        module.execute();
        assertLog("actionLog", String.class, null);

        // wrong type
        request("action", "context.myAction", "doGetString");
        rundata.getContext().put("aaa", new Object());
        module.execute();
        assertLog("actionLog", String.class, null);

        // right type
        request("action", "context.myAction", "doGetString");
        rundata.getContext().put("aaa", "sss");
        module.execute();
        assertLog("actionLog", String.class, "sss");

        request("action", "context.myAction", "doGetString");
        rundata.getContext().put("aaa", "");
        module.execute();
        assertLog("actionLog", String.class, "");
    }

    @Test
    public void controlContext() throws Exception {
        request("action", "context.myAction", "doGetString");

        Context context = new MappedContext();
        context.put("aaa", "sss");
        rundata.pushContext(context);

        module.execute();
        assertLog("actionLog", String.class, "sss");

        rundata.popContext();

        module.execute();
        assertLog("actionLog", String.class, null);
    }

    @Test
    public void noName() throws Exception {
        try {
            request("action", "context.myActionWrong", "doWrong");
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e, exception("missing @ContextValue's name: DataResolverContext"));
        }
    }

    protected void request(String moduleType, String moduleName, String eventName) throws Exception {
        getInvocationContext("/app1?event_submit_" + eventName + "=yes");
        initRequestContext();

        rundata = (TurbineRunDataInternal) TurbineUtil.getTurbineRunData(newRequest);
        module = moduleLoaderService.getModule(moduleType, moduleName);
    }
}
