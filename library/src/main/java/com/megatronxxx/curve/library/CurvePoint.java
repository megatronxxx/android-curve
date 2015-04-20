package com.megatronxxx.curve.library;

import java.io.IOException;
import java.io.InputStream;

public class CurvePoint {

    public int x;
    public int y;

    public void parse(InputStream inputStream) throws IOException {
        inputStream.read();
        y = inputStream.read();
        inputStream.read();
        x = inputStream.read();
    }
}
