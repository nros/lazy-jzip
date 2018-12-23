package io.github.tsabirgaliev.zip.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Random;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Test CountingInputStream")
public class TestCountingInitInputStream  {


    @Test
    public void testProxiedInstanceIsCalled() throws IOException {

        final Random random = new Random();
        final int bufferSize = 400;
        final byte[] testBytes = new byte[bufferSize];
        random.nextBytes(testBytes);

        final CountingInputStream countingInputStream = new CountingInputStream(new ByteArrayInputStream(testBytes));
        long bytesHaveBeenRead = 0;

        // read a sequence of bytes and skip some
        countingInputStream.read();
        bytesHaveBeenRead++;

        countingInputStream.skip(2);
        bytesHaveBeenRead += 2;

        final int bufferSize2 = 10;
        final byte[] buffer = new byte[bufferSize2];
        countingInputStream.read(buffer);
        bytesHaveBeenRead += buffer.length;

        final int bytesToRead = 5;
        final int testOffset = 2;
        countingInputStream.read(buffer, testOffset, bytesToRead);
        bytesHaveBeenRead += bytesToRead;

        countingInputStream.close();

        Assertions.assertEquals(
            countingInputStream.getByteCount(),
            bytesHaveBeenRead,
            "the counted bytes that on reading is different than expected"
        );
    }
}
