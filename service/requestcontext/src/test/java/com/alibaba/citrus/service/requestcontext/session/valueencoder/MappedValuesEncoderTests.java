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

import static com.alibaba.citrus.util.CollectionUtil.*;

import java.text.SimpleDateFormat;
import java.util.Map;

import com.alibaba.citrus.test.runner.Prototyped.Prototypes;
import com.alibaba.citrus.test.runner.Prototyped.TestData;

public class MappedValuesEncoderTests extends AbstractSingleValueEncoderTests {
    @Prototypes
    public static TestData<MappedValuesEncoderTests> data() throws Exception {
        TestData<MappedValuesEncoderTests> data = TestData.getInstance(MappedValuesEncoderTests.class);
        MappedValuesEncoderTests prototype;

        prototype = data.newPrototype();
        prototype.beanName = "mapped_default";
        prototype.attrName = "mapped-default";
        prototype.cookieName = "mapped-default-cookie";
        prototype.value1 = newMap("hello", "你好", "world", "世界");
        prototype.value1Encoded = "\"hello:%E4%BD%A0%E5%A5%BD&world:%E4%B8%96%E7%95%8C\""; // utf8 encoded
        prototype.value2 = newMap("hello", "你好", "world", "中国");
        prototype.value2Encoded = "\"hello:%E4%BD%A0%E5%A5%BD&world:%E4%B8%AD%E5%9B%BD\""; // utf8 encoded
        prototype.value3 = newMap("hello", "world");
        prototype.value3Encoded = "\"hello:world\"";

        prototype = data.newPrototype();
        prototype.beanName = "mapped_specified";
        prototype.attrName = "mapped-specified";
        prototype.cookieName = "mapped-specified-cookie";
        prototype.value1 = newMap("hello", "你好", "world", "世界");
        prototype.value1Encoded = "\"hello:%C4%E3%BA%C3&world:%CA%C0%BD%E7\""; // gbk encoded
        prototype.value2 = newMap("hello", "你好", "world", "中国");
        prototype.value2Encoded = "\"hello:%C4%E3%BA%C3&world:%D6%D0%B9%FA\""; // gbk encoded
        prototype.value3 = newMap("hello", "world");
        prototype.value3Encoded = "\"hello:world\"";

        prototype = data.newPrototype();
        prototype.beanName = "mapped_encrypted";
        prototype.attrName = "mapped-encrypted";
        prototype.cookieName = "mapped-encrypted-cookie";
        prototype.value1 = newMap("hello", "你好", "world", "世界");
        prototype.value1Encoded = "iAgfpRQ1Tos2bm%2B7FoZNjf1HlHmXefwxOg3Nzcg"
                + "QVVe0GT6V0xchmppCBj05e6b%2F490%2Fd65pNOA42zHQFCrxiQ%3D%3D"; // aes encrypted and base64, compressed
        prototype.value2 = newMap("hello", "你好", "world", "中国");
        prototype.value2Encoded = "iAgfpRQ1Tos2bm%2B7FoZNjf1HlHmXefwxOg3Nzcg"
                + "QVVeSAPjeZF%2BIKFJXcNDIYcQfN3Ii4GGpJMWRzZwn6hY%2B1A%3D%3D"; // aes encrypted and base64, compressed
        prototype.value3 = newMap("hello", "world");
        prototype.value3Encoded = "O9RZVE9yM6vCHt0WEvkl94t%2BUqtklT5fF3QoFNrH77o%3D"; // aes encrypted and base64, compressed

        prototype = data.newPrototype();
        prototype.beanName = "mapped_int";
        prototype.attrName = "mapped-int";
        prototype.cookieName = "mapped-int-cookie";
        prototype.value1 = newMap("hello", 111, "world", 222);
        prototype.value1Encoded = "\"hello:111&world:222\"";
        prototype.value2 = newMap("hello", 111, "world", 333);
        prototype.value2Encoded = "\"hello:111&world:333\"";
        prototype.value3 = newMap("hello", 444);
        prototype.value3Encoded = "\"hello:444\"";

        prototype = data.newPrototype();
        prototype.beanName = "mapped_date";
        prototype.attrName = "mapped-date";
        prototype.cookieName = "mapped-date-cookie";
        prototype.value1 = newMap("hello", new SimpleDateFormat("yyyy-MM-dd").parse("1989-06-04"));
        prototype.value1Encoded = "\"hello:1989-06-04\"";
        prototype.value2 = newMap("hello", new SimpleDateFormat("yyyy-MM-dd").parse("2008-05-12"));
        prototype.value2Encoded = "\"hello:2008-05-12\"";
        prototype.value3 = newMap("hello", new SimpleDateFormat("yyyy-MM-dd").parse("2010-08-07"));
        prototype.value3Encoded = "\"hello:2010-08-07\"";

        return data;
    }

    private static Map<Object, Object> newMap(Object... keyValues) {
        Map<Object, Object> map = createLinkedHashMap();

        for (int i = 0; i < keyValues.length; i += 2) {
            map.put(keyValues[i], keyValues[i + 1]);
        }

        return map;
    }
}
