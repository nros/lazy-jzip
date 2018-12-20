package io.github.tsabirgaliev.zip.packets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import io.github.tsabirgaliev.zip.ZipEntryDataWithCachedPackets;
import io.github.tsabirgaliev.zip.ZipEntry;

/***
 * creates the bytes of the local file header packet, which prepends the compressed data of the entry.
 *
 * @author Tair Sabirgaliev <tair.sabirgaliev@gmail.com>
 * @author nros <508093+nros@users.noreply.github.com>
 */
public class LocalFileHeaderBuilder extends BaseZipPacketBuilder implements ZipEntryPacketBuilder {

    public final static int     COMPRESSION_METHOD_STORED = 0;  // no compression
    public final static int     COMPRESSION_METHOD_DEFLATE = 8; // DEFLATE


    public final static long   PACKET_SIGNATURE = 0x04034b50;
    public final static byte[] PACKET_VERSION    = {20, 0};
    public final static long   PACKET_FLAGS     = (1 << 3)   // DataDescriptor used
                                                | (1 << 11); // file_name is UTF-8

    private final static long   UNKOWN_CRC = 0;
    private final static long   UNKOWN_COMPRESSED_SIZE = 0;
    private final static long   UNKOWN_UNCOMPRESSED_SIZE = 0;
    private final static long   NO_EXTRA_FIELD = 0;



    @Override
    public byte[] getBytes(final ZipEntryDataWithCachedPackets zipEntry) {

        if (zipEntry == null) {
            throw new IllegalArgumentException("zip entry data parameter is null");
        }

        final ZipEntry entryData = zipEntry.getZipEntry();
        if (entryData == null) {
            throw new IllegalArgumentException(
                "zip entry data parameter does not have a ZipEntry info instance attached"
            );
        }

        if (entryData.getName() == null) {
            throw new IllegalArgumentException(
                "zip entry data does not contain a name for this entry"
            );
        }


        try (final ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            baos.write(this.convertLongToUInt32(LocalFileHeaderBuilder.PACKET_SIGNATURE));
            baos.write(LocalFileHeaderBuilder.PACKET_VERSION);
            baos.write(this.convertLongToUInt16(LocalFileHeaderBuilder.PACKET_FLAGS));

            if (entryData.getMethod() == java.util.zip.ZipEntry.STORED) {
                baos.write(this.convertLongToUInt16(LocalFileHeaderBuilder.COMPRESSION_METHOD_STORED));

            } else if (entryData.getMethod() == java.util.zip.ZipEntry.DEFLATED) {
                baos.write(this.convertLongToUInt16(LocalFileHeaderBuilder.COMPRESSION_METHOD_DEFLATE));

            } else {
                throw new IllegalArgumentException(
                    "zip entry data has set an unsupported compression method: " + entryData.getMethod()
                );
            }

            baos.write(this.convertFileTimeToZipTime(entryData.getLastModifiedTime()));
            baos.write(this.convertFileTimeToZipDate(entryData.getLastModifiedTime()));

            // file sizes and CRC are yet unknown - thus an extra data descriptor is added after the compressed bytes
            baos.write(this.convertLongToUInt32(LocalFileHeaderBuilder.UNKOWN_CRC));
            baos.write(this.convertLongToUInt32(LocalFileHeaderBuilder.UNKOWN_COMPRESSED_SIZE));
            baos.write(this.convertLongToUInt32(LocalFileHeaderBuilder.UNKOWN_UNCOMPRESSED_SIZE));

            final byte[] fileName = entryData.getName().getBytes(StandardCharsets.UTF_8);
            baos.write(this.convertLongToUInt16(fileName.length));
            baos.write(this.convertLongToUInt16(LocalFileHeaderBuilder.NO_EXTRA_FIELD));
            baos.write(fileName);

            return baos.toByteArray();
        } catch(final IOException exception) {
            throw new RuntimeException("failed to create local file header of entry", exception);
        }
    }
}
