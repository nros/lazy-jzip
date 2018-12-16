package io.github.tsabirgaliev.zip.io;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.Test;

public class ProxiedInitInputStream  {


    @Test
    public void testProxiedInstanceIsCalled() throws IOException {

        @SuppressWarnings("resource")
        final InputStream mockedInputStream = mock(InputStream.class);
        final ProxyInputStream lazyInputStream = new ProxyInputStream(mockedInputStream);

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
