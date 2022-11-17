/*
 * Decompiled with CFR 0.150.
 */
package hcsmod.server;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

public class LimitedSizeByteArrayOutputStream
extends OutputStream {
    protected byte[] buf;
    protected int count;

    public LimitedSizeByteArrayOutputStream(int size) {
        if (size < 0) {
            throw new IllegalArgumentException("Negative initial size: " + size);
        }
        this.buf = new byte[size];
    }

    private void ensureCapacity(int minCapacity) throws IOException {
        if (minCapacity - this.buf.length > 0) {
            throw new IOException("Too much data");
        }
    }

    @Override
    public synchronized void write(int b) throws IOException {
        this.ensureCapacity(this.count + 1);
        ++this.count;
    }

    @Override
    public synchronized void write(byte[] b, int off, int len) throws IOException {
        if (off < 0 || off > b.length || len < 0 || off + len - b.length > 0) {
            throw new IndexOutOfBoundsException();
        }
        this.ensureCapacity(this.count + len);
        this.count += len;
    }

    public synchronized void reset() {
        this.count = 0;
    }

    public synchronized String toString() {
        return new String(this.buf, 0, this.count);
    }

    public synchronized String toString(String charsetName) throws UnsupportedEncodingException {
        return new String(this.buf, 0, this.count, charsetName);
    }

    @Deprecated
    public synchronized String toString(int hibyte) {
        return new String(this.buf, hibyte, 0, this.count);
    }

    @Override
    public void close() throws IOException {
    }
}

