/*     / \____  _    _  ____   ______  / \ ____  __    _ _____
 *    /  /    \/ \  / \/    \ /  /\__\/  //    \/  \  / /  _  \   Javaslang
 *  _/  /  /\  \  \/  /  /\  \\__\\  \  //  /\  \ /\\/  \__/  /   Copyright 2014-now Daniel Dietrich
 * /___/\_/  \_/\____/\_/  \_/\__\/__/___\_/  \_//  \__/_____/    Licensed under the Apache License, Version 2.0
 */
package javaslang.control;

import javaslang.Serializables;
import javaslang.collection.Seq;
import org.junit.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

public class OptionTest {

    // -- construction

    @Test
    public void shouldMapNullToNone() {
        assertThat(Option.of(null)).isEqualTo(Option.none());
    }

    @Test
    public void shouldMapNonNullToSome() {
        final Option<?> option = Option.of(new Object());
        assertThat(option.isDefined()).isTrue();
    }

    @Test
    public void shouldWrapNullInSome() {
        final Option<?> some = Option.some(null);
        assertThat(some.get()).isEqualTo(null);
    }

    @Test
    public void shouldCreateNothing() {
        assertThat(Option.nothing()).isEqualTo(Option.some(null));
    }

    @Test
    public void shouldWrapIfTrue() {
        assertThat(Option.of(null)).isEqualTo(Option.when(true, () -> null));
    }

    @Test
    public void shouldNotWrapIfFalse() {
        assertThat(Option.none()).isEqualTo(Option.when(false, () -> null));
    }

    @Test
    public void shouldNotExecuteIfFalse() {
        assertThat(Option.none()).isEqualTo(Option.when(false, () -> {
            throw new RuntimeException();
        }));
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowExceptionOnNull() {
        assertThat(Option.none()).isEqualTo(Option.when(false, null));
    }

    @Test
    public void shouldWrapEmptyOptional() {
        assertThat(Option.none()).isEqualTo(Option.ofOptional(Optional.empty()));
    }

    @Test
    public void shouldWrapSomeOptional() {
        assertThat(Option.of(1)).isEqualTo(Option.ofOptional(Optional.of(1)));
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowExceptionOnNullOptional() {
        assertThat(Option.none()).isEqualTo(Option.ofOptional(null));
    }

    // -- sequence

    @Test
    public void shouldConvertListOfNonEmptyOptionsToOptionOfList() {
        List<Option<String>> options = Arrays.asList(Option.of("a"), Option.of("b"), Option.of("c"));
        Option<Seq<String>> reducedOption = Option.sequence(options);
        assertThat(reducedOption instanceof Option.Some).isTrue();
        assertThat(reducedOption.get().size()).isEqualTo(3);
        assertThat(reducedOption.get().mkString()).isEqualTo("abc");
    }

    @Test
    public void shouldConvertListOfEmptyOptionsToOptionOfList() {
        List<Option<String>> options = Arrays.asList(Option.none(), Option.none(), Option.none());
        Option<Seq<String>> option = Option.sequence(options);
        assertThat(option instanceof Option.None).isTrue();
    }

    @Test
    public void shouldConvertListOfMixedOptionsToOptionOfList() {
        List<Option<String>> options = Arrays.asList(Option.of("a"), Option.none(), Option.of("c"));
        Option<Seq<String>> option = Option.sequence(options);
        assertThat(option instanceof Option.None).isTrue();
    }

    // -- get

    @Test
    public void shouldSucceedOnGetWhenValueIsPresent() {
        assertThat(Option.of(1).get()).isEqualTo(1);
    }

    @Test(expected = NoSuchElementException.class)
    public void shouldThrowOnGetWhenValueIsNotPresent() {
        Option.none().get();
    }

    // -- reduce

    @Test(expected = NoSuchElementException.class)
    public void shouldThrowWhenReduceNil() {
        Option.none().reduce((a, b) -> a);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowWhenReduceNullOperator() {
        Option.none().reduce(null);
    }

    @Test
    public void shouldReduceNonNil() {
        assertThat(Option.of(1).reduce((a, b) -> a + b)).isEqualTo(1);
    }

    // -- reduceOption

    @Test
    public void shouldThrowWhenReduceOptionNil() {
        assertThat(Option.none().reduceOption((a, b) -> a)).isSameAs(Option.none());
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowWhenReduceOptionNullOperator() {
        Option.none().reduceOption(null);
    }

    @Test
    public void shouldReduceOptionNonNil() {
        assertThat(Option.of(1).reduceOption((a, b) -> a + b)).isEqualTo(Option.of(1));
    }

    // -- reduceLeft

    @Test(expected = NoSuchElementException.class)
    public void shouldThrowWhenReduceLeftNil() {
        Option.none().reduceLeft((a, b) -> a);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowWhenReduceLeftNullOperator() {
        Option.none().reduceLeft(null);
    }

    @Test
    public void shouldReduceLeftNonNil() {
        assertThat(Option.of(1).reduceLeft((xs, x) -> xs + x)).isEqualTo(1);
    }

    // -- reduceLeftOption

    @Test
    public void shouldReduceLeftOptionNil() {
        assertThat(Option.none().reduceLeftOption((a, b) -> a)).isSameAs(Option.none());
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowWhenReduceLeftOptionNullOperator() {
        Option.none().reduceLeftOption(null);
    }

    @Test
    public void shouldReduceLeftOptionNonNil() {
        assertThat(Option.of(1).reduceLeftOption((xs, x) -> xs + x)).isEqualTo(Option.of(1));
    }

    // -- reduceRight

    @Test(expected = NoSuchElementException.class)
    public void shouldThrowWhenReduceRightNil() {
        Option.none().reduceRight((a, b) -> a);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowWhenReduceRightNullOperator() {
        Option.none().reduceRight(null);
    }

    @Test
    public void shouldReduceRightNonNil() {
        assertThat(Option.of(1).reduceRight((x, xs) -> x + xs)).isEqualTo(1);
    }

    // -- reduceRightOption

    @Test
    public void shouldReduceRightOptionNil() {
        assertThat(Option.none().reduceRightOption((a, b) -> a)).isSameAs(Option.none());
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowWhenReduceRightOptionNullOperator() {
        Option.none().reduceRightOption(null);
    }

    @Test
    public void shouldReduceRightOptionNonNil() {
        assertThat(Option.of(1).reduceRightOption((x, xs) -> x + xs)).isEqualTo(Option.of(1));
    }

    // -- orElse

    @Test
    public void shouldGetValueOnOrElseWhenValueIsPresent() {
        assertThat(Option.of(1).getOrElse(2)).isEqualTo(1);
    }

    @Test
    public void shouldGetAlternativeOnOrElseWhenValueIsNotPresent() {
        assertThat(Option.none().getOrElse(2)).isEqualTo(2);
    }

    // -- orElseGet

    @Test
    public void shouldGetValueOnOrElseGetWhenValueIsPresent() {
        assertThat(Option.of(1).getOrElse(() -> 2)).isEqualTo(1);
    }

    @Test
    public void shouldGetAlternativeOnOrElseGetWhenValueIsNotPresent() {
        assertThat(Option.none().getOrElse(() -> 2)).isEqualTo(2);
    }

    // -- orElseThrow

    @Test
    public void shouldGetValueOnOrElseThrowWhenValueIsPresent() {
        assertThat(Option.of(1).orElseThrow(() -> new RuntimeException("none"))).isEqualTo(1);
    }

    @Test(expected = RuntimeException.class)
    public void shouldThrowOnOrElseThrowWhenValueIsNotPresent() {
        Option.none().orElseThrow(() -> new RuntimeException("none"));
    }

    // -- toJavaOptional

    @Test
    public void shouldConvertNoneToJavaOptional() {
        final Option<Object> none = Option.none();
        assertThat(none.toJavaOptional()).isEqualTo(Optional.empty());
    }

    @Test
    public void shouldConvertSomeToJavaOptional() {
        final Option<Integer> some = Option.some(1);
        assertThat(some.toJavaOptional()).isEqualTo(Optional.of(1));
    }

    // -- isPresent

    @Test
    public void shouldBePresentOnIsPresentWhenValueIsPresent() {
        assertThat(Option.of(1).isDefined()).isTrue();
    }

    @Test
    public void shouldNotBePresentOnIsPresentWhenValueIsNotPresent() {
        assertThat(Option.none().isDefined()).isFalse();
    }

    // -- isEmpty

    @Test
    public void shouldBeEmptyOnIsEmptyWhenValueIsEmpty() {
        assertThat(Option.none().isEmpty()).isTrue();
    }

    @Test
    public void shouldBePresentOnIsEmptyWhenValueIsPresent() {
        assertThat(Option.of(1).isEmpty()).isFalse();
    }

    // -- ifPresent

    @Test
    public void shouldConsumePresentValueOnIsPresentWhenValueIsPresent() {
        final int[] actual = new int[] { -1 };
        Option.of(1).forEach(i -> actual[0] = i);
        assertThat(actual[0]).isEqualTo(1);
    }

    @Test
    public void shouldNotConsumeAnythingOnIsPresentWhenValueIsNotPresent() {
        final int[] actual = new int[] { -1 };
        Option.<Integer> none().forEach(i -> actual[0] = i);
        assertThat(actual[0]).isEqualTo(-1);
    }

    // -- filter

    @Test
    public void shouldReturnSomeOnFilterWhenValueIsPresentAndPredicateMatches() {
        assertThat(Option.of(1).filter(i -> i == 1)).isEqualTo(Option.of(1));
    }

    @Test
    public void shouldReturnNoneOnFilterWhenValueIsPresentAndPredicateNotMatches() {
        assertThat(Option.of(1).filter(i -> i == 2)).isEqualTo(Option.none());
    }

    @Test
    public void shouldReturnNoneOnFilterWhenValueIsNotPresentAndPredicateNotMatches() {
        assertThat(Option.<Integer> none().filter(i -> i == 1)).isEqualTo(Option.none());
    }

    // -- map

    @Test
    public void shouldMapSome() {
        assertThat(Option.of(1).map(String::valueOf)).isEqualTo(Option.of("1"));
    }

    @Test
    public void shouldMapNone() {
        assertThat(Option.<Integer> none().map(String::valueOf)).isEqualTo(Option.none());
    }

    // -- flatMap

    @Test
    public void shouldFlatMapSome() {
        assertThat(Option.of(1).flatMap(i -> Option.of(String.valueOf(i)))).isEqualTo(Option.of("1"));
    }

    @Test
    public void shouldFlatMapNone() {
        assertThat(Option.<Integer> none().flatMap(i -> Option.of(String.valueOf(i)))).isEqualTo(Option.none());
    }

    @Test
    public void shouldFlatMapNonEmptyIterable() {
        final Option<Integer> option = Option.some(2);
        assertThat(Option.of(1).<Integer> flatMap(i -> option)).isEqualTo(Option.of(2));
    }

    @Test
    public void shouldFlatMapEmptyIterable() {
        final Option<Integer> option = Option.none();
        assertThat(Option.of(1).<Integer> flatMap(i -> option)).isEqualTo(Option.none());
    }

    // -- exists

    @Test
    public void shouldBeAwareOfPropertyThatHoldsExistsOfSome() {
        assertThat(Option.some(1).exists(i -> i == 1)).isTrue();
    }

    @Test
    public void shouldBeAwareOfPropertyThatNotHoldsExistsOfSome() {
        assertThat(Option.some(1).exists(i -> i == 2)).isFalse();
    }

    @Test
    public void shouldNotHoldPropertyExistsOfNone() {
        assertThat(Option.none().exists(e -> true)).isFalse();
    }

    // -- forall

    @Test
    public void shouldBeAwareOfPropertyThatHoldsForAllOfSome() {
        assertThat(Option.some(1).forAll(i -> i == 1)).isTrue();
    }

    @Test
    public void shouldBeAwareOfPropertyThatNotHoldsForAllOfSome() {
        assertThat(Option.some(1).forAll(i -> i == 2)).isFalse();
    }

    @Test // a property holds for all elements of no elements
    public void shouldNotHoldPropertyForAllOfNone() {
        assertThat(Option.none().forAll(e -> true)).isTrue();
    }

    // -- forEach

    @Test
    public void shouldConsumePresentValueOnForEachWhenValueIsPresent() {
        final int[] actual = new int[] { -1 };
        Option.of(1).forEach(i -> actual[0] = i);
        assertThat(actual[0]).isEqualTo(1);
    }

    @Test
    public void shouldNotConsumeAnythingOnForEachWhenValueIsNotPresent() {
        final int[] actual = new int[] { -1 };
        Option.<Integer> none().forEach(i -> actual[0] = i);
        assertThat(actual[0]).isEqualTo(-1);
    }

    // -- peek

    @Test
    public void shouldConsumePresentValueOnPeekWhenValueIsPresent() {
        final int[] actual = new int[] { -1 };
        final Option<Integer> testee = Option.of(1).peek(i -> actual[0] = i);
        assertThat(actual[0]).isEqualTo(1);
        assertThat(testee).isEqualTo(Option.of(1));
    }

    @Test
    public void shouldNotConsumeAnythingOnPeekWhenValueIsNotPresent() {
        final int[] actual = new int[] { -1 };
        final Option<Integer> testee = Option.<Integer> none().peek(i -> actual[0] = i);
        assertThat(actual[0]).isEqualTo(-1);
        assertThat(testee).isEqualTo(Option.none());
    }

    // -- iterator

    @Test
    public void shouldReturnIteratorOfSome() {
        assertThat((Iterator<Integer>) Option.some(1).iterator()).isNotNull();
    }

    @Test
    public void shouldReturnIteratorOfNone() {
        assertThat((Iterator<Object>) Option.none().iterator()).isNotNull();
    }

    // -- equals

    @Test
    public void shouldEqualNoneIfObjectIsSame() {
        final Option<?> none = Option.none();
        assertThat(none).isEqualTo(none);
    }

    @Test
    public void shouldEqualSomeIfObjectIsSame() {
        final Option<?> some = Option.some(1);
        assertThat(some).isEqualTo(some);
    }

    @Test
    public void shouldNotEqualNoneIfObjectIsNull() {
        assertThat(Option.none()).isNotNull();
    }

    @Test
    public void shouldNotEqualSomeIfObjectIsNull() {
        assertThat(Option.some(1)).isNotNull();
    }

    @Test
    public void shouldNotEqualNoneIfObjectIsOfDifferentType() {
        final Object none = Option.none();
        assertThat(none.equals(new Object())).isFalse();
    }

    @Test
    public void shouldNotEqualSomeIfObjectIsOfDifferentType() {
        final Object some = Option.some(1);
        assertThat(some.equals(new Object())).isFalse();
    }

    @Test
    public void shouldEqualSome() {
        assertThat(Option.some(1)).isEqualTo(Option.some(1));
    }

    // -- hashCode

    @Test
    public void shouldHashNone() {
        assertThat(Option.none().hashCode()).isEqualTo(Objects.hash());
    }

    @Test
    public void shouldHashSome() {
        assertThat(Option.some(1).hashCode()).isEqualTo(Objects.hashCode(1));
    }

    // -- toString

    @Test
    public void shouldConvertSomeToString() {
        assertThat(Option.some(1).toString()).isEqualTo("Some(1)");
    }

    @Test
    public void shouldConvertNoneToString() {
        assertThat(Option.none().toString()).isEqualTo("None");
    }

    // -- serialization

    @Test
    public void shouldPreserveSingletonWhenDeserializingNone() {
        final Object none = Serializables.deserialize(Serializables.serialize(Option.none()));
        assertThat(none == Option.none()).isTrue();
    }
}
