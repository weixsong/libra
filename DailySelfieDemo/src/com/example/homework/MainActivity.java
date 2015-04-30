package com.example.homework;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import android.app.ListActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.ListView;

public class MainActivity extends ListActivity{

	public static final String ALBUM = "homework";
	public static final String FOLDERPATH = Environment.getExternalStoragePublicDirectory(
			Environment.DIRECTORY_PICTURES).getAbsolutePath() + "/" + ALBUM + "/";
	
	private static final int REQUEST_IMAGE_CAPTURE = 1;
	private final String TAG = "homework"; 
	private final String dataFormat = "yyyyMMdd_HHmmss";
	private final int targetW = 100;
	private final int targetH = 100;
	
	private String currentImagPath;
	private String imageSaveFolderPath;
	private ListView listView;

	private RecordAdapter adapter;
	private NotificationManager notificationManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Set the list choice mode to allow multi items selection at a time
		listView = getListView();
		listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		listView.setMultiChoiceModeListener(mcmListener);

		imageSaveFolderPath = getAlbumStorageDir(ALBUM).toString();
		
		adapter = new RecordAdapter(this);
		this.setListAdapter(adapter);
		
		// async load images
		AsyncImageLoader asyncImageLoader = new AsyncImageLoader(this, adapter, targetW, targetH);
		asyncImageLoader.execute(imageSaveFolderPath);
		
		// start service
		// what the fuck, My phone Xiaomi 3 do not support start service on boot
		// it don't broadcaset android.intent.action.BOOT_COMPLETED
		//startService(new Intent(this, NotificationService.class));
		
		// alarm setting, notify by AlarmManager
		// but this way will not work if the application is destroyed or not running
		notificationManager = new NotificationManager(this);
		notificationManager.setAlarm();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	protected void onResume() {
		Log.i(TAG, "onResume");
		super.onResume();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.open_camera) {
			// TODO: open camera and take photo
			dispatchTakePictureIntent();
			return true;
		} else if (id == R.id.cancel_alarm) {

			if (notificationManager != null) {
				notificationManager.cancelAlarm();
			}
		} else if (id == R.id.set_alarm) {
			
			if (notificationManager != null) {
				notificationManager.setAlarm();
			}
		}
		return super.onOptionsItemSelected(item);
	}
	
	public String getImageSaveFolderPath() {
		return imageSaveFolderPath;
	}
	
	private void dispatchTakePictureIntent() {
	    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
	    // Ensure that there's a camera activity to handle the intent
	    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
	        // Create the File where the photo should go
	        File photoFile = null;
	        try {
	            photoFile = createImageFile();
	        } catch (IOException ex) {
	            // Error occurred while creating the File
	            Log.i(TAG, ex.getMessage());
	        }
	        // Continue only if the File was successfully created
	        if (photoFile != null) {
	            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
	                    Uri.fromFile(photoFile));
	            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
	        }
	    }
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.i(TAG, "onActivityResult");
	    if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
	    	Log.i(TAG, currentImagPath);
	    	
	    	// I found a bug of Android, oooooooops
	    	// there is a bug with Android when using MediaStore.EXTRA_OUTPUT
	        // Bundle extras = data.getExtras();
	        // Bitmap imageBitmap = (Bitmap) extras.get("data");
	    	
	    	AsyncImageLoader asyncImageLoader = new AsyncImageLoader(this, adapter, targetW, targetH);
			asyncImageLoader.execute(currentImagPath);
	    }
	}
	
	private File getAlbumStorageDir(String albumName) {
	    // Get the directory for the user's public pictures directory.
	    File file = new File(Environment.getExternalStoragePublicDirectory(
	            Environment.DIRECTORY_PICTURES), albumName);
	    if (!file.mkdirs()) {
	        Log.e(TAG, "Directory not created");
	    }
	    return file;
	}
	
	private File createImageFile() throws IOException {
	    // Create an image file name
	    String imageFileName = new SimpleDateFormat(dataFormat).format(new Date());
	    File image = new File(imageSaveFolderPath + "/" + imageFileName + ".jpg");
	    
	    currentImagPath = image.getAbsolutePath();
	    Log.i(TAG, currentImagPath);
	    return image;
	}
	
	MultiChoiceModeListener mcmListener = new MultiChoiceModeListener() {

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			// TODO Auto-generated method stub
			switch (item.getItemId()) {
			case R.id.delete:
				// Calls getSelectedIds method from ListViewAdapter Class
				SparseBooleanArray selected = adapter.getSelectedIds();
				// Captures all selected ids with a loop
				for (int i = (selected.size() - 1); i >= 0; i--) {
					if (selected.valueAt(i)) {
						Map<String, Object> selecteditem = (Map<String, Object>)adapter.getItem(selected.keyAt(i));
						// Remove selected items following the ids
						adapter.remove(selecteditem);
						
						String fileName = (String)selecteditem.get(RecordAdapter.KEY_NAME);
						fileName = MainActivity.FOLDERPATH + fileName;
						deleteImageFile(fileName);
					}
				}
				// Close CAB
				mode.finish();
				return true;
			default:
				return false;
			}
		}

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			// TODO Auto-generated method stub
			mode.getMenuInflater().inflate(R.menu.delete_item, menu);
			return true;
		}

		@Override
		public void onDestroyActionMode(ActionMode arg0) {
			// TODO Auto-generated method stub
			adapter.removeSelection();
		}

		@Override
		public boolean onPrepareActionMode(ActionMode arg0, Menu arg1) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void onItemCheckedStateChanged(ActionMode mode, int position,
				long id, boolean checked) {
			// TODO Auto-generated method stub
			final int checkedCount = listView.getCheckedItemCount();
			// Set the CAB title according to total checked items
			mode.setTitle(checkedCount + " Selected");
			// Calls toggleSelection method from ListViewAdapter Class
			adapter.addSelection(position);
		}
		
		private void deleteImageFile(String fileName) {
			File file = new File(fileName);
			try {
				file.delete();
			} catch (Exception e) {
				Log.i(TAG, e.toString());
			}
		}
	};
}
