package com.alibaba.citrus.springext;

import java.util.Set;

/**
 * 如果一个<code>Schemas</code>同时实现这个接口，那么可以从中取得所有的namespace列表。
 *
 * @author Michael Zhou
 */
public interface Namespaces {
    Set<String> getAvailableNamespaces();
}
