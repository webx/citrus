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
package com.alibaba.citrus.service.requestcontext.buffered;

import com.alibaba.citrus.service.requestcontext.RequestContext;
import com.alibaba.citrus.util.io.ByteArray;

/**
 * 对response.<code>getWriter()</code>和response.<code>getOutputStream()</code>
 * 所返回的输出流进行缓存操作。
 * 
 * @author Michael Zhou
 */
public interface BufferedRequestContext extends RequestContext {
    /**
     * 设置是否将所有信息保存在内存中。
     * 
     * @return 如果是，则返回<code>true</code>
     */
    boolean isBuffering();

    /**
     * 设置buffer模式，如果设置成<code>true</code>，表示将所有信息保存在内存中，否则直接输出到原始response中。
     * <p>
     * 此方法必须在<code>getOutputStream</code>和<code>getWriter</code>方法之前执行，否则将抛出
     * <code>IllegalStateException</code>。
     * </p>
     * 
     * @param buffering 是否buffer内容
     * @throws IllegalStateException <code>getOutputStream</code>或
     *             <code>getWriter</code>方法已经被执行
     */
    void setBuffering(boolean buffering);

    /**
     * 创建新的buffer，保存老的buffer。
     * 
     * @throws IllegalStateException 如果不在buffer模式，或<code>getWriter</code>及
     *             <code>getOutputStream</code>方法从未被调用
     */
    void pushBuffer();

    /**
     * 弹出最近的buffer，如果堆栈中只有一个buffer，则弹出后再创建一个新的。
     * 
     * @return 最近的buffer内容
     * @throws IllegalStateException 如果不在buffer模式，或<code>getWriter</code>
     *             方法曾被调用，或 <code>getOutputStream</code>方法从未被调用
     */
    ByteArray popByteBuffer();

    /**
     * 弹出最近的buffer，如果堆栈中只有一个buffer，则弹出后再创建一个新的。
     * 
     * @return 最近的buffer内容
     * @throws IllegalStateException 如果不在buffer模式，或<code>getOutputStream</code>
     *             方法曾被调用，或<code>getWriter</code>方法从未被调用
     */
    String popCharBuffer();
}
