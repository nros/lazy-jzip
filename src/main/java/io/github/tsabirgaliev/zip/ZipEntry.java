package io.github.tsabirgaliev.zip;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;
import java.util.zip.Deflater;

import io.github.tsabirgaliev.zip.io.DeflaterCheckedInputStream;
import io.github.tsabirgaliev.zip.io.ProxyInputStreamWithCloseListener;
import io.github.tsabirgaliev.zip.packets.LocalFileHeaderBuilder;

/**
 * extends the default Java {@code ZipEntry} with some information missing in the original class.
 *
 * <p>
 * Since the actual zipping of the entries is meant to be performed on-the-fly, as the ZIP bytes are read,
 * more information about the zipping is needed. eg: the compression level for the next entry can not be set on the
 * global ZIP handling class but must be stored with each entry.
 * </p>
 *
 * <p>
 * Instead of copying over bytes to a temporary file, all bytes in the ZIP are read directly from the source.
 * In order to use this zipping strategy in its full power, the original source file must be available with the zip
 * entry information. So these additional data must be provided.
 * </p>
 *
 * <p>
 * The collector of this instance is repsonsible to close the input streams after usage.
 * </p>
 */
public class ZipEntry extends java.util.zip.ZipEntry {

    private int compressionLevel = -1;
    private InputStream inStream;
    private InputStream compressedDataStream;
    private File file;
    private boolean isDeleteFileOnFullyRead = false;


    public ZipEntry(final String entryPathInZip) {
        super(entryPathInZip);
    }


    /**
     * copies over all data from the other entry.
     *
     * <p>
     * Beware, in case the other entry is of this very same type {@code ZipEntry}, the data set via
     * {@link #setInputStream(InputStream)} or {@link #setFile(File)} are copied as well. This might be lead to
     * an access conflict, of both instance are used simultaneously. It is not possible clone the other input stream.
     * Thus the very same input stream is used at the same time. In case of the file, this does not seem to be a
     * problem, because file access is limited to reading the file data. Concurrent reads of the same file usually
     * is possible on most operating systems.
     * </p>
     *
     * @param otherEntry - the other entry to copy over all data from.
     */
    public ZipEntry(final java.util.zip.ZipEntry otherEntry) {
        super(otherEntry);
        if (otherEntry instanceof ZipEntry) {
            this.file = ((ZipEntry)otherEntry).file;
            this.compressionLevel = ((ZipEntry)otherEntry).compressionLevel;
            this.compressedDataStream = ((ZipEntry)otherEntry).compressedDataStream;
            this.inStream = ((ZipEntry)otherEntry).inStream;
        }
    }


    /**
     * copies over the path as zip entry name and the {@code InputStream} of the provided entry data.
     *
     * @param entryData - the zip entry data to copy over the data from.
     */
    public ZipEntry(final ZipEntryData entryData) {
        super(entryData.getPath());

        @SuppressWarnings("resource")
        final InputStream inData = entryData.getStream();
        if (inData == null) {
            throw new IllegalArgumentException("The ZIP entry data does not contain an input stream to read from");
        }
        this.setInputStream(inData);
    }


    /**
     * provide a compression level if the entry used any compression.
     *
     * <p>
     * The level must be in the range of {@code 0 - 9}, according to {@link java.util.zip.Deflater}.
     * {@code 0} means "no compression (best speed)" (see {@link java.util.zip.Deflater#BEST_SPEED})
     * and {@code 9} is for "best compression" (see {@link java.util.zip.Deflater#BEST_COMPRESSION})
     * </p>
     *
     * @return a compression level or {@code -1} in case the level has not been set.
     */
    public int getCompressionLevel() {
        return this.compressionLevel;
    }


    /**
     * sets a new compression level.
     *
     * <p>
     * The level must be in the range of {@code 0 - 9}, according to {@link java.util.zip.Deflater}.
     * {@code 0} means "no compression (best speed)" (see {@link java.util.zip.Deflater#BEST_SPEED})
     * and {@code 9} is for "best compression" (see {@link java.util.zip.Deflater#BEST_COMPRESSION})
     * </p>
     *
     * @param newCompressionLevel - the new level. If the level is lower than {@link java.util.zip.Deflater#BEST_SPEED}
     *     it is set to {@code BEST_SPEED}. If it is higher than {@link java.util.zip.Deflater#BEST_COMPRESSION}
     *     it is set to {@code BEST_COMPRESSION}
     *
     *
     * @return this instance to allow a <a href="https://en.wikipedia.org/wiki/Fluent_interface">fluent interface</a>
     */
    public ZipEntry setCompressionLevel(final int newCompressionLevel) {

        this.compressionLevel = Math.min(
            Math.max(newCompressionLevel, Deflater.BEST_SPEED),
            Math.min(newCompressionLevel, Deflater.BEST_COMPRESSION)
        );

        return this;
    }


    /**
     * use a file instead of an {@code InputStream} as source for the data to add to the ZIP archive.
     *
     * <p>
     * Using an {@code InputStream} has precedence over using a file.
     * </p>
     *
     * @param fileToZip - the file to add to the ZIP
     * @param deleteFileOnFullyRead - if set to {@code true}, the file is deleted as soon as the created input stream
     *     is closed. Use with care and set only if you delegate the clean-up of the temporary file to this class.
     * @return this instance to allow a <a href="https://en.wikipedia.org/wiki/Fluent_interface">fluent interface</a>
     * @throws IllegalArgumentException in case an {@code InputStream} has already been set with
     *     {@link #setInputStream(InputStream)}
     */
    public ZipEntry setFile(final File fileToZip, final boolean deleteFileOnFullyRead) {
        if (this.getInputStream() != null) {
            throw new IllegalArgumentException("zip entry already has an input stream set as source of data");
        }
        this.file = fileToZip;
        this.isDeleteFileOnFullyRead = deleteFileOnFullyRead;
        return this;
    }


    /**
     * use a file instead of an {@code InputStream} as source for the data to add to the ZIP archive.
     *
     * <p>
     * Using an {@code InputStream} has precedence over using a file.
     * </p>
     *
     * @param fileToZip - the file to add to the ZIP. The file is not deleted when reading finishes.
     * @return this instance to allow a <a href="https://en.wikipedia.org/wiki/Fluent_interface">fluent interface</a>
     * @throws IllegalArgumentException in case an {@code InputStream} has already been set with
     *     {@link #setInputStream(InputStream)}
     */
    public ZipEntry setFile(final File fileToZip) {
        return this.setFile(fileToZip, false);
    }


    /**
     * use a file instead of an {@code InputStream} as source for the data to add to the ZIP archive.
     *
     * <p>
     * Using an {@code InputStream} has precedence over using a file.
     * </p>
     *
     * @param fileToZip - the file to add to the ZIP. The file is deleted when reading its byte has finished.
     * @return this instance to allow a <a href="https://en.wikipedia.org/wiki/Fluent_interface">fluent interface</a>
     * @throws IllegalArgumentException in case an {@code InputStream} has already been set with
     *     {@link #setInputStream(InputStream)}
     */
    public ZipEntry setTemporaryFile(final File fileToZip) {
        return this.setFile(fileToZip, true);
    }


    /**
     * returns the file to use instead of an {@code InputStream} as source for the data.
     *
     * <p>
     * Using an {@code InputStream} has precedence over using a file.
     * </p>
     *
     * @return the file to use as source of data or {@code null} in case no file has been set.
     */
    public File getFile() {
        return this.file;
    }


    /**
     * returns the file to use instead of an {@code InputStream} as source for the data.
     *
     * <p>
     * Using an {@code InputStream} has precedence over using a file.
     * </p>
     *
     * @return the file to use as source of data or {@code null} in case no file has been set.
     */
    public boolean isFileDeletedOnFullyRead() {
        return this.isDeleteFileOnFullyRead;
    }


    /**
     * provide an optionally compressed {@code InputStream} to read the bytes from.
     *
     * <p>
     * The previously set stream (by {@link #setInputStream(InputStream)}) is wrapped with a compressing filter stream
     * to compress all the bytes are compressed on the fly. A {@link java.util.zip.Deflater} stream is used.
     * In case a file is used instead of an {@code InputStream}, the file is opened and a
     * {@link java.io.FileInputStream} is created and wrapped with the {@code Deflater} stream.
     * </p>
     *
     * <p>
     * While reading from the provided stream, some metrics are recorded, like the amount of bytes read from the
     * uncompressed stream, the amount of bytes from the compressed stream wrapper and the CRC32 checksum of the
     * compressed data stream.
     * These metrics are then stored in this zip entry to be used by the zipper class.
     * </p>
     *
     * @return a stream to compress on-the-fly reading the bytes either from the provided input stream
     *     (see {@link #getInputStream()}) or from the file obtained with {@link #getFile()}.
     */
    @SuppressWarnings("resource")
    public InputStream getStream() {

        if (this.compressedDataStream == null) {

            InputStream inData = this.getInputStream();
            try {
                if (inData == null && this.file != null && this.isDeleteFileOnFullyRead) {

                    final File temporaryFile = this.file;
                    final ProxyInputStreamWithCloseListener<InputStream> closeableStream =
                        new ProxyInputStreamWithCloseListener<InputStream>(new FileInputStream(temporaryFile));

                    closeableStream.addCloseListener(new Consumer<InputStream>() {
                        @Override
                        public void accept(final InputStream t) {
                            temporaryFile.delete();
                        }
                    });
                    inData = closeableStream;

                } else if (inData == null && this.file != null) {
                    inData = new FileInputStream(this.file);
                }

            } catch (final FileNotFoundException notFoundException) {
                throw new RuntimeException("failed to open file: " + this.file, notFoundException);
            }

            if (inData != null) {
                final boolean useCompression = this.getMethod() == LocalFileHeaderBuilder.COMPRESSION_METHOD_DEFLATE;

                final DeflaterCheckedInputStream compressedStream =
                    new DeflaterCheckedInputStream(inData, useCompression)
                ;
                this.compressedDataStream = new ProxyInputStreamWithCloseListener<DeflaterCheckedInputStream>(
                    compressedStream
                ).addCloseListener((checkedStream) -> {
                    this.setCrc(checkedStream.getCrc());
                    this.setCompressedSize(checkedStream.getCompressedSize());
                    this.setSize(checkedStream.getSize());
                });

            } else {
                throw new RuntimeException("No data is available for this zip entry");
            }

        }


        if (this.compressedDataStream == null) {
            throw new RuntimeException(
                "failed to acquire input data as stream. File available? "
                + (this.file != null ? this.file.getPath() : "NO")
            );
        }
        return this.compressedDataStream;
    }


    /**
     * rather than reading the data from a file, use that {@code InputStream} instead to read uncompressed bytes from.
     *
     * <p>
     * The provided {@code InputStream} has higher precedence than a provided file.
     * </p>
     *
     * @param newInDataStream - the new input stream to set
     * @return this instance to allow a <a href="https://en.wikipedia.org/wiki/Fluent_interface">fluent interface</a>
     * @throws IllegalArgumentException in case a file has been set as source of the data.
     */
    public ZipEntry setInputStream(final InputStream newInDataStream) {

        if (this.getFile() != null) {
            throw new IllegalArgumentException("zip entry already has a file set as source of data");
        }

        return this.setInputStreamAndClosePrevious(newInDataStream);
    }


    /**
     * returns the raw, uncompressed input stream that is available or {@code null} if a file is used as data.
     *
     * @return the previously set
     */
    public InputStream getInputStream() {
        return this.inStream;
    }


    /**
     * rather than reading the data from a file, use that {@code InputStream} instead to read uncompressed bytes from.
     *
     * <p>
     * The provided {@code InputStream} has higher precedence than a provided file. Previous input streams are closed
     * to avoid resource leaks.
     * </p>
     *
     * @param newInDataStream - the new input stream to set
     * @return this instance to allow a <a href="https://en.wikipedia.org/wiki/Fluent_interface">fluent interface</a>
     */
    private ZipEntry setInputStreamAndClosePrevious(final InputStream newInDataStream) {

        final InputStream oldInStream = this.inStream;
        this.inStream = newInDataStream;
        if (oldInStream != null) {
            try {
                oldInStream.close();
            } catch (final IOException ignore) {
                throw new RuntimeException("failed to close previous input stream", ignore);
            }
        }

        return this;
    }
}
