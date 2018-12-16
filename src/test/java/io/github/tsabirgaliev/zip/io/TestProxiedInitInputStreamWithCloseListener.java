package io.github.tsabirgaliev.zip.io;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestProxiedInitInputStreamWithCloseListener  {


    @Test
    public void testCloseListenerIsCalled() throws IOException {

        @SuppressWarnings("resource")
        final InputStream mockedInputStream = mock(InputStream.class);
        final ProxyInputStreamWithCloseListener<InputStream> proxyStream =
            new ProxyInputStreamWithCloseListener<InputStream>(mockedInputStream);

        final boolean[] consumerWasCalled = new boolean[] { false };
        proxyStream.addCloseListener((Consumer<InputStream>)((inputStream) -> consumerWasCalled[0] = true));

        proxyStream.close();
        verify(mockedInputStream, atLeastOnce()).close();

        Assertions.assertTrue(
            consumerWasCalled[0],
            "close listerner was not called"
        );
    }


    @Test
    public void testMoreCloseListenersAreCalled() throws IOException {

        @SuppressWarnings("resource")
        final InputStream mockedInputStream = mock(InputStream.class);
        final ProxyInputStreamWithCloseListener<InputStream> proxyStream =
            new ProxyInputStreamWithCloseListener<InputStream>(mockedInputStream);

        final int listenerCount = 5;
        final boolean[] consumerWasCalled = new boolean[listenerCount];
        for (int i = 0; i < listenerCount; i++) {
            final int currentListenerNr = i;
            consumerWasCalled[i] = false;
            proxyStream.addCloseListener(
                (Consumer<InputStream>)((inputStream) -> consumerWasCalled[currentListenerNr] = true)
            );
        }

        proxyStream.close();
        verify(mockedInputStream, atLeastOnce()).close();

        for (int i = 0; i < listenerCount; i++) {
            Assertions.assertTrue(
                consumerWasCalled[i],
                "close listerner " + i + " was not called"
            );
        }
    }


    @Test
    public void testCloseListenersAfterExceptionInListener() throws IOException {

        @SuppressWarnings("resource")
        final InputStream mockedInputStream = mock(InputStream.class);
        final ProxyInputStreamWithCloseListener<InputStream> proxyStream =
            new ProxyInputStreamWithCloseListener<InputStream>(mockedInputStream);

        final int listenerCount = 5;
        final boolean[] consumerWasCalled = new boolean[listenerCount];
        for (int i = 0; i < listenerCount; i++) {
            final int currentListenerNr = i;
            consumerWasCalled[i] = false;
            proxyStream.addCloseListener(
                (Consumer<InputStream>)((inputStream) -> {
                    consumerWasCalled[currentListenerNr] = true;
                    if (currentListenerNr == 3) {
                        throw new RuntimeException("test exception");
                    }
                })
            );
        }

        proxyStream.close();
        verify(mockedInputStream, atLeastOnce()).close();

        for (int i = 0; i < listenerCount; i++) {
            Assertions.assertTrue(
                consumerWasCalled[i],
                "close listerner " + i + " was not called"
            );
        }
    }
}
