package io.github.tsabirgaliev.zip.io;

import java.io.InputStream;
import java.util.function.Supplier;

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
