package io.github.tsabirgaliev.zip.io;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Test LazyInitInputStream")
public class TestLazyInitInputStream  {


    @Test
    public void testSupplierIsCalledOnlyAfterFirstRead() throws IOException {

        final InputStream mockedInputStream = mock(InputStream.class);
        when(mockedInputStream.available()).thenReturn(1);
        when(mockedInputStream.read()).thenReturn((int)'A');

        final int[] supplierWasCalledCounter = new int[1];

        supplierWasCalledCounter[0] = 0;
        final LazyInitInputStream<InputStream> lazyInputStream = new LazyInitInputStream<InputStream>(() -> {
            supplierWasCalledCounter[0]++;
            return mockedInputStream;
        });

        Assertions.assertEquals(
            supplierWasCalledCounter[0],
            0,
            "supplier to provide input stream has been called too soon!"
        );

        lazyInputStream.available();
        Assertions.assertEquals(
            supplierWasCalledCounter[0],
            1,
            "supplier to provide input stream has not been called!"
        );

        lazyInputStream.read();
        Assertions.assertEquals(
            supplierWasCalledCounter[0],
            1,
            "supplier to provide input stream has been called a second time!"
        );

        lazyInputStream.close();
    }



    @Test
    public void testProxiedInstanceIsCalled() throws IOException {

        @SuppressWarnings("resource")
        final InputStream mockedInputStream = mock(InputStream.class);
        final LazyInitInputStream<InputStream> lazyInputStream =
            new LazyInitInputStream<InputStream>(() -> mockedInputStream);

        lazyInputStream.available();
        verify(mockedInputStream).available();

        lazyInputStream.read();
        verify(mockedInputStream).read();

        final int numericParameter = 10;
        lazyInputStream.mark(numericParameter);
        verify(mockedInputStream, atLeastOnce()).mark(numericParameter);

        lazyInputStream.markSupported();
        verify(mockedInputStream, atLeastOnce()).markSupported();

        lazyInputStream.reset();
        verify(mockedInputStream, atLeastOnce()).reset();

        final int bytesToSkip = 97;
        lazyInputStream.skip(bytesToSkip);
        verify(mockedInputStream, atLeastOnce()).skip(bytesToSkip);

        final int bufferSize = 10;
        final byte[] buffer = new byte[bufferSize];
        lazyInputStream.read(buffer);
        verify(mockedInputStream, atLeastOnce()).read(buffer);

        lazyInputStream.read(buffer, 2, bufferSize / 2);
        verify(mockedInputStream, atLeastOnce()).read(buffer, 2, bufferSize / 2);

        lazyInputStream.close();
        verify(mockedInputStream, atLeastOnce()).close();
    }
}
