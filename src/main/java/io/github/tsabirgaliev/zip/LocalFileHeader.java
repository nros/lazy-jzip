package io.github.tsabirgaliev.zip;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Calendar;

public class LocalFileHeader {
    long signature = 0x04034b50;

    byte[] version = {20, 0};
    long flags     = (1 << 3)   // DataDescriptor used
                   | (1 << 11); // file_name is UTF-8

    long compression_method = 8; // DEFLATE

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

    public LocalFileHeader(final String filename) {
        this.file_name = filename.getBytes(Charset.forName("UTF-8"));
    }

    public byte[] getBytes() throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        baos.write(CentralDirectoryUtilities.bytes4(this.signature));
        baos.write(this.version);
        baos.write(CentralDirectoryUtilities.bytes2(this.flags));
        baos.write(CentralDirectoryUtilities.bytes2(this.compression_method));
        baos.write(this.modification_time);
        baos.write(this.modification_date);
        baos.write(CentralDirectoryUtilities.bytes4(this.crc32_checksum));
        baos.write(CentralDirectoryUtilities.bytes4(this.compressed_size));
        baos.write(CentralDirectoryUtilities.bytes4(this.uncompressed_size));

        baos.write(CentralDirectoryUtilities.bytes2(this.file_name.length));

        baos.write(CentralDirectoryUtilities.bytes2(this.extra_field_length));
        baos.write(this.file_name);

        baos.close();

        return baos.toByteArray();
    }
}
