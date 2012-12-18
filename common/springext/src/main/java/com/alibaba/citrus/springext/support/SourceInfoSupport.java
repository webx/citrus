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

package com.alibaba.citrus.springext.support;

import static com.alibaba.citrus.util.Assert.*;

import com.alibaba.citrus.springext.ResourceResolver.Resource;
import com.alibaba.citrus.springext.SourceInfo;

public class SourceInfoSupport<P extends SourceInfo<?>> implements SourceInfo<P> {
    private final P parent;

    private Resource source;
    private int lineNumber = -1;

    public SourceInfoSupport() {
        this.parent = null;
    }

    public SourceInfoSupport(P parent) {
        this.parent = assertNotNull(parent, "no parent sourceInfo provided");
    }

    public P getParent() {
        return parent;
    }

    public SourceInfo<P> setSource(Resource source) {
        setSource(source, -1);
        return this;
    }

    public SourceInfo<P> setSource(Resource source, int lineNumber) {
        this.source = source;
        this.lineNumber = (source != null && lineNumber > 0) ? lineNumber : -1;
        return this;
    }

    public Resource getSource() {
        return source;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    @Override
    public String toString() {
        if (source != null) {
            if (lineNumber > 0) {
                return source + " (line " + lineNumber + ")";
            } else {
                return source.toString();
            }
        } else {
            return getClass().getSimpleName();
        }
    }
}
