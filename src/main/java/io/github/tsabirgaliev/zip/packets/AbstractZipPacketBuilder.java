package io.github.tsabirgaliev.zip.packets;

import java.nio.file.attribute.FileTime;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * this abstract base class for packet builders for the ZIP archive provides utility functions for packet building.
 */
public abstract class AbstractZipPacketBuilder {

    private static final int SHIFT_0_BYTE = 0;
    private static final int SHIFT_1_BYTE = 8;
    private static final int SHIFT_2_BYTES = AbstractZipPacketBuilder.SHIFT_1_BYTE * 2;
    private static final int SHIFT_3_BYTES = AbstractZipPacketBuilder.SHIFT_1_BYTE * 3;

    private static final String TIMEZONE_NAME_UTC = "UTC";

    private static final int BASE_YEAR_1980 = 1980;
    private static final int SHIFT__BITS_OF_DATE = 5;
    private static final int SHIFT__BITS_OF_MONTH = AbstractZipPacketBuilder.SHIFT__BITS_OF_DATE + 4;


    private static final int SHIFT__BITS_OF_SECONDS = 5;
    private static final int SHIFT__BITS_OF_MINUTES = AbstractZipPacketBuilder.SHIFT__BITS_OF_SECONDS + 6;



    /**
     * converts an integer to the byte representation of an unsigned integer with 16 bits in little endian.
     *
     * @param i - the integer value to convert to byte representation. Values higher than 65536 are just
     *     ignored. All values are truncated.
     * @return two bytes, representing unsigned integer with 16 bits
     */
    protected byte[] convertLongToUInt16(final long i) {
        return new byte[] {
            (byte)(i >> AbstractZipPacketBuilder.SHIFT_0_BYTE),
            (byte)(i >> AbstractZipPacketBuilder.SHIFT_1_BYTE),
        };
    }


    /**
     * converts a long value to the byte representation of an unsigned integer with 32 bits in little endian.
     *
     * @param i - the value to convert to byte representation. Values higher than 4294967296 are just
     *     ignored. All values are truncated.
     * @return four bytes, representing unsigned integer with 32 bits in little endian
     */
    protected byte[] convertLongToUInt32(final long i) {
        return new byte[] {
            (byte)(i >> AbstractZipPacketBuilder.SHIFT_0_BYTE),
            (byte)(i >> AbstractZipPacketBuilder.SHIFT_1_BYTE),
            (byte)(i >> AbstractZipPacketBuilder.SHIFT_2_BYTES),
            (byte)(i >> AbstractZipPacketBuilder.SHIFT_3_BYTES),
        };
    }

    /**
     * converts a file time into the byte representation of time within ZIP archive file.
     *
     * <p>
     * Date and times are represented with two bytes (in little endian) and are encoded in standard MS-DOS format.
     * the time is encoded with the following bits:
     * </p>
     *   <ul>
     *     <li>Bits 0 - 4:  seconds / 2 - with values from 0 to 29</li>
     *     <li>Bits 5 - 10:  minutes - with values from 0 to 59</li>
     *     <li>Bits 11 - 15:  hours - with values from 0 to 23</li>
     *   </ul>
     *
     * @param fileTime - the date and time to be encoded
     * @return two bytes representing the time of the {@code FileTime}, encoded is MS-DOS format.
     * @see <a href="https://pkware.cachefly.net/webdocs/APPNOTE/APPNOTE-4.5.0.txt"
     *     >https://pkware.cachefly.net/webdocs/APPNOTE/APPNOTE-4.5.0.txt</a>
     * @see <a href="http://www.sandersonforensics.com/forum/content.php?131-A-brief-history-of-time-stamps"
     *     >http://www.sandersonforensics.com/forum/content.php?131-A-brief-history-of-time-stamps</a>
     */
    protected byte[] convertFileTimeToZipTime(final FileTime fileTime) {

        final Calendar fileDate =
            Calendar.getInstance(TimeZone.getTimeZone(AbstractZipPacketBuilder.TIMEZONE_NAME_UTC));
        if (fileTime != null) {
            fileDate.setTimeInMillis(fileTime.toMillis());
        }

        final int result = (
            fileDate.get(Calendar.HOUR_OF_DAY) << AbstractZipPacketBuilder.SHIFT__BITS_OF_MINUTES)
            | (fileDate.get(Calendar.MINUTE) << AbstractZipPacketBuilder.SHIFT__BITS_OF_SECONDS)
            | (fileDate.get(Calendar.SECOND) / 2
        );

        return this.convertLongToUInt16(result);
    }


    /**
     * converts a file time into the byte representation of a date within ZIP archive file.
     *
     * <p>
     * Date and times are represented with two bytes (in little endian) and are encoded in standard MS-DOS format.
     * the date is encoded with the following bits:
     * </p>
     *   <ul>
     *     <li>Bits 0 - 4:  day - with values from 1 to 31</li>
     *     <li>Bits 5 - 8:  month - with values from 1 to 12</li>
     *     <li>Bits 9 - 15:  year - with values from 0 to 127</li>
     *   </ul>
     * <p>
     * The epoch for a MS-DOS date is 1980, thus you need to add {@code 1980} to the stored year value to calculate
     * the real year. Hence the maximum year in MS-DOS format is 2107.
     * </p>
     *
     *
     * @param fileTime - the date and time to be encoded
     * @return two bytes representing the date of the {@code FileTime}, encoded is MS-DOS format.
     * @see <a href="https://pkware.cachefly.net/webdocs/APPNOTE/APPNOTE-4.5.0.txt"
     *     >https://pkware.cachefly.net/webdocs/APPNOTE/APPNOTE-4.5.0.txt</a>
     * @see <a href="http://www.sandersonforensics.com/forum/content.php?131-A-brief-history-of-time-stamps"
     *     >http://www.sandersonforensics.com/forum/content.php?131-A-brief-history-of-time-stamps</a>
     */
    protected byte[] convertFileTimeToZipDate(final FileTime fileTime) {

        final Calendar fileDate =
            Calendar.getInstance(TimeZone.getTimeZone(AbstractZipPacketBuilder.TIMEZONE_NAME_UTC));
        if (fileTime != null) {
            fileDate.setTimeInMillis(fileTime.toMillis());
        }

        final int result =
            (
                (
                    fileDate.get(Calendar.YEAR) - AbstractZipPacketBuilder.BASE_YEAR_1980
                ) << AbstractZipPacketBuilder.SHIFT__BITS_OF_MONTH
            )
            | ((fileDate.get(Calendar.MONTH) + 1) << AbstractZipPacketBuilder.SHIFT__BITS_OF_DATE)
            | fileDate.get(Calendar.DATE);

        return this.convertLongToUInt16(result);
    }
}
