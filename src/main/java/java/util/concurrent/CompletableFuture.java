package java.util.concurrent;

import def.js.Promise;
import def.js.PromiseLike;
import jsweet.lang.Async;
import jsweet.lang.Erased;
import jsweet.util.Lang;

import java.util.function.*;

import static jsweet.util.Lang.any;
import static jsweet.util.Lang.await;

public class CompletableFuture<T> implements CompletionStage<T>, Future<T> {
    private final Promise<T> promise;
    private Consumer<T> goodStage;
    private Consumer<Object> badStage;
    private T result;
    private Throwable rejected;
    private Boolean completedGood = null;
    private int dependent = 0;

    public CompletableFuture() {
        this(new Promise<>(((Consumer<T> tConsumer, Consumer<Object> objectConsumer) -> {
        })));
    }

    public CompletableFuture(Promise<T> promise) {
        this.promise = Promise.race(Lang.<Promise.IterablePromiseLikeT<T>>any(new Promise[]{promise, new Promise<>((Consumer<T> tConsumer, Consumer<Object> objectConsumer) -> {
            goodStage = tConsumer;
            badStage = objectConsumer;
        })}));

        this.promise.then((T t) -> {
                    this.result = t;
                    completedGood = true;
                    return t;
                },
                (Object o) -> {
                    this.rejected = (Throwable) o;
                    completedGood = false;
                    return null;
                });
    }

    public static CompletableFuture<Void> allOf(CompletableFuture<?>... cfs) {
        for (CompletableFuture<?> cf : cfs) {
            cf.dependent += 1;
        }

        return new CompletableFuture<>(Promise.all(Lang.<Promise.IterablePromiseLikeT<?>>any(cfs)).then(() -> {
        }));
    }

    public static CompletableFuture<Object> anyOf(CompletableFuture<?>... cfs) {
        for (CompletableFuture<?> cf : cfs) {
            cf.dependent += 1;
        }

        return new CompletableFuture<>(Promise.race(Lang.<Promise.IterablePromiseLikeT<Object>>any(cfs)));
    }

    public static <U> CompletableFuture<U> completedFuture(U value) {
        return new CompletableFuture<>(Promise.resolve(value));
    }

    /*
    @Override
    public <U> CompletionStage<U> thenApplyAsync(Function<T, U> transform, Executor executor) {
        return null;
    }
    */

    public static CompletableFuture<Void> runAsync(Runnable runnable) {
        return new CompletableFuture<>(new Promise<>((Consumer<Void> good, Consumer<Object> bad) -> {
            runnable.run();
            good.accept(null);
        }));
    }

    public static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier) {
        return new CompletableFuture<>(new Promise<>((Consumer<U> good, Consumer<Object> bad) -> {
            good.accept(supplier.get());
        }));
    }

    /*
    @Override
    public CompletionStage<Void> thenAcceptAsync(Consumer<T> consumer, Executor executor) {
        return null;
    }
    */

    @Override
    @Async
    public Promise<T> getPromise() {
        return promise;
    }

    @Override
    public <U> CompletionStage<U> thenApply(Function<T, U> transform) {
        this.dependent += 1;
        return new CompletableFuture<>(getPromise().then(transform));
    }

    /*
    @Override
    public CompletionStage<Void> thenRunAsync(Runnable runnable, Executor executor) {
        return null;
    }
    */

    @Override
    public <U> CompletionStage<U> thenApplyAsync(Function<T, U> transform) {
        this.dependent += 1;
        return new CompletableFuture<>(getPromise().thenAsync(t -> Promise.resolve(transform.apply(t))));
    }

    @Override
    public CompletionStage<Void> thenAccept(Consumer<T> consumer) {
        this.dependent += 1;
        return new CompletableFuture<>(getPromise().then(consumer));
    }

    /*
    @Override
    public <U, V> CompletionStage<V> thenCombineAsync(CompletionStage<U> completionStage, BiFunction<T, U, V> biFunction, Executor executor) {
        return null;
    }
    */

    @Override
    public CompletionStage<Void> thenAcceptAsync(Consumer<T> consumer) {
        this.dependent += 1;
        return new CompletableFuture<>(getPromise().thenAsync(t -> {
            consumer.accept(t);
            return Promise.resolve();
        }));
    }

    @Override
    public CompletionStage<Void> thenRun(Runnable runnable) {
        this.dependent += 1;
        return new CompletableFuture<>(getPromise().then(runnable));
    }

    /*
    @Override
    public <U> CompletionStage<Void> thenAcceptBothAsync(CompletionStage<U> completionStage, BiConsumer<T, U> biConsumer, Executor executor) {
        return null;
    }
    */

    @Override
    public CompletionStage<Void> thenRunAsync(Runnable runnable) {
        this.dependent += 1;
        return new CompletableFuture<>(getPromise().thenAsync(t -> {
            runnable.run();
            return Promise.resolve();
        }));
    }

    @Override
    public <U, V> CompletionStage<V> thenCombine(CompletionStage<U> completionStage, BiFunction<T, U, V> biFunction) {
        this.dependent += 1;
        completionStage.toCompletableFuture().dependent += 1;
        return new CompletableFuture<>(Promise.all(Lang.<Promise.IterablePromiseLikeT<Object>>any(new PromiseLike[]{this.getPromise(), completionStage.getPromise()}))
                .then((Object[] arr) -> biFunction.apply(any(arr[0]), any(arr[1]))));
    }

    /*
    @Override
    public CompletionStage<Void> runAfterBothAsync(CompletionStage<?> completionStage, Runnable runnable, Executor executor) {
        return null;
    }
    */

    @Override
    public <U, V> CompletionStage<V> thenCombineAsync(CompletionStage<U> completionStage, BiFunction<T, U, V> biFunction) {
        this.dependent += 1;
        completionStage.toCompletableFuture().dependent += 1;
        return new CompletableFuture<>(Promise.all(Lang.<Promise.IterablePromiseLikeT<Object>>any(new PromiseLike[]{this.getPromise(), completionStage.getPromise()}))
                .thenAsync((Object[] arr) -> Promise.resolve(biFunction.apply(any(arr[0]), any(arr[1])))));
    }

    @Override
    public <U> CompletionStage<Void> thenAcceptBoth(CompletionStage<U> completionStage, BiConsumer<T, U> biConsumer) {
        this.dependent += 1;
        completionStage.toCompletableFuture().dependent += 1;
        return new CompletableFuture<>(Promise.all(Lang.<Promise.IterablePromiseLikeT<Object>>any(new PromiseLike[]{this.getPromise(), completionStage.getPromise()}))
                .then((Object[] arr) -> biConsumer.accept(any(arr[0]), any(arr[1]))));
    }

    /*
    @Override
    public <U> CompletionStage<U> applyToEitherAsync(CompletionStage<T> completionStage, Function<T, U> transform, Executor executor) {
        return null;
    }
    */

    @Override
    public <U> CompletionStage<Void> thenAcceptBothAsync(CompletionStage<U> completionStage, BiConsumer<T, U> biConsumer) {
        this.dependent += 1;
        completionStage.toCompletableFuture().dependent += 1;
        return new CompletableFuture<>(Promise.all(Lang.<Promise.IterablePromiseLikeT<Object>>any(new PromiseLike[]{this.getPromise(), completionStage.getPromise()}))
                .thenAsync((Object[] arr) -> {
                    biConsumer.accept(any(arr[0]), any(arr[1]));
                    return Promise.resolve();
                }));
    }

    @Override
    public CompletionStage<Void> runAfterBoth(CompletionStage<?> completionStage, Runnable runnable) {
        this.dependent += 1;
        completionStage.toCompletableFuture().dependent += 1;
        return new CompletableFuture<>(Promise.all(Lang.<Promise.IterablePromiseLikeT<Object>>any(new PromiseLike[]{this.getPromise(), completionStage.getPromise()}))
                .then(runnable));
    }

    /*
    @Override
    public CompletionStage<Void> acceptEitherAsync(CompletionStage<T> completionStage, Consumer<T> consumer, Executor executor) {
        return null;
    }
    */

    @Override
    public CompletionStage<Void> runAfterBothAsync(CompletionStage<?> completionStage, Runnable runnable) {
        this.dependent += 1;
        completionStage.toCompletableFuture().dependent += 1;
        return new CompletableFuture<>(Promise.all(Lang.<Promise.IterablePromiseLikeT<Object>>any(new PromiseLike[]{this.getPromise(), completionStage.getPromise()}))
                .thenAsync(arr -> {
                    runnable.run();
                    return Promise.resolve();
                }));
    }

    @Override
    public <U> CompletionStage<U> applyToEither(CompletionStage<T> completionStage, Function<T, U> transform) {
        this.dependent += 1;
        completionStage.toCompletableFuture().dependent += 1;
        return new CompletableFuture<>(Promise.<T>race(Lang.<Promise.IterablePromiseLikeT<T>>any(new PromiseLike[]{this.getPromise(), completionStage.getPromise()}))
                .then(transform));
    }

    /*
    @Override
    public CompletionStage<Void> runAfterEitherAsync(CompletionStage<?> completionStage, Runnable runnable, Executor executor) {
        return null;
    }
    */

    @Override
    public <U> CompletionStage<U> applyToEitherAsync(CompletionStage<T> completionStage, Function<T, U> transform) {
        this.dependent += 1;
        completionStage.toCompletableFuture().dependent += 1;
        return new CompletableFuture<>(Promise.<T>race(Lang.<Promise.IterablePromiseLikeT<T>>any(new PromiseLike[]{this.getPromise(), completionStage.getPromise()}))
                .thenAsync((T t) -> Promise.resolve(transform.apply(t))));
    }

    @Override
    public CompletionStage<Void> acceptEither(CompletionStage<T> completionStage, Consumer<T> consumer) {
        this.dependent += 1;
        completionStage.toCompletableFuture().dependent += 1;
        return new CompletableFuture<>(Promise.<T>race(Lang.<Promise.IterablePromiseLikeT<T>>any(new PromiseLike[]{this.getPromise(), completionStage.getPromise()}))
                .then(consumer));
    }

    /*
    @Override
    public <U> CompletionStage<U> thenComposeAsync(Function<T, CompletionStage<U>> function, Executor executor) {
        return null;
    }
    */

    @Override
    public CompletionStage<Void> acceptEitherAsync(CompletionStage<T> completionStage, Consumer<T> consumer) {
        this.dependent += 1;
        completionStage.toCompletableFuture().dependent += 1;
        return new CompletableFuture<Void>(Promise.<T>race(Lang.<Promise.IterablePromiseLikeT<T>>any(new PromiseLike[]{this.getPromise(), completionStage.getPromise()}))
                .thenAsync((T t) -> {
                    consumer.accept(t);
                    return Promise.resolve();
                }));
    }

    @Override
    public CompletionStage<Void> runAfterEither(CompletionStage<?> completionStage, Runnable runnable) {
        this.dependent += 1;
        completionStage.toCompletableFuture().dependent += 1;
        return new CompletableFuture<>(Promise.race(Lang.<Promise.IterablePromiseLikeT<Object>>any(new PromiseLike[]{this.getPromise(), completionStage.getPromise()}))
                .then(runnable));
    }

    @Override
    public CompletionStage<Void> runAfterEitherAsync(CompletionStage<?> completionStage, Runnable runnable) {
        this.dependent += 1;
        completionStage.toCompletableFuture().dependent += 1;
        return new CompletableFuture<>(Promise.race(Lang.<Promise.IterablePromiseLikeT<Object>>any(new PromiseLike[]{this.getPromise(), completionStage.getPromise()}))
                .thenAsync(t -> {
                    runnable.run();
                    return Promise.resolve();
                }));
    }

    /*
    @Override
    public CompletionStage<T> whenCompleteAsync(BiConsumer<T, Throwable> biConsumer, Executor executor) {
        return null;
    }
    */

    @Override
    public <U> CompletionStage<U> thenCompose(Function<T, CompletionStage<U>> function) {
        return new CompletableFuture<U>(getPromise().thenAsync((T t) -> function.apply(t).getPromise()));
    }

    @Override
    public <U> CompletionStage<U> thenComposeAsync(Function<T, CompletionStage<U>> function) {
        return new CompletableFuture<U>(getPromise().thenAsync((T t) -> function.apply(t).getPromise()));
    }

    /*
    @Override
    public <U> CompletionStage<U> handleAsync(BiFunction<T, Throwable, U> biFunction, Executor executor) {
        return null;
    }
    */

    @Override
    public CompletionStage<T> exceptionally(Function<Throwable, T> transform) {
        this.dependent += 1;
        return new CompletableFuture<>(getPromise().Catch((Object t) -> transform.apply((Throwable) t)));
    }

    @Override
    public CompletionStage<T> whenComplete(BiConsumer<T, Throwable> biConsumer) {
        this.dependent += 1;
        return new CompletableFuture<>(getPromise().then(t -> {
            biConsumer.accept(t, null);
            return t;
        }, (Object e) -> biConsumer.accept(null, (Throwable) e)));
    }

    @Override
    public CompletionStage<T> whenCompleteAsync(BiConsumer<T, Throwable> biConsumer) {
        this.dependent += 1;
        return new CompletableFuture<>(getPromise().thenAsync(t -> {
            biConsumer.accept(t, null);
            return Promise.resolve(t);
        }, (Object e) -> biConsumer.accept(null, (Throwable) e)));
    }

    @Override
    public <U> CompletionStage<U> handle(BiFunction<T, Throwable, U> biFunction) {
        this.dependent += 1;
        return new CompletableFuture<>(getPromise().then(t -> biFunction.apply(t, null), (Object e) -> biFunction.apply(null, (Throwable) e)));
    }

    @Override
    public <U> CompletionStage<U> handleAsync(BiFunction<T, Throwable, U> biFunction) {
        this.dependent += 1;
        return new CompletableFuture<>(getPromise().thenAsync(t -> Promise.resolve(biFunction.apply(t, null)), (Object e) -> biFunction.apply(null, (Throwable) e)));
    }

    @Override
    public CompletableFuture<T> toCompletableFuture() {
        return this;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        if (completedGood != null)
            return false;

        badStage.accept(new CancellationException("" + mayInterruptIfRunning));
        return true;
    }

    public boolean complete(T value) {
        if (completedGood != null)
            return false;

        goodStage.accept(value);
        return true;
    }

    public boolean completeExceptionally(Throwable ex) {
        if (completedGood != null)
            return false;

        badStage.accept(ex);
        return true;
    }

    public T getNow(T valueIfAbsent) {
        return completedGood != null && completedGood ? result : valueIfAbsent;
    }

    public int getNumberOfDependents() {
        return dependent;
    }

    @Override
    public boolean isCancelled() {
        return rejected instanceof CancellationException;
    }

    public boolean isCompletedExceptionally() {
        return completedGood != null && !completedGood;
    }

    @Override
    public boolean isDone() {
        return completedGood != null;
    }

    public void obtrudeException(Throwable ex) {
        if (!isDone()) {
            completeExceptionally(ex);
            return;
        }

        completedGood = false;
        result = null;
        rejected = ex;
    }

    /*
    public static CompletableFuture<Void> runAsync(Runnable runnable, Executor executor) {
        return null;
    }
    */

    public void obtrudeValue(T value) {
        if (!isDone()) {
            complete(value);
            return;
        }

        completedGood = true;
        result = value;
        rejected = null;
    }

    /*
    public static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier, Executor executor) {
        return null;
    }
    */

    // @Inlined
    // @Async
    @Erased
    public T join() {
        if (isDone()) {
            if (completedGood) {
                return result;
            } else {
                throw new CompletionException(rejected);
            }
        }
        return await(getPromise());
    }
}