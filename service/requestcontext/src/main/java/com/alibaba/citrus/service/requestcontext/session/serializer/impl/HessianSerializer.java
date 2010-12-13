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
package com.alibaba.citrus.service.requestcontext.session.serializer.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.alibaba.citrus.hessian.io.Hessian2Input;
import com.alibaba.citrus.hessian.io.Hessian2Output;
import com.alibaba.citrus.service.requestcontext.session.serializer.Serializer;
import com.alibaba.citrus.springext.support.parser.AbstractSingleBeanDefinitionParser;

/**
 * Hessian–Ú¡–ªØ°£
 * 
 * @author Michael Zhou
 */
public class HessianSerializer implements Serializer {
    public void serialize(Object objectToEncode, OutputStream os) throws Exception {
        Hessian2Output ho = null;

        try {
            ho = new Hessian2Output(os);
            ho.writeObject(objectToEncode);
        } finally {
            if (ho != null) {
                try {
                    ho.close();
                } catch (IOException e) {
                }
            }
        }
    }

    public Object deserialize(InputStream is) throws Exception {
        Hessian2Input hi = null;

        try {
            hi = new Hessian2Input(is);
            return hi.readObject();
        } finally {
            if (hi != null) {
                try {
                    hi.close();
                } catch (IOException e) {
                }
            }
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    public static class DefinitionParser extends AbstractSingleBeanDefinitionParser<HessianSerializer> {
    }
}
