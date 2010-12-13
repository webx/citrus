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
package com.meterware.httpunit.cookies;

import static com.alibaba.citrus.test.TestUtil.*;

public class PatchedCookieJar extends CookieJar {
    public PatchedCookieJar() {
    }

    public PatchedCookieJar(CookieSource source) {
        try {
            getAccessibleField(getClass(), "_press").set(this, new CookiePress(source.getURL()));
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        findCookies(source.getHeaderFields("Set-Cookie"), new RFC2109CookieRecipe() {
            @Override
            protected boolean isCookieReservedWord(String token) {
                return token.equalsIgnoreCase("httpOnly") || super.isCookieReservedWord(token);
            }
        });

        findCookies(source.getHeaderFields("Set-Cookie2"), new RFC2965CookieRecipe());
    }

    private void findCookies(String cookieHeader[], CookieRecipe recipe) {
        for (String element : cookieHeader) {
            recipe.findCookies(element);
        }
    }
}
