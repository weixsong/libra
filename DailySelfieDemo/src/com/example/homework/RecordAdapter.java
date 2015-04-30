package com.example.homework;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class RecordAdapter extends BaseAdapter{

	public static final String DATA = "data";
	public static final String KEY_NAME = "name";
	public static final String KEY_BITMAP = "bitmap";
	
	private final String TAG = "com.example.homework.RecoredAdapter";
	private final Context mContext;
	
	private List<Map<String, Object>> list;
	private SparseBooleanArray mSelectedItemsIds;
	
	public RecordAdapter(Context context) {
		mContext = context;
		list = new ArrayList<Map<String, Object>>();
		mSelectedItemsIds = new SparseBooleanArray();
	}
	
	public void addRecord(Map<String, Object> map) {
		Log.i(TAG, "add record");
		list.add(map);
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return list.size();
	}
	
	public void clear() {
		list.clear();
		this.notifyDataSetChanged();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return list.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}
	
	public void addSelection(int position) {
		mSelectedItemsIds.put(position, true);
    }
	
	public void removeSelection() {
        mSelectedItemsIds.clear();
        notifyDataSetChanged();
    }
	
    public void remove(Map<String, Object> object) {
		list.remove(object);
        notifyDataSetChanged();
    }
    
    public SparseBooleanArray getSelectedIds() {
        return mSelectedItemsIds;
    }

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		Map<String, Object> map = (Map<String, Object>)getItem(position);
		
		RelativeLayout layout = (RelativeLayout)convertView;
		if (convertView == null) {
			LayoutInflater inflater = LayoutInflater.from(mContext);
			layout = (RelativeLayout)inflater.inflate(R.layout.recored_item, null);
		}
		
		final String name = (String)map.get(KEY_NAME);
		final TextView titleView = (TextView)layout.findViewById(R.id.image_name);
		titleView.setText(name);
		
		final ImageView imageView = (ImageView)layout.findViewById(R.id.image);
		imageView.setImageBitmap((Bitmap)map.get(KEY_BITMAP));
		
		imageView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//Toast.makeText(mContext, "image clicked", Toast.LENGTH_SHORT).show();
				Intent intent = new Intent(mContext, ImageViewerActivity.class);
				intent.putExtra(DATA, name);
				mContext.startActivity(intent);
			}
		});
		
		return layout;
	}

}
