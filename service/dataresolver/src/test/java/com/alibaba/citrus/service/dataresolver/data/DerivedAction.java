package com.alibaba.citrus.service.dataresolver.data;

import javax.servlet.http.HttpServletRequest;

public class DerivedAction extends Action {
    @Override
    public void execute(HttpServletRequest request, String name) {
        super.execute(request, name);
    }

    @Override
    public void execute(HttpServletRequest request, String name, String value) {
        super.execute(request, name, value);
    }
}
