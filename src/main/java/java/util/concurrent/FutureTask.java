package java.util.concurrent;

import def.js.Promise;

public class FutureTask<V> implements RunnableFuture<V> {
    private final CompletableFuture<V> completableFuture = new CompletableFuture<>();

    private final Callable<V> callable;
    private final Runnable runnable;
    private final V result;

    public FutureTask(Callable<V> callable) {
        this.callable = callable;
        this.runnable = null;
        this.result = null;
    }

    public FutureTask(Runnable runnable, V result) {
        this.callable = null;
        this.runnable = runnable;
        this.result = result;
    }

    @Override
    public void run() {
        try {
            if (callable != null) {
                completableFuture.complete(callable.call());
            } else {
                if (runnable != null) {
                    runnable.run();
                }
                completableFuture.complete(result);
            }
        } catch (Exception e) {
            completableFuture.completeExceptionally(e);
        }
    }

    @Override
    public boolean cancel(boolean b) {
        return completableFuture.cancel(b);
    }

    @Override
    public boolean isCancelled() {
        return completableFuture.isCancelled();
    }

    @Override
    public boolean isDone() {
        return completableFuture.isDone();
    }

    @Override
    public Promise<V> getPromise() {
        return completableFuture.getPromise();
    }


}
