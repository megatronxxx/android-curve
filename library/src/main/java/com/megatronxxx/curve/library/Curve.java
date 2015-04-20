package com.megatronxxx.curve.library;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Comparator;

public class Curve {

    CurvePoint[] points;

    public void parse(InputStream is) throws IOException {
        is.read();
        short count = (short) is.read();
        points = new CurvePoint[count];
        for (int i = 0; i < count; i++) {
            points[i] = new CurvePoint();
            points[i].parse(is);
        }

        Arrays.sort(points, new Comparator<CurvePoint>() {
            @Override
            public int compare(final CurvePoint lhs, final CurvePoint rhs) {
                return lhs.x < rhs.x ? -1 : (lhs.x == rhs.x ? 0 : 1);
            }
        });

    }

    public int apply(int value) {
        float[] x = new float[points.length];
        float[] y = new float[points.length];
        for (int i = 0; i < points.length; i++) {
            x[i] = points[i].x;
            y[i] = points[i].y;
        }
        final Spline spline = Spline.createSpline(x, y);
        return (int) spline.interpolate(value);
    }
}