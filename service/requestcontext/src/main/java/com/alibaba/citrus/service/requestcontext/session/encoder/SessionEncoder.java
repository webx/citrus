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
package com.alibaba.citrus.service.requestcontext.session.encoder;

import java.util.Map;

import com.alibaba.citrus.service.requestcontext.session.SessionStore.StoreContext;

/**
 * 将attributes map转换成字符串值或反之。
 * 
 * @author Michael Zhou
 */
public interface SessionEncoder {
    /**
     * 将对象编码成字符串。
     * 
     * @throws SessionEncoderException 如果编码失败
     */
    String encode(Map<String, Object> attrs, StoreContext storeContext) throws SessionEncoderException;

    /**
     * 将字符串解码成对象。
     * <p>
     * 如果有多个encoders存在，当前encoder解码失败抛出异常以后，系统会去尝试用其它encoders解码
     * </p>
     * 
     * @throws SessionEncoderException 如果解码失败
     */
    Map<String, Object> decode(String encodedValue, StoreContext storeContext) throws SessionEncoderException;
}
