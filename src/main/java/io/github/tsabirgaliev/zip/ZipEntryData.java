package io.github.tsabirgaliev.zip;

import java.io.InputStream;
import java.util.zip.ZipEntry;

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
     * returns the path to the file to be added to the ZIP.
     *
     * Either this is an absolute path or relative to the current working directory. This package will not take care
     * of setting a working directory but use the current application settings.
     *
     * If {@code null} is returned, then ZIP entry information must be provided by {@link #getZipEntry()}
     */
    public String getPath();

    /***
     * rather than opening the file directly, this input stream is used to read the file bytes.
     */
    public InputStream getStream();


    /***
     * (optionally) provide more detailed information about the ZIP entry.
     * In case {@code null} is returned, such information is created based on the file read from the path fetched from
     * {@link #getPath()}
     *
     * @return {@code null} or detailed information about the ZIP entry.
     */
    public ZipEntry getZipEntry();
}

