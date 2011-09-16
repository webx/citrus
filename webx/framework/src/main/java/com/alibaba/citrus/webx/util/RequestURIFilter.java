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

/**
 * 用来匹配和过滤<code>request.getRequestURI()</code>。
 * 
 * @author Michael Zhou
 */
public class RequestURIFilter {
    private final String[] uris;
    private final Pattern[] patterns;

    public RequestURIFilter(String uris) {
        List<String> names = createLinkedList();
        List<Pattern> patterns = createLinkedList();

        for (String uri : split(defaultIfNull(uris, EMPTY_STRING), ", \r\n")) {
            uri = trimToNull(uri);

            if (uri != null) {
                names.add(uri);
                patterns.add(PathNameWildcardCompiler.compilePathName(uri));
            }
        }

        if (!patterns.isEmpty()) {
            this.uris = names.toArray(new String[names.size()]);
            this.patterns = patterns.toArray(new Pattern[patterns.size()]);
        } else {
            this.uris = EMPTY_STRING_ARRAY;
            this.patterns = null;
        }
    }

    public boolean matches(HttpServletRequest request) {
        if (patterns != null) {
            String requestURI = request.getRequestURI();

            for (Pattern pattern : patterns) {
                if (pattern.matcher(requestURI).find()) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public String toString() {
        return new ToStringBuilder().append("FilterOf").append(uris).toString();
    }
}
