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
package com.alibaba.citrus.service.velocity.impl.parser;

import java.lang.reflect.Field;

import org.apache.velocity.runtime.parser.node.Node;
import org.apache.velocity.runtime.parser.node.SimpleNode;

public class SimpleNodeUtil {
    private static final Field childrenField;

    static {
        try {
            childrenField = SimpleNode.class.getDeclaredField("children");
            childrenField.setAccessible(true);
        } catch (Exception e) {
            throw new RuntimeException("Could not reflect on SimpleNode", e);
        }
    }

    public static void jjtSetChild(SimpleNode parent, Node child, int index) {
        Node[] children;

        try {
            children = (Node[]) childrenField.get(parent);
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not get children for node", e);
        }

        children[index] = child;
    }
}
