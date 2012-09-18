package org.sais.rasoid.counter;

import java.util.Calendar;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.LinearLayout;

public class CounterRowView extends LinearLayout implements OnTouchListener {

	public static int SCREEN_WIDTH = -1;
	private int NARROW_WIDTH = 50;
	private int DPP = 40;

	private int value;
	private UView _type;
	private UView _amount;
	private String mId;
	private boolean mMirror;

	public CounterRowView(Context context, int id) {
		super(context);
		setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, 0, 1));
		setOrientation(LinearLayout.HORIZONTAL);
		mId = "RowId" + id;
		int def = id == 0 || id == 3 ? 0 : 20;
		value = PreferenceManager.getDefaultSharedPreferences(getContext())
				.getInt(mId, def);

		switch (id) {
		case 0:
			_type = new UView(getContext(), false, "呪力", Color.CYAN, 20);
			_amount = new UView(getContext(), true, String.valueOf(value),
					Color.CYAN, 72);
			mMirror = true;
			break;
		case 1:
			_type = new UView(getContext(), false, "体力", Color.YELLOW, 20);
			_amount = new UView(getContext(), true, String.valueOf(value),
					Color.YELLOW, 72);
			mMirror = true;
			break;
		case 2:
			_type = new UView(getContext(), false, "体力", Color.YELLOW, 20);
			_amount = new UView(getContext(), true, String.valueOf(value),
					Color.YELLOW, 72);
			mMirror = false;
			break;
		case 3:
			_type = new UView(getContext(), false, "呪力", Color.CYAN, 20);
			_amount = new UView(getContext(), true, String.valueOf(value),
					Color.CYAN, 72);
			mMirror = false;
			break;
		}

		_type.setOnTouchListener(this);
		_amount.setOnTouchListener(this);
		this.addView(_type);
		this.addView(_amount);
	}

	public class UView extends View {

		private String mText;
		private int mColor;
		private int mSize;

		public UView(Context context, boolean fill, String text, int color,
				int size) {
			super(context);
			if (fill) {
				setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
						LayoutParams.FILL_PARENT));
			} else {
				setLayoutParams(new LayoutParams(NARROW_WIDTH,
						LayoutParams.FILL_PARENT));
			}
			mText = text;
			mColor = color;
			mSize = size;
		}

		@Override
		protected void onDraw(Canvas canvas) {
			Paint paint = new Paint();
			Paint textpaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			paint.setColor(Color.WHITE);
			textpaint.setColor(Color.WHITE);
			paint.setStrokeWidth(2);
			textpaint.setTextSize(16);
			textpaint.setTextAlign(Align.CENTER);
			if (mMirror) {
				canvas.rotate(180);
				canvas.translate(-this.getWidth(), 0);
				int no = this.getWidth() > NARROW_WIDTH ? 0
						: ((SCREEN_WIDTH - NARROW_WIDTH) / DPP) + 2;
				for (int x = this.getWidth() > NARROW_WIDTH ? 0 : DPP
						- NARROW_WIDTH % DPP; x <= this.getWidth(); x += DPP - 2) {
					canvas.drawLine(x, -20, x, 0, paint);
					canvas.drawText(String.valueOf(no), x + 8, -4, textpaint);
					++no;
				}
				if (this.getWidth() <= NARROW_WIDTH) {
					canvas.drawText(String
							.valueOf((SCREEN_WIDTH - NARROW_WIDTH) / DPP + 1),
							4, -4, textpaint);
				}
			} else {
				int no = this.getWidth() > NARROW_WIDTH ? (NARROW_WIDTH / DPP) + 1
						: 0;
				for (int x = this.getWidth() > NARROW_WIDTH ? DPP
						- NARROW_WIDTH % DPP : 0; x <= this.getWidth(); x += DPP - 2) {
					canvas.drawLine(x, this.getHeight() - 20, x, this
							.getHeight(), paint);
					canvas.drawText(String.valueOf(no), x + 8,
							this.getHeight() - 6, textpaint);
					++no;
				}
			}

			paint = new Paint(Paint.ANTI_ALIAS_FLAG);
			paint.setColor(mColor);
			paint.setTextSize(mSize);
			paint.setTextAlign(Align.CENTER);
			if (mMirror) {
				canvas.translate(this.getWidth() / 2,
						-this.getHeight() / 2 + 25);
			} else {
				canvas
						.translate(this.getWidth() / 2,
								this.getHeight() / 2 + 25);
			}
			canvas.drawText(mText, 0, 0, paint);

		}
	}

	private float mBaseX;
	private float mBaseY;
	private int mBaseValue;

	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mBaseX = event.getX();
			mBaseY = event.getY();
			mBaseValue = value;
			return true;
		case MotionEvent.ACTION_UP:
			float deltaY = event.getY() - mBaseY;
			if (Math.abs(deltaY) > this.getHeight() / 2) {
				int delta;
				if (mMirror) {
					delta = deltaY > 0.0 ? 1 : -1;
				} else {
					delta = deltaY > 0.0 ? -1 : 1;
				}
				setValue(mBaseValue + delta);
			}

			HistoryDatabaseHelper db = new HistoryDatabaseHelper(getContext());
			db.openWritable();
			db.put(mId.charAt(mId.length() - 1), Calendar.getInstance(), value);
			db.shutdown();
			return true;
		case MotionEvent.ACTION_MOVE:
			int newValue;
			float deltaX = event.getX() - mBaseX;
			if (mMirror) {
				newValue = mBaseValue - (int) (deltaX / DPP);
			} else {
				newValue = mBaseValue + (int) (deltaX / DPP);
			}
			if (value != newValue) {
				setValue(newValue);
			}
			return true;
		}

		return false;
	}

	private void setValue(int value) {
		this.value = value;
		PreferenceManager.getDefaultSharedPreferences(getContext()).edit()
				.putInt(mId, value).commit();
		_amount.mText = String.valueOf(value);
		_amount.invalidate();
	}

	public void reset() {
		value = isHP() ? 20 : 0;
		setValue(value);
		HistoryDatabaseHelper db = new HistoryDatabaseHelper(getContext());
		db.openWritable();
		db.put(mId.charAt(mId.length() - 1), Calendar.getInstance(), value);
		db.shutdown();
	}

	private boolean isHP() {
		return "RowId0".equals(mId) || "RowId3".equals(mId) ? false : true;
	}

}
