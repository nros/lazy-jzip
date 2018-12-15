package io.github.tsabirgaliev.zip;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/***
 * creates the bytes of the data descriptor packet, which is used to hold some size information about an entry.
 *
 * The "data descriptor" is an optional, additional structure, added to the ZIP file after the compressed data
 * of the entry. It contains information about the sizes and CRC32 of the compressed data, which have not been
 * calculated prior to adding the compressed data to the ZIP file.
 *
 * This is very handy, as this package handles arbitrary {@link java.io.InputStream}, where it is impossible
 * know the sizes in advance. Although this packet is optional, it is used extensively with this ZIP packager.
 * All this to read the bytes of the compressed entry and add the sizes afterwards.
 *
 * @author Tair Sabirgaliev <tair.sabirgaliev@gmail.com>
 * @author nros <508093+nros@users.noreply.github.com>
 */
public class DataDescriptor extends BaseZipPacketBuilder {

    private static final long PACKET_SIGNATURE = 0x08074b50;

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

        baos.write(this.convertLongToUInt32(DataDescriptor.PACKET_SIGNATURE));
        baos.write(this.convertLongToUInt32(this.crc32_checksum));
        baos.write(this.convertLongToUInt32(this.compressed_size));
        baos.write(this.convertLongToUInt32(this.uncompressed_size));

        baos.close();

        return baos.toByteArray();
    }
}
