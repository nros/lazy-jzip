package io.github.tsabirgaliev.zip;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class FileHeader {
    long signature = 0x02014b50;
    long file_comment_length = 0;
    long disk_num_start = 0;
    long internal_attrs = 0;
    byte[] external_attrs = {0x0, 0x0, (byte)0xa4, (byte)0x81};
    long local_header_offset = 0;

    LocalFileHeader lfh;

    public FileHeader(final LocalFileHeader lfh, final long lfh_offset) {
        this.lfh = lfh;
        this.local_header_offset = lfh_offset;
    }

    public byte[] getBytes() throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        baos.write(CentralDirectoryUtilities.bytes4(this.signature));
        baos.write(this.lfh.version); // version made by
        baos.write(this.lfh.version); // version needed to extract
        baos.write(CentralDirectoryUtilities.bytes2(this.lfh.flags));
        baos.write(CentralDirectoryUtilities.bytes2(this.lfh.compression_method));
        baos.write(this.lfh.modification_time);
        baos.write(this.lfh.modification_date);
        baos.write(CentralDirectoryUtilities.bytes4(this.lfh.crc32_checksum));
        baos.write(CentralDirectoryUtilities.bytes4(this.lfh.compressed_size));
        baos.write(CentralDirectoryUtilities.bytes4(this.lfh.uncompressed_size));
        baos.write(CentralDirectoryUtilities.bytes2(this.lfh.file_name.length));
        baos.write(CentralDirectoryUtilities.bytes2(this.lfh.extra_field_length));
        baos.write(CentralDirectoryUtilities.bytes2(this.file_comment_length));
        baos.write(CentralDirectoryUtilities.bytes2(this.disk_num_start));
        baos.write(CentralDirectoryUtilities.bytes2(this.internal_attrs));
        baos.write(this.external_attrs);
        baos.write(CentralDirectoryUtilities.bytes4(this.local_header_offset));
        baos.write(this.lfh.file_name);

        baos.close();

        return baos.toByteArray();
    }
}
