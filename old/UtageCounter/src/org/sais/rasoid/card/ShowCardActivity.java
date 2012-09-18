package org.sais.rasoid.card;

import java.io.File;
import java.util.ArrayList;

import org.sais.rasoid.R;
import org.sais.rasoid.ROGestureListener;
import org.sais.rasoid.ROGestureListener.ROGestureCallback;
import org.sais.rasoid.Tools;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;

public class ShowCardActivity extends Activity implements ROGestureCallback {

	private TextView _text;
	private ImageView _image;
	private int mCardnum;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setWindowAnimations(android.R.anim.slide_out_right);

		setContentView(R.layout.show_card);
		int cardnum = getIntent().getIntExtra("cardnum", 1);
		ROGestureListener lis = new ROGestureListener(this);
		((ScrollView) findViewById(R.id.outer)).setOnTouchListener(lis);
		_text = (TextView) findViewById(R.id.text);
		_image = (ImageView) findViewById(R.id.image);
		Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE))
				.getDefaultDisplay();
		int width = display.getWidth() - 20;
		_image.setLayoutParams(new LayoutParams(width, width * 29 / 20));
		setCard(cardnum);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}
	
	private void setCard(int cardnum) {
		mCardnum = cardnum;
		_text.setText(Tools.getVisualString(getApplicationContext(), cardnum));
		String path = "/sdcard/Rasoid/icon/" + cardnum + "big.jpg";
		File f = new File(path);
		if (f.exists()) {
			_image.setVisibility(View.VISIBLE);
			_image.setImageDrawable(Drawable.createFromPath(path));
		} else {
			_image.setVisibility(View.GONE);
		}
	}

	public void onGestureDone(String gestureString) {
		try {
			if (gestureString.endsWith("L")) {
				ArrayList<Integer> indices = Tools.getIndices();
				setCard(indices.get(indices.indexOf(mCardnum) - 1));
			} else if (gestureString.endsWith("R")) {
				ArrayList<Integer> indices = Tools.getIndices();
				setCard(indices.get(indices.indexOf(mCardnum) + 1));
			}
		} catch (Exception e) {
			// TODO: Cycle
		}
	}

	public void onGestureCancel() {
	}

}
