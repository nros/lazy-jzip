package io.github.tsabirgaliev.zip.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.CheckedInputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterInputStream;

import io.github.tsabirgaliev.zip.ByteCountingCRC32;
import io.github.tsabirgaliev.zip.packets.DataDescriptorBuilder;


/**
 * counts the amount of bytes and checksum while reading from the {@code InputStream}.
 *
 * <p>
 * After consuming all the bytes from the {@link java.io.InputStream}, the optional {@link DataDescriptorBuilder}
 * is created to be added to the ZIP stream after the compressed entry.
 * </p>
 */
public class DeflaterCheckedInputStream extends FilterInputStream {

    private long compressedSize;
    private final ByteCountingCRC32 checksum;

    /**
     * wraps the raw data stream with a deflater stream, recording the sizes and CRC of the stream while reading.
     *
     * @param rawDataStream - the stream to wrap with a compressing filter
     * @param useCompression - if set to {@code true}, then compression is used, and none otherwise
     */
    public DeflaterCheckedInputStream(final InputStream rawDataStream, final boolean useCompression) {
        super(null);
        this.checksum = new ByteCountingCRC32();
        this.compressedSize = 0;

        final CheckedInputStream checkedIn = new CheckedInputStream(rawDataStream, this.checksum);

        if (useCompression) {
            final DeflaterInputStream deflateIn = new DeflaterInputStream(checkedIn, new Deflater(Deflater.DEFAULT_COMPRESSION, true));
            this.in = deflateIn;

        } else {
            // do not compress
            this.in = checkedIn;
        }
    }

    @Override
    public int read() throws IOException {
        final int b = super.read();
        if (b != -1) {
            this.compressedSize++;
        }

        return b;
    }

    @Override
    public int read(final byte[] b) throws IOException {
        final int c = super.read(b);
        if (c != -1) {
            this.compressedSize += c;
        }

        return c;
    }

    @Override
    public int read(final byte[] b, final int off, final int len) throws IOException {
        final int c = super.read(b, off, len);
        if (c != -1) {
            this.compressedSize += c;
        }

        return c;
    }


    public long getCrc() {
        return this.checksum.getValue();
    }

    public long getCompressedSize() {
        return this.compressedSize;
    }

    public long getSize() {
        return this.checksum.getByteCounter();
    }
}
