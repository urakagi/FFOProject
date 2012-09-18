package org.sais.rasoid;

import java.io.File;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

public class CardViewFragment extends Fragment {

	private TextView _text;
	private ImageView _image;
	private int mCardnum;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.show_card, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		int cardnum = getActivity().getIntent().getIntExtra("cardnum", 1);
		_text = (TextView) getActivity().findViewById(R.id.text);
		_image = (ImageView) getActivity().findViewById(R.id.image);
		Display display = ((WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		int width = display.getWidth() - 20;
		_image.setLayoutParams(new LayoutParams(width, width * 29 / 20));
		setCard(cardnum);
	}

	private void setCard(int cardnum) {
		mCardnum = cardnum;
		_text.setText(Tools.getVisualString(getActivity(), cardnum));
		String path = "/sdcard/Rasoid/icon/" + cardnum + "big.jpg";
		File f = new File(path);
		if (f.exists()) {
			_image.setVisibility(View.VISIBLE);
			_image.setImageDrawable(Drawable.createFromPath(path));
		} else {
			_image.setVisibility(View.GONE);
		}
	}

}
