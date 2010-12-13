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
package com.alibaba.citrus.util.io;

import java.io.IOException;
import java.io.OutputStream;

/**
 * 增量地将数据源写入到指定输出流的引擎。
 * <p>
 * <code>OutputEngine</code>有什么用？举例说明。 <code>GZIPInputStream</code>是对压缩流进行解压缩：
 * </p>
 * 
 * <pre>
 * read 原始数据 &lt;- decompress &lt;- compressed data stream
 * </pre>
 * <p>
 * <code>GZIPOutputStream</code>是对输出流进行压缩：
 * </p>
 * 
 * <pre>
 * write 原始数据 -&gt; compress -&gt; compressed data stream
 * </pre>
 * <p>
 * 但是JDK中不存在这样一个流：
 * </p>
 * 
 * <pre>
 * read compressed data &lt;- compress &lt;- 原始数据流
 * </pre>
 * <p>
 * 利用OutputEngine就可以实现这样的流。
 * </p>
 * <p>
 * 本代码移植自IBM developer works文章：
 * </p>
 * <ul>
 * <li><a
 * href="http://www.ibm.com/developerworks/cn/java/j-io1/index.shtml">彻底转变流，第 1
 * 部分：从输出流中读取</a>
 * <li><a
 * href="http://www.ibm.com/developerworks/cn/java/j-io2/index.shtml">彻底转变流，第 2
 * 部分：优化 Java 内部 I/O</a>
 * </ul>
 * 
 * @author Michael Zhou
 */
public interface OutputEngine {
    /** 默认的输出流工厂, 直接返回指定的输出流. */
    OutputStreamFactory DEFAULT_OUTPUT_STREAM_FACTORY = new OutputStreamFactory() {
        public OutputStream getOutputStream(OutputStream out) {
            return out;
        }
    };

    /**
     * 初始化输出引擎, 通常<code>OutputEngine</code>的实现会将一个
     * <code>FilterOutputStream</code>连接到指定的输出流中.
     * 
     * @param out 输出到指定的输出流
     * @throws IOException 输入输出异常
     */
    void open(OutputStream out) throws IOException;

    /**
     * 执行一次输出引擎. 此操作在<code>OutputEngine</code>的生命期中会被执行多次,
     * 每次都将少量数据写入到初始化时指定的输出流.
     * 
     * @throws IOException 输入输出异常
     */
    void execute() throws IOException;

    /**
     * 扫尾工作. 当所有的输出都完成以后, 此方法被调用.
     * 
     * @throws IOException 输入输出异常
     */
    void close() throws IOException;

    /**
     * 创建输出流的工厂.
     */
    interface OutputStreamFactory {
        /**
         * 创建输出流, 通常返回一个<code>FilterOutputStream</code>连接到指定的输出流中.
         * 
         * @param out 输出到指定的输出流
         * @return 输出流
         * @throws IOException 输入输出异常
         */
        OutputStream getOutputStream(OutputStream out) throws IOException;
    }
}
