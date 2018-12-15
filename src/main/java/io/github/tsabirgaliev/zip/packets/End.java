package io.github.tsabirgaliev.zip.packets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;


/***
 * builds the end packet of the central directory (EOCD), which can be detected because of its special signature.
 *
 * The location of the central directory (CED) within a ZIP file is found by reading the ending packet of the CED.
 * This end packet contains information about the beginning of the CED and the amount of parts of it.
 * So unzippers read the end of the file backwards until they read the package signature of this ending packet.
 * Then the CED can be located.
 *
 * @author Tair Sabirgaliev <tair.sabirgaliev@gmail.com>
 * @author nros <508093+nros@users.noreply.github.com>
 */
public class End extends BaseZipPacketBuilder {

    private static final long PACKET_SIGNATURE = 0x06054b50;
    private static final long NUMBER_OF_ARHIVE_PARTS = 0;
    private static final long ARHIVE_NUMBER_WITH_DIRECTORY = 0;

    long disk_entries = 1;
    long total_entries = 1;
    long cd_size = 0x33;
    long cd_offset = 0x23;
    long comment_length = 0;


    public End(final long entries, final long cd_size, final long cd_offset) {
        this.disk_entries = this.total_entries = entries;
        this.cd_size = cd_size;
        this.cd_offset = cd_offset;
    }

    public byte[] getBytes() throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        baos.write(this.convertLongToUInt32(End.PACKET_SIGNATURE));
        baos.write(this.convertLongToUInt16(End.NUMBER_OF_ARHIVE_PARTS));
        baos.write(this.convertLongToUInt16(End.ARHIVE_NUMBER_WITH_DIRECTORY));
        baos.write(this.convertLongToUInt16(this.disk_entries));
        baos.write(this.convertLongToUInt16(this.total_entries));
        baos.write(this.convertLongToUInt32(this.cd_size));
        baos.write(this.convertLongToUInt32(this.cd_offset));
        baos.write(this.convertLongToUInt16(this.comment_length));

        baos.close();

        return baos.toByteArray();
    }
}
