package io.github.tsabirgaliev.zip;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.Deflater;

import io.github.tsabirgaliev.zip.io.DeflaterCheckedInputStream;
import io.github.tsabirgaliev.zip.io.DeflaterDDInputStream;
import io.github.tsabirgaliev.zip.packets.LocalFileHeaderBuilder;

/***
 * extends the default Java {@code ZipEntry} with some information missing in the original class.
 *
 * Since the actual zipping of the entries is meant to be performed on-the-fly, as the ZIP bytes are read,
 * more information about the zipping is needed. eg: the compression level for the next entry can not be set on the
 * global ZIP handling class but must be stored with each entry.
 *
 * Instead of copying over bytes to a temporary file, all bytes in the ZIP are read directly from the source.
 * In order to use this zipping strategy in its full power, the original source file must be available with the zip
 * entry information. So these additional data must be provided.
 *
 * The collector of this instance is repsonsible to close the input streams after usage.
 *
 * @author nros <508093+nros@users.noreply.github.com>
 */
public class ZipEntry extends java.util.zip.ZipEntry {

    private int compressionLevel = -1;
    private InputStream inStream;
    private InputStream compressedDataStream;
    private File file;


    public ZipEntry(final String entryPathInZIP) {
        super(entryPathInZIP);
    }


    /***
     * copies over all data from the other entry.
     *
     * Beware, in case the other entry is of this very same type {@code ZipEntry}, the data set via
     * {@link #setInputStream(InputStream)} or {@link #setFile(File)} are copied as well. This might be lead to
     * an access conflict, of both instance are used simultaneously. It is not possible clone the other input stream.
     * Thus the very same input stream is used at the same time. In case of the file, this does not seem to be a
     * problem, because file access is limited to reading the file data. Concurrent reads of the same file usually
     * is possible on most operating systems.
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


    /***
     * copies over the path as zip entry name and the {@code InputStream} of the provided entry data
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


    /***
     * provide a compression level if the entry used any compression.
     *
     * The level must be in the range of {@code 0 - 9}, according to {@link java.util.zip.Deflater}.
     * {@code 0} means "no compression (best speed)" (see {@link java.util.zip.Deflater#BEST_SPEED})
     * and {@code 9} is for "best compression" (see {@link java.util.zip.Deflater#BEST_COMPRESSION})
     *
     * @return a compression level or {@code -1} in case the level has not been set.
     */
    public int getCompressionLevel() {
        return this.compressionLevel;
    }


    /***
     * sets a new compression level.
     *
     * The level must be in the range of {@code 0 - 9}, according to {@link java.util.zip.Deflater}.
     * {@code 0} means "no compression (best speed)" (see {@link java.util.zip.Deflater#BEST_SPEED})
     * and {@code 9} is for "best compression" (see {@link java.util.zip.Deflater#BEST_COMPRESSION})
     *
     * @param newCompressionLevel - the new level. If the level is lower than {@link java.util.zip.Deflater#BEST_SPEED}
     *     it is set to {@code BEST_SPEED}. If it is higher than {@link java.util.zip.Deflater#BEST_COMPRESSION}
     *     it is set to {@code BEST_COMPRESSION}
     *
     *
     * @return a compression level or {@code -1} in case the level has not been set.
     */
    public ZipEntry setCompressionLevel(final int newCompressionLevel) {

        this.compressionLevel = Math.min(
            Math.max(newCompressionLevel, Deflater.BEST_SPEED),
            Math.min(newCompressionLevel, Deflater.BEST_COMPRESSION)
        );

        return this;
    }


    /***
     * use a file instead of an {@code InputStream} as source for the data to add to the ZIP archive.
     *
     * Using an {@code InputStream} has precedence over using a file.

     * @param fileToZip - the file to add to the ZIP
     * @throws IllegalArgumentException in case an {@code InputStream} has already been set with
     *     {@link #setInputStream(InputStream)}
     */
    public ZipEntry setFile(final File fileToZip) {
        if (this.getInputStream() != null) {
            throw new IllegalArgumentException("zip entry already has an input stream set as source of data");
        }
        this.file = fileToZip;
        return this;
    }


    /***
     * returns the file to use instead of an {@code InputStream} as source for the data.
     *
     * Using an {@code InputStream} has precedence over using a file.
     * @return the file to use as source of data or {@code null} in case no file has been set.
     */
    public File getFile() {
        return this.file;
    }


    /***
     * provide an optionally compressed {@code InputStream} to read the bytes from.
     *
     * The previously set stream (by {@link #setInputStream(InputStream)}) is wrapped with a compressing filter stream
     * to compress all the bytes are compressed on the fly. A {@link java.util.zip.Deflater} stream is used.
     * In case a file is used instead of an {@code InputStream}, the file is opened and a
     * {@link java.io.FileInputStream} is created and wrapped with the {@code Deflater} stream.
     */
    @SuppressWarnings("resource")
    public InputStream getStream() {

        if (this.compressedDataStream == null) {

            InputStream inData = this.getInputStream();
            if (inData == null && this.file != null) {
                try {
                    this.setInputStream(new FileInputStream(this.file));
                    inData = this.getInputStream();

                } catch (final FileNotFoundException e) {
                    throw new RuntimeException("failed to open file: " + this.file, e);
                }
            }

            if (inData != null) {
                final boolean useCompression = this.getMethod() == LocalFileHeaderBuilder.COMPRESSION_METHOD_DEFLATE;

                final DeflaterCheckedInputStream compressedStream =
                    new DeflaterCheckedInputStream(this.inStream, useCompression)
                ;
                this.compressedDataStream = new DeflaterDDInputStream(
                    compressedStream,
                    (checkedStream) -> {
                        this.setCrc(checkedStream.getCrc());
                        this.setCompressedSize(checkedStream.getCompressedSize());
                        this.setSize(checkedStream.getSize());
                    }
                );

            } else {
                throw new RuntimeException("Failed to set input stream");
            }

        }


        if (this.compressedDataStream == null) {
            throw new RuntimeException(
                "failed to acquire input data as stream. File available? " + (this.file != null ? this.file.getPath() : "NO")
            );
        }
        return this.compressedDataStream;
    }


    /***
     * rather than reading the data from a file, use that {@code InputStream} instead to read uncompressed bytes from.
     *
     * The provided {@code InputStream} has higher precedence than a provided file.
     */
    public ZipEntry setInputStream(final InputStream newInDataStream) {

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


    /***
     * returns the raw, uncompressed input stream that is available or {@code null} if a file is used as data.
     */
    public InputStream getInputStream() {
        return this.inStream;
    }
}
