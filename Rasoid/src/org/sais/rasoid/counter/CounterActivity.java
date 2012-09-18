package org.sais.rasoid.counter;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.sais.rasoid.R;
import org.sais.rasoid.SLog;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore.Images;
import android.util.DisplayMetrics;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;

public class CounterActivity extends Activity {

	private static final int REQUEST_PICK_IMAGE = 1;
	private LinearLayout _panel;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.counter);
		initViews();
	}

	private void initViews() {
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		CounterRowView.SCREEN_WIDTH = metrics.widthPixels;

		_panel = (LinearLayout) findViewById(R.id.panel);
		for (int i = 0; i < 4; ++i) {
			_panel.addView(new CounterRowView(this, i));
			View _sep = new View(this);
			_sep.setBackgroundColor(Color.WHITE);
			_sep.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, 1));
			_panel.addView(_sep);
		}
		String suri = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("counterbg",
				null);
		if (suri == null) {
			_panel.setBackgroundResource(R.drawable.hina);
		} else {
			try {
				Bitmap bitmap = Images.Media.getBitmap(getContentResolver(), Uri.parse(suri));
				_panel.setBackgroundDrawable(new BitmapDrawable(getResources(), bitmap));
			} catch (Exception e) {
				SLog.e("", e);
				Toast.makeText(this, e.getClass().getName(), Toast.LENGTH_SHORT).show();
				PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().remove("counterbg")
						.commit();
			} catch (Error e) {
				SLog.e("", e);
				Toast.makeText(this, e.getClass().getName(), Toast.LENGTH_SHORT).show();
				PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().remove("counterbg")
						.commit();
			}
		}

	}

	public boolean onCreateOptionsMenu(android.view.Menu menu) {
		super.onCreateOptionsMenu(menu);

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_main, menu);

		return true;
	};

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.reset:
			AlertDialog.Builder b = new AlertDialog.Builder(this);
			b.setMessage("mjd?");
			b.setTitle("reset");
			b.setPositiveButton("はいはい", new OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					for (int i = 0; i < _panel.getChildCount(); ++i) {
						if (_panel.getChildAt(i) instanceof CounterRowView) {
							((CounterRowView) _panel.getChildAt(i)).reset();
						}
					}
				}
			});
			b.setNegativeButton("ないわー", new OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
				}
			});
			b.show();
			return true;
		case R.id.history:
			Intent intent = new Intent(this, HistoryActivity.class);
			startActivity(intent);
			return true;
		case R.id.bg:
			startActivityForResult(new Intent(Intent.ACTION_PICK,
					android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI), REQUEST_PICK_IMAGE);
			return true;
		case R.id.defbg:
			PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().remove("counterbg").commit();
			_panel.setBackgroundResource(R.drawable.hina);
			return true;
		}
		return false;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_PICK_IMAGE:
			if (resultCode == RESULT_OK) {
				try {
					Uri uri = data.getData();
					PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit()
							.putString("counterbg", uri.toString()).commit();
					Bitmap bitmap = Images.Media.getBitmap(getContentResolver(), uri);
					_panel.setBackgroundDrawable(new BitmapDrawable(getResources(), bitmap));
				} catch (FileNotFoundException e) {
					SLog.e("", e);
				} catch (IOException e) {
					SLog.e("", e);
				}
			}
			return;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

}