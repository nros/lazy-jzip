package io.github.tsabirgaliev.zip.io;

import java.io.IOException;
import java.io.InputStream;


/**
 * a proxy input stream that wraps another stream.
 *
 * <p>
 * This proxy class can be used as a base class for descendant classes that implement additional features. Descendant
 * classes do not need to supply the stream to wrap to the base class' constructor. Instead these may supply it
 * by overriding {@link #getInputStream()}
 * </p>
 */
public class ProxyInputStream extends InputStream {

    private InputStream in = null;
    private boolean isProxyStreamInUsed = false;

    public ProxyInputStream(final InputStream inputStreamToProxy) {
        this();
        this.setInputStream(inputStreamToProxy);
    }

    protected ProxyInputStream() {
        super();
    }



    @Override
    public int read() throws IOException {
        this.isProxyStreamInUsed = true;
        return this.getInputStreamChecked().read();
    }

    @Override
    public int read(final byte[] b) throws IOException {
        this.isProxyStreamInUsed = true;
        return this.getInputStreamChecked().read(b);
    }

    @Override
    public int read(final byte[] b, final int off, final int len) throws IOException {
        this.isProxyStreamInUsed = true;
        return this.getInputStreamChecked().read(b, off, len);
    }

    @Override
    public long skip(final long n) throws IOException {
        this.isProxyStreamInUsed = true;
        return this.getInputStreamChecked().skip(n);
    }

    @Override
    public int available() throws IOException {
        this.isProxyStreamInUsed = true;
        return this.getInputStreamChecked().available();
    }

    @Override
    public void close() throws IOException {
        this.isProxyStreamInUsed = false;
        this.getInputStreamChecked().close();
    }

    @Override
    public synchronized void mark(final int readlimit) {
        this.isProxyStreamInUsed = true;
        this.getInputStreamChecked().mark(readlimit);
    }

    @Override
    public synchronized void reset() throws IOException {
        this.isProxyStreamInUsed = true;
        this.getInputStreamChecked().reset();
    }

    @Override
    public boolean markSupported() {
        this.isProxyStreamInUsed = true;
        return this.getInputStreamChecked().markSupported();
    }


    /**
     * returns the input stream that is wrapped.
     * Descendant classes may overwrite this function to supply the wrapped stream at a later state than the
     * constructor.
     *
     * @return the input stream that is being used to delegate all function calls to it.
     */
    protected InputStream getInputStream() {
        return this.in;
    }


    /**
     * sets a new stream to wrap.
     *
     * <p>
     * a new stream to wrap can only be set if no byte has been read from the previous stream or if the previous
     * stream is {@code null}. Descendant classes may use this function to store a newly created stream to wrap.
     * </p>
     *
     * @param newStreamToProxy - the new stream to wrap
     * @return this instance to allow a <a href="https://en.wikipedia.org/wiki/Fluent_interface">fluent interface</a>
     */
    protected ProxyInputStream setInputStream(final InputStream newStreamToProxy) {

        // close previous stream
        if (this.in != null && this.isProxyStreamInUsed) {
            throw new RuntimeException("can not replace previous stream as it is in use!");
        }

        this.in = newStreamToProxy;
        if (this.in == null) {
            throw new IllegalArgumentException("stream to proxy is invalid <null>");
        }

        return this;
    }


    /**
     * returns the input stream but throws an exception in case no input stream has been set yet.
     *
     * @return returns the delegate input stream but checks is validity to not be {@code null}
     * @throws NullPointerException in case no input stream has been set yet.
     */
    protected InputStream getInputStreamChecked() {
        final InputStream inStream = this.getInputStream();
        if (inStream == null) {
            throw new NullPointerException("No stream to proxy has been set yet!");
        }
        return inStream;
    }
}
