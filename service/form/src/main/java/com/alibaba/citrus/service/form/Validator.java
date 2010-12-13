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

import com.alibaba.citrus.service.form.configuration.FieldConfig;

/**
 * 用于验证表单的验证器。
 * 
 * @author Michael Zhou
 */
public interface Validator extends Cloneable {
    /**
     * 当GroupConfig被初始化完成以后被调用，此时可取得同组中其它的fields。
     */
    void init(FieldConfig fieldConfig) throws Exception;

    /**
     * 取得validator的ID，通过该ID可以找到指定的validator。
     */
    String getId();

    /**
     * 取得出错信息。
     */
    String getMessage(Context context);

    /**
     * 验证一个字段。
     */
    boolean validate(Context context);

    /**
     * 生成副本。
     */
    Validator clone();

    /**
     * 携带着validator验证所必须的上下文信息。
     */
    interface Context {
        /**
         * 取得当前field。
         */
        Field getField();

        /**
         * 取得指定名称的field。
         */
        Field getField(String fieldName);

        /**
         * 取得值。
         */
        Object getValue();

        /**
         * 取得指定类型的值。
         */
        <T> T getValueAsType(Class<T> type);

        /**
         * 取得错误信息。
         */
        String getMessage();

        /**
         * 设置错误信息。
         */
        void setMessage(String message);

        /**
         * 取得用来计算表达式的上下文对象。
         */
        MessageContext getMessageContext();
    }
}
