package java.util.concurrent;

import def.js.Promise;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public interface CompletionStage<T> {
    <U> CompletionStage<U> thenApply(Function<T, U> transform);

    <U> CompletionStage<U> thenApplyAsync(Function<T, U> transform);

//     <U> CompletionStage<U> thenApplyAsync(Function<T, U> transform, Executor executor);

    CompletionStage<Void> thenAccept(Consumer<T> consume);

    CompletionStage<Void> thenAcceptAsync(Consumer<T> consume);

//     CompletionStage<Void> thenAcceptAsync(Consumer<T> consume, Executor executor);

    CompletionStage<Void> thenRun(Runnable action);

    CompletionStage<Void> thenRunAsync(Runnable action);

//     CompletionStage<Void> thenRunAsync(Runnable action, Executor executor);

    <U, V> CompletionStage<V> thenCombine(CompletionStage<U> other, BiFunction<T, U, V> transform);

    <U, V> CompletionStage<V> thenCombineAsync(CompletionStage<U> other, BiFunction<T, U, V> transform);

//     <U, V> CompletionStage<V> thenCombineAsync(CompletionStage<U> other, BiFunction<T, U, V> transform, Executor executor);

    <U> CompletionStage<Void> thenAcceptBoth(CompletionStage<U> other, BiConsumer<T, U> consume);

    <U> CompletionStage<Void> thenAcceptBothAsync(CompletionStage<U> other, BiConsumer<T, U> consume);

//     <U> CompletionStage<Void> thenAcceptBothAsync(CompletionStage<U> other, BiConsumer<T, U> consume, Executor executor);

    CompletionStage<Void> runAfterBoth(CompletionStage<?> other, Runnable action);

    CompletionStage<Void> runAfterBothAsync(CompletionStage<?> other, Runnable action);

//     CompletionStage<Void> runAfterBothAsync(CompletionStage<?> other, Runnable action, Executor executor);

    <U> CompletionStage<U> applyToEither(CompletionStage<T> other, Function<T, U> transform);

    <U> CompletionStage<U> applyToEitherAsync(CompletionStage<T> other, Function<T, U> transform);

//     <U> CompletionStage<U> applyToEitherAsync(CompletionStage<T> other, Function<T, U> transform, Executor executor);

    CompletionStage<Void> acceptEither(CompletionStage<T> other, Consumer<T> consume);

    CompletionStage<Void> acceptEitherAsync(CompletionStage<T> other, Consumer<T> consume);

//     CompletionStage<Void> acceptEitherAsync(CompletionStage<T> other, Consumer<T> consume, Executor executor);

    CompletionStage<Void> runAfterEither(CompletionStage<?> other, Runnable action);

    CompletionStage<Void> runAfterEitherAsync(CompletionStage<?> other, Runnable action);

//     CompletionStage<Void> runAfterEitherAsync(CompletionStage<?> other, Runnable action, Executor executor);

    <U> CompletionStage<U> thenCompose(Function<T, CompletionStage<U>> var1);

    <U> CompletionStage<U> thenComposeAsync(Function<T, CompletionStage<U>> var1);

//    <U> CompletionStage<U> thenComposeAsync(Function<? super T, ? extends CompletionStage<U>> var1, Executor var2);

    CompletionStage<T> exceptionally(Function<Throwable, T> atException);

    CompletionStage<T> whenComplete(BiConsumer<T, Throwable> finallyConsume);

    CompletionStage<T> whenCompleteAsync(BiConsumer<T, Throwable> finallyConsume);

//     CompletionStage<T> whenCompleteAsync(BiConsumer<T, Throwable> finallyConsume, Executor executor);

    <U> CompletionStage<U> handle(BiFunction<T, Throwable, U> finallyMake);

    <U> CompletionStage<U> handleAsync(BiFunction<T, Throwable, U> finallyMake);

//     <U> CompletionStage<U> handleAsync(BiFunction<T, Throwable, U> finallyMake, Executor executor);

    CompletableFuture<T> toCompletableFuture();
}
