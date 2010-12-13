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

/**
 * 代表form service的常量。
 * 
 * @author Michael Zhou
 */
public final class FormConstant {
    /**
     * 在request attribute中，或用户提交的form中，作为form key的前缀。
     */
    public final static String FORM_KEY_PREFIX = "_fm";

    /**
     * 分隔field key各部分的分隔符。
     */
    public final static char FIELD_KEY_SEPARATOR = '.';

    /**
     * 默认的group instance key。
     */
    public final static String DEFAULT_GROUP_INSTANCE_KEY = "_0";

    /**
     * 当用户提交的表单中未包含指定field信息时，取该值作为field的值。
     */
    public final static String FORM_FIELD_ABSENT_KEY = ".absent";

    /**
     * 代表某个field对象的附件的key的后缀。
     */
    public final static String FORM_FIELD_ATTACHMENT_KEY = ".attach";

    /**
     * 代表message code的默认前缀。
     */
    public final static String FORM_MESSAGE_CODE_PREFIX = "form.";
}
