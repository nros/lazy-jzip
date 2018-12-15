package io.github.tsabirgaliev.zip;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

import io.github.tsabirgaliev.zip.packets.DataDescriptor;

public class DeflaterDDInputStream extends InputStream {
    private final Consumer<DataDescriptor> ddConsumer;
    DeflaterCheckedInputStream in;

    boolean inExhausted = false;

    InputStream ddIn;

    public DeflaterDDInputStream(final InputStream in, final Consumer<DataDescriptor> ddConsumer) {
        this.in = new DeflaterCheckedInputStream(in);
        this.ddConsumer = ddConsumer;
    }


    @Override
    public int read() throws IOException {
        int b = -1;
        if (!this.inExhausted) {
            b = this.in.read();

            if (b == -1) {
                this.in.close();
                this.inExhausted = true;
                final DataDescriptor dd = this.in.getDataDescriptor();
                this.ddConsumer.accept(dd);
                this.ddIn = new ByteArrayInputStream(dd.getBytes());
                b = this.ddIn.read();
            }
        } else {
            b = this.ddIn.read();
        }

        return b;
    }
}

