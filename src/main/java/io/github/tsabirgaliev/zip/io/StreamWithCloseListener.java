package io.github.tsabirgaliev.zip.io;

import java.io.Closeable;
import java.util.function.Consumer;

/***
 * implementing streams accept a listener to the closing action.
 *
 * The listeners are called after the stream has been closed.
 *
 * @author nros <508093+nros@users.noreply.github.com>
 */
public interface StreamWithCloseListener<T extends Closeable> extends Closeable {

    public StreamWithCloseListener<T> addCloseListener(final Consumer<T> closeListener);

}
