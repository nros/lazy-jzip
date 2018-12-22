package io.github.tsabirgaliev.zip.packets;

/**
 * implementing classes create various ZIP packets for entries as the ZIP file is organized in packets.
 */
public interface ZipEntryPacketBuilder {

    /**
     * create the bytes of this packet for the current ZIP entry.
     *
     * @param zipEntry the ZIP entry to process to create the bytes of this part of the ZIP archive.
     * @return the bytes for this packet or {@code null}
     */
    public byte[] getBytes(final ProcessedZipEntry zipEntry);
}
