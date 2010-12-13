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
package com.alibaba.citrus.service.requestcontext.buffered.impl;

import java.io.IOException;

import com.alibaba.citrus.service.requestcontext.RequestContext;
import com.alibaba.citrus.service.requestcontext.buffered.BufferCommitFailedException;
import com.alibaba.citrus.service.requestcontext.buffered.BufferedRequestContext;
import com.alibaba.citrus.service.requestcontext.support.AbstractRequestContextWrapper;
import com.alibaba.citrus.util.io.ByteArray;

/**
 * 对response.<code>getWriter()</code>和response.<code>getOutputStream()</code>
 * 所返回的输出流进行缓存操作。
 * 
 * @author Michael Zhou
 */
public class BufferedRequestContextImpl extends AbstractRequestContextWrapper implements BufferedRequestContext {
    /**
     * 包装一个<code>RequestContext</code>对象。
     * 
     * @param wrappedContext 被包装的<code>RequestContext</code>
     */
    public BufferedRequestContextImpl(RequestContext wrappedContext) {
        super(wrappedContext);

        setResponse(new BufferedResponseImpl(this, wrappedContext.getResponse()));
    }

    /**
     * 设置是否将所有信息保存在内存中。
     * 
     * @return 如果是，则返回<code>true</code>
     */
    public boolean isBuffering() {
        return getBufferedResponse().isBuffering();
    }

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
    public void setBuffering(boolean buffering) {
        getBufferedResponse().setBuffering(buffering);
    }

    /**
     * 创建新的buffer，保存老的buffer。
     * 
     * @throws IllegalStateException 如果不在buffer模式，或<code>getWriter</code>及
     *             <code>getOutputStream</code>方法从未被调用
     */
    public void pushBuffer() {
        getBufferedResponse().pushBuffer();
    }

    /**
     * 弹出最近的buffer，如果堆栈中只有一个buffer，则弹出后再创建一个新的。
     * 
     * @return 最近的buffer内容
     * @throws IllegalStateException 如果不在buffer模式，或<code>getWriter</code>
     *             方法曾被调用，或 <code>getOutputStream</code>方法从未被调用
     */
    public ByteArray popByteBuffer() {
        return getBufferedResponse().popByteBuffer();
    }

    /**
     * 弹出最近的buffer，如果堆栈中只有一个buffer，则弹出后再创建一个新的。
     * 
     * @return 最近的buffer内容
     * @throws IllegalStateException 如果不在buffer模式，或<code>getOutputStream</code>
     *             方法曾被调用，或<code>getWriter</code>方法从未被调用
     */
    public String popCharBuffer() {
        return getBufferedResponse().popCharBuffer();
    }

    /**
     * 将buffer中的内容提交到真正的servlet输出流中。
     * <p>
     * 如果从来没有执行过<code>getOutputStream</code>或<code>getWriter</code>
     * 方法，则该方法不做任何事情。
     * </p>
     * 
     * @throws BufferCommitFailedException 如果提交失败
     * @throws IllegalStateException 如果buffer栈中不止一个buffer
     */
    @Override
    public void commit() throws BufferCommitFailedException {
        if (getBufferedResponse().isBuffering()) {
            try {
                getBufferedResponse().commitBuffer();
            } catch (IOException e) {
                throw new BufferCommitFailedException(e);
            }
        }
    }

    /**
     * 取得<code>BufferedRunDataResponse</code>实例。
     * 
     * @return <code>BufferedRunDataResponse</code>实例
     */
    private BufferedResponseImpl getBufferedResponse() {
        return (BufferedResponseImpl) getResponse();
    }
}
