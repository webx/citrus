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
package com.alibaba.citrus.service.requestcontext.session;

/**
 * 包含session系统的配置。
 * 
 * @author Michael Zhou
 */
public interface SessionConfig {
    Integer MAX_INACTIVE_INTERVAL_DEFAULT = 0;
    Long FORCE_EXPIRATION_PERIOD_DEFAULT = 0L;
    String MODEL_KEY_DEFAULT = "SESSION_MODEL";
    Boolean KEEP_IN_TOUCH_DEFAULT = false;

    /**
     * Session的最长不活动时间（秒）。假如用户不活动，超过这个时限，session将被作废。值<code>0</code>表示永不过期。
     */
    int getMaxInactiveInterval();

    /**
     * Session强制作废期限（秒）。无论用户活动与否，从session创建之时算起，超过这个期限，session将被强制作废。值
     * <code>0</code>表示永不作废。
     */
    long getForceExpirationPeriod();

    /**
     * 代表session model在session中被保存的键值。Session
     * model保存着当前session的状态，它也被保存在session中。在store-mappings设置中，你可以把session
     * model分配到一个session store中。
     */
    String getModelKey();

    /**
     * 是否每次请求都touch session。如果设为<code>false</code>，只在session值有改变时touch。当将session
     * model保存在cookie store中时，这样做可以减少流量。
     */
    boolean isKeepInTouch();

    /**
     * 取得session ID的配置。
     */
    IdConfig getId();

    /**
     * 取得所有stores。
     */
    StoresConfig getStores();

    /**
     * 取得所有store mappings。
     */
    StoreMappingsConfig getStoreMappings();

    /**
     * 取得model encoders。
     */
    SessionModelEncoder[] getSessionModelEncoders();

    /**
     * 取得用来监听session行为的interceptors。
     */
    SessionInterceptor[] getSessionInterceptors();

    /**
     * 代表session ID的配置。
     */
    interface IdConfig {
        Boolean COOKIE_ENABLED_DEFAULT = true;
        Boolean URL_ENCODE_ENABLED_DEFAULT = false;

        /**
         * 是否把session ID保存在cookie中，如若不是，则只能保存的URL中。
         */
        boolean isCookieEnabled();

        /**
         * 是否支持把session ID保存在URL中。
         */
        boolean isUrlEncodeEnabled();

        /**
         * 取得session ID cookie的配置。
         */
        CookieConfig getCookie();

        /**
         * 取得session ID URL encode的配置。
         */
        UrlEncodeConfig getUrlEncode();

        /**
         * 取得session ID生成器。
         */
        SessionIDGenerator getGenerator();
    }

    /**
     * 代表cookie的配置。
     */
    interface CookieConfig {
        String COOKIE_NAME_DEFAULT = "JSESSIONID";
        String COOKIE_DOMAIN_DEFAULT = null;
        String COOKIE_PATH_DEFAULT = "/";
        Integer COOKIE_MAX_AGE_DEFAULT = 0;
        Boolean COOKIE_HTTP_ONLY_DEFAULT = true;
        Boolean COOKIE_SECURE_DEFAULT = false;

        /**
         * 取得cookie名称。
         */
        String getName();

        /**
         * 取得cookie的域名。值<code>null</code>表示根据当前请求自动设置domain。
         */
        String getDomain();

        /**
         * 取得cookie的路径。
         */
        String getPath();

        /**
         * Cookie的最长存活时间（秒）。值<code>0</code>表示临时cookie，随浏览器的关闭而消失。
         */
        int getMaxAge();

        /**
         * 在cookie上设置httpOnly标记。在IE6及更新版本中，可以缓解XSS攻击的危险。
         */
        boolean isHttpOnly();

        /**
         * 在cookie上设置secure标记。只有https安全请求才能访问该cookie。
         */
        boolean isSecure();
    }

    /**
     * 代表url encode的配置。
     */
    interface UrlEncodeConfig {
        String URL_ENCODE_NAME_DEFAULT = "JSESSIONID";

        /**
         * 取得URL encode的名称。
         */
        String getName();
    }

    /**
     * 代表stores的配置。
     */
    interface StoresConfig {
        /**
         * 取得所有的session store的名称。
         */
        String[] getStoreNames();

        /**
         * 取得指定名称的对象所存放的session store。
         */
        SessionStore getStore(String storeName);
    }

    /**
     * 代表store mappings的配置。
     */
    interface StoreMappingsConfig {
        String MATCHES_ALL_ATTRIBUTES = "*";

        /**
         * 取得指定session attribute名称的对象所存放的session store。
         */
        String getStoreNameForAttribute(String attrName);

        /**
         * 反查指定store名称所对应的所有精确匹配的attribute名称。
         * <p>
         * 假如存在非精确匹配的attributes，则返回<code>null</code>。
         * </p>
         */
        String[] getExactMatchedAttributeNames(String storeName);
    }
}
