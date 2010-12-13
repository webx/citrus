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
package com.alibaba.citrus.service.requestcontext.session.valueencoder;

import java.text.SimpleDateFormat;

import com.alibaba.citrus.test.runner.Prototyped.Prototypes;
import com.alibaba.citrus.test.runner.Prototyped.TestData;

public class SimpleValueEncoderTests extends AbstractSingleValueEncoderTests {
    @Prototypes
    public static TestData<SimpleValueEncoderTests> data() throws Exception {
        TestData<SimpleValueEncoderTests> data = TestData.getInstance(SimpleValueEncoderTests.class);
        SimpleValueEncoderTests prototype;

        prototype = data.newPrototype();
        prototype.beanName = "simple_default";
        prototype.attrName = "simple-default";
        prototype.cookieName = "simple-default-cookie";
        prototype.value1 = "hello, 世界";
        prototype.value1Encoded = "hello%2C+%E4%B8%96%E7%95%8C"; // utf8 encoded
        prototype.value2 = "hello, 中国";
        prototype.value2Encoded = "hello%2C+%E4%B8%AD%E5%9B%BD"; // utf8 encoded
        prototype.value3 = "hello, world";
        prototype.value3Encoded = "hello%2C+world";

        prototype = data.newPrototype();
        prototype.beanName = "simple_specified";
        prototype.attrName = "simple-specified";
        prototype.cookieName = "simple-specified-cookie";
        prototype.value1 = "hello, 世界";
        prototype.value1Encoded = "hello%2C+%CA%C0%BD%E7"; // gbk encoded
        prototype.value2 = "hello, 中国";
        prototype.value2Encoded = "hello%2C+%D6%D0%B9%FA"; // gbk encoded
        prototype.value3 = "hello, world";
        prototype.value3Encoded = "hello%2C+world";

        prototype = data.newPrototype();
        prototype.beanName = "simple_encrypted";
        prototype.attrName = "simple-encrypted";
        prototype.cookieName = "simple-encrypted-cookie";
        prototype.value1 = "hello, 世界";
        prototype.value1Encoded = "371M9HBO8gt7KxgvMoO5PQ%3D%3D"; // aes encrypted and base64, no compression
        prototype.value2 = "hello, 中国";
        prototype.value2Encoded = "Z1bysRNn7lOYEkWMUsOOyw%3D%3D"; // aes encrypted and base64, no compression
        prototype.value3 = "hello, world";
        prototype.value3Encoded = "M81hCx4qUfD38DtyQOUVaA%3D%3D"; // aes encrypted and base64, no compression

        prototype = data.newPrototype();
        prototype.beanName = "simple_int";
        prototype.attrName = "simple-int";
        prototype.cookieName = "simple-int-cookie";
        prototype.value1 = 111;
        prototype.value1Encoded = "111";
        prototype.value2 = 222;
        prototype.value2Encoded = "222";
        prototype.value3 = 333;
        prototype.value3Encoded = "333";

        prototype = data.newPrototype();
        prototype.beanName = "simple_date";
        prototype.attrName = "simple-date";
        prototype.cookieName = "simple-date-cookie";
        prototype.value1 = new SimpleDateFormat("yyyy-MM-dd").parse("1989-06-04");
        prototype.value1Encoded = "1989-06-04";
        prototype.value2 = new SimpleDateFormat("yyyy-MM-dd").parse("2008-05-12");
        prototype.value2Encoded = "2008-05-12";
        prototype.value3 = new SimpleDateFormat("yyyy-MM-dd").parse("2010-08-07");
        prototype.value3Encoded = "2010-08-07";

        return data;
    }
}
