package io.github.tsabirgaliev.zip;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.function.Consumer;
import java.util.function.Supplier;
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
 * The collector of this instance is responsible to close the input streams after usage.
 * </p>
 */
public class ZipEntry extends java.util.zip.ZipEntry {

    private int compressionLevel = -1;
    private Supplier<InputStream> inStreamSupplier;
    private InputStream fetchedInputStreamFromSupplier;
    private InputStream compressedDataStream;


    public ZipEntry(final String entryPathInZip) {
        super(entryPathInZip);
    }


    /**
     * copies over all data from the other entry.
     *
     * <p>
     * Beware, in case the other entry is of this very same type {@code ZipEntry}, the data set via
     * {@link #setInputStream(InputStream)} are copied as well. This might be lead to
     * an access conflict, of both instance are used simultaneously. It is not possible to clone the other input
     * stream, though. Thus the very same input stream is used at the same time.
     * In case of the file, this does not seem to be a problem, because the file is only read - not written.
     * </p>
     *
     * @param otherEntry - the other entry to copy over all data from.
     */
    public ZipEntry(final java.util.zip.ZipEntry otherEntry) {
        super(otherEntry);
        if (otherEntry instanceof ZipEntry) {
            this.compressionLevel = ((ZipEntry)otherEntry).compressionLevel;
            this.inStreamSupplier = ((ZipEntry)otherEntry).inStreamSupplier;
        }
    }


    /**
     * copies over the path as zip entry name and the {@code InputStream} of the provided entry data.
     *
     * @param entryData - the zip entry data to copy over the data from.
     */
    public ZipEntry(final ZipEntryData entryData) {
        this(entryData.getPath());
        this.setInputStreamSupplier(() -> entryData.getStream());
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
     * Any previously provided supplier for an {@code InputStream} is replaced.
     * </p>
     *
     * @param fileToZip - the file to add to the ZIP
     * @param deleteFileOnFullyRead - if set to {@code true}, the file is deleted as soon as the created input stream
     *     is closed. Use with care and set only if you delegate the clean-up of the temporary file to this class.
     * @return this instance to allow a <a href="https://en.wikipedia.org/wiki/Fluent_interface">fluent interface</a>
     * @throws IllegalArgumentException in case an {@code InputStream} has already been fetched from a previous
     *     supplier via {@link #getInputStream()}
     */
    @SuppressWarnings("resource")
    public ZipEntry setFile(final File fileToZip, final boolean deleteFileOnFullyRead) {


        if (deleteFileOnFullyRead) {
            return this.setInputStreamSupplier(() -> {
                try {
                    final ProxyInputStreamWithCloseListener<InputStream> closeableStream =
                        new ProxyInputStreamWithCloseListener<InputStream>(new FileInputStream(fileToZip));

                    closeableStream.addCloseListener(new Consumer<InputStream>() {
                        @Override
                        public void accept(final InputStream t) {
                            fileToZip.delete();
                        }
                    });

                    return closeableStream;

                } catch (final FileNotFoundException exception) {
                    throw new RuntimeException(
                        "failed to open temporary file: " + fileToZip.getAbsolutePath(),
                        exception
                    );
                }

            });

        } else {
            return this.setInputStreamSupplier(() -> {
                try {
                    return new FileInputStream(fileToZip);
                } catch (final FileNotFoundException exception) {
                    throw new RuntimeException(
                        "failed to open file: " + fileToZip.getAbsolutePath(),
                        exception
                    );
                }
            });
        }
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
     * @return a stream to compress on-the-fly reading the bytes from the provided input stream
     *     (see {@link #getInputStream()})
     */
    @SuppressWarnings("resource")
    public InputStream getStream() {

        if (this.compressedDataStream == null) {

            final InputStream inData = this.getInputStream();
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
            throw new RuntimeException("failed to acquire input data as stream ");
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
        return this.setInputStreamSupplier(() -> newInDataStream);
    }


    /**
     * rather than reading the data from a file, use that an {@code InputStream} fetched from the provided supplier.
     *
     * <p>
     * The provided {@code InputStream} has higher precedence than a provided file.
     * </p>
     *
     * @param newInDataStreamSupplier - the new supplier to get an Input Stream from.
     * @return this instance to allow a <a href="https://en.wikipedia.org/wiki/Fluent_interface">fluent interface</a>
     * @throws IllegalArgumentException in case a file has been set as source of the data.
     */
    public ZipEntry setInputStreamSupplier(final Supplier<InputStream> newInDataStreamSupplier) {


        if (this.fetchedInputStreamFromSupplier != null) {
            throw new IllegalArgumentException("zip entry has already read from another source of data");
        }

        this.inStreamSupplier = newInDataStreamSupplier;
        return this;
    }


    /**
     * returns the raw, uncompressed input stream that is available or {@code null} if a file is used as data.
     *
     * @return the previously set
     */
    protected InputStream getInputStream() {
        if (this.fetchedInputStreamFromSupplier == null && this.inStreamSupplier != null) {
            this.fetchedInputStreamFromSupplier = this.inStreamSupplier.get();
        }
        return this.fetchedInputStreamFromSupplier;
    }
}
