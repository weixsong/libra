package com.example.homework;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.googlecode.tesseract.android.TessBaseAPI;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

public class MainActivity extends Activity {

	public static final String ALBUM = "OCR";
	public static final String OCR_CONTENT = "OCR_CONTENT";

	private static final String TAG = "MainActivity";

	private static final int REQUEST_IMAGE_CAPTURE = 1;
	private static final int REQUEST_IMAGE_CROP = 2;
	private String lang = "eng";
	private String crop_intent = "com.android.camera.action.CROP";

	private ImageView iv;

	private static final String TESSDATA = "tessdata";
	private static final String IMAGENAME = "ocr.jpg";
	private static final String CROPNAME = "crop.jpg";

	private static final String DATA_PATH = Environment
			.getExternalStorageDirectory().toString() + "/" + ALBUM + "/";
	private static final String DATA_PATH_TESSDATA = DATA_PATH + TESSDATA + "/";

	public static final String IMAGE = MainActivity.DATA_PATH + IMAGENAME;
	private static final String IMAGE_CROP = MainActivity.DATA_PATH + CROPNAME;

	private final String traineddata_path = DATA_PATH_TESSDATA + lang
			+ ".traineddata";
	private final String asset_tessdata = "tessdata/" + lang + ".traineddata";
	
	private static final boolean IFCROP = true;

	private int crop_aspectX = 2;
	private int crop_aspectY = 1;
	private int output_X = 256;
	private int output_Y = 128;
	
	private int inSampleSize = 4;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);
		iv = (ImageView) findViewById(R.id.imageView1);

		// check and copy files
		checkAndCopyFiles();

		iv.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				dispatchTakePictureIntent();
			}
		});
	}

	private void checkAndCopyFiles() {
		String[] paths = new String[] { DATA_PATH, DATA_PATH_TESSDATA };
		for (String path : paths) {
			File dir = new File(path);
			if (!dir.exists()) {
				if (!dir.mkdirs()) {
					Log.v(TAG, "ERROR: Creation of directory " + path
							+ " on sdcard failed");
					return;
				} else {
					Log.v(TAG, "Created directory " + path + " on sdcard");
				}
			}
		}

		if (!(new File(traineddata_path)).exists()) {
			try {
				AssetManager assetManager = getAssets();
				InputStream in = assetManager.open(asset_tessdata);
				OutputStream os = new FileOutputStream(traineddata_path);

				// Transfer bytes from in to out
				byte[] buf = new byte[1024];
				int len;
				while ((len = in.read(buf)) > 0) {
					os.write(buf, 0, len);
				}
				in.close();
				os.close();

				Log.v(TAG, "Copied " + lang + " traineddata");
			} catch (IOException e) {
				Log.e(TAG,
						"unable to copy " + lang + " traineddata "
								+ e.toString());
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.open_camera) {
			dispatchTakePictureIntent();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	private void dispatchTakePictureIntent() {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

		// Ensure that there's a camera activity to handle the intent
		if (intent.resolveActivity(getPackageManager()) != null) {
			// Create the File where the photo should go
			File photoFile = new File(IMAGE);
			// Continue only if the File was successfully created
			if (photoFile != null) {
				intent.putExtra(MediaStore.EXTRA_OUTPUT,
						Uri.fromFile(photoFile));
				startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.i(TAG, "onActivityResult");
		if (resultCode == RESULT_OK
				&& requestCode == MainActivity.REQUEST_IMAGE_CAPTURE) {
			Intent intent = new Intent(crop_intent);
			Uri uri = Uri.fromFile(new File(IMAGE));
			intent.setDataAndType(uri, "image/*");
			intent.putExtra("crop", IFCROP);
			intent.putExtra("aspectX", crop_aspectX);
			intent.putExtra("aspectY", crop_aspectY);
			intent.putExtra("outputX", output_X);
			intent.putExtra("outputY", output_Y);

			File croppedFile = new File(IMAGE_CROP);
			intent.putExtra("output", Uri.fromFile(croppedFile));
			intent.putExtra("outputFormat", "JPEG");
			intent.putExtra("return-data", true);
			startActivityForResult(intent, REQUEST_IMAGE_CROP);

		} else if (resultCode == Activity.RESULT_OK
				&& requestCode == MainActivity.REQUEST_IMAGE_CROP) {
			ocr_process(data);
		} else {
			Log.v(TAG, "User cancelled");
		}
	}

	private void ocr_process(Intent data) {
		Log.i(TAG, "on ocr_process");

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = inSampleSize;
		Bitmap bitmap = BitmapFactory.decodeFile(IMAGE_CROP, options);

		try {
			ExifInterface exif = new ExifInterface(IMAGE);
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

		Log.v(TAG, "BaseApi initializing...");
		TessBaseAPI baseApi = new TessBaseAPI();
		baseApi.setDebug(true);
		baseApi.init(DATA_PATH, lang);
		baseApi.setPageSegMode(TessBaseAPI.PageSegMode.PSM_AUTO_OSD);
		baseApi.setImage(bitmap);
		String recognizedText = baseApi.getUTF8Text();
		baseApi.end();

		recognizedText = recognizedText.trim();

		Intent intent = new Intent(this, ResultViewer.class);
		intent.putExtra(OCR_CONTENT, recognizedText);
		this.startActivity(intent);
	}
}
