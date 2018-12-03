package io.github.tsabirgaliev.zip;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class DataDescriptor {
    long signature = 0x08074b50;
    long crc32_checksum;
    long compressed_size;
    long uncompressed_size;

    public DataDescriptor(final long crc32_checksum, final long compressed_size, final long uncompressed_size) {
        this.crc32_checksum = crc32_checksum;
        this.compressed_size = compressed_size;
        this.uncompressed_size = uncompressed_size;
    }

    public byte[] getBytes() throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        baos.write(CentralDirectoryUtilities.bytes4(this.signature));
        baos.write(CentralDirectoryUtilities.bytes4(this.crc32_checksum));
        baos.write(CentralDirectoryUtilities.bytes4(this.compressed_size));
        baos.write(CentralDirectoryUtilities.bytes4(this.uncompressed_size));

        baos.close();

        return baos.toByteArray();
    }
}
