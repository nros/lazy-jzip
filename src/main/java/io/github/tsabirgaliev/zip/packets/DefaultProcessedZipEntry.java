package io.github.tsabirgaliev.zip.packets;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;

import io.github.tsabirgaliev.zip.ZipEntry;
import io.github.tsabirgaliev.zip.io.LazyInitInputStream;

/**
 * provides access to the ZIP packets that have been created for this ZIP entry, with the except to the data bytes.
 *
 * <p>
 * During the processing of ZIP entries, various information packets are created for each entry. Such packets might
 * be referred to in later steps, so all the packets are cached in this entity, until the ZIP file has been
 * created.
 * </p>
 *
 * <p>
 * Each of these packets are created on the fly and cached internally for later re-use.
 * </p>
 *
 * @see <a href="https://users.cs.jmu.edu/buchhofp/forensics/formats/pkzip.html"
 *     >https://users.cs.jmu.edu/buchhofp/forensics/formats/pkzip.html</a>
 */
public class DefaultProcessedZipEntry implements ProcessedZipEntry {

    private static WeakReference<DataDescriptorBuilder>      BUILDER_DATA_DESCRIPTOR = null;
    private static WeakReference<DirectoryFileHeaderBuilder> BUILDER_DIRECTORY_FILE_HEADER = null;
    private static WeakReference<LocalFileHeaderBuilder>     BUILDER_LOCAL_FILE_HEADER = null;


    private final ZipEntry zipEntry;
    private long localFileHeaderOffset = -1;

    private byte[] cachedDateDescriptor = null;
    private byte[] cachedDirectoryFileHeader = null;
    private byte[] cachedLocalFileHeader = null;

    public DefaultProcessedZipEntry(final ZipEntry entry) {
        this.zipEntry = entry;
    }


    @Override
    public ZipEntry getZipEntry() {
        return this.zipEntry;
    }


    @Override
    public byte[] getDataDescriptor() {
        if (this.cachedDateDescriptor == null) {

            DataDescriptorBuilder builder = DefaultProcessedZipEntry.BUILDER_DATA_DESCRIPTOR != null ?
                DefaultProcessedZipEntry.BUILDER_DATA_DESCRIPTOR.get() : null;
            if (builder == null) {
               builder = new DataDescriptorBuilder();
                DefaultProcessedZipEntry.BUILDER_DATA_DESCRIPTOR = new WeakReference<>(builder);
            }
            this.cachedDateDescriptor = builder.getBytes(this);
        }
        return this.cachedDateDescriptor;
    }

    @Override
    public byte[] getLocalFileHeader() {
        if (this.cachedLocalFileHeader == null) {

            LocalFileHeaderBuilder builder = DefaultProcessedZipEntry.BUILDER_LOCAL_FILE_HEADER != null ?
                DefaultProcessedZipEntry.BUILDER_LOCAL_FILE_HEADER.get() : null;
            if (builder == null) {
               builder = new LocalFileHeaderBuilder();
                DefaultProcessedZipEntry.BUILDER_LOCAL_FILE_HEADER = new WeakReference<>(builder);
            }

            this.cachedLocalFileHeader = builder.getBytes(this);
        }
        return this.cachedLocalFileHeader;
    }

    @Override
    public byte[] getDirectoryFileHeader() {
        if (this.cachedDirectoryFileHeader == null) {

            DirectoryFileHeaderBuilder builder = DefaultProcessedZipEntry.BUILDER_DIRECTORY_FILE_HEADER != null ?
                DefaultProcessedZipEntry.BUILDER_DIRECTORY_FILE_HEADER.get() : null;
            if (builder == null) {
               builder = new DirectoryFileHeaderBuilder();
                DefaultProcessedZipEntry.BUILDER_DIRECTORY_FILE_HEADER = new WeakReference<>(builder);
            }

            this.cachedDirectoryFileHeader = builder.getBytes(this);;
        }
        return this.cachedDirectoryFileHeader;
    }

    @Override
    public InputStream getDataDescriptorPacketStream() {
        return new LazyInitInputStream<InputStream>(() -> new ByteArrayInputStream(this.getDataDescriptor()));
    }

    @Override
    public long getLocalFileHeaderOffset() {
        if (this.localFileHeaderOffset < 0) {
            throw new RuntimeException("local file header offset has not been set yet!");
        }
        return this.localFileHeaderOffset;
    }

    @Override
    public DefaultProcessedZipEntry setLocalFileHeaderOffset(final long offset) {
        this.localFileHeaderOffset = offset;
        return this;
    }
}

