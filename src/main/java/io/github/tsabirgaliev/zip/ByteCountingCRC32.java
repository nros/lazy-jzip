package io.github.tsabirgaliev.zip;

import java.util.zip.CRC32;
import java.util.zip.Checksum;

/***
 * implements the CRC32 checksum utilizing {@link java.util.zip.CRC32} internally but keeping track on counted bytes.
 *
 * All bytes added to the calculation of the checksum are counted. In the end, this counter can be retrieved.
 * It helps to know, how many bytes were used to calculate the CRC32. This is most useful to drive the
 * cebtral diretory entries of a file within the ZIP archive.
 *
 * @author Tair Sabirgaliev <tair.sabirgaliev@gmail.com>
 * @author nros <508093+nros@users.noreply.github.com>
 */
public class ByteCountingCRC32 implements Checksum {
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
        this.counter = 0L;
    }

    /***
     * return the amount of bytes that were used to calculate the checksum.
     * The counter is being reset to {@code 0}, whenever {@link #reset()} is called.
     */
    public long getByteCounter() {
        return this.counter;
    }
}
