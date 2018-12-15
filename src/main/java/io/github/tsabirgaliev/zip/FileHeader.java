package io.github.tsabirgaliev.zip;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/***
 * creates the bytes of the file header in the central directory, which contains information about the compressed bytes
 *
 * Most important, it contains the offset from the start of the file where the local file header is located. This is
 * vital to locate the compressed data.
 *
 * @author Tair Sabirgaliev <tair.sabirgaliev@gmail.com>
 * @author nros <508093+nros@users.noreply.github.com>
 */
public class FileHeader extends BaseZipPacketBuilder {


    private final static long PACKET_SIGNATURE      = 0x02014b50;
    private final static byte[] EXTERNAL_ATTRIBUTES = {0x0, 0x0, (byte)0xa4, (byte)0x81};

    long file_comment_length = 0;
    long disk_num_start = 0;
    long internal_attrs = 0;
    long local_header_offset = 0;

    LocalFileHeader lfh;

    public FileHeader(final LocalFileHeader lfh, final long lfh_offset) {
        this.lfh = lfh;
        this.local_header_offset = lfh_offset;
    }

    public byte[] getBytes() throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        baos.write(this.convertLongToUInt32(FileHeader.PACKET_SIGNATURE));
        baos.write(LocalFileHeader.PACKET_VERSION); // version made by
        baos.write(LocalFileHeader.PACKET_VERSION); // version needed to extract
        baos.write(this.convertLongToUInt16(LocalFileHeader.PACKET_FLAGS));
        baos.write(this.convertLongToUInt16(LocalFileHeader.COMPRESSION_METHOD_DEFLATE));
        baos.write(this.lfh.modification_time);
        baos.write(this.lfh.modification_date);
        baos.write(this.convertLongToUInt32(this.lfh.crc32_checksum));
        baos.write(this.convertLongToUInt32(this.lfh.compressed_size));
        baos.write(this.convertLongToUInt32(this.lfh.uncompressed_size));
        baos.write(this.convertLongToUInt16(this.lfh.file_name.length));
        baos.write(this.convertLongToUInt16(this.lfh.extra_field_length));
        baos.write(this.convertLongToUInt16(this.file_comment_length));
        baos.write(this.convertLongToUInt16(this.disk_num_start));
        baos.write(this.convertLongToUInt16(this.internal_attrs));
        baos.write(FileHeader.EXTERNAL_ATTRIBUTES);
        baos.write(this.convertLongToUInt32(this.local_header_offset));
        baos.write(this.lfh.file_name);

        baos.close();

        return baos.toByteArray();
    }
}
