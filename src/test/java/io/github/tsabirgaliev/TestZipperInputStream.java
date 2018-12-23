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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.github.tsabirgaliev.zip.ZipEntryData;

@DisplayName("Test ZipperInputStream")
public class TestZipperInputStream {

    private final byte[] file1data = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa".getBytes();
    private final byte[] file2data = "other bytes".getBytes();

    private final ZipEntryData file1 = new ZipEntryData() {
        @Override
        public String getPath() {
            return "file1";
        }

        @Override
        public InputStream getStream() {
            return new ByteArrayInputStream(TestZipperInputStream.this.file1data);
        }
    };

    private final ZipEntryData file2 = new ZipEntryData() {
        @Override
        public String getPath() {
            return "folder1/file2";
        }

        @Override
        public InputStream getStream() {
            return new ByteArrayInputStream(TestZipperInputStream.this.file2data);
        }
    };

    private static Enumeration<ZipEntryData> enumerate(final ZipEntryData... files) {
        return Collections.enumeration(Arrays.asList(files));
    }

    @Test
    public void testJdkCompatibility() throws IOException {
        final ZipperInputStream lzis = new ZipperInputStream(TestZipperInputStream.enumerate(this.file1, this.file2));

        final ZipInputStream zis = new ZipInputStream(lzis);

        final ZipEntry entry1 = zis.getNextEntry();
        assert this.file1.getPath().equals(entry1.getName());
        assert IOUtils.contentEquals(zis, this.file1.getStream());
        zis.closeEntry();

        final ZipEntry entry2 = zis.getNextEntry();
        assert this.file2.getPath().equals(entry2.getName());
        assert IOUtils.contentEquals(zis, this.file2.getStream());
        zis.closeEntry();

        zis.close();
    }

    @Test
    public void testFileSystemOutput() throws IOException {
        final ZipperInputStream lzis = new ZipperInputStream(TestZipperInputStream.enumerate(this.file1, this.file2));

        Files.copy(lzis, Paths.get("target", "output.zip"), StandardCopyOption.REPLACE_EXISTING);
        lzis.close();
    }
}
