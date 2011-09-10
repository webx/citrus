package com.alibaba.citrus.webx.util;

import static com.alibaba.citrus.util.BasicConstant.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.ObjectUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.citrus.util.ToStringBuilder;
import com.alibaba.citrus.util.regex.PathNameWildcardCompiler;

public class ExcludeFilter {
    private final String[] excludeNames;
    private final Pattern[] excludes;

    public ExcludeFilter(String excludes) {
        List<String> excludeNames = createLinkedList();
        List<Pattern> excludePatterns = createLinkedList();

        for (String exclude : split(defaultIfNull(excludes, EMPTY_STRING), ", \r\n")) {
            exclude = trimToNull(exclude);

            if (exclude != null) {
                excludeNames.add(exclude);
                excludePatterns.add(PathNameWildcardCompiler.compilePathName(exclude));
            }
        }

        if (!excludePatterns.isEmpty()) {
            this.excludeNames = excludeNames.toArray(new String[excludeNames.size()]);
            this.excludes = excludePatterns.toArray(new Pattern[excludePatterns.size()]);
        } else {
            this.excludeNames = EMPTY_STRING_ARRAY;
            this.excludes = null;
        }
    }

    public boolean isExcluded(HttpServletRequest request) {
        if (excludes != null) {
            String requestURI = request.getRequestURI();

            for (Pattern exclude : excludes) {
                if (exclude.matcher(requestURI).find()) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public String toString() {
        return new ToStringBuilder().append("Excludes").append(excludeNames).toString();
    }
}
