package io.github.tsabirgaliev.zip;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.CheckedInputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterInputStream;


/***
 * counts the amount of bytes and checksum while reading from the {@code InputStream}.
 *
 * After consuming all the bytes from the {@link java.io.InputStream}, the optional {@link DataDescriptor}
 * is created to be added to the ZIP stream after the compressed entry.
 *
 * @author Tair Sabirgaliev <tair.sabirgaliev@gmail.com>
 * @author nros <508093+nros@users.noreply.github.com>
 */
public class DeflaterCheckedInputStream extends FilterInputStream {
    long compressedSize = 0;
    ByteCountingCRC32 checksum = new ByteCountingCRC32();

    DeflaterCheckedInputStream(final InputStream in) {
        super(null);
        final CheckedInputStream checkedIn = new CheckedInputStream(in, this.checksum);
        final DeflaterInputStream deflateIn = new DeflaterInputStream(checkedIn, new Deflater(Deflater.DEFAULT_COMPRESSION, true));
        this.in = deflateIn;
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

    public DataDescriptor getDataDescriptor() {
        return new DataDescriptor(this.checksum.getValue(), this.compressedSize, this.checksum.getByteCounter());
    }
}
