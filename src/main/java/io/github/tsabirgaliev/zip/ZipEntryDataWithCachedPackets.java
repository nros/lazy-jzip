package io.github.tsabirgaliev.zip;

import java.io.InputStream;

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
public interface ZipEntryDataWithCachedPackets extends ZipEntryData {

    /***
     * returns the bytes of the optional data descriptor packet as created by
     * {@link io.github.tsabirgaliev.zip.packets.DataDescriptorBuilder}
     */
    public byte[] getDataDescriptor();

    /***
     * returns the local file header packet as created by {@link io.github.tsabirgaliev.zip.packets.LocalFileHeaderBuilder}
     */
    public byte[] getLocalFileHeader();

    /***
     * returns the file header packet of the central directory as created by
     * {@link io.github.tsabirgaliev.zip.packets.LocalFileHeaderBuilder}
     */
    public byte[] getDirectoryFileHeader();

    /***
     * returns the stream to read the data descriptor from.
     * The data descriptor packet is only created as soon as the first byte is being read from this stream,
     * in case it has not been created yet.
     */
    public InputStream getDataDescriptorPacketStream();

    /***
     * returns the offset of the local file header within the ZIP file.
     */
    public long getLocalFileHeaderOffset();

    /***
     * sets the offset of the local file header within the ZIP file.
     */
    public ZipEntryDataWithCachedPackets setLocalFileHeaderOffset(final long localFileHeaderOffset);
}

