/*
 * Copyright (c) 2022, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
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

package java.lang;

import java.lang.TemplatePolicy.StringPolicy;
import java.lang.invoke.*;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

import jdk.internal.javac.PreviewFeature;

/**
 * This interface describes the methods provided by a generalized string template policy. The
 * primary method {@link TemplatePolicy#apply} is used to validate and compose a result using
 * a {@link TemplatedString TemplatedString's} stencil (or fragments) and values list. For example:
 *
 * {@snippet :
 * class MyPolicy implements TemplatePolicy<String, IllegalArgumentException> {
 *     @Override
 *     public String apply(TemplatedString templatedString) throws IllegalArgumentException {
 *          StringBuilder sb = new StringBuilder();
 *          Iterator<String> fragmentsIter = templatedString.fragments().iterator();
 *
 *          for (Object value : templatedString.values()) {
 *              sb.append(fragmentsIter.next());
 *
 *              if (value instanceof Boolean) {
 *                  throw new IllegalArgumentException("I don't like Booleans");
 *              }
 *
 *              sb.append(value);
 *          }
 *
 *          sb.append(fragmentsIter.next());
 *
 *          return sb.toString();
 *     }
 * }
 *
 * MyPolicy myPolicy = new MyPolicy();
 * try {
 *     int x = 10;
 *     int y = 20;
 *     String result = myPolicy."\{x} + \{y} = \{x + y}";
 *     ...
 * } catch (IllegalArgumentException ex) {
 *     ...
 * }
 * }
 * Implementations of this interface may provide, but are not limited to, validating
 * inputs, composing inputs into a result, and transforming an intermediate string
 * result to a non-string value before delivering the final result.
 * <p>
 * The user has the option of validating inputs used in composition. For example an SQL
 * policy could prevent injection vulnerabilities by sanitizing inputs or throwing an
 * exception of type {@code E} if an SQL statement is a potential vulnerability.
 * <p>
 * Composing allows user control over how the result is assembled. Most often, a
 * user will construct a new string from the template string, with placeholders
 * replaced by stringified objects from the values list.
 * <p>
 * Transforming allows the policy to return something other than a string. For
 * instance, a JSON policy could return a JSON object, by parsing the string created
 * by composition, instead of the composed string.
 * <p>
 * {@link TemplatePolicy} is a {@link FunctionalInterface}. This permits declaration of a
 * policy using lambda expressions;
 * {@snippet :
 * TemplatePolicy<String, RuntimeException> concatPolicy = ts -> {
 *             StringBuilder sb = new StringBuilder();
 *             Iterator<String> fragmentsIter = ts.fragments().iterator();
 *             for (Object value : ts.values()) {
 *                 sb.append(fragmentsIter.next());
 *                 sb.append(value);
 *             }
 *             sb.append(fragmentsIter.next());
 *            return sb.toString();
 *         };
 * }
 * The {@link FunctionalInterface} {@link TemplatePolicy.SimplePolicy} is supplied to avoid
 * declaring checked exceptions;
 * {@snippet :
 * SimplePolicy<String> concatPolicy = ts -> {
 *             StringBuilder sb = new StringBuilder();
 *             Iterator<String> fragmentsIter = ts.fragments().iterator();
 *             for (Object value : ts.values()) {
 *                 sb.append(fragmentsIter.next());
 *                 sb.append(value);
 *             }
 *             sb.append(fragmentsIter.next());
 *            return sb.toString();
 *         };
 * }
 * The {@link FunctionalInterface} {@link java.lang.TemplatePolicy.StringPolicy} is supplied if
 * the policy returns {@link String};
 * {@snippet :
 * StringPolicy concatPolicy = ts -> {
 *             StringBuilder sb = new StringBuilder();
 *             Iterator<String> fragmentsIter = ts.fragments().iterator();
 *             for (Object value : ts.values()) {
 *                 sb.append(fragmentsIter.next());
 *                 sb.append(value);
 *             }
 *             sb.append(fragmentsIter.next());
 *            return sb.toString();
 *         };
 * }
 * The {@link TemplatedString#concat()} method is available for those policies that just need
 * to work with the concatenation;
 * {@snippet :
 * StringPolicy concatPolicy = TemplateString::concat;
 * }
 * or simply transform the string concatenation into something other than
 * {@link String};
 * {@snippet :
 * SimplePolicy<JSONObject> jsonPolicy = ts -> new JSONObject(ts.concat());
 * }
 * @implNote The Java compiler automatically imports {@link STR},
 * {@link java.util.FormatterPolicy#FMTR}, {@link SimplePolicy} and
 * {@link StringPolicy}.
 *
 * @param <R>  Policy's apply result type.
 * @param <E>  Exception thrown type.
 *
 * @see java.util.FormatterPolicy
 */
@PreviewFeature(feature=PreviewFeature.Feature.TEMPLATED_STRINGS)
@FunctionalInterface
public interface TemplatePolicy<R, E extends Throwable> {

    /**
     * Constructs a result based on the template string and values in the
     * supplied {@link TemplatedString templatedString} object.
     *
     * @param templatedString  a {@link TemplatedString} instance
     *
     * @return constructed object of type R
     *
     * @throws E exception thrown by the template policy when validation fails
     */
    R apply(TemplatedString templatedString) throws E;

    /**
     * This interface simplifies declaration of {@link TemplatePolicy TemplatePolicys}
     * that do not throw checked exceptions. For example:
     * {@snippet :
     * SimplePolicy<String> concatPolicy = ts -> {
     *             StringBuilder sb = new StringBuilder();
     *             Iterator<String> fragmentsIter = ts.fragments().iterator();
     *             for (Object value : ts.values()) {
     *                 sb.append(fragmentsIter.next());
     *                 sb.append(value);
     *             }
     *             sb.append(fragmentsIter.next());
     *            return sb.toString();
     *         });
     * }
     *
     * @param <R>  Policy's apply result type.
     */
    @PreviewFeature(feature=PreviewFeature.Feature.TEMPLATED_STRINGS)
    @FunctionalInterface
    interface SimplePolicy<R> extends TemplatePolicy<R, RuntimeException> {
        /**
         * Chain template policies to produce a new policy that applies the supplied
         * policies from right to left. The {@code head} policy is a {@link SimplePolicy}
         * The {@code tail} policies must return type {@link TemplatedString}.
         *
         * @param head  last {@link SimplePolicy} to be applied, return type {@code R}
         * @param tail  first policies to apply, return type {@code TemplatedString}
         *
         * @return a new {@link SimplePolicy} that applies the supplied policies
         *         from right to left
         *
         * @param <R> return type of the head policy and resulting policy
         *
         * @throws NullPointerException if any of the arguments is null.
         */
        @SuppressWarnings("varargs")
        @SafeVarargs
        public static <R> SimplePolicy<R>
            chain(SimplePolicy<R> head,
                  TemplatePolicy<TemplatedString, RuntimeException>... tail) {
            Objects.requireNonNull(head, "head must not be null");
            Objects.requireNonNull(tail, "tail must not be null");

            if (tail.length == 0) {
                return head;
            }

            TemplatePolicy<TemplatedString, RuntimeException> last =
                    TemplatePolicy.chain(tail[0], Arrays.copyOfRange(tail, 1, tail.length));

            return ts -> head.apply(last.apply(ts));
        }
    }

    /**
     * This interface simplifies declaration of {@link java.lang.TemplatePolicy TemplatePolicys}
     * that do not throw checked exceptions and have a result type of {@link String}. For example:
     * {@snippet :
     * StringPolicy policy = ts -> ts.concat();
     * }
     */
    @PreviewFeature(feature=PreviewFeature.Feature.TEMPLATED_STRINGS)
    @FunctionalInterface
    interface StringPolicy extends SimplePolicy<String> {
        /**
         * Chain template policies to produce a new policy that applies the supplied
         * policies from right to left. The {@code head} policy is a {@link StringPolicy}
         * The {@code tail} policies must return type {@link TemplatedString}.
         *
         * @param head  last {@link StringPolicy} to be applied, return type {@link String}
         * @param tail  first policies to apply, return type {@code TemplatedString}
         *
         * @return a new {@link StringPolicy} that applies the supplied policies
         *         from right to left
         *
         * @throws NullPointerException if any of the arguments is null.
         */
        @SuppressWarnings("varargs")
        @SafeVarargs
        public static StringPolicy
            chain(StringPolicy head,
                 TemplatePolicy<TemplatedString, RuntimeException>... tail) {
            Objects.requireNonNull(head, "head must not be null");
            Objects.requireNonNull(tail, "tail must not be null");

            if (tail.length == 0) {
                return head;
            }

            TemplatePolicy<TemplatedString, RuntimeException> last =
                    TemplatePolicy.chain(tail[0], Arrays.copyOfRange(tail, 1, tail.length));

            return ts -> head.apply(last.apply(ts));
        }
    }

    /**
     * Chain template policies to produce a new policy that applies the supplied
     * policies from right to left. The {@code tail} policies must return type
     * {@link TemplatedString}.
     *
     * @param head  last {@link TemplatePolicy} to be applied, return type {@code R}
     * @param tail  first policies to apply, return type {@code TemplatedString}
     *
     * @return a new {@link TemplatePolicy} that applies the supplied
     *         policies from right to left
     *
     * @param <R> return type of the head policy and resulting policy
     * @param <E> exception thrown type by head policy and resulting policy
     *
     * @throws NullPointerException if any of the arguments is null.
     */
    @SafeVarargs
    public static <R, E extends Throwable> TemplatePolicy<R, E>
            chain(TemplatePolicy<R, E> head,
              TemplatePolicy<TemplatedString, RuntimeException>... tail) {
        Objects.requireNonNull(head, "head must not be null");
        Objects.requireNonNull(tail, "tail must not be null");

        if (tail.length == 0) {
            return head;
        }

        int index = tail.length;
        TemplatePolicy<TemplatedString, RuntimeException> current = tail[--index];

        while (index != 0) {
            TemplatePolicy<TemplatedString, RuntimeException> second = tail[--index];
            TemplatePolicy<TemplatedString, RuntimeException> first = current;
            current = ts -> second.apply(first.apply(ts));
        }

        TemplatePolicy<TemplatedString, RuntimeException> last = current;

        return ts -> head.apply(last.apply(ts));
    }

    /**
     * Simple concatenation policy instance.
     *
     * @implNote The result of concatenation is not interned.
     */
    public static final StringPolicy STR = new StringPolicy() {
        @Override
        public String apply(TemplatedString templatedString) {
            Objects.requireNonNull(templatedString);

            return templatedString.concat();
        }
    };

    /**
     * Policies using this additional interface have the flexibility to specialize
     * the composition of the templated string by returning a customized
     * {@link CallSite CallSites} from {@link TemplatePolicy.PolicyLinkage#applier applier}.
     * These specializations are typically implemented to improve performance;
     * specializing value types or avoiding boxing and vararg arrays.
     *
     * @implNote This interface is sealed to only allow standard policies.
     */
    @PreviewFeature(feature=PreviewFeature.Feature.TEMPLATED_STRINGS)
    sealed interface PolicyLinkage permits FormatterPolicy {
        /**
         * Construct a {@link MethodHandle} that constructs a result based on the
         * bootstrap method information.
         *
         * @param stencil     stencil string with placeholders
         * @param type        methiod type
         *
         * @return {@link MethodHandle} for the policy applied to template
         *
         * @throws NullPointerException if any of the arguments are null
         */
        MethodHandle applier(String stencil, MethodType type);
    }

}
