package com.example.ndk;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.widget.Button;
import android.view.View;
import android.widget.ImageView;

public class MainActivity extends Activity {

	ImageView imgView;
	Button btnNDK, btnRestore;
	
	private String title = "Canny detect by NDK";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		this.setTitle(title);
		btnRestore = (Button) this.findViewById(R.id.btnRestore);
		btnRestore.setOnClickListener(new ClickEvent());
		btnNDK = (Button) this.findViewById(R.id.btnNDK);
		btnNDK.setOnClickListener(new ClickEvent());
		imgView = (ImageView) this.findViewById(R.id.ImageView01);
		Bitmap img = ((BitmapDrawable) getResources().getDrawable(
				R.drawable.lena)).getBitmap();
		imgView.setImageBitmap(img);
	}

	class ClickEvent implements View.OnClickListener {
		public void onClick(View v) {
			if (v == btnNDK) {
				long current = System.currentTimeMillis();
				Bitmap img1 = ((BitmapDrawable) getResources().getDrawable(
						R.drawable.lena)).getBitmap();
				int w = img1.getWidth(), h = img1.getHeight();
				int[] pix = new int[w * h];
				img1.getPixels(pix, 0, w, 0, 0, w, h);
				int[] resultInt = CannyDetect.cannyDetect(pix, w, h);
				Bitmap resultImg = Bitmap.createBitmap(w, h, Config.RGB_565);
				resultImg.setPixels(resultInt, 0, w, 0, 0, w, h);
				long performance = System.currentTimeMillis() - current;
				imgView.setImageBitmap(resultImg);
				MainActivity.this.setTitle("NDK consumed: "
						+ String.valueOf(performance) + " ms");
			} else if (v == btnRestore) {
				Bitmap img2 = ((BitmapDrawable) getResources().getDrawable(
						R.drawable.lena)).getBitmap();
				imgView.setImageBitmap(img2);
				MainActivity.this.setTitle("使用OpenCV进行图像处理");
			}
		}
	}
}
