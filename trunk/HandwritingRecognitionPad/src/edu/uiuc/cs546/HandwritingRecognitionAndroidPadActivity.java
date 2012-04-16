package edu.uiuc.cs546;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableLayout.LayoutParams;
import android.widget.TableRow;
import android.widget.TextView;
import edu.uiuc.cs546.data.Datapoint;
import edu.uiuc.cs546.data.Stroke;

public class HandwritingRecognitionAndroidPadActivity extends Activity {
	/** Used as a pulse to gradually fade the contents of the window. */
	private static final int MSG_FADE = 1;

	/** Menu ID for the command to clear the window. */
	private static final int CLEAR_ID = Menu.FIRST;

	/** Menu ID for the command to toggle fading. */
	private static final int FADE_ID = Menu.FIRST + 1;

	/** How often to fade the contents of the window (in ms). */
	private static final int FADE_DELAY = 100;

	/** Colors to cycle through. */
	static final int[] COLORS = new int[] { Color.WHITE, Color.RED,
			Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE, Color.MAGENTA, };

	/** Background color. */
	static final int BACKGROUND_COLOR = Color.BLACK;

	/** The view responsible for drawing the window. */
	PaintView paintView;

	/** Is fading mode enabled? */
	boolean mFading;

	/** The index of the current color to use. */
	int mColorIndex;

	private TextView tvCoord;
	private TextView tvStatus;
	private TextView tvPoints;

	private List<Stroke> strokes;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		strokes = new ArrayList<Stroke>();

		TableLayout table = (TableLayout) this.findViewById(R.id.TableLayout);

		TableRow row1 = new TableRow(this);

		TextView tvTitle = new TextView(this);
		tvTitle.setText("Cursive Writing Panel");
		tvTitle.setTextSize(25);
		tvTitle.setHeight(100);
		tvTitle.setPadding(30, 0, 0, 0);
		tvTitle.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
		TextView tvCoordLabel = new TextView(this);
		tvCoordLabel.setText("Status:");

		row1.addView(tvTitle);
		row1.addView(tvCoordLabel);

		TableRow row2 = new TableRow(this);

		TextView tvHint = new TextView(this);
		tvHint.setText("Write a digit or English letter below.");
		tvHint.setTextSize(15);
		tvHint.setPadding(30, 0, 0, 10);
		tvStatus = new TextView(this);
		tvStatus.setText("Wait for writing...");

		row2.addView(tvHint);
		row2.addView(tvStatus);

		TableRow row3 = new TableRow(this);

		// Create and attach the view that is responsible for painting.
		paintView = new PaintView(this);
		paintView.setPadding(30, 30, 30, 30);

		LinearLayout result = new LinearLayout(this);
		result.setOrientation(1);
		tvCoord = new TextView(this);
		tvCoord.setText("Current coordinate:");
		tvCoord.setWidth(300);
		tvCoord.setPadding(0, 100, 0, 20);
		tvCoord.setLayoutParams(new ViewGroup.LayoutParams(
				ViewGroup.LayoutParams.FILL_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT));
		TextView tvObserveSeqLabel = new TextView(this);
		tvObserveSeqLabel.setText("HMM observation sequence:");
		tvObserveSeqLabel.setWidth(300);
		tvObserveSeqLabel.setPadding(0, 0, 0, 20);
		tvObserveSeqLabel.setLayoutParams(new ViewGroup.LayoutParams(
				ViewGroup.LayoutParams.FILL_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT));
		TextView tvObserveSeq = new TextView(this);
		tvObserveSeq.setText("[1,2,3,4,5]");
		tvObserveSeq.setHeight(100);
		tvObserveSeq.setLayoutParams(new ViewGroup.LayoutParams(
				ViewGroup.LayoutParams.FILL_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT));
		TextView tvProbs = new TextView(this);
		tvProbs.setText("p('1')=0.329\tp('a')=0.328\np('2')=0.889");
		tvProbs.setLayoutParams(new ViewGroup.LayoutParams(
				ViewGroup.LayoutParams.FILL_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT));
		tvPoints = new TextView(this);
		tvPoints.setLayoutParams(new ViewGroup.LayoutParams(
				ViewGroup.LayoutParams.FILL_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT));
		result.addView(tvCoord);
		result.addView(tvObserveSeqLabel);
		result.addView(tvObserveSeq);
		result.addView(tvProbs);
		result.addView(tvPoints);

		// getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		// WindowManager.LayoutParams.FLAG_FULLSCREEN);
		// requestWindowFeature(Window.FEATURE_NO_TITLE);

		DrawView drawView = new DrawView(this);
		drawView.requestFocus();

		row3.addView(drawView);
		row3.addView(result);

		table.addView(row1, new TableLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		table.addView(row2, new TableLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		table.addView(row3, new TableLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

		paintView.requestFocus();

		// Restore the fading option if we are being thawed from a
		// previously saved state. Note that we are not currently remembering
		// the contents of the bitmap.
		if (savedInstanceState != null) {
			mFading = savedInstanceState.getBoolean("fading", true);
			mColorIndex = savedInstanceState.getInt("color", 0);
		} else {
			mFading = true;
			mColorIndex = 0;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, CLEAR_ID, 0, "Clear");
		menu.add(0, FADE_ID, 0, "Fade").setCheckable(true);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(FADE_ID).setChecked(mFading);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case CLEAR_ID:
			paintView.clear();
			return true;
		case FADE_ID:
			mFading = !mFading;
			if (mFading) {
				startFading();
			} else {
				stopFading();
			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		// If fading mode is enabled, then as long as we are resumed we want
		// to run pulse to fade the contents.
		if (mFading) {
			startFading();
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		// Save away the fading state to restore if needed later. Note that
		// we do not currently save the contents of the display.
		outState.putBoolean("fading", mFading);
		outState.putInt("color", mColorIndex);
	}

	@Override
	protected void onPause() {
		super.onPause();

		// Make sure to never run the fading pulse while we are paused or
		// stopped.
		stopFading();
	}

	/**
	 * Start up the pulse to fade the screen, clearing any existing pulse to
	 * ensure that we don't have multiple pulses running at a time.
	 */
	void startFading() {
		mHandler.removeMessages(MSG_FADE);
		scheduleFade();
	}

	/**
	 * Stop the pulse to fade the screen.
	 */
	void stopFading() {
		mHandler.removeMessages(MSG_FADE);
	}

	/**
	 * Schedule a fade message for later.
	 */
	void scheduleFade() {
		mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_FADE),
				FADE_DELAY);
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			// Upon receiving the fade pulse, we have the view perform a
			// fade and then enqueue a new message to pulse at the desired
			// next time.
			case MSG_FADE: {
				paintView.fade();
				scheduleFade();
				tvStatus.setText("Start fading...");
				break;
			}
			// can start recognition here!!!!!!!!!!!!
			// case
			default:
				super.handleMessage(msg);
			}
		}
	};

	enum PaintMode {
		Draw, Splat, Erase,
	}

	/**
	 * This view implements the drawing canvas.
	 * 
	 * It handles all of the input events and drawing functions.
	 */
	class PaintView extends View {
		private static final int FADE_ALPHA = 0x06;
		private static final int MAX_FADE_STEPS = 256 / FADE_ALPHA + 4;
		private static final int TRACKBALL_SCALE = 10;

		private static final int SPLAT_VECTORS = 40;

		private final Random mRandom = new Random();
		private Bitmap mBitmap;
		private Canvas mCanvas;
		private final Paint mPaint;
		private final Paint mFadePaint;
		private float mCurX;
		private float mCurY;
		private int mOldButtonState;
		private int mFadeSteps = MAX_FADE_STEPS;

		public PaintView(Context c) {
			super(c);
			setFocusable(true);

			mPaint = new Paint();
			mPaint.setAntiAlias(true);

			mFadePaint = new Paint();
			// mFadePaint.setColor(BACKGROUND_COLOR);
			mFadePaint.setAlpha(FADE_ALPHA);
		}

		public void clear() {
			if (mCanvas != null) {
				mPaint.setColor(BACKGROUND_COLOR);
				mCanvas.drawPaint(mPaint);
				invalidate();

				mFadeSteps = MAX_FADE_STEPS;
			}
		}

		public void fade() {
			if (mCanvas != null && mFadeSteps < MAX_FADE_STEPS) {
				mCanvas.drawPaint(mFadePaint);
				invalidate();

				mFadeSteps++;
			}
		}

		@Override
		protected void onSizeChanged(int w, int h, int oldw, int oldh) {
			int curW = mBitmap != null ? mBitmap.getWidth() : 0;
			int curH = mBitmap != null ? mBitmap.getHeight() : 0;
			if (curW >= w && curH >= h) {
				return;
			}

			if (curW < w)
				curW = w;
			if (curH < h)
				curH = h;

			Bitmap newBitmap = Bitmap.createBitmap(curW, curH,
					Bitmap.Config.ARGB_8888);
			Canvas newCanvas = new Canvas();
			newCanvas.setBitmap(newBitmap);
			if (mBitmap != null) {
				newCanvas.drawBitmap(mBitmap, 0, 0, null);
			}
			mBitmap = newBitmap;
			mCanvas = newCanvas;
			mFadeSteps = MAX_FADE_STEPS;
		}

		@Override
		protected void onDraw(Canvas canvas) {
			if (mBitmap != null) {
				canvas.drawBitmap(mBitmap, 0, 0, null);
			}
		}

		@Override
		public boolean onTrackballEvent(MotionEvent event) {
			final int action = event.getActionMasked();
			if (action == MotionEvent.ACTION_DOWN) {
				// Advance color when the trackball button is pressed.
				advanceColor();
			}

			if (action == MotionEvent.ACTION_DOWN
					|| action == MotionEvent.ACTION_MOVE) {
				final int N = event.getHistorySize();
				final float scaleX = event.getXPrecision() * TRACKBALL_SCALE;
				final float scaleY = event.getYPrecision() * TRACKBALL_SCALE;
				for (int i = 0; i < N; i++) {
					moveTrackball(event.getHistoricalX(i) * scaleX,
							event.getHistoricalY(i) * scaleY);
				}
				moveTrackball(event.getX() * scaleX, event.getY() * scaleY);
			}
			return true;
		}

		private void moveTrackball(float deltaX, float deltaY) {
			final int curW = mBitmap != null ? mBitmap.getWidth() : 0;
			final int curH = mBitmap != null ? mBitmap.getHeight() : 0;

			mCurX = Math.max(Math.min(mCurX + deltaX, curW - 1), 0);
			mCurY = Math.max(Math.min(mCurY + deltaY, curH - 1), 0);
			paint(PaintMode.Draw, mCurX, mCurY);
		}

		@Override
		public boolean onTouchEvent(MotionEvent event) {
			return onTouchOrHoverEvent(event, true /* isTouch */);
		}

		@Override
		public boolean onHoverEvent(MotionEvent event) {
			return onTouchOrHoverEvent(event, false /* isTouch */);
		}

		private boolean onTouchOrHoverEvent(MotionEvent event, boolean isTouch) {
			final int buttonState = event.getButtonState();
			int pressedButtons = buttonState & ~mOldButtonState;
			mOldButtonState = buttonState;

			if ((pressedButtons & MotionEvent.BUTTON_SECONDARY) != 0) {
				// Advance color when the right mouse button or first stylus
				// button
				// is pressed.
				advanceColor();
			}

			PaintMode mode;
			if ((buttonState & MotionEvent.BUTTON_TERTIARY) != 0) {
				// Splat paint when the middle mouse button or second stylus
				// button is pressed.
				mode = PaintMode.Splat;
			} else if (isTouch
					|| (buttonState & MotionEvent.BUTTON_PRIMARY) != 0) {
				// Draw paint when touching or if the primary button is pressed.
				mode = PaintMode.Draw;
				tvStatus.setText("Drawing...");
			} else {
				// Otherwise, do not paint anything.
				tvStatus.setText("Waiting for drawing...");
				return false;
			}

			final int action = event.getActionMasked();
			if (action == MotionEvent.ACTION_DOWN
					|| action == MotionEvent.ACTION_MOVE
					|| action == MotionEvent.ACTION_HOVER_MOVE) {
				final int N = event.getHistorySize();
				final int P = event.getPointerCount();
				for (int i = 0; i < N; i++) {
					for (int j = 0; j < P; j++) {
						paint(getPaintModeForTool(event.getToolType(j), mode),
								event.getHistoricalX(j, i),
								event.getHistoricalY(j, i),
								event.getHistoricalPressure(j, i),
								event.getHistoricalTouchMajor(j, i),
								event.getHistoricalTouchMinor(j, i),
								event.getHistoricalOrientation(j, i),
								event.getHistoricalAxisValue(
										MotionEvent.AXIS_DISTANCE, j, i),
								event.getHistoricalAxisValue(
										MotionEvent.AXIS_TILT, j, i));
					}
				}
				Stroke stroke = new Stroke();
				String str = "";
				for (int j = 0; j < P; j++) {
					paint(getPaintModeForTool(event.getToolType(j), mode),
							event.getX(j), event.getY(j), event.getPressure(j),
							event.getTouchMajor(j), event.getTouchMinor(j),
							event.getOrientation(j),
							event.getAxisValue(MotionEvent.AXIS_DISTANCE, j),
							event.getAxisValue(MotionEvent.AXIS_TILT, j));
					str += "(" + event.getX(j) + ", " + event.getY(j) + ")\n";
				}
				mCurX = event.getX();
				mCurY = event.getY();

				int x = (int) mCurX;
				int y = (int) mCurY;
				// can i just use the historical points as strokes?

				strokes.add(stroke);
				tvCoord.setText("Current coordinate: (" + x + ", " + y
						+ "), N: " + N + " P:" + P + " strokes:"
						+ strokes.size());
				tvPoints.setText(str);
			}
			return true;
		}

		private PaintMode getPaintModeForTool(int toolType,
				PaintMode defaultMode) {
			if (toolType == MotionEvent.TOOL_TYPE_ERASER) {
				return PaintMode.Erase;
			}
			return defaultMode;
		}

		private void advanceColor() {
			mColorIndex = (mColorIndex + 1) % COLORS.length;
		}

		private void paint(PaintMode mode, float x, float y) {
			paint(mode, x, y, 1.0f, 0, 0, 0, 0, 0);
		}

		private void paint(PaintMode mode, float x, float y, float pressure,
				float major, float minor, float orientation, float distance,
				float tilt) {
			if (mBitmap != null) {
				if (major <= 0 || minor <= 0) {
					// If size is not available, use a default value.
					major = minor = 16;
				}

				switch (mode) {
				case Draw:
					mPaint.setColor(COLORS[mColorIndex]);
					mPaint.setAlpha(Math.min((int) (pressure * 128), 255));
					drawOval(mCanvas, x, y, major, minor, orientation, mPaint);
					break;

				case Erase:
					mPaint.setColor(BACKGROUND_COLOR);
					mPaint.setAlpha(Math.min((int) (pressure * 128), 255));
					drawOval(mCanvas, x, y, major, minor, orientation, mPaint);
					break;

				case Splat:
					mPaint.setColor(COLORS[mColorIndex]);
					mPaint.setAlpha(64);
					drawSplat(mCanvas, x, y, orientation, distance, tilt,
							mPaint);
					break;
				}
			}
			mFadeSteps = 0;
			invalidate();
		}

		/**
		 * Draw an oval.
		 * 
		 * When the orienation is 0 radians, orients the major axis vertically,
		 * angles less than or greater than 0 radians rotate the major axis left
		 * or right.
		 */
		private final RectF mReusableOvalRect = new RectF();

		private void drawOval(Canvas canvas, float x, float y, float major,
				float minor, float orientation, Paint paint) {
			canvas.save(Canvas.MATRIX_SAVE_FLAG);
			canvas.rotate((float) (orientation * 180 / Math.PI), x, y);
			mReusableOvalRect.left = x - minor / 2;
			mReusableOvalRect.right = x + minor / 2;
			mReusableOvalRect.top = y - major / 2;
			mReusableOvalRect.bottom = y + major / 2;
			canvas.drawOval(mReusableOvalRect, paint);
			canvas.restore();
		}

		/**
		 * Splatter paint in an area.
		 * 
		 * Chooses random vectors describing the flow of paint from a round
		 * nozzle across a range of a few degrees. Then adds this vector to the
		 * direction indicated by the orientation and tilt of the tool and
		 * throws paint at the canvas along that vector.
		 * 
		 * Repeats the process until a masterpiece is born.
		 */
		private void drawSplat(Canvas canvas, float x, float y,
				float orientation, float distance, float tilt, Paint paint) {
			float z = distance * 2 + 10;

			// Calculate the center of the spray.
			float nx = (float) (Math.sin(orientation) * Math.sin(tilt));
			float ny = (float) (-Math.cos(orientation) * Math.sin(tilt));
			float nz = (float) Math.cos(tilt);
			if (nz < 0.05) {
				return;
			}
			float cd = z / nz;
			float cx = nx * cd;
			float cy = ny * cd;

			for (int i = 0; i < SPLAT_VECTORS; i++) {
				// Make a random 2D vector that describes the direction of a
				// speck of paint
				// ejected by the nozzle in the nozzle's plane, assuming the
				// tool is
				// perpendicular to the surface.
				double direction = mRandom.nextDouble() * Math.PI * 2;
				double dispersion = mRandom.nextGaussian() * 0.2;
				double vx = Math.cos(direction) * dispersion;
				double vy = Math.sin(direction) * dispersion;
				double vz = 1;

				// Apply the nozzle tilt angle.
				double temp = vy;
				vy = temp * Math.cos(tilt) - vz * Math.sin(tilt);
				vz = temp * Math.sin(tilt) + vz * Math.cos(tilt);

				// Apply the nozzle orientation angle.
				temp = vx;
				vx = temp * Math.cos(orientation) - vy * Math.sin(orientation);
				vy = temp * Math.sin(orientation) + vy * Math.cos(orientation);

				// Determine where the paint will hit the surface.
				if (vz < 0.05) {
					continue;
				}
				float pd = (float) (z / vz);
				float px = (float) (vx * pd);
				float py = (float) (vy * pd);

				// Throw some paint at this location, relative to the center of
				// the spray.
				mCanvas.drawCircle(x + px - cx, y + py - cy, 1.0f, paint);
			}
		}
	}

	public class DrawView extends View implements OnTouchListener {
		private static final String TAG = "DrawView";

		List<Datapoint> points = new ArrayList<Datapoint>();
		Paint paint = new Paint();

		Timer timer = new Timer();

		long stopDrawTime = -1;
		Stroke stroke = new Stroke();

		public DrawView(Context context) {
			super(context);
			setFocusable(true);
			setFocusableInTouchMode(true);

			this.setOnTouchListener(this);

			paint.setColor(Color.WHITE);
			paint.setAntiAlias(true);
		}

		@Override
		public void onDraw(Canvas canvas) {

			for (Datapoint point : points) {
				canvas.drawCircle((float) point.x, (float) point.y, 10, paint);
				// Log.d(TAG, "Painting: "+point);
			}
		}

		public boolean onTouch(View view, MotionEvent event) {
			// timer.cancel();
			// timer.cancel();
			// timer.schedule(new EraseTimerTask(), 3000);
			Datapoint point = new Datapoint(event.getX(), event.getY());
			stroke.addDatapoint(point);
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
			case MotionEvent.ACTION_MOVE:
			case MotionEvent.ACTION_HOVER_MOVE:

				// if(event.getAction() != MotionEvent.ACTION_DOWN)
				// return super.onTouchEvent(event);

				points.add(point);
				invalidate();
				break;
			case MotionEvent.ACTION_UP:
				strokes.add(stroke);
				stroke = new Stroke();
				stopDrawTime = System.currentTimeMillis();
				timer.schedule(new EraseTimerTask(), 2000);
				Log.d(TAG,
						"point: " + point + " total points: " + points.size());
			}

			return true;
		}

		class EraseTimerTask extends TimerTask {
			public void run() {
				if (stopDrawTime != -1
						&& System.currentTimeMillis() - stopDrawTime >= 2000) {
					Log.d(TAG, "stroke size: " + strokes.size());
					// do recognition here
					strokes = new ArrayList<Stroke>();
					points = new ArrayList<Datapoint>();
				}
			}
		}
	}
}