package com.alibaba.citrus.webx.servlet;

import com.alibaba.citrus.webx.util.RequestURIFilter;

public interface PassThruSupportable {
    void setPassthruFilter(RequestURIFilter passthru);
}
