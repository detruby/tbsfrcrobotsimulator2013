/* Copyright @ 2013 by Dave Truby. All rights reserved. */
package com.tbs.widgit;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.graphics.drawable.shapes.Shape;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewDebug;
import android.view.ViewParent;
import android.widget.ProgressBar;
import android.widget.RemoteViews.RemoteView;

import com.tbs.frc.robotsim.R;

@RemoteView
public class VerticalProgressBar extends View {

	private static final int MAX_LEVEL = 10000;

	int mMinWidth;
	int mMaxWidth;
	int mMinHeight;
	int mMaxHeight;

	private int mProgress;
	private int mSecondaryProgress;
	private int mMax;

	private Drawable mProgressDrawable;
	private Drawable mCurrentDrawable;
	Bitmap mSampleTile;
	private boolean mNoInvalidate;
	private RefreshProgressRunnable mRefreshProgressRunnable;
	private long mUiThreadId;

	private boolean mInDrawing;

	protected int mScrollX;
	protected int mScrollY;
	protected int mPaddingLeft;
	protected int mPaddingRight;
	protected int mPaddingTop;
	protected int mPaddingBottom;
	protected ViewParent mParent;

	/**
	 * Create a new progress bar with range 0...100 and initial progress of 0.
	 * 
	 * @param context
	 *            the application environment
	 */
	public VerticalProgressBar(final Context context) {
		this(context, null);
	}

	public VerticalProgressBar(final Context context, final AttributeSet attrs) {
		this(context, attrs, android.R.attr.progressBarStyle);
	}

	public VerticalProgressBar(final Context context, final AttributeSet attrs,
			final int defStyle) {
		super(context, attrs, defStyle);
		this.mUiThreadId = Thread.currentThread().getId();
		this.initProgressBar();

		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.ProgressBar, defStyle, 0);

		this.mNoInvalidate = true;

		Drawable drawable = a
				.getDrawable(R.styleable.ProgressBar_android_progressDrawable);
		if (drawable != null) {
			drawable = this.tileify(drawable, false);
			// Calling this method can set mMaxHeight, make sure the
			// corresponding
			// XML attribute for mMaxHeight is read after calling this method
			this.setProgressDrawable(drawable);
		}

		this.mMinWidth = a.getDimensionPixelSize(
				R.styleable.ProgressBar_android_minWidth, this.mMinWidth);
		this.mMaxWidth = a.getDimensionPixelSize(
				R.styleable.ProgressBar_android_maxWidth, this.mMaxWidth);
		this.mMinHeight = a.getDimensionPixelSize(
				R.styleable.ProgressBar_android_minHeight, this.mMinHeight);
		this.mMaxHeight = a.getDimensionPixelSize(
				R.styleable.ProgressBar_android_maxHeight, this.mMaxHeight);

		this.setMax(a.getInt(R.styleable.ProgressBar_android_max, this.mMax));

		this.setProgress(a.getInt(R.styleable.ProgressBar_android_progress,
				this.mProgress));

		this.setSecondaryProgress(a.getInt(
				R.styleable.ProgressBar_android_secondaryProgress,
				this.mSecondaryProgress));

		this.mNoInvalidate = false;

		a.recycle();
	}

	/**
	 * Converts a drawable to a tiled version of itself. It will recursively
	 * traverse layer and state list drawables.
	 */
	private Drawable tileify(final Drawable drawable, final boolean clip) {

		if (drawable instanceof LayerDrawable) {
			LayerDrawable background = (LayerDrawable) drawable;
			final int N = background.getNumberOfLayers();
			Drawable[] outDrawables = new Drawable[N];

			for (int i = 0; i < N; i++) {
				int id = background.getId(i);
				outDrawables[i] = this
						.tileify(
								background.getDrawable(i),
								(id == android.R.id.progress || id == android.R.id.secondaryProgress));
			}

			LayerDrawable newBg = new LayerDrawable(outDrawables);

			for (int i = 0; i < N; i++) {
				newBg.setId(i, background.getId(i));
			}

			return newBg;

		} else if (drawable instanceof StateListDrawable) {

			StateListDrawable out = new StateListDrawable();

			return out;

		} else if (drawable instanceof BitmapDrawable) {
			final Bitmap tileBitmap = ((BitmapDrawable) drawable).getBitmap();
			if (this.mSampleTile == null) {
				this.mSampleTile = tileBitmap;
			}

			final ShapeDrawable shapeDrawable = new ShapeDrawable(
					this.getDrawableShape());
			return (clip) ? new ClipDrawable(shapeDrawable, Gravity.LEFT,
					ClipDrawable.HORIZONTAL) : shapeDrawable;
		}

		return drawable;
	}

	Shape getDrawableShape() {
		final float[] roundedCorners = new float[] { 5, 5, 5, 5, 5, 5, 5, 5 };
		return new RoundRectShape(roundedCorners, null, null);
	}

	/**
	 * <p>
	 * Initialize the progress bar's default values:
	 * </p>
	 * <ul>
	 * <li>progress = 0</li>
	 * <li>max = 100</li>
	 * </ul>
	 */
	private void initProgressBar() {
		this.mMax = 100;
		this.mProgress = 0;
		this.mSecondaryProgress = 0;
		this.mMinWidth = 24;
		this.mMaxWidth = 48;
		this.mMinHeight = 24;
		this.mMaxHeight = 48;
	}

	/**
	 * <p>
	 * Get the drawable used to draw the progress bar in progress mode.
	 * </p>
	 * 
	 * @return a {@link android.graphics.drawable.Drawable} instance
	 * 
	 * @see #setProgressDrawable(android.graphics.drawable.Drawable)
	 */
	public Drawable getProgressDrawable() {
		return this.mProgressDrawable;
	}

	/**
	 * <p>
	 * Define the drawable used to draw the progress bar in progress mode.
	 * </p>
	 * 
	 * @param d
	 *            the new drawable
	 * 
	 * @see #getProgressDrawable()
	 */
	public void setProgressDrawable(final Drawable d) {
		if (d != null) {
			d.setCallback(this);
			// Make sure the ProgressBar is always tall enough
			int drawableHeight = d.getMinimumHeight();
			if (this.mMaxHeight < drawableHeight) {
				this.mMaxHeight = drawableHeight;
				this.requestLayout();
			}
		}
		this.mProgressDrawable = d;
		this.mCurrentDrawable = d;
		this.postInvalidate();
	}

	/**
	 * @return The drawable currently used to draw the progress bar
	 */
	Drawable getCurrentDrawable() {
		return this.mCurrentDrawable;
	}

	@Override
	protected boolean verifyDrawable(final Drawable who) {
		return who == this.mProgressDrawable || super.verifyDrawable(who);
	}

	@Override
	public void postInvalidate() {
		if (!this.mNoInvalidate) {
			super.postInvalidate();
		}
	}

	private class RefreshProgressRunnable implements Runnable {

		private int mId;
		private int mProgress;
		private boolean mFromUser;

		RefreshProgressRunnable(final int id, final int progress,
				final boolean fromUser) {
			this.mId = id;
			this.mProgress = progress;
			this.mFromUser = fromUser;
		}

		@Override
		public void run() {
			VerticalProgressBar.this.doRefreshProgress(this.mId,
					this.mProgress, this.mFromUser);
			// Put ourselves back in the cache when we are done
			VerticalProgressBar.this.mRefreshProgressRunnable = this;
		}

		public void setup(final int id, final int progress,
				final boolean fromUser) {
			this.mId = id;
			this.mProgress = progress;
			this.mFromUser = fromUser;
		}

	}

	private synchronized void doRefreshProgress(final int id,
			final int progress, final boolean fromUser) {
		float scale = this.mMax > 0 ? (float) progress / (float) this.mMax : 0;
		final Drawable d = this.mCurrentDrawable;
		if (d != null) {
			Drawable progressDrawable = null;

			if (d instanceof LayerDrawable) {
				progressDrawable = ((LayerDrawable) d)
						.findDrawableByLayerId(id);
			}

			final int level = (int) (scale * MAX_LEVEL);
			(progressDrawable != null ? progressDrawable : d).setLevel(level);
		} else {
			this.invalidate();
		}

		if (id == android.R.id.progress) {
			this.onProgressRefresh(scale, fromUser);
		}
	}

	void onProgressRefresh(final float scale, final boolean fromUser) {
	}

	private synchronized void refreshProgress(final int id, final int progress,
			final boolean fromUser) {
		if (this.mUiThreadId == Thread.currentThread().getId()) {
			this.doRefreshProgress(id, progress, fromUser);
		} else {
			RefreshProgressRunnable r;
			if (this.mRefreshProgressRunnable != null) {
				// Use cached RefreshProgressRunnable if available
				r = this.mRefreshProgressRunnable;
				// Uncache it
				this.mRefreshProgressRunnable = null;
				r.setup(id, progress, fromUser);
			} else {
				// Make a new one
				r = new RefreshProgressRunnable(id, progress, fromUser);
			}
			this.post(r);
		}
	}

	/**
	 * <p>
	 * Set the current progress to the specified value.
	 * </p>
	 * 
	 * @param progress
	 *            the new progress, between 0 and {@link #getMax()}
	 * 
	 * @see #getProgress()
	 * @see #incrementProgressBy(int)
	 */
	public synchronized void setProgress(final int progress) {
		this.setProgress(progress, false);
	}

	synchronized void setProgress(int progress, final boolean fromUser) {
		if (progress < 0) {
			progress = 0;
		}

		if (progress > this.mMax) {
			progress = this.mMax;
		}

		if (progress != this.mProgress) {
			this.mProgress = progress;
			this.refreshProgress(android.R.id.progress, this.mProgress,
					fromUser);
		}
	}

	/**
	 * <p>
	 * Set the current secondary progress to the specified value.
	 * </p>
	 * 
	 * @param secondaryProgress
	 *            the new secondary progress, between 0 and {@link #getMax()}
	 * @see #getSecondaryProgress()
	 * @see #incrementSecondaryProgressBy(int)
	 */
	public synchronized void setSecondaryProgress(int secondaryProgress) {
		if (secondaryProgress < 0) {
			secondaryProgress = 0;
		}

		if (secondaryProgress > this.mMax) {
			secondaryProgress = this.mMax;
		}

		if (secondaryProgress != this.mSecondaryProgress) {
			this.mSecondaryProgress = secondaryProgress;
			this.refreshProgress(android.R.id.secondaryProgress,
					this.mSecondaryProgress, false);
		}
	}

	/**
	 * <p>
	 * Get the progress bar's current level of progress.
	 * </p>
	 * 
	 * @return the current progress, between 0 and {@link #getMax()}
	 * 
	 * @see #setProgress(int)
	 * @see #setMax(int)
	 * @see #getMax()
	 */
	@ViewDebug.ExportedProperty
	public synchronized int getProgress() {
		return this.mProgress;
	}

	/**
	 * <p>
	 * Get the progress bar's current level of secondary progress.
	 * </p>
	 * 
	 * @return the current secondary progress, between 0 and {@link #getMax()}
	 * 
	 * @see #setSecondaryProgress(int)
	 * @see #setMax(int)
	 * @see #getMax()
	 */
	@ViewDebug.ExportedProperty
	public synchronized int getSecondaryProgress() {
		return this.mSecondaryProgress;
	}

	/**
	 * <p>
	 * Return the upper limit of this progress bar's range.
	 * </p>
	 * 
	 * @return a positive integer
	 * 
	 * @see #setMax(int)
	 * @see #getProgress()
	 * @see #getSecondaryProgress()
	 */
	@ViewDebug.ExportedProperty
	public synchronized int getMax() {
		return this.mMax;
	}

	/**
	 * <p>
	 * Set the range of the progress bar to 0...<tt>max</tt>.
	 * </p>
	 * 
	 * @param max
	 *            the upper range of this progress bar
	 * 
	 * @see #getMax()
	 * @see #setProgress(int)
	 * @see #setSecondaryProgress(int)
	 */
	public synchronized void setMax(int max) {
		if (max < 0) {
			max = 0;
		}
		if (max != this.mMax) {
			this.mMax = max;
			this.postInvalidate();

			if (this.mProgress > max) {
				this.mProgress = max;
				this.refreshProgress(android.R.id.progress, this.mProgress,
						false);
			}
		}
	}

	/**
	 * <p>
	 * Increase the progress bar's progress by the specified amount.
	 * </p>
	 * 
	 * @param diff
	 *            the amount by which the progress must be increased
	 * 
	 * @see #setProgress(int)
	 */
	public synchronized final void incrementProgressBy(final int diff) {
		this.setProgress(this.mProgress + diff);
	}

	/**
	 * <p>
	 * Increase the progress bar's secondary progress by the specified amount.
	 * </p>
	 * 
	 * @param diff
	 *            the amount by which the secondary progress must be increased
	 * 
	 * @see #setSecondaryProgress(int)
	 */
	public synchronized final void incrementSecondaryProgressBy(final int diff) {
		this.setSecondaryProgress(this.mSecondaryProgress + diff);
	}

	@Override
	public void setVisibility(final int v) {
		if (this.getVisibility() != v) {
			super.setVisibility(v);
		}
	}

	@Override
	public void invalidateDrawable(final Drawable dr) {
		if (!this.mInDrawing) {
			if (this.verifyDrawable(dr)) {
				final Rect dirty = dr.getBounds();
				final int scrollX = this.mScrollX + this.mPaddingLeft;
				final int scrollY = this.mScrollY + this.mPaddingTop;

				this.invalidate(dirty.left + scrollX, dirty.top + scrollY,
						dirty.right + scrollX, dirty.bottom + scrollY);
			} else {
				super.invalidateDrawable(dr);
			}
		}
	}

	@Override
	protected void onSizeChanged(final int w, final int h, final int oldw,
			final int oldh) {
		// onDraw will translate the canvas so we draw starting at 0,0
		int right = w - this.mPaddingRight - this.mPaddingLeft;
		int bottom = h - this.mPaddingBottom - this.mPaddingTop;

		if (this.mProgressDrawable != null) {
			this.mProgressDrawable.setBounds(0, 0, right, bottom);
		}
	}

	@Override
	protected synchronized void onDraw(final Canvas canvas) {
		super.onDraw(canvas);

		Drawable d = this.mCurrentDrawable;
		if (d != null) {
			// Translate canvas so a indeterminate circular progress bar with
			// padding
			// rotates properly in its animation
			canvas.save();
			canvas.translate(this.mPaddingLeft, this.mPaddingTop);
			d.draw(canvas);
			canvas.restore();
		}
	}

	@Override
	protected synchronized void onMeasure(final int widthMeasureSpec,
			final int heightMeasureSpec) {
		Drawable d = this.mCurrentDrawable;

		int dw = 0;
		int dh = 0;
		if (d != null) {
			dw = Math.max(this.mMinWidth,
					Math.min(this.mMaxWidth, d.getIntrinsicWidth()));
			dh = Math.max(this.mMinHeight,
					Math.min(this.mMaxHeight, d.getIntrinsicHeight()));
		}
		dw += this.mPaddingLeft + this.mPaddingRight;
		dh += this.mPaddingTop + this.mPaddingBottom;

		this.setMeasuredDimension(resolveSize(dw, widthMeasureSpec),
				resolveSize(dh, heightMeasureSpec));
	}

	@Override
	protected void drawableStateChanged() {
		super.drawableStateChanged();

		int[] state = this.getDrawableState();

		if (this.mProgressDrawable != null
				&& this.mProgressDrawable.isStateful()) {
			this.mProgressDrawable.setState(state);
		}
	}

	static class SavedState extends BaseSavedState {
		int progress;
		int secondaryProgress;

		/**
		 * Constructor called from {@link ProgressBar#onSaveInstanceState()}
		 */
		SavedState(final Parcelable superState) {
			super(superState);
		}

		/**
		 * Constructor called from {@link #CREATOR}
		 */
		private SavedState(final Parcel in) {
			super(in);
			this.progress = in.readInt();
			this.secondaryProgress = in.readInt();
		}

		@Override
		public void writeToParcel(final Parcel out, final int flags) {
			super.writeToParcel(out, flags);
			out.writeInt(this.progress);
			out.writeInt(this.secondaryProgress);
		}

		public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
			@Override
			public SavedState createFromParcel(final Parcel in) {
				return new SavedState(in);
			}

			@Override
			public SavedState[] newArray(final int size) {
				return new SavedState[size];
			}
		};
	}

	@Override
	public Parcelable onSaveInstanceState() {
		// Force our ancestor class to save its state
		Parcelable superState = super.onSaveInstanceState();
		SavedState ss = new SavedState(superState);

		ss.progress = this.mProgress;
		ss.secondaryProgress = this.mSecondaryProgress;

		return ss;
	}

	@Override
	public void onRestoreInstanceState(final Parcelable state) {
		SavedState ss = (SavedState) state;
		super.onRestoreInstanceState(ss.getSuperState());

		this.setProgress(ss.progress);
		this.setSecondaryProgress(ss.secondaryProgress);
	}

}
