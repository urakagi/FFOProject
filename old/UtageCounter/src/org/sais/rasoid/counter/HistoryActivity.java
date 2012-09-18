package org.sais.rasoid.counter;

import org.sais.rasoid.R;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

public class HistoryActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.history);
		initViews();
	}

	private void initViews() {
		HistoryDatabaseHelper db = new HistoryDatabaseHelper(this);
		db.openreadable();
		initRow((LinearLayout) findViewById(R.id.row1), db.list('0'));
		initRow((LinearLayout) findViewById(R.id.row2), db.list('1'));
		initRow((LinearLayout) findViewById(R.id.row3), db.list('2'));
		initRow((LinearLayout) findViewById(R.id.row4), db.list('3'));
		db.shutdown();
	}

	private void initRow(LinearLayout line, Cursor c) {
		TextView v = new TextView(line.getContext());
		v.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.FILL_PARENT));
		StringBuffer mes = new StringBuffer();
		long lastdiff = Long.MIN_VALUE;
		if (c != null) {
			while (c.moveToNext()) {
				long timediff = System.currentTimeMillis()
						- c
								.getLong(c
										.getColumnIndexOrThrow(HistoryDatabaseHelper.COL_TIME));
				long diffdiff = timediff - lastdiff;
				if (diffdiff < 5000L && diffdiff > 0) {
					continue;
				}
				String diff;
				if (timediff > 86400000L) {
					diff = "*";
				} else {
					long sec = (timediff / 1000L) % 60L;
					long min = timediff / 60000L;
					diff = min + "'" + sec + "\"";
				}
				int value = c
						.getInt(c
								.getColumnIndexOrThrow(HistoryDatabaseHelper.COL_VALUE));

				mes.append(value + " (" + diff + ")←");
				lastdiff = timediff;
			}
		}
		c.close();
		v.setText(mes.toString());
		v.setTextSize(20);
		v.setPadding(3, 0, 3, 0);
		line.addView(v);
	}

	public boolean onCreateOptionsMenu(android.view.Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, 0, 0, "履歴削除");
		return true;
	};

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 0:
			HistoryDatabaseHelper db = new HistoryDatabaseHelper(this);
			db.clear(this);
			break;
		}
		return false;
	}

}
