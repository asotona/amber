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

package java.lang.runtime;

/*
 * @test
 * @summary Test features provided by the Carrier class.
 * @compile --patch-module java.base=${test.src} --enable-preview -source ${jdk.version} CarrierTest.java
 * @run main/othervm --patch-module java.base=${test.class.path} --enable-preview java.lang.runtime.CarrierTest
 */

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.runtime.Carriers;
import java.lang.runtime.Carriers.*;
import java.util.Arrays;

public class CarrierTest {
    public static void main(String[] args) throws Throwable {
        primitivesTest();
        limitsTest();
        cacheTest();
        staticTest();
    }

    static void assertTrue(boolean test, String message) {
        if (!test) {
            throw new RuntimeException(message);
        }
    }

    static void primitivesTest() throws Throwable {
        MethodType methodType =
                MethodType.methodType(Object.class, byte.class, short.class,
                        char.class, int.class, long.class,
                        float.class, double.class,
                        boolean.class, String.class);
        CarrierElements carrier = Carriers.CarrierFactory.of(methodType);
        Class<?> carrierClass = carrier.carrierClass();
        assertTrue(!carrierClass.isArray(), "carrier should be instance");
        MethodHandle constructor = carrier.constructor();
        Object object = (Object)constructor.invokeExact((byte)0xFF, (short)0xFFFF,
                'C', 0xFFFFFFFF, 0xFFFFFFFFFFFFFFFFL,
                1.0f / 3.0f, 1.0 / 3.0,
                true, "abcde");
        MethodHandle[] components = carrier.components().toArray(new MethodHandle[0]);

        assertTrue((byte)components[0].invokeExact(object) == (byte)0xFF,
                "primitive byte test failure");
        assertTrue((short)components[1].invokeExact(object) == (short)0xFFFF,
                "primitive short test failure");
        assertTrue((char)components[2].invokeExact(object) == 'C',
                "primitive char test failure");
        assertTrue((int)components[3].invokeExact(object) == 0xFFFFFFFF,
                "primitive int test failure");
        assertTrue((long)components[4].invokeExact(object) == 0xFFFFFFFFFFFFFFFFL,
                "primitive long test failure");
        assertTrue((float)components[5].invokeExact(object) == 1.0f / 3.0f,
                "primitive float test failure");
        assertTrue((double)components[6].invokeExact(object) == 1.0 / 3.0,
                "primitive double test failure");
        assertTrue((boolean)components[7].invokeExact(object),
                "primitive boolean test failure");
        assertTrue("abcde".equals((String)components[8].invokeExact(object)),
                "primitive String test failure");

        MethodHandle component = carrier.component(8);
        assertTrue("abcde".equals((String)component.invokeExact(object)),
                "primitive String test failure");
    }

    static void limitsTest() {
        boolean passed;

        passed = false;
        try {
            Class<?>[] ptypes = new Class<?>[Carriers.MAX_COMPONENTS + 1];
            Arrays.fill(ptypes, Object.class);
            MethodType methodType = MethodType.methodType(Object.class, ptypes);
            CarrierElements carrier = Carriers.CarrierFactory.of(methodType);
            MethodHandle constructor = carrier.constructor();
        } catch (IllegalArgumentException ex) {
            passed = true;
        }

        assertTrue(passed, "failed to report too many components");

        passed = false;
        try {
            Class<?>[] ptypes = new Class<?>[Carriers.MAX_COMPONENTS / 2 + 1];
            Arrays.fill(ptypes, long.class);
            MethodType methodType = MethodType.methodType(Object.class, ptypes);
            CarrierElements carrier = Carriers.CarrierFactory.of(methodType);
            MethodHandle constructor = carrier.constructor();
        } catch (IllegalArgumentException ex) {
            passed = true;
        }

        assertTrue(passed, "failed to report too many components");
    }

    static void cacheTest() {
        Class<?>[] ptypes = new Class<?>[] {
                byte.class, short.class,
                char.class, int.class, long.class,
                float.class, double.class,
                boolean.class, String.class
        };
        MethodType methodType = MethodType.methodType(Object.class, ptypes);
        CarrierElements carrier1 = Carriers.CarrierFactory.of(ptypes);
        CarrierElements carrier2 = Carriers.CarrierFactory.of(methodType);

        assertTrue(carrier1 == carrier2, "carrier cache not matching correctly");
    }

    static void staticTest() {
        Class<?>[] ptypes = new Class<?>[] {
                byte.class, short.class,
                char.class, int.class, long.class,
                float.class, double.class,
                boolean.class, String.class
        };
        MethodType methodType = MethodType.methodType(Object.class, ptypes);
        CarrierElements carrier = Carriers.CarrierFactory.of(methodType);
        assertTrue(carrier.constructor() == Carriers.constructor(methodType),
                "static constructor incorrect");
        assertTrue(carrier.components() == Carriers.components(methodType),
                "static components incorrect");
        assertTrue(carrier.component(1) == Carriers.component(methodType, 1),
                "static component incorrect");
    }
}
