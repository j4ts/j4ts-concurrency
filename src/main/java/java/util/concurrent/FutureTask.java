package java.util.concurrent;

import def.js.Promise;

public class FutureTask<V> implements RunnableFuture<V> {
    private final Callable<V> callable;
    private final Runnable runnable;
    private final V result;
    private CompletableFuture<V> completableFuture = new CompletableFuture<>();

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
        if (isDone())
            return;
        try {
            if (callable != null) {
                set(callable.call());
            } else {
                if (runnable != null) {
                    runnable.run();
                }
                set(result);
            }
        } catch (Exception e) {
            setException(e);
        }
    }

    @Override
    public boolean cancel(boolean b) {
        if (isDone())
            return false;

        setException(new CancellationException());
        return true;
    }

    @Override
    public boolean isCancelled() {
        return completableFuture.isCancelled();
    }

    @Override
    public boolean isDone() {
        return completableFuture.isDone();
    }

    protected void done() {
    }

    protected void set(V v) {
        if (isDone())
            return;

        completableFuture.complete(v);
        done();
    }

    protected void setException(Throwable t) {
        if (isDone())
            return;

        completableFuture.completeExceptionally(t);
        done();
    }

    public Promise<V> getPromise() {
        return completableFuture.getPromise();
    }

    protected boolean runAndReset() {
        run();
        if (completableFuture.isCompletedExceptionally())
            return false;

        completableFuture = new CompletableFuture<>();
        return true;
    }
}
