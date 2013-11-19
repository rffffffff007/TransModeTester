package com.example.sensortest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import android.content.Context;
import android.util.Log;

public class RouteModeClassifier {
	private static final String TAG = "RouteModeClassifier";
	private static final String TREE_PATH = "C45_tree";
	private Context mContext;
	private C45Tree mTree;
	private PointRoute mRoute;
	private long mInterval = Config.WINDOW_SIZE / 2;;
	private OnClassifyListener mOnClassifyListener;
	private ClassifyRunnable mClassifyRunnable;

	private Executor mExecutor = Executors.newSingleThreadExecutor();

	public RouteModeClassifier(Context context) {
		mContext = context;
		mRoute = new PointRoute();
		mTree = buildTree(TREE_PATH);
	}

	private C45Tree buildTree(String path) {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(mContext
					.getResources().getAssets().open(TREE_PATH)));
			StringBuffer data = new StringBuffer();
			while (true) {
				String line = reader.readLine();
				if (line == null || line.length() == 0)
					break;
				data.append(line + "\n");
			}
			return new C45Tree(data.toString());
		} catch (IOException e) {
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
				}
			}
		}
		return null;
	}

	public PointRoute getPointRoute() {
		return mRoute;
	}

	public void start() {
		if (mClassifyRunnable == null) {
			mClassifyRunnable = new ClassifyRunnable();
			mExecutor.execute(mClassifyRunnable);
		}
	}

	public void stop() {
		if (mClassifyRunnable != null) {
			mClassifyRunnable.isStoped = true;
			mClassifyRunnable = null;
		}
	}

	private static final double STAY_POINT_THRESHOLD = 0.02;

	private class ClassifyRunnable implements Runnable {
		public boolean isStoped = false;

		public void run() {
			if (mTree != null && mRoute != null) {
				Map<String, Double> params = buildParams();
				Log.i(TAG, params.toString());
				int mode = 0;
				if (mRoute.getGAccelVariance() < STAY_POINT_THRESHOLD)
					mode = -1;
				else
					mode = mTree.classify(params);
				if (mOnClassifyListener != null)
					mOnClassifyListener.onClassify(mode);
			}
			try {
				Thread.sleep(mInterval);
			} catch (InterruptedException e) {
			}
			if (!isStoped)
				mExecutor.execute(this);
		};
	};

	private Map<String, Double> buildParams() {
		Map<String, Double> params = new LinkedHashMap<String, Double>();
		params.put("SpanVelocity", mRoute.getSpanVelocity());
		params.put("MedianVelocity", mRoute.getMedianVelocity());
		params.put("GAccelMean", mRoute.getGAccelMean());
		params.put("GAccelAbsMean", mRoute.getGAbsMean());
		params.put("GAccelEnergy", mRoute.getGAccelEnergy());
		params.put("GAccelVariance", mRoute.getGAccelVariance());
		return params;
	}

	public void setOnClassifyListener(OnClassifyListener l) {
		mOnClassifyListener = l;
	}

	public interface OnClassifyListener {
		public void onClassify(int mode);
	}
}
