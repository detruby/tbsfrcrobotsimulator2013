/* Copyright @ 2013 by Dave Truby. All rights reserved. */
package com.tbs.widgit;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.tbs.frc.robotsim.R;

public class AbsVerticalSeekBar extends VerticalProgressBar {

	private Drawable mThumb;
	private int mThumbOffset;

	/**
	 * On touch, this offset plus the scaled value from the position of the
	 * touch will form the progress value. Usually 0.
	 */
	float mTouchProgressOffset;

	/**
	 * Whether this is user seekable.
	 */
	boolean mIsUserSeekable = true;

	/**
	 * On key presses (right or left), the amount to increment/decrement the
	 * progress.
	 */
	private int mKeyProgressIncrement = 1;

	private static final int NO_ALPHA = 0xFF;
	private float mDisabledAlpha;

	public AbsVerticalSeekBar(final Context context) {
		super(context);
	}

	public AbsVerticalSeekBar(final Context context, final AttributeSet attrs) {
		super(context, attrs);
	}

	public AbsVerticalSeekBar(final Context context, final AttributeSet attrs,
			final int defStyle) {
		super(context, attrs, defStyle);

		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.SeekBar, defStyle, 0);
		Drawable thumb = a.getDrawable(R.styleable.SeekBar_android_thumb);
		this.setThumb(thumb);

		int thumbOffset = a.getDimensionPixelOffset(
				R.styleable.SeekBar_android_thumbOffset, this.getThumbOffset());
		this.setThumbOffset(thumbOffset);
		a.recycle();

		a = context.obtainStyledAttributes(attrs, R.styleable.Theme, 0, 0);
		this.mDisabledAlpha = a.getFloat(
				R.styleable.Theme_android_disabledAlpha, 0.5f);
		a.recycle();
	}

	/**
	 * Sets the thumb that will be drawn at the end of the progress meter within
	 * the SeekBar.
	 * <p>
	 * If the thumb is a valid drawable (i.e. not null), half its width will be
	 * used as the new thumb offset (@see #setThumbOffset(int)).
	 * 
	 * @param thumb
	 *            Drawable representing the thumb
	 */
	public void setThumb(final Drawable thumb) {
		if (thumb != null) {
			thumb.setCallback(this);

			// Assuming the thumb drawable is symmetric, set the thumb offset
			// such that the thumb will hang halfway off either edge of the
			// progress bar.
			this.mThumbOffset = thumb.getIntrinsicHeight() / 2;
		}
		this.mThumb = thumb;
		this.invalidate();
	}

	/**
	 * @see #setThumbOffset(int)
	 */
	public int getThumbOffset() {
		return this.mThumbOffset;
	}

	/**
	 * Sets the thumb offset that allows the thumb to extend out of the range of
	 * the track.
	 * 
	 * @param thumbOffset
	 *            The offset amount in pixels.
	 */
	public void setThumbOffset(final int thumbOffset) {
		this.mThumbOffset = thumbOffset;
		this.invalidate();
	}

	/**
	 * Sets the amount of progress changed via the arrow keys.
	 * 
	 * @param increment
	 *            The amount to increment or decrement when the user presses the
	 *            arrow keys.
	 */
	public void setKeyProgressIncrement(final int increment) {
		this.mKeyProgressIncrement = increment < 0 ? -increment : increment;
	}

	/**
	 * Returns the amount of progress changed via the arrow keys.
	 * <p>
	 * By default, this will be a value that is derived from the max progress.
	 * 
	 * @return The amount to increment or decrement when the user presses the
	 *         arrow keys. This will be positive.
	 */
	public int getKeyProgressIncrement() {
		return this.mKeyProgressIncrement;
	}

	@Override
	public synchronized void setMax(final int max) {
		super.setMax(max);

		if ((this.mKeyProgressIncrement == 0)
				|| (this.getMax() / this.mKeyProgressIncrement > 20)) {
			// It will take the user too long to change this via keys, change it
			// to something more reasonable
			this.setKeyProgressIncrement(Math.max(1,
					Math.round((float) this.getMax() / 20)));
		}
	}

	@Override
	protected boolean verifyDrawable(final Drawable who) {
		return who == this.mThumb || super.verifyDrawable(who);
	}

	@Override
	protected void drawableStateChanged() {
		super.drawableStateChanged();

		Drawable progressDrawable = this.getProgressDrawable();
		if (progressDrawable != null) {
			progressDrawable.setAlpha(this.isEnabled() ? NO_ALPHA
					: (int) (NO_ALPHA * this.mDisabledAlpha));
		}

		if (this.mThumb != null && this.mThumb.isStateful()) {
			int[] state = this.getDrawableState();
			this.mThumb.setState(state);
		}
	}

	@Override
	void onProgressRefresh(final float scale, final boolean fromUser) {
		Drawable thumb = this.mThumb;
		if (thumb != null) {
			this.setThumbPos(this.getHeight(), thumb, scale, Integer.MIN_VALUE);
			/*
			 * Since we draw translated, the drawable's bounds that it signals
			 * for invalidation won't be the actual bounds we want invalidated,
			 * so just invalidate this whole view.
			 */
			this.invalidate();
		}
	}

	@Override
	protected void onSizeChanged(final int w, final int h, final int oldw,
			final int oldh) {
		Drawable d = this.getCurrentDrawable();
		Drawable thumb = this.mThumb;
		int thumbWidth = thumb == null ? 0 : thumb.getIntrinsicWidth();
		// The max height does not incorporate padding, whereas the height
		// parameter does
		int trackWidth = Math.min(this.mMaxWidth, w - this.mPaddingRight
				- this.mPaddingLeft);
		int max = this.getMax();
		float scale = max > 0 ? (float) this.getProgress() / (float) max : 0;

		if (thumbWidth > trackWidth) {
			int gapForCenteringTrack = (thumbWidth - trackWidth) / 2;
			if (thumb != null) {
				this.setThumbPos(h, thumb, scale, gapForCenteringTrack * -1);
			}
			if (d != null) {
				// Canvas will be translated by the padding, so 0,0 is where we
				// start drawing
				d.setBounds(gapForCenteringTrack, 0, w - this.mPaddingRight
						- this.mPaddingLeft - gapForCenteringTrack, h
						- this.mPaddingBottom - this.mPaddingTop);
			}
		} else {
			if (d != null) {
				// Canvas will be translated by the padding, so 0,0 is where we
				// start drawing
				d.setBounds(0, 0, w - this.mPaddingRight - this.mPaddingLeft, h
						- this.mPaddingBottom - this.mPaddingTop);
			}
			int gap = (trackWidth - thumbWidth) / 2;
			if (thumb != null) {
				this.setThumbPos(h, thumb, scale, gap);
			}
		}
	}

	/**
	 * @param gap
	 *            If set to {@link Integer#MIN_VALUE}, this will be ignored and
	 */
	private void setThumbPos(final int h, final Drawable thumb,
			final float scale, final int gap) {
		int available = h - this.mPaddingTop - this.mPaddingBottom;
		int thumbWidth = thumb.getIntrinsicWidth();
		int thumbHeight = thumb.getIntrinsicHeight();
		available -= thumbHeight;

		// The extra space for the thumb to move on the track
		available += this.mThumbOffset * 2;
		int thumbPos = (int) ((1 - scale) * available);
		int leftBound, rightBound;
		if (gap == Integer.MIN_VALUE) {
			Rect oldBounds = thumb.getBounds();
			leftBound = oldBounds.left;
			rightBound = oldBounds.right;
		} else {
			leftBound = gap;
			rightBound = gap + thumbWidth;
		}

		// Canvas will be translated, so 0,0 is where we start drawing
		thumb.setBounds(leftBound, thumbPos, rightBound, thumbPos + thumbHeight);
	}

	@Override
	protected synchronized void onDraw(final Canvas canvas) {
		super.onDraw(canvas);
		if (this.mThumb != null) {
			canvas.save();
			// Translate the padding. For the x, we need to allow the thumb to
			// draw in its extra space
			canvas.translate(this.mPaddingLeft, this.mPaddingTop
					- this.mThumbOffset);
			this.mThumb.draw(canvas);
			canvas.restore();
		}
	}

	@Override
	protected synchronized void onMeasure(final int widthMeasureSpec,
			final int heightMeasureSpec) {
		Drawable d = this.getCurrentDrawable();

		int thumbWidth = this.mThumb == null ? 0 : this.mThumb
				.getIntrinsicWidth();
		int dw = 0;
		int dh = 0;
		if (d != null) {
			dw = Math.max(this.mMinWidth,
					Math.min(this.mMaxWidth, d.getIntrinsicWidth()));
			dw = Math.max(thumbWidth, dh);
			dh = Math.max(this.mMinHeight,
					Math.min(this.mMaxHeight, d.getIntrinsicHeight()));
		}
		dw += this.mPaddingLeft + this.mPaddingRight;
		dh += this.mPaddingTop + this.mPaddingBottom;

		this.setMeasuredDimension(resolveSize(dw, widthMeasureSpec),
				resolveSize(dh, heightMeasureSpec));
	}

	@Override
	public boolean onTouchEvent(final MotionEvent event) {
		if (!this.mIsUserSeekable || !this.isEnabled()) {
			return false;
		}

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			this.setPressed(true);
			this.onStartTrackingTouch();
			this.trackTouchEvent(event);
			break;

		case MotionEvent.ACTION_MOVE:
			this.trackTouchEvent(event);
			this.attemptClaimDrag();
			break;

		case MotionEvent.ACTION_UP:
			this.trackTouchEvent(event);
			this.onStopTrackingTouch();
			this.setPressed(false);
			// ProgressBar doesn't know to repaint the thumb drawable
			// in its inactive state when the touch stops (because the
			// value has not apparently changed)
			this.invalidate();
			break;

		case MotionEvent.ACTION_CANCEL:
			this.onStopTrackingTouch();
			this.setPressed(false);
			this.invalidate(); // see above explanation
			break;
		}
		return true;
	}

	private void trackTouchEvent(final MotionEvent event) {
		final int height = this.getHeight();
		final int available = height - this.mPaddingTop - this.mPaddingBottom;
		int y = height - (int) event.getY();
		float scale;
		float progress = 0;
		if (y < this.mPaddingBottom) {
			scale = 0.0f;
		} else if (y > height - this.mPaddingTop) {
			scale = 1.0f;
		} else {
			scale = (float) (y - this.mPaddingBottom) / (float) available;
			progress = this.mTouchProgressOffset;
		}

		final int max = this.getMax();
		progress += scale * max;

		this.setProgress((int) progress, true);
	}

	/**
	 * Tries to claim the user's drag motion, and requests disallowing any
	 * ancestors from stealing events in the drag.
	 */
	private void attemptClaimDrag() {
		if (this.mParent != null) {
			this.mParent.requestDisallowInterceptTouchEvent(true);
		}
	}

	/**
	 * This is called when the user has started touching this widget.
	 */
	void onStartTrackingTouch() {
	}

	/**
	 * This is called when the user either releases his touch or the touch is
	 * canceled.
	 */
	void onStopTrackingTouch() {
	}

	/**
	 * Called when the user changes the seekbar's progress by using a key event.
	 */
	void onKeyChange() {
	}

	@Override
	public boolean onKeyDown(final int keyCode, final KeyEvent event) {
		int progress = this.getProgress();

		switch (keyCode) {
		case KeyEvent.KEYCODE_DPAD_DOWN:
			if (progress <= 0)
				break;
			this.setProgress(progress - this.mKeyProgressIncrement, true);
			this.onKeyChange();
			return true;

		case KeyEvent.KEYCODE_DPAD_UP:
			if (progress >= this.getMax())
				break;
			this.setProgress(progress + this.mKeyProgressIncrement, true);
			this.onKeyChange();
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

}
