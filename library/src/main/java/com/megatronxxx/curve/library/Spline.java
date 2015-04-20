package com.megatronxxx.curve.library;

public abstract class Spline {

    public static Spline createSpline(float[] x, float[] y) {
        if (!isStrictlyIncreasing(x)) {
            throw new IllegalArgumentException("The control points must all have strictly "
                    + "increasing X values.");
        }

        if (isMonotonic(y)) {
            return createMonotoneCubicSpline(x, y);
        } else {
            return createLinearSpline(x, y);
        }
    }

    public static Spline createMonotoneCubicSpline(float[] x, float[] y) {
        return new MonotoneCubicSpline(x, y);
    }

    public static Spline createLinearSpline(float[] x, float[] y) {
        return new LinearSpline(x, y);
    }

    private static boolean isStrictlyIncreasing(float[] x) {
        if (x == null || x.length < 2) {
            throw new IllegalArgumentException("There must be at least two control points.");
        }
        float prev = x[0];
        for (int i = 1; i < x.length; i++) {
            float curr = x[i];
            if (curr <= prev) {
                return false;
            }
            prev = curr;
        }
        return true;
    }

    private static boolean isMonotonic(float[] array) {
        if (array == null || array.length < 2) {
            throw new IllegalArgumentException("There must be at least two control points.");
        }
        float prev = array[0];
        for (int i = 1; i < array.length; i++) {
            float curr = array[i];
            if (curr < prev) {
                return false;
            }
            prev = curr;
        }
        return true;
    }

    public abstract float interpolate(float x);

    public static class MonotoneCubicSpline extends Spline {
        private float[] mX;
        private float[] mY;
        private float[] mM;

        public MonotoneCubicSpline(float[] x, float[] y) {
            if (x == null || y == null || x.length != y.length || x.length < 2) {
                throw new IllegalArgumentException("There must be at least two control "
                        + "points and the arrays must be of equal length.");
            }

            final int n = x.length;
            float[] d = new float[n - 1]; // could optimize this out
            float[] m = new float[n];

            // Compute slopes of secant lines between successive points.
            for (int i = 0; i < n - 1; i++) {
                float h = x[i + 1] - x[i];
                if (h <= 0f) {
                    throw new IllegalArgumentException("The control points must all "
                            + "have strictly increasing X values.");
                }
                d[i] = (y[i + 1] - y[i]) / h;
            }

            // Initialize the tangents as the average of the secants.
            m[0] = d[0];
            for (int i = 1; i < n - 1; i++) {
                m[i] = (d[i - 1] + d[i]) * 0.5f;
            }
            m[n - 1] = d[n - 2];

            // Update the tangents to preserve monotonicity.
            for (int i = 0; i < n - 1; i++) {
                if (d[i] == 0f) { // successive Y values are equal
                    m[i] = 0f;
                    m[i + 1] = 0f;
                } else {
                    float a = m[i] / d[i];
                    float b = m[i + 1] / d[i];
                    if (a < 0f || b < 0f) {
                        throw new IllegalArgumentException("The control points must have "
                                + "monotonic Y values.");
                    }
                    float h = (float) Math.hypot(a, b);
                    if (h > 9f) {
                        float t = 3f / h;
                        m[i] = t * a * d[i];
                        m[i + 1] = t * b * d[i];
                    }
                }
            }

            mX = x;
            mY = y;
            mM = m;
        }

        @Override
        public float interpolate(float x) {
            // Handle the boundary cases.
            final int n = mX.length;
            if (Float.isNaN(x)) {
                return x;
            }
            if (x <= mX[0]) {
                return mY[0];
            }
            if (x >= mX[n - 1]) {
                return mY[n - 1];
            }

            // Find the index 'i' of the last point with smaller X.
            // We know this will be within the spline due to the boundary tests.
            int i = 0;
            while (x >= mX[i + 1]) {
                i += 1;
                if (x == mX[i]) {
                    return mY[i];
                }
            }

            // Perform cubic Hermite spline interpolation.
            float h = mX[i + 1] - mX[i];
            float t = (x - mX[i]) / h;
            return (mY[i] * (1 + 2 * t) + h * mM[i] * t) * (1 - t) * (1 - t)
                    + (mY[i + 1] * (3 - 2 * t) + h * mM[i + 1] * (t - 1)) * t * t;
        }
    }

    public static class LinearSpline extends Spline {
        private final float[] mX;
        private final float[] mY;
        private final float[] mM;

        public LinearSpline(float[] x, float[] y) {
            if (x == null || y == null || x.length != y.length || x.length < 2) {
                throw new IllegalArgumentException("There must be at least two control "
                        + "points and the arrays must be of equal length.");
            }
            final int N = x.length;
            mM = new float[N - 1];
            for (int i = 0; i < N - 1; i++) {
                mM[i] = (y[i + 1] - y[i]) / (x[i + 1] - x[i]);
            }
            mX = x;
            mY = y;
        }

        @Override
        public float interpolate(float x) {
            // Handle the boundary cases.
            final int n = mX.length;
            if (Float.isNaN(x)) {
                return x;
            }
            if (x <= mX[0]) {
                return mY[0];
            }
            if (x >= mX[n - 1]) {
                return mY[n - 1];
            }

            // Find the index 'i' of the last point with smaller X.
            // We know this will be within the spline due to the boundary tests.
            int i = 0;
            while (x >= mX[i + 1]) {
                i += 1;
                if (x == mX[i]) {
                    return mY[i];
                }
            }
            return mY[i] + mM[i] * (x - mX[i]);
        }
    }
}