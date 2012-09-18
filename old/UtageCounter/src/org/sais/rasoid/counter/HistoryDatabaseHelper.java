package org.sais.rasoid.counter;

import java.util.Calendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class HistoryDatabaseHelper extends SQLiteOpenHelper {
	
	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "utagecounter";
	private static final String[] TABLES_NAME = new String[] { "utagehistory0",
			"utagehistory1", "utagehistory2", "utagehistory3" };
	public static final String COL_TIME = "time";
	public static final String COL_VALUE = "value";
	private Context ctx;
	
	private SQLiteDatabase mDb;

	public HistoryDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		ctx = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		for (int i = 0; i < TABLES_NAME.length; ++i) {
			db.execSQL(getQueryCreate(i));
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		for (String t : TABLES_NAME) {
			db.execSQL("DROP TABLE IF EXISTS " + t);
		}
		onCreate(db);
	}

	private static String getQueryCreate(int index) {
		return "create table " + TABLES_NAME[index] + " (" + COL_TIME
				+ " integer, " + COL_VALUE + " integer" + ");";
	}
	
	public void openWritable() {
		mDb = getWritableDatabase();
	}

	public void openreadable() {
		mDb = getReadableDatabase();
	}
	
	public void shutdown() {
		mDb.close();
		close();
	}

	public void put(char id, Calendar cal, int value) {
		ContentValues values = new ContentValues();
		values.put(COL_TIME, cal.getTimeInMillis());
		values.put(COL_VALUE, value);
		mDb.insert("utagehistory" + id, null, values);
	}
	
	public Cursor list(char id) {
		return mDb.query("utagehistory" + id, null, null, null, null, null, COL_TIME + " DESC");
	}
	
	public void clear(final Activity act) {
		AlertDialog.Builder b = new AlertDialog.Builder(ctx);
		b.setMessage("mjd?");
		b.setTitle("clear");
		b.setPositiveButton("はいはい", new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				openWritable();
				for (String s : TABLES_NAME) {
					mDb.delete(s, null, null);
				}
				shutdown();
				act.finish();	
			}
		});
		b.setNegativeButton("ないわー", new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
			}
		});
		b.show();

	}
	
}
