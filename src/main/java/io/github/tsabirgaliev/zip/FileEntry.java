package io.github.tsabirgaliev.zip;

import io.github.tsabirgaliev.zip.packets.DataDescriptor;
import io.github.tsabirgaliev.zip.packets.LocalFileHeader;

public class FileEntry {
    public LocalFileHeader lfh;
    public DataDescriptor dd;

    public FileEntry(final LocalFileHeader lfh, final DataDescriptor dd) {
        this.lfh = lfh;
        this.dd = dd;
    }
}

