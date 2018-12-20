package io.github.tsabirgaliev.zip;

import java.io.InputStream;

/***
 * provide information about the file to be zipped by this package.
 *
 * ZIP entries need some information about the entry. Such information may be optionally provided by
 * {@link #getZipEntry()} or will created from the provided path of the file {@link #getPath()}.
 * So one of these two functions must return valid information or the data in the stream can not be added to the
 * ZIP archive and will be ignored.
 *
 * In case you would like to ZIP data from the memory, then you only need to provide the data via {@link #getStream()}
 * and proper ZIP entry meta data via {@link #getZipEntry()}. Then you may omit the file path and make
 * {@link #getPath()} to return {@code null}
 *
 * @author Tair Sabirgaliev <tair.sabirgaliev@gmail.com>
 * @author nros <508093+nros@users.noreply.github.com>
 */
public interface ZipEntryData {

    /***
     * returns the path of the entry within the ZIP file, which should be absolute from the root of the ZIP's directory
     */
    public String getPath();


    /***
     * provide an {@code InputStream} to read the uncompressed bytes from.
     */
    public InputStream getStream();
}

