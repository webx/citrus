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
package com.alibaba.citrus.service.configuration.impl;

import com.alibaba.citrus.service.configuration.MyBean1;
import com.alibaba.citrus.service.configuration.MyBean2;
import com.alibaba.citrus.service.configuration.MyBean3;
import com.alibaba.citrus.service.configuration.MyConfiguration;
import com.alibaba.citrus.service.configuration.support.PropertiesConfigurationSupport;

public class MyConfigurationImpl extends PropertiesConfigurationSupport<MyConfiguration> implements MyConfiguration {
    public final static String DEFAULT_NAME = "myConfiguration";

    public MyConfigurationImpl() {
        super();
    }

    public MyConfigurationImpl(MyConfigurationImpl parent) {
        super(parent);
    }

    @Override
    protected String getDefaultName() {
        return DEFAULT_NAME;
    }

    public String getStringValue() {
        return getProperty("stringValue", "string");
    }

    public void setStringValue(String value) {
        setProperty("stringValue", value);
    }

    public MyBean1 getMyBean1() {
        return getBean("myBean1", "myBean1", MyBean1.class);
    }

    public void setMyBean1Ref(String value) {
        setProperty("myBean1", value);
    }

    public MyBean2 getMyBean2() {
        return getBean("myBean2", "myBean2", MyBean2.class);
    }

    public void setMyBean2Ref(String value) {
        setProperty("myBean2", value);
    }

    public MyBean3 getMyBean3() {
        return getBean("myBean3", "myBean3", MyBean3.class, false);
    }

    public void setMyBean3Ref(String value) {
        setProperty("myBean3", value);
    }
}
