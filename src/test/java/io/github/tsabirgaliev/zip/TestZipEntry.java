package io.github.tsabirgaliev.zip;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Test ZipEntry")
public class TestZipEntry {

    private static final String ZIP_ENTRY_NAME = "/test";

    private byte[] createTemporaryData() throws IOException {
        final int bufferSize = 2048;
        final Random random = new Random();
        final byte[] data = new byte[bufferSize];
        random.nextBytes(data);
        return data;
    }


    private File createTemporaryFileWithData() throws IOException {
        final byte[] data = this.createTemporaryData();

        final File temporaryFile = File.createTempFile("test", ".bin");
        final OutputStream temporaryOut = new FileOutputStream(temporaryFile);
        temporaryFile.deleteOnExit();
        temporaryOut.write(data);
        temporaryOut.close();

        return temporaryFile;
    }


    @Test
    public void getStream_opensFile() throws IOException {

        final File temporaryFile = this.createTemporaryFileWithData();
        final ZipEntry zipEntry = new ZipEntry(TestZipEntry.ZIP_ENTRY_NAME).setFile(temporaryFile);
        final InputStream zipIn = zipEntry.getStream();

        Assertions.assertNotNull(zipIn, "zip entry does not return a proper stream");

        Assertions.assertArrayEquals(
            IOUtils.toByteArray(zipIn),
            IOUtils.toByteArray(temporaryFile.toURI()),
            "Hm, different data was read from the ZIP"
        );
        zipIn.close();

        Assertions.assertTrue(temporaryFile.exists(), "the temporary file has been deleted unexpectedly");
        temporaryFile.delete();
    }


    @Test
    public void getStream_opensFileAndDeletesFile() throws IOException {

        final File temporaryFile = this.createTemporaryFileWithData();
        final ZipEntry zipEntry = new ZipEntry(TestZipEntry.ZIP_ENTRY_NAME).setFile(temporaryFile, true);
        final InputStream zipIn = zipEntry.getStream();

        Assertions.assertNotNull(zipIn, "zip entry stram is invalid (null)");

        Assertions.assertArrayEquals(
            IOUtils.toByteArray(zipIn),
            IOUtils.toByteArray(temporaryFile.toURI()),
            "some different, unknown data was read"
        );
        zipIn.close();

        Assertions.assertTrue(!temporaryFile.exists(), "the temporary file has not been deleted");
        temporaryFile.delete();
    }


    @Test
    public void getStream_opensTemporaryFileAndDeletesFile() throws IOException {

        final File temporaryFile = this.createTemporaryFileWithData();
        final ZipEntry zipEntry = new ZipEntry(TestZipEntry.ZIP_ENTRY_NAME).setTemporaryFile(temporaryFile);
        final InputStream zipIn = zipEntry.getStream();

        Assertions.assertNotNull(zipIn, "zip entry stream is invalid with temporary file");

        Assertions.assertArrayEquals(
            IOUtils.toByteArray(temporaryFile.toURI()),
            IOUtils.toByteArray(zipIn),
            "temporary data has not been stored in the ZIP properly"
        );
        zipIn.close();

        Assertions.assertTrue(!temporaryFile.exists(), "the temporary file has not been deleted, although expected");
        temporaryFile.delete();
    }


    @Test
    public void setInputStream_AfterFile() throws IOException {

        final File temporaryFile = this.createTemporaryFileWithData();
        final ZipEntry zipEntry = new ZipEntry(TestZipEntry.ZIP_ENTRY_NAME).setTemporaryFile(temporaryFile);
        final byte[] otherTestData = this.createTemporaryData();

        final InputStream testInStream = new ByteArrayInputStream(otherTestData);
        zipEntry.setInputStream(testInStream);

        try (final InputStream zipIn = zipEntry.getStream()) {
            Assertions.assertArrayEquals(
                IOUtils.toByteArray(new ByteArrayInputStream(otherTestData)),
                IOUtils.toByteArray(zipIn),
                "input stream is not being used after file"
            );
        }


        temporaryFile.delete();
    }


    @Test
    public void setInputStream_BeforeFile() throws IOException {

        final File temporaryFile = this.createTemporaryFileWithData();
        final byte[] otherTestData = this.createTemporaryData();
        final ZipEntry zipEntry = new ZipEntry(TestZipEntry.ZIP_ENTRY_NAME);


        try (final InputStream zipIn = zipEntry.getInputStream()) {
            Assertions.assertNull(zipIn, "zip entry uses an input stream but has a file being set!");
        }


        zipEntry.setInputStream(new ByteArrayInputStream(otherTestData));
        try (final InputStream zipIn = zipEntry.getInputStream()) {
            Assertions.assertNotNull(zipIn, "zip entry does not uses the passed-in input stream");
        }


        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            zipEntry.setTemporaryFile(temporaryFile);
        }, "setting a file when an InputStream is used does not fail, as it is expected.");


        try (final InputStream zipIn = zipEntry.getStream()) {
            Assertions.assertArrayEquals(
                otherTestData,
                IOUtils.toByteArray(zipIn),
                "input stream is not being read"
            );
        }

        temporaryFile.delete();
    }


    @Test
    public void testConstructorOfZipEntryData() throws IOException {

        final byte[] testData = this.createTemporaryData();
        final InputStream testStream = new ByteArrayInputStream(testData);
        final ZipEntryData zipEntryData = new ZipEntryData() {

            @Override
            public String getPath() {
                return TestZipEntry.ZIP_ENTRY_NAME;
            }

            @Override
            public InputStream getStream() {
                return testStream;
            }
        };
        final ZipEntry zipEntry = new ZipEntry(zipEntryData);

        Assertions.assertEquals(
            zipEntryData.getPath(),
            zipEntry.getName(),
            "name of zip entry has not been copied correctly!"
        );

        Assertions.assertArrayEquals(
            testData,
            IOUtils.toByteArray(zipEntry.getStream()),
            "data for entry in the ZIP is invalid"
        );
    }


    @Test
    public void testConstructorOfZipEntry() throws IOException {

        final byte[] testData = this.createTemporaryData();
        final InputStream testStream = new ByteArrayInputStream(testData);
        final ZipEntryData zipEntryData = new ZipEntryData() {

            @Override
            public String getPath() {
                return TestZipEntry.ZIP_ENTRY_NAME;
            }

            @Override
            public InputStream getStream() {
                return testStream;
            }
        };
        final ZipEntry zipEntry = new ZipEntry(zipEntryData);
        final ZipEntry zipEntry2 = new ZipEntry(zipEntry);

        Assertions.assertEquals(
            zipEntryData.getPath(),
            zipEntry2.getName(),
            "name of zip entry is invalid"
        );

        Assertions.assertArrayEquals(
            testData,
            IOUtils.toByteArray(zipEntry2.getStream()),
            "invalid data of entry in the ZIP"
        );
    }


    @Test
    public void testConstructorOfJavaZipEntry() throws IOException {

        final java.util.zip.ZipEntry zipEntry = new java.util.zip.ZipEntry("/myTest");
        final ZipEntry zipEntry2 = new ZipEntry(zipEntry);

        Assertions.assertEquals(
            zipEntry.getName(),
            zipEntry2.getName(),
            "name of zip entry has not been stored correctly in ZIP"
        );

        Assertions.assertNull(
            zipEntry2.getInputStream(),
            "some stream has been set unexpectedly"
        );

        Assertions.assertThrows(RuntimeException.class, () -> {
            zipEntry2.getStream();
        }, "getting a stream although neither a file nor any input data has been set");
    }
}
