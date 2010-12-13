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
package com.alibaba.citrus.util.collection;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public abstract class AbstractTests {
    protected final boolean isEqual(Object o1, Object o2) {
        return o1 == null ? o2 == null : o1.equals(o2);
    }

    protected final <T> T cloneBySerialization(T obj) {
        if (obj == null || obj instanceof Serializable) {
            try {
                ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(byteStream);

                oos.writeObject(obj);

                ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(byteStream.toByteArray()));

                @SuppressWarnings("unchecked")
                T copy = (T) ois.readObject();

                return copy;
            } catch (ClassNotFoundException cnfe) {
                throw new RuntimeException("Failed deep cloning object", cnfe);
            } catch (IOException ioe) {
                throw new RuntimeException("Failed deep cloning object", ioe);
            }
        } else {
            throw new UnsupportedOperationException("Object is not serializable");
        }
    }
}
