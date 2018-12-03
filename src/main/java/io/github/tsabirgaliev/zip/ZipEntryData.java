package io.github.tsabirgaliev.zip;

import java.io.InputStream;

public interface ZipEntryData {
    String getPath();
    InputStream getStream();
}

