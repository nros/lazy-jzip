package io.github.tsabirgaliev.zip.packets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;


/**
 * this helps building the central directory of the ZIP archive, which every archive contains.
 *
 * <p>
 * The central directory is located at the end of the ZIP file. It can be located by looking for the
 * "End of central directory record (EOCD)", which contains an offset from the beginning where the central directory
 * starts. The EOCD uses a special signature byte in order to locate it at the end of the file.
 * </p>
 */
public class CentralDirectoryBuilder extends AbstractZipPacketBuilder {

    /**
     * create the central directory for the list of processed zip entries - in bytes.
     *
     * @param entries - the entries to register with the central directory.
     * @return the bytes representing the COD within the ZIP archive.
     */
    public byte[] getBytes(final List<ProcessedZipEntry> entries) {

        try (final ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            long directoryOffset = 0;
            long directorySize = 0;


            for (final ProcessedZipEntry zipEntry : entries) {

                final byte[] directoryFileEntry = zipEntry.getDirectoryFileHeader();
                final byte[] dataDescrptor = zipEntry.getDataDescriptor();

                directorySize += directoryFileEntry.length;
                directoryOffset  +=
                    directoryFileEntry.length
                    + zipEntry.getZipEntry().getCompressedSize()
                    + dataDescrptor.length;

                baos.write(directoryFileEntry);
            }

            baos.write(new EndOfCentralDirectoryBuilder().getBytes(entries.size(), directorySize, directoryOffset));
            return baos.toByteArray();

        } catch (final IOException ignore) {
            throw new RuntimeException("failed to create central directory", ignore);
        }
    }
}
