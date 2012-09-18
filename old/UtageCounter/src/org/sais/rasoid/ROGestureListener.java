package org.sais.rasoid;

import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class ROGestureListener implements OnTouchListener {
	/*
	 * Use GestureString to save gestures. Like "RL", "UD", "UR", "R"
	 */

	protected float mX;
	protected float mY;
	protected int mCurrentDirection;
	protected long mEventTime;
	protected float mMovement; // The movement has drafted in mCurrentDirection
	protected ROGestureCallback caller;

	protected String mGestureString;

	// Constants
	protected static final float Threshold_Movement = 12;

	protected static final int DIR_NONE = -1;
	protected static final int DIR_UP = 0;
	protected static final int DIR_DOWN = 1;
	protected static final int DIR_LEFT = 2;
	protected static final int DIR_RIGHT = 3;

	public ROGestureListener(ROGestureCallback caller) {
		this.caller = caller;
	}

	public boolean onTouch(View v, MotionEvent ev) {
		// return true if gesture is detected or canceled, which means the event
		// is consumed here
		int motion = ev.getAction();

		switch (motion) {
		case MotionEvent.ACTION_DOWN:
			startGesture(ev);
			break;
		case MotionEvent.ACTION_UP:
			if (mMovement > Threshold_Movement) {
				updateGestureString();
			}
			// If user stops 1 seconds before release finger, stops the gesture
			if (ev.getEventTime() - mEventTime > 1000) {
				mGestureString = "";
				caller.onGestureCancel();
				return false;
			} else {
				if (mGestureString.equals("") && mMovement < 7) {
					if (ev.getX() < v.getWidth() / 3) {
						mGestureString = "CL";
					} else if (ev.getX() > v.getWidth() * 2 / 3) {
						mGestureString = "CR";
					}
				}
			}
			caller.onGestureDone(mGestureString);
			return true;
		case MotionEvent.ACTION_MOVE:
			if (!identify(ev)) {
				return false;
			}
			break;
		default:
		}
		return false;
	}

	protected void startGesture(MotionEvent ev) {
		mX = ev.getX();
		mY = ev.getY();
		mMovement = 0f;
		mGestureString = "";
		mCurrentDirection = DIR_NONE;
		mEventTime = ev.getEventTime();
	}

	protected boolean identify(MotionEvent ev) {
		// Return true if direction changing is sured
		float moveX = ev.getX() - mX;
		float moveY = ev.getY() - mY;
		int dir;

		if (moveX == 0) {
			if (moveY < 0) {
				dir = DIR_UP;
				mMovement += moveY;
				return false;
			} else if (moveY > 0) {
				dir = DIR_DOWN;
				mMovement += moveY;
				return false;
			} else {
				dir = DIR_NONE;
			}
		} else {
			float slope = moveY / moveX;
			if (slope < 0.7 && slope > -0.7) {
				// Horizontal
				if (moveX > 0) {
					dir = DIR_RIGHT;
				} else {
					dir = DIR_LEFT;
				}
			} else if (slope > 1.2 || slope < -1.2) {
				// Vertical
				if (moveY > 0) {
					dir = DIR_DOWN;
					mMovement += (float) Math.sqrt(moveX * moveX + moveY
							* moveY);
					return false;
				} else {
					dir = DIR_UP;
					mMovement += (float) Math.sqrt(moveX * moveX + moveY
							* moveY);
					return false;
				}
			} else {
				dir = DIR_NONE;
			}
		}

		// Update informations
		mX = ev.getX();
		mY = ev.getY();
		mEventTime = ev.getEventTime();

		float currentMovement = (float) Math
				.sqrt(moveX * moveX + moveY * moveY);
		if (dir == mCurrentDirection || mCurrentDirection == DIR_NONE) {
			mCurrentDirection = dir;
			mMovement += currentMovement;
		} else {
			if (mMovement > Threshold_Movement) {
				updateGestureString();
				mCurrentDirection = dir;
				mMovement = currentMovement;

			} else {
				if (mGestureString.length() == 0) {
					mCurrentDirection = DIR_NONE;
					mMovement = currentMovement;
				} else {
					switch (mGestureString.charAt(mGestureString.length() - 1)) {
					case 'U':
						mCurrentDirection = DIR_UP;
						break;
					case 'D':
						mCurrentDirection = DIR_DOWN;
						break;
					case 'L':
						mCurrentDirection = DIR_LEFT;
						break;
					case 'R':
						mCurrentDirection = DIR_RIGHT;
						break;
					}

					mGestureString = mGestureString.substring(0,
							mGestureString.length() - 1);
					mMovement = Threshold_Movement + 1;
				}

			}
		}
		return true;
	}

	protected void updateGestureString() {
		switch (mCurrentDirection) {
		case DIR_NONE:
			break;
		case DIR_UP:
			mGestureString += "U";
			break;
		case DIR_DOWN:
			mGestureString += "D";
			break;
		case DIR_LEFT:
			mGestureString += "L";
			break;
		case DIR_RIGHT:
			mGestureString += "R";
			break;
		}
	}

	public String getGestureString() {
		return mGestureString;
	}

	public interface ROGestureCallback {
		void onGestureDone(String gestureString);

		void onGestureCancel();
	}

}
