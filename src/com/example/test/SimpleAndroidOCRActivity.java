package com.example.test;
import java.io.File;
import java.io.IOException;

import com.googlecode.tesseract.android.TessBaseAPI;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class SimpleAndroidOCRActivity extends Activity {
  public static final String DATA_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
 
  // You should have the trained data file in assets folder
  // You can get them at:
  // http://code.google.com/p/tesseract-ocr/downloads/list
  public static final String lang = "chi_sim";

  private static final String TAG = "SimpleAndroidOCR.java";

  protected Button _button;
  // protected ImageView _image;
  protected EditText _field;
  protected String _path;
  protected boolean _taken;

  protected static final String PHOTO_TAKEN = "photo_taken";

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_simple_android_ocr);
     // _image = (ImageView) findViewById(R.id.image);
    _field = (EditText) findViewById(R.id.field);
    _button = (Button) findViewById(R.id.button);
    _button.setOnClickListener(new View.OnClickListener() {
		@Override
		public void onClick(View arg0) {
		      Log.v(TAG, "Starting Camera app");
		      File file = new File(_path);
		      Uri outputFileUri = Uri.fromFile(file);

		      final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		      intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);

		      startActivityForResult(intent, 0);
		}
	});

    _path = DATA_PATH + "/ocr.jpg";
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {

    Log.i(TAG, "resultCode: " + resultCode);

    if (resultCode == -1) {
      onPhotoTaken();
    } else {
      Log.v(TAG, "User cancelled");
    }
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    outState.putBoolean(SimpleAndroidOCRActivity.PHOTO_TAKEN, _taken);
  }

  @Override
  protected void onRestoreInstanceState(Bundle savedInstanceState) {
    Log.i(TAG, "onRestoreInstanceState()");
    if (savedInstanceState.getBoolean(SimpleAndroidOCRActivity.PHOTO_TAKEN)) {
      onPhotoTaken();
    }
  }

  protected void onPhotoTaken() {
    _taken = true;

    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inSampleSize = 4;

    Bitmap bitmap = BitmapFactory.decodeFile(_path, options);

    try {
      ExifInterface exif = new ExifInterface(_path);
      int exifOrientation = exif.getAttributeInt(
          ExifInterface.TAG_ORIENTATION,
          ExifInterface.ORIENTATION_NORMAL);

      Log.v(TAG, "Orient: " + exifOrientation);

      int rotate = 0;

      switch (exifOrientation) {
      case ExifInterface.ORIENTATION_ROTATE_90:
        rotate = 90;
        break;
      case ExifInterface.ORIENTATION_ROTATE_180:
        rotate = 180;
        break;
      case ExifInterface.ORIENTATION_ROTATE_270:
        rotate = 270;
        break;
      }

      Log.v(TAG, "Rotation: " + rotate);

      if (rotate != 0) {

        // Getting width & height of the given image.
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        // Setting pre rotate
        Matrix mtx = new Matrix();
        mtx.preRotate(rotate);

        // Rotating Bitmap
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, false);
      }

      // Convert to ARGB_8888, required by tess
      bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

    } catch (IOException e) {
      Log.e(TAG, "Couldn't correct orientation: " + e.toString());
    }
    new as().execute(new Bitmap[]{bitmap});
    // _image.setImageBitmap( bitmap );
   
  }

  class as extends AsyncTask<Bitmap, Integer, Integer>{

	@Override
	protected Integer doInBackground(Bitmap... arg0) {
		Bitmap bitmap = arg0[0];
	    Log.v(TAG, "Before baseApi");

	    TessBaseAPI baseApi = new TessBaseAPI();
	    baseApi.setDebug(true);
	    Log.v("Init status", String.valueOf(baseApi.init(DATA_PATH, lang)));
	    baseApi.setImage(bitmap);
	   
	    String recognizedText = baseApi.getUTF8Text();
	   
	    baseApi.end();

	    // You now have the text in recognizedText var, you can do anything with it.
	    // We will display a stripped out trimmed alpha-numeric version of it (if lang is eng)
	    // so that garbage doesn't make it to the display.

	    Log.v(TAG, "OCRED TEXT: " + recognizedText);
		return null;
	}
	  
  }

}