package io.github.tsabirgaliev.lazyzip;


public abstract class CentralDirectoryUtilities {

    static byte[] bytes2(final long i) {
        return new byte[] {
                (byte)(i >> 0),
                (byte)(i >> 8)
        };
    }

    static byte[] bytes4(final long i) {
        return new byte[] {
                (byte)(i >> 0),
                (byte)(i >> 8),
                (byte)(i >> 16),
                (byte)(i >> 24)
        };
    }


}
