package io.github.tsabirgaliev.lazyzip;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class End {
    long signature = 0x06054b50;
    long disk_number = 0;
    long disk_number_with_cd = 0;
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

        baos.write(CentralDirectoryUtilities.bytes4(this.signature));
        baos.write(CentralDirectoryUtilities.bytes2(this.disk_number));
        baos.write(CentralDirectoryUtilities.bytes2(this.disk_number_with_cd));
        baos.write(CentralDirectoryUtilities.bytes2(this.disk_entries));
        baos.write(CentralDirectoryUtilities.bytes2(this.total_entries));
        baos.write(CentralDirectoryUtilities.bytes4(this.cd_size));
        baos.write(CentralDirectoryUtilities.bytes4(this.cd_offset));
        baos.write(CentralDirectoryUtilities.bytes2(this.comment_length));

        baos.close();

        return baos.toByteArray();
    }
}
