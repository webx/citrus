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

import static org.easymock.EasyMock.*;
import static org.easymock.classextension.EasyMock.*;

import org.junit.Before;

import com.alibaba.citrus.service.requestcontext.session.SessionModel;
import com.alibaba.citrus.test.TestEnvStatic;

public abstract class AbstractSessionModelEncoderTests {
    protected SessionModel.Factory factory;
    protected SessionModel model;

    {
        TestEnvStatic.init();
    }

    @Before
    public final void initFactory() {
        factory = new SessionModel.Factory() {
            public SessionModel newInstance(String sessionID, long creationTime, long lastAccessedTime,
                                            int maxInactiveInterval) {
                SessionModel model = createMock(SessionModel.class);

                expect(model.getSessionID()).andReturn(sessionID).anyTimes();
                expect(model.getCreationTime()).andReturn(creationTime).anyTimes();
                expect(model.getLastAccessedTime()).andReturn(lastAccessedTime).anyTimes();
                expect(model.getMaxInactiveInterval()).andReturn(maxInactiveInterval).anyTimes();

                replay(model);

                return model;
            }
        };
    }

    protected SessionModel.Factory createMockFactory(String sessionID, long creationTime, long lastAccessedTime,
                                                     int maxInactiveInterval) {
        SessionModel.Factory mockFactory = createMock(SessionModel.Factory.class);

        expect(mockFactory.newInstance(sessionID, creationTime, lastAccessedTime, maxInactiveInterval))
                .andReturn(model);

        replay(mockFactory);

        return mockFactory;
    }
}
