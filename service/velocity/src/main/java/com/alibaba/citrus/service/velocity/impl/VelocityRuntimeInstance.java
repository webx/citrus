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
package com.alibaba.citrus.service.velocity.impl;

import java.io.Reader;

import org.apache.velocity.runtime.RuntimeInstance;
import org.apache.velocity.runtime.parser.ParseException;
import org.apache.velocity.runtime.parser.node.ASTStringLiteral;
import org.apache.velocity.runtime.parser.node.Node;
import org.apache.velocity.runtime.parser.node.SimpleNode;

import com.alibaba.citrus.service.velocity.impl.parser.ASTStringLiteralEnhanced;
import com.alibaba.citrus.service.velocity.impl.parser.SimpleNodeUtil;

/**
 * 扩展了velocity的<code>RuntimeInstance</code>类，实现一个功能，使
 * <code>ReferenceInsertionEventHandler</code>可以感知被拦截的引用是否位于
 * <code>StringLiteral</code>中。 例如：
 * <p>
 * <code>EscapeSupport</code>可以根据引用的位置，来决定是否要对结果进行escape转义。下面的velocity语句将不会被转义：
 * </p>
 * 
 * <pre>
 * #set ($value = "hello, $name")
 * </pre>
 * <p>
 * 通过调用<code>InterpolationUtil.isInInterpolation(context)</code>即可知晓此细节。
 * </p>
 * <p>
 * 通过velocity configuration：
 * <code>runtime.interpolate.string.literals.hack</code>可以开关此特性，默认值为
 * <code>true</code>。
 * </p>
 * 
 * @author Michael Zhou
 */
public class VelocityRuntimeInstance extends RuntimeInstance {
    private static final String INTERPOLATION_HACK_KEY = "runtime.interpolate.string.literals.hack";
    private static final Boolean INTERPOLATION_HACK_DEFAULT = true;
    private boolean interpolationHack;

    @Override
    public synchronized void init() throws Exception {
        super.init();
        interpolationHack = getConfiguration().getBoolean(INTERPOLATION_HACK_KEY, INTERPOLATION_HACK_DEFAULT);
    }

    @Override
    public SimpleNode parse(Reader reader, String templateName, boolean dumpNamespace) throws ParseException {
        SimpleNode node = super.parse(reader, templateName, dumpNamespace);

        if (interpolationHack) {
            node = traversNode(node);
        }

        return node;
    }

    private SimpleNode traversNode(SimpleNode node) {
        int length = node.jjtGetNumChildren();

        for (int i = 0; i < length; i++) {
            Node child = node.jjtGetChild(i);

            if (child instanceof ASTStringLiteral) {
                replaceStringLiteral(node, (ASTStringLiteral) child, i);
            }

            if (child instanceof SimpleNode) {
                traversNode((SimpleNode) child);
            }
        }

        return node;
    }

    private void replaceStringLiteral(SimpleNode parent, ASTStringLiteral strLit, int index) {
        if (!(strLit instanceof ASTStringLiteralEnhanced)) {
            SimpleNodeUtil.jjtSetChild(parent, new ASTStringLiteralEnhanced(strLit), index);
        }
    }
}
