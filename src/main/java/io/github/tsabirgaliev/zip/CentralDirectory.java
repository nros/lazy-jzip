package io.github.tsabirgaliev.lazyzip;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CentralDirectory {

    public List<FileHeader> headers;
    public End              end;

    public CentralDirectory(final List<FileEntry> entries) throws IOException {
        long offset = 0;
        long cd_size = 0;

        this.headers = new ArrayList<>(entries.size());

        for (final FileEntry fileEntry : entries) {
            final FileHeader fh = new FileHeader(fileEntry.lfh, offset);
            offset += fileEntry.lfh.getBytes().length + fileEntry.dd.compressed_size + fileEntry.dd.getBytes().length;
            cd_size += fh.getBytes().length;
            this.headers.add(fh);
        }

        this.end = new End(this.headers.size(), cd_size, offset);

    }

    public byte[] getBytes() throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        for(final FileHeader fh : this.headers) {
            baos.write(fh.getBytes());
        }

        baos.write(this.end.getBytes());

        baos.close();

        return baos.toByteArray();
    }
}
