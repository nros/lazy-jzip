package io.github.tsabirgaliev.zip.packets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;

/**
 * creates the bytes of the data descriptor packet, which is used to hold some size information about an entry.
 *
 * <p>
 * The "data descriptor" is an optional, additional structure, added to the ZIP file after the compressed data
 * of the entry. It contains information about the sizes and CRC32 of the compressed data, which have not been
 * calculated prior to adding the compressed data to the ZIP file.
 * </p>
 *
 * <p>
 * This is very handy, as this package handles arbitrary {@link java.io.InputStream}, where it is impossible
 * know the sizes in advance. Although this packet is optional, it is used extensively with this ZIP packager.
 * All this to read the bytes of the compressed entry and add the sizes afterwards.
 * </p>
 */
public class DataDescriptorBuilder extends BaseZipPacketBuilder implements ZipEntryPacketBuilder {

    private static final long PACKET_SIGNATURE = 0x08074b50;

    @Override
    public byte[] getBytes(final ProcessedZipEntry zipEntry) {

        if (zipEntry == null) {
            throw new IllegalArgumentException("zip entry data parameter is null");
        }

        if (zipEntry.getZipEntry() == null) {
            throw new IllegalArgumentException(
                "zip entry data parameter does not have a ZipEntry info instance attached"
            );
        }


        try (final ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            final ZipEntry entryData = zipEntry.getZipEntry();

            baos.write(this.convertLongToUInt32(DataDescriptorBuilder.PACKET_SIGNATURE));
            baos.write(this.convertLongToUInt32(entryData.getCrc()));
            baos.write(this.convertLongToUInt32(entryData.getCompressedSize()));
            baos.write(this.convertLongToUInt32(entryData.getSize()));

            return baos.toByteArray();

        } catch (final IOException exception) {
            throw new RuntimeException("failed to create data descriptor of entry", exception);
        }
    }
}
