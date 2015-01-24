/**    / \____  _    ______   _____ / \____   ____  _____
 *    /  \__  \/ \  / \__  \ /  __//  \__  \ /    \/ __  \   Javaslang
 *  _/  // _\  \  \/  / _\  \\_  \/  // _\  \  /\  \__/  /   Copyright 2014-2015 Daniel Dietrich
 * /___/ \_____/\____/\_____/____/\___\_____/_/  \_/____/    Licensed under the Apache License, Version 2.0
 */
package javaslang.test;

import javaslang.algebra.*;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;

/**
 * Represents an arbitrary object of type T.
 *
 * @param <T> The type of the arbitrary object.
 */
@FunctionalInterface
public interface Arbitrary<T> extends IntFunction<Gen<T>>, Function<Integer, Gen<T>>, Monad<T, Arbitrary<?>> {

    /**
     * <p>
     * Returns a generator for objects of type T.
     * Use {@link Gen#map(java.util.function.Function)} and {@link Gen#flatMap(java.util.function.Function)} to
     * combine object generators.
     * </p>
     * Example:
     * <pre>
     * <code>
     * // represents arbitrary binary trees of a certain depth n
     * final class ArbitraryTree implements Arbitrary&lt;BinaryTree&lt;Integer&gt;&gt; {
     *     &#64;Override
     *     public Gen&lt;BinaryTree&lt;Integer&gt;&gt; apply(int n) {
     *         return Gen.choose(-1000, 1000).flatMap(value -&gt; {
     *                  if (n == 0) {
     *                      return Gen.of(BinaryTree.leaf(value));
     *                  } else {
     *                      return Gen.frequency(
     *                              Tuple.of(1, Gen.of(BinaryTree.leaf(value))),
     *                              Tuple.of(4, Gen.of(BinaryTree.branch(apply(n / 2).get(), value, apply(n / 2).get())))
     *                      );
     *                  }
     *         });
     *     }
     * }
     *
     * // tree generator with a size hint of 10
     * final Gen&lt;BinaryTree&lt;Integer&gt;&gt; treeGen = new ArbitraryTree().apply(10);
     *
     * // stream sum of tree node values to console for 100 arbitrary trees
     * Stream.of(treeGen).map(Tree::sum).take(100).stdout();
     * </code>
     * </pre>
     *
     * @param size A size parameter which may be interpreted idividually and is constant for all arbitrary objects regarding one property check.
     * @return A generator for objects of type T.
     */
    @Override
    Gen<T> apply(int size);

    /**
     * Calls {@link #apply(int)}.
     *
     * @param size A size hint.
     * @return The result of {@link #apply(int)}.
     */
    @Override
    default Gen<T> apply(Integer size) {
        Objects.requireNonNull(size, "size is null");
        return apply((int) size);
    }

    /**
     * Maps arbitrary objects T to arbitrary object U.
     *
     * @param mapper A function that maps an arbitrary T to an object of type U.
     * @param <U>    Type of the mapped object
     * @return A new generator
     */
    @Override
    default <U> Arbitrary<U> map(Function<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        return n -> {
            final Gen<T> generator = apply(n);
            return () -> mapper.apply(generator.get());
        };
    }

    /**
     * Maps arbitrary objects T to arbitrary object U.
     *
     * @param mapper A function that maps arbitrary Ts to arbitrary Us given a mapper.
     * @param <U>    New type of arbitrary objects
     * @return A new Arbitrary
     */
    @SuppressWarnings("unchecked")
    @Override
    default <U, ARBITRARY extends HigherKinded<U, Arbitrary<?>>> Arbitrary<U> flatMap(Function<? super T, ARBITRARY> mapper) {
        return n -> {
            final Gen<T> generator = apply(n);
            return ((Arbitrary<U>) mapper.apply(generator.get())).apply(n);
        };
    }

    /**
     * Returns an Arbitrary based on this Arbitrary which produces values that fulfill the given predicate.
     *
     * @param predicate A predicate
     * @return A new generator
     */
    default Arbitrary<T> filter(Predicate<T> predicate) {
        return n -> apply(n).filter(predicate);
    }
}