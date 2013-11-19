package com.example.sensortest;

public enum TransMode {
	Stay(-1), Walk(0), Bike(1), Car(2), Run(3);
	private int mMode;

	private TransMode(int mode) {
		mMode = mode;
	}

	public int mode() {
		return mMode;
	}

	public static TransMode getTransMode(int mode) {
		switch (mode) {
		case 0:
			return Walk;
		case 1:
			return Bike;
		case 2:
			return Car;
		case 3:
			return Run;
		default:
			return Stay;
		}
	}
}
