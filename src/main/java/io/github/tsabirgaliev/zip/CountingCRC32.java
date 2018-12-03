package io.github.tsabirgaliev.zip;

import java.util.zip.CRC32;
import java.util.zip.Checksum;

public class CountingCRC32 implements Checksum {
    CRC32 checksum = new CRC32();
    long counter = 0;

    @Override
    public void update(final int b) {
        this.checksum.update(b);
        this.counter++;
    }

    @Override
    public void update(final byte[] b, final int off, final int len) {
        this.checksum.update(b, off, len);
        this.counter += len;
    }

    @Override
    public long getValue() {
        return this.checksum.getValue();
    }

    @Override
    public void reset() {
        this.checksum.reset();
    }

    public long getCounter() {
        return this.counter;
    }
}
