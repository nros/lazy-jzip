package io.github.tsabirgaliev.lazyzip;

import java.io.InputStream;

public interface ZipEntryData {
    String getPath();
    InputStream getStream();
}

