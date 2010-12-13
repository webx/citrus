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
package com.alibaba.citrus.service.uribroker.impl;

import static com.alibaba.citrus.springext.util.SpringExtUtil.*;
import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;
import static java.util.Collections.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.citrus.service.AbstractService;
import com.alibaba.citrus.service.uribroker.URIBrokerService;
import com.alibaba.citrus.service.uribroker.uri.URIBroker;

/**
 * URI Broker服务的实现。
 * 
 * @author Michael Zhou
 * @author dux.fangl
 */
public class URIBrokerServiceImpl extends AbstractService<URIBrokerService> implements URIBrokerService {
    private final HttpServletRequest request;
    private Boolean requestAware;
    private String defaultCharset;
    private URIBrokerInfo[] brokerInfos; // 临时brokers信息，仅用于初始化
    private Map<String, URIBroker> brokers;
    private List<String> exposedNames;
    private List<String> names;

    /**
     * 创建服务，取得request proxy。
     */
    public URIBrokerServiceImpl(HttpServletRequest request) {
        this.request = assertProxy(request); // request可以为空
    }

    /**
     * 是否使用request的参量。
     */
    public boolean isRequestAware() {
        return requestAware == null ? true : requestAware;
    }

    /**
     * 设置是否使用request的参量。
     */
    public void setRequestAware(boolean requestAware) {
        this.requestAware = requestAware;
    }

    /**
     * 取得默认的charset。
     * <p>
     * 如果不特别指定charset，uri broker将取该值作为charset。
     * </p>
     */
    public String getDefaultCharset() {
        return defaultCharset;
    }

    /**
     * 设置默认的charset。
     */
    public void setDefaultCharset(String defaultCharset) {
        this.defaultCharset = defaultCharset;
    }

    /**
     * 设置一批uri broker集合。
     */
    public void setBrokers(URIBrokerInfo[] brokerInfos) {
        this.brokerInfos = brokerInfos;
    }

    /**
     * 取得所有URI broker名称。
     */
    public List<String> getNames() {
        return unmodifiableList(names);
    }

    /**
     * 取得所有被导出的URI broker名称。
     */
    public List<String> getExposedNames() {
        return unmodifiableList(exposedNames);
    }

    /**
     * 取得指定名称的URI broker。
     */
    public URIBroker getURIBroker(String name) {
        URIBroker broker = brokers.get(name);
        return broker == null ? null : broker.fork();
    }

    /**
     * 取得指定名称的URI broker，但不fork。
     */
    URIBroker getURIBrokerInternal(String name) {
        return brokers.get(name);
    }

    /**
     * 初始化时所有的brokers。
     */
    @Override
    protected void init() {
        assertNotNull(brokerInfos, "brokers");

        brokers = createLinkedHashMap();
        names = createLinkedList();
        exposedNames = createLinkedList();

        // 建立name和broker的映射
        Map<String, URIBrokerInfo> brokerInfoMap = createLinkedHashMap();

        for (URIBrokerInfo brokerInfo : brokerInfos) {
            URIBroker broker = assertNotNull(brokerInfo == null ? null : brokerInfo.broker, "broker");

            brokerInfo.name = assertNotNull(trimToNull(brokerInfo.name), "broker ID");
            brokerInfo.parentName = trimToNull(brokerInfo.parentName);

            assertTrue(!brokerInfoMap.containsKey(brokerInfo.name), "duplicated broker ID: %s", brokerInfo.name);

            brokerInfoMap.put(brokerInfo.name, brokerInfo);
            brokers.put(brokerInfo.name, broker);
            names.add(brokerInfo.name);

            if (brokerInfo.exposed) {
                exposedNames.add(brokerInfo.name);
            }

            // 除非设置了requestAware，否则保持broker中的默认值
            if (requestAware != null) {
                broker.setRequestAware(requestAware);
            }

            // 除非设置了defaultCharset，否则保持默认的broker charset。
            if (defaultCharset != null && broker.getCharset() == null) {
                broker.setCharset(defaultCharset);
            }

            broker.setRequest(request);
        }

        brokerInfos = null;

        // 设置parent brokers，确保parent broker在类层次上也是父类或同类，同时确保没有递归派生
        for (Map.Entry<String, URIBrokerInfo> entry : brokerInfoMap.entrySet()) {
            String name = entry.getKey();
            URIBrokerInfo brokerInfo = entry.getValue();
            String parentName = brokerInfo.parentName;

            // 检查继承链，确保没有递归
            checkCyclic(brokerInfoMap, name, parentName);

            if (parentName != null) {
                URIBroker parentBroker = assertNotNull(brokers.get(parentName),
                        "parent \"%s\" not found for broker \"%s\"", parentName, brokerInfo.name);
                URIBroker thisBroker = brokerInfo.broker;

                thisBroker.setParent(parentBroker);
            }
        }

        // 递归复制parent中的信息
        for (URIBroker broker : brokers.values()) {
            broker.init();
        }
    }

    private String checkCyclic(Map<String, URIBrokerInfo> brokerInfoMap, String name, String parentName) {
        Set<String> inheritanceChain = createLinkedHashSet(name);

        for (; parentName != null; parentName = brokerInfoMap.containsKey(parentName) ? brokerInfoMap.get(parentName).parentName
                : null) {
            if (inheritanceChain.contains(parentName)) {
                StringBuilder buf = new StringBuilder();

                buf.append("Cyclic detected: ");

                for (String item : inheritanceChain) {
                    buf.append(item).append("->");
                }

                buf.append(parentName);

                throw new IllegalArgumentException(buf.toString());
            }

            inheritanceChain.add(parentName);
        }
        return parentName;
    }

    /**
     * 列出所有的URI brokers。
     */
    public String dump() {
        StringWriter buf = new StringWriter();
        dump(buf);
        return buf.toString();
    }

    /**
     * 列出所有的URI brokers。
     */
    public void dump(Writer writer) {
        PrintWriter out = null;

        if (writer instanceof PrintWriter) {
            out = (PrintWriter) writer;
        } else {
            out = new PrintWriter(writer);
        }

        // 取得最长的key的长度
        int classWidth = 0;
        int keyWidth = 0;

        for (Map.Entry<String, URIBroker> entry : brokers.entrySet()) {
            String name = entry.getKey();
            URIBroker broker = entry.getValue();
            String className = broker.getClass().getSimpleName();

            if (className.length() > classWidth) {
                classWidth = className.length();
            }

            if (name.length() > keyWidth) {
                keyWidth = name.length();
            }
        }

        for (Map.Entry<String, URIBroker> entry : brokers.entrySet()) {
            String name = entry.getKey();
            URIBroker broker = entry.getValue();

            broker = broker.fork();

            StringBuilder format = new StringBuilder();

            if (exposedNames.contains(name)) {
                format.append("* ");
            } else {
                format.append("  ");
            }

            format.append("%-").append(classWidth + 2).append("s %-").append(keyWidth).append("s= %s%n");

            out.printf(format.toString(), "(" + broker.getClass().getSimpleName() + ")", name, broker);
        }

        out.flush();
    }

    /**
     * 存放uri broker的配置信息。
     */
    public static class URIBrokerInfo {
        public String name;
        public String parentName;
        public boolean exposed;
        public URIBroker broker;

        public URIBrokerInfo(String name, String parentName, boolean exposed, URIBroker broker) {
            this.name = trimToNull(name);
            this.parentName = trimToNull(parentName);
            this.exposed = exposed;
            this.broker = broker;
        }
    }
}
