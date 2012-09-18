package org.sais.rasoid.watcher;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;

import org.sais.rasoid.R;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

public class WatcherActivity extends Activity {

	private Socket mSocket;
	private TextView _text;
	private ScrollView _scroll;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.watcher);
		_text = (TextView) findViewById(R.id.text);
		_scroll = (ScrollView) findViewById(R.id.scroll);
		((Button) findViewById(R.id.connect))
				.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						String host = ((EditText) findViewById(R.id.ip))
								.getText().toString();
						int port = 12700;
						String ip = host;
						if (ip.contains(":")) {
							port = Integer.parseInt(host.split(":")[1]);
							ip = host.split(":")[0];
						}
						show("Connecting " + ip + ":" + port + "...");
						new WatchTask(ip, port).execute();
					}
				});

	}

	@Override
	protected void onDestroy() {
		try {
			if (mSocket != null) {
				BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
						mSocket.getOutputStream(), "Unicode"), 8192);
				out.write("$DISCONNECT:");
				out.newLine();
				out.flush();
				mSocket.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		mSocket = null;
		super.onDestroy();
	}

	class WatchTask extends AsyncTask<Void, String, Void> {

		private String ip;
		private int port;

		public WatchTask(String ip, int port) {
			this.ip = ip;
			this.port = port;
		}

		@Override
		protected Void doInBackground(Void... params) {
			try {
				mSocket = new Socket(ip, port);
				BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
						mSocket.getOutputStream(), "Unicode"), 8192);
				out.write("$IWANTTOWATCH:");
				out.newLine();
				out.flush();
				publishProgress("Connected.");
				BufferedReader in = new BufferedReader(new InputStreamReader(
						mSocket.getInputStream(), "Unicode"), 8192);
				for (String line = in.readLine(); line != null; line = in
						.readLine()) {
					if (line.indexOf("$") < 0) {
						continue;
					}
					line = line.substring(line.indexOf("$"));
					if (line.startsWith("$YOUAREWATCHER:")) {
						out.write("$WATCHER:ラソイド");
						out.newLine();
						out.flush();
					}
					if (line.startsWith("$WATCHCLIENT:")) {
						line = line.substring("$WATCHCLIENT:".length());
					}
					if (line.startsWith("$WATCHHOST:")) {
						line = line.substring("$WATCHHOST:".length());
					}
					String cmd;
					cmd = "$REPLAY:";
					if (line.startsWith(cmd)) {
						publishProgress(line.substring(cmd.length()));
						continue;
					}
					cmd = "$REPLAYWITHNEWLINE:";
					if (line.startsWith(cmd)) {
						publishProgress(line.substring(cmd.length()));
						continue;
					}
					cmd = "$CHAT:";
					if (line.startsWith(cmd)) {
						publishProgress(line.substring(cmd.length()));
						continue;
					}
					cmd = "$MSG:";
					if (line.startsWith(cmd)) {
						publishProgress(line.substring(cmd.length()));
						continue;
					}
					cmd = "$DECKNAME:";
					if (line.startsWith(cmd)) {
						publishProgress(line.substring(cmd.length()));
						continue;
					}
					cmd = "$DECKCHAR:";
					if (line.startsWith(cmd)) {
						publishProgress(line.substring(cmd.length()));
						continue;
					}
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(String... values) {
			show(values[0]);
		}
		
	}

	protected void show(String message) {
		_text.setText(_text.getText() + message + "\n");
		if (_text.getHeight() - _scroll.getScrollY() - _scroll.getHeight() <= 100) {
			_scroll.smoothScrollTo(0, _text.getHeight());
		}
	}

}