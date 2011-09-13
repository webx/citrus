package com.alibaba.citrus.service.pull.support;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.alibaba.citrus.service.pull.AbstractPullServiceConfigTests;
import com.alibaba.citrus.service.pull.PullService;

public class ContextExposerToolSetTests extends AbstractPullServiceConfigTests {
    @Test
    public void withoutBeanName() throws Exception {
        pullService = (PullService) factory.getBean("pullService");

        @SuppressWarnings("unchecked")
        Map<String, Integer> mymap = (Map<String, Integer>) pullService.getContext().pull("mymap");

        assertArrayEquals(new String[] { "a", "b" }, mymap.keySet().toArray(new String[mymap.size()]));
        assertEquals(new Integer(111), mymap.get("a"));
        assertEquals(new Integer(222), mymap.get("b"));
    }

    @Test
    public void withBeanName() throws Exception {
        pullService = (PullService) factory.getBean("pullService");

        @SuppressWarnings("unchecked")
        List<Integer> mylist = (List<Integer>) pullService.getContext().pull("mylist2");

        assertArrayEquals(new Integer[] { 333, 444, 555 }, mylist.toArray(new Integer[mylist.size()]));
    }
}
