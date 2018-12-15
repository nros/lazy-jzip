package io.github.tsabirgaliev.zip;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Calendar;

/***
 * creates the bytes of the local file header packet, which prepends the compressed data of the entry.
 *
 * @author Tair Sabirgaliev <tair.sabirgaliev@gmail.com>
 * @author nros <508093+nros@users.noreply.github.com>
 */
public class LocalFileHeader extends BaseZipPacketBuilder {

    public final static long   PACKET_SIGNATURE = 0x04034b50;
    public final static byte[] PACKET_VERSION    = {20, 0};
    public final static long   PACKET_FLAGS     = (1 << 3)   // DataDescriptor used
                                                | (1 << 11); // file_name is UTF-8

    public final static long   COMPRESSION_METHOD_DEFLATE = 8; // DEFLATE


    static byte[] currentDosTime(final Calendar cal) {
        final int result = (cal.get(Calendar.HOUR_OF_DAY) << 11)
                   | (cal.get(Calendar.MINUTE)      << 5)
                   | (cal.get(Calendar.SECOND)      / 2);

        return new byte[] {
                (byte)(result >> 0),
                (byte)(result >> 8)
        };
    }

    static byte[] currentDosDate(final Calendar cal) {
        final int result = ((cal.get(Calendar.YEAR) - 1980) << 9)
                   | ((cal.get(Calendar.MONTH) + 1)   << 5)
                   | cal.get(Calendar.DATE);

        return new byte[] {
                (byte)(result >> 0),
                (byte)(result >> 8)
        };
    }


    byte[] modification_time = LocalFileHeader.currentDosTime(Calendar.getInstance())
    , modification_date = LocalFileHeader.currentDosDate(Calendar.getInstance())
    ;

    final long crc32_checksum = 0
    , compressed_size   = 0
    , uncompressed_size = 0
    ;

    long extra_field_length = 0;
    byte[] file_name = {};


    private final ZipEntryData _zipEntryData;

    public LocalFileHeader(final ZipEntryData zipEntryData) {

        if (zipEntryData == null) {
            throw new IllegalArgumentException("invalid zip entra data provided <null>");
        }

        this._zipEntryData = zipEntryData;
        this.file_name = this._zipEntryData.getPath().getBytes(Charset.forName("UTF-8"));
    }

    public byte[] getBytes() throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        baos.write(this.convertLongToUInt32(LocalFileHeader.PACKET_SIGNATURE));
        baos.write(LocalFileHeader.PACKET_VERSION);
        baos.write(this.convertLongToUInt16(LocalFileHeader.PACKET_FLAGS));
        baos.write(this.convertLongToUInt16(LocalFileHeader.COMPRESSION_METHOD_DEFLATE));
        baos.write(this.modification_time);
        baos.write(this.modification_date);
        baos.write(this.convertLongToUInt32(this.crc32_checksum));
        baos.write(this.convertLongToUInt32(this.compressed_size));
        baos.write(this.convertLongToUInt32(this.uncompressed_size));

        baos.write(this.convertLongToUInt16(this.file_name.length));

        baos.write(this.convertLongToUInt16(this.extra_field_length));
        baos.write(this.file_name);

        baos.close();

        return baos.toByteArray();
    }
}
