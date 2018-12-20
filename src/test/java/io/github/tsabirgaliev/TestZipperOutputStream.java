package io.github.tsabirgaliev;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestZipperOutputStream {


    @Test
    public void testJDKCompatibility() throws IOException {

        final Random random = new Random();
        final int numberOfTestCases = 6;
        final List<byte[]> testCases = new ArrayList<>();

        final ZipperOutputStream zip = new ZipperOutputStream();
        for (int i = 0; i < numberOfTestCases; i++) {
            final byte[] data = new byte[random.nextInt(100)];
            random.nextBytes(data);
            testCases.add(data);

            zip.putNextEntry(new java.util.zip.ZipEntry("data" + i));
            zip.write(data);
            zip.closeEntry();
        }

        zip.close();

        final ZipInputStream zipReader = new ZipInputStream(zip.asInputStream());

        int i = 0;
        ZipEntry zipEntry = zipReader.getNextEntry();
        while (zipEntry != null) {

            Assertions.assertEquals(
                "data" + i,
                zipEntry.getName(),
                "zip entry name is different from expected name"
            );

            Assertions.assertTrue(
                IOUtils.contentEquals(zipReader, new ByteArrayInputStream(testCases.get(i))),
                "stored content is different than expected for item " + i
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
        final List<byte[]> testCases = new ArrayList<>();

        final ZipperOutputStream zip = new ZipperOutputStream();
        for (int i = 0; i < numberOfTestCases; i++) {
            final byte[] data = new byte[random.nextInt(100)];
            random.nextBytes(data);
            testCases.add(data);

            zip.putNextEntry(
                new io.github.tsabirgaliev.zip.ZipEntry("data" + i)
                    .setInputStream(new ByteArrayInputStream(data))
            );
        }

        zip.close();

        final ZipInputStream zipReader = new ZipInputStream(zip.asInputStream());

        int i = 0;
        ZipEntry zipEntry = zipReader.getNextEntry();
        while (zipEntry != null) {

            Assertions.assertEquals(
                "data" + i,
                zipEntry.getName(),
                "zip entry name is different from expected name"
            );

            Assertions.assertTrue(
                IOUtils.contentEquals(zipReader, new ByteArrayInputStream(testCases.get(i))),
                "stored content is different than expected for item " + i
            );

            zipReader.closeEntry();
            zipEntry = zipReader.getNextEntry();
            i++;
        }

        zipReader.close();
    }

}
