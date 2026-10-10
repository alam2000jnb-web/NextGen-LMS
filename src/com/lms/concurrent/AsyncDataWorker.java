package com.lms.concurrent;

import javax.swing.SwingWorker;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

/**
 * Generic SwingWorker wrapper for executing asynchronous operations in Swing GUI
 * without freezing the Event Dispatch Thread (EDT).
 *
 * @param <T> Result type
 */
public class AsyncDataWorker<T> extends SwingWorker<T, Void> {

    private final Callable<T> task;
    private final Consumer<T> onSuccess;
    private final Consumer<Exception> onFailure;

    public AsyncDataWorker(Callable<T> task, Consumer<T> onSuccess, Consumer<Exception> onFailure) {
        this.task = task;
        this.onSuccess = onSuccess;
        this.onFailure = onFailure;
    }

    @Override
    protected T doInBackground() throws Exception {
        return task.call();
    }

    @Override
    protected void done() {
        try {
            T result = get();
            if (onSuccess != null) {
                onSuccess.accept(result);
            }
        } catch (Exception e) {
            if (onFailure != null) {
                onFailure.accept(e);
            } else {
                e.printStackTrace();
            }
        }
    }

    public static <T> void execute(Callable<T> task, Consumer<T> onSuccess, Consumer<Exception> onFailure) {
        new AsyncDataWorker<>(task, onSuccess, onFailure).execute();
    }
}
