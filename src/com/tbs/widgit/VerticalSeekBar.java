/* Copyright @ 2013 by Dave Truby. All rights reserved. */
package com.tbs.widgit;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ProgressBar;
import android.widget.SeekBar;

public class VerticalSeekBar extends AbsVerticalSeekBar {

	/**
	 * A callback that notifies clients when the progress level has been
	 * changed. This includes changes that were initiated by the user through a
	 * touch gesture or arrow key/trackball as well as changes that were
	 * initiated programmatically.
	 */
	public interface OnSeekBarChangeListener {

		/**
		 * Notification that the progress level has changed. Clients can use the
		 * fromUser parameter to distinguish user-initiated changes from those
		 * that occurred programmatically.
		 * 
		 * @param seekBar
		 *            The SeekBar whose progress has changed
		 * @param progress
		 *            The current progress level. This will be in the range
		 *            0..max where max was set by
		 *            {@link ProgressBar#setMax(int)}. (The default value for
		 *            max is 100.)
		 * @param fromUser
		 *            True if the progress change was initiated by the user.
		 */
		void onProgressChanged(VerticalSeekBar seekBar, int progress,
				boolean fromUser);

		/**
		 * Notification that the user has started a touch gesture. Clients may
		 * want to use this to disable advancing the seekbar.
		 * 
		 * @param seekBar
		 *            The SeekBar in which the touch gesture began
		 */
		void onStartTrackingTouch(VerticalSeekBar seekBar);

		/**
		 * Notification that the user has finished a touch gesture. Clients may
		 * want to use this to re-enable advancing the seekbar.
		 * 
		 * @param seekBar
		 *            The SeekBar in which the touch gesture began
		 */
		void onStopTrackingTouch(VerticalSeekBar seekBar);
	}

	private OnSeekBarChangeListener mOnSeekBarChangeListener;

	public VerticalSeekBar(final Context context) {
		this(context, null);
	}

	public VerticalSeekBar(final Context context, final AttributeSet attrs) {
		this(context, attrs, android.R.attr.seekBarStyle);
	}

	public VerticalSeekBar(final Context context, final AttributeSet attrs,
			final int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	void onProgressRefresh(final float scale, final boolean fromUser) {
		super.onProgressRefresh(scale, fromUser);

		if (this.mOnSeekBarChangeListener != null) {
			this.mOnSeekBarChangeListener.onProgressChanged(this,
					this.getProgress(), fromUser);
		}
	}

	/**
	 * Sets a listener to receive notifications of changes to the SeekBar's
	 * progress level. Also provides notifications of when the user starts and
	 * stops a touch gesture within the SeekBar.
	 * 
	 * @param l
	 *            The seek bar notification listener
	 * 
	 * @see SeekBar.OnSeekBarChangeListener
	 */
	public void setOnSeekBarChangeListener(final OnSeekBarChangeListener l) {
		this.mOnSeekBarChangeListener = l;
	}

	@Override
	void onStartTrackingTouch() {
		if (this.mOnSeekBarChangeListener != null) {
			this.mOnSeekBarChangeListener.onStartTrackingTouch(this);
		}
	}

	@Override
	void onStopTrackingTouch() {
		if (this.mOnSeekBarChangeListener != null) {
			this.mOnSeekBarChangeListener.onStopTrackingTouch(this);
		}
	}

}
