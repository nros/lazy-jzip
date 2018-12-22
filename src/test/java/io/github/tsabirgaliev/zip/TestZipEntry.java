package io.github.tsabirgaliev.zip;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestZipEntry {

    public byte[] createTemporaryData() throws IOException {
        final Random random = new Random();
        final byte[] data = new byte[2048];
        random.nextBytes(data);
        return data;
    }


    public File createTemporaryFileWithData() throws IOException {
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
        final ZipEntry zipEntry = new ZipEntry("test").setFile(temporaryFile);
        final InputStream zipIn = zipEntry.getStream();

        Assertions.assertNotNull(zipIn, "zip entry does not return a proper stream");

        Assertions.assertArrayEquals(
            IOUtils.toByteArray(zipIn),
            IOUtils.toByteArray(temporaryFile.toURI()),
            "some different, unknown data was read"
        );
        zipIn.close();

        Assertions.assertTrue(temporaryFile.exists(), "the temporary file has been deleted unexpectedly");
        temporaryFile.delete();
    }


    @Test
    public void getStream_opensFileAndDeletesFile() throws IOException {

        final File temporaryFile = this.createTemporaryFileWithData();
        final ZipEntry zipEntry = new ZipEntry("test").setFile(temporaryFile, true);
        final InputStream zipIn = zipEntry.getStream();

        Assertions.assertNotNull(zipIn, "zip entry does not return a proper stream");

        Assertions.assertTrue(
            zipEntry.isFileDeletedOnFullyRead(),
            "marker to delete the temporary file is not being set properly"
        );

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
        final ZipEntry zipEntry = new ZipEntry("test").setTemporaryFile(temporaryFile);
        final InputStream zipIn = zipEntry.getStream();

        Assertions.assertNotNull(zipIn, "zip entry does not return a proper stream");

        Assertions.assertTrue(
            zipEntry.isFileDeletedOnFullyRead(),
            "marker to delete the temporary file is not being set properly"
        );

        Assertions.assertArrayEquals(
            IOUtils.toByteArray(temporaryFile.toURI()),
            IOUtils.toByteArray(zipIn),
            "some different, unknown data was read"
        );
        zipIn.close();

        Assertions.assertTrue(!temporaryFile.exists(), "the temporary file has not been deleted");
        temporaryFile.delete();
    }


    @Test
    public void getInputStream() throws IOException {

        final File temporaryFile = this.createTemporaryFileWithData();
        final ZipEntry zipEntry = new ZipEntry("test").setTemporaryFile(temporaryFile);
        try (final InputStream zipIn = zipEntry.getInputStream()) {
            Assertions.assertNull(zipIn, "zip entry uses an input stream but has a file being set");
        }

        Assertions.assertTrue(
            zipEntry.getFile() == temporaryFile,
            "zip entry does not return same file as passed-in"
        );
        temporaryFile.delete();
    }


    @Test
    public void setInputStream_AfterFile() throws IOException {

        final File temporaryFile = this.createTemporaryFileWithData();
        final ZipEntry zipEntry = new ZipEntry("test").setTemporaryFile(temporaryFile);
        final byte[] otherTestData = this.createTemporaryData();

        try (final InputStream zipIn = zipEntry.getInputStream()) {
            Assertions.assertNull(zipIn, "zip entry uses an input stream but has a file being set");
        }

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            zipEntry.setInputStream(new ByteArrayInputStream(otherTestData));
        }, "setting a file when an InputStream is used does not fail, as it would have been expected.");


        try (final InputStream zipIn = zipEntry.getInputStream()) {
            Assertions.assertNull(zipIn, "zip entry does use an input stream although a file is used");
        }

        Assertions.assertTrue(
            zipEntry.getFile() == temporaryFile,
            "zip entry does not return same file as passed-in"
        );


        try (final InputStream zipIn = zipEntry.getStream()) {

            try (final InputStream zipIn2 = zipEntry.getInputStream()) {
                Assertions.assertNull(zipIn2, "zip entry does use an input stream although a file is used");
            }

            Assertions.assertArrayEquals(
                IOUtils.toByteArray(temporaryFile.toURI()),
                IOUtils.toByteArray(zipIn),
                "input stream does not get precedence over file"
            );
        }


        temporaryFile.delete();
    }


    @Test
    public void setInputStream_BeforeFile() throws IOException {

        final File temporaryFile = this.createTemporaryFileWithData();
        final byte[] otherTestData = this.createTemporaryData();
        final ZipEntry zipEntry = new ZipEntry("test");


        try (final InputStream zipIn = zipEntry.getInputStream()) {
            Assertions.assertNull(zipIn, "zip entry uses an input stream but has a file being set");
        }


        zipEntry.setInputStream(new ByteArrayInputStream(otherTestData));
        try (final InputStream zipIn = zipEntry.getInputStream()) {
            Assertions.assertNotNull(zipIn, "zip entry does not uses the passed-in input stream");
        }


        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            zipEntry.setTemporaryFile(temporaryFile);
        }, "setting a file when an InputStream is used does not fail, as it would have been expected.");


        Assertions.assertNull(
            zipEntry.getFile(),
            "zip entry has a file being set although an InputStream is used"
        );


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
                return "/test";
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
            "name of zip entry has not been copied correctly"
        );

        Assertions.assertArrayEquals(
            testData,
            IOUtils.toByteArray(zipEntry.getStream()),
            "some different, unknown data was read"
        );
    }


    @Test
    public void testConstructorOfZipEntry() throws IOException {

        final byte[] testData = this.createTemporaryData();
        final InputStream testStream = new ByteArrayInputStream(testData);
        final ZipEntryData zipEntryData = new ZipEntryData() {

            @Override
            public String getPath() {
                return "/test";
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
            "name of zip entry has not been copied correctly"
        );

        Assertions.assertArrayEquals(
            testData,
            IOUtils.toByteArray(zipEntry2.getStream()),
            "some different, unknown data was read"
        );
    }


    @Test
    public void testConstructorOfJavaZipEntry() throws IOException {

        final java.util.zip.ZipEntry zipEntry = new java.util.zip.ZipEntry("/myTest");
        final ZipEntry zipEntry2 = new ZipEntry(zipEntry);

        Assertions.assertEquals(
            zipEntry.getName(),
            zipEntry2.getName(),
            "name of zip entry has not been copied correctly"
        );

        Assertions.assertNull(
            zipEntry2.getInputStream(),
            "some stream has been set unexpectedly"
        );

        Assertions.assertNull(
            zipEntry2.getFile(),
            "some file has been set unexpectedly"
        );

        Assertions.assertThrows(RuntimeException.class, () -> {
            zipEntry2.getStream();
        }, "getting a stream although neither a file nor any input data has been set");
    }


    @Test
    public void setInputStream_afterInputStream() throws IOException {

        try (final InputStream mockedInputStream = mock(InputStream.class)) {
            final ZipEntry zipEntry = new ZipEntry("/myTest").setInputStream(mockedInputStream);
            zipEntry.setInputStream(new ByteArrayInputStream(this.createTemporaryData()));
            verify(mockedInputStream).close();
        }

    }
}
