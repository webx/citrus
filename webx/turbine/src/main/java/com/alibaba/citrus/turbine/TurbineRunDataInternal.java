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
 */

package com.alibaba.citrus.turbine;

/**
 * 仅供框架内部使用。
 * 
 * @author Michael Zhou
 */
public interface TurbineRunDataInternal extends TurbineRunData, Navigator, ControlParameters {
    void setTarget(String target);

    void setAction(String action);

    void setActionEvent(String actionEvent);

    Context getContext(String componentName);

    Context getContext();

    /**
     * 取得当前的context。
     */
    Context getCurrentContext();

    /**
     * 修改当前的context。
     */
    void pushContext(Context context);

    /**
     * 修改当前的context。
     */
    void pushContext(Context context, String template);

    /**
     * 弹出当前的context，恢复上一个context。
     * 
     * @throws IllegalStateException 如果pop和push不配对，则抛错
     */
    Context popContext() throws IllegalStateException;

    /**
     * 取得明确指定的layout模板。
     */
    String getLayoutTemplateOverride();
}
