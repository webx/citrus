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

package app1.module.action.myprod;

import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.citrus.service.requestcontext.rundata.RunData

class MyAction {
    @Autowired
    RunData rundata;

    public void doSomething() throws Exception {
        rundata.setAttribute("handler", "doSomething");
    }

    public void doPerform() throws Exception {
        rundata.setAttribute("handler", "doPerform");
    }

    public void beforeExecution() {
        rundata.setAttribute("before", "yes");
    }

    public void afterExecution() {
        rundata.setAttribute("after", "yes");
    }
}
