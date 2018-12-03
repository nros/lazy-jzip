package io.github.tsabirgaliev.zip;

import java.util.Random;
import java.util.zip.CRC32;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestByteCountingCRC32 {

    @Test
    public void testByteCounting_updateSingle() {
        final ByteCountingCRC32 crc32 = new ByteCountingCRC32();
        final Random random = new Random();

        final long bytesToCount = Math.max(random.nextInt(1024), 10);
        for (long i=0; i < bytesToCount; i++) {
            crc32.update(random.nextInt());
        }

        Assertions.assertEquals(bytesToCount, crc32.getByteCounter());
    }


    @Test
    public void testByteCounting_updateWithFullArray() {
        final ByteCountingCRC32 crc32 = new ByteCountingCRC32();
        final Random random = new Random();
        final int bytesToCount = Math.max(random.nextInt(1024), 10);

        final byte[] bytes = new byte[bytesToCount];
        random.nextBytes(bytes);

        crc32.update(bytes, 0, bytes.length);
        Assertions.assertEquals(bytesToCount, crc32.getByteCounter());
    }


    @Test
    public void testByteCounting_updateWithArrayParts() {
        final ByteCountingCRC32 crc32 = new ByteCountingCRC32();
        final Random random = new Random();
        final int bytesToCount = Math.max(random.nextInt(1024), 10);

        final byte[] bytes = new byte[bytesToCount];
        random.nextBytes(bytes);

        crc32.update(bytes, 4, bytes.length);
        Assertions.assertEquals(bytesToCount - 4, crc32.getByteCounter());
    }


    @Test
    public void testByteCounting_updateWithArrayInvalidLength() {
        final ByteCountingCRC32 crc32 = new ByteCountingCRC32();
        final Random random = new Random();
        final int bytesToCount = Math.max(random.nextInt(1024), 10);

        final byte[] bytes = new byte[bytesToCount];
        random.nextBytes(bytes);

        crc32.update(bytes, 4, bytes.length + 200);
        Assertions.assertEquals(bytesToCount - 4, crc32.getByteCounter());
    }


    @Test
    public void testByteCounting_updateWithArrayInvalidZeroLength() {
        final ByteCountingCRC32 crc32 = new ByteCountingCRC32();
        final Random random = new Random();
        final int bytesToCount = Math.max(random.nextInt(1024), 10);

        final byte[] bytes = new byte[bytesToCount];
        random.nextBytes(bytes);

        crc32.update(bytes, 4, 0);
        Assertions.assertEquals(0, crc32.getByteCounter());
    }


    @Test
    public void testByteCounting_updateWithArrayInvalidOffset() {
        final ByteCountingCRC32 crc32 = new ByteCountingCRC32();
        final Random random = new Random();
        final int bytesToCount = Math.max(random.nextInt(1024), 10);

        final byte[] bytes = new byte[bytesToCount];
        random.nextBytes(bytes);

        crc32.update(bytes, -10, 0);
        Assertions.assertEquals(0, crc32.getByteCounter());

        crc32.update(bytes, bytes.length + 1, 0);
        Assertions.assertEquals(0, crc32.getByteCounter());
    }


    @Test
    public void testCRC32() {
        final ByteCountingCRC32 crc32 = new ByteCountingCRC32();
        final CRC32 originalCRC32 = new CRC32();
        final Random random = new Random();

        final long bytesToCount = Math.max(random.nextInt(1024), 10);
        for (long i=0; i < bytesToCount; i++) {
            final int byteToAdd = random.nextInt();
            crc32.update(byteToAdd);
            originalCRC32.update(byteToAdd);
        }
        Assertions.assertEquals(originalCRC32.getValue(), crc32.getValue());
    }
}
