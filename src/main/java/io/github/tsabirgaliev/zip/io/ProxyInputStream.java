package io.github.tsabirgaliev.zip.io;

import java.io.IOException;
import java.io.InputStream;


/**
 * a proxy input stream that wraps another stream.
 *
 * <p>
 * This proxy class can be used as a base class for descendant classes that implement additional features. Descendant
 * classes do not need to supply the stream to wrap to the base class' constructor. Instead these may supply it later
 * by overriding {@link #getInputStream()} or setting a delegate at some point in time with
 * {@link #setInputStream(InputStream)}. Nevertheless this must be done before the first byte is read from this input
 * stream. Otherwise a {@link java.lang.NullPointerException} is thrown upon reading a byte.
 * </p>
 *
 * <p>
 * All functions reading a byte, are first requesting the delegate input stream by calling
 * {@link #getInputStreamChecked()}. This function will check for the existence of the delegate and issue a
 * {@link java.lang.NullPointerException} in case no delegate has been set yet.
 * </p>
 */
public class ProxyInputStream extends InputStream {

    private InputStream in = null;
    private boolean isProxyStreamInUsed = false;

    /**
     * creates a new prox input stream, using the provided input stream as a delegate.
     *
     * @param inputStreamToProxy - the delegate to call when reading bytes or closing.
     * @throws IllegalArgumentException - if the provided delegate parameter is {@code null}
     */
    public ProxyInputStream(final InputStream inputStreamToProxy) {
        this();
        if (inputStreamToProxy == null) {
            throw new IllegalArgumentException("the provided delegate is invalid (<null>)");
        }
        this.setInputStream(inputStreamToProxy);
    }


    /**
     * creates a new proxy without any delegate.
     *
     * <p>
     * The proxy is invalid as long as there as not been set a delegate with
     * {@link #setInputStream(InputStream)}. Hence only subclasses are allowed to use this kind of constructor
     * and must the delegate at some point in time, or at least overwrite {@link #getInputStream()}
     * </p>
     */
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
