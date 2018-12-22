package io.github.tsabirgaliev.zip;

import java.io.InputStream;

/**
 * provide information about the file to be zipped by this package.
 *
 * <p>
 * ZIP entries need some information about the entry.
 * The path of the entry within the ZIP directory is denoted by {@link #getPath()}.
 * The uncompressed content of the entry can be read from {@link #getStream()}
 * </p>
 */
public interface ZipEntryData {

    /**
     * returns the path of the entry within the ZIP file, which should be absolute from the root of the ZIP directory.
     *
     * @return the path to use. must not return {@code null} as the entry can not be added with an invalid name.
     */
    public String getPath();


    /**
     * provide an {@code InputStream} to read the uncompressed bytes from.
     *
     * @return stream to read the uncompressed data from. Must not return {@code null}.
     */
    public InputStream getStream();
}

