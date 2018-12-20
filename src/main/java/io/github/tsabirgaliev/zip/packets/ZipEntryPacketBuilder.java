package io.github.tsabirgaliev.zip.packets;

/***
 * implementing classes create various ZIP packets for entries as the ZIP file is organized in packets.
 *
 * @author nros <508093+nros@users.noreply.github.com>
 */
public interface ZipEntryPacketBuilder {

    /**
     * create the bytes of this packet for the current ZIP entry.
     * @return the bytes for this packet or {@code null}
     */
    public byte[] getBytes(final ProcessedZipEntry zipEntry);
}
