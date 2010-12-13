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
package com.alibaba.citrus.service.requestcontext.rewrite.impl;

import static com.alibaba.citrus.util.CollectionUtil.*;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import java.util.List;

/**
 * 由于flags属性的getter和setter方法具有不同的类型，所以必须人工实现beanInfo。
 * 
 * @author Michael Zhou
 */
public class RewriteSubstitutionBeanInfo extends SimpleBeanInfo {
    @Override
    public PropertyDescriptor[] getPropertyDescriptors() {
        List<PropertyDescriptor> descriptors;

        try {
            descriptors = createLinkedList(Introspector.getBeanInfo(RewriteSubstitution.class.getSuperclass())
                    .getPropertyDescriptors());

            descriptors.add(new PropertyDescriptor("uri", RewriteSubstitution.class, null, "setUri"));
            descriptors.add(new PropertyDescriptor("flags", RewriteSubstitution.class, null, "setFlags"));
            descriptors.add(new PropertyDescriptor("parameters", RewriteSubstitution.class, null, "setParameters"));
        } catch (IntrospectionException e) {
            return super.getPropertyDescriptors();
        }

        return descriptors.toArray(new PropertyDescriptor[descriptors.size()]);
    }
}
