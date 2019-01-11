package io.github.tsabirgaliev.zip;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


@DisplayName("Test ZipperOutputStream")
public class TestZipperOutputStream {

    private static final String ZIP_ENTRY_NAME_PREFIX = "/data";


    @Test
    public void testJdkCompatibility() throws IOException {


        final Random random = new Random();
        final int numberOfTestCases = 6;
        final int maxBufferSize = 100;
        final List<byte[]> testCases = new ArrayList<>();

        final ZipperOutputStream zip = new ZipperOutputStream();
        for (int i = 0; i < numberOfTestCases; i++) {
            final byte[] data = new byte[random.nextInt(maxBufferSize)];
            random.nextBytes(data);
            testCases.add(data);

            zip.putNextEntry(new java.util.zip.ZipEntry(TestZipperOutputStream.ZIP_ENTRY_NAME_PREFIX + i));
            zip.write(data);
            zip.closeEntry();
        }

        zip.close();

        final ZipInputStream zipReader = new ZipInputStream(zip.asInputStream());

        int i = 0;
        ZipEntry zipEntry = zipReader.getNextEntry();
        while (zipEntry != null) {

            Assertions.assertEquals(
                TestZipperOutputStream.ZIP_ENTRY_NAME_PREFIX + i,
                zipEntry.getName(),
                "zip entry name is different from expected name"
            );

            Assertions.assertTrue(
                IOUtils.contentEquals(zipReader, new ByteArrayInputStream(testCases.get(i))),
                "stored content is different than expected for item " + TestZipperOutputStream.ZIP_ENTRY_NAME_PREFIX + i
            );

            zipReader.closeEntry();
            zipEntry = zipReader.getNextEntry();
            i++;
        }

        zipReader.close();
    }


    @Test
    public void testWithFileAndInputStream() throws IOException {

        final Random random = new Random();
        final int numberOfTestCases = 3;
        final int maxBufferSize = 100;
        final List<byte[]> testCases = new ArrayList<>();

        final ZipperOutputStream zip = new ZipperOutputStream();
        for (int i = 0; i < numberOfTestCases; i++) {
            final byte[] data = new byte[random.nextInt(maxBufferSize)];
            random.nextBytes(data);
            testCases.add(data);

            zip.putNextEntry(
                new io.github.tsabirgaliev.zip.ZipEntry(TestZipperOutputStream.ZIP_ENTRY_NAME_PREFIX + i)
                    .setInputStream(new ByteArrayInputStream(data))
            );
        }

        zip.close();

        final ZipInputStream zipReader = new ZipInputStream(zip.asInputStream());

        int i = 0;
        ZipEntry zipEntry = zipReader.getNextEntry();
        while (zipEntry != null) {

            Assertions.assertEquals(
                TestZipperOutputStream.ZIP_ENTRY_NAME_PREFIX + i,
                zipEntry.getName(),
                "The entry name was not used with this ZIP"
            );

            Assertions.assertTrue(
                IOUtils.contentEquals(zipReader, new ByteArrayInputStream(testCases.get(i))),
                "stored content is wrong for item " + TestZipperOutputStream.ZIP_ENTRY_NAME_PREFIX + i
            );

            zipReader.closeEntry();
            zipEntry = zipReader.getNextEntry();
            i++;
        }

        zipReader.close();
    }


    @Test
    public void testWithEnumerationEntry() throws IOException {

        final Random random = new Random();
        final int numberOfTestCases = 15;
        final int maxBufferSize = 100;
        final List<byte[]> testCases = new ArrayList<>();
        final List<ZipEntryData> enumerationTestCases = new ArrayList<>();

        final ZipperOutputStream zip = new ZipperOutputStream();
        for (int i = 0; i <= numberOfTestCases; i++) {
            final byte[] data = new byte[random.nextInt(maxBufferSize)];
            random.nextBytes(data);

            testCases.add(data);
            if (i < numberOfTestCases / 2) {
                zip.putNextEntry(
                    new io.github.tsabirgaliev.zip.ZipEntry(TestZipperOutputStream.ZIP_ENTRY_NAME_PREFIX + i)
                        .setInputStream(new ByteArrayInputStream(data))
                );
            } else if (i == numberOfTestCases) {

                zip.putMultipleNextEntries(new Enumeration<ZipEntryData>() {

                    @Override
                    public boolean hasMoreElements() {
                        return !enumerationTestCases.isEmpty();
                    }

                    @Override
                    public ZipEntryData nextElement() {
                        return enumerationTestCases.remove(0);
                    }

                });

                zip.putNextEntry(
                    new io.github.tsabirgaliev.zip.ZipEntry(TestZipperOutputStream.ZIP_ENTRY_NAME_PREFIX + i)
                        .setInputStream(new ByteArrayInputStream(data))
                );

            } else {

                final int entryIndex = i;
                final String entryPath = TestZipperOutputStream.ZIP_ENTRY_NAME_PREFIX + i;
                enumerationTestCases.add(
                    new ZipEntryData() {

                        @Override
                        public String getPath() {
                            return entryPath;
                        }

                        @Override
                        public InputStream getStream() {
                            return new ByteArrayInputStream(testCases.get(entryIndex));
                        }
                    }
                );
            }
        }


        zip.close();

        final ZipInputStream zipReader = new ZipInputStream(zip.asInputStream());

        int i = 0;
        ZipEntry zipEntry = zipReader.getNextEntry();
        while (zipEntry != null) {

            Assertions.assertEquals(
                TestZipperOutputStream.ZIP_ENTRY_NAME_PREFIX + i,
                zipEntry.getName(),
                "The entry name can not be found in the ZIP"
            );

            Assertions.assertTrue(
                IOUtils.contentEquals(zipReader, new ByteArrayInputStream(testCases.get(i))),
                "stored content in ZIP is wrong for item " + TestZipperOutputStream.ZIP_ENTRY_NAME_PREFIX + i
            );

            zipReader.closeEntry();
            zipEntry = zipReader.getNextEntry();
            i++;
        }

        zipReader.close();
    }

}
