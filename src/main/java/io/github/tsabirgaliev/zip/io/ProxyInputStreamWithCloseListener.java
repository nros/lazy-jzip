package io.github.tsabirgaliev.zip.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * a proxy input stream to create an event on closing the stream and call some handlers, waiting for notification.
 *
 * @param <T> the type of {@link java.io.InputStream} that is used as a delegate. Defining the type helps to call
 *     the close listener correctly.
 */
public class ProxyInputStreamWithCloseListener<T extends InputStream> extends ProxyInputStream
    implements StreamWithCloseListener<T> {

    private final List<Consumer<T>> closeListeners = new ArrayList<>();


    /**
     * creates a new prox input stream, using the provided input stream as a delegate.
     *
     * @param inputStreamToProxy - the delegate to call when reading bytes or closing.
     * @throws IllegalArgumentException - if the provided delegate parameter is {@code null}
     */
    public ProxyInputStreamWithCloseListener(final T inputStreamToProxy) {
        super(inputStreamToProxy);
    }


    /**
     * creates a new proxy without any delegate.
     *
     * <p>
     * The proxy is invalid as long as there as not been set a delegate with
     * {@link io.github.tsabirgaliev.zip.io.ProxyInputStream#setInputStream(InputStream)}. Hence only subclasses
     * are allowed to use this kind of constructor.
     * </p>
     */
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
            // CHECKSTYLE:OFF
            try {
                closeListener.accept(wrappedStream);
            } catch (final Exception ignore) {
            }
            // CHECKSTYLE:ON
        }
    }
}
