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
package com.alibaba.citrus.util.templatelite;

import static com.alibaba.citrus.util.ArrayUtil.*;
import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.Assert.ExceptionType.*;
import static com.alibaba.citrus.util.ExceptionUtil.*;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 一个将template的内容输出到<code>Appendable</code>的visitor。
 * 
 * @author Michael Zhou
 */
public abstract class TextWriter<A extends Appendable> implements VisitorInvocationErrorHandler {
    private final static Logger log = LoggerFactory.getLogger(Template.class);
    private A out;

    public TextWriter() {
    }

    public TextWriter(A out) {
        setOut(out);
    }

    /**
     * 访问纯文本。
     */
    public final void visitText(String text) throws IOException {
        out().append(text);
    }

    /**
     * 取得用于输出的<code>Appendable</code>实例。
     */
    public final A out() {
        return assertNotNull(out, ILLEGAL_STATE, "setOut() not called yet");
    }

    /**
     * 设置用于输出的<code>Appendable</code>实例。
     */
    public final void setOut(A out) {
        this.out = out;
    }

    /**
     * 默认打印异常信息。
     */
    public void handleInvocationError(String desc, Throwable e) {
        log.debug("Error rendering " + desc, e);

        try {
            e = getRootCause(e);

            String msg = e.getClass().getSimpleName() + " - " + e.getMessage();

            StackTraceElement[] stackTrace = e.getStackTrace();

            if (!isEmptyArray(stackTrace)) {
                msg += " - " + stackTrace[0];
            }

            out.append(msg);
        } catch (Exception ee) {
            // ignore quietly
        }
    }
}
