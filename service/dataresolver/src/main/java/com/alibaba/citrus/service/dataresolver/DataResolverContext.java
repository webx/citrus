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
package com.alibaba.citrus.service.dataresolver;

import static com.alibaba.citrus.util.BasicConstant.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import com.alibaba.citrus.generictype.TypeInfo;
import com.alibaba.citrus.util.internal.ToStringBuilder;
import com.alibaba.citrus.util.internal.ToStringBuilder.MapBuilder;

public class DataResolverContext {
    public final static Annotation[] EMPTY_ANNOTATIONS = new Annotation[0];
    private final TypeInfo type;
    private final Annotation[] annotations;
    private final Object[] extraInfo;

    public DataResolverContext(Type type, Annotation[] annotations, Object[] extraInfo) {
        this.type = TypeInfo.factory.getType(type);
        this.annotations = annotations == null ? EMPTY_ANNOTATIONS : annotations;
        this.extraInfo = extraInfo == null ? EMPTY_OBJECT_ARRAY : extraInfo;
    }

    public TypeInfo getTypeInfo() {
        return type;
    }

    public Annotation[] getAnnotations() {
        return annotations;
    }

    public Object[] getExtraInfo() {
        return extraInfo;
    }

    public <A extends Annotation> A getAnnotation(Class<A> type) {
        for (Annotation anno : annotations) {
            if (type.isInstance(anno)) {
                return type.cast(anno);
            }
        }

        return null;
    }

    public <T> T getExtraObject(Class<T> type) {
        for (Object obj : extraInfo) {
            if (type.isInstance(obj)) {
                return type.cast(obj);
            }
        }

        return null;
    }

    @Override
    public String toString() {
        MapBuilder mb = new MapBuilder();

        mb.append("type", type);
        mb.append("annotations", annotations);
        mb.append("extraInfo", extraInfo);

        return new ToStringBuilder().append(getClass().getSimpleName()).append(mb).toString();
    }
}
