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

package io.github.tsabirgaliev.zip;

import java.io.IOException;
import java.util.Enumeration;

import io.github.tsabirgaliev.zip.io.ProxyInputStream;

/**
 * ZipperInputStream lets you lazily provide file names and data streams.
 * in spirit of java.util.zip.DeflaterInputStream.
 *
 * <p>
 * the ZIP format created mimics the version by Java and should be compatible. Only single ZIP file is supported
 * with a single central directory. So no multiple spanned ZIP files are created.
 * </p>
 *
 * @see <a href="https://en.wikipedia.org/wiki/Zip_(file_format)"
 *     >https://en.wikipedia.org/wiki/Zip_(file_format)</a>
 * @see <a href="http://www.info-zip.org/doc/appnote-19970311-iz.zip"
 *     >http://www.info-zip.org/doc/appnote-19970311-iz.zip</a>
 * @see <a href="https://pkware.cachefly.net/webdocs/APPNOTE/APPNOTE-4.5.0.txt"
 *     >https://pkware.cachefly.net/webdocs/APPNOTE/APPNOTE-4.5.0.txt</a>
 * @see <a href="https://users.cs.jmu.edu/buchhofp/forensics/formats/pkzip.html"
 *     >https://users.cs.jmu.edu/buchhofp/forensics/formats/pkzip.html</a>
 */
public class ZipperInputStream extends ProxyInputStream {

    /**
     * create a new ZIP input stream from the list (enumeration) of entries to add to the ZIP archive.
     *
     * @param zipEntries - all the entries to add to the ZIP archive
     * @throws IOException in case the internally used {@link ZipperOutputStream} throws an exception.
     */
    public ZipperInputStream(final Enumeration<ZipEntryData> zipEntries) throws IOException {
        super();
        final ZipperOutputStream zip = new ZipperOutputStream();
        zip.putMultipleNextEntries(zipEntries);

        zip.close();
        this.setInputStream(zip.asInputStream());
    }
}

