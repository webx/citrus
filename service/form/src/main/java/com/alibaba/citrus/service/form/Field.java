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

import javax.servlet.http.HttpServletRequest;

import com.alibaba.citrus.service.form.configuration.FieldConfig;
import com.alibaba.citrus.service.requestcontext.util.ValueList;

/**
 * 代表用户所提交表单中的一个field。
 * <p>
 * 注意：field对象不是线程安全的，不能被多线程共享。
 * </p>
 * 
 * @author Michael Zhou
 */
public interface Field extends ValueList, CustomErrors {
    /**
     * 取得field的配置信息。
     */
    FieldConfig getFieldConfig();

    /**
     * 取得包含此field的group。
     */
    Group getGroup();

    /**
     * 判定field是否通过验证。
     */
    boolean isValid();

    /**
     * 取得在form中唯一代表该field的key。
     * <p>
     * 由固定前缀<code>"_fm"</code>，加上group名的缩写，加上group instance
     * fieldKey，再加上field名的缩写构成。例如：<code>_fm.m._0.n</code>。
     * </p>
     */
    String getKey();

    /**
     * 取得在form中唯一代表该field的key，当用户提交的表单中未包含此field的信息时，取这个key的值作为该field的值。
     * <p>
     * 这对于checkbox之类的HTML控件特别有用。
     * </p>
     * <p>
     * Key的格式为：<code>_fm.groupKey.instanceKey.fieldKey.absent</code>。
     * </p>
     */
    String getAbsentKey();

    /**
     * 取得在form中和当前field绑定的附件的key。
     * <p>
     * Key的格式为：<code>_fm.groupKey.instanceKey.fieldKey.attach</code>。
     * </p>
     */
    String getAttachmentKey();

    /**
     * 取得出错信息。
     */
    String getMessage();

    /**
     * 初始化field值，但不验证表单字段。其中，<code>request</code>可以是<code>null</code>。
     */
    void init(HttpServletRequest request);

    /**
     * 取得field name，相当于<code>getFieldConfig().getName()</code>。
     */
    String getName();

    /**
     * 取得参数值，如果指定名称的参数不存在，则返回<code>""</code>。
     */
    String getStringValue();

    /**
     * 取得用来显示field的名称，相当于<code>getFieldConfig().getDisplayName()</code>。
     */
    String getDisplayName();

    /**
     * 取得默认值，相当于<code>getFieldConfig().getDefaultValue()</code>。
     */
    String getDefaultValue();

    /**
     * 取得默认值，相当于<code>getFieldConfig().getDefaultValues()</code>。
     */
    String[] getDefaultValues();

    /**
     * 添加参数名/参数值。
     */
    void addValue(Object value);

    /**
     * 设置附件。
     */
    Object getAttachment();

    /**
     * 设置编码后的附件。
     */
    String getAttachmentEncoded();

    /**
     * 是否包含附件？
     */
    boolean hasAttachment();

    /**
     * 设置附件。
     * <p>
     * 注意，当attachment已经存在时，该方法调用无效。欲强制设入，请先调用<code>clearAttachment()</code>。
     * </p>
     */
    void setAttachment(Object attachment);

    /**
     * 清除附件。
     */
    void clearAttachment();
}
