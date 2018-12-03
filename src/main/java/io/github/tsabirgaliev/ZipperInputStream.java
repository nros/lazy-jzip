/*
 * Copyright 2017 Tair Sabyrgaliyev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.tsabirgaliev;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.NoSuchElementException;

import io.github.tsabirgaliev.zip.CentralDirectory;
import io.github.tsabirgaliev.zip.DeflaterDDInputStream;
import io.github.tsabirgaliev.zip.FileEntry;
import io.github.tsabirgaliev.zip.LocalFileHeader;
import io.github.tsabirgaliev.zip.ZipEntryData;

/**
 * ZipperInputStream lets you lazily provide file names and data streams
 * in spirit of java.util.zip.DeflaterInputStream.
 */
public class ZipperInputStream extends SequenceInputStream {

    public ZipperInputStream(final Enumeration<ZipEntryData> enumeration) throws IOException {
        super(new Enumeration<InputStream>() {
            List<FileEntry> fileEntries = new ArrayList<>();

            boolean cdProcessed = false;

            @Override
            public boolean hasMoreElements() {
                return !this.cdProcessed;
            }

            @Override
            public InputStream nextElement() {
                try {
                    if (enumeration.hasMoreElements()) {
                        final ZipEntryData zipEntryData = enumeration.nextElement();
                        final LocalFileHeader lfh = new LocalFileHeader(zipEntryData.getPath());
                        final ByteArrayInputStream lfhIn = new ByteArrayInputStream(lfh.getBytes());
                        final DeflaterDDInputStream dddIn = new DeflaterDDInputStream(zipEntryData.getStream(), dd -> {
                            this.fileEntries.add(new FileEntry(lfh, dd));
                        });

                        return new SequenceInputStream(Collections.enumeration(Arrays.asList(lfhIn, dddIn)));
                    } else if (!this.cdProcessed) {
                        this.cdProcessed = true;
                        final CentralDirectory cd = new CentralDirectory(this.fileEntries);
                        return new ByteArrayInputStream(cd.getBytes());
                    }
                } catch (final IOException e) {
                    throw new RuntimeException("Error processing zip entry data", e);
                }

                throw new NoSuchElementException("No more elements to produce!");
            }
        });
    }
}
