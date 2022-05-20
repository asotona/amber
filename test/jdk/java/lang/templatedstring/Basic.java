/*
 * Copyright (c) 2022, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

/*
 * @test
 * @bug 0000000
 * @summary Exercise runtime handing of templated strings.
 * @compile --enable-preview -source ${jdk.version} Basic.java
 * @run main/othervm --enable-preview Basic
 */

import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;

public class Basic {
    public static void main(String... arg) {
        concatenationTests();
        componentTests();
        limitsTests();
        policyTests();
        templatedStringCoverage();
        templateBuilderCoverage();
        templatePolicyCoverage();
        policyBuilderCoverage();
    }

    static void ASSERT(String a, String b) {
        if (!Objects.equals(a, b)) {
            throw new RuntimeException("Test failed");
        }
    }

    static void ASSERT(Object a, Object b) {
        if (!Objects.deepEquals(a, b)) {
            throw new RuntimeException("Test failed");
        }
    }

    /*
     * Concatenation tests.
     */
    static void concatenationTests() {
        int x = 10;
        int y = 20;

        ASSERT(STR."\{x} \{y}", x + " " + y);
        ASSERT(STR."\{x + y}", "" + (x + y));
        ASSERT(STR.apply("\{x} \{y}"), x + " " + y);
        ASSERT(STR.apply("\{x + y}"), "" + (x + y));
        ASSERT(("\{x} \{y}").apply(STR), x + " " + y);
        ASSERT(("\{x + y}").apply(STR), "" + (x + y));
    }

    /*
     * Component tests.
     */
    static void componentTests() {
        int x = 10;
        int y = 20;

        TemplatedString ts = "\{x} + \{y} = \{x + y}";
        ASSERT(ts.stencil(), "\uFFFC + \uFFFC = \uFFFC");
        ASSERT(ts.values(), List.of(x, y, x + y));
        ASSERT(ts.fragments(), List.of("", " + ", " = ", ""));
        ASSERT(ts.concat(), x + " + " + y + " = " + (x + y));
    }

    /*
     * Limits tests.
     */
    static void limitsTests() {
        int x = 9;

        TemplatedString ts250 = """
             \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x} \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}
             \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x} \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}
             \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x} \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}
             \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x} \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}
             \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x} \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}

             \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x} \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}
             \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x} \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}
             \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x} \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}
             \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x} \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}
             \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x} \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}

             \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x} \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}
             \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x} \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}
             \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}
             """;
        ASSERT(ts250.values().size(), 250);
        ASSERT(ts250.concat(), """
               9999999999 9999999999
               9999999999 9999999999
               9999999999 9999999999
               9999999999 9999999999
               9999999999 9999999999

               9999999999 9999999999
               9999999999 9999999999
               9999999999 9999999999
               9999999999 9999999999
               9999999999 9999999999

               9999999999 9999999999
               9999999999 9999999999
               9999999999
               """);

        TemplatedString ts251 = """
             \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x} \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}
             \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x} \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}
             \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x} \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}
             \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x} \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}
             \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x} \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}

             \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x} \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}
             \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x} \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}
             \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x} \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}
             \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x} \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}
             \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x} \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}

             \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x} \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}
             \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x} \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}
             \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x} \{x}
             """;
        ASSERT(ts251.values().size(), 251);
        ASSERT(ts251.concat(), """
               9999999999 9999999999
               9999999999 9999999999
               9999999999 9999999999
               9999999999 9999999999
               9999999999 9999999999

               9999999999 9999999999
               9999999999 9999999999
               9999999999 9999999999
               9999999999 9999999999
               9999999999 9999999999

               9999999999 9999999999
               9999999999 9999999999
               9999999999 9
               """);

        TemplatedString ts252 = """
             \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x} \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}
             \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x} \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}
             \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x} \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}
             \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x} \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}
             \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x} \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}

             \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x} \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}
             \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x} \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}
             \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x} \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}
             \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x} \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}
             \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x} \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}

             \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x} \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}
             \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x} \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}
             \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x} \{x}\{x}
             """;
        ASSERT(ts252.values().size(), 252);
        ASSERT(ts252.concat(), """
               9999999999 9999999999
               9999999999 9999999999
               9999999999 9999999999
               9999999999 9999999999
               9999999999 9999999999

               9999999999 9999999999
               9999999999 9999999999
               9999999999 9999999999
               9999999999 9999999999
               9999999999 9999999999

               9999999999 9999999999
               9999999999 9999999999
               9999999999 99
               """);

        TemplatedString ts253 = """
             \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x} \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}
             \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x} \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}
             \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x} \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}
             \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x} \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}
             \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x} \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}

             \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x} \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}
             \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x} \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}
             \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x} \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}
             \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x} \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}
             \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x} \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}

             \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x} \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}
             \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x} \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}
             \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x} \{x}\{x}\{x}
             """;
        ASSERT(ts253.values().size(), 253);
        ASSERT(ts253.concat(), """
               9999999999 9999999999
               9999999999 9999999999
               9999999999 9999999999
               9999999999 9999999999
               9999999999 9999999999

               9999999999 9999999999
               9999999999 9999999999
               9999999999 9999999999
               9999999999 9999999999
               9999999999 9999999999

               9999999999 9999999999
               9999999999 9999999999
               9999999999 999
               """);

        TemplatedString ts254 = """
             \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x} \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}
             \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x} \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}
             \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x} \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}
             \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x} \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}
             \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x} \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}

             \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x} \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}
             \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x} \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}
             \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x} \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}
             \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x} \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}
             \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x} \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}

             \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x} \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}
             \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x} \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}
             \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x} \{x}\{x}\{x}\{x}
             """;
        ASSERT(ts254.values().size(), 254);
        ASSERT(ts254.concat(), """
               9999999999 9999999999
               9999999999 9999999999
               9999999999 9999999999
               9999999999 9999999999
               9999999999 9999999999

               9999999999 9999999999
               9999999999 9999999999
               9999999999 9999999999
               9999999999 9999999999
               9999999999 9999999999

               9999999999 9999999999
               9999999999 9999999999
               9999999999 9999
               """);

        TemplatedString ts255 = """
             \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x} \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}
             \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x} \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}
             \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x} \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}
             \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x} \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}
             \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x} \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}

             \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x} \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}
             \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x} \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}
             \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x} \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}
             \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x} \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}
             \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x} \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}

             \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x} \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}
             \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x} \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}
             \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x} \{x}\{x}\{x}\{x}\{x}
             """;
        ASSERT(ts255.values().size(), 255);
        ASSERT(ts255.concat(), """
               9999999999 9999999999
               9999999999 9999999999
               9999999999 9999999999
               9999999999 9999999999
               9999999999 9999999999

               9999999999 9999999999
               9999999999 9999999999
               9999999999 9999999999
               9999999999 9999999999
               9999999999 9999999999

               9999999999 9999999999
               9999999999 9999999999
               9999999999 99999
               """);

        TemplatedString ts256 = """
             \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x} \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}
             \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x} \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}
             \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x} \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}
             \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x} \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}
             \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x} \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}

             \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x} \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}
             \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x} \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}
             \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x} \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}
             \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x} \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}
             \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x} \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}

             \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x} \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}
             \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x} \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}
             \{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x}\{x} \{x}\{x}\{x}\{x}\{x}\{x}
             """;
        ASSERT(ts256.values().size(), 256);
        ASSERT(ts256.concat(), """
               9999999999 9999999999
               9999999999 9999999999
               9999999999 9999999999
               9999999999 9999999999
               9999999999 9999999999

               9999999999 9999999999
               9999999999 9999999999
               9999999999 9999999999
               9999999999 9999999999
               9999999999 9999999999

               9999999999 9999999999
               9999999999 9999999999
               9999999999 999999
               """);

    }

    /*
     * Policy tests.
     */
    public static final SimplePolicy<TemplatedString> STRINGIFY = ts -> {
        String stencil = ts.stencil().toUpperCase();
        List<Object> values = ts.values()
                .stream()
                .map(v -> (Object)String.valueOf(v))
                .toList();

        return TemplatedString.of(stencil, values);
    };

    public static final SimplePolicy<TemplatedString> UPPER = ts -> {
        String stencil = ts.stencil().toUpperCase();
        List<Object> values = ts.values()
                .stream()
                .map(v -> v instanceof String s ? s.toUpperCase() : v)
                .toList();

        return TemplatedString.of(stencil, values);
    };

    static void policyTests() {
        String name = "Joan";
        int age = 25;
        StringPolicy policy = StringPolicy.chain(STR, UPPER, STRINGIFY);
        ASSERT(policy."\{name} is \{age} years old", "JOAN IS 25 YEARS OLD");
    }

    /*
     *  TemplatedString coverage
     */
    static void templatedStringCoverage() {
        TemplatedString tsNoValues = TemplatedString.of("No Values");

        ASSERT(tsNoValues.stencil(), "No Values");
        ASSERT(tsNoValues.values(), List.of());
        ASSERT(tsNoValues.fragments(), List.of("No Values"));
        ASSERT(tsNoValues.concat(), STR."No Values");

        int x = 10, y = 20;
        TemplatedString src = "\{x} + \{y} = \{x + y}";
        TemplatedString tsValues = TemplatedString.of(src.stencil(), src.values());
        ASSERT(tsValues.stencil(), "\uFFFC + \uFFFC = \uFFFC");
        ASSERT(tsValues.values(), List.of(x, y, x + y));
        ASSERT(tsValues.fragments(), List.of("", " + ", " = ", ""));
        ASSERT(tsValues.concat(), x + " + " + y + " = " + (x + y));

        ASSERT(TemplatedString.split(src.stencil()), List.of("", " + ", " = ", ""));
        ASSERT(TemplatedString.split("No Values"), List.of("No Values"));
        ASSERT(TemplatedString.split(""), List.of(""));
    }

    /*
     * TemplateBuilder coverage.
     */
    static void templateBuilderCoverage() {
        int x = 10;
        int y = 20;
        TemplatedString ts = TemplatedString.builder()
                .fragment("The result of adding ")
                .value(x)
                .template(" and \{y} equals \{x + y}")
                .build();
        ASSERT(STR.apply(ts), "The result of adding 10 and 20 equals 30");

        ts = TemplatedString.builder()
                .fragment("x = ")
                .value(x)
                .clear()
                .fragment("y = ")
                .value(y)
                .build();

        ASSERT(STR.apply(ts), "y = 20");
    }

    /*
     * TemplatePolicy coverage.
     */

    static class Policy0 implements TemplatePolicy<String, IllegalArgumentException> {
        @Override
        public String apply(TemplatedString templatedString) throws IllegalArgumentException {
            StringBuilder sb = new StringBuilder();
            Iterator<String> fragmentsIter = templatedString.fragments().iterator();

            for (Object value : templatedString.values()) {
                sb.append(fragmentsIter.next());

                if (value instanceof Boolean) {
                    throw new IllegalArgumentException("I don't like Booleans");
                }

                sb.append(value);
            }

            sb.append(fragmentsIter.next());

            return sb.toString();
        }
    }

    static Policy0 policy0 = new Policy0();

    static TemplatePolicy<String, RuntimeException> policy1 = ts -> {
        StringBuilder sb = new StringBuilder();
        Iterator<String> fragmentsIter = ts.fragments().iterator();
        for (Object value : ts.values()) {
            sb.append(fragmentsIter.next());
            sb.append(value);
        }
        sb.append(fragmentsIter.next());
        return sb.toString();
    };

    static SimplePolicy<String> policy2 = ts -> {
        StringBuilder sb = new StringBuilder();
        Iterator<String> fragmentsIter = ts.fragments().iterator();
        for (Object value : ts.values()) {
            sb.append(fragmentsIter.next());
            sb.append(value);
        }
        sb.append(fragmentsIter.next());
        return sb.toString();
    };

    static StringPolicy policy3 = ts -> {
        StringBuilder sb = new StringBuilder();
        Iterator<String> fragmentsIter = ts.fragments().iterator();
        for (Object value : ts.values()) {
            sb.append(fragmentsIter.next());
            sb.append(value);
        }
        sb.append(fragmentsIter.next());
        return sb.toString();
    };

    static StringPolicy policy4 = TemplatedString::concat;

    static void templatePolicyCoverage() {
        try {
            int x = 10;
            int y = 20;
            ASSERT(policy0."\{x} + \{y} = \{x + y}", "10 + 20 = 30");
            ASSERT(policy1."\{x} + \{y} = \{x + y}", "10 + 20 = 30");
            ASSERT(policy2."\{x} + \{y} = \{x + y}", "10 + 20 = 30");
            ASSERT(policy3."\{x} + \{y} = \{x + y}", "10 + 20 = 30");
            ASSERT(policy4."\{x} + \{y} = \{x + y}", "10 + 20 = 30");
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException("policy fail");
        }
    }

    static String justify(String string, int width) {
        boolean leftJustify = width < 0;
        int length = string.length();
        width = Math.abs(width);
        int diff = width - length;

        if (diff < 0) {
            string = "*".repeat(width);
        } else if (0 < diff) {
            if (leftJustify) {
                string += " ".repeat(diff);
            } else {
                string = " ".repeat(diff) + string;
            }
        }

        return string;
    }

    /*
     * PolicyBuilder coverage.
     */
    static void policyBuilderCoverage() {
        int x = 10;
        int y = 12345;
        int z = -123;

        StringPolicy policy0 = TemplatePolicy.builder()
                .preliminary(ts -> TemplatedString.of(ts.stencil().toUpperCase(), ts.values()))
                .build();
        ASSERT(policy0."Some lowercase", "SOME LOWERCASE");

        StringPolicy policy1 = TemplatePolicy.builder()
                .fragment(f -> f.toUpperCase())
                .build();
        ASSERT(policy1."Some lowercase", "SOME LOWERCASE");

        StringPolicy policy2 = TemplatePolicy.builder()
                .fragment(f -> f.toUpperCase())
                .fragment(f -> f.toLowerCase())
                .build();
        ASSERT(policy2."Some lowercase", "some lowercase");

        StringPolicy policy3 = TemplatePolicy.builder()
                .value(v -> v instanceof Integer i ? Math.abs(i) : v)
                .build();
        ASSERT(policy3."\{z}", "123");

        StringPolicy policy4 = TemplatePolicy.builder()
                .format("%", (specifier, value) ->
                        justify(String.valueOf(value), Integer.parseInt(specifier)))
                .build();
        ASSERT(policy4."%4\{x} + %4\{y} = %-5\{x + y}", "  10 + **** = 12355");

        Supplier<String> supplier = () -> "x";
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<String> future = executor.submit(() -> "y");
        FutureTask<String> futureTask = new FutureTask<String>(() -> "z");

        StringPolicy policy5 = TemplatePolicy.builder()
                .resolve()
                .build();
        ASSERT(policy5."\{supplier} \{future} \{futureTask}", "x y z");

        SimplePolicy<Integer> policy6 = TemplatePolicy.builder()
                .build(s -> Integer.parseInt(s));
        ASSERT(policy6."123".toString(), "123");
    }
}
