package com.megatronxxx.curve.library;

import java.io.IOException;
import java.io.InputStream;

public class CurvesFile {
    public byte version;
    public Curve[] curves;
    public byte[][] cTable;

    public void parse(InputStream is) throws IOException {
        is.read();
        version = (byte) is.read();
        is.read();
        byte curvesCount = (byte) is.read();
        curves = new Curve[curvesCount];
        for (int i = 0; i < curvesCount; i++) {
            curves[i] = new Curve();
            curves[i].parse(is);
        }
    }

    @Override
    public String toString() {
        return version + " " + curves.length;
    }

    public int convert(int argb) {
        initTables();
        final int red = (argb & 0x00ff0000) >> 16;
        final int green = (argb & 0x0000ff00) >> 8;
        final int blue = argb & 0x000000ff;
        return argb & 0xff000000 | cTable[0][red] << 16 | cTable[1][green] << 8 | cTable[2][blue];
    }

    public void initTables() {
        if (cTable != null)
            return;
        cTable = new byte[3][256];
        for (int i = 0; i < 256; i++) {
            final int main = curves[0].apply(i);
            cTable[0][i] = (byte) curves[1].apply(main);
            cTable[1][i] = (byte) curves[2].apply(main);
            cTable[2][i] = (byte) curves[3].apply(main);
        }
    }
}
