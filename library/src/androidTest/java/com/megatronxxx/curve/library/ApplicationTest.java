package com.megatronxxx.curve.library;

import android.app.Application;
import android.test.ApplicationTestCase;

import junit.framework.Assert;

import java.io.IOException;
import java.io.InputStream;

import curve.megatronxxx.com.library.R;

public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);
    }

    public void testNullCurve() {
        final InputStream inputStream = getContext().getResources().openRawResource(R.raw.curve_null);
        final CurvesFile curvesFile = new CurvesFile();
        try {
            curvesFile.parse(inputStream);
            inputStream.close();
            Assert.assertEquals(curvesFile.version, 4);
            Assert.assertEquals(curvesFile.curves.length, 5);
        } catch (IOException e) {
            Assert.fail();
        }
    }

    public void testGothamCurve() {
        final InputStream inputStream = getContext().getResources().openRawResource(R.raw.gotham);
        final CurvesFile curvesFile = new CurvesFile();
        try {
            curvesFile.parse(inputStream);
            inputStream.close();
            Assert.assertEquals(curvesFile.version, 4);
            Assert.assertEquals(curvesFile.curves.length, 5);
        } catch (IOException e) {
            Assert.fail();
        }
    }

    public void testHefeCurve() {
        final InputStream inputStream = getContext().getResources().openRawResource(R.raw.hefe);
        final CurvesFile curvesFile = new CurvesFile();
        try {
            curvesFile.parse(inputStream);
            inputStream.close();
            Assert.assertEquals(curvesFile.version, 4);
            Assert.assertEquals(curvesFile.curves.length, 5);
        } catch (IOException e) {
            Assert.fail();
        }
    }

    public void testCurveConvertion() {
        final InputStream inputStream = getContext().getResources().openRawResource(R.raw.curve_null);
        final CurvesFile curvesFile = new CurvesFile();
        try {
            curvesFile.parse(inputStream);
            inputStream.close();
            Assert.assertEquals(curvesFile.curves[0].apply((byte) 0), 0);
            Assert.assertEquals(curvesFile.curves[0].apply((byte) 255), (byte) 255);
            Assert.assertEquals(curvesFile.curves[1].apply((byte) 128), (byte) 128);
            Assert.assertEquals(curvesFile.curves[2].apply((byte) 25), (byte) 25);
        } catch (IOException e) {
            Assert.fail();
        }
    }


    public void testConvertor() {
        final InputStream inputStream = getContext().getResources().openRawResource(R.raw.curve_null);
        final CurvesFile curvesFile = new CurvesFile();
        try {
            curvesFile.parse(inputStream);
            inputStream.close();
            Assert.assertEquals(curvesFile.convert(0xff112233), 0xff112233);
        } catch (IOException e) {
            Assert.fail();
        }
    }

}