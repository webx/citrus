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
package com.alibaba.citrus.generictype.introspect;

import static com.alibaba.citrus.test.TestUtil.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import com.alibaba.citrus.generictype.introspect.PropertyPath.Visitor;

/**
 * ≤‚ ‘<code>PropertyPath</code>°£
 * 
 * @author Michael Zhou
 */
public class PropertyPathTests {
    @Test(expected = NullPointerException.class)
    public void parse_null() {
        PropertyPath.parse(null);
    }

    @Test
    public void parse_propertyNames() {
        assertParser("");

        assertParser("abc", "abc");
        assertParser("a123", "a123");
        assertParser(".abc", "abc");
        assertParser("  abc  ", "abc");
        assertParser(" . abc  ", "abc");

        assertParser("abc.def", "abc", "def");
        assertParser(".abc.def", "abc", "def");
        assertParser("  abc .def ", "abc", "def");
        assertParser(" . abc .  def ", "abc", "def");

        assertParser("abc.def.ghi", "abc", "def", "ghi");
        assertParser("  abc . def. ghi ", "abc", "def", "ghi");

        assertParserFail("123", "");
        assertParserFail("a.123", "a.");
        assertParserFail("a b", "a ");
        assertParserFail("a.", "a.");
    }

    @Test
    public void parse_index() {
        assertParser("[123]", "[123]");
        assertParser("[123][345]", "[123]", "[345]");
        assertParser(" [ 123 ] ", "[123]");
        assertParser(" [ 123 ] [ 345 ]", "[123]", "[345]");

        assertParser("abc[123]", "abc", "[123]");
        assertParser("abc [123]", "abc", "[123]");
        assertParser(" abc [ 123 ] ", "abc", "[123]");

        assertParser("abc[123][456]", "abc", "[123]", "[456]");
        assertParser("  abc  [ 123 ] [ 456 ] ", "abc", "[123]", "[456]");

        assertParserFail("[123a]", "[123");
        assertParserFail("[abc]", "[");
        assertParserFail("[123", "[123");
        assertParserFail("[12 3]", "[12 ");
        assertParserFail("abc.[123]", "abc.");
    }

    @Test
    public void parse_key_doubleQuote() {
        assertParser("[\"123\"]", "[\"123\"]");
        assertParser("[\"123\"][\"345\"]", "[\"123\"]", "[\"345\"]");
        assertParser(" [ \"123\" ] ", "[\"123\"]");
        assertParser(" [ \"123\" ] [ \"345\" ]", "[\"123\"]", "[\"345\"]");

        assertParser("abc[\"123\"]", "abc", "[\"123\"]");
        assertParser("abc [\"123\"]", "abc", "[\"123\"]");
        assertParser(" abc [ \"123\" ] ", "abc", "[\"123\"]");

        assertParser("abc[\"123\"][\"456\"]", "abc", "[\"123\"]", "[\"456\"]");
        assertParser("  abc  [ \"123\" ] [ \"456\" ] ", "abc", "[\"123\"]", "[\"456\"]");

        assertParserFail("[\"123\"", "[\\\"123\\\"");
        assertParserFail("[\"123", "[\\\"123");
        assertParserFail("[\"12 3]", "[\\\"12 3]");
        assertParserFail("abc.[\"123\"]", "abc.");
    }

    @Test
    public void parse_key_singleQuote() {
        assertParser("['123']", "[\"123\"]");
        assertParser("['123']['345']", "[\"123\"]", "[\"345\"]");
        assertParser(" [ '123' ] ", "[\"123\"]");
        assertParser(" [ '123' ] [ '345' ]", "[\"123\"]", "[\"345\"]");

        assertParser("abc['123']", "abc", "[\"123\"]");
        assertParser("abc ['123']", "abc", "[\"123\"]");
        assertParser(" abc [ '123' ] ", "abc", "[\"123\"]");

        assertParser("abc['123']['456']", "abc", "[\"123\"]", "[\"456\"]");
        assertParser("  abc  [ '123' ] [ '456' ] ", "abc", "[\"123\"]", "[\"456\"]");

        assertParserFail("['123'", "['123'");
        assertParserFail("['123", "['123");
        assertParserFail("['12 3]", "['12 3]");
        assertParserFail("abc.['123']", "abc.");
    }

    @Test
    public void parse_key_escape() {
        assertParser("['\\u02-3']", "[\"\\\\u02-3\"]");
        assertParser("['\\u02-3\\u4e2d']", "[\"\\\\u02-3÷–\"]");
        assertParser("['test']", "[\"test\"]");
        assertParser("['\ntest\b']", "[\"\\ntest\\b\"]");
        assertParser("['\u4e2d25foo\ntest\b']", "[\"÷–25foo\\ntest\\b\"]");
        assertParser("['\\'\foo\teste\r']", "[\"'\\foo\\teste\\r\"]");

        assertParserFail("['\\']", "['\\\\']");
    }

    @Test
    public void parse_with_visitor() {
        String propPath = "abc['abc'][123].def[1234]";
        MyVisitor visitor;

        // success=false
        visitor = new MyVisitor();
        visitor.success = false;
        PropertyPath.parse(propPath, visitor);

        assertArrayEquals(new String[] { "abc[abc]", "abc", "[abc]", "[123]", "def[1234].last", "def", "[1234].last" },
                visitor.getResults());
        assertArrayEquals(new String[] { "abc['abc']", "abc", "abc['abc']", "abc['abc'][123]",
                "abc['abc'][123].def[1234]", "abc['abc'][123].def", "abc['abc'][123].def[1234]" }, visitor
                .getDisplayNames());

        // success=true
        visitor = new MyVisitor();
        visitor.success = true;
        PropertyPath.parse(propPath, visitor);

        assertArrayEquals(new String[] { "abc[abc]", "[123]", "def[1234].last" }, visitor.getResults());
        assertArrayEquals(new String[] { "abc['abc']", "abc['abc'][123]", "abc['abc'][123].def[1234]" }, visitor
                .getDisplayNames());

        // another prop path
        propPath = " abc [ 'abc' ] [ 123 ] . def ";

        // success=false
        visitor = new MyVisitor();
        visitor.success = false;
        PropertyPath.parse(propPath, visitor);

        assertArrayEquals(new String[] { "abc[abc]", "abc", "[abc]", "[123]", "def.last" }, visitor.getResults());
        assertArrayEquals(new String[] { " abc [ 'abc' ]", " abc", " abc [ 'abc' ]", " abc [ 'abc' ] [ 123 ]",
                " abc [ 'abc' ] [ 123 ] . def" }, visitor.getDisplayNames());

        // success=true
        visitor = new MyVisitor();
        visitor.success = true;
        PropertyPath.parse(propPath, visitor);

        assertArrayEquals(new String[] { "abc[abc]", "[123]", "def.last" }, visitor.getResults());
        assertArrayEquals(new String[] { " abc [ 'abc' ]", " abc [ 'abc' ] [ 123 ]", " abc [ 'abc' ] [ 123 ] . def" },
                visitor.getDisplayNames());
    }

    @Test
    public void node_accept_with_visitor() {
        String propPath = "abc['abc'][123].def[1234]";
        PropertyPath path = PropertyPath.parse(propPath);
        MyVisitor visitor;

        // success=false
        visitor = new MyVisitor();
        visitor.success = false;
        path.accept(visitor);

        assertArrayEquals(new String[] { "abc[abc]", "abc", "[abc]", "[123]", "def[1234].last", "def", "[1234].last" },
                visitor.getResults());
        assertArrayEquals(new String[] { "abc['abc']", "abc", "abc['abc']", "abc['abc'][123]",
                "abc['abc'][123].def[1234]", "abc['abc'][123].def", "abc['abc'][123].def[1234]" }, visitor
                .getDisplayNames());

        // success=true
        visitor = new MyVisitor();
        visitor.success = true;
        path.accept(visitor);

        assertArrayEquals(new String[] { "abc[abc]", "[123]", "def[1234].last" }, visitor.getResults());
        assertArrayEquals(new String[] { "abc['abc']", "abc['abc'][123]", "abc['abc'][123].def[1234]" }, visitor
                .getDisplayNames());

        // another prop path
        propPath = " abc [ 'abc' ] [ 123 ] . def ";
        path = PropertyPath.parse(propPath);

        // success=false
        visitor = new MyVisitor();
        visitor.success = false;
        path.accept(visitor);

        assertArrayEquals(new String[] { "abc[abc]", "abc", "[abc]", "[123]", "def.last" }, visitor.getResults());
        assertArrayEquals(new String[] { " abc [ 'abc' ]", " abc", " abc [ 'abc' ]", " abc [ 'abc' ] [ 123 ]",
                " abc [ 'abc' ] [ 123 ] . def" }, visitor.getDisplayNames());

        // success=true
        visitor = new MyVisitor();
        visitor.success = true;
        path.accept(visitor);

        assertArrayEquals(new String[] { "abc[abc]", "[123]", "def.last" }, visitor.getResults());
        assertArrayEquals(new String[] { " abc [ 'abc' ]", " abc [ 'abc' ] [ 123 ]", " abc [ 'abc' ] [ 123 ] . def" },
                visitor.getDisplayNames());
    }

    private class MyVisitor implements Visitor {
        private List<String> results = createLinkedList();
        private List<String> displayNames = createLinkedList();
        private boolean success;

        public String[] getResults() {
            return results.toArray(new String[results.size()]);
        }

        public String[] getDisplayNames() {
            return displayNames.toArray(new String[displayNames.size()]);
        }

        public boolean visitIndexedProperty(String propertyName, int index, String displayName, boolean last) {
            results.add(String.format("%s[%d]%s", propertyName, index, last ? ".last" : ""));
            displayNames.add(displayName);
            return success;
        }

        public boolean visitMappedProperty(String propertyName, String key, String displayName, boolean last) {
            results.add(String.format("%s[%s]%s", propertyName, key, last ? ".last" : ""));
            displayNames.add(displayName);
            return success;
        }

        public void visitSimpleProperty(String propertyName, String displayName, boolean last) {
            results.add(String.format("%s%s", propertyName, last ? ".last" : ""));
            displayNames.add(displayName);
        }

        public void visitIndex(int index, String displayName, boolean last) {
            results.add(String.format("[%d]%s", index, last ? ".last" : ""));
            displayNames.add(displayName);
        }

        public void visitKey(String key, String displayName, boolean last) {
            results.add(String.format("[%s]%s", key, last ? ".last" : ""));
            displayNames.add(displayName);
        }
    }

    private void assertParser(String pathStr, String... results) {
        PropertyPath path = PropertyPath.parse(pathStr);
        List<String> nodes = createLinkedList();

        for (PropertyPath.Node node : path.getNodes()) {
            nodes.add(node.toString());
        }

        assertArrayEquals(results, nodes.toArray(new String[nodes.size()]));
    }

    private void assertParserFail(String pathStr, String near) {
        try {
            PropertyPath.parse(pathStr);
            fail();
        } catch (InvalidPropertyPathException e) {
            assertThat(e, exception("near \"" + near + "\""));
        }
    }
}
