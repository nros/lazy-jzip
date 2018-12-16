package io.github.tsabirgaliev.zip.io;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestLazyInitInputStream  {


    @Test
    public void testSupplierIsCalledOnlyAfterFirstRead() throws IOException {

        final InputStream mockedInputStream = mock(InputStream.class);
        when(mockedInputStream.available()).thenReturn(1);
        when(mockedInputStream.read()).thenReturn((int)'A');

        final int[] supplierWasCalledCounter = new int[1];

        supplierWasCalledCounter[0] = 0;
        final LazyInitInputStream lazyInputStream = new LazyInitInputStream(() -> {
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
        final LazyInitInputStream lazyInputStream = new LazyInitInputStream(() -> mockedInputStream);

        lazyInputStream.available();
        verify(mockedInputStream).available();

        lazyInputStream.read();
        verify(mockedInputStream).read();

        lazyInputStream.mark(10);
        verify(mockedInputStream, atLeastOnce()).mark(10);

        lazyInputStream.markSupported();
        verify(mockedInputStream, atLeastOnce()).markSupported();

        lazyInputStream.reset();
        verify(mockedInputStream, atLeastOnce()).reset();

        lazyInputStream.skip(97);
        verify(mockedInputStream, atLeastOnce()).skip(97);

        final byte[] buffer = new byte[10];
        lazyInputStream.read(buffer);
        verify(mockedInputStream, atLeastOnce()).read(buffer);

        lazyInputStream.read(buffer, 2, 5);
        verify(mockedInputStream, atLeastOnce()).read(buffer, 2, 5);

        lazyInputStream.close();
        verify(mockedInputStream, atLeastOnce()).close();
    }
}
