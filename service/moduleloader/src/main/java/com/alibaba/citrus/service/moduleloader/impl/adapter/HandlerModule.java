/*
 * Copyright 2012 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.citrus.service.moduleloader.impl.adapter;

import java.util.Map;

import com.alibaba.citrus.service.moduleloader.Module;

/**
 * 类HandleModule.java的实现描述：TODO 类实现描述
 * 
 * @author qianchao 2012-6-29 上午11:31:30
 */
public interface HandlerModule extends Module {

    Map<String, MethodInvoker> getHandlers();
}
