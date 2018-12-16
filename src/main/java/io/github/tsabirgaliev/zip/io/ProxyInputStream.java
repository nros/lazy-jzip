package io.github.tsabirgaliev.zip.io;

import java.io.IOException;
import java.io.InputStream;

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


    protected InputStream getInputStream() {
        return this.in;
    }


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


    /***
     * returns the input stream but throws an exception in case no input stream has been set yet.
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
