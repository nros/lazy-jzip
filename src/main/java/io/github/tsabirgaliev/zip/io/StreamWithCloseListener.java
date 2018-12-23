package io.github.tsabirgaliev.zip.io;

import java.io.Closeable;
import java.util.function.Consumer;

/**
 * implementing streams accept a listener to the closing action.
 * The listeners are called after the stream has been closed.
 *
 * @param <T> the type of {@link java.io.Closeable} that the close listener will expect to receive.
 */
public interface StreamWithCloseListener<T extends Closeable> extends Closeable {

    /**
     * adds a new listener for the close action/event that will be notified of closing the stream.
     *
     * @param closeListener - the listener that is being called upon closing the stream.
     *     Although all listeners are encapsulated within a try/catch block, all exceptions should be handled by the
     *     listener itself, as these are just silently ignored.
     * @return this instance to allow a <a href="https://en.wikipedia.org/wiki/Fluent_interface">fluent interface</a>
     */
    public StreamWithCloseListener<T> addCloseListener(final Consumer<T> closeListener);

}
