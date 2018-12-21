package io.github.tsabirgaliev.zip.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Random;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestCountingInitInputStream  {


    @Test
    public void testProxiedInstanceIsCalled() throws IOException {

        final Random random = new Random();
        final byte[] testBytes = new byte[400];
        random.nextBytes(testBytes);

        final CountingInputStream countingInputStream = new CountingInputStream(new ByteArrayInputStream(testBytes));
        long bytesHaveBeenRead = 0;

        // read a sequence of bytes and skip some
        countingInputStream.read();
        bytesHaveBeenRead++;

        countingInputStream.skip(2);
        bytesHaveBeenRead += 2;

        final byte[] buffer = new byte[10];
        countingInputStream.read(buffer);
        bytesHaveBeenRead += buffer.length;

        countingInputStream.read(buffer, 2, 5);
        bytesHaveBeenRead += 5;

        countingInputStream.close();

        Assertions.assertEquals(
            countingInputStream.getByteCount(),
            bytesHaveBeenRead,
            "the counted bytes that on reading is different than expected"
        );
    }
}
