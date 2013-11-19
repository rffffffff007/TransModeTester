package com.example.sensortest;

public class GSensorPoint {
	private float[] mAccels;
	private long mTime;
	private double mMagnitude;

	public GSensorPoint(float[] accels) {
		mAccels = accels;
		mTime = System.currentTimeMillis();
		float mag = 0;
		for (float a : mAccels) {
			mag += a * a;
		}
		mMagnitude = Math.sqrt(mag);
	}

	public double getMagnitude() {
		return mMagnitude;
	}

	public long getTime() {
		return mTime;
	}
}