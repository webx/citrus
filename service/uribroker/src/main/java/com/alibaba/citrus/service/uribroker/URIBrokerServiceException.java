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
package com.alibaba.citrus.service.uribroker;

/**
 * 代表URIBrokerService的异常。
 * 
 * @author Michael Zhou
 * @author dux.fangl
 * @version $Id: URIBrokerServiceException.java 1291 2005-03-04 03:23:30Z baobao
 *          $
 */
public class URIBrokerServiceException extends RuntimeException {
    private static final long serialVersionUID = 3257566204763058484L;

    /**
     * 创建一个异常。
     */
    public URIBrokerServiceException() {
        super();
    }

    /**
     * 创建一个异常。
     */
    public URIBrokerServiceException(String message) {
        super(message);
    }

    /**
     * 创建一个异常。
     */
    public URIBrokerServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 创建一个异常。
     */
    public URIBrokerServiceException(Throwable cause) {
        super(cause);
    }
}
