package org.sais.rasoid.deckeditor;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import org.sais.rasoid.R;
import org.sais.rasoid.Tools;
import org.sais.rasoid.card.ShowCardActivity;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ArrayAdapter;

public class DeckEditorActivity extends ListActivity {
	
	private static final String TAG = "Rasoid - DeckEditor";
	private static final int MESSAGE_DATABASE_READ = 0;
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getListView().setFastScrollEnabled(true);
		new ReadCardDataBaseThread().start();
	}
	
	private Handler mHandler = new Handler() {
		
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MESSAGE_DATABASE_READ:
				ArrayList<String> values = new ArrayList<String>();
				values.add(getResources().getString(R.string.help));
				for (Integer i : Tools.getIndices()) {
					values.add(Tools.getCard(i).cardnum + " " + Tools.getCard(i).cardname);
				}
				ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.listitem_simplerow, values);
				getListView().setAdapter(adapter);
				break;
			}
		};
	};
	
	protected void onListItemClick(android.widget.ListView l, android.view.View v, int position, long id) {
		Intent intent = new Intent(this, ShowCardActivity.class);
		intent.putExtra("cardnum", Tools.getIndices().get(position - 1)); // Minus 1 due to the picture hint
		startActivity(intent);
	};
	
	class ReadCardDataBaseThread extends Thread {
		@Override
		public void run() {
			try {
				Tools.readCardBase(getApplicationContext());
				Tools.readPronunciation(getApplicationContext());
				mHandler.sendEmptyMessage(MESSAGE_DATABASE_READ);
			} catch (FileNotFoundException e) {
				Log.e(TAG, "", e);
			} catch (IOException e) {
				Log.e(TAG, "", e);
			}
		}
	}
	
}
