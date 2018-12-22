package io.github.tsabirgaliev.zip.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * a proxy input stream to create an event on closing the stream and call some handlers, waiting for notification.
 */
public class ProxyInputStreamWithCloseListener<T extends InputStream> extends ProxyInputStream
    implements StreamWithCloseListener<T>
{

    private final List<Consumer<T>> closeListeners = new ArrayList<>();


    public ProxyInputStreamWithCloseListener(final T inputStreamToProxy) {
        super(inputStreamToProxy);
    }


    protected ProxyInputStreamWithCloseListener() {
        super();
    }


    @Override
    public ProxyInputStreamWithCloseListener<T> addCloseListener(final Consumer<T> closeListener) {
        this.closeListeners.add(closeListener);
        return this;
    }


    @Override
    public void close() throws IOException {
        @SuppressWarnings({ "unchecked", "resource" })
        final T wrappedStream = (T)this.getInputStream();
        super.close();

        for (final Consumer<T> closeListener : this.closeListeners) {
            try {
                closeListener.accept(wrappedStream);
            } catch (final Exception ignore) {
            }
        }
    }
}
