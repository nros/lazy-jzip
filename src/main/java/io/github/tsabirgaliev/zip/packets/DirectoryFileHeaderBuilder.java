package io.github.tsabirgaliev.zip.packets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;

/**
 * creates the bytes of the file header in the central directory, which contains information about the data bytes.
 *
 * <p>
 * Most important, it contains the offset from the start of the file where the local file header is located. This is
 * vital to locate the compressed data.
 * </p>
 */
public class DirectoryFileHeaderBuilder extends BaseZipPacketBuilder implements ZipEntryPacketBuilder {


    private static final byte[] EXTERNAL_ATTRIBUTES = {0x0, 0x0, (byte)0xa4, (byte)0x81};
    private static final long PACKET_SIGNATURE      = 0x02014b50;


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
            final byte[] fileName = entryData.getName().getBytes(StandardCharsets.UTF_8);

            baos.write(this.convertLongToUInt32(DirectoryFileHeaderBuilder.PACKET_SIGNATURE));

            // version made by
            baos.write(LocalFileHeaderBuilder.PACKET_VERSION);

            // version needed to extract
            baos.write(LocalFileHeaderBuilder.PACKET_VERSION);

            baos.write(this.convertLongToUInt16(LocalFileHeaderBuilder.PACKET_FLAGS));
            baos.write(this.convertLongToUInt16(entryData.getMethod()));
            baos.write(this.convertFileTimeToZipTime(entryData.getLastModifiedTime()));
            baos.write(this.convertFileTimeToZipDate(entryData.getLastModifiedTime()));
            baos.write(this.convertLongToUInt32(entryData.getCrc()));
            baos.write(this.convertLongToUInt32(entryData.getCompressedSize()));
            baos.write(this.convertLongToUInt32(entryData.getSize()));
            baos.write(this.convertLongToUInt16(fileName.length));

            final byte[] extraFielData = entryData.getExtra();
            baos.write(this.convertLongToUInt16(extraFielData != null ? extraFielData.length : 0));

            final String commentString = entryData.getComment();
            final byte[] comment = commentString != null ? commentString.getBytes(StandardCharsets.UTF_8) : null;
            baos.write(this.convertLongToUInt16(comment != null ? comment.length : 0));

            // only a single ZIP file is supported right now
            baos.write(this.convertLongToUInt16(0));

            baos.write(this.convertLongToUInt16(0));
            baos.write(DirectoryFileHeaderBuilder.EXTERNAL_ATTRIBUTES);

            baos.write(this.convertLongToUInt32(zipEntry.getLocalFileHeaderOffset()));

            baos.write(fileName);

            return baos.toByteArray();

        } catch (final IOException exception) {
            throw new RuntimeException("failed to create central directory entry", exception);
        }
    }
}
