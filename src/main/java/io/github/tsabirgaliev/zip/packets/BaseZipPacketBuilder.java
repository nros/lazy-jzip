package io.github.tsabirgaliev.zip.packets;


public abstract class BaseZipPacketBuilder {

    protected byte[] convertLongToUInt16(final long i) {
        return new byte[] {
                (byte)(i >> 0),
                (byte)(i >> 8)
        };
    }

    protected byte[] convertLongToUInt32(final long i) {
        return new byte[] {
                (byte)(i >> 0),
                (byte)(i >> 8),
                (byte)(i >> 16),
                (byte)(i >> 24)
        };
    }
}
