package com.mysql.management.util;

import java.io.IOException;
import java.io.OutputStream;

/**
 * This class is final simply as a hint to the compiler, it may be un-finalized
 * safely.
 */
public final class TeeOutputStream extends OutputStream {

    private final OutputStream out1;

    private OutputStream out2;

    public TeeOutputStream(OutputStream out1, OutputStream out2) {
        this.out1 = out1;
        setStream2(out2);
    }

    private void setStream2(OutputStream out) {
        out2 = out;
    }

    public void write(int arg0) throws IOException {
        out1.write(arg0);
        out2.write(arg0);
    }

    public void nullStreamTwo() {
        this.setStream2(new NullPrintStream());
    }
}
