package com.megatronxxx.curve.app;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;

import com.megatronxxx.curve.library.CurvesFile;
import com.megatronxxx.curve.rs.ScriptC_curve;

import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends ActionBarActivity {

    private final int NUM_BITMAPS = 3;
    RenderScriptTask currentTask = null;
    private int mCurrentBitmap = 0;
    private Bitmap mBitmapIn;
    private Bitmap[] mBitmapsOut;
    private ImageView mImageView;

    private Allocation mInAllocation;
    private Allocation[] mOutAllocations;
    private ScriptC_curve mScript;
    private RenderScript rs;
    private int curveId = R.raw.curve_null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mBitmapIn = loadBitmap(R.drawable.aaa);
        mBitmapsOut = new Bitmap[NUM_BITMAPS];
        for (int i = 0; i < NUM_BITMAPS; ++i) {
            mBitmapsOut[i] = Bitmap.createBitmap(mBitmapIn.getWidth(),
                    mBitmapIn.getHeight(), mBitmapIn.getConfig());
        }
        mImageView = (ImageView) findViewById(R.id.imageView);
        Spinner spinner = (Spinner) findViewById(R.id.curve_selector);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.curves_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String string = (String) parent.getItemAtPosition(position);
                curveId = getResources().getIdentifier(string, "raw", getPackageName());
                onButtonClick();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        createScript();
    }

    private Bitmap loadBitmap(int resource) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        return BitmapFactory.decodeResource(getResources(), resource, options);
    }

    public void onButtonClick() {
        final InputStream inputStream = getResources().openRawResource(curveId);
        final CurvesFile curvesFile = new CurvesFile();
        try {
            curvesFile.parse(inputStream);
            inputStream.close();
        } catch (IOException e) {
        }
        curvesFile.initTables();
        Allocation r = Allocation.createSized(rs, Element.I8(rs), curvesFile.cTable[0].length);
        r.copyFrom(curvesFile.cTable[0]);
        mScript.bind_r(r);

        Allocation g = Allocation.createSized(rs, Element.I8(rs), curvesFile.cTable[1].length);
        g.copyFrom(curvesFile.cTable[1]);
        mScript.bind_g(g);

        Allocation b = Allocation.createSized(rs, Element.I8(rs), curvesFile.cTable[2].length);
        b.copyFrom(curvesFile.cTable[2]);
        mScript.bind_b(b);

        updateImage(curvesFile);
    }

    private void createScript() {
        rs = RenderScript.create(this);
        mInAllocation = Allocation.createFromBitmap(rs, mBitmapIn);
        mOutAllocations = new Allocation[NUM_BITMAPS];
        for (int i = 0; i < NUM_BITMAPS; ++i) {
            mOutAllocations[i] = Allocation.createFromBitmap(rs, mBitmapsOut[i]);
        }
        mScript = new ScriptC_curve(rs);


    }

    private void updateImage(CurvesFile curvesFile) {
        if (currentTask != null)
            currentTask.cancel(false);
        currentTask = new RenderScriptTask();
        currentTask.execute(curvesFile);
    }

    private class RenderScriptTask extends AsyncTask<CurvesFile, Integer, Integer> {
        Boolean issued = false;

        protected Integer doInBackground(CurvesFile... curvesFile) {
            int index = -1;
            if (!isCancelled()) {
                issued = true;
                index = mCurrentBitmap;
                mScript.forEach_apply(mInAllocation, mOutAllocations[index]);
                mOutAllocations[index].copyTo(mBitmapsOut[index]);
                mCurrentBitmap = (mCurrentBitmap + 1) % NUM_BITMAPS;
            }
            return index;
        }

        void updateView(Integer result) {
            if (result != -1) {
                mImageView.setImageBitmap(mBitmapsOut[result]);
                mImageView.invalidate();
            }
        }

        protected void onPostExecute(Integer result) {
            updateView(result);
        }

        protected void onCancelled(Integer result) {
            if (issued) {
                updateView(result);
            }
        }
    }
}