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
package com.alibaba.citrus.service.requestcontext.rundata;

/**
 * 代表一个访问WEB应用程序的用户。
 * 
 * @author Michael Zhou
 */
public interface User {
    /**
     * 取得用来区别不同用户的ID。
     * 
     * @return 用户ID，不同用户的ID不能相同
     */
    String getId();

    /**
     * 判断用户是否已经登录。
     * 
     * @return 如果用户已经登录，则返回<code>true</code>
     */
    boolean hasLoggedIn();

    /**
     * 取得和用户绑定的对象。当用户被保存时（例如保存在HTTP session中），所有的attributes也将被保存。
     * 当用户对象被恢复时，所有的attributes也将被恢复。
     * 
     * @param key 对象的key
     * @return 和key相对应的对象
     */
    Object getAttribute(String key);

    /**
     * 将指定对象绑定到用户对象中。当用户被保存时（例如保存在HTTP session中），所有的attributes也将被保存。
     * 当用户对象被恢复时，所有的attributes也将被恢复。
     * 
     * @param key 对象的key
     * @param object 和key相对应的对象
     */
    void setAttribute(String key, Object object);
}
