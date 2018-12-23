package io.github.tsabirgaliev.zip;

import java.util.Random;
import java.util.zip.CRC32;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Test ByteCountingCrc32")
public class TestByteCountingCrc32 {

    @Test
    public void testByteCounting_updateSingle() {
        final ByteCountingCrc32 crc32 = new ByteCountingCrc32();
        final Random random = new Random();

        final long bytesToCount = Math.max(random.nextInt(1024), 10);
        for (long i = 0; i < bytesToCount; i++) {
            crc32.update(random.nextInt());
        }

        Assertions.assertEquals(bytesToCount, crc32.getByteCounter());
    }


    @Test
    public void testByteCounting_updateWithFullArray() {
        final ByteCountingCrc32 crc32 = new ByteCountingCrc32();
        final Random random = new Random();
        final int bytesToCount = Math.max(random.nextInt(1024), 10);

        final byte[] bytes = new byte[bytesToCount];
        random.nextBytes(bytes);

        crc32.update(bytes, 0, bytes.length);
        Assertions.assertEquals(bytesToCount, crc32.getByteCounter());
    }


    @Test
    public void testByteCounting_updateWithArrayParts() {
        final ByteCountingCrc32 crc32 = new ByteCountingCrc32();
        final Random random = new Random();
        final int minimalBufferSize = 10;
        final int maximalBufferSize = 1024;
        final int bytesToCount = Math.max(random.nextInt(maximalBufferSize), minimalBufferSize);

        final byte[] bytes = new byte[bytesToCount];
        random.nextBytes(bytes);

        final int testOffset = 4;
        crc32.update(bytes, testOffset, bytes.length);
        Assertions.assertEquals(bytesToCount - testOffset, crc32.getByteCounter());
    }


    @Test
    public void testByteCounting_updateWithArrayInvalidLength() {
        final ByteCountingCrc32 crc32 = new ByteCountingCrc32();
        final Random random = new Random();
        final int minimalBufferSize = 10;
        final int maximalBufferSize = 1024;
        final int bytesToCount = Math.max(random.nextInt(maximalBufferSize), minimalBufferSize);

        final byte[] bytes = new byte[bytesToCount];
        random.nextBytes(bytes);

        final int testOffset = 4;
        crc32.update(bytes, testOffset, bytes.length + bytes.length);
        Assertions.assertEquals(bytesToCount - testOffset, crc32.getByteCounter());
    }


    @Test
    public void testByteCounting_updateWithArrayInvalidZeroLength() {
        final ByteCountingCrc32 crc32 = new ByteCountingCrc32();
        final Random random = new Random();
        final int minimalBufferSize = 10;
        final int maximalBufferSize = 1024;
        final int bytesToCount = Math.max(random.nextInt(maximalBufferSize), minimalBufferSize);

        final byte[] bytes = new byte[bytesToCount];
        random.nextBytes(bytes);

        final int testOffset = 4;
        crc32.update(bytes, testOffset, 0);
        Assertions.assertEquals(0, crc32.getByteCounter());
    }


    @Test
    public void testByteCounting_updateWithArrayInvalidOffset() {
        final ByteCountingCrc32 crc32 = new ByteCountingCrc32();
        final Random random = new Random();
        final int minimalBufferSize = 10;
        final int maximalBufferSize = 1024;
        final int bytesToCount = Math.max(random.nextInt(maximalBufferSize), minimalBufferSize);

        final byte[] bytes = new byte[bytesToCount];
        random.nextBytes(bytes);

        final int invalidTestOffset = -10;
        crc32.update(bytes, invalidTestOffset, 0);
        Assertions.assertEquals(0, crc32.getByteCounter());

        crc32.update(bytes, bytes.length + 1, 0);
        Assertions.assertEquals(0, crc32.getByteCounter());
    }


    @Test
    public void testCrc32() {
        final ByteCountingCrc32 crc32 = new ByteCountingCrc32();
        final CRC32 originalCrc32 = new CRC32();
        final Random random = new Random();

        final int minimalBufferSize = 10;
        final int maximalBufferSize = 1024;
        final int bytesToCount = Math.max(random.nextInt(maximalBufferSize), minimalBufferSize);

        for (long i = 0; i < bytesToCount; i++) {
            final int byteToAdd = random.nextInt();
            crc32.update(byteToAdd);
            originalCrc32.update(byteToAdd);
        }
        Assertions.assertEquals(originalCrc32.getValue(), crc32.getValue());
    }
}
