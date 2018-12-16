package io.github.tsabirgaliev.zip;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.zip.ZipEntry;

import io.github.tsabirgaliev.zip.io.DeflaterCheckedInputStream;
import io.github.tsabirgaliev.zip.io.DeflaterDDInputStream;
import io.github.tsabirgaliev.zip.io.LazyInitInputStream;
import io.github.tsabirgaliev.zip.packets.DataDescriptorBuilder;
import io.github.tsabirgaliev.zip.packets.DirectoryFileHeaderBuilder;
import io.github.tsabirgaliev.zip.packets.LocalFileHeaderBuilder;

/***
 * provides access to the ZIP packets that have been created for this ZIP entry, with the except to the data bytes.
 *
 * During the processing of ZIP entries, various information packets are created for each entry. Such packets might
 * be referred to in later steps, so all the packets are cached in this entity, until the ZIP file has been
 * created.
 *
 * Each of these packets are created on the fly and cached internally for later re-use.
 *
 * @author nros <508093+nros@users.noreply.github.com>
 * @see https://users.cs.jmu.edu/buchhofp/forensics/formats/pkzip.html
 */
public class ZipEntryDataWithCachedPacketsImpl implements ZipEntryDataWithCachedPackets {

    private static WeakReference<DataDescriptorBuilder>      BUILDER_DATA_DESCRIPTOR = null;
    private static WeakReference<DirectoryFileHeaderBuilder> BUILDER_DIRECTORY_FILE_HEADER = null;
    private static WeakReference<LocalFileHeaderBuilder>     BUILDER_LOCAL_FILE_HEADER = null;


    private final ZipEntryData zipEntryData;
    private ZipEntry zipEntry = null;
    private InputStream compressedDataStream = null;

    private long localFileHeaderOffset = -1;

    private byte[] cachedDateDescriptor = null;
    private byte[] cachedDirectoryFileHeader = null;
    private byte[] cachedLocalFileHeader = null;

    public ZipEntryDataWithCachedPacketsImpl(final ZipEntryData entry) {
        this.zipEntryData = entry;
    }


    @Override
    public String getPath() {
        final String filePath = this.zipEntryData.getPath();
        return filePath != null ? filePath : this.getZipEntry().getName();
    }


    @Override
    public InputStream getStream() {
        if (this.compressedDataStream == null) {

            final ZipEntry entryData = this.getZipEntry();

            @SuppressWarnings("resource")
            InputStream zipDataStream = this.zipEntryData.getStream();
            try {
                if (zipDataStream == null) {
                    // try to open file as stream
                    zipDataStream = new FileInputStream(new File(this.getPath()));
                }
            } catch(final FileNotFoundException fileNotFoundException) {
                throw new RuntimeException("file not found: " + this.getPath(), fileNotFoundException);
            }


            final boolean useCompression = entryData.getMethod() == LocalFileHeaderBuilder.COMPRESSION_METHOD_DEFLATE;

            @SuppressWarnings("resource")
            final DeflaterCheckedInputStream compressedStream = new DeflaterCheckedInputStream(zipDataStream, useCompression);
            this.compressedDataStream = new DeflaterDDInputStream(
                compressedStream,
                (checkedStream) -> {
                    entryData.setCrc(checkedStream.getCrc());
                    entryData.setCompressedSize(checkedStream.getCompressedSize());
                    entryData.setSize(checkedStream.getSize());
                }
            );
        }


        return this.compressedDataStream;
    }


    @Override
    public ZipEntry getZipEntry() {
        if (this.zipEntry == null) {
            this.zipEntry = this.zipEntryData.getZipEntry();
        }

        if (this.zipEntry == null) {
            this.zipEntry = new ZipEntry(this.zipEntryData.getPath());
            this.zipEntry.setMethod(LocalFileHeaderBuilder.COMPRESSION_METHOD_DEFLATE);
        }

        return this.zipEntry;
    }


    @Override
    public byte[] getDataDescriptor() {
        if (this.cachedDateDescriptor == null) {

            DataDescriptorBuilder builder = ZipEntryDataWithCachedPacketsImpl.BUILDER_DATA_DESCRIPTOR != null ?
                ZipEntryDataWithCachedPacketsImpl.BUILDER_DATA_DESCRIPTOR.get() : null;
            if (builder == null) {
               builder = new DataDescriptorBuilder();
                ZipEntryDataWithCachedPacketsImpl.BUILDER_DATA_DESCRIPTOR = new WeakReference<>(builder);
            }
            this.cachedDateDescriptor = builder.getBytes(this);
        }
        return this.cachedDateDescriptor;
    }

    @Override
    public byte[] getLocalFileHeader() {
        if (this.cachedLocalFileHeader == null) {

            LocalFileHeaderBuilder builder = ZipEntryDataWithCachedPacketsImpl.BUILDER_LOCAL_FILE_HEADER != null ?
                ZipEntryDataWithCachedPacketsImpl.BUILDER_LOCAL_FILE_HEADER.get() : null;
            if (builder == null) {
               builder = new LocalFileHeaderBuilder();
                ZipEntryDataWithCachedPacketsImpl.BUILDER_LOCAL_FILE_HEADER = new WeakReference<>(builder);
            }

            this.cachedLocalFileHeader = builder.getBytes(this);
        }
        return this.cachedLocalFileHeader;
    }

    @Override
    public byte[] getDirectoryFileHeader() {
        if (this.cachedDirectoryFileHeader == null) {

            DirectoryFileHeaderBuilder builder = ZipEntryDataWithCachedPacketsImpl.BUILDER_DIRECTORY_FILE_HEADER != null ?
                ZipEntryDataWithCachedPacketsImpl.BUILDER_DIRECTORY_FILE_HEADER.get() : null;
            if (builder == null) {
               builder = new DirectoryFileHeaderBuilder();
                ZipEntryDataWithCachedPacketsImpl.BUILDER_DIRECTORY_FILE_HEADER = new WeakReference<>(builder);
            }

            this.cachedDirectoryFileHeader = builder.getBytes(this);;
        }
        return this.cachedDirectoryFileHeader;
    }

    @Override
    public InputStream getDataDescriptorPacketStream() {
        final ZipEntryDataWithCachedPacketsImpl me = this;
        return new LazyInitInputStream(() -> new ByteArrayInputStream(me.getDataDescriptor()));
    }

    @Override
    public long getLocalFileHeaderOffset() {
        if (this.localFileHeaderOffset < 0) {
            throw new RuntimeException("local file header offset has not been set yet!");
        }
        return this.localFileHeaderOffset;
    }

    @Override
    public ZipEntryDataWithCachedPacketsImpl setLocalFileHeaderOffset(final long offset) {
        this.localFileHeaderOffset = offset;
        return this;
    }
}

