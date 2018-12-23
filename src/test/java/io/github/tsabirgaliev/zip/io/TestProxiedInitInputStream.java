package io.github.tsabirgaliev.zip.io;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Test ProxyInputStream")
public class TestProxiedInitInputStream  {


    @Test
    public void testProxiedInstanceIsCalled() throws IOException {

        @SuppressWarnings("resource")
        final InputStream mockedInputStream = mock(InputStream.class);
        final ProxyInputStream lazyInputStream = new ProxyInputStream(mockedInputStream);

        lazyInputStream.available();
        verify(mockedInputStream).available();

        lazyInputStream.read();
        verify(mockedInputStream).read();

        final int markerPosition = 10;
        lazyInputStream.mark(markerPosition);
        verify(mockedInputStream, atLeastOnce()).mark(markerPosition);

        lazyInputStream.markSupported();
        verify(mockedInputStream, atLeastOnce()).markSupported();

        lazyInputStream.reset();
        verify(mockedInputStream, atLeastOnce()).reset();

        final int bytesToSkip = 96;
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
