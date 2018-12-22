package io.github.tsabirgaliev.zip;

import java.util.zip.CRC32;
import java.util.zip.Checksum;

/**
 * implements the CRC32 checksum utilizing {@link java.util.zip.CRC32} internally but keeping track on counted bytes.
 *
 * <p>
 * All bytes added to the calculation of the checksum are counted. In the end, this counter can be retrieved.
 * It helps to know, how many bytes were used to calculate the CRC32. This is most useful to drive the
 * central directory entries of a file within the ZIP archive.
 * </p>
 */
public class ByteCountingCRC32 implements Checksum {
    private final CRC32 checksum = new CRC32();
    private long counter = 0;

    @Override
    public void update(final int b) {
        this.checksum.update(b);
        this.counter++;
    }

    @Override
    public void update(final byte[] bytes, final int offset, final int length) {

        if (
            bytes != null && bytes.length > 0 && length > 0
            && offset >= 0 && offset < bytes.length
        ) {
            this.checksum.update(bytes, offset, Math.min(length, bytes.length - offset));

            // the byte array may be shorter than the specified length to use
            this.counter += Math.min(length, bytes.length - offset);
        }
    }

    @Override
    public long getValue() {
        return this.checksum.getValue();
    }

    @Override
    public void reset() {
        this.checksum.reset();
        this.counter = 0L;
    }

    /**
     * return the amount of bytes that were used to calculate the checksum.
     * The counter is being reset to {@code 0}, whenever {@link #reset()} is called.
     *
     * @return the amount of bytes that have been read up to now. The value is never lower than {@code 0}.
     */
    public long getByteCounter() {
        return this.counter;
    }
}
