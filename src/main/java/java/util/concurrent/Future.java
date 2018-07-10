package java.util.concurrent;

import def.js.Array;
import def.js.Promise;
import def.js.PromiseLike;
import jsweet.lang.Async;
import jsweet.lang.Erased;
import jsweet.util.Lang;

import java.util.function.Consumer;

import static def.dom.Globals.setTimeout;
import static jsweet.util.Lang.await;
import static jsweet.util.Lang.object;
import static jsweet.util.Lang.typeof;

public interface Future<V> {
    // @Inline
    // @Async
    @Erased
    default V get() {
        return await(getPromise());
    }

    // @Inline
    // @Async
    @Erased
    default V get(long timeout, TimeUnit timeunit) {
        return await(Promise.race(Lang.<Promise.IterablePromiseLikeT<V>> any(new PromiseLike[]{ delay(timeout, timeunit), getPromise() })));
    }

    boolean cancel(boolean mayInterruptIfRunning);

    boolean isCancelled();

    boolean isDone();

    Promise<V> getPromise();

    @Async
    static <T> Promise<T> delay(long timeout, TimeUnit timeunit) {
        return new Promise<>((Consumer<T> result, Consumer<Object> error) -> setTimeout((Runnable)() -> result.accept(null), timeunit == null || !typeof(timeunit).equals("object") || object(timeunit).$get("toMillis") == null ? timeout : timeunit.toMillis(timeout)));
    }
}
