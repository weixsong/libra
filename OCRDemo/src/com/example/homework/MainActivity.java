package com.example.homework;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends Activity {

	public static final String ALBUM = "OCR";
	public static final String OCR_CONTENT = "OCR_CONTENT";

	private static final String TAG = "MainActivity";

	private static final int REQUEST_IMAGE_CAPTURE = 1;
	public static String LANG = "chi_sim";

	private static final String TESSDATA = "tessdata";
	private static final String IMAGENAME = "ocr.jpg";

	public static final String DATA_PATH = Environment
			.getExternalStorageDirectory().toString() + "/" + ALBUM + "/";
	private static final String DATA_PATH_TESSDATA = DATA_PATH + TESSDATA + "/";

	public static final String IMAGE_PATH = MainActivity.DATA_PATH + IMAGENAME;

	private final String traineddata_path = DATA_PATH_TESSDATA + LANG
			+ ".traineddata";
	private final String asset_tessdata = "tessdata/" + LANG + ".traineddata";

	private ProgressDialog mProgressDialog;
	private TesstwoOCR ocr;
	
	private int targetW = 800;
	private int targetH = 600;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);

		// check and copy files
		checkAndCopyFiles();

		ocr = new TesstwoOCR();
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

				Log.v(TAG, "Copied " + LANG + " traineddata");
			} catch (IOException e) {
				Log.e(TAG,
						"unable to copy " + LANG + " traineddata "
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
		
		if (id == R.id.setting) {
			// TODO: setting which language to use
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	private void dispatchTakePictureIntent() {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

		// Ensure that there's a camera activity to handle the intent
		if (intent.resolveActivity(getPackageManager()) != null) {
			// Create the File where the photo should go
			File photoFile = new File(IMAGE_PATH);
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
			ocr_process();
		} else {
			Log.v(TAG, "User cancelled");
		}
	}

	private void ocr_process() {
		Log.i(TAG, "on ocr_process");

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(IMAGE_PATH, options);
		int photoW = options.outWidth;
		int photoH = options.outHeight;

		// Determine how much to scale down the image
		int scaleFactor = Math.min(photoW / targetW, photoH / targetH);
		options.inJustDecodeBounds = false;
		options.inSampleSize = scaleFactor << 1;
		options.inPurgeable = true;

		Bitmap bitmap = BitmapFactory.decodeFile(IMAGE_PATH, options);
		doOCR(bitmap);
	}

	private void doOCR(final Bitmap bitmap) {
		if (mProgressDialog == null) {
			mProgressDialog = ProgressDialog.show(this, "Processing",
					"Doing OCR...", true);
		} else {
			mProgressDialog.show();
		}

		new Thread(new Runnable() {
			public void run() {

				final String result = ocr.doOCR(bitmap);
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Intent intent = new Intent(MainActivity.this,
								ResultViewer.class);
						intent.putExtra(OCR_CONTENT, result);
						MainActivity.this.startActivity(intent);
						mProgressDialog.dismiss();
					}
				});

			};
		}).start();
	}
}
