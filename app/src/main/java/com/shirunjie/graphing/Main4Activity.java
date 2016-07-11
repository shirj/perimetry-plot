package com.shirunjie.graphing;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.ViewTreeObserver;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class Main4Activity extends Activity {

    private boolean needSaveImage = false;
    private PerimetryDataView dataView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main4);

        dataView = (PerimetryDataView) findViewById(R.id.data_view);
        PerimetryData entries = new PerimetryData(new ArrayList<Entry>());
        for (int i = -27; i <= 27; i += 6) {
            for (int j = -27; j <= 27; j += 6) {
                if (Math.pow(i, 2) + Math.pow(j, 2) < 800) {
                    //                    entries.add(new Entry(i, j, String.format("(%d,%d)", i, j)));
                    entries.add(new Entry(i, j, 34));
                }
            }
        }
        entries.add(new Entry(0, 0, "⊙"));

        dataView.setBackgroundColor(0xFFFFFFFF);
        dataView.setData(entries);
        dataView.setDrawingCacheEnabled(true);
        //        dataView.invalidate();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
            needSaveImage = true;
        } else {
            saveImage();
        }

    }

    private void saveImage() {
        dataView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                dataView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                needSaveImage = false;
                final AsyncTask<Void, Void, String> saveImageAsyncTask = new AsyncTask<Void, Void, String>() {
                    private Bitmap b;

                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        b = dataView.getDrawingCache();
                    }

                    @Override
                    protected String doInBackground(Void... params) {
                        File sdCard = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                        File file   = new File(sdCard, "image.png");
                        try {
                            FileOutputStream fos = new FileOutputStream(file);
                            b.compress(Bitmap.CompressFormat.PNG, 100, fos);
                            fos.close();
                            return file.toString();
                        } catch (Exception e) {
                            e.printStackTrace();
                            return null;
                        }
                    }

                    @Override
                    protected void onPostExecute(String string) {
                        super.onPostExecute(string);
                        if (string != null) {
                            Toast.makeText(Main4Activity.this, "Saved image to " + string, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(Main4Activity.this, "Save image failed", Toast.LENGTH_SHORT).show();
                        }

                    }
                };
                saveImageAsyncTask.execute();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (needSaveImage) {
            needSaveImage = false;
            saveImage();
        }
    }
}