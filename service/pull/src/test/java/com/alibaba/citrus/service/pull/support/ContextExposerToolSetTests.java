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

package com.alibaba.citrus.service.pull.support;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;

import com.alibaba.citrus.service.pull.AbstractPullServiceConfigTests;
import com.alibaba.citrus.service.pull.PullService;
import org.junit.Test;

public class ContextExposerToolSetTests extends AbstractPullServiceConfigTests {
    @Test
    public void withoutBeanName() throws Exception {
        pullService = (PullService) factory.getBean("pullService");

        @SuppressWarnings("unchecked")
        Map<String, Integer> mymap = (Map<String, Integer>) pullService.getContext().pull("mymap");

        assertArrayEquals(new String[] { "a", "b" }, mymap.keySet().toArray(new String[mymap.size()]));
        assertEquals(new Integer(111), mymap.get("a"));
        assertEquals(new Integer(222), mymap.get("b"));
    }

    @Test
    public void withBeanName() throws Exception {
        pullService = (PullService) factory.getBean("pullService");

        @SuppressWarnings("unchecked")
        List<Integer> mylist = (List<Integer>) pullService.getContext().pull("mylist2");

        assertArrayEquals(new Integer[] { 333, 444, 555 }, mylist.toArray(new Integer[mylist.size()]));
    }
}
