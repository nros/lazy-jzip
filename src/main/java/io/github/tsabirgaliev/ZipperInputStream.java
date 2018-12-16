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

import io.github.tsabirgaliev.zip.ZipEntryData;
import io.github.tsabirgaliev.zip.ZipEntryDataWithCachedPackets;
import io.github.tsabirgaliev.zip.ZipEntryDataWithCachedPacketsImpl;
import io.github.tsabirgaliev.zip.packets.CentralDirectoryBuilder;

/**
 * ZipperInputStream lets you lazily provide file names and data streams
 * in spirit of java.util.zip.DeflaterInputStream.
 *
 * the ZIP format created mimics the version by Java and should be compatible. Only single ZIP file is supported
 * with a single central directory. So no multiple spanned ZIP files are created.
 *
 * @author Tair Sabirgaliev <tair.sabirgaliev@gmail.com>
 * @author nros <508093+nros@users.noreply.github.com>
 *
 * @see https://en.wikipedia.org/wiki/Zip_(file_format)
 * @see http://www.info-zip.org/doc/appnote-19970311-iz.zip
 * @see https://pkware.cachefly.net/webdocs/APPNOTE/APPNOTE-4.5.0.txt
 * @see https://users.cs.jmu.edu/buchhofp/forensics/formats/pkzip.html
 */
public class ZipperInputStream extends SequenceInputStream {

    private final static CentralDirectoryBuilder centralDirectoryBuilder = new CentralDirectoryBuilder();

    public ZipperInputStream(final Enumeration<ZipEntryData> enumeration) throws IOException {

        super(new Enumeration<InputStream>() {
            final List<ZipEntryDataWithCachedPackets> fileEntries = new ArrayList<>();

            boolean wasCentralDirectoryProvided = false;

            @Override
            public boolean hasMoreElements() {
                return !this.wasCentralDirectoryProvided;
            }

            @Override
            public InputStream nextElement() {

                if (enumeration.hasMoreElements()) {

                    final ZipEntryData zipEntryData = enumeration.nextElement();
                    final ZipEntryDataWithCachedPackets entry = new ZipEntryDataWithCachedPacketsImpl(zipEntryData);
                    this.fileEntries.add(entry);

                    return new SequenceInputStream(Collections.enumeration(Arrays.asList(
                        new ByteArrayInputStream(entry.getLocalFileHeader()),
                        entry.getStream(),
                        entry.getDataDescriptorPacketStream()
                    )));

                } else if (!this.wasCentralDirectoryProvided) {

                    this.wasCentralDirectoryProvided = true;
                    return new ByteArrayInputStream(ZipperInputStream.centralDirectoryBuilder.getBytes(this.fileEntries));
                }

                throw new NoSuchElementException("No more elements to produce!");
            }
        });
    }
}
