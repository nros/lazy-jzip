package io.github.tsabirgaliev.zip.packets;

import java.io.IOException;
import java.nio.file.attribute.FileTime;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Test AbstractZipPacketBuilder UInt conversion")
public class TestAbstractZipPacketBuilderUIntConversion {


    private static final byte BYTE_ALL_BITS = (byte)0xFF;
    private static final long MAX_UNSIGNED_INTEGER_16_BIT = 0xFFFFL;
    private static final long MAX_UNSIGNED_INTEGER_32_BIT = 0xFFFFFFFFL;


    @Test
    public void testConvertIntegerToUInt() throws IOException {

        final TestPacketuilder packetBuilder = new TestPacketuilder();

        final long uint16TestValue = 26379;
        final byte[] convertedUInt16TestValue = new byte[] {
            (byte)0b00001011,
            (byte)0b01100111,
        };

        final long uint32TestValue = 389884679;
        final byte[] convertedUInt32TestValue = new byte[] {
            (byte)0b00000111,
            (byte)0b00101011,
            (byte)0b00111101,
            (byte)0b00010111,
        };

        Assertions.assertArrayEquals(
            convertedUInt16TestValue,
            packetBuilder.convertLongToUInt16(uint16TestValue),
            "conversion of integer to an unsigned UINT16 failed"
        );
        Assertions.assertArrayEquals(
            convertedUInt32TestValue,
            packetBuilder.convertLongToUInt32(uint32TestValue),
            "conversion of integer to unsigned UINT32 failed"
        );


        Assertions.assertArrayEquals(
            new byte[] {
                0,
                0,
            },
            packetBuilder.convertLongToUInt16(0),
            "conversion of 0 to UINT16 failed"
        );
        Assertions.assertArrayEquals(
            new byte[] {
                0,
                0,
                0,
                0,
            },
            packetBuilder.convertLongToUInt32(0),
            "conversion of 0 to UINT32 failed"
        );


        Assertions.assertArrayEquals(
            new byte[] {
                TestAbstractZipPacketBuilderUIntConversion.BYTE_ALL_BITS,
                TestAbstractZipPacketBuilderUIntConversion.BYTE_ALL_BITS,
            },
            packetBuilder.convertLongToUInt16(TestAbstractZipPacketBuilderUIntConversion.MAX_UNSIGNED_INTEGER_16_BIT),
            "conversion of maximum unsigned integer (16) value UINT16 failed"
        );
        Assertions.assertArrayEquals(
            new byte[] {
                TestAbstractZipPacketBuilderUIntConversion.BYTE_ALL_BITS,
                TestAbstractZipPacketBuilderUIntConversion.BYTE_ALL_BITS,
                TestAbstractZipPacketBuilderUIntConversion.BYTE_ALL_BITS,
                TestAbstractZipPacketBuilderUIntConversion.BYTE_ALL_BITS,
            },
            packetBuilder.convertLongToUInt32(TestAbstractZipPacketBuilderUIntConversion.MAX_UNSIGNED_INTEGER_32_BIT),
            "conversion of maximum unsigned integer (32) value UINT32 failed"
        );
    }


    @Test
    public void testConvertIntegerToUIntWithHighBitsInByte() throws IOException {

        final TestPacketuilder packetBuilder = new TestPacketuilder();

        final long uint16TestValue = 59275;
        final byte[] convertedUInt16TestValue = new byte[] {
            (byte)0b10001011,
            (byte)0b11100111,
        };

        final long uint32TestValue = 398273415;
        final byte[] convertedUInt32TestValue = new byte[] {
            (byte)0b10000111,
            (byte)0b00101011,
            (byte)0b10111101,
            (byte)0b00010111,
        };


        Assertions.assertArrayEquals(
            convertedUInt16TestValue,
            packetBuilder.convertLongToUInt16(uint16TestValue),
            "conversion of integer to UINT16 failed"
        );
        Assertions.assertArrayEquals(
            convertedUInt32TestValue,
            packetBuilder.convertLongToUInt32(uint32TestValue),
            "conversion of integer to UINT32 failed"
        );
    }


    @Test
    public void testConvertSignedIntegerToUInt() throws IOException {

        final TestPacketuilder packetBuilder = new TestPacketuilder();

        final long uint16UnsignedTestValue = 39157;
        final byte[] convertedUInt16TestValue = new byte[] {
            (byte)0b11110101,
            (byte)0b10011000,
        };

        final long uint16SignedTestValue = -26379;

        boolean isCollissionDetected = false;
        IllegalArgumentException catchedException = null;
        try {
            final byte[] convertedUnsigned = packetBuilder.convertLongToUInt16(uint16UnsignedTestValue);
            Assertions.assertNotNull(
                convertedUnsigned,
                "unsigned integer has not been encoded in any way"
            );

            Assertions.assertArrayEquals(
                convertedUInt16TestValue,
                convertedUnsigned,
                "unsigned integer has not been encoded properly"
            );

            final byte[] convertedSignedValue = packetBuilder.convertLongToUInt16(uint16SignedTestValue);
            Assertions.assertNotNull(
                convertedSignedValue,
                "failed to encode signed integer"
            );

            isCollissionDetected = convertedUInt16TestValue.length == convertedSignedValue.length;
            for (int i = 0; isCollissionDetected && i < convertedUInt16TestValue.length; i++) {
                isCollissionDetected = isCollissionDetected && convertedUInt16TestValue[i] == convertedSignedValue[i];
            }

            Assertions.assertTrue(
                !isCollissionDetected,
                "collision detected, while encoding signed and unsigned value"
            );

        } catch (final IllegalArgumentException illegalArgumentEx) {

            // Perfect, signed integers are banned!
            catchedException = illegalArgumentEx;
        }

        Assertions.assertTrue(
            !isCollissionDetected || catchedException != null,
            "signed integers should be banned as their value leads to unexpected results and collissions!"
        );
    }


    /**
     * overrides the abstract class and changes visibility of protected functions to public - for testing.
     */
    private static class TestPacketuilder extends AbstractZipPacketBuilder {
        @Override
        public byte[] convertLongToUInt16(final long i) {
            return super.convertLongToUInt16(i);
        }

        @Override
        public byte[] convertLongToUInt32(final long i) {
            return super.convertLongToUInt32(i);
        }

        @Override
        public byte[] convertFileTimeToZipTime(final FileTime fileTime)  {
            return super.convertFileTimeToZipTime(fileTime);
        }

        @Override
        public byte[] convertFileTimeToZipDate(final FileTime fileTime)  {
            return super.convertFileTimeToZipDate(fileTime);
        }
    }
}
