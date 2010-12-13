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
package com.alibaba.test2.module.action.param;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.citrus.turbine.dataresolver.Param;

public class MyAction {
    @Autowired
    private HttpServletRequest request;

    public void doGetInt(@Param("aaa") int i) {
        setAttribute(i);
    }

    public void doGetIntDefault(@Param(name = "aaa", defaultValue = "123") int i) {
        setAttribute(i);
    }

    public void doGetIntArray(@Param("aaa") int[] i) {
        setAttribute(i);
    }

    public void doGetIntArrayDefault(@Param(name = "aaa", defaultValue = "123") int[] i) {
        setAttribute(i);
    }

    public void doGetInteger(@Param("aaa") Integer i) {
        setAttribute(i);
    }

    public void doGetIntegerDefault(@Param(name = "aaa", defaultValue = "123") Integer i) {
        setAttribute(i);
    }

    public void doGetIntegerArray(@Param("aaa") Integer[] i) {
        setAttribute(i);
    }

    public void doGetIntegerArrayDefault(@Param(name = "aaa", defaultValues = { "123", "456" }) Integer[] i) {
        setAttribute(i);
    }

    public void doGetIntegerList(@Param("aaa") List<Integer> i) {
        setAttribute(i);
    }

    public void doGetIntegerListDefault(@Param(name = "aaa", defaultValues = { "123", "456" }) List<Integer> i) {
        setAttribute(i);
    }

    public void doGetLong(@Param("aaa") long i) {
        setAttribute(i);
    }

    public void doGetLongDefault(@Param(name = "aaa", defaultValue = "ten") long i) {
        setAttribute(i);
    }

    public void doGetBool(@Param("aaa") boolean s) {
        setAttribute(s);
    }

    public void doGetBooleanArrayDefault(@Param(name = "aaa", defaultValues = { "true", "false" }) Boolean[] s) {
        setAttribute(s);
    }

    public void doGetString(@Param("aaa") String s) {
        setAttribute(s);
    }

    public void doGetStringArrayDefault(@Param(name = "aaa", defaultValues = { "", "abc" }) String[] s) {
        setAttribute(s);
    }

    public void doGetFileItem(@Param("myFile") FileItem file) {
        setAttribute(file);
    }

    public void doGetFileItemAsString(@Param("myFile") String file) {
        setAttribute(file);
    }

    public void doGetFileItemList(@Param("myFile") List<FileItem> file) {
        setAttribute(file);
    }

    private void setAttribute(Object data) {
        request.setAttribute("actionLog", data);
    }
}
