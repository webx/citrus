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

package com.alibaba.citrus.service.form.impl.validation;

import static com.alibaba.citrus.util.StringUtil.*;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.alibaba.citrus.service.form.support.AbstractOptionalValidator;
import com.alibaba.citrus.service.form.support.AbstractValidatorDefinitionParser;

/**
 * 检查输入值的日期格式是否符合指定的格式，是否在设定的区域内。
 * 如：指定日期格式DateFromat为"yyyy-MM-dd"，如果输入的日期值格式不符合，则不合法；
 * 若同时指定了最小值为"2000-12-31"，最大值为"2005-12-31"，而输入的日期不在此区间 内，则不合法；也可以只指定其中的某一个。
 * 
 * @author Michael Zhou
 */
public class DateValidator extends AbstractOptionalValidator {
    private final static String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    private String dateFormatStr;
    private Date minDate;
    private String minDateStr;
    private Date maxDate;
    private String maxDateStr;

    /**
     * 取日期格式，参见<code>SimpleDateFormat</code>的注释。
     */
    public String getFormat() {
        return dateFormatStr;
    }

    /**
     * 设置日期格式，参见<code>SimpleDateFormat</code>的注释。
     */
    public void setFormat(String formatStr) {
        this.dateFormatStr = trimToNull(formatStr);
    }

    /**
     * 取最小日期，字符串形式，格式符合setDateFormat()设置。
     */
    public String getMinDate() {
        return minDateStr;
    }

    /**
     * 设置最小日期，字符串形式，格式符合setDateFormat()设置。
     */
    public void setMinDate(String minDate) {
        this.minDateStr = trimToNull(minDate);
    }

    /**
     * 取最小日期，字符串形式，格式符合setDateFormat()设置。
     */
    public String getMaxDate() {
        return maxDateStr;
    }

    /**
     * 设置最大日期，字符串形式，格式符合setDateFormat()设置。
     */
    public void setMaxDate(String maxDate) {
        this.maxDateStr = trimToNull(maxDate);
    }

    private DateFormat getDateFormat() {
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormatStr);
        sdf.setLenient(false);
        return sdf;
    }

    @Override
    protected void init() throws Exception {
        super.init();

        if (dateFormatStr == null) {
            dateFormatStr = DEFAULT_DATE_FORMAT;
        }

        DateFormat format = getDateFormat();

        if (minDateStr != null) {
            minDate = format.parse(minDateStr);
            minDateStr = format.format(minDate);
        }

        if (maxDateStr != null) {
            maxDate = format.parse(maxDateStr);
            maxDateStr = format.format(maxDate);
        }
    }

    /**
     * 校验输入日期值是否符合指定的格式，是否在设定的区域内。
     * 如：指定日期格式DateFromat为"yyyy-MM-dd"，如果输入的日期值格式不符合，则不合法；
     * 若同时指定了最小值为"2000-12-31"，最大值为"2005-12-31"，而输入的日期不在此区间 内，则不合法；也可以只指定其中的某一个
     */
    @Override
    protected boolean validate(Context context, String value) {
        DateFormat format = getDateFormat();
        Date inputDate;

        try {
            inputDate = format.parse(value);
        } catch (ParseException e) {
            return false;
        }

        if (minDate != null && inputDate.before(minDate)) {
            return false;
        }

        if (maxDate != null && inputDate.after(maxDate)) {
            return false;
        }

        return true;
    }

    public static class DefinitionParser extends AbstractValidatorDefinitionParser<DateValidator> {
    }
}
