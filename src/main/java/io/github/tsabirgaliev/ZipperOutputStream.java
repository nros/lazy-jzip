/*
 * Copyright 2017 Tair Sabyrgaliyev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.tsabirgaliev;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.SequenceInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.zip.Deflater;
import java.util.zip.ZipOutputStream;

import io.github.tsabirgaliev.zip.ZipEntry;
import io.github.tsabirgaliev.zip.ZipEntryData;
import io.github.tsabirgaliev.zip.io.CountingInputStream;
import io.github.tsabirgaliev.zip.io.ProxyInputStreamWithCloseListener;
import io.github.tsabirgaliev.zip.packets.CentralDirectoryBuilder;
import io.github.tsabirgaliev.zip.packets.DefaultProcessedZipEntry;
import io.github.tsabirgaliev.zip.packets.ProcessedZipEntry;

/**
 * ZipperOutputStream lets you lazily add data and files to a ZIP archive.
 * in spirit of java.util.zip.DeflaterInputStream.
 *
 * <p>
 * Only single ZIP file is supported with a single central directory. So no multiple spanned ZIP files are possible.
 * </p>
 *
 * <p>
 * This class implements the same methods as {@link java.util.zip.ZipOutputStream}. Hence it is a drop-in replacement.
 * Nevertheless its power can only be fully utilized with the additional functions provided.
 * </p>
 *
 * @see <a href="https://en.wikipedia.org/wiki/Zip_(file_format)"
 *     >https://en.wikipedia.org/wiki/Zip_(file_format)</a>
 * @see <a href="http://www.info-zip.org/doc/appnote-19970311-iz.zip"
 *     >http://www.info-zip.org/doc/appnote-19970311-iz.zip</a>
 * @see <a href="https://pkware.cachefly.net/webdocs/APPNOTE/APPNOTE-4.5.0.txt"
 *     >https://pkware.cachefly.net/webdocs/APPNOTE/APPNOTE-4.5.0.txt</a>
 * @see <a href="https://users.cs.jmu.edu/buchhofp/forensics/formats/pkzip.html"
 *     >https://users.cs.jmu.edu/buchhofp/forensics/formats/pkzip.html</a>
 */
public class ZipperOutputStream extends OutputStream {

    private ZipEntry currentEntry = null;
    private OutputStream temporaryFileForEntryBytes = null;

    private final List<ZipEntry> entriesToZip = new ArrayList<>();
    private final List<ProcessedZipEntry> fileEntries = new ArrayList<>();
    private boolean wasCentralDirectoryProvided = false;

    private int compressionLevel = Deflater.DEFAULT_COMPRESSION;
    private int compressionMethod = ZipOutputStream.DEFLATED;

    private InputStream createdInputStream = null;


    /**
     * creates a new instance, which will start to collect entries to the ZIP file right-away.
     */
    public ZipperOutputStream() {
        super();
        try {
            super.close();
        } catch (final IOException ignore) {
        }
    }


    /**
     * adds a new entry to the ZIP archive, not yet reading the data.
     *
     * @param addEntryToZip - the ZIP entry to be written
     * @return this instance to allow a <a href="https://en.wikipedia.org/wiki/Fluent_interface">fluent interface</a>
     * @throws IOException in case no more entries can be added because the central directory of the ZIP archive has
     *     already been created.
     */
    public ZipperOutputStream putNextEntry(final ZipEntryData addEntryToZip) throws IOException {
        if (addEntryToZip != null) {
            this.putNextEntry(new ZipEntry(addEntryToZip));
        }

        return this;
    }


    /**
     * adds a new entry to the ZIP archive, not yet reading the data.
     *
     * @param entryName - the path of the entry within the ZIP file.
     * @param entryFile - the file to read the data for this entry from.
     * @return this instance to allow a <a href="https://en.wikipedia.org/wiki/Fluent_interface">fluent interface</a>
     * @throws IOException in case no more entries can be added because the central directory of the ZIP archive has
     *     already been created.
     */
    public ZipperOutputStream putNextEntry(final String entryName, final File entryFile) throws IOException {
        if (entryName != null && entryFile != null && entryFile.exists() && entryFile.canRead()) {
            this.putNextEntry(new ZipEntry(entryName).setFile(entryFile));
        }

        return this;
    }


    /**
     * adds a new entry to the ZIP archive, not yet reading the data.
     *
     * @param entryName - the path of the entry within the ZIP file.
     * @param entryData - the input stream to read the data for this entry from.
     * @return this instance to allow a <a href="https://en.wikipedia.org/wiki/Fluent_interface">fluent interface</a>
     * @throws IOException in case no more entries can be added because the central directory of the ZIP archive has
     *     already been created.
     */
    public ZipperOutputStream putNextEntry(final String entryName, final InputStream entryData) throws IOException {
        this.putNextEntry(new ZipEntryData() {
            @Override
            public String getPath() {
                return entryName;
            }

            @Override
            public InputStream getStream() {
                return entryData;
            }
        });

        return this;
    }


    /**
     * Begins writing a new ZIP file entry and positions the stream to the start of the entry data.
     *
     * <p>
     * Closes the previous entry if still active.
     * The default compression method will be used if no compression method was specified for the entry, and the
     * current time will be used if the entry has no set modification time.
     * </p>
     *
     * <p>
     * No bytes of this entry are written to any ZIP yet. So any changes to the data in the passed-in entry will have
     * an effect, with the exception to the compression method and compression level.
     * Therefore the CRC32 checksum and file sizes will be calculated automatically by this class.
     * </p>
     *
     * @param addEntryToZip - the ZIP entry to be written. Preferable the instance is of class
     *     {@link io.github.tsabirgaliev.zip.ZipEntry}
     * @return this instance to allow a <a href="https://en.wikipedia.org/wiki/Fluent_interface">fluent interface</a>
     * @throws IOException in case no more entries can be added because the central directory of the ZIP archive has
     *     already been created.
     * @see java.util.zip.ZipOutputStream#putNextEntry(java.util.zip.ZipEntry)
     */
    public ZipperOutputStream putNextEntry(final java.util.zip.ZipEntry addEntryToZip) throws IOException {
        if (!this.canAddMoreZipEntry()) {
            throw new IOException("Cannot add more ZIP entries. central directory has already been written!");
        }

        this.closeEntry();

        ZipEntry entryToAdd = null;
        if (addEntryToZip != null && addEntryToZip instanceof ZipEntry) {
            entryToAdd = (ZipEntry)addEntryToZip;
            this.entriesToZip.add(entryToAdd);

        } else if (addEntryToZip != null) {

            entryToAdd = new ZipEntry(addEntryToZip);
            this.currentEntry = entryToAdd;
        }

        if (
            entryToAdd != null
                && entryToAdd.getMethod() != ZipOutputStream.DEFLATED
                && entryToAdd.getMethod() != ZipOutputStream.STORED
        ) {
            entryToAdd.setMethod(this.compressionMethod);
        }

        if (entryToAdd != null && this.compressionLevel >= 0) {
            entryToAdd.setCompressionLevel(this.compressionLevel);
        }

        return this;
    }


    /**
     * closes the current active entry and adds its data to the ZIP index.
     *
     * <p>
     * Although the current active entry is closed, its data is not yet consumed. This is done as
     * soon as you read data from this {@code InputStream} and all previous items in the ZIP have
     * already been consumed.
     * </p>
     *
     * <p>
     * The purpose of this method is to mimic the interface of {@link java.util.zip.ZipOutputStream}
     * </p>
     *
     * @return this instance to allow a <a href="https://en.wikipedia.org/wiki/Fluent_interface">fluent interface</a>
     * @see <a href="https://docs.oracle.com/javase/7/docs/api/java/util/zip/ZipOutputStream.html#closeEntry()"
     *     >https://docs.oracle.com/javase/7/docs/api/java/util/zip/ZipOutputStream.html#closeEntry()</a>
     * @throws IOException in case closing the temporary file stream failed - if any has been used.
     */
    public ZipperOutputStream closeEntry() throws IOException {
        if (this.currentEntry != null) {
            this.entriesToZip.add(this.currentEntry);
            this.currentEntry = null;
        }

        // close the temporary stream
        if (this.temporaryFileForEntryBytes != null) {
            this.temporaryFileForEntryBytes.close();
            this.temporaryFileForEntryBytes = null;
        }

        return this;
    }


    /**
     * Writes an array of bytes to the current active ZIP entry data.
     *
     * <p>
     * This method will block until all the bytes are written. Since the nature of this class is, to create the
     * ZIP on-the-fly, only when reading the bytes of the ZIP file, the data passed-in here is just written to
     * a temporary file. This temporary file will be used in the end to assemble the whole ZIP file with all its
     * entries.
     * </p>
     *
     * <p>
     * The purpose of this method is to mimic the interface of {@link java.util.zip.ZipOutputStream}
     * </p>
     *
     * @param bytes - the data to write to the ZIP entry. This parameter must not be {@code null}, although its length
     *     may be {@code 0}. Passing a {@code null} is regarded as unintended and thus handled as an exception and
     *     sever error. If you want to call this method but not write any data, pass-in an empty array.
     * @param offset - where to start in the array consuming and writing the bytes. If below {@code 0} or beyond the
     *     length of the array an {@link java.lang.IllegalArgumentException} is thrown.
     * @param length - the number of bytes of the array that are written. If below {@code 0}this is regarded as an
     *     and an {@link java.lang.IllegalArgumentException} is thrown. If the value exceeds the maximum available
     *     bytes - starting from the offset - then nothing is being written and the method returns without an error.
     *
     * @throws IOException - in case the temporary file or an {@link java.io.FileOutputStream} for this temporary
     *     file can not be created.
     * @see java.util.zip.ZipOutputStream
     */
    @Override
    public void write(final byte[] bytes, final int offset, final int length) throws IOException {

        if (bytes == null) {
            throw new NullPointerException("no data to write to the ZIP entry have been provided. Parameter is NULL");
        }

        if (length < 0) {
            throw new IllegalArgumentException("length of bytes to be written is invalid: " + length);

        } else if (length == 0) {
            return;
        }

        if (offset < 0 || offset >= bytes.length) {
            throw new IllegalArgumentException(
                "offset (" + offset +  ") is invalid to read from byte array with length " + bytes.length
            );
        }

        // do not read more than available bytes
        final int checkedLength = Math.min(length, bytes.length - offset);

        // write all bytes
        this.createTemporaryEntryStream().write(bytes, offset, checkedLength);
    }


    /**
     * Writes an array of bytes to the current active ZIP entry data.
     *
     * <p>
     * This method will block until all the bytes are written. Since the nature of this class is, to create the
     * ZIP on-the-fly, only when reading the bytes of the ZIP file, the data passed-in here is just written to
     * a temporary file. This temporary file will be used in the end to assemble the whole ZIP file with all its
     * entries.
     * </p>
     *
     * <p>
     * The purpose of this method is to mimic the interface of {@link java.util.zip.ZipOutputStream}
     * </p>
     *
     * @throws IOException - in case the temporary file or an {@link java.io.FileOutputStream} for this temporary
     *     file can not be created.
     * @see java.util.zip.ZipOutputStream#write(byte[])
     */
    @Override
    public void write(final byte[] bytes) throws IOException {
        if (bytes != null) {
            this.write(bytes, 0, bytes.length);
        }
    }


    /**
     * Writes an array of bytes to the current active ZIP entry data.
     *
     * <p>
     * This method will block until all the bytes are written. Since the nature of this class is, to create the
     * ZIP on-the-fly, only when reading the bytes of the ZIP file, the data passed-in here is just written to
     * a temporary file. This temporary file will be used in the end to assemble the whole ZIP file with all its
     * entries.
     * </p>
     *
     * <p>
     * The purpose of this method is to mimic the interface of {@link java.util.zip.ZipOutputStream}
     * </p>
     *
     * @throws IOException - in case the temporary file or an {@link java.io.FileOutputStream} for this temporary
     *     file can not be created.
     * @see <a href="https://docs.oracle.com/javase/7/docs/api/java/util/zip/ZipOutputStream.html#write()"
     *     >https://docs.oracle.com/javase/7/docs/api/java/util/zip/ZipOutputStream.html#write()</a>
     * @see java.util.zip.ZipOutputStream
     */
    @Override
    public void write(final int b) throws IOException {
        this.createTemporaryEntryStream().write(b);
    }


    /**
     * flushes the temporary output stream of the current active ZIP entry.
     *
     * <p>
     * Calling this method does not have any effect, if no call to {@link #write(byte[], int, int)},
     * {@link #write(byte[])} or {@link #write(int)} has been created yet.
     * </p>
     *
     * <p>
     * The purpose of this method is to mimic the interface of {@link java.util.zip.ZipOutputStream}
     * </p>
     *
     * @throws IOException - in case the temporary file or an {@link java.io.FileOutputStream} for this temporary
     *     file can not be flushed.
     */
    @Override
    public void flush() throws IOException {
        if (this.temporaryFileForEntryBytes != null) {
            this.temporaryFileForEntryBytes.flush();
        }
    }


    /**
     * does not perform any thing, it just mimics the {@link java.util.zip.ZipOutputStream} API.
     *
     * @return this instance to allow a <a href="https://en.wikipedia.org/wiki/Fluent_interface">fluent interface</a>
     * @throws IOException in case finishing this output stream fails - as nothing is being done at the moment,
     *     no exception is likely to be thrown.
     */
    public ZipperOutputStream finish() throws IOException {
        return this;
    }


    /**
     * writes the entry data utilizing an {@code OutputStream}.
     *
     * <p>
     * This is useful in cases where some data generator only accepts an output stream to write to.
     * An example would be {@code PGPEncryptedDataGenerator} of Bouncy Castle. The data generator is used to encrypt
     * something with PGP and the output is directly written to the stream. Stacking that togehter, it would be
     * possible to store an PGP encrypted value directly in the ZIP.
     * </p>
     *
     * <p>
     * The original {@link java.util.zip.ZipOutputStream} can be used as an output stream to achieve that. Since this
     * inherits from an {@code InputStream}, this intermediate function is needed.
     * </p>
     *
     * @return an output stream to write data directly to the entries' temporary storage. Later the data from this
     *     storage is read to add it to the ZIP archive.
     */
    @SuppressWarnings("resource")
    public OutputStream getEntryStream() {
        final ZipperOutputStream myself = this;
        return new OutputStream() {

            @Override
            public void write(final int b) throws IOException {
                myself.write(b);
            }

            @Override
            public void write(final byte[] b) throws IOException {
                myself.write(b);
            }

            @Override
            public void write(final byte[] b, final int off, final int len) throws IOException {
                myself.write(b, off, len);
            }

            @Override
            public void flush() throws IOException {
                myself.flush();
            }

            @Override
            public void close() throws IOException {
                myself.close();
            }
        };
    }


    /**
     * Sets the compression level for subsequent entries which are DEFLATED.
     *
     * <p>
     * The default setting is {@link java.util.zip.Deflater#DEFAULT_COMPRESSION}.
     * </p>
     *
     * <p>
     * This default will be used whenever the compression level is not specified for an individual ZIP file entry.
     * Whenever {@link #putNextEntry(java.util.zip.ZipEntry)} is called, the entry receives this level specified
     * here in case it does not set its own value.
     * </p>
     *
     * <p>
     * The purpose of this method is to mimic the interface of {@link java.util.zip.ZipOutputStream}
     * </p>
     *
     * @param newCompressionLevel - the new compression level to use. Its value must be (inclusive) between
     *     {@code 0} (see {@link java.util.zip.Deflater#BEST_SPEED}) and {@code 9}
     *     (see {@link java.util.zip.Deflater#BEST_COMPRESSION}}
     * @return this instance to allow a <a href="https://en.wikipedia.org/wiki/Fluent_interface">fluent interface</a>
     */
    public ZipperOutputStream setLevel(final int newCompressionLevel)  {

        this.compressionLevel = Math.min(
            Math.max(newCompressionLevel, Deflater.BEST_SPEED),
            Math.min(newCompressionLevel, Deflater.BEST_COMPRESSION)
        );

        return this;
    }


    /**
     * Sets the default compression method for subsequent entries.
     *
     * <p>
     * This default will be used whenever the compression method is not specified for an individual ZIP file entry, and
     * is initially set to DEFLATED. Whenever {@link #putNextEntry(java.util.zip.ZipEntry)}
     * is called, the entry receives the method specified here in case it does not set its own value.
     * </p>
     *
     * <p>
     * The purpose of this method is to mimic the interface of {@link java.util.zip.ZipOutputStream}
     * </p>
     *
     * @param method - the method to use for compression. At the moment the following values are suppored only:
     *     <ul>
     *         <li>0 ... store without compression, see {@link java.util.zip.ZipOutputStream#STORED}</li>
     *         <li>8 ... compress with deflate algorithm, see {@link java.util.zip.ZipOutputStream#DEFLATED}</li>
     *     </ul>
     * @return this instance to allow a <a href="https://en.wikipedia.org/wiki/Fluent_interface">fluent interface</a>
     * @throws IllegalArgumentException - in case the specified method is not one of
     *     {@link java.util.zip.ZipOutputStream#DEFLATED} or {@link java.util.zip.ZipOutputStream#STORED}
     */
    public ZipperOutputStream setMethod(final int method) {
        if (method != ZipOutputStream.DEFLATED && method != ZipOutputStream.STORED) {
            throw new IllegalArgumentException("Unsupported ZIP compression method specified: " + method);
        }
        this.compressionMethod = method;

        return this;
    }


    /**
     * does not perform any thing, it just mimics the {@link java.util.zip.ZipOutputStream} API.
     *
     * @param zipFileComment - the comment to use with the ZIP archive - just ignored at the moment.
     * @return this instance to allow a <a href="https://en.wikipedia.org/wiki/Fluent_interface">fluent interface</a>
     */
    public ZipperOutputStream setComment(final String zipFileComment)  {
        return this;
    }


    /**
     * calls {@link #closeEntry()} and releases all temporary resources.
     *
     * @throws IOException if the current active entry can not be closed.
     */
    @Override
    public void close() throws IOException {
        this.closeEntry();
    }


    /**
     * checks whether new entries can be added to the ZIP file.
     *
     * <p>
     * If the central directory of the ZIP file has already been created, then no more ZIP entries can
     * be added. Then this function will return {@code false}. Until then, more entries can be added,
     * even if previous ZIP entries have already been consumed.
     * </p>
     *
     * @return {@code true} in case the central directory (COD) of the ZIP file has not yet been created
     *     and {@code false} otherwise.
     */
    public boolean canAddMoreZipEntry() {
        return !this.wasCentralDirectoryProvided;
    }


    /**
     * returns an input stream containing the ZIP file.
     *
     * @return assemble a {@link java.io.SequenceInputStream} to deliver the whole ZIP archive as an input stream.
     *     the parts of the ZIP archive are enumerated by {@link #createEnumerationWrapper()}
     */
    public InputStream asInputStream() {
        if (this.createdInputStream == null) {
            this.createdInputStream = new SequenceInputStream(this.createEnumerationWrapper());
        }
        return this.createdInputStream;
    }


    /**
     * creates an enumeration wrapper to read all the collected ZIP entries as separate input streams.
     *
     * <p>
     * The enumeration is used to create a consecutive view/stream on all the bytes in the ZIP utilizing
     * {@link java.io.SequenceInputStream}. This mechanism enables this class to read the bytes of the files
     * to be put into the ZIP archive just when reading the bytes of the archive.
     * </p>
     *
     * @return the enumeration contains all parts of the ZIP archive as separate input streams. Just a wrapper is
     *     returned and not a fixed list. The list grows as more elements are added to the instance of
     *     {@code ZipperOutputStream}. After the central directory of the ZIP archive has been created, no more
     *     entries can be added.
     */
    protected Enumeration<InputStream> createEnumerationWrapper() {

        @SuppressWarnings("resource")
        final ZipperOutputStream myself = this;
        return new Enumeration<InputStream>() {

            private long localHeaderOffset = 0;

            @Override
            public boolean hasMoreElements() {
                return !myself.wasCentralDirectoryProvided;
            }

            @SuppressWarnings("resource")
            @Override
            public InputStream nextElement() {

                if (myself.entriesToZip.isEmpty() && myself.currentEntry != null) {
                    try {
                        myself.closeEntry();
                    } catch (final IOException ignore) {
                    }
                }

                if (!myself.entriesToZip.isEmpty()) {

                    final ZipEntry zipEntry = myself.entriesToZip.remove(0);
                    final ProcessedZipEntry entry = new DefaultProcessedZipEntry(zipEntry);
                    entry.setLocalFileHeaderOffset(this.localHeaderOffset);
                    myself.fileEntries.add(entry);

                    final List<InputStream> entryParts = Arrays.asList(
                        new ByteArrayInputStream(entry.getLocalFileHeader()),
                        zipEntry.getStream(),
                        entry.getDataDescriptorPacketStream()
                    );

                    return new ProxyInputStreamWithCloseListener<CountingInputStream>(
                        new CountingInputStream(
                            new SequenceInputStream(Collections.enumeration(entryParts))
                        )
                    ).addCloseListener((countingStream) -> this.localHeaderOffset += countingStream.getByteCount());

                } else if (!myself.wasCentralDirectoryProvided) {

                    myself.wasCentralDirectoryProvided = true;
                    return new ByteArrayInputStream(new CentralDirectoryBuilder().getBytes(myself.fileEntries));
                }

                throw new NoSuchElementException("No more elements to produce!");
            }
        };
    }


    /**
     * if necessary, creates a temporary file and opens a {@link java.io.FileOutputStream} to the file.
     *
     * <p>
     * The output stream to the temporary file is returned.
     * </p>
     *
     * @return create an output stream to the temporary file of the current active zip entry.
     * @throws IPEception if there is no current entry active or if this entry already has a file or an input stream
     *     attached as data source.
     */
    private OutputStream createTemporaryEntryStream() throws IOException {

        if (this.currentEntry == null) {
            throw new IOException("no entry to write to has been created yet");
        }

        // create a new temporary file
        if (this.temporaryFileForEntryBytes == null) {

            if (this.currentEntry.getInputStream() != null || this.currentEntry.getFile() != null) {
                throw new IOException("the entry already has data attached and no more can be added!");
            }


            final File temporaryFile = File.createTempFile("ZipEntry", ".bin");
            temporaryFile.deleteOnExit();
            this.currentEntry.setTemporaryFile(temporaryFile);
            this.temporaryFileForEntryBytes = new FileOutputStream(temporaryFile);
        }

        return this.temporaryFileForEntryBytes;
    }
}
