package io.github.tsabirgaliev.zip.packets;

import java.io.InputStream;

import io.github.tsabirgaliev.zip.ZipEntry;

/**
 * While processing a ZIP entry, various ZIP data blocks (packets) are created, which are kept for later user.
 *
 * <p>
 * This instance provides access to these ZIP packets, with the except to the data bytes.
 * The created data packets might be referred to in later steps, so all the packets are cached in this entity,
 * until the ZIP file has been created.
 * </p>
 *
 * <p>
 * Each of these packets are created on the fly and cached internally for later re-use.
 * </p>
 *
 * @see <a href="https://users.cs.jmu.edu/buchhofp/forensics/formats/pkzip.html"
 *     >https://users.cs.jmu.edu/buchhofp/forensics/formats/pkzip.html</a>
 */
public interface ProcessedZipEntry {


    /**
     * return the meta information data instance, releated to this entry.
     *
     * @return the meta information for the entry, stored in {@code ZipEntry}
     */
    public ZipEntry getZipEntry();

    /**
     * creates and caches the data descriptor packet by calling the {@code DataDescriptorBuilder}.
     *
     * @return the bytes of the optional data descriptor packet as created by
     *     {@link io.github.tsabirgaliev.zip.packets.DataDescriptorBuilder}.
     * @see io.github.tsabirgaliev.zip.packets.DataDescriptorBuilder
     */
    public byte[] getDataDescriptor();

    /**
     * creates and caches the local file header packet by calling the {@code LocalFileHeaderBuilder}.
     *
     * @return the bytes of the local file header packet as created by
     *     {@link io.github.tsabirgaliev.zip.packets.LocalFileHeaderBuilder}.
     * @see io.github.tsabirgaliev.zip.packets.LocalFileHeaderBuilder
     */
    public byte[] getLocalFileHeader();

    /**
     * creates and caches the file header packet of the central directory, calling {@code DirectoryFileHeaderBuilder}.
     *
     * @return the bytes of the file header packet of the central directory as created by
     *    {@link io.github.tsabirgaliev.zip.packets.DirectoryFileHeaderBuilder}.
     * @see io.github.tsabirgaliev.zip.packets.DirectoryFileHeaderBuilder
     */
    public byte[] getDirectoryFileHeader();

    /**
     * returns the stream to read the data descriptor from.
     *
     * <p>
     * The data descriptor packet is only created as soon as the first byte is being read from this stream,
     * in case it has not been created yet.
     * Using this stream instead of the bytes as returned by {@link #getDataDescriptor()} ensures, that the data
     * descriptor is created late in the process, expecting to be created after processing the entry data. Only
     * then the exact sizes of the compressed/uncompressed data and the checksum are known.
     * </p>
     *
     * @return a proxied input stream that creates the real input stream just when the first byte is reqd.
     */
    public InputStream getDataDescriptorPacketStream();

    /**
     * returns the offset of the local file header within the ZIP file.
     *
     * @return the offset of the local file header within the ZIP file. It is related to the starting byte of the ZIP
     *     archive file.
     * @throws RuntimeException - if the offset has not been set yet and is thus unknown.
     */
    public long getLocalFileHeaderOffset();

    /**
     * sets the offset of the local file header within the ZIP file.
     *
     * @param localFileHeaderOffset - the offset of this ZIP entry's local file header within the ZIP archive.
     *     The offset is related to the starting byte of the ZIP file.
     * @return this instance to allow a <a href="https://en.wikipedia.org/wiki/Fluent_interface">fluent interface</a>
     */
    public ProcessedZipEntry setLocalFileHeaderOffset(final long localFileHeaderOffset);
}

