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
package com.alibaba.citrus.service.requestcontext.session.impl;

import static org.easymock.classextension.EasyMock.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.alibaba.citrus.service.requestcontext.session.SessionModel;

public class SessionModelEncoderTests extends AbstractSessionModelEncoderTests {
    private SessionModelEncoderImpl encoder;

    @Before
    public void init() {
        encoder = new SessionModelEncoderImpl();
    }

    @Test
    public void encode() {
        model = factory.newInstance("myId", 1111111111111111111L, 2222222222222222222L, 333333333);
        assertEquals("{id:\"myId\",ct:1111111111111111111,ac:2222222222222222222,mx:333333333}", encoder.encode(model));

        model = factory.newInstance(null, 0, 0, -1);
        assertEquals("{id:\"\",ct:0,ac:0,mx:-1}", encoder.encode(model));
    }

    @Test
    public void decode() {
        // test1
        SessionModel.Factory mockFactory = createMockFactory("myId", 1111111111111111111L, 2222222222222222222L,
                333333333);
        assertSame(model,
                encoder.decode("{id:\"myId\",ct:1111111111111111111,ac:2222222222222222222,mx:333333333}", mockFactory));
        verify(mockFactory);

        // test2
        mockFactory = createMockFactory(null, 0, 0, -1);
        assertSame(model, encoder.decode("{id:\"\",ct:0,ac:0,mx:-1}", mockFactory));
        verify(mockFactory);
    }
}
