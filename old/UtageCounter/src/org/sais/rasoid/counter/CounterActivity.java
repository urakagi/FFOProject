package org.sais.rasoid.counter;

import org.sais.rasoid.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

public class CounterActivity extends Activity {

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
		_panel.setBackgroundResource(R.drawable.hina);
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
		}
		return false;
	}

}