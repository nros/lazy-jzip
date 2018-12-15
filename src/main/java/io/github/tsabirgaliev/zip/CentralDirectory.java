package io.github.tsabirgaliev.zip;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.github.tsabirgaliev.zip.BaseZipPacketBuilder;


/***
 * this helps building the central directory of the ZIP archive, which every archive contains.
 *
 * The central directory is located at the end of the ZIP file. It can be located by looking for the
 * "End of central directory record (EOCD)", which contains an offset from the beginning where the central directory
 * starts. The EOCD uses a special signature byte in order to locate it at the end of the file.
 *
 * @author Tair Sabirgaliev <tair.sabirgaliev@gmail.com>
 * @author nros <508093+nros@users.noreply.github.com>
 */
public class CentralDirectory extends BaseZipPacketBuilder {

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
