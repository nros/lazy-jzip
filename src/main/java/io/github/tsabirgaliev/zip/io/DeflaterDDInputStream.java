package io.github.tsabirgaliev.zip.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

public class DeflaterDDInputStream extends InputStream {
    private Consumer<DeflaterCheckedInputStream> checkedInputConsumer;
    private DeflaterCheckedInputStream in;

    public DeflaterDDInputStream(
        final DeflaterCheckedInputStream inStream, final Consumer<DeflaterCheckedInputStream> consumer
    ) {
        this.in = inStream;
        this.checkedInputConsumer = consumer;
    }


    @Override
    public int read() throws IOException {
        final int b = this.in != null ? this.in.read() : -1;
        if (b == -1) {
            this.close();
        }

        return b;
    }


    @Override
    public void close() throws IOException {

        if (this.in != null) {
            this.in.close();

            if (this.checkedInputConsumer != null) {
                this.checkedInputConsumer.accept(this.in);
                this.checkedInputConsumer = null;
            }

            this.in = null;
        }
    }
}

