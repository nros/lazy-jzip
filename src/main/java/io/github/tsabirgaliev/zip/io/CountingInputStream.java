package io.github.tsabirgaliev.zip.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * wraps another input stream and counts all bytes that are read or skipped from the wrapped stream.
 */
public class CountingInputStream extends ProxyInputStream {

    private long bytesReadCounter = 0;

    /**
     * creates a new wrapper around a delegate in order to count all bytes read from the delegate.
     *
     * @param inputStreamToProxy - the delegate to wrap.
     */
    public CountingInputStream(final InputStream inputStreamToProxy) {
        super(inputStreamToProxy);
    }


    @Override
    public int read() throws IOException {
        final int chr = super.read();
        if (chr >= 0) {
            this.bytesReadCounter++;
        }
        return chr;
    }

    @Override
    public int read(final byte[] b) throws IOException {
        final int bytesRead = this.getInputStreamChecked().read(b);
        this.bytesReadCounter += bytesRead;
        return bytesRead;
    }

    @Override
    public int read(final byte[] b, final int off, final int len) throws IOException {
        final int bytesRead = this.getInputStreamChecked().read(b, off, len);
        this.bytesReadCounter += bytesRead;
        return bytesRead;
    }

    @Override
    public long skip(final long n) throws IOException {
        final long bytesRead = super.skip(n);
        this.bytesReadCounter += bytesRead;
        return bytesRead;
    }

    /**
     * returns the amount of bytes that have been read from the delegate so far.
     *
     * @return return the bytes that have been read so far, can not be below {@code 0}
     */
    public long getByteCount() {
        return this.bytesReadCounter;
    }
}
