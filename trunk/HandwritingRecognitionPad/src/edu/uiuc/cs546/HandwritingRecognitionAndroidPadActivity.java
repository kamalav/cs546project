package edu.uiuc.cs546;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableLayout.LayoutParams;
import android.widget.TableRow;
import android.widget.TextView;
import be.ac.ulg.montefiore.run.jahmm.ObservationInteger;
import be.ac.ulg.montefiore.run.jahmm.io.HmmBinaryReader;
import edu.uiuc.cs546.data.Datapoint;
import edu.uiuc.cs546.data.Stroke;
import edu.uiuc.cs546.data.io.HmmIOUtil;
import edu.uiuc.cs546.data.util.OberservationSequence;
import edu.uiuc.cs546.hmm.LeftToRightHmm2;
import edu.uiuc.cs546.hmm.feature.FeatureQuantizer;
import edu.uiuc.cs546.preprocess.Smoother;
import edu.uiuc.cs546.recognize.Recognizer;

public class HandwritingRecognitionAndroidPadActivity extends Activity {

	private TextView tvCoord;
	private TextView tvStatus;
	private TextView tvPoints;
	private TextView tvProbs;
	private TextView tvObservation;
	private TextView tvRecognition;

	private List<Stroke> strokes;

	private List<Datapoint> points;

	// models
	Map<String, LeftToRightHmm2> hmms;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		strokes = new ArrayList<Stroke>();
		points = new ArrayList<Datapoint>();

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

		LinearLayout result = new LinearLayout(this);
		result.setOrientation(1);
		tvCoord = new TextView(this);
		tvCoord.setText("Current coordinate:");
		tvCoord.setPadding(0, 10, 0, 20);
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
		tvObservation = new TextView(this);
		tvObservation.setWidth(300);
		tvObservation.setTextSize(10);
		tvObservation.setLayoutParams(new ViewGroup.LayoutParams(
				ViewGroup.LayoutParams.FILL_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT));
		TextView tvRecognitionLabel = new TextView(this);
		tvRecognitionLabel.setText("Written strokes were recognized as:");
		tvRecognitionLabel.setLayoutParams(new ViewGroup.LayoutParams(
				ViewGroup.LayoutParams.FILL_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT));
		tvRecognition = new TextView(this);
		tvRecognition.setTextSize(30);
		tvRecognition.setTextColor(Color.RED);
		tvRecognition.setLayoutParams(new ViewGroup.LayoutParams(
				ViewGroup.LayoutParams.FILL_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT));
		tvProbs = new TextView(this);
		tvProbs.setTextSize(10);
		tvProbs.setLayoutParams(new ViewGroup.LayoutParams(
				ViewGroup.LayoutParams.FILL_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT));
		tvPoints = new TextView(this);
		tvPoints.setTextSize(10);
		tvPoints.setLayoutParams(new ViewGroup.LayoutParams(
				ViewGroup.LayoutParams.FILL_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT));
		result.addView(tvRecognition);
		result.addView(tvCoord);
		result.addView(tvObserveSeqLabel);
		result.addView(tvObservation);
		result.addView(tvRecognitionLabel);
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

		// read hmms
		hmms = new TreeMap<String, LeftToRightHmm2>();
		String[] characters = { "0", "1", "2", "3", "4", "5", "6", "7", "8",
				"9" };
		int[] modelHandles = { R.raw.hmm0, R.raw.hmm1, R.raw.hmm2, R.raw.hmm3,
				R.raw.hmm4, R.raw.hmm5, R.raw.hmm6, R.raw.hmm7, R.raw.hmm8,
				R.raw.hmm9 };
		for (int i = 0; i < characters.length; i++) {
			String character = characters[i];
			int handle = modelHandles[i];
			InputStream dis;
			try {
				dis = getResources().openRawResource(handle);
				LeftToRightHmm2 hmm = new LeftToRightHmm2(
						HmmBinaryReader.read(dis));
				hmms.put(character, hmm);
				// Log.d("reading hmms", hmm.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}

			FeatureQuantizer.prototypeVectors = HmmIOUtil
					.readSavedFeatureVectors(HmmIOUtil.readRawTextFile(
							getBaseContext(), R.raw.vectors));
		}
	}

	public class DrawView extends View implements OnTouchListener {
		private static final String TAG = "DrawView";

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
			final int x = (int) event.getX();
			final int y = (int) event.getY();
			Datapoint point = new Datapoint(x, y);
			stroke.addDatapoint(point);
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				tvStatus.setText("Writing...");
			case MotionEvent.ACTION_MOVE:
			case MotionEvent.ACTION_HOVER_MOVE:

				// if(event.getAction() != MotionEvent.ACTION_DOWN)
				// return super.onTouchEvent(event);

				runOnUiThread(new Runnable() {
					public void run() {
						tvCoord.setText("Current coordinate: (" + x + "," + y
								+ ")");
					}
				});

				points.add(point);
				invalidate();
				break;
			case MotionEvent.ACTION_UP:
				strokes.add(stroke);
				stroke = new Stroke();
				stopDrawTime = System.currentTimeMillis();
				timer.schedule(new RecognitionTimerTask(), 3000);
				Log.d(TAG,
						"point: " + point + " total points: " + points.size());
			}

			return true;
		}

		class RecognitionTimerTask extends TimerTask {
			public void run() {
				if (stopDrawTime != -1
						&& System.currentTimeMillis() - stopDrawTime >= 3000) {
					Log.d(TAG, "stroke size: " + strokes.size());

					runOnUiThread(new Runnable() {
						public void run() {
							recognize();
						}
					});

				}
			}
		}
	}

	private void recognize() {
		// update status
		tvStatus.setText("Recognizing...");

		Log.d("Recognizer", "stroke 0: " + strokes.get(0));

		// preprocess
		strokes = Stroke.connectStrokes(strokes);
		strokes = Stroke.makeYNegative(strokes);
		// strokes = DatapointSampler.inStrokeSampling(strokes, 3, false);
		strokes = Smoother.noiseSmoothing(strokes);

		// recognize
		List<ObservationInteger> sequence = OberservationSequence
				.fromStrokes(strokes);
		StringBuffer probs = new StringBuffer("");
		String recognizedStr = Recognizer.recognize(hmms, sequence, probs);

		// show result
		tvStatus.setText("Recognized. See the result below.");
		tvRecognition.setText(recognizedStr);
		tvObservation.setText(sequence.toString());
		tvProbs.setText(probs.toString());

		// clean up
		strokes = new ArrayList<Stroke>();
		points = new ArrayList<Datapoint>();
	}
}