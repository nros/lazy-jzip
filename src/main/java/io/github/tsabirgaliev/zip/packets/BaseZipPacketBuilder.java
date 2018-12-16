package io.github.tsabirgaliev.zip.packets;

import java.nio.file.attribute.FileTime;
import java.util.Calendar;
import java.util.TimeZone;

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


    protected byte[] convertFileTimeToZipTime(final FileTime fileTime) {

        final Calendar fileDate = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        if (fileTime != null) {
            fileDate.setTimeInMillis(fileTime.toMillis());
        }

        final int result = (fileDate.get(Calendar.HOUR_OF_DAY) << 11)
                   | (fileDate.get(Calendar.MINUTE)      << 5)
                   | (fileDate.get(Calendar.SECOND)      / 2);

        return this.convertLongToUInt16(result);
    }

    protected byte[] convertFileTimeToZipDate(final FileTime fileTime) {

        final Calendar fileDate = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        if (fileTime != null) {
            fileDate.setTimeInMillis(fileTime.toMillis());
        }

        final int result = ((fileDate.get(Calendar.YEAR) - 1980) << 9)
                   | ((fileDate.get(Calendar.MONTH) + 1)   << 5)
                   | fileDate.get(Calendar.DATE);

        return this.convertLongToUInt16(result);
    }
}
