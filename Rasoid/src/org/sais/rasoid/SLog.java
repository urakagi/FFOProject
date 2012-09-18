package org.sais.rasoid;

import java.util.HashMap;

import android.util.Log;

public class SLog {

	private static final String TAG = "Rasoid";

	public static final boolean sDebuggable = true;

	public static void e(String message) {
		if (sDebuggable)
			Log.e(TAG, message);
	}

	public static void e(String message, Throwable e) {
		if (sDebuggable)
			Log.e(TAG, message, e);
	}

	public static void w(String message) {
		if (sDebuggable)
			Log.w(TAG, message);
	}

	public static void w(String message, Throwable e) {
		if (sDebuggable)
			Log.w(TAG, message, e);
	}

	public static void i(String message) {
		if (sDebuggable)
			Log.i(TAG, message);
	}

	public static void i(String message, Throwable e) {
		if (sDebuggable)
			Log.i(TAG, message, e);
	}

	public static void d(Object message) {
		if (sDebuggable)
			if (message == null)
				Log.d(TAG, "null");
			else
				Log.d(TAG, message.toString() + " ");

	}

	public static void d(Object message, Throwable e) {
		if (sDebuggable)
			if (message == null)
				Log.d(TAG, "null", e);
			else
				Log.d(TAG, message.toString() + " ", e);

	}

	public static void v(Object message) {
		if (sDebuggable)
			if (message == null)
				Log.v(TAG, "null");
			else
				Log.v(TAG, message.toString() + " ");
	}

	public static void v(Object message, Throwable e) {
		if (sDebuggable)
			if (message == null)
				Log.v(TAG, "null", e);
			else
				Log.v(TAG, message.toString() + " ", e);
	}

	public static void currentMethod() {
		if (sDebuggable) {
			Throwable t = new Throwable();
			Log.v(TAG, "==== " + t.getStackTrace()[1].getClassName() + "#"
					+ t.getStackTrace()[1].getMethodName() + " ====");
		}
	}

	static HashMap<Long, Long> sIntervals = new HashMap<Long, Long>();

	public static void logInterval(String message) {
		if (sDebuggable) {
			Long threadId = Thread.currentThread().getId();
			if (sIntervals.containsKey(threadId)) {
				long delta = System.currentTimeMillis()
						- sIntervals.get(threadId);
				Log.d(TAG, message + " - " + delta + " ms.");
				sIntervals.put(threadId, System.currentTimeMillis());
			} else {
				Log.d(TAG, message + " - Interval start.");
				sIntervals.put(threadId, System.currentTimeMillis());
			}
		}
	}

}
