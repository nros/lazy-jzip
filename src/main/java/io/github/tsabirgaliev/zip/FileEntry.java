package io.github.tsabirgaliev.zip;

public class FileEntry {
    LocalFileHeader lfh;
    DataDescriptor dd;

    public FileEntry(final LocalFileHeader lfh, final DataDescriptor dd) {
        this.lfh = lfh;
        this.dd = dd;
    }
}

