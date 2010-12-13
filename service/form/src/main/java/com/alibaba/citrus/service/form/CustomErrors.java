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
package com.alibaba.citrus.service.form;

import java.util.Map;

/**
 * 可设置错误信息的field。
 * 
 * @author Michael Zhou
 */
public interface CustomErrors {
    /**
     * 设置错误信息，同时置<code>isValid()</code>为<code>false</code>。
     * <p>
     * 对于<code>isValid()</code>已经是<code>false</code>的字段，该方法无效（不覆盖现有的错误信息）
     * </p>
     * <p>
     * id表示错误信息的ID，必须定义的form描述文件中。
     * </p>
     */
    void setMessage(String id);

    /**
     * 设置错误信息，同时置<code>isValid()</code>为<code>false</code>。
     * <p>
     * 对于<code>isValid()</code>已经是<code>false</code>的字段，该方法无效（不覆盖现有的错误信息）
     * </p>
     * <p>
     * id表示错误信息的ID，必须定义的form描述文件中。params表示生成错误信息的参数表。
     * </p>
     */
    void setMessage(String id, Map<String, ?> params);
}
