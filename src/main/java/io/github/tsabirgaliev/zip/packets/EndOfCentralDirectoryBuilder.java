package io.github.tsabirgaliev.zip.packets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;


/**
 * builds the end packet of the central directory (EOCD), which can be detected because of its special signature.
 *
 * <p>
 * The location of the central directory (CED) within a ZIP file is found by reading the ending packet of the CED.
 * This end packet contains information about the beginning of the CED and the amount of parts of it.
 * So unzippers read the end of the file backwards until they read the package signature of this ending packet.
 * Then the CED can be located.
 * </p>
 *
 * @see <a href="https://users.cs.jmu.edu/buchhofp/forensics/formats/pkzip.html"
 *     >https://users.cs.jmu.edu/buchhofp/forensics/formats/pkzip.html</a>
 */
public class EndOfCentralDirectoryBuilder extends BaseZipPacketBuilder {

    private static final long PACKET_SIGNATURE = 0x06054b50;
    private static final long NUMBER_OF_ARCHIVE_PARTS = 0;
    private static final long ARHIVE_NUMBER_WITH_DIRECTORY = 0;
    private static final long COMMENT_LENGTH = 0;


    /**
     * return a new end-of-directory packet (EDO), based on the input parameter.
     *
     * @param amountOfEntries - the amount of entries in the central directory of the ZIP archive
     * @param directorySize - the size in bytes of the central directory
     * @param directoryOffset - the offset from the starting byte of the file to the first byte of the central
     *     directory of the ZIP archive.
     * @return a new EOD in bytes.
     */
    public byte[] getBytes(final long amountOfEntries, final long directorySize, final long directoryOffset) {

        try (final ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            baos.write(this.convertLongToUInt32(EndOfCentralDirectoryBuilder.PACKET_SIGNATURE));
            baos.write(this.convertLongToUInt16(EndOfCentralDirectoryBuilder.NUMBER_OF_ARCHIVE_PARTS));
            baos.write(this.convertLongToUInt16(EndOfCentralDirectoryBuilder.ARHIVE_NUMBER_WITH_DIRECTORY));
            baos.write(this.convertLongToUInt16(amountOfEntries));
            baos.write(this.convertLongToUInt16(amountOfEntries));
            baos.write(this.convertLongToUInt32(directorySize));
            baos.write(this.convertLongToUInt32(directoryOffset));
            baos.write(this.convertLongToUInt16(EndOfCentralDirectoryBuilder.COMMENT_LENGTH));

            return baos.toByteArray();

        } catch(final IOException exception) {
            throw new RuntimeException("failed to create end of central directory entry", exception);
        }
    }
}
