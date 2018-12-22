package io.github.tsabirgaliev.zip.io;

import java.io.InputStream;
import java.util.function.Supplier;


/**
 * a proxy input stream that fetches the stream to wrap just when first byte is read.
 *
 * <p>
 * a {@link java.util.function.Supplier} is required to supply this proxy with the input stream to wrap on demand.
 * The supplier is asked for the stream just when the first byte is needed.
 * </p>
 *
 * <p>
 * fetching the input stream from a supplier only when the first byte is read helps keeping the amount of open
 * files as low as possible. This will help saving resources.
 * </p>
 */
public class LazyInitInputStream<T extends InputStream> extends ProxyInputStreamWithCloseListener<T> {

    private final Supplier<InputStream> supplierOfIn;

    public LazyInitInputStream(final Supplier<InputStream> supplierOfInput) {
        super();
        this.supplierOfIn = supplierOfInput;
    }


    @SuppressWarnings("resource")
    @Override
    protected InputStream getInputStream() {
        InputStream proxiedInputStream = super.getInputStream();
        if (proxiedInputStream == null) {
            proxiedInputStream = this.supplierOfIn.get();
            this.setInputStream(proxiedInputStream);
        }
        return proxiedInputStream;
    }
}
