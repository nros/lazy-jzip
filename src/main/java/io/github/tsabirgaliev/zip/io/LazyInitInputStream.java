package io.github.tsabirgaliev.zip.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Supplier;

public class LazyInitInputStream extends InputStream {

    private InputStream in = null;
    private final Supplier<InputStream> supplierOfIn;

    public LazyInitInputStream(final Supplier<InputStream> supplierOfInput) {
        super();
        this.supplierOfIn = supplierOfInput;
    }


    @Override
    public int read() throws IOException {
        return this.getInputStream().read();
    }

    @Override
    public int read(final byte[] b) throws IOException {
        return this.getInputStream().read(b);
    }

    @Override
    public int read(final byte[] b, final int off, final int len) throws IOException {
        return this.getInputStream().read(b, off, len);
    }

    @Override
    public long skip(final long n) throws IOException {
        return this.getInputStream().skip(n);
    }

    @Override
    public int available() throws IOException {
        return this.getInputStream().available();
    }

    @Override
    public void close() throws IOException {
        this.getInputStream().close();
    }

    @Override
    public synchronized void mark(final int readlimit) {
        this.getInputStream().mark(readlimit);
    }

    @Override
    public synchronized void reset() throws IOException {
        this.getInputStream().reset();
    }

    @Override
    public boolean markSupported() {
        return this.getInputStream().markSupported();
    }


    private InputStream getInputStream() {
        if (this.in == null) {
            this.in = this.supplierOfIn.get();
        }
        return this.in;
    }
}
