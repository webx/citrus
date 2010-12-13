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
package com.alibaba.citrus.generictype.impl;

import static com.alibaba.citrus.util.CollectionUtil.*;
import static java.util.Collections.*;

import java.util.List;

import com.alibaba.citrus.generictype.GenericDeclarationInfo;
import com.alibaba.citrus.generictype.TypeInfo;
import com.alibaba.citrus.generictype.WildcardTypeInfo;

/**
 * 对{@link WildcardTypeInfo}的实现。
 * 
 * @author Michael Zhou
 */
class WildcardTypeImpl extends AbstractBoundedTypeInfo implements WildcardTypeInfo {
    private final List<TypeInfo> lowerBounds;
    private final boolean unknown;

    WildcardTypeImpl(TypeInfo[] upperBounds, TypeInfo[] lowerBounds) {
        super(upperBounds);
        this.lowerBounds = unmodifiableList(asList(lowerBounds));
        this.unknown = upperBounds.length == 1 && upperBounds[0].equals(TypeInfo.OBJECT);
    }

    public String getName() {
        return "?";
    }

    public String getSimpleName() {
        return "?";
    }

    @Override
    public List<TypeInfo> getLowerBounds() {
        return lowerBounds;
    }

    public boolean isUnknown() {
        return unknown;
    }

    // Implementation of TypeInfo.resolve
    public TypeInfo resolve(GenericDeclarationInfo context) {
        return resolve(context, true);
    }

    // Implementation of TypeInfo.resolve
    public TypeInfo resolve(GenericDeclarationInfo context, boolean includeBaseType) {
        if (unknown) {
            return this;
        } else {
            return getUpperBounds().get(0).resolve(context, includeBaseType); // baseType.resolve(context)
        }
    }

    /**
     * 取得字符串表示。
     */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();

        buf.append("?");

        List<TypeInfo> lowerBounds = getLowerBounds();

        // 显示upperBounds。 如果唯一的基类为Object，即unknown wildcard，就不需要特别显示了。
        if (!unknown) {
            buf.append(" extends ");
            join(buf, getUpperBounds(), " & ");

            if (!lowerBounds.isEmpty()) {
                buf.append(", ");
            }
        }

        // 显示lowerBounds。
        if (!lowerBounds.isEmpty()) {
            buf.append(" super ");
            join(buf, lowerBounds, " & ");
        }

        return buf.toString();
    }
}
