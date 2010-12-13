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
package com.alibaba.citrus.service.resource;

import static com.alibaba.citrus.util.Assert.*;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * 代表一次资源搜索过程。
 * 
 * @author Michael Zhou
 */
public class ResourceTrace implements Iterable<ResourceTraceElement> {
    private final ResourceTraceElement[] elements;

    public ResourceTrace(Collection<ResourceTraceElement> elements) {
        this.elements = elements == null ? new ResourceTraceElement[0] : elements
                .toArray(new ResourceTraceElement[elements.size()]);
    }

    public int length() {
        return elements.length;
    }

    public Iterator<ResourceTraceElement> iterator() {
        return new Iterator<ResourceTraceElement>() {
            private int i;

            public boolean hasNext() {
                return i < elements.length;
            }

            public ResourceTraceElement next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }

                return elements[i++];
            }

            public void remove() {
                unsupportedOperation("remove()");
            }
        };
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();

        for (int i = 0; i < elements.length; i++) {
            buf.append(elements[i]);

            if (i < elements.length - 1) {
                buf.append("\n");
            }
        }

        return buf.toString();
    }
}
