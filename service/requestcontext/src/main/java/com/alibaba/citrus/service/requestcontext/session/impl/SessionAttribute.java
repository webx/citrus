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
package com.alibaba.citrus.service.requestcontext.session.impl;

import com.alibaba.citrus.service.requestcontext.session.SessionStore;
import com.alibaba.citrus.service.requestcontext.session.SessionStore.StoreContext;
import com.alibaba.citrus.util.internal.ToStringBuilder;
import com.alibaba.citrus.util.internal.ToStringBuilder.MapBuilder;

/**
 * 代表session中的一个属性。
 * 
 * @author Michael Zhou
 */
public class SessionAttribute {
    private String storeName;
    private SessionStore store;
    private StoreContext storeContext;
    private SessionImpl session;
    private String name;
    private Object value;
    private boolean loaded;
    private boolean modified;

    /**
     * 创建一个attribute。
     * 
     * @param name attribute的名称
     */
    public SessionAttribute(String name, SessionImpl session, String storeName, StoreContext storeContext) {
        this.name = name;
        this.session = session;
        this.storeName = storeName;
        this.store = session.getSessionRequestContext().getSessionConfig().getStores().getStore(storeName);
        this.storeContext = storeContext;
    }

    /**
     * 取得attribute的名字。
     * 
     * @return attribute的名字
     */
    public String getName() {
        return name;
    }

    /**
     * 取得attribute的值。
     * 
     * @return attribute的值
     */
    public Object getValue() {
        if (!loaded && !modified) {
            value = store.loadAttribute(getName(), session.getId(), storeContext);
            loaded = true;
        }

        return value;
    }

    /**
     * 设置attribute的值。
     * <p>
     * 当值为<code>null</code>时，表示该属性将被删除。
     * </p>
     * 
     * @param value attribute的值
     */
    public void setValue(Object value) {
        this.value = value;
        this.modified = true;
    }

    /**
     * 仅仅设置attribute值，但不改变其modified标志。
     * <p>
     * 用于设置session model。
     * </p>
     */
    void updateValue(Object value) {
        this.value = value;
    }

    /**
     * 值是否被改变。
     * 
     * @return 如果被改变，则返回<code>true</code>
     */
    public boolean isModified() {
        return modified;
    }

    /**
     * 取得store名称。
     * 
     * @return store的名称
     */
    public String getStoreName() {
        return storeName;
    }

    /**
     * 取得store。
     * 
     * @return <code>SessionStore</code>实例
     */
    public SessionStore getStore() {
        return store;
    }

    @Override
    public String toString() {
        MapBuilder mb = new MapBuilder();

        mb.append("name", name);
        mb.append("value", value);
        mb.append("storeName", storeName);
        mb.append("loaded", loaded);
        mb.append("modified", modified);

        return new ToStringBuilder().append("SessionAttribute").append(mb).toString();
    }

}
